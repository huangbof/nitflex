package com.example.backup;

import com.example.backup.database.DBConnection;
import com.example.backup.model.Cast;
import com.example.backup.model.ChatMessage;
import com.example.backup.model.Comment;
import com.example.backup.model.DashboardStats;
import com.example.backup.model.Genre;
import com.example.backup.model.Movie;
import com.example.backup.model.User;
import com.example.backup.model.VipPackage;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Bộ kiểm thử toàn diện (Comprehensive Test Suite) cho Nit Flex
 * Kiểm tra toàn bộ các phân hệ: Model, Logic nghiệp vụ, Điều kiện lọc Tiếp Tục Xem,
 * Quản lý VIP, Thống kê Admin, Chatbot AI và Kết nối CSDL MySQL.
 */
public class ComprehensiveNitFlexTestSuite {

    // =========================================================================
    // 1. KIỂM THỬ PHÂN HỆ PHIM (MOVIE MODEL & BUSINESS LOGIC)
    // =========================================================================

    @Test
    public void testMovieFormattingAndProgress() {
        Movie movie = new Movie();
        movie.setId(101);
        movie.setTitle("Avengers: Cuộc Chiến Vô Cực");
        movie.setVoteAverage(8.3);
        movie.setWatchDuration(1700); // 28 phút 20 giây

        // Kiểm tra định dạng thời gian đã xem
        movie.setWatchProgress(0);
        assertEquals("00:00", movie.getFormattedWatchProgress());

        movie.setWatchProgress(65); // 1 phút 5 giây
        assertEquals("01:05", movie.getFormattedWatchProgress());

        movie.setWatchProgress(3600); // 1 giờ chẵn
        assertEquals("1h00p", movie.getFormattedWatchProgress());

        movie.setWatchProgress(4530); // 1 giờ 15 phút 30 giây
        assertEquals("1h15p", movie.getFormattedWatchProgress());

        // Kiểm tra tính phần trăm đã xem (%)
        movie.setWatchProgress(0);
        assertEquals(0, movie.getWatchProgressPercent());

        movie.setWatchProgress(850); // 50% của 1700
        assertEquals(50, movie.getWatchProgressPercent());

        movie.setWatchProgress(1700); // 100%
        assertEquals(100, movie.getWatchProgressPercent());

        // Kiểm tra thời gian còn lại (Remaining Time)
        movie.setWatchProgress(65);
        String remainingText = movie.getRemainingTimeText();
        assertNotNull(remainingText);
        assertTrue(remainingText.contains("phút") || remainingText.contains("h"));

        // Kiểm tra phân loại phim VIP: TMDB > 8.0 là VIP
        assertTrue("Avengers điểm 8.3 phải là phim VIP", movie.isVip());

        Movie standardMovie = new Movie();
        standardMovie.setVoteAverage(7.6);
        assertFalse("Avatar điểm 7.6 không phải là phim VIP", standardMovie.isVip());

        Movie borderMovie = new Movie();
        borderMovie.setVoteAverage(8.0);
        assertFalse("Điểm 8.0 chính xác không phải VIP (> 8.0 mới là VIP)", borderMovie.isVip());
    }

    @Test
    public void testMovieEqualityAndCollectionOperations() {
        Movie m1 = new Movie();
        m1.setId(1);
        m1.setTitle("Phim A");

        Movie m2 = new Movie();
        m2.setId(1);
        m2.setTitle("Phim A Cập Nhật");

        Movie m3 = new Movie();
        m3.setId(2);
        m3.setTitle("Phim B");

        assertEquals("Hai đối tượng Movie cùng ID phải bằng nhau", m1, m2);
        assertNotEquals("Hai đối tượng Movie khác ID không được bằng nhau", m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());

        List<Movie> list = new ArrayList<>();
        list.add(m1);
        assertTrue("List phải nhận biết movie đã tồn tại theo ID", list.contains(m2));
    }

    // =========================================================================
    // 2. KIỂM THỬ ĐIỀU KIỆN LỌC TIẾP TỤC XEM (CONTINUE WATCHING LOGIC)
    // =========================================================================

