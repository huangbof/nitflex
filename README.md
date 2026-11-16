# 🎬 NIT FLEX - Ứng Dụng Xem Phim Trực Tuyến Hiện Đại Trên Android

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-green?logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Java%2017-orange?logo=java" alt="Language" />
  <img src="https://img.shields.io/badge/Architecture-MVC%20%2B%20DAO%20Pattern-blue" alt="Architecture" />
  <img src="https://img.shields.io/badge/AI-Google%20Gemini%201.5%20Flash-purple?logo=google" alt="Gemini AI" />
  <img src="https://img.shields.io/badge/Database-MySQL%20%2B%20JDBC-00758F?logo=mysql" alt="MySQL" />
  <img src="https://img.shields.io/badge/Tests-10%2F10%20Passing%20(100%25)-success" alt="Tests" />
</p>

---

## 📖 Giới Thiệu (Overview)

**Nit Flex** là ứng dụng xem phim trực tuyến hiện đại trên nền tảng Android, được xây dựng theo kiến trúc **MVC kết hợp DAO Pattern** chuẩn mực. Ứng dụng kết nối đồng bộ dữ liệu thời gian thực với cơ sở dữ liệu **MySQL**, tích hợp trợ lý ảo thông minh **Google Gemini AI Chatbot**, hệ thống thanh toán gói cước **VIP (VietQR / MoMo)**, bảng quản trị **Admin Dashboard** toàn diện và trình phát video tối ưu hóa cao.

---

## ✨ Tính Năng Nổi Bật (Core Features)

### 🤖 1. Trợ Lý Ảo Gemini AI Chatbot (AI Assistant)
- **Tích hợp Google Gemini 1.5 Flash API**: Trả lời ngôn ngữ tự nhiên tức thì.
- **Tư vấn phim thông minh**: Gợi ý theo tâm trạng (*buồn, chữa lành, thư giãn*), thể loại (*Hành động, Tình cảm, Anime, Viễn tưởng, Kinh dị...*), diễn viên hoặc điểm số IMDb/TMDB.
- **Thẻ phim tương tác trực quan**: Tin nhắn phản hồi tự động đính kèm Card phim với poster, điểm số và nút phát trực tiếp.
- **2-Layer Fallback**: Cơ chế dự phòng ngoại tuyến thông minh đảm bảo trợ lý luôn phản hồi ngay cả khi mất mạng.

### 🎬 2. Trình Phát Video & Xử Lý Âm Thanh Chuyên Sâu
- **Phát phim MP4 mượt mà**: Hỗ trợ cả nguồn phim cục bộ (`res/raw/`) và luồng trực tuyến (Online Stream).
- **VideoRemuxer & Monotonic PTS**: Bộ chuyển mã nhanh on-the-fly giải quyết triệt để lỗi mất tiếng (Audio drop) trên các file Fragmented MP4 (fMP4) bằng cách kiểm soát timestamp tăng đơn điệu cho từng track riêng biệt.
- **Nhận diện thời lượng thực tế**: Tự động phát hiện chính xác độ dài phim qua `detectMediaDurationMs`, khắc phục lỗi phim bị cắt ngắn 1 phút.
- **Quản lý âm lượng & Audio Focus**: Khóa luồng âm lượng `STREAM_MUSIC`, tự động mở tiếng khi máy bị mute và yêu cầu Audio Focus ưu tiên phát âm thanh.

### 🍿 3. Tiếp Tục Xem & Lịch Sử Xem Phim (Continue Watching)
- **Đồng bộ tự động**: Ghi nhớ mốc thời gian xem dở dang chính xác đến từng giây.
- **Hiển thị trực quan**: Thanh carousel "Tiếp Tục Xem" trên Trang Chủ hiển thị tiến độ thanh đỏ và thời gian còn lại.
- **Xử lý xem hết thông minh**: Khi xem xong, phim tự động ẩn khỏi Tiếp Tục Xem nhưng vẫn được bảo lưu trong Lịch Sử Xem (Profile); khi bấm xem lại sẽ tự động phát từ đầu (`00:00`).
- **Phản ứng tức thì khi đổi Tab**: Tích hợp `onHiddenChanged` trên toàn bộ Fragment giúp dữ liệu luôn cập nhật tức thời khi chạm chuyển tab.

