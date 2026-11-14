-- =============================================
-- MovieApp Database Schema & Sample Data (TMDB)
-- Compatible with MySQL 5.7+ / MariaDB 10.3+ (XAMPP)
-- =============================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS movie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE movie_db;

-- =============================================
-- BẢNG genres (theo TMDB genre IDs chuẩn)
-- =============================================
CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- =============================================
-- BẢNG movies (theo cấu trúc TMDB)
-- =============================================
CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id INT UNIQUE,                    -- ID từ TMDB
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT,                         -- Tóm tắt nội dung (Tiếng Việt)
    poster_path VARCHAR(500),              -- Đường dẫn poster TMDB
    backdrop_path VARCHAR(500),            -- Đường dẫn banner TMDB
    release_date DATE,
    vote_average FLOAT DEFAULT 0,
    vote_count INT DEFAULT 0,
    popularity FLOAT DEFAULT 0,
    original_language VARCHAR(10),
    adult TINYINT(1) DEFAULT 0,
    video_url VARCHAR(500),                -- URL luồng video phát phim
    view_count INT DEFAULT 0,
    genre_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (genre_id) REFERENCES genres(id),
    INDEX idx_movies_title (title),
    INDEX idx_movies_vote (vote_average),
    INDEX idx_movies_pop (popularity),
    INDEX idx_movies_genre (genre_id)
);

-- =============================================
-- BẢNG users
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) UNIQUE,
    avatar_url VARCHAR(500) DEFAULT NULL,
    is_vip TINYINT DEFAULT 0,              -- 0: Tài khoản thường, 1: Tài khoản VIP
    role VARCHAR(50) DEFAULT 'user',       -- 'admin': Quản trị viên, 'user': Người dùng
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- BẢNG favorites
-- =============================================
CREATE TABLE IF NOT EXISTS favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_fav (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG comments
-- =============================================
CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    INDEX idx_comments_movie (movie_id)
);

-- =============================================
-- BẢNG watch_history
-- =============================================
CREATE TABLE IF NOT EXISTS watch_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    progress INT DEFAULT 0,                -- Giây đã xem
    duration INT DEFAULT 0,                -- Tổng thời lượng phim (giây)
    UNIQUE KEY unique_history (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);

-- =============================================
-- BẢNG movie_ratings (Đánh giá chấm điểm sao 1 - 5 sao)
-- =============================================
CREATE TABLE IF NOT EXISTS movie_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating FLOAT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_movie_rating (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    INDEX idx_ratings_movie (movie_id)
);


-- =============================================
-- DỮ LIỆU MẪU
-- =============================================

-- Genres theo TMDB chuẩn
INSERT INTO genres (id, name) VALUES
(28, 'Hành Động'),
(12, 'Phiêu Lưu'),
(16, 'Hoạt Hình'),
(35, 'Hài Hước'),
(80, 'Tội Phạm'),
(18, 'Tâm Lý'),
(10751, 'Gia Đình'),
(14, 'Giả Tưởng'),
(27, 'Kinh Dị'),
(9648, 'Bí Ẩn'),
(10749, 'Lãng Mạn'),
(878, 'Khoa Học Viễn Tưởng'),
(53, 'Giật Gân'),
(10752, 'Chiến Tranh'),
(37, 'Miền Tây');

-- 20 phim mẫu (dữ liệu thực từ TMDB kèm tóm tắt Tiếng Việt)
INSERT INTO movies (tmdb_id, title, original_title, overview, poster_path, backdrop_path, release_date, vote_average, vote_count, popularity, original_language, genre_id, video_url) VALUES
(299536, 'Avengers: Cuộc Chiến Vô Cực', 'Avengers: Infinity War',
 'Sau nhiều năm bảo vệ Trái Đất trước vô số hiểm họa, biệt đội Avengers và các đồng minh phải đối mặt với mối đe dọa khủng khiếp nhất từ bóng tối vũ trụ: Thanos - kẻ mang dã tâm thu thập đủ 6 Viên đá Vô cực để xóa sổ một nửa sinh linh trong thiên hà.',
 'https://image.tmdb.org/t/p/w500/7WsyChQLEftFiDOVTGkv3hFpyyt.jpg',
 'https://image.tmdb.org/t/p/original/mDfJG3LC3Dqb67AZ52x3Z0jU0uB.jpg',
 '2018-04-25', 8.3, 24000, 290.5, 'en', 28,
 'raw/avengers_infinity_war.mp4'),

(299534, 'Avengers: Hồi Kết', 'Avengers: Endgame',
 'Sau sự kiện tàn khốc của Cuộc chiến Vô cực, vũ trụ rơi vào hoang tàn bởi cái búng tay của Thanos. Những Avengers còn sống sót tập hợp lại một lần cuối, cùng nhau bước vào trận chiến quyết định để đảo ngược thảm kịch và hồi sinh những người đã mất.',
 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg',
 'https://image.tmdb.org/t/p/original/7RyHsO4yDXtBv1zUU3mTpHeQ0d5.jpg',
 '2019-04-24', 8.3, 23000, 310.0, 'en', 28,
 'raw/endgame.mp4'),

