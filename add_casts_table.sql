-- ========================================================
-- Script tạo bảng và cập nhật đầy đủ diễn viên cho 20 phim
-- Chạy trong phpMyAdmin: chọn database movie_db → tab SQL → paste → Go
-- Cập nhật lần cuối: 2026-11-09
-- ========================================================

USE movie_db;

-- Tạo bảng movie_casts nếu chưa có
CREATE TABLE IF NOT EXISTS movie_casts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    actor_name VARCHAR(100) NOT NULL,
    character_name VARCHAR(150),
    profile_path VARCHAR(500),
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);

-- Xóa dữ liệu cũ, nạp lại từ đầu
DELETE FROM movie_casts;

-- ========================================================
-- 1. Avengers: Infinity War (tmdb_id = 299536)
-- ========================================================
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
SELECT id, 'Chadwick Boseman', 'T''Challa / Black Panther', 'https://image.tmdb.org/t/p/w200/yeumEDMcDUBMVKkSMFiVuuJHNVS.jpg' FROM movies WHERE tmdb_id = 299536;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Josh Brolin', 'Thanos', 'https://image.tmdb.org/t/p/w200/sX2etBbIkxRaCsATyw5ZpOVMPTD.jpg' FROM movies WHERE tmdb_id = 299536;

-- ========================================================
-- 2. Avengers: Endgame (tmdb_id = 299534)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robert Downey Jr.', 'Tony Stark / Iron Man', 'https://image.tmdb.org/t/p/w200/im9SAqJPZKEbVZGmjXuLI4O7RvM.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Chris Evans', 'Steve Rogers / Captain America', 'https://image.tmdb.org/t/p/w200/3bOGNsHlrswhyW79uvIHH1V43JI.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Mark Ruffalo', 'Bruce Banner / Hulk', 'https://image.tmdb.org/t/p/w200/5GilhnOwtVj5sZz0r144c8Abehn.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Chris Hemsworth', 'Thor Odinson', 'https://image.tmdb.org/t/p/w200/jpurJ9jAcLCYjgHHfYF32m3zJYm.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Scarlett Johansson', 'Natasha Romanoff / Black Widow', 'https://image.tmdb.org/t/p/w200/6NsMbJXRlDZuDzatJA4ak4ho19f.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Jeremy Renner', 'Clint Barton / Hawkeye', 'https://image.tmdb.org/t/p/w200/yB84D1cNmYATHDRNoFqI9RaHXnR.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Paul Rudd', 'Scott Lang / Ant-Man', 'https://image.tmdb.org/t/p/w200/8eTtJ7XVXY0BnEeUaItewmTo7T.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Brie Larson', 'Carol Danvers / Captain Marvel', 'https://image.tmdb.org/t/p/w200/8cduWpA4z6e50G8A8tZ0eW1h38I.jpg' FROM movies WHERE tmdb_id = 299534;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Josh Brolin', 'Thanos', 'https://image.tmdb.org/t/p/w200/sX2etBbIkxRaCsATyw5ZpOVMPTD.jpg' FROM movies WHERE tmdb_id = 299534;

-- ========================================================
-- 3. Avatar (2009) (tmdb_id = 19995)
-- ========================================================
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
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'CCH Pounder', 'Mo''at', 'https://image.tmdb.org/t/p/w200/9wzTy8aFVJ3jvezPaYhwJHE7EHG.jpg' FROM movies WHERE tmdb_id = 19995;

-- ========================================================
-- 4. Avatar: The Way of Water (2022) (tmdb_id = 76600)
-- ========================================================
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
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Britain Dalton', 'Lo''ak', 'https://image.tmdb.org/t/p/w200/9yiGzjSUYlgfL3kk2wIVGxN4eDv.jpg' FROM movies WHERE tmdb_id = 76600;