### 🛡️ 4. Bảng Quản Trị Hệ Thống Toàn Diện (Admin Dashboard)
- **Thống kê & Analytics thời gian thực**: Tổng phim, Tổng người dùng, Thành viên VIP, Tổng lượt xem và bảng xếp hạng **Top 5 Phim Xem Nhiều Nhất** (tự động tăng view khi phát phim).
- **Quản lý Phim**: Thêm, sửa, xóa phim; chọn video và ảnh poster trực tiếp từ bộ nhớ máy hoặc nhập URL.
- **Quản lý Người Dùng**: Danh sách thành viên, nâng/hạ cấp VIP, đổi mật khẩu mặc định, xóa tài khoản (bảo vệ an toàn cho Root Admin ID #1).
- **Quản trị Bình luận**: Hiển thị tên phim và người gửi; kiểm duyệt xóa bình luận vi phạm.
- **Quản trị Gói VIP**: Cấu hình giá tiền, số ngày sử dụng và các quyền lợi đặc quyền.

### 👑 5. Gói VIP & Thanh Toán Động (VietQR / MoMo)
- **Phân loại phim VIP**: Tự động nhận diện phim đạt TMDB $> 8.0$ là phim VIP.
- **Gói cước linh hoạt**: Tải danh sách gói từ CSDL (`VIP_1M`, `VIP_3M`, `VIP_1Y`).
- **Mã VietQR & Ví MoMo**: Hiển thị mã thanh toán trực quan kèm mô phỏng xác nhận nâng cấp VIP tức thì.

### 📱 6. Điều Hướng Hiện Đại (Modern Navigation)
- Thanh điều hướng đáy **BottomNavigationView** 5 tab:
  1. 🏠 **Trang chủ (`HomeFragment`)**: Banner phim nổi bật, Tiếp tục xem, Thể loại, Lưới phim đề xuất.
  2. 🔍 **Tìm kiếm (`SearchFragment`)**: Tìm kiếm theo tên, phân loại phim theo chip thể loại và bộ lọc nâng cao.
  3. 🤖 **Trợ lý AI (`AiChatFragment`)**: Trung tâm trò chuyện và gợi ý phim thông minh.
  4. ❤️ **Yêu thích (`FavoritesFragment`)**: Quản lý danh sách phim yêu thích cá nhân.
  5. 👤 **Cá nhân (`ProfileFragment`)**: Thông tin người dùng, trạng thái VIP, lịch sử xem phim và lối tắt Admin.

---

## 🏗️ Cấu Trúc Thư Mục Dự Án (Project Structure)

```text
backup/
├── app/
│   ├── src/main/java/com/example/backup/
│   │   ├── MainActivity.java                  # Host Activity điều phối 5 Fragment chính
│   │   ├── adapter/                           # RecyclerView Adapters
│   │   │   ├── AdminCastAdapter.java
│   │   │   ├── AdminCommentAdapter.java
│   │   │   ├── AdminMovieAdapter.java
│   │   │   ├── AdminUserAdapter.java
│   │   │   ├── AdminVipPackageAdapter.java
│   │   │   ├── CastAdapter.java
│   │   │   ├── ChatAdapter.java
│   │   │   ├── CommentAdapter.java
│   │   │   ├── ContinueWatchingAdapter.java
│   │   │   ├── GenreAdapter.java
│   │   │   └── MovieAdapter.java
│   │   ├── controller/                        # Tầng điều phối Controller
│   │   │   ├── AdminController.java
│   │   │   ├── AiAssistantController.java
│   │   │   ├── AuthController.java
│   │   │   └── MovieController.java
│   │   ├── dao/                               # Tầng truy cập CSDL (DAO Pattern)
│   │   │   ├── AdminDao.java
│   │   │   ├── CastDao.java
│   │   │   ├── CommentDao.java
│   │   │   ├── FavoriteDao.java
│   │   │   ├── GenreDao.java
│   │   │   ├── MovieDao.java
│   │   │   ├── MovieRatingDao.java
│   │   │   ├── UserDao.java
│   │   │   ├── VipPackageDao.java
│   │   │   └── WatchHistoryDao.java
│   │   ├── database/                          # Kết nối MySQL qua JDBC
│   │   │   └── DBConnection.java
│   │   ├── fragment/                          # 5 Fragment giao diện chính
│   │   │   ├── AiChatFragment.java
│   │   │   ├── FavoritesFragment.java
│   │   │   ├── HomeFragment.java
│   │   │   ├── ProfileFragment.java
│   │   │   └── SearchFragment.java
│   │   ├── model/                             # Data Models
│   │   │   ├── Cast.java
│   │   │   ├── ChatMessage.java
│   │   │   ├── Comment.java
│   │   │   ├── DashboardStats.java
│   │   │   ├── Genre.java
│   │   │   ├── Movie.java
│   │   │   ├── User.java
│   │   │   └── VipPackage.java
│   │   ├── service/                           # Session & Helpers
│   │   │   ├── ApiConfig.java
│   │   │   ├── HttpService.java
│   │   │   ├── NotificationHelper.java
│   │   │   └── SessionManager.java
│   │   ├── util/                              # Tiện ích bổ trợ
│   │   │   ├── VideoRemuxer.java              # Remuxing fMP4 & Monotonic PTS
│   │   │   └── VipDialogHelper.java           # Dialog nâng cấp VIP & VietQR
│   │   └── view/                              # Activities của ứng dụng
│   │       ├── AdminDashboardActivity.java
│   │       ├── ChatAiActivity.java
│   │       ├── FavoritesActivity.java
│   │       ├── HomeActivity.java
│   │       ├── LoginActivity.java
│   │       ├── MovieDetailActivity.java
│   │       ├── PlayerActivity.java
│   │       ├── ProfileActivity.java
│   │       ├── RegisterActivity.java
│   │       ├── SearchActivity.java
│   │       └── SplashActivity.java
│   ├── src/main/res/                          # Tài nguyên giao diện (Layouts, Drawables, Themes)
│   │   └── raw/                               # Thư mục lưu video MP4 nội bộ (không commit lên Git)
│   └── src/test/java/com/example/backup/      # Bộ kiểm thử tự động Unit Tests
│       ├── ComprehensiveNitFlexTestSuite.java # Kiểm thử toàn diện 8 phân hệ (100% Pass)
│       └── ExampleUnitTest.java
├── movie_db.sql                               # Script khởi tạo toàn bộ CSDL MySQL
├── build.gradle.kts                           # Cấu hình Gradle Root
└── README.md                                  # Tài liệu hướng dẫn dự án
```

---

## 🛠️ Hướng Dẫn Cài Đặt & Khởi Chạy (Getting Started)

### 1. Chuẩn Bị Cơ Sở Dữ Liệu (MySQL / XAMPP)
1. Cài đặt và khởi động **Apache & MySQL** trong XAMPP (hoặc MySQL 8.0 Server).
2. Mở phpMyAdmin tại: `http://localhost/phpmyadmin`.
3. Tạo mới database có tên: `movie_db` (bảng mã `utf8mb4_unicode_ci`).
4. Import file **`movie_db.sql`** có sẵn trong thư mục gốc của project vào CSDL `movie_db`.
5. Cấu hình IP kết nối trong file [DBConnection.java](file:///C:/Users/dmh20/AndroidStudioProjects/backup/app/src/main/java/com/example/backup/database/DBConnection.java):
   - Chạy trên máy ảo Android Emulator: Dùng IP mặc định `10.0.2.2`.
   - Chạy trên điện thoại thật: Đổi thành địa chỉ IP mạng LAN của máy tính (ví dụ: `192.168.1.15`).

### 2. Chuẩn Bị File Video MP4 Nội Bộ (Tùy chọn)
Do các file phim có dung lượng lớn nên được loại trừ khỏi Git (`*.mp4` trong `.gitignore`). Bạn có thể copy các file video MP4 của bạn vào thư mục:
`app/src/main/res/raw/` với các tên file tương ứng trong CSDL:
- `endgame.mp4`
- `avengers_infinity_war.mp4`
- `avatar.mp4`
- `avatar_the_way_of_water.mp4`
- `the_godfather.mp4`
- `the_godfather_part_ii.mp4`
- `interstellar.mp4`
- `inception.mp4`
- `fight_club.mp4`
- `spirited_away.mp4`
- `your_name.mp4`

### 3. Mở & Chạy Dự Án Trong Android Studio
1. Mở Android Studio -> Chọn **Open** -> Chọn thư mục `backup`.
2. Chờ Gradle tự động Sync xong dependencies.
3. Chạy kiểm thử tự động (Optional):
   ```bash
   ./gradlew test
   ```
4. Bấm nút **Run ▶** (màu xanh) trên thanh công cụ để cài đặt và trải nghiệm ứng dụng trên máy ảo hoặc thiết bị thật.

### 4. Tài Khoản Đăng Nhập Mặc Định
| Vai Trò | Tên Đăng Nhập | Mật Khẩu | Quyền Hạn |
| :--- | :--- | :--- | :--- |
| **Quản Trị Viên (Admin)** | `admin` | `123456` | Toàn quyền truy cập Bảng quản trị hệ thống, xem phim VIP không giới hạn |
| **Thành Viên VIP** | `234` hoặc `user1` | `123456` | Mở khóa toàn bộ phim VIP điểm TMDB > 8.0 |
| **Người Dùng Thường** | `user` | `123456` | Xem phim tiêu chuẩn, nâng cấp VIP bất cứ lúc nào |

---

## 🧪 Kiểm Thử Tự Động (Automated Testing)

Toàn bộ logic nghiệp vụ, thuật toán điều kiện lọc phim, phân quyền và kết nối CSDL được bảo vệ bởi bộ test:
* **File kiểm thử:** `ComprehensiveNitFlexTestSuite.java`
* **Lệnh thực thi:** `./gradlew test`
* **Kết quả:** `10/10 tests passed (100% Success Rate)`.

---

## 📄 Bản Quyền (License)
Dự án được phát triển phục vụ mục đích học tập và nghiên cứu công nghệ phát triển ứng dụng di động Android. Phát hành theo giấy phép [MIT License](LICENSE).