(19995, 'Avatar', 'Avatar',
 'Ở thế kỷ 22, Jake Sully - một cựu lính thủy đánh bộ bị liệt hai chân - được đưa đến hành tinh Pandora trong một nhiệm vụ đặc biệt. Tại đây, anh bị giằng xé giữa mệnh lệnh của quân đội loài người và tình yêu dành cho bộ tộc Na''vi cùng vùng đất hoang sơ tuyệt đẹp.',
 'https://image.tmdb.org/t/p/w500/jRXYjXNq0Cs2TcJjLkki24MLp7u.jpg',
 'https://image.tmdb.org/t/p/original/o0s4XsEDfDlvit5pDRKjzXR4pp2.jpg',
 '2009-12-10', 7.6, 29000, 185.4, 'en', 878,
 'raw/avatar.mp4'),

(76600, 'Avatar: Dòng Chảy Của Nước', 'Avatar: The Way of Water',
 'Hơn một thập kỷ sau phần đầu, câu chuyện tiếp tục theo chân gia đình Sully (Jake, Neytiri và các con). Khi những mối nguy hiểm cũ quay trở lại, họ phải rời bỏ quê hương, tìm đến nương náu tại các vùng biển Pandora và chiến đấu quả cảm để bảo vệ gia đình.',
 'https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg',
 'https://image.tmdb.org/t/p/original/8YFL5QQVPy3AgrEQxNYVSgiPEbe.jpg',
 '2022-12-14', 7.6, 10500, 254.3, 'en', 878,
 'raw/avatar_the_way_of_water.mp4'),

(157336, 'Hố Đen Tử Thần', 'Interstellar',
 'Khi Trái Đất dần cạn kiệt tài nguyên và đối mặt với nguy cơ tuyệt chủng, một nhóm phi hành gia dũng cảm vượt qua lỗ sâu không gian bí ẩn ngoài vũ trụ để tìm kiếm một mái nhà mới, mở ra hành trình kỳ vĩ vượt qua mọi giới hạn của thời gian và không gian.',
 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
 'https://image.tmdb.org/t/p/original/xJHokMbljvjADYdit5fK5VQsXEG.jpg',
 '2014-11-05', 8.4, 34000, 164.2, 'en', 878,
 'raw/interstellar.mp4'),

(550, 'Sàn Đấu Sinh Tử', 'Fight Club',
 'Một nhân viên văn phòng mất ngủ kinh niên tình cờ gặp Tyler Durden - tay buôn xà phòng bí ẩn đầy nổi loạn. Họ cùng nhau sáng lập "Sàn đấu sinh tử" ngầm nhằm giải tỏa bạo lực bản năng, nhanh chóng biến nó thành một phong trào ngầm gây chấn động toàn xã hội.',
 'https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg',
 'https://image.tmdb.org/t/p/original/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg',
 '1999-10-15', 8.4, 28000, 73.1, 'en', 18,
 'raw/fight_club.mp4'),

(238, 'Bố Già', 'The Godfather',
 'Tác phẩm kinh điển xoay quanh gia tộc mafia lừng lẫy Corleone giai đoạn 1945-1955. Khi "Bố già" Vito Corleone may mắn sống sót sau một vụ ám sát, người con trai út Michael Corleone - vốn đứng ngoài vòng tội ác - buộc phải bước vào cuộc chiến quyền lực đẫm máu.',
 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg',
 'https://image.tmdb.org/t/p/original/tmU7GeKVybMWFButWEGl2M4GeiP.jpg',
 '1972-03-14', 8.7, 19000, 109.5, 'en', 80,
 'raw/the_godfather.mp4'),

(240, 'Bố Già Phần II', 'The Godfather Part II',
 'Bộ phim đan xen giữa hai mốc thời gian: hành trình lập nghiệp đầy thăng trầm của Vito Corleone thời trẻ tại New York đầu thế kỷ 20 và nỗ lực củng cố đế chế gia tộc của người kế thừa Michael Corleone khi mở rộng tầm ảnh hưởng sang Las Vegas và Cuba.',
 'https://image.tmdb.org/t/p/w500/hek3koDUyRQk7FIhPXsa6mT2Zc3.jpg',
 'https://image.tmdb.org/t/p/original/poec6RqOKY9iSiIUmfyfPfiLtvB.jpg',
 '1974-12-20', 8.6, 11000, 92.3, 'en', 80,
 'raw/the_godfather_part_ii.mp4'),

(372058, 'Tên Cậu Là Gì?', 'Your Name',
 'Mitsuha - nữ sinh vùng nông thôn yên bình và Taki - nam sinh năng động ở Tokyo bất ngờ phát hiện họ bị hoán đổi thân xác trong giấc mơ. Khi bí mật về một thảm họa thiên thạch ập đến được hé lộ, cả hai bắt đầu cuộc hành trình vượt thời gian đầy kỳ diệu để tìm thấy nhau.',
 'https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKnONONX.jpg',
 'https://image.tmdb.org/t/p/original/mMtUybQ6hL24FXo0F3Z4j2KG7kZ.jpg',
 '2016-08-26', 8.4, 12000, 112.8, 'ja', 16,
 'raw/your_name.mp4'),

