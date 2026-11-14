-- ====================================================
-- TẠO BẢNG BÌNH LUẬN PHIM (MOVIE COMMENTS) CHO NIT FLEX
-- ====================================================

CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);

-- Thêm một số bình luận mẫu đặc sắc
INSERT INTO comments (user_id, movie_id, content) VALUES
(1, 1, 'Phim Avengers Endgame quá đỉnh! Cảnh đại chiến cuối cùng mãn nhãn từng giây.'),
(1, 1, 'Kỹ xảo xuất sắc, âm nhạc hào hùng làm nổi da gà.'),
(1, 2, 'Top Gun Maverick quá chân thực, âm thanh động cơ phản lực gầm rú cực đã tai!'),
(1, 3, 'Interstellar xứng đáng là kiệt tác khoa học viễn tưởng vĩ đại nhất thế kỷ 21.')
ON DUPLICATE KEY UPDATE content = VALUES(content);
