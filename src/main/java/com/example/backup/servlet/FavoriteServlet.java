package com.example.backup.servlet;

import com.example.backup.service.*;
import com.example.backup.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.List;

@WebServlet("/favorites")
public class FavoriteServlet extends HttpServlet {

    private final FavoriteService favoriteService = new FavoriteService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String userId = req.getParameter("user_id");
            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Missing user_id parameter\"}");
                return;
            }
            List<Movie> favorites = favoriteService.getFavoritesByUser(Integer.parseInt(userId));
            out.print(gson.toJson(favorites));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();
            int userId  = body.get("user_id").getAsInt();
            int movieId = body.get("movie_id").getAsInt();

            boolean exists = favoriteService.isFavorite(userId, movieId);
            if (exists) {
                favoriteService.removeFavorite(userId, movieId);
                out.print("{\"status\":\"removed\"}");
            } else {
                favoriteService.addFavorite(userId, movieId);
                out.print("{\"status\":\"added\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String userId  = req.getParameter("user_id");
            String movieId = req.getParameter("movie_id");

            if (userId == null || movieId == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Missing user_id or movie_id parameter\"}");
                return;
            }

            favoriteService.removeFavorite(Integer.parseInt(userId), Integer.parseInt(movieId));
            out.print("{\"status\":\"removed\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
