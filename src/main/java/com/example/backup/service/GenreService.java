package com.example.backup.service;

import com.example.backup.model.Genre;
import com.example.backup.repository.GenreRepository;
import java.sql.SQLException;
import java.util.List;

public class GenreService {
    private GenreRepository repo = new GenreRepository();

    public List<Genre> getAllGenres() throws SQLException {
        return repo.findAll();
    }

    public Genre getById(int id) throws SQLException {
        return repo.findById(id);
    }
}
