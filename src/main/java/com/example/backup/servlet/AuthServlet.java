package com.example.backup.servlet;

import com.example.backup.service.*;
import com.example.backup.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try (PrintWriter out = resp.getWriter()) {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String action = body.get("action").getAsString();

            if ("login".equals(action)) {
                String username = body.get("username").getAsString();
                String password = body.get("password").getAsString();

                User user = userService.login(username, password);
                if (user != null) {
                    JsonObject result = new JsonObject();
                    result.addProperty("status", "success");
                    result.addProperty("user_id", user.getUserId());
                    result.addProperty("username", user.getUsername());
                    out.print(gson.toJson(result));
                } else {
                    out.print("{\"status\":\"error\",\"message\":\"Invalid username or password\"}");
                }

            } else if ("register".equals(action)) {
                String username = body.get("username").getAsString();
                String password = body.get("password").getAsString();
                String email    = body.get("email").getAsString();

                boolean success = userService.register(username, password, email);
                if (success) {
                    out.print("{\"status\":\"success\",\"message\":\"Registration successful\"}");
                } else {
                    out.print("{\"status\":\"error\",\"message\":\"Username or email already exists\"}");
                }

            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Unknown action\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