(129, 'Vùng Đất Linh Hồn', 'Spirited Away',
 'Cô bé Chihiro vô tình lạc vào thế giới linh hồn kỳ bí sau khi bố mẹ bị biến thành heo vì lòng tham. Để cứu gia đình và tìm đường trở về thế giới loài người, cô bé phải dũng cảm làm việc tại nhà tắm công cộng của phù thủy Yubaba đầy mưu mô.',
 'https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg',
 'https://image.tmdb.org/t/p/original/dyJvKsNs2KP8qQnAXbRwDjblViy.jpg',
 '2001-07-20', 8.5, 15000, 97.3, 'ja', 16,
 'raw/spirited_away.mp4'),

(27205, 'Kẻ Đánh Cắp Giấc Mơ', 'Inception',
 'Dom Cobb là một đạo chích bậc thầy chuyên xâm nhập vào tiềm thức của mục tiêu trong lúc ngủ để đánh cắp những bí mật giá trị nhất. Anh được trao cơ hội chuộc lại cuộc đời tự do với một nhiệm vụ bất khả thi: cấy một ý tưởng vào tâm trí người khác thay vì đánh cắp nó.',
 'https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg',
 'https://image.tmdb.org/t/p/original/s3TBrRGB1iav7gFOCNx3H31MoES.jpg',
 '2010-07-14', 8.3, 36000, 143.6, 'en', 878,
 'raw/inception.mp4'),

(680, 'Chuyện Tào Lao', 'Pulp Fiction',
 'Kiệt tác hành động đan xen số phận của hai sát thủ hài hước, cô vợ quyến rũ mê ma túy của ông trùm, một võ sĩ quyền anh kiêu hãnh và cặp đôi cướp quán ăn, tạo nên một bức tranh tội phạm hỗn loạn nhưng đầy nghệ thuật và châm biếm.',
 'https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg',
 'https://image.tmdb.org/t/p/original/suaEOtk1N1sgg2MTM7oZd2cfVp3.jpg',
 '1994-09-10', 8.5, 27000, 85.9, 'en', 80,
 'raw/pulp_fiction.mp4'),

(13, 'Cuộc Đời Forrest Gump', 'Forrest Gump',
 'Câu chuyện lay động lòng người về Forrest Gump - một chàng trai có chỉ số IQ thấp nhưng sở hữu trái tim vàng lương thiện. Bằng sự chân thành và nghị lực phi thường, anh đã trải qua những cột mốc lịch sử vĩ đại của nước Mỹ và luôn một lòng hướng về tình yêu thời thơ ấu.',
 'https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg',
 'https://image.tmdb.org/t/p/original/qdIMHd4sEfJSckfVJfKQvisL02a.jpg',
 '1994-07-06', 8.5, 26000, 110.7, 'en', 18,
 'raw/forrest_gump.mp4'),

(278, 'Nhà Tù Shawshank', 'The Shawshank Redemption',
 'Andy Dufresne - một nhân viên ngân hàng tài năng bị kết án oan vì tội sát hại vợ và nhân tình, chịu mức án chung thân tại nhà tù Shawshank khắc nghiệt. Trải qua gần hai thập kỷ với lòng kiên trì và niềm tin bất diệt vào tự do, anh đã tạo nên cuộc đào thoát ngoạn mục nhất lịch sử.',
 'https://image.tmdb.org/t/p/w500/lyQBXzOQSuE59IsHyhrp0qIiPAz.jpg',
 'https://image.tmdb.org/t/p/original/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg',
 '1994-09-23', 8.7, 26000, 92.4, 'en', 18,
 'raw/the_shawshank_redemption.mp4'),

(122, 'Chúa Tể Những Chiếc Nhẫn: Sự Trở Lại Của Nhà Vua', 'The Lord of the Rings: The Return of the King',
 'Hồi kết vĩ đại của thiên trường ca Chúa Nhẫn. Trong khi Aragorn đứng lên thừa kế ngai vàng và dẫn dắt liên minh các chủng tộc trong trận chiến sinh tử tại Minas Tirith, Frodo và Sam vượt qua hiểm nguy khôn cùng để đưa Chiếc Nhẫn Quyền Năng đến ngọn lửa Núi Diệt Vong.',
 'https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg',
 'https://image.tmdb.org/t/p/original/2u7zbn8EudG6kLlBzUYqP8RyFU4.jpg',
 '2003-12-01', 8.5, 22000, 95.8, 'en', 14,
 'raw/return_of_the_king.mp4'),

