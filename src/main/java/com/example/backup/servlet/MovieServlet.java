package com.example.backup.servlet;

import com.example.backup.service.*;
import com.example.backup.model.*;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/movies")
public class MovieServlet extends HttpServlet {

    private final MovieService movieService = new MovieService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String id      = req.getParameter("id");
            String genreId = req.getParameter("genre_id");
            String keyword = req.getParameter("keyword");
            String type    = req.getParameter("type");

            if (id != null) {
                Movie movie = movieService.getMovieById(Integer.parseInt(id));
                out.print(gson.toJson(movie));

            } else if (genreId != null) {
                List<Movie> movies = movieService.getMoviesByGenre(Integer.parseInt(genreId));
                out.print(gson.toJson(movies));

            } else if (keyword != null) {
                List<Movie> movies = movieService.searchMovies(keyword);
                out.print(gson.toJson(movies));

            } else if ("top_rated".equals(type)) {
                List<Movie> movies = movieService.getTopRatedMovies();
                out.print(gson.toJson(movies));

            } else if ("popular".equals(type)) {
                List<Movie> movies = movieService.getPopularMovies();
                out.print(gson.toJson(movies));

            } else if ("latest".equals(type)) {
                List<Movie> movies = movieService.getLatestMovies();
                out.print(gson.toJson(movies));

            } else {
                List<Movie> movies = movieService.getAllMovies();
                out.print(gson.toJson(movies));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
