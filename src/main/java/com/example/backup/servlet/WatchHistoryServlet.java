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

@WebServlet("/history")
public class WatchHistoryServlet extends HttpServlet {

    private final WatchHistoryService watchHistoryService = new WatchHistoryService();
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
            List<WatchHistory> history = watchHistoryService.getHistoryByUser(Integer.parseInt(userId));
            out.print(gson.toJson(history));
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
            int userId   = body.get("user_id").getAsInt();
            int movieId  = body.get("movie_id").getAsInt();
            int progress = body.get("progress").getAsInt();

            watchHistoryService.saveProgress(userId, movieId, progress);
            out.print("{\"status\":\"success\",\"message\":\"Progress saved\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