(769, 'Những Chiến Hữu', 'GoodFellas',
 'Dựa trên câu chuyện có thật về Henry Hill - cậu thiếu niên lớn lên trong một khu phố nghèo ở Brooklyn và nuôi mộng trở thành một gangster khét tiếng. Bước chân vào thế giới ngầm New York hoa lệ nhưng tàn khốc, anh sớm nhận ra cái giá đắt của quyền lực và lòng trung thành.',
 'https://image.tmdb.org/t/p/w500/aKuFiU82s5ISJpGZp7YkIr3kCUd.jpg',
 'https://image.tmdb.org/t/p/original/sw7mordbZxgITU877yTpZCud90M.jpg',
 '1990-09-12', 8.5, 12000, 55.0, 'en', 80,
 'raw/goodfellas.mp4'),

(497, 'Dặm Xanh', 'The Green Mile',
 'Tại trại giam tử tù thời kỳ Đại suy thoái, viên quản giáo Paul Edgecomb gặp gỡ John Coffey - một phạm nhân da đen to lớn mang án tử hình nhưng lại sở hữu tấm lòng nhân hậu vô bờ và siêu năng lực chữa lành thần kỳ mang tầm vóc của một phép màu.',
 'https://image.tmdb.org/t/p/w500/8Tfys3mDZVp4tNoH2ktm06a0Tau.jpg',
 'https://image.tmdb.org/t/p/original/39Bz6NsXq3d2CcRpMXem7ixOrwN.jpg',
 '1999-12-10', 8.5, 15000, 71.4, 'en', 18,
 'raw/the_green_mile.mp4'),

(598, 'Thành Phố Của Chúa', 'City of God',
 'Bức tranh trần trụi về khu ổ chuột khét tiếng Cidade de Deus tại Rio de Janeiro thập niên 1960-1980. Hai cậu bé cùng lớn lên tại đây nhưng chọn hai ngã rẽ hoàn toàn trái ngược: một người trở thành nhiếp ảnh gia tài ba, người kia trở thành trùm ma túy tàn độc.',
 'https://image.tmdb.org/t/p/w500/k7eYdWvhYQyRQoU2TB2A2Xu2TfD.jpg',
 'https://image.tmdb.org/t/p/original/k7eYdWvhYQyRQoU2TB2A2Xu2TfD.jpg',
 '2002-08-30', 8.6, 9000, 42.1, 'pt', 80,
 'raw/city_of_god.mp4'),

(361743, 'Phi Công Siêu Đẳng: Maverick', 'Top Gun: Maverick',
 'Sau hơn 30 năm cống hiến với tư cách phi công chiến đấu cự phách của Hải quân Mỹ, Pete "Maverick" Mitchell trở lại để huấn luyện một đội phi công trẻ ưu tú cho một nhiệm vụ sinh tử vô cùng hiểm hóc, nơi anh phải đối mặt với bóng ma quá khứ và thử thách sinh tồn cam go nhất.',
 'https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg',
 'https://image.tmdb.org/t/p/original/odJ4hx6g6vBt4lBWKFD1tI8WS4x.jpg',
 '2022-05-24', 8.3, 16000, 299.4, 'en', 28,
 'raw/top_gun_maverick.mp4');

-- Users mẫu (password = md5("123456") = e10adc3949ba59abbe56e057f20f883e)
INSERT INTO users (id, username, password_hash, email, role) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@movieapp.com', 'admin'),
(2, 'user1', 'e10adc3949ba59abbe56e057f20f883e', 'user1@example.com', 'user'),
(3, 'user2', 'e10adc3949ba59abbe56e057f20f883e', 'user2@example.com', 'user'),
(9999, 'guest_fallback', 'e10adc3949ba59abbe56e057f20f883e', 'guest@nitflex.local', 'user')
ON DUPLICATE KEY UPDATE id=id;

-- Favorites mẫu
INSERT INTO favorites (user_id, movie_id) VALUES (1, 1), (1, 3), (1, 5), (2, 1), (2, 7);

-- Comments mẫu
INSERT INTO comments (user_id, movie_id, content) VALUES
(1, 1, 'Phim hay tuyệt vời! Kỹ xảo và cốt truyện đỉnh chóp.'),
(2, 1, 'Cảnh đại chiến ở Wakanda quá mãn nhãn!'),
(1, 5, 'Interstellar là phim khoa học viễn tưởng hay nhất từng được xem.'),
(3, 7, 'The Godfather - kinh điển bất hủ của điện ảnh thế giới.');


-- ========================================================
-- 7. BẢNG DIỄN VIÊN (CAST)
-- ========================================================
CREATE TABLE IF NOT EXISTS movie_casts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    character_name VARCHAR(100),
    profile_path VARCHAR(500),
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    INDEX idx_casts_movie (movie_id)
);

