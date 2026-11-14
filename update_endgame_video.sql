-- ========================================================
-- Script cập nhật video phát cho Avengers: Endgame
-- Chạy script này trong phpMyAdmin (database: movie_db)
-- ========================================================

USE movie_db;

UPDATE movies 
SET video_url = 'raw/endgame' 
WHERE tmdb_id = 299534 
   OR title LIKE '%Endgame%' 
   OR original_title LIKE '%Endgame%'
   OR title LIKE '%Hồi Kết%';

-- Kiểm tra lại kết quả
SELECT id, tmdb_id, title, original_title, video_url 
FROM movies 
WHERE video_url = 'raw/endgame';
