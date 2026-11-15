# 🎬 NIT FLEX - Ứng dụng xem phim dành cho điện thoại



---

## 📖 Giới thiệu (Overview)
**Nit Flex** là ứng dụng xem phim trực tuyến hiện đại trên nền tảng Android được xây dựng theo kiến trúc MVC tối ưu, kết nối đồng bộ dữ liệu thời gian thực với cơ sở dữ liệu **MySQL** và tích hợp trợ lý ảo thông minh **Google Gemini AI**. Ứng dụng cung cấp trải nghiệm giải trí toàn diện tương đương các nền tảng streaming hàng đầu như Netflix, Disney+.

---

## ✨ Tính năng nổi bật (Core Features)

### 🤖 1. Trợ Lý Ảo Gemini AI Chatbot (AI Assistant)
- Tích hợp trực tiếp Google Gemini 1.5 Flash API qua chuẩn Authorization Key.
- Nhận diện câu hỏi tự nhiên theo thể loại (*Tình cảm, Hành động, Anime, Viễn tưởng, Kinh dị, Hài hước, Tâm lý, Bí ẩn...*).
- Tư vấn phim theo tâm trạng (*buồn, cần chữa lành, thư giãn*), theo diễn viên (*Leonardo DiCaprio, Tom Hanks, Tom Cruise, Robert Downey Jr.*) hoặc theo điểm đánh giá IMDb/TMDB.
- Trả về câu trả lời tự nhiên kèm **Thẻ phim tương tác trực quan** (Poster, điểm số, nút xem ngay).
- Cơ chế **2-Layer Fallback** thông minh: Tự động dự phòng dữ liệu ngoại tuyến chuẩn xác 100% nếu mất kết nối mạng.

### 📱 2. Kiến trúc Điều Hướng Hiện Đại (Modern Navigation)
- Thanh điều hướng đáy **BottomNavigationView** 5 tab:
  1. 🏠 **Trang chủ (HomeFragment)**: Banner phim nổi bật, hàng phim đang xem dở (Continue Watching), danh mục thể loại cuộn ngang, lưới phim đặc sắc.
  2. 🔍 **Tìm kiếm (SearchFragment)**: Tìm kiếm thời gian thực, chip sắp xếp (Độ hot, Điểm số, Mới nhất) và Bottom Sheet lọc đa tiêu chí (Thể loại, Năm phát hành, Gói VIP, Điểm đánh giá).
  3. 🤖 **Trợ lý AI (AiChatFragment)**: Trung tâm hỏi đáp gợi ý phim thông minh.
  4. ❤️ **Yêu thích (FavoritesFragment)**: Quản lý danh sách phim đã lưu, đồng bộ MySQL.
  5. 👤 **Cá nhân (ProfileFragment)**: Hồ sơ người dùng, quản lý gói thành viên VIP, lịch sử xem phim và lối tắt vào Bảng quản trị.
- Cơ chế chuyển đổi tab tức thì với `FragmentTransaction.show()` / `hide()` bảo toàn vị trí cuộn trang.
- Tối ưu khoảng đệm an toàn **Window Insets & Display Cutout** (không bị đè Status Bar hay camera nốt ruồi).

### 🎬 3. Trình Phát Video & Trải Nghiệm Xem Phim
- Trình phát video MP4 toàn màn hình hỗ trợ định dạng local trong máy và online stream.
- **Tiếp tục xem (Continue Watching)**: Tự động ghi nhớ chính xác thời lượng và giây xem dở, hiển thị thanh tiến độ màu đỏ Netflix và cho phép xóa trực tiếp.
- Đánh giá sao (Rating) và bình luận tương tác (Comments) đồng bộ cơ sở dữ liệu.