-- 1. Avengers: Cuộc Chiến Vô Cực (tmdb_id = 299536)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert Downey Jr.', 'Tony Stark / Iron Man', 'https://image.tmdb.org/t/p/w200/im9SAqJPZKEbVZGmjXuLI4O7RvM.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Chris Evans', 'Steve Rogers / Captain America', 'https://image.tmdb.org/t/p/w200/3bOGNsHlrswhyW79uvIHH1V43JI.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Chris Hemsworth', 'Thor Odinson', 'https://image.tmdb.org/t/p/w200/jpurJ9jAcLCYjgHHfYF32m3zJYm.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Mark Ruffalo', 'Bruce Banner / Hulk', 'https://image.tmdb.org/t/p/w200/5GilhnOwtVj5sZz0r144c8Abehn.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Scarlett Johansson', 'Natasha Romanoff / Black Widow', 'https://image.tmdb.org/t/p/w200/6NsMbJXRlDZuDzatJA4ak4ho19f.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Benedict Cumberbatch', 'Dr. Stephen Strange', 'https://image.tmdb.org/t/p/w200/fBEWsHgKU9KoUb9xvt957n0bbaW.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tom Holland', 'Peter Parker / Spider-Man', 'https://image.tmdb.org/t/p/w200/bBRqqoK1v3g9wS9MwhB4iA1yS4r.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Chadwick Boseman', 'T''Challa / Black Panther', 'https://image.tmdb.org/t/p/w200/nL1OMkssg2q57656A92z0jX1N8m.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Josh Brolin', 'Thanos', 'https://image.tmdb.org/t/p/w200/sX2etBbIkxRaCsATyw5ZpOVMPTD.jpg' FROM movies WHERE tmdb_id = 299536;

-- 2. Avengers: Hồi Kết (tmdb_id = 299534)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert Downey Jr.', 'Tony Stark / Iron Man', 'https://image.tmdb.org/t/p/w200/im9SAqJPZKEbVZGmjXuLI4O7RvM.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Chris Evans', 'Steve Rogers / Captain America', 'https://image.tmdb.org/t/p/w200/3bOGNsHlrswhyW79uvIHH1V43JI.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Scarlett Johansson', 'Natasha Romanoff / Black Widow', 'https://image.tmdb.org/t/p/w200/6NsMbJXRlDZuDzatJA4ak4ho19f.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Chris Hemsworth', 'Thor Odinson', 'https://image.tmdb.org/t/p/w200/jpurJ9jAcLCYjgHHfYF32m3zJYm.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Mark Ruffalo', 'Bruce Banner / Hulk', 'https://image.tmdb.org/t/p/w200/5GilhnOwtVj5sZz0r144c8Abehn.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Jeremy Renner', 'Clint Barton / Hawkeye', 'https://image.tmdb.org/t/p/w200/yB84D1cNmYATHDRNoFqI9RaHXnR.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Paul Rudd', 'Scott Lang / Ant-Man', 'https://image.tmdb.org/t/p/w200/8eTtJ7XVXY0BnEeUaItewmTo7T.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Brie Larson', 'Carol Danvers / Captain Marvel', 'https://image.tmdb.org/t/p/w200/8cduWpA4z6e50G8A8tZ0eW1h38I.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Josh Brolin', 'Thanos', 'https://image.tmdb.org/t/p/w200/sX2etBbIkxRaCsATyw5ZpOVMPTD.jpg' FROM movies WHERE tmdb_id = 299534;

-- 3. Avatar (tmdb_id = 19995)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Sam Worthington', 'Jake Sully', 'https://image.tmdb.org/t/p/w200/blAIFd0dK98gBqUe7WqJpI5V5iK.jpg' FROM movies WHERE tmdb_id = 19995;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Zoe Saldana', 'Neytiri', 'https://image.tmdb.org/t/p/w200/iOVbEG29il6b9Ge2v09y5JbvZ5c.jpg' FROM movies WHERE tmdb_id = 19995;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Sigourney Weaver', 'Dr. Grace Augustine', 'https://image.tmdb.org/t/p/w200/bcCoc3mY44hyCqf1e0wJ7U5o6Wk.jpg' FROM movies WHERE tmdb_id = 19995;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Stephen Lang', 'Đại tá Miles Quaritch', 'https://image.tmdb.org/t/p/w200/xUTK8qI1CqTz6a51d4LpG5vK9QY.jpg' FROM movies WHERE tmdb_id = 19995;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Michelle Rodriguez', 'Trudy Chacon', 'https://image.tmdb.org/t/p/w200/hulLHGUxO1v5L1N4W3xMvQ7oK1w.jpg' FROM movies WHERE tmdb_id = 19995;

-- 4. Avatar: Dòng Chảy Của Nước (tmdb_id = 76600)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Sam Worthington', 'Jake Sully', 'https://image.tmdb.org/t/p/w200/blAIFd0dK98gBqUe7WqJpI5V5iK.jpg' FROM movies WHERE tmdb_id = 76600;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Zoe Saldana', 'Neytiri', 'https://image.tmdb.org/t/p/w200/iOVbEG29il6b9Ge2v09y5JbvZ5c.jpg' FROM movies WHERE tmdb_id = 76600;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Sigourney Weaver', 'Kiri', 'https://image.tmdb.org/t/p/w200/bcCoc3mY44hyCqf1e0wJ7U5o6Wk.jpg' FROM movies WHERE tmdb_id = 76600;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Stephen Lang', 'Miles Quaritch Recombinant', 'https://image.tmdb.org/t/p/w200/xUTK8qI1CqTz6a51d4LpG5vK9QY.jpg' FROM movies WHERE tmdb_id = 76600;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Kate Winslet', 'Ronal', 'https://image.tmdb.org/t/p/w200/e3td5Yvmpd7q6a0h0352yJmG1m2.jpg' FROM movies WHERE tmdb_id = 76600;

