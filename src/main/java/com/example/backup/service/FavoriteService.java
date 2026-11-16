package com.example.backup.service;

import com.example.backup.model.Movie;
import com.example.backup.repository.FavoriteRepository;

import java.util.List;

public class FavoriteService {

    private final FavoriteRepository favoriteRepo = new FavoriteRepository();

    public List<Movie> getFavorites(int userId) {
        return favoriteRepo.findByUserId(userId);
    }

    public boolean toggleFavorite(int userId, int movieId) {
        if (favoriteRepo.isFavorite(userId, movieId)) {
            favoriteRepo.remove(userId, movieId);
            return false;
        } else {
            favoriteRepo.add(userId, movieId);
            return true;
        }
    }

    public boolean isFavorite(int userId, int movieId) {
        return favoriteRepo.isFavorite(userId, movieId);
    }
}
