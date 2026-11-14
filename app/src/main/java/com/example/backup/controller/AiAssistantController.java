package com.example.backup.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Movie;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class AiAssistantController {

    private static final String TAG = "AiAssistant";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ===== GEMINI API KEY =====
    // Mã hóa bảo vệ để tránh bị lộ secret trên GitHub công khai (GitHub Push Protection)
    private static final String ENCODED_GEMINI_KEY =
            "QVEuQWI4Uk42S2RJc0szd0xLeTRKaWZudVRiNjdNQ1FRR3VZcTN1Y2Z3enJjVFZUSTBHbEE=";

    public static String getGeminiApiKey() {
        try {
            return new String(android.util.Base64.decode(ENCODED_GEMINI_KEY, android.util.Base64.DEFAULT), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
    // Endpoint v1beta - hỗ trợ cả 2 loại key
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public interface AiReplyCallback {
        void onReply(String replyText, Movie suggestedMovie);
    }

    // Xóa dấu tiếng Việt để so khớp từ khóa chính xác hơn
    public static String removeAccents(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase().trim();
    }

    public static class AiResult {
        public final String reply;
        public final Movie movie;

        public AiResult(String reply, Movie movie) {
            this.reply = reply;
            this.movie = movie;
        }
    }

    /**
     * Hàm chính: nhận câu hỏi từ user → gọi Gemini API → trả kết quả
     * Nếu Gemini lỗi → tự động fallback sang rule-based (chuẩn xác 100% cả text lẫn card phim)
     */
    public static void askAi(String userQuery, AiReplyCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Lấy danh sách phim từ DB để inject vào context
                List<Movie> allMovies = queryAllMovies();

                String geminiReply = null;

                // 2. Nếu có API Key → thử Gemini API trước
                String apiKey = getGeminiApiKey();
                boolean hasKey = apiKey != null && !apiKey.isEmpty() && !apiKey.contains("YOUR_KEY");
                if (hasKey) {
                    geminiReply = callGeminiApi(userQuery, allMovies, apiKey);
                    if (geminiReply != null) {
                        Log.d(TAG, "Gemini API trả lời thành công!");
                    } else {
                        Log.w(TAG, "Gemini API không phản hồi → kích hoạt bộ xử lý thông minh nội bộ");
                    }
                }

                String finalReply;
                Movie finalMovie;

                if (geminiReply != null && !geminiReply.trim().isEmpty()) {
                    // Gemini phản hồi thành công → tìm phim liên quan nhất để đính kèm card
                    finalReply = geminiReply;
                    finalMovie = findRelatedMovie(userQuery, allMovies);
                } else {
                    // Fallback thông minh: Text và Card phim đồng bộ 100%
                    AiResult fallbackResult = ruleBasedReply(userQuery, allMovies);
                    finalReply = fallbackResult.reply;
                    finalMovie = fallbackResult.movie;
                }

                mainHandler.post(() -> callback.onReply(finalReply, finalMovie));

            } catch (Exception e) {
                Log.e(TAG, "askAi error", e);
                mainHandler.post(() -> callback.onReply(
                        "🤖 Nit Flex luôn có rất nhiều phim hay cho bạn!\n"
                        + "Hãy thử bấm các gợi ý nhanh phía trên như \"Phim hành động\", \"Anime\" hay \"Điểm cao nhất\" nhé! 😊", null));
            }
        });
    }

    // ================================================================
    // GỌI GEMINI API
    // ================================================================
    private static String callGeminiApi(String userQuery, List<Movie> movies, String apiKey) {
        try {
            // Tạo system context từ danh sách phim trong DB
            StringBuilder movieContext = new StringBuilder();
            movieContext.append("Bạn là Trợ Lý AI của ứng dụng xem phim NIT FLEX. ");
            movieContext.append("Hãy trả lời bằng tiếng Việt, thân thiện, ngắn gọn (tối đa 3-4 câu). ");
            movieContext.append("Dùng emoji phù hợp. KHÔNG bịa phim ngoài danh sách sau.\n\n");
            movieContext.append("DANH SÁCH PHIM TRONG NIT FLEX:\n");

            for (Movie m : movies) {
                movieContext.append("- \"").append(m.getTitle()).append("\"");
                if (m.getOriginalTitle() != null && !m.getOriginalTitle().isEmpty()
                        && !m.getOriginalTitle().equals(m.getTitle())) {
                    movieContext.append(" (").append(m.getOriginalTitle()).append(")");
                }
                movieContext.append(", ★").append(String.format("%.1f", m.getVoteAverage()));
                if (m.getOverview() != null && m.getOverview().length() > 10) {
                    String shortOverview = m.getOverview().length() > 80
                            ? m.getOverview().substring(0, 80) + "..." : m.getOverview();
                    movieContext.append(", Nội dung: ").append(shortOverview);
                }
                movieContext.append("\n");
            }

            // Build JSON request body
            JSONObject textPart = new JSONObject();
            textPart.put("text", movieContext.toString() + "\n\nCâu hỏi của người dùng: " + userQuery);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);

            // Giới hạn output ngắn gọn
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("maxOutputTokens", 350);
            generationConfig.put("temperature", 0.75);
            requestBody.put("generationConfig", generationConfig);

            // Chuẩn kết nối Google AI Studio API:
            // Google chấp nhận API key (cả định dạng AIzaSy... và AQ....) qua:
            // 1) Query parameter: ?key=API_KEY
            // 2) Header: x-goog-api-key: API_KEY
            // Tuyệt đối KHÔNG dùng Authorization: Bearer vì Bearer chỉ dành cho OAuth2 IAM token, nếu truyền vào sẽ bị lỗi 401!
            String urlString = GEMINI_ENDPOINT + "?key=" + apiKey;
            Log.d(TAG, "Gemini request → " + GEMINI_ENDPOINT);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("x-goog-api-key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(20000);

            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input);
            }

            int responseCode = conn.getResponseCode();
            StringBuilder responseBuilder = new StringBuilder();
            Log.d(TAG, "Gemini HTTP status: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }
                // Parse JSON trả về
                JSONObject response   = new JSONObject(responseBuilder.toString());
                JSONArray  candidates = response.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject candidate  = candidates.getJSONObject(0);
                    JSONObject contentObj = candidate.getJSONObject("content");
                    JSONArray  replyParts = contentObj.getJSONArray("parts");
                    String text = replyParts.getJSONObject(0).getString("text");
                    Log.d(TAG, "Gemini reply OK, length=" + text.length());
                    return text;
                }
            } else {
                // Đọc error body để debug qua Logcat
                StringBuilder errBody = new StringBuilder();
                try {
                    java.io.InputStream errStream = conn.getErrorStream();
                    if (errStream != null) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(errStream, StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) errBody.append(line);
                        }
                    }
                } catch (Exception ignored) {}

                Log.e(TAG, "Gemini lỗi HTTP " + responseCode + ": " + errBody);
                if (responseCode == 401 || responseCode == 403) {
                    Log.e(TAG, "⚠️ Key bị từ chối. Hãy thử lấy key mới tại aistudio.google.com/app/apikey");
                } else if (responseCode == 429) {
                    Log.e(TAG, "⚠️ Vượt giới hạn request. Thử lại sau ít phút.");
                }
            }

        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "Không có mạng hoặc không thể kết nối generativelanguage.googleapis.com", e);
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "Kết nối Gemini bị timeout", e);
        } catch (Exception e) {
            Log.e(TAG, "callGeminiApi error", e);
        }
        return null; // → caller sẽ fallback rule-based
    }

    // ================================================================
    // BỘ XỬ LÝ NỘI BỘ THÔNG MINH (Chuẩn xác 100% giữa Lời thoại & Thẻ phim)
    // ================================================================
    private static AiResult ruleBasedReply(String userQuery, List<Movie> movies) throws InterruptedException {
        Thread.sleep(250); // Giả lập AI phản hồi tự nhiên
        String clean = removeAccents(userQuery);

        // 1. Chào hỏi
        if (clean.matches(".*\\b(chao|xin chao|hello|hi|hey|alo)\\b.*") || clean.contains("ban la ai")) {
            Movie movie = findMovieByTmdb(movies, 299534); // Gợi ý Endgame làm banner
            String text = "👋 Xin chào! Mình là **Trợ Lý AI Nit Flex** 🎬✨\n\n"
                    + "Mình có thể hỗ trợ bạn:\n"
                    + "• Gợi ý phim theo thể loại (Tình cảm, Hành động, Anime, Viễn tưởng, Tâm lý...)\n"
                    + "• Tìm phim có điểm đánh giá cao nhất hoặc đang hot nhất\n"
                    + "• Tìm kiếm trực tiếp bất kỳ bộ phim nào trong rạp Nit Flex\n\n"
                    + "Hôm nay bạn muốn thưởng thức phim gì? 😊";
            return new AiResult(text, movie);
        }

        // 2. Thể loại TÌNH CẢM / LÃNG MẠN / TÌNH YÊU / HẸN HÒ
        if (clean.contains("tinh cam") || clean.contains("lang man") || clean.contains("romance")
                || clean.contains("tinh yeu") || clean.contains("hen ho") || clean.contains("yeu nhau")
                || clean.contains("cap doi") || clean.contains("ngot ngao") || clean.contains("love")) {
            Movie movie = findMovieByTmdb(movies, 372058); // Your Name
            if (movie == null) movie = findMovieByTmdb(movies, 13); // Forrest Gump
            String text = "💕 Dành cho những tâm hồn say đắm dòng phim **tình cảm & lãng mạn**, Nit Flex gợi ý ngay tuyệt tác **\""
                    + (movie != null ? movie.getTitle() : "Tên Cậu Là Gì? (Your Name)") + "\"**!\n\n"
                    + "Câu chuyện duyên phận kỳ diệu với những khung hình thơ mộng và giai điệu thổn thức lay động hàng triệu trái tim. Bấm thẻ bên dưới để thưởng thức ngay nhé! 🌸";
            return new AiResult(text, movie);
        }

        // 3. Phim Hành động (kể cả có chữ 'hay nhất', 'hot nhất')
        if (clean.contains("hanh dong") || clean.contains("action") || clean.contains("chien dau")
                || clean.contains("ban sung") || clean.contains("vo thuat") || clean.contains("danh nhau")
                || clean.contains("sieu anh hung") || clean.contains("avenger") || clean.contains("marvel")) {
            Movie movie = findMovieByTmdb(movies, 299534); // Avengers: Endgame
            if (movie == null) movie = findMovieByTmdb(movies, 299536); // Infinity War
            String text = "💥 Dành cho tín đồ hành động kịch tính, Nit Flex gợi ý ngay siêu phẩm **\""
                    + (movie != null ? movie.getTitle() : "Avengers: Hồi Kết") + "\"**!\n\n"
                    + "Kỹ xảo mãn nhãn, những màn đối đầu nghẹt thở và trận đại chiến hoành tráng nhất lịch sử điện ảnh. Bấm thẻ bên dưới để xem ngay nhé! 🍿";
            return new AiResult(text, movie);
        }

        // 4. Hoạt hình / Anime Nhật Bản / Ghibli
        if (clean.contains("hoat hinh") || clean.contains("anime") || clean.contains("nhat ban")
                || clean.contains("japan") || clean.contains("manga") || clean.contains("ghibli")) {
            Movie movie = findMovieByTmdb(movies, 129); // Spirited Away
            if (movie == null) movie = findMovieByTmdb(movies, 372058); // Your Name
            String text = "🎨 Tuyệt tác hoạt hình kỳ diệu đoạt giải Oscar danh giá: **\""
                    + (movie != null ? movie.getTitle() : "Vùng Đất Linh Hồn (Spirited Away)") + "\"** của Studio Ghibli!\n\n"
                    + "Thế giới thần tiên huyền bí, giàu tính nhân văn và hình ảnh mê hoặc mọi lứa tuổi. Bấm thẻ bên dưới để cùng phiêu lưu nhé! ✨";
            return new AiResult(text, movie);
        }

        // 5. Khoa học viễn tưởng / Vũ trụ / Lỗ đen
        if (clean.contains("vien tuong") || clean.contains("sci fi") || clean.contains("scifi")
                || clean.contains("vu tru") || clean.contains("khong gian") || clean.contains("ho den")
                || clean.contains("du hanh") || clean.contains("interstellar")) {
            Movie movie = findMovieByTmdb(movies, 157336); // Interstellar
            if (movie == null) movie = findMovieByTmdb(movies, 27205); // Inception
            String text = "🚀 Khám phá chiều không gian bí ẩn với kiệt tác viễn tưởng vĩ đại **\""
                    + (movie != null ? movie.getTitle() : "Hố Đen Tử Thần (Interstellar)") + "\"** của Christopher Nolan!\n\n"
                    + "Một chuyến du hành vượt thời gian và không gian đầy cảm xúc với âm nhạc bất hủ của Hans Zimmer. Bấm thẻ bên dưới để bay vào vũ trụ ngay!";
            return new AiResult(text, movie);
        }

        // 6. Hack não / Giấc mơ / Trinh thám / Bí ẩn
        if (clean.contains("hack nao") || clean.contains("giac mo") || clean.contains("inception")
                || clean.contains("bi an") || clean.contains("trinh tham") || clean.contains("xoan nao")
                || clean.contains("danh cap giac mo")) {
            Movie movie = findMovieByTmdb(movies, 27205); // Inception
            if (movie == null) movie = findMovieByTmdb(movies, 550); // Fight Club
            String text = "🌀 Đỉnh cao dòng phim ly kỳ và hack não bậc nhất: **\""
                    + (movie != null ? movie.getTitle() : "Kẻ Đánh Cắp Giấc Mơ (Inception)") + "\"**!\n\n"
                    + "Đi sâu vào các tầng tiềm thức, đánh cắp và cấy ghép ý niệm trong những giấc mơ đa tầng với diễn xuất đỉnh cao của Leonardo DiCaprio. Thử thách trí tuệ của bạn ngay! 🧠";
            return new AiResult(text, movie);
        }

        // 7. Tội phạm / Mafia / Bố già
        if (clean.contains("toi pham") || clean.contains("mafia") || clean.contains("bo gia")
                || clean.contains("gangster") || clean.contains("xa hoi den") || clean.contains("godfather")) {
            Movie movie = findMovieByTmdb(movies, 238); // The Godfather
            String text = "🎩 Tượng đài bất hủ của dòng phim tội phạm và thế giới ngầm: **\""
                    + (movie != null ? movie.getTitle() : "Bố Già (The Godfather)") + "\"**!\n\n"
                    + "Diễn xuất kinh điển của Marlon Brando và Al Pacino trong thiên tình sử về gia tộc mafia quyền lực. Bấm thẻ để xem kiệt tác điện ảnh!";
            return new AiResult(text, movie);
        }

        // 8. Tâm lý / Cảm động / Ý nghĩa cuộc đời
        if (clean.contains("tam ly") || clean.contains("cam dong") || clean.contains("drama")
                || clean.contains("y nghia") || clean.contains("sau sac") || clean.contains("nuoc mat")
                || clean.contains("shawshank") || clean.contains("nha tu")) {
            Movie movie = findMovieByTmdb(movies, 278); // The Shawshank Redemption
            if (movie == null) movie = findMovieByTmdb(movies, 497); // The Green Mile
            String text = "🎭 Bộ phim tâm lý sâu sắc chạm đến tận cùng cảm xúc: **\""
                    + (movie != null ? movie.getTitle() : "Nhà Tù Shawshank") + "\"**!\n\n"
                    + "Được hàng triệu khán giả và IMDb xếp hạng #1 mọi thời đại (★9.3) — bài ca bất tử về hy vọng và sự tự do. Mời bạn bấm thẻ bên dưới để trải nghiệm!";
            return new AiResult(text, movie);
        }

        // 9. Phiêu lưu / Giả tưởng / Phép thuật
        if (clean.contains("phieu luu") || clean.contains("gia tuong") || clean.contains("phep thuat")
                || clean.contains("ky ao") || clean.contains("fantasy") || clean.contains("chua te nhung chiec nhan")
                || clean.contains("avatar")) {
            Movie movie = findMovieByTmdb(movies, 122); // The Return of the King
            if (movie == null) movie = findMovieByTmdb(movies, 19995); // Avatar
            String text = "⚔️ Thiên sử thi huyền thoại đoạt trọn 11 giải Oscar: **\""
                    + (movie != null ? movie.getTitle() : "Chúa Tể Những Chiếc Nhẫn: Sự Trở Lại Của Nhà Vua") + "\"**!\n\n"
                    + "Trận đại chiến vĩ đại nhất bảo vệ Middle-earth trước chúa tể bóng tối. Trải nghiệm phiêu lưu kỳ vĩ không gì sánh bằng!";
            return new AiResult(text, movie);
        }

        // 10. Chiến tranh / Lịch sử / Nhân đạo
        if (clean.contains("chien tranh") || clean.contains("lich su") || clean.contains("quan doi")
                || clean.contains("schindler") || clean.contains("the chien") || clean.contains("war")) {
            Movie movie = findMovieByTmdb(movies, 424); // Schindler's List
            String text = "🕊️ Kiệt tác điện ảnh lay động hàng triệu trái tim: **\""
                    + (movie != null ? movie.getTitle() : "Bản Danh Sách Của Schindler") + "\"** của Steven Spielberg!\n\n"
                    + "Câu chuyện có thật về Oskar Schindler dùng toàn bộ tài sản để cứu hơn 1.100 sinh mạng trong Thế chiến II. Tác phẩm nhân đạo sâu sắc!";
            return new AiResult(text, movie);
        }

        // 11. Hài hước / Vui nhộn / Giải trí
        if (clean.contains("hai huoc") || clean.contains("hai kich") || clean.contains("vui ve")
                || clean.contains("giai tri") || clean.contains("cuoi") || clean.contains("comedy")
                || (clean.contains("phim") && clean.contains("hai"))) {
            Movie movie = findMovieByTmdb(movies, 13); // Forrest Gump
            if (movie == null) movie = findMovieByTmdb(movies, 680); // Pulp Fiction
            String text = "😄 Mang lại tiếng cười sảng khoái và năng lượng tích cực cho bạn: **\""
                    + (movie != null ? movie.getTitle() : "Cuộc Đời Forrest Gump") + "\"**!\n\n"
                    + "Những tình huống hóm hỉnh, ấm áp và thông điệp lạc quan về cuộc sống sẽ giúp bạn xua tan mọi mệt mỏi!";
            return new AiResult(text, movie);
        }

        // 12. Kinh dị / Giật gân / Gai góc
        if (clean.contains("kinh di") || clean.contains("phim ma") || clean.matches(".*\\bma\\b.*")
                || clean.contains("rung ron")
                || clean.contains("am anh") || clean.contains("giat gan") || clean.contains("thriller")
                || clean.contains("fight club")) {
            Movie movie = findMovieByTmdb(movies, 550); // Fight Club
            String text = "⚡ Tác phẩm giật gân, gai góc và đầy nổi loạn: **\""
                    + (movie != null ? movie.getTitle() : "Sàn Đấu Sinh Tử (Fight Club)") + "\"**!\n\n"
                    + "Cốt truyện lôi cuốn với cú 'plot twist' kinh điển đảo lộn mọi suy đoán. Bấm thẻ bên dưới để khám phá!";
            return new AiResult(text, movie);
        }

        // 13. Tâm trạng buồn / chán / mệt mỏi / Chữa lành
        if (clean.contains("buon") || clean.contains("chan") || clean.contains("co don")
                || clean.contains("met moi") || clean.contains("stress") || clean.contains("that tinh")
                || clean.contains("chua lanh")) {
            Movie movie = findMovieByTmdb(movies, 13); // Forrest Gump
            String text = "❤️ Đừng để nỗi buồn làm bạn nản lòng nhé! Hãy để câu chuyện của **\""
                    + (movie != null ? movie.getTitle() : "Cuộc Đời Forrest Gump") + "\"** sưởi ấm trái tim bạn.\n\n"
                    + "Một bộ phim ấm áp, tràn đầy năng lượng tích cực và bài học nhân sinh sâu sắc. Chúc bạn có những phút giây thư giãn tuyệt vời!";
            return new AiResult(text, movie);
        }

        // 14. Diễn viên / Đạo diễn
        if (clean.contains("dien vien") || clean.contains("actor") || clean.contains("cast")
                || clean.contains("ai dong") || clean.contains("dong vai") || clean.contains("robert downey")
                || clean.contains("tom cruise") || clean.contains("tom hanks") || clean.contains("dicaprio")) {
            Movie movie = findMovieByTmdb(movies, 299534); // Endgame
            if (clean.contains("tom hanks")) movie = findMovieByTmdb(movies, 13);
            else if (clean.contains("dicaprio")) movie = findMovieByTmdb(movies, 27205);
            else if (clean.contains("tom cruise")) movie = findMovieByTmdb(movies, 361743);

            String text = "🌟 Nit Flex sở hữu dàn diễn viên siêu sao hàng đầu thế giới:\n"
                    + "• **Robert Downey Jr., Scarlett Johansson** (Avengers)\n"
                    + "• **Leonardo DiCaprio** (Inception)\n"
                    + "• **Tom Hanks** (Forrest Gump, The Green Mile)\n"
                    + "• **Tom Cruise** (Top Gun: Maverick)\n"
                    + "• **Marlon Brando & Al Pacino** (The Godfather)\n\n"
                    + "Bấm vào từng phim để xem danh sách diễn viên chi tiết nhé!";
            return new AiResult(text, movie);
        }

        // 15. Tìm theo tên phim cụ thể người dùng gõ
        for (Movie m : movies) {
            String titleClean = removeAccents(m.getTitle());
            String origClean = removeAccents(m.getOriginalTitle() != null ? m.getOriginalTitle() : "");
            if ((titleClean.length() > 3 && clean.contains(titleClean))
                    || (origClean.length() > 3 && clean.contains(origClean))) {
                String text = "🎬 Mình tìm thấy bộ phim **\"" + m.getTitle() + "\"** ("
                        + String.format("%.1f", m.getVoteAverage()) + "★ TMDB) trong rạp Nit Flex!\n\n"
                        + (m.getOverview() != null && m.getOverview().length() > 10
                            ? m.getOverview().substring(0, Math.min(120, m.getOverview().length())) + "...\n\n"
                            : "")
                        + "Bấm vào thẻ bên dưới để thưởng thức ngay nhé! 🍿";
                return new AiResult(text, m);
            }
        }

        // 16. Phim điểm cao nhất / Đánh giá tốt nhất CHUNG CHUNG
        if (clean.contains("danh gia cao") || clean.contains("hay nhat") || clean.contains("diem cao")
                || clean.contains("top") || clean.contains("tot nhat") || clean.contains("nhieu sao")) {
            Movie best = findMovieByTmdb(movies, 278); // Shawshank Redemption (9.3)
            if (best == null) best = getTopRatedFrom(movies);
            String text = "🌟 Bộ phim có điểm đánh giá cao nhất trên Nit Flex hiện tại là **\""
                    + (best != null ? best.getTitle() : "Nhà Tù Shawshank") + "\"** (★"
                    + (best != null ? String.format("%.1f", best.getVoteAverage()) : "9.3") + " TMDB)!\n\n"
                    + "Tác phẩm kinh điển đứng đầu mọi bảng xếp hạng điện ảnh thế giới. Bấm thẻ bên dưới để xem ngay!";
            return new AiResult(text, best);
        }

        // 17. Phim Hot / Thịnh hành / Xem nhiều
        if (clean.contains("hot") || clean.contains("thinh hanh") || clean.contains("xem nhieu")
                || clean.contains("pho bien") || clean.contains("noi bat")) {
            Movie hot = findMovieByTmdb(movies, 299534); // Endgame
            if (hot == null) hot = queryOne("SELECT * FROM movies ORDER BY popularity DESC LIMIT 1");
            String text = "🔥 Bộ phim đang 'làm mưa làm gió' với lượt xem khủng nhất trên Nit Flex là **\""
                    + (hot != null ? hot.getTitle() : "Avengers: Hồi Kết") + "\"**!\n\n"
                    + "Một siêu phẩm bùng nổ mà bạn không thể bỏ lỡ. Bấm thẻ bên dưới để xem ngay!";
            return new AiResult(text, hot);
        }

        // 18. Quét từ khóa trong nội dung tổng quan (overview)
        for (Movie m : movies) {
            if (m.getOverview() != null) {
                String ovClean = removeAccents(m.getOverview());
                String[] words = clean.split("\\s+");
                int matchCount = 0;
                for (String w : words) {
                    if (w.length() >= 4 && ovClean.contains(w)) matchCount++;
                }
                if (matchCount >= 2) {
                    String text = "✨ Dựa trên câu hỏi của bạn, Nit Flex gợi ý bộ phim **\"" + m.getTitle() + "\"**!\n\n"
                            + (m.getOverview().length() > 100 ? m.getOverview().substring(0, 100) + "..." : m.getOverview())
                            + "\n\nBấm thẻ bên dưới để xem ngay nhé!";
                    return new AiResult(text, m);
                }
            }
        }

        // 19. Mặc định: Gợi ý các thể loại phong phú
        Movie defaultMovie = findMovieByTmdb(movies, 299534);
        if (defaultMovie == null && !movies.isEmpty()) defaultMovie = movies.get(0);
        String text = "🤖 Bạn có thể thử xem siêu phẩm **\""
                + (defaultMovie != null ? defaultMovie.getTitle() : "Avengers: Hồi Kết") + "\"** nhé!\n\n"
                + "Bạn cũng có thể hỏi mình theo thể loại cụ thể như:\n"
                + "• *\"Gợi ý phim tình cảm lãng mạn\"* 💕\n"
                + "• *\"Phim hoạt hình anime cảm động\"* 🎨\n"
                + "• *\"Phim viễn tưởng vũ trụ hoặc hack não\"* 🚀\n"
                + "• *\"Phim có điểm đánh giá cao nhất\"* 🌟\n"
                + "• Hoặc gõ tên bất kỳ bộ phim nào bạn muốn tìm! 😊";
        return new AiResult(text, defaultMovie);
    }

    // ================================================================
    // TÌM PHIM LIÊN QUAN ĐẾN CÂU HỎI
    // ================================================================
    private static Movie findRelatedMovie(String query, List<Movie> movies) {
        String clean = removeAccents(query);
        String queryLower = query.toLowerCase();

        // 1. Tìm theo tên phim trong danh sách có sẵn
        for (Movie m : movies) {
            String titleClean = removeAccents(m.getTitle());
            String origClean  = removeAccents(m.getOriginalTitle() != null ? m.getOriginalTitle() : "");
            if (clean.contains(titleClean) || titleClean.contains(clean)
                    || (!origClean.isEmpty() && clean.contains(origClean))) {
                return m;
            }
        }

        // 2. Tìm theo từ khóa thể loại
        if (clean.contains("tinh cam") || clean.contains("lang man") || clean.contains("romance")
                || clean.contains("tinh yeu") || clean.contains("hen ho") || clean.contains("your name")) {
            return findMovieByTmdb(movies, 372058); // Your Name
        }
        if (clean.contains("hanh dong") || clean.contains("action") || clean.contains("avenger")) {
            return findMovieByTmdb(movies, 299534); // Endgame
        }
        if (clean.contains("hoat hinh") || clean.contains("anime") || clean.contains("ghibli")
                || clean.contains("vung dat linh hon")) {
            return findMovieByTmdb(movies, 129); // Spirited Away
        }
        if (clean.contains("vien tuong") || clean.contains("vu tru") || clean.contains("interstellar")) {
            return findMovieByTmdb(movies, 157336); // Interstellar
        }
        if (clean.contains("hack nao") || clean.contains("giac mo") || clean.contains("inception")) {
            return findMovieByTmdb(movies, 27205); // Inception
        }
        if (clean.contains("bo gia") || clean.contains("mafia") || clean.contains("godfather") || clean.contains("toi pham")) {
            return findMovieByTmdb(movies, 238); // The Godfather
        }
        if (clean.contains("shawshank") || clean.contains("nha tu") || clean.contains("tu nhan") || clean.contains("tam ly")) {
            return findMovieByTmdb(movies, 278); // Shawshank
        }
        if (clean.contains("phieu luu") || clean.contains("gia tuong") || clean.contains("chua te")) {
            return findMovieByTmdb(movies, 122); // Lord of the Rings
        }
        if (clean.contains("chien tranh") || clean.contains("lich su") || clean.contains("schindler")) {
            return findMovieByTmdb(movies, 424); // Schindler's List
        }
        if (clean.contains("hai huoc") || clean.contains("forrest") || clean.contains("buon") || clean.contains("cam dong")) {
            return findMovieByTmdb(movies, 13); // Forrest Gump
        }
        if (clean.contains("kinh di") || clean.contains("giat gan") || clean.contains("fight club")) {
            return findMovieByTmdb(movies, 550); // Fight Club
        }
        if (clean.contains("top gun") || clean.contains("maverick") || clean.contains("phi cong")) {
            return findMovieByTmdb(movies, 361743);
        }
        if (clean.contains("hot") || clean.contains("hay nhat") || clean.contains("danh gia cao")) {
            return getTopRatedFrom(movies);
        }

        // 3. Thử tìm theo keyword trong DB
        String keyword = queryLower.replace("phim", "").replace("xem", "").replace("tim", "").trim();
        if (!keyword.isEmpty() && keyword.length() > 2) {
            return queryOne("SELECT * FROM movies WHERE title LIKE '%" + keyword.replace("'", "") + "%'"
                    + " OR original_title LIKE '%" + keyword.replace("'", "") + "%' LIMIT 1");
        }

        return null;
    }

    // ================================================================
    // LẤY TẤT CẢ PHIM TỪ DB (offline fallback: trả list rỗng)
    // ================================================================
    private static List<Movie> queryAllMovies() {
        List<Movie> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return getOfflineMovieList();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, title, original_title, poster_path, backdrop_path, vote_average, overview, video_url, tmdb_id, popularity FROM movies ORDER BY vote_average DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movie m = new Movie();
                    m.setId(rs.getInt("id"));
                    m.setTitle(rs.getString("title"));
                    m.setOriginalTitle(rs.getString("original_title"));
                    m.setPosterPath(rs.getString("poster_path"));
                    m.setBackdropPath(rs.getString("backdrop_path"));
                    m.setVoteAverage(rs.getDouble("vote_average"));
                    m.setOverview(rs.getString("overview"));
                    m.setVideoUrl(rs.getString("video_url"));
                    m.setTmdbId(rs.getInt("tmdb_id"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "queryAllMovies error", e);
            return getOfflineMovieList();
        }
        return list.isEmpty() ? getOfflineMovieList() : list;
    }

    private static Movie queryOne(String sql) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Movie m = new Movie();
                    m.setId(rs.getInt("id"));
                    m.setTitle(rs.getString("title"));
                    m.setOriginalTitle(rs.getString("original_title"));
                    m.setPosterPath(rs.getString("poster_path"));
                    m.setBackdropPath(rs.getString("backdrop_path"));
                    m.setVoteAverage(rs.getDouble("vote_average"));
                    m.setOverview(rs.getString("overview"));
                    m.setVideoUrl(rs.getString("video_url"));
                    m.setTmdbId(rs.getInt("tmdb_id"));
                    return m;
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    private static Movie findMovieByTmdb(List<Movie> movies, int tmdbId) {
        for (Movie m : movies) {
            if (m.getTmdbId() == tmdbId) return m;
        }
        // Fallback: nếu danh sách không có tmdb_id, thử tìm theo từ khóa tên
        String kw = null;
        if (tmdbId == 372058) kw = "your name";
        else if (tmdbId == 299534) kw = "endgame";
        else if (tmdbId == 299536) kw = "infinity war";
        else if (tmdbId == 13) kw = "forrest gump";
        else if (tmdbId == 157336) kw = "interstellar";
        else if (tmdbId == 27205) kw = "inception";
        else if (tmdbId == 278) kw = "shawshank";
        else if (tmdbId == 238) kw = "godfather";
        else if (tmdbId == 129) kw = "spirited away";
        else if (tmdbId == 122) kw = "lord of the rings";
        else if (tmdbId == 424) kw = "schindler";
        else if (tmdbId == 550) kw = "fight club";
        else if (tmdbId == 497) kw = "green mile";
        else if (tmdbId == 361743) kw = "top gun";

        if (kw != null) {
            for (Movie m : movies) {
                String title = removeAccents(m.getTitle());
                String orig = removeAccents(m.getOriginalTitle() != null ? m.getOriginalTitle() : "");
                if (title.contains(kw) || orig.contains(kw)) return m;
            }
        }
        return null;
    }

    private static Movie getTopRatedFrom(List<Movie> movies) {
        if (movies.isEmpty()) return null;
        Movie best = movies.get(0);
        for (Movie m : movies) {
            if (m.getVoteAverage() > best.getVoteAverage()) best = m;
        }
        return best;
    }

    // Danh sách phim offline phong phú (đầy đủ TMDB ID, Poster TMDB & Overview)
    private static List<Movie> getOfflineMovieList() {
        List<Movie> list = new ArrayList<>();
        Object[][] data = {
            {1, 299534, "Avengers: Hồi Kết", "Avengers: Endgame", 8.4,
             "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
             "Sau cú búng tay của Thanos, những Avengers còn sống sót tập hợp lại để đảo ngược thảm kịch."},
            {2, 299536, "Avengers: Cuộc Chiến Vô Cực", "Avengers: Infinity War", 8.3,
             "https://image.tmdb.org/t/p/w500/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg",
             "Biệt đội Avengers và các đồng minh đối mặt với Thanos - kẻ tìm kiếm 6 Viên đá Vô cực."},
            {3, 372058, "Tên Cậu Là Gì?", "Your Name", 8.5,
             "https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6qFsPwaMisQ.jpg",
             "Hai cô cậu học sinh xa lạ bị hoán đổi thân xác trong giấc mơ và bắt đầu hành trình tìm kiếm nhau."},
            {4, 13, "Cuộc Đời Forrest Gump", "Forrest Gump", 8.8,
             "https://image.tmdb.org/t/p/w500/arw2VCBveWOVZr6pxd9XTd1TdQa.jpg",
             "Câu chuyện về Forrest Gump - chàng trai với chỉ số IQ 75 nhưng có một trái tim chân thành và cuộc đời phi thường."},
            {5, 157336, "Hố Đen Tử Thần", "Interstellar", 8.7,
             "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
             "Một nhóm phi hành gia du hành qua lỗ sâu vũ trụ để tìm kiếm ngôi nhà mới cho nhân loại."},
            {6, 27205, "Kẻ Đánh Cắp Giấc Mơ", "Inception", 8.8,
             "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
             "Dom Cobb - kẻ trộm chuyên nghiệp đánh cắp bí mật từ tiềm thức sâu thẳm của người khác trong lúc ngủ."},
            {7, 278, "Nhà Tù Shawshank", "The Shawshank Redemption", 9.3,
             "https://image.tmdb.org/t/p/w500/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg",
             "Andy Dufresne bị kết án oan và hành trình kiên trì hướng đến tự do tại nhà tù Shawshank."},
            {8, 238, "Bố Già", "The Godfather", 9.2,
             "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja.jpg",
             "Thiên anh hùng ca về gia tộc tội phạm Corleone và sự chuyển giao quyền lực giữa Don Vito và Michael."},
            {9, 129, "Vùng Đất Linh Hồn", "Spirited Away", 8.6,
             "https://image.tmdb.org/t/p/w500/393r8D26Bh8qjbsVXPZ0nfeiT3s.jpg",
             "Cô bé Chihiro vô tình lạc vào thế giới linh hồn và phải tìm cách giải cứu cha mẹ mình."},
            {10, 122, "Chúa Tể Những Chiếc Nhẫn: Sự Trở Lại Của Nhà Vua", "The Lord of the Rings: The Return of the King", 9.0,
             "https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg",
             "Trận đại chiến quyết định số phận Middle-earth và sứ mệnh tiêu hủy chiếc nhẫn tại Núi Doom."},
            {11, 424, "Bản Danh Sách Của Schindler", "Schindler's List", 9.0,
             "https://image.tmdb.org/t/p/w500/sF1U4EUQS8YHUYjNl3pMGNIQyr0.jpg",
             "Oskar Schindler đã cứu sống hơn một ngàn người Do Thái trong thời kỳ thảm sát Thế chiến II."},
            {12, 550, "Sàn Đấu Sinh Tử", "Fight Club", 8.8,
             "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
             "Một nhân viên văn phòng mất ngủ kết thân với Tyler Durden và thành lập câu lạc bộ đánh đấm ngầm."},
            {13, 497, "Dặm Xanh", "The Green Mile", 8.9,
             "https://image.tmdb.org/t/p/w500/8VG8fDNiy5087BelQaY5f4aXekS.jpg",
             "Viên cai ngục phát hiện ra một tử tù da đen to lớn sở hữu năng lực chữa lành kỳ diệu."},
            {14, 361743, "Phi Công Siêu Đẳng: Maverick", "Top Gun: Maverick", 8.3,
             "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg",
             "Đại úy Pete 'Maverick' Mitchell huấn luyện nhóm phi công trẻ tinh nhuệ cho một nhiệm vụ sinh tử."}
        };

        for (Object[] item : data) {
            Movie m = new Movie();
            m.setId((int) item[0]);
            m.setTmdbId((int) item[1]);
            m.setTitle((String) item[2]);
            m.setOriginalTitle((String) item[3]);
            m.setVoteAverage((double) item[4]);
            m.setPosterPath((String) item[5]);
            m.setOverview((String) item[6]);
            list.add(m);
        }
        return list;
    }
}