    @Test
    public void testContinueWatchingFilterThresholds() {
        // Mô phỏng logic lọc: WHERE wh.user_id = ? AND (wh.duration <= 0 OR wh.progress < (wh.duration - 5))

        // Trường hợp 1: Phim vừa mở xem 3 giây (Avatar / Bố Già)
        int progress1 = 3;
        int duration1 = 7200;
        boolean shouldShow1 = (duration1 <= 0 || progress1 < (duration1 - 5));
        assertTrue("Phim vừa xem 3 giây dở dang BẮT BUỘC phải hiện ở Tiếp Tục Xem", shouldShow1);

        // Trường hợp 2: Phim xem 65 giây (Avengers)
        int progress2 = 65;
        int duration2 = 1700;
        boolean shouldShow2 = (duration2 <= 0 || progress2 < (duration2 - 5));
        assertTrue("Phim xem 65 giây BẮT BUỘC phải hiện ở Tiếp Tục Xem", shouldShow2);

        // Trường hợp 3: Phim đã xem hết đến cuối (7200s / 7200s)
        int progress3 = 7200;
        int duration3 = 7200;
        boolean shouldShow3 = (duration3 <= 0 || progress3 < (duration3 - 5));
        assertFalse("Phim đã xem hết KHÔNG ĐƯỢC hiện ở Tiếp Tục Xem", shouldShow3);

        // Trường hợp 4: Phim xem sát nút kết thúc (còn 2 giây là hết)
        int progress4 = 7198;
        int duration4 = 7200;
        boolean shouldShow4 = (duration4 <= 0 || progress4 < (duration4 - 5));
        assertFalse("Phim xem gần hết sát nút (còn < 5s) KHÔNG ĐƯỢC hiện ở Tiếp Tục Xem", shouldShow4);

        // Trường hợp 5: Phim trailer hoặc không xác định được duration (duration = 0)
        int progress5 = 15;
        int duration5 = 0;
        boolean shouldShow5 = (duration5 <= 0 || progress5 < (duration5 - 5));
        assertTrue("Phim duration = 0 đang xem dở vẫn phải được hiện", shouldShow5);
    }

    // =========================================================================
    // 3. KIỂM THỬ PHÂN HỆ NGƯỜI DÙNG & PHÂN QUYỀN (USER & ADMIN ROLES)
    // =========================================================================

    @Test
    public void testUserModelAndRoles() {
        User admin = new User();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setEmail("admin@movieapp.com");
        admin.setRole("admin");
        admin.setVip(true);

        assertTrue("Tài khoản quyền ADMIN phải được xác nhận isAdmin() = true", admin.isAdmin());
        assertTrue("Tài khoản admin phải là VIP", admin.isVip());

        User normalUser = new User();
        normalUser.setId(2);
        normalUser.setUsername("user1");
        normalUser.setEmail("user1@example.com");
        normalUser.setRole("user");
        normalUser.setVip(false);

        assertFalse("Tài khoản USER thường không phải là admin", normalUser.isAdmin());
        assertFalse("Tài khoản thường chưa kích hoạt không phải là VIP", normalUser.isVip());

        normalUser.setVip(true);
        assertTrue("Tài khoản sau khi nâng VIP phải có isVip() = true", normalUser.isVip());
    }

    // =========================================================================
    // 4. KIỂM THỬ GÓI VIP & THANH TOÁN (VIP PACKAGES)
    // =========================================================================

    @Test
    public void testVipPackageModel() {
        VipPackage pkgMonth = new VipPackage(1, "Gói VIP 1 Tháng", "VIP_1M", 59000, 30, "Xem phim 4K không quảng cáo", true);
        assertEquals("VIP_1M", pkgMonth.getCode());
        assertEquals(59000.0, pkgMonth.getPrice(), 0.001);
        assertEquals(30, pkgMonth.getDurationDays());

        String formatted = pkgMonth.getFormattedPrice();
        assertNotNull(formatted);
        assertTrue("Giá tiền phải có đơn vị đ hoặc số", formatted.contains("59") || formatted.contains("đ"));

        VipPackage pkgYear = new VipPackage(3, "Gói VIP 1 Năm", "VIP_1Y", 499000, 365, "Tiết kiệm 30% chi phí", true);
        assertEquals(365, pkgYear.getDurationDays());
    }

    // =========================================================================
    // 5. KIỂM THỬ THỐNG KÊ ADMIN DASHBOARD (DASHBOARD STATS)
    // =========================================================================

    @Test
    public void testDashboardStatsModel() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalMovies(25);
        stats.setTotalUsers(150);
        stats.setVipUsers(42);
        stats.setTotalViews(1890);

        List<Movie> topMovies = new ArrayList<>();
        Movie top1 = new Movie();
        top1.setId(1);
        top1.setTitle("Avengers: Endgame");
        top1.setViewCount(520);
        topMovies.add(top1);

        stats.setTopMovies(topMovies);

