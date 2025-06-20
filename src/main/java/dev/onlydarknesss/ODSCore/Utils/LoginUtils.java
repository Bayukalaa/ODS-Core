package dev.onlydarknesss.ODSCore.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.onlydarknesss.ODSCore.Database.Database;
import dev.onlydarknesss.ODSCore.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;

public class LoginUtils implements HttpHandler {
    private final Main pl;
    private final Gson gson = new Gson();
    private final Database database;

    public LoginUtils(Main pl) {
        this.pl = pl;
        this.database = new Database(pl);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        JsonObject responseJson = new JsonObject();

        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            JsonObject requestBody = gson.fromJson(isr, JsonObject.class);
            String username = requestBody.get("username").getAsString().trim();
            String password = requestBody.get("password").getAsString().trim();

            pl.getLogger().info("[LoginUtils] Login attempt for username: '" + username + "'");

            database.connect();
            Connection con = database.getCon();

            try (PreparedStatement stmt = con.prepareStatement("SELECT password FROM users WHERE username = ?")) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        if (storedHash == null || storedHash.isEmpty()) {
                            pl.getLogger().warning("[LoginUtils] Stored hash is null or empty for user: " + username);
                            responseJson.addProperty("status", "fail");
                            responseJson.addProperty("message", "User password invalid");
                            sendResponse(exchange, 401, responseJson);
                            return;
                        }
                        storedHash = storedHash.trim();

                      //  pl.getLogger().info("[LoginUtils] Stored hash from DB: '" + storedHash + "'");
                      //  pl.getLogger().info("[LoginUtils] Password entered: '" + password + "'");

                        if (PasswordUtils.checkPass(password, storedHash)) {
                            String token = UUID.randomUUID().toString();
                            TokenStorage.storeToken(token, username);

                            responseJson.addProperty("status", "success");
                            responseJson.addProperty("token", token);
                            sendResponse(exchange, 200, responseJson);
                        } else {
                            pl.getLogger().info("[LoginUtils] Incorrect password for user: " + username);
                            responseJson.addProperty("status", "fail");
                            responseJson.addProperty("message", "Incorrect password");
                            sendResponse(exchange, 401, responseJson);
                        }
                    } else {
                        pl.getLogger().info("[LoginUtils] User not found: " + username);
                        responseJson.addProperty("status", "fail");
                        responseJson.addProperty("message", "User not found");
                        sendResponse(exchange, 404, responseJson);
                    }
                }
            } finally {
                database.disconnect();
            }

        } catch (Exception e) {
            pl.getLogger().severe("[LoginUtils] Exception: " + e.getMessage());
            e.printStackTrace();
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Server error");
            sendResponse(exchange, 500, responseJson);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, JsonObject responseJson) throws IOException {
        String response = responseJson.toString();
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