-- ========================================================
-- 5. Interstellar (2014) (tmdb_id = 157336)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Matthew McConaughey', 'Joseph Cooper', 'https://image.tmdb.org/t/p/w200/wJiGedOCZhwmx9DizKonJ6Sdw0s.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Anne Hathaway', 'Dr. Amelia Brand', 'https://image.tmdb.org/t/p/w200/tLfl4w1lTnhBt6vB07A9r8q7h9c.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Jessica Chastain', 'Murphy Cooper (Trưởng thành)', 'https://image.tmdb.org/t/p/w200/sEEMnNVxnxDVfJbEMllhvemuvQG.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Michael Caine', 'Giáo sư John Brand', 'https://image.tmdb.org/t/p/w200/7fJT8bqYiwSaJa2vYy4A6W1P8bh.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Matt Damon', 'Dr. Mann', 'https://image.tmdb.org/t/p/w200/2lcIyBKbMBMD3FVhEJuqfXDGMji.jpg' FROM movies WHERE tmdb_id = 157336;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Mackenzie Foy', 'Murphy Cooper (Nhỏ)', 'https://image.tmdb.org/t/p/w200/hAtB1GpV4NHrA9eFkfXWwTfJZJk.jpg' FROM movies WHERE tmdb_id = 157336;

-- ========================================================
-- 6. Fight Club (1999) (tmdb_id = 550)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Edward Norton', 'The Narrator (Người kể chuyện)', 'https://image.tmdb.org/t/p/w200/8nytsqL59SFJTVYMgR5bONRCKqP.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Brad Pitt', 'Tyler Durden', 'https://image.tmdb.org/t/p/w200/cckcYc2v0yh1tc9QjRelptsqoS4.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Helena Bonham Carter', 'Marla Singer', 'https://image.tmdb.org/t/p/w200/DDeITcCEMqlPnqz6h1f7p7m9Nq.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Meat Loaf', 'Robert "Bob" Paulson', 'https://image.tmdb.org/t/p/w200/dXMklMBvVqvjVFRO18NeJqKAMVH.jpg' FROM movies WHERE tmdb_id = 550;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Jared Leto', 'Angel Face', 'https://image.tmdb.org/t/p/w200/sAbTKLlAY64vqoLBpYlBhkp5Mj0.jpg' FROM movies WHERE tmdb_id = 550;

-- ========================================================
-- 7. The Godfather (1972) (tmdb_id = 238)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Marlon Brando', 'Don Vito Corleone', 'https://image.tmdb.org/t/p/w200/fuTEPMsBtV1zE98D65kZ4e8p8r8.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Al Pacino', 'Michael Corleone', 'https://image.tmdb.org/t/p/w200/2eKMH8ILnP1lbqGTT80u0UGtXiJ.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'James Caan', 'Sonny Corleone', 'https://image.tmdb.org/t/p/w200/fMxBOEWMwHZ1f5nqKE2WRNBM0bP.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robert Duvall', 'Tom Hagen', 'https://image.tmdb.org/t/p/w200/18e1mLGc2sEEDpj8JUzWLJDO4nf.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Diane Keaton', 'Kay Adams', 'https://image.tmdb.org/t/p/w200/8OQP4f7OUWiHbk0YpBd3bj3OBiH.jpg' FROM movies WHERE tmdb_id = 238;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Richard Castellano', 'Peter Clemenza', 'https://image.tmdb.org/t/p/w200/jYy1dkuGQZ8XcEAHt3c50vQOPKs.jpg' FROM movies WHERE tmdb_id = 238;

