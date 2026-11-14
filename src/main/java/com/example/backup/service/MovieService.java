package com.example.backup.service;

import com.example.backup.model.Movie;
import com.example.backup.repository.GenreRepository;
import com.example.backup.repository.MovieRepository;

import java.util.List;

public class MovieService {

    private final MovieRepository movieRepo = new MovieRepository();
    private final GenreRepository genreRepo = new GenreRepository();

    // -----------------------------------------------------------------------
    // getAllMovies
    // -----------------------------------------------------------------------
    public List<Movie> getAllMovies() {
        return movieRepo.findAll();
    }

    // -----------------------------------------------------------------------
    // getByGenre
    // -----------------------------------------------------------------------
    public List<Movie> getByGenre(int genreId) {
        return movieRepo.findByGenre(genreId);
    }

    // -----------------------------------------------------------------------
    // getDetail — also increments the view counter
    // -----------------------------------------------------------------------
    public Movie getDetail(int id) {
        Movie movie = movieRepo.findById(id);
        if (movie != null) {
            movieRepo.incrementViewCount(id);
        }
        return movie;
    }

    // -----------------------------------------------------------------------
    // search
    // -----------------------------------------------------------------------
    public List<Movie> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return movieRepo.findAll();
        }
        return movieRepo.search(keyword.trim());
    }

    // -----------------------------------------------------------------------
    // getTopRated
    // -----------------------------------------------------------------------
    public List<Movie> getTopRated() {
        return movieRepo.findTopRated();
    }

    // -----------------------------------------------------------------------
    // getPopular
    // -----------------------------------------------------------------------
    public List<Movie> getPopular() {
        return movieRepo.findPopular();
    }

    // -----------------------------------------------------------------------
    // getLatest
    // -----------------------------------------------------------------------
    public List<Movie> getLatest() {
        return movieRepo.findLatest();
    }
}
