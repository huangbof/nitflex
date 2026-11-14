package com.example.backup.service;

import com.example.backup.model.Movie;
import com.example.backup.repository.FavoriteRepository;

import java.util.List;

public class FavoriteService {

    private final FavoriteRepository favoriteRepo = new FavoriteRepository();

    // -----------------------------------------------------------------------
    // getFavorites
    // -----------------------------------------------------------------------
    public List<Movie> getFavorites(int userId) {
        return favoriteRepo.findByUserId(userId);
    }

    // -----------------------------------------------------------------------
    // toggleFavorite — removes if already present, adds if not
    // Returns true if the movie is now a favourite (was added),
    //         false if it was removed.
    // -----------------------------------------------------------------------
    public boolean toggleFavorite(int userId, int movieId) {
        if (favoriteRepo.isFavorite(userId, movieId)) {
            favoriteRepo.remove(userId, movieId);
            return false;
        } else {
            favoriteRepo.add(userId, movieId);
            return true;
        }
    }

    // -----------------------------------------------------------------------
    // isFavorite
    // -----------------------------------------------------------------------
    public boolean isFavorite(int userId, int movieId) {
        return favoriteRepo.isFavorite(userId, movieId);
    }
}
