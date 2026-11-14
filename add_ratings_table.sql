-- ========================================================
-- Script tạo bảng movie_ratings cho chức năng đánh giá sao (1-5 sao)
-- Chạy trong phpMyAdmin: chọn database movie_db -> tab SQL -> paste -> Go
-- ========================================================

USE movie_db;

CREATE TABLE IF NOT EXISTS movie_ratings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating FLOAT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_movie_rating (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);

-- Thêm một số đánh giá mẫu để kiểm tra hiển thị điểm cộng đồng
INSERT INTO movie_ratings (user_id, movie_id, rating) VALUES
(1, 1, 5.0),
(1, 2, 5.0),
(1, 5, 5.0),
(1, 10, 4.5),
(1, 12, 4.5)
ON DUPLICATE KEY UPDATE rating = VALUES(rating);
