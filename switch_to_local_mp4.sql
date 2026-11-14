-- ================================================================
-- CHUYỂN NGUỒN PHÁT VIDEO SANG FILE MP4 NỘI BỘ (res/raw/)
-- Chạy file này trên phpMyAdmin (CSDL movie_db) để xóa toàn bộ URL online cũ
-- ================================================================

-- 1. Phim Avengers: Endgame trỏ về file endgame.mp4 trong thư mục res/raw/
UPDATE movies SET video_url = 'raw/endgame.mp4' 
WHERE tmdb_id = 299534 OR title LIKE '%Endgame%' OR title LIKE '%Hồi Kết%';

-- 2. Tất cả các phim còn lại chuyển về file sample_movie.mp4 nội bộ
UPDATE movies SET video_url = 'raw/sample_movie.mp4' 
WHERE video_url LIKE '%commondatastorage%' OR video_url IS NULL OR video_url = '';
