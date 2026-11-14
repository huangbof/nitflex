-- ====================================================
-- CẬP NHẬT TÍNH NĂNG VIP CHO NIT FLEX
-- ====================================================

USE movie_db;

-- 1. Bổ sung cột is_vip vào bảng users (nếu chưa có)
-- 0: Tài khoản thường (Free)
-- 1: Tài khoản VIP (Xem phim > 7.0 TMDB không giới hạn)
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_vip TINYINT DEFAULT 0;

-- Cập nhật mẫu tài khoản admin (id = 1) làm tài khoản VIP để test
UPDATE users SET is_vip = 1 WHERE id = 1;