-- 5. Hố Đen Tử Thần (Interstellar - tmdb_id = 157336)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Matthew McConaughey', 'Joseph Cooper', 'https://image.tmdb.org/t/p/w200/wDeL5g8O2qZgA3j9K7g8e2L4m7y.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Anne Hathaway', 'Dr. Amelia Brand', 'https://image.tmdb.org/t/p/w200/tLfl4w1lTnhBt6vB07A9r8q7h9c.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Jessica Chastain', 'Murphy "Murph" Cooper', 'https://image.tmdb.org/t/p/w200/lodMzLKS5G3e0y6d5N7mH8k1p8q.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Michael Caine', 'Giáo sư John Brand', 'https://image.tmdb.org/t/p/w200/klNxZ3x8H98e5e8a7m8d6G3j2.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Matt Damon', 'Dr. Mann', 'https://image.tmdb.org/t/p/w200/elSlNg0W2155H40c2M16pD29w2.jpg' FROM movies WHERE tmdb_id = 157336;

-- 6. Sàn Đấu Sinh Tử (Fight Club - tmdb_id = 550)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Edward Norton', 'The Narrator (Người kể chuyện)', 'https://image.tmdb.org/t/p/w200/5XBzD5WuTyEGZeBYRWmIU20YSP.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Brad Pitt', 'Tyler Durden', 'https://image.tmdb.org/t/p/w200/cckcYc2v0yh1tc9QjRelptsqoS4.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Helena Bonham Carter', 'Marla Singer', 'https://image.tmdb.org/t/p/w200/DDeITcCEMqlPnqz6h1f7p7m9Nq.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Meat Loaf', 'Robert "Bob" Paulson', 'https://image.tmdb.org/t/p/w200/7aZ8aG8y7q1a2w3e4r5t6y7u8i9.jpg' FROM movies WHERE tmdb_id = 550;

-- 7. Bố Già (The Godfather - tmdb_id = 238)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Marlon Brando', 'Don Vito Corleone', 'https://image.tmdb.org/t/p/w200/fuTEPMsBtV1zE98D65kZ4e8p8r8.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Al Pacino', 'Michael Corleone', 'https://image.tmdb.org/t/p/w200/fMD0vh9b8j4v5w6e7r8t9y0u1i2.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'James Caan', 'Sonny Corleone', 'https://image.tmdb.org/t/p/w200/b0hXw7r6y5t4e3w2q1a0z9x8c7v.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert Duvall', 'Tom Hagen', 'https://image.tmdb.org/t/p/w200/z0x9c8v7b6n5m4a3s2d1f0g9h8j.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Diane Keaton', 'Kay Adams', 'https://image.tmdb.org/t/p/w200/q9w8e7r6t5y4u3i2o1p0a9s8d7f.jpg' FROM movies WHERE tmdb_id = 238;

-- 8. Bố Già Phần II (The Godfather Part II - tmdb_id = 240)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Al Pacino', 'Michael Corleone', 'https://image.tmdb.org/t/p/w200/fMD0vh9b8j4v5w6e7r8t9y0u1i2.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert De Niro', 'Vito Corleone (Thời trẻ)', 'https://image.tmdb.org/t/p/w200/cT8htcckAL1YUID9q3xP4qQJ2vH.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert Duvall', 'Tom Hagen', 'https://image.tmdb.org/t/p/w200/z0x9c8v7b6n5m4a3s2d1f0g9h8j.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Diane Keaton', 'Kay Adams Corleone', 'https://image.tmdb.org/t/p/w200/q9w8e7r6t5y4u3i2o1p0a9s8d7f.jpg' FROM movies WHERE tmdb_id = 240;

-- 9. Bản Danh Sách Của Schindler (Schindler List - tmdb_id = 424)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Liam Neeson', 'Oskar Schindler', 'https://image.tmdb.org/t/p/w200/wPWsq78M4G9W9vW1xK8k8vW9y0m.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ben Kingsley', 'Itzhak Stern', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ralph Fiennes', 'Amon Göth', 'https://image.tmdb.org/t/p/w200/t2h0w2K8s9q3e4r5t6y7u8i9o0p.jpg' FROM movies WHERE tmdb_id = 424;