-- ========================================================
-- 8. The Godfather Part II (1974) (tmdb_id = 240)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Al Pacino', 'Michael Corleone', 'https://image.tmdb.org/t/p/w200/2eKMH8ILnP1lbqGTT80u0UGtXiJ.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robert De Niro', 'Vito Corleone (Thời trẻ)', 'https://image.tmdb.org/t/p/w200/cT8htcckAL1YUID9q3xP4qQJ2vH.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robert Duvall', 'Tom Hagen', 'https://image.tmdb.org/t/p/w200/18e1mLGc2sEEDpj8JUzWLJDO4nf.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Diane Keaton', 'Kay Adams Corleone', 'https://image.tmdb.org/t/p/w200/8OQP4f7OUWiHbk0YpBd3bj3OBiH.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Talia Shire', 'Connie Corleone', 'https://image.tmdb.org/t/p/w200/v2XxdFKFSXcDLBOlJAHlFmzJVgx.jpg' FROM movies WHERE tmdb_id = 240;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Lee Strasberg', 'Hyman Roth', 'https://image.tmdb.org/t/p/w200/pIPZYXfE7VRSVyX7wGaXuvxWVr6.jpg' FROM movies WHERE tmdb_id = 240;

-- ========================================================
-- 9. Schindler's List (1993) (tmdb_id = 424)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Liam Neeson', 'Oskar Schindler', 'https://image.tmdb.org/t/p/w200/xqomdocBpvABGxC8MCUZG3a3LKP.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ben Kingsley', 'Itzhak Stern', 'https://image.tmdb.org/t/p/w200/9h4ue8vqAIl09aSN5XsaT2zDGJT.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ralph Fiennes', 'Amon Göth', 'https://image.tmdb.org/t/p/w200/fO3gMMkFHyZa5HMiSQlq0HTAWXF.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Caroline Goodall', 'Emilie Schindler', 'https://image.tmdb.org/t/p/w200/klWvwKpBchWRxRTOkIHv1b88gUJ.jpg' FROM movies WHERE tmdb_id = 424;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Embeth Davidtz', 'Helen Hirsch', 'https://image.tmdb.org/t/p/w200/9hW2ZTHK5qz6mD3sqJrXKpqXnCn.jpg' FROM movies WHERE tmdb_id = 424;