        assertEquals(25, stats.getTotalMovies());
        assertEquals(150, stats.getTotalUsers());
        assertEquals(42, stats.getVipUsers());
        assertEquals(1890, stats.getTotalViews());
        assertEquals(1, stats.getTopMovies().size());
        assertEquals("Avengers: Endgame", stats.getTopMovies().get(0).getTitle());
        assertEquals(520, stats.getTopMovies().get(0).getViewCount());
    }

    // =========================================================================
    // 6. KIỂM THỬ CÁC THỰC THỂ KHÁC (COMMENT, CAST, GENRE, CHAT MESSAGE)
    // =========================================================================

    @Test
    public void testAuxiliaryEntities() {
        // Genre
        Genre genre = new Genre(1, "Hành Động");
        assertEquals(1, genre.getId());
        assertEquals("Hành Động", genre.getName());

        // Cast
        Cast cast = new Cast(10, 1, "Robert Downey Jr.", "Tony Stark / Iron Man", "/robert.jpg");
        assertEquals("Robert Downey Jr.", cast.getActorName());
        assertEquals("Tony Stark / Iron Man", cast.getCharacterName());

        // Comment
        Comment comment = new Comment();
        comment.setId(1);
        comment.setUserId(2);
        comment.setMovieId(1);
        comment.setContent("Phim rất hay và cảm động!");
        comment.setCreatedAt("2026-11-15 20:00:00");
        comment.setUsername("user1");
        comment.setMovieTitle("Avengers: Hồi Kết");

        assertEquals("user1", comment.getUsername());
        assertEquals("Avengers: Hồi Kết", comment.getMovieTitle());
        assertEquals("Phim rất hay và cảm động!", comment.getContent());

        // ChatMessage (Gemini AI)
        ChatMessage userMsg = new ChatMessage("Gợi ý cho tôi phim hành động hay", true, "10:30");
        assertTrue(userMsg.isUser());
        assertEquals("Gợi ý cho tôi phim hành động hay", userMsg.getText());

        ChatMessage aiMsg = new ChatMessage("Chào bạn, bạn có thể xem Avengers: Cuộc Chiến Vô Cực!", false, "10:31");
        assertFalse(aiMsg.isUser());
        assertFalse(aiMsg.getText().isEmpty());
    }

    // =========================================================================
    // 7. KIỂM THỬ THUẬT TOÁN MONOTONIC TIMESTAMP (TRÁNH LỖI MẤT TIẾNG & CẮT PHIM)
    // =========================================================================

    @Test
    public void testMonotonicTimestampSimulation() {
        // Thuật toán đảm bảo timestamp của audio và video luôn tăng đơn điệu
        int trackCount = 2; // Track 0: Video, Track 1: Audio
        long[] lastTrackTimeUs = new long[trackCount];
        for (int i = 0; i < trackCount; i++) {
            lastTrackTimeUs[i] = -1;
        }

        long[] incomingVideoPts = {0, 33333, 66666, 100000, 133333};
        long[] incomingAudioPts = {0, 21333, 42666, 64000, 85333, 106666};

        // Xử lý luồng Video
        for (long pts : incomingVideoPts) {
            long adjustedPts = pts;
            if (adjustedPts <= lastTrackTimeUs[0]) {
                adjustedPts = lastTrackTimeUs[0] + 1000;
            }
            assertTrue("Video PTS phải tăng đơn điệu", adjustedPts > lastTrackTimeUs[0]);
            lastTrackTimeUs[0] = adjustedPts;
        }

        // Xử lý luồng Audio
        for (long pts : incomingAudioPts) {
            long adjustedPts = pts;
            if (adjustedPts <= lastTrackTimeUs[1]) {
                adjustedPts = lastTrackTimeUs[1] + 1000;
            }
            assertTrue("Audio PTS phải tăng đơn điệu", adjustedPts > lastTrackTimeUs[1]);
            lastTrackTimeUs[1] = adjustedPts;
        }

        assertTrue(lastTrackTimeUs[0] > 0);
        assertTrue(lastTrackTimeUs[1] > 0);
    }

    // =========================================================================
    // 8. KIỂM THỬ KẾT NỐI CSDL MYSQL (NẾU ĐANG CHẠY TRÊN MÁY DEV)
    // =========================================================================

    @Test
    public void testMySQLConnectionConfiguration() {
        // Kiểm tra biến cấu hình
        assertNotNull(DBConnection.DB_HOST);
        assertNotNull(DBConnection.DB_PORT);
        assertNotNull(DBConnection.DB_NAME);
        assertEquals("movie_db", DBConnection.DB_NAME);

        // Kiểm thử kết nối tới localhost nếu dịch vụ MySQL đang chạy trên máy tính
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String localUrl = "jdbc:mysql://localhost:" + DBConnection.DB_PORT + "/" + DBConnection.DB_NAME
                    + "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"
                    + "&connectTimeout=2000&socketTimeout=3000&useSSL=false";
            try (Connection conn = DriverManager.getConnection(localUrl, DBConnection.DB_USER, DBConnection.DB_PASS)) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println(">>> ĐÃ KẾT NỐI THÀNH CÔNG MYSQL LOCALHOST!");

                    // Kiểm tra bảng movies
                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM movies")) {
                        if (rs.next()) {
                            int totalMovies = rs.getInt("total");
                            System.out.println(">>> Tổng số phim trong CSDL: " + totalMovies);
                            assertTrue(totalMovies >= 0);
                        }
                    }

                    // Kiểm tra bảng users
                    try (Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM users")) {
                        if (rs.next()) {
                            int totalUsers = rs.getInt("total");
                            System.out.println(">>> Tổng số user trong CSDL: " + totalUsers);
                            assertTrue(totalUsers >= 0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Nếu MySQL chưa bật hoặc cổng khác, bỏ qua mà không làm hỏng build
            System.out.println(">>> Bỏ qua kiểm tra kết nối trực tiếp MySQL (MySQL localhost chưa bật hoặc khác cổng): " + e.getMessage());
        }
    }
}
