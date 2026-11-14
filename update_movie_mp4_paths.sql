-- ================================================================
-- CẬP NHẬT ĐƯỜNG DẪN MP4 NỘI BỘ (res/raw/) CHO TOÀN BỘ 20 BỘ PHIM
-- Chạy script này trong phpMyAdmin (CSDL: movie_db)
-- ================================================================

-- Avengers: Cuộc Chiến Vô Cực (Avengers: Infinity War) -> avengers_infinity_war.mp4
UPDATE movies SET video_url = 'raw/avengers_infinity_war.mp4' WHERE tmdb_id = 299536;

-- Avengers: Hồi Kết (Avengers: Endgame) -> endgame.mp4 (ĐÃ CÓ SẴN)
UPDATE movies SET video_url = 'raw/endgame.mp4' WHERE tmdb_id = 299534;

-- Avatar (Avatar) -> avatar.mp4
UPDATE movies SET video_url = 'raw/avatar.mp4' WHERE tmdb_id = 19995;

-- Avatar: Dòng Chảy Của Nước (Avatar: The Way of Water) -> avatar_the_way_of_water.mp4
UPDATE movies SET video_url = 'raw/avatar_the_way_of_water.mp4' WHERE tmdb_id = 76600;

-- Hố Đen Tử Thần (Interstellar) -> interstellar.mp4
UPDATE movies SET video_url = 'raw/interstellar.mp4' WHERE tmdb_id = 157336;

-- Sàn Đấu Sinh Tử (Fight Club) -> fight_club.mp4
UPDATE movies SET video_url = 'raw/fight_club.mp4' WHERE tmdb_id = 550;

-- Bố Già (The Godfather) -> the_godfather.mp4
UPDATE movies SET video_url = 'raw/the_godfather.mp4' WHERE tmdb_id = 238;

-- Bố Già Phần II (The Godfather Part II) -> the_godfather_part_ii.mp4
UPDATE movies SET video_url = 'raw/the_godfather_part_ii.mp4' WHERE tmdb_id = 240;

-- Bản Danh Sách Của Schindler (Schindler's List) -> schindlers_list.mp4
UPDATE movies SET video_url = 'raw/schindlers_list.mp4' WHERE tmdb_id = 424;

-- Tên Cậu Là Gì? (Your Name) -> your_name.mp4
UPDATE movies SET video_url = 'raw/your_name.mp4' WHERE tmdb_id = 372058;

-- Vùng Đất Linh Hồn (Spirited Away) -> spirited_away.mp4
UPDATE movies SET video_url = 'raw/spirited_away.mp4' WHERE tmdb_id = 129;

-- Kẻ Đánh Cắp Giấc Mơ (Inception) -> inception.mp4
UPDATE movies SET video_url = 'raw/inception.mp4' WHERE tmdb_id = 27205;

-- Chuyện Tào Lao (Pulp Fiction) -> pulp_fiction.mp4
UPDATE movies SET video_url = 'raw/pulp_fiction.mp4' WHERE tmdb_id = 680;

-- Cuộc Đời Forrest Gump (Forrest Gump) -> forrest_gump.mp4
UPDATE movies SET video_url = 'raw/forrest_gump.mp4' WHERE tmdb_id = 13;

-- Nhà Tù Shawshank (The Shawshank Redemption) -> the_shawshank_redemption.mp4
UPDATE movies SET video_url = 'raw/the_shawshank_redemption.mp4' WHERE tmdb_id = 278;

-- Chúa Tể Những Chiếc Nhẫn: Sự Trở Lại Của Nhà Vua (The Lord of the Rings: The Return of the King) -> return_of_the_king.mp4
UPDATE movies SET video_url = 'raw/return_of_the_king.mp4' WHERE tmdb_id = 122;

-- Những Chiến Hữu (GoodFellas) -> goodfellas.mp4
UPDATE movies SET video_url = 'raw/goodfellas.mp4' WHERE tmdb_id = 769;

-- Dặm Xanh (The Green Mile) -> the_green_mile.mp4
UPDATE movies SET video_url = 'raw/the_green_mile.mp4' WHERE tmdb_id = 497;

-- Thành Phố Của Chúa (City of God) -> city_of_god.mp4
UPDATE movies SET video_url = 'raw/city_of_god.mp4' WHERE tmdb_id = 598;

-- Phi Công Siêu Đẳng: Maverick (Top Gun: Maverick) -> top_gun_maverick.mp4
UPDATE movies SET video_url = 'raw/top_gun_maverick.mp4' WHERE tmdb_id = 361743;