-- ========================================================
-- 10. Your Name / Kimi no Na wa (2016) (tmdb_id = 372058)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ryunosuke Kamiki', 'Taki Tachibana (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/fNDv0yTyXkGaXpXjKfqVMKlJeST.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Mone Kamishiraishi', 'Mitsuha Miyamizu (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/1jXLF4FamKMoWYXHoE1jO5FRhFU.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ryo Narita', 'Katsuhiko Teshigawara (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/9qPJelrx7MNJPJl5qnBVYJlLMVn.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Aoi Yuki', 'Yotsuha Miyamizu (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/zEWJCdRGpFJD7bqUKpn29bKvdpP.jpg' FROM movies WHERE tmdb_id = 372058;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Nobunaga Shimazaki', 'Tsukasa Fujii (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/9N4EbSvwRYtgGBPjYKpxBuB8LB1.jpg' FROM movies WHERE tmdb_id = 372058;

-- ========================================================
-- 11. Spirited Away / Sen to Chihiro (2001) (tmdb_id = 129)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Rumi Hiiragi', 'Chihiro Ogino / Sen (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/wUl5gLJWYUBuHNBZk0jcbMuDMzb.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Miyu Irino', 'Haku (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/9Rr4msDsOxJl5AolmvAmSQ9Fvwv.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Mari Natsuki', 'Yubaba / Zeniba (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/3LReLFDq6OC3ikzFt5fXSHJkdZj.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Takashi Naito', 'Akio Ogino — cha Chihiro (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/h84tPUFqjhLaAFJo9pRa3caqmNH.jpg' FROM movies WHERE tmdb_id = 129;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Yasuko Sawaguchi', 'Yuko Ogino — mẹ Chihiro (Lồng tiếng)', 'https://image.tmdb.org/t/p/w200/qT7DkE9YwBBpSXCG4jQ7Q6E49L2.jpg' FROM movies WHERE tmdb_id = 129;

-- ========================================================
-- 12. Inception (2010) (tmdb_id = 27205)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Leonardo DiCaprio', 'Dom Cobb', 'https://image.tmdb.org/t/p/w200/wo2hJpn04vbtmh0B9utCFdsQhxM.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Joseph Gordon-Levitt', 'Arthur', 'https://image.tmdb.org/t/p/w200/0bFBV9hNFRa47xJ71XqAXNw0bEo.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Elliot Page', 'Ariadne', 'https://image.tmdb.org/t/p/w200/wPmF9MBxFbMFEHapbxrNBaOfCFQ.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tom Hardy', 'Eames', 'https://image.tmdb.org/t/p/w200/d81K0RH8UX7tZj49tZaQhZ9ewH.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Cillian Murphy', 'Robert Fischer', 'https://image.tmdb.org/t/p/w200/wSAWwZ1bI2NZH8kEGV9P7mq9zzp.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ken Watanabe', 'Mr. Saito', 'https://image.tmdb.org/t/p/w200/saqoI9JIijLXBcf8KfULcqTmvZj.jpg' FROM movies WHERE tmdb_id = 27205;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Marion Cotillard', 'Mal Cobb', 'https://image.tmdb.org/t/p/w200/wUZ5HB8OmPbqMnB6CQP5OmIuCJH.jpg' FROM movies WHERE tmdb_id = 27205;

-- ========================================================
-- 13. Pulp Fiction (1994) (tmdb_id = 680)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'John Travolta', 'Vincent Vega', 'https://image.tmdb.org/t/p/w200/wImywHRjZ1DP0EItGm3g5y1LLQD.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Samuel L. Jackson', 'Jules Winnfield', 'https://image.tmdb.org/t/p/w200/nCJJ3NVksHNtuPtFGyB8g1LwO0o.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Uma Thurman', 'Mia Wallace', 'https://image.tmdb.org/t/p/w200/jQ1kHMQJBbLiDX7MivJJzKqOcGV.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Bruce Willis', 'Butch Coolidge', 'https://image.tmdb.org/t/p/w200/kU9Elqx3NMFt0eDVgBZwqF2ODSZ.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Harvey Keitel', 'Winston "The Wolf" Wolfe', 'https://image.tmdb.org/t/p/w200/mwRse13PGKFlJGBaio3l5OL70XL.jpg' FROM movies WHERE tmdb_id = 680;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tim Roth', 'Pumpkin / Ringo', 'https://image.tmdb.org/t/p/w200/4G2EQEJLMKB9p8HgTKoA5MJBBaL.jpg' FROM movies WHERE tmdb_id = 680;

-- ========================================================
-- 14. Forrest Gump (1994) (tmdb_id = 13)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tom Hanks', 'Forrest Gump', 'https://image.tmdb.org/t/p/w200/xndFsrA7L3h8j6m3k2v5b8w0z9q.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robin Wright', 'Jenny Curran', 'https://image.tmdb.org/t/p/w200/wNmYUDRhDYJPpPNxCZUJxLi1UJo.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Gary Sinise', 'Trung úy Dan Taylor', 'https://image.tmdb.org/t/p/w200/9SZvHaFJKQGSVZZ0oBkNmFEXRGl.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Sally Field', 'Bà Gump (Mẹ Forrest)', 'https://image.tmdb.org/t/p/w200/gy61JZKVcD3JfRBWEqhqhvNAbvr.jpg' FROM movies WHERE tmdb_id = 13;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Mykelti Williamson', 'Benjamin Buford "Bubba" Blue', 'https://image.tmdb.org/t/p/w200/vSfPKHKDm6R4FjO4OxNiMzVTuXI.jpg' FROM movies WHERE tmdb_id = 13;

-- ========================================================
-- 15. The Shawshank Redemption (1994) (tmdb_id = 278)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tim Robbins', 'Andy Dufresne', 'https://image.tmdb.org/t/p/w200/wBZCRnUe2t3JxJLmHDPdVLHbGZH.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Morgan Freeman', 'Ellis Boyd "Red" Redding', 'https://image.tmdb.org/t/p/w200/oGJQhOpT8S1M58Apz6LulUqIQfN.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Bob Gunton', 'Cai ngục Samuel Norton', 'https://image.tmdb.org/t/p/w200/9j7MUBdFJlqXU7HBOFbxZDi5X7a.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Clancy Brown', 'Đội trưởng Byron Hadley', 'https://image.tmdb.org/t/p/w200/1MEEHrpBd1VTqMcwzJjVd1mIKcm.jpg' FROM movies WHERE tmdb_id = 278;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'William Sadler', 'Heywood', 'https://image.tmdb.org/t/p/w200/3zCE5fjLcRYVNbBaXK7FoHbC0Yl.jpg' FROM movies WHERE tmdb_id = 278;

-- ========================================================
-- 16. The Lord of the Rings: The Return of the King (2003) (tmdb_id = 122)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Elijah Wood', 'Frodo Baggins', 'https://image.tmdb.org/t/p/w200/pR1LcIaIuJBYsEpUfSdxFiARCfh.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ian McKellen', 'Gandalf Áo Trắng', 'https://image.tmdb.org/t/p/w200/kR7aDzBLVl9hNTXWfbnFcbxs1R4.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Viggo Mortensen', 'Aragorn / Elessar', 'https://image.tmdb.org/t/p/w200/vH5gVSpHAMhDaFWfh0Q7BG61O1y.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Orlando Bloom', 'Legolas', 'https://image.tmdb.org/t/p/w200/u3sDHKJW62HEbFEFGSLfCY7NjKT.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Andy Serkis', 'Gollum / Sméagol', 'https://image.tmdb.org/t/p/w200/ldEVNGEQ5aZOZ7VLRMjXaFWREhP.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Sean Astin', 'Samwise "Sam" Gamgee', 'https://image.tmdb.org/t/p/w200/mEfETVFqC2Mwf0PoS5YWMCdNfpV.jpg' FROM movies WHERE tmdb_id = 122;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Cate Blanchett', 'Galadriel', 'https://image.tmdb.org/t/p/w200/mT9GfL4bQXpCxgW49pq4u6rHiRj.jpg' FROM movies WHERE tmdb_id = 122;

-- ========================================================
-- 17. GoodFellas (1990) (tmdb_id = 769)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Robert De Niro', 'James Conway', 'https://image.tmdb.org/t/p/w200/cT8htcckAL1YUID9q3xP4qQJ2vH.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Ray Liotta', 'Henry Hill', 'https://image.tmdb.org/t/p/w200/pTCznXp0LxTBKWB0m3kiG4MBzBE.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Joe Pesci', 'Tommy DeVito', 'https://image.tmdb.org/t/p/w200/yEqGQ5CPKE7W8BaEMjN7LOp7BHC.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Lorraine Bracco', 'Karen Hill', 'https://image.tmdb.org/t/p/w200/mH3vqWMMEKtMEo0QzNwRrqN2OeA.jpg' FROM movies WHERE tmdb_id = 769;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Paul Sorvino', 'Paul Cicero', 'https://image.tmdb.org/t/p/w200/dqSR8YrXOTxQGzpvG3uuoqx7rCn.jpg' FROM movies WHERE tmdb_id = 769;

-- ========================================================
-- 18. The Green Mile (1999) (tmdb_id = 497)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tom Hanks', 'Paul Edgecomb', 'https://image.tmdb.org/t/p/w200/xndFsrA7L3h8j6m3k2v5b8w0z9q.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Michael Clarke Duncan', 'John Coffey', 'https://image.tmdb.org/t/p/w200/8c8hiUMkJNWxV3QRe7bXLnuPFkY.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'David Morse', 'Brutus "Brutal" Howell', 'https://image.tmdb.org/t/p/w200/pX8OqnN3HGTYi0C1qW5q0P9AeUF.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Bonnie Hunt', 'Jan Edgecomb', 'https://image.tmdb.org/t/p/w200/pJNwdM73FQVdpHKonHkWJE6EvGS.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'James Cromwell', 'Warden Hal Moores', 'https://image.tmdb.org/t/p/w200/h5m7lBXIwcqkB3XWXr8sDMUPXmW.jpg' FROM movies WHERE tmdb_id = 497;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Doug Hutchison', 'Percy Wetmore', 'https://image.tmdb.org/t/p/w200/fFCbJKA0Y3GJ2rSYTpWQCaCRHHe.jpg' FROM movies WHERE tmdb_id = 497;

-- ========================================================
-- 19. City of God / Cidade de Deus (2002) (tmdb_id = 598)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Alexandre Rodrigues', 'Buscapé / Rocket', 'https://image.tmdb.org/t/p/w200/pSHdGnwNl4vhT2kSmYlJbG1FXJI.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Leandro Firmino', 'Zé Pequeno / Li''l Zé', 'https://image.tmdb.org/t/p/w200/mfQfGrfNJ9SgbWFNV5EiKqtVvHt.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Phellipe Haagensen', 'Bené', 'https://image.tmdb.org/t/p/w200/o5a5bRf7EHHyFQQhBQrFm7F2Mgs.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Douglas Silva', 'Dadinho / Li''l Dice (Thời nhỏ)', 'https://image.tmdb.org/t/p/w200/pLfMG4mNxFUw8mFsGCe3pVOIVfT.jpg' FROM movies WHERE tmdb_id = 598;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Alice Braga', 'Angélica', 'https://image.tmdb.org/t/p/w200/eHSP0CKBW4P4F0BnYtlqhGUJm1Q.jpg' FROM movies WHERE tmdb_id = 598;

-- ========================================================
-- 20. Top Gun: Maverick (2022) (tmdb_id = 361743)
-- ========================================================
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Tom Cruise', 'Pete "Maverick" Mitchell', 'https://image.tmdb.org/t/p/w200/8qBylBsQf4llkGrWR3qAsOtOU8O.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Miles Teller', 'Bradley "Rooster" Bradshaw', 'https://image.tmdb.org/t/p/w200/mEDhNkYMNHAlxkzrk01ETq8UD7a.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Jennifer Connelly', 'Penny Benjamin', 'https://image.tmdb.org/t/p/w200/sD6RNfzHxDsB4Fph8xJHB3DIj8P.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Jon Hamm', 'Beau "Cyclone" Simpson', 'https://image.tmdb.org/t/p/w200/1GfVXsqv7i22kGrHMDYH5LKsZSL.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Glen Powell', 'Jake "Hangman" Seresin', 'https://image.tmdb.org/t/p/w200/vfQuMXFuECIjYNJXoG8sVqGCYLz.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Val Kilmer', 'Tom "Iceman" Kazansky', 'https://image.tmdb.org/t/p/w200/wkqKvmiFy3VPt3cQiP2bFb0VEqC.jpg' FROM movies WHERE tmdb_id = 361743;
INSERT INTO movie_casts (movie_id, actor_name, character_name, profile_path)
SELECT id, 'Lewis Pullman', 'Robert "Bob" Floyd', 'https://image.tmdb.org/t/p/w200/mKDzMHO50MNP4qHGXfnuAeKLo3B.jpg' FROM movies WHERE tmdb_id = 361743;

-- ========================================================
-- Kiểm tra kết quả: đếm số diễn viên mỗi phim
-- ========================================================
SELECT 
    m.id,
    m.title,
    m.tmdb_id,
    COUNT(c.id) AS so_dien_vien
FROM movies m
LEFT JOIN movie_casts c ON m.id = c.movie_id
GROUP BY m.id, m.title, m.tmdb_id
ORDER BY m.id;
