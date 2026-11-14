-- Cập nhật bảng watch_history để lưu trữ thời lượng tổng của phim
-- Chạy câu lệnh này trong phpMyAdmin (database: movie_db) nếu bảng watch_history đã tồn tại trước đó:

ALTER TABLE watch_history ADD COLUMN duration INT DEFAULT 0;