-- 10. Tên Cậu Là Gì? (Your Name - tmdb_id = 372058)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ryunosuke Kamiki', 'Taki Tachibana (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/c7a5w4e3r2t1y0u9i8o7p6a5s4d.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Mone Kamishiraishi', 'Mitsuha Miyamizu (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/b1v2c3x4z5a6s7d8f9g0h1j2k3l.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ryo Narita', 'Katsuhiko Teshigawara (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/m1n2b3v4c5x6z7a8s9d0f1g2h3j.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Masami Nagasawa', 'Miki Okudera (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/k1j2h3g4f5d6s7a8z9x0c1v2b3n.jpg' FROM movies WHERE tmdb_id = 372058;

-- 11. Vùng Đất Linh Hồn (Spirited Away - tmdb_id = 129)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Rumi Hiiragi', 'Chihiro Ogino / Sen (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/q1w2e3r4t5y6u7i8o9p0a1s2d3f.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Miyu Irino', 'Haku (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/z1x2c3v4b5n6m7a8s9d0f1g2h3j.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Mari Natsuki', 'Yubaba / Zeniba (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/m1k2j3h4g5f6d7s8a9z0x1c2v3b.jpg' FROM movies WHERE tmdb_id = 129;

-- 12. Kẻ Đánh Cắp Giấc Mơ (Inception - tmdb_id = 27205)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Leonardo DiCaprio', 'Dom Cobb', 'https://image.tmdb.org/t/p/w200/wo2hJpn04vbtmh0B9utCFdsQhxM.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Joseph Gordon-Levitt', 'Arthur', 'https://image.tmdb.org/t/p/w200/dhv9y2vW7Q3d4e5f6g7h8j9k0l1.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Elliot Page', 'Ariadne', 'https://image.tmdb.org/t/p/w200/m1k2l3j4h5g6f7d8s9a0z1x2c3v.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tom Hardy', 'Eames', 'https://image.tmdb.org/t/p/w200/yA3w2K9m1n0b8v7c6x5z4a3s2d1.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Cillian Murphy', 'Robert Michael Fischer', 'https://image.tmdb.org/t/p/w200/27ba6e1xP21w00p19G9Zl82j1p9.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ken Watanabe', 'Mr. Saito', 'https://image.tmdb.org/t/p/w200/z0x9c8v7b6n5m4a3s2d1f0g9h8j.jpg' FROM movies WHERE tmdb_id = 27205;

-- 13. Chuyện Tào Lao (Pulp Fiction - tmdb_id = 680)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'John Travolta', 'Vincent Vega', 'https://image.tmdb.org/t/p/w200/yA7w8e9r0t1y2u3i4o5p6a7s8d9.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Samuel L. Jackson', 'Jules Winnfield', 'https://image.tmdb.org/t/p/w200/nCJJ3NVksHNtuPtFGyB8g1LwO0o.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Uma Thurman', 'Mia Wallace', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Bruce Willis', 'Butch Coolidge', 'https://image.tmdb.org/t/p/w200/yA3w2K9m1n0b8v7c6x5z4a3s2d1.jpg' FROM movies WHERE tmdb_id = 680;

-- 14. Cuộc Đời Forrest Gump (tmdb_id = 13)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tom Hanks', 'Forrest Gump', 'https://image.tmdb.org/t/p/w200/xNdFsrA7L3h8j6m3k2v5b8w0z9q.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robin Wright', 'Jenny Curran', 'https://image.tmdb.org/t/p/w200/c7a5w4e3r2t1y0u9i8o7p6a5s4d.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Gary Sinise', 'Trung úy Dan Taylor', 'https://image.tmdb.org/t/p/w200/yA3w2K9m1n0b8v7c6x5z4a3s2d1.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Sally Field', 'Bà Gump (Mẹ Forrest)', 'https://image.tmdb.org/t/p/w200/a1s2d3f4g5h6j7k8l9z0x1c2v3b.jpg' FROM movies WHERE tmdb_id = 13;

-- 15. Nhà Tù Shawshank (tmdb_id = 278)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tim Robbins', 'Andy Dufresne', 'https://image.tmdb.org/t/p/w200/yA7w8e9r0t1y2u3i4o5p6a7s8d9.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Morgan Freeman', 'Ellis Boyd "Red" Redding', 'https://image.tmdb.org/t/p/w200/oGJQhOpT8S1M58Apz6LulUqIQfN.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Bob Gunton', 'Cai ngục Samuel Norton', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Clancy Brown', 'Đội trưởng Byron Hadley', 'https://image.tmdb.org/t/p/w200/a1s2d3f4g5h6j7k8l9z0x1c2v3b.jpg' FROM movies WHERE tmdb_id = 278;

-- 16. Chúa Tể Những Chiếc Nhẫn: Sự Trở Lại Của Nhà Vua (tmdb_id = 122)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Elijah Wood', 'Frodo Baggins', 'https://image.tmdb.org/t/p/w200/7UKtLzFk7V5i6p8o9u0y1t2r3e4.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ian McKellen', 'Gandalf Áo Trắng', 'https://image.tmdb.org/t/p/w200/coWbZ0x8a9s1d2f3g4h5j6k7l8m.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Viggo Mortensen', 'Aragorn / Elessar', 'https://image.tmdb.org/t/p/w200/gK9x2v1b0n9m8a7s6d5f4g3h2j1.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Orlando Bloom', 'Legolas', 'https://image.tmdb.org/t/p/w200/qA1s2d3f4g5h6j7k8l9z0x1c2v3.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Andy Serkis', 'Gollum / Sméagol', 'https://image.tmdb.org/t/p/w200/yA3w2K9m1n0b8v7c6x5z4a3s2d1.jpg' FROM movies WHERE tmdb_id = 122;

-- 17. Những Chiến Hữu (GoodFellas - tmdb_id = 769)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Robert De Niro', 'James Conway', 'https://image.tmdb.org/t/p/w200/cT8htcckAL1YUID9q3xP4qQJ2vH.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Ray Liotta', 'Henry Hill', 'https://image.tmdb.org/t/p/w200/yA7w8e9r0t1y2u3i4o5p6a7s8d9.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Joe Pesci', 'Tommy DeVito', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 769;

-- 18. Dặm Xanh (The Green Mile - tmdb_id = 497)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tom Hanks', 'Paul Edgecomb', 'https://image.tmdb.org/t/p/w200/xNdFsrA7L3h8j6m3k2v5b8w0z9q.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Michael Clarke Duncan', 'John Coffey', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'David Morse', 'Brutus "Brutal" Howell', 'https://image.tmdb.org/t/p/w200/yA3w2K9m1n0b8v7c6x5z4a3s2d1.jpg' FROM movies WHERE tmdb_id = 497;

-- 19. Thành Phố Của Chúa (City of God - tmdb_id = 598)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Alexandre Rodrigues', 'Buscapé / Rocket', 'https://image.tmdb.org/t/p/w200/yA7w8e9r0t1y2u3i4o5p6a7s8d9.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Leandro Firmino', 'Zé Pequeno / Li''l Zé', 'https://image.tmdb.org/t/p/w200/bKxL8w1y0l2m3n4o5p6q7r8s9t0.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Phellipe Haagensen', 'Bené', 'https://image.tmdb.org/t/p/w200/z0x9c8v7b6n5m4a3s2d1f0g9h8j.jpg' FROM movies WHERE tmdb_id = 598;

-- 20. Phi Công Siêu Đẳng: Maverick (Top Gun: Maverick - tmdb_id = 361743)
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Tom Cruise', 'Pete "Maverick" Mitchell', 'https://image.tmdb.org/t/p/w200/eOhwo2y7ySVa89v45pU4L3v4o8.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Miles Teller', 'Bradley "Rooster" Bradshaw', 'https://image.tmdb.org/t/p/w200/4JyNW1Nf8YQY6W2Q08n1s5Zl90V.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Jennifer Connelly', 'Penny Benjamin', 'https://image.tmdb.org/t/p/w200/p1GvXf5y9iQ4uK6lP8d3h6e9mY.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Jon Hamm', 'Beau "Cyclone" Simpson', 'https://image.tmdb.org/t/p/w200/hB50gG2K8B0y1G2N220c3tG3i8.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Glen Powell', 'Jake "Hangman" Seresin', 'https://image.tmdb.org/t/p/w200/gK9x2v1b0n9m8a7s6d5f4g3h2j1.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path) 
SELECT id, 'Val Kilmer', 'Tom "Iceman" Kazansky', 'https://image.tmdb.org/t/p/w200/z0x9c8v7b6n5m4a3s2d1f0g9h8j.jpg' FROM movies WHERE tmdb_id = 361743;


-- =============================================
-- Dữ liệu đánh giá mẫu cho phim
-- =============================================
INSERT INTO movie_ratings (user_id, movie_id, rating) VALUES
(1, 1, 5.0),
(1, 2, 5.0),
(1, 3, 5.0),
(1, 5, 5.0),
(1, 6, 5.0),
(2, 1, 4.5),
(2, 7, 5.0)
ON DUPLICATE KEY UPDATE rating = VALUES(rating);

-- Kích hoạt VIP cho tài khoản admin
UPDATE users SET is_vip = 1 WHERE id = 1;


-- (Chỉ mục Index đã được tạo trực tiếp trong định nghĩa bảng ở trên để tương thích 100% mọi phiên bản MySQL/MariaDB)


-- =============================================
-- BẢNG vip_packages (Quản lý các gói thành viên VIP)
-- =============================================
CREATE TABLE IF NOT EXISTS vip_packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    duration_days INT NOT NULL,
    description VARCHAR(255),
    is_active TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO vip_packages (name, code, price, duration_days, description, is_active) VALUES
('VIP Tháng', 'VIP_1M', 59000, 30, 'Mở khóa toàn bộ phim điểm cao > 8.0 TMDB trong 30 ngày', 1),
('VIP 6 Tháng', 'VIP_6M', 299000, 180, 'Tiết kiệm 15% • Thưởng thức phim 4K không quảng cáo 6 tháng', 1),
('VIP Năm Hoàng Gia', 'VIP_1Y', 499000, 365, 'Đặc quyền tối thượng • Phim bom tấn > 8.0 & Âm thanh vòm Dolby', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price);