### 🛡️ 4. Bảng Quản Trị Hệ Thống Toàn Diện (Admin Dashboard)
- **Thống kê & Phân tích (Analytics)**: Tổng doanh thu VIP, số lượng người dùng, tỷ lệ chuyển đổi VIP, tổng lượt xem và Top phim thịnh hành kèm thanh biểu đồ trực quan.
- **Quản lý Phim**: Thêm, sửa, xóa phim. Hỗ trợ **chọn video MP4 và ảnh poster trực tiếp từ bộ nhớ điện thoại** mà không cần gõ URL thủ công.
- **Quản lý Người Dùng**: Xem danh sách thành viên, nâng/hạ cấp VIP, đổi mật khẩu, xóa tài khoản.
- **Quản lý Diễn Viên (Cast Management)**: Thêm diễn viên cho từng bộ phim cụ thể.
- **Quản lý Gói VIP (VIP Packages)**: Cấu hình giá tiền, số ngày sử dụng và các quyền lợi đặc quyền.

### 🔐 5. Xác thực & Phân quyền (Authentication)
- Đăng nhập/Đăng ký tài khoản hệ thống (băm mật khẩu bảo mật).
- Tích hợp **Google Sign-In** một chạm tiện lợi.
- Phân quyền Admin / User chặt chẽ.

---

## 🏗️ Cấu trúc Thư mục Dự án (Project Structure)

```text
backup/
├── app/
│   ├── src/main/java/com/example/backup/
│   │   ├── MainActivity.java                  # Main Host Activity chứa 5 Fragments
│   │   ├── adapter/                           # RecyclerView Adapters (Movie, Chat, Cast...)
│   │   ├── controller/                        # Business Logic (AuthController, MovieController, AiAssistantController...)
│   │   ├── database/                          # MySQL JDBC Connection (DBConnection)
│   │   ├── fragment/                          # 5 Màn hình Fragment chính
│   │   │   ├── HomeFragment.java
│   │   │   ├── SearchFragment.java
│   │   │   ├── AiChatFragment.java
│   │   │   ├── FavoritesFragment.java
│   │   │   └── ProfileFragment.java
│   │   ├── model/                             # Data Models (Movie, User, Comment, Cast, VipPackage...)
│   │   ├── service/                           # SessionManager, NotificationHelper
│   │   ├── util/                              # Dialog helpers (VipDialogHelper...)
│   │   └── view/                              # Activities (AdminDashboard, Player, Detail, Login, Splash...)
│   └── src/main/res/                          # Layouts, Menus, Drawables, Colors, Themes
├── src/main/java/                             # Java Servlet Backend (REST API alternative)
├── movie_db.sql                               # Script khởi tạo toàn bộ CSDL MySQL
└── build.gradle.kts                           # Gradle dependencies & configuration
```

---

## 🛠️ Hướng dẫn Cài đặt & Chạy Dự án (Getting Started)

### 1. Chuẩn bị Cơ sở dữ liệu (MySQL / XAMPP)
1. Cài đặt và khởi động **Apache & MySQL** trong XAMPP (hoặc MySQL Server).
2. Mở phpMyAdmin (`http://localhost/phpmyadmin`).
3. Tạo cơ sở dữ liệu mới với tên: `movie_db` (bảng mã `utf8mb4_unicode_ci`).
4. Nhập (Import) file **`movie_db.sql`** có sẵn trong thư mục gốc của project.
5. Kiểm tra cấu hình kết nối trong file `app/src/main/java/com/example/backup/database/DBConnection.java`:
   - Chạy trên máy ảo Android Emulator: dùng IP `10.0.2.2`.
   - Chạy trên điện thoại thật: đổi thành IP mạng LAN máy tính của bạn (ví dụ: `192.168.1.15`).

### 2. Mở và Chạy Ứng dụng trong Android Studio
1. Mở Android Studio -> Chọn **Open** -> Chọn thư mục project `backup`.
2. Chờ Gradle đồng bộ (Sync) dependencies hoàn tất.
3. Bấm nút **Run ▶** (màu xanh) để khởi chạy trên máy ảo hoặc thiết bị thật.

### 3. Tài khoản thử nghiệm (Test Accounts)
- **Tài khoản Admin**:
  - Tên đăng nhập: `admin`
  - Mật khẩu: `123456`
- **Tài khoản Người dùng**:
  - Tên đăng nhập: `user`
  - Mật khẩu: `123456`
  *(Hoặc bấm đăng nhập bằng Google / Đăng ký tài khoản mới)*.

---

## 📄 License
Dự án được phát hành dưới giấy phép [MIT License](LICENSE).
