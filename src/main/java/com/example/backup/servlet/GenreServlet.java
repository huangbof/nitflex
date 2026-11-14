package com.example.backup.servlet;

import com.example.backup.service.GenreService;
import com.example.backup.model.Genre;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/genres")
public class GenreServlet extends HttpServlet {

    private final GenreService genreService = new GenreService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            List<Genre> genres = genreService.getAllGenres();
            out.print(gson.toJson(genres));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
