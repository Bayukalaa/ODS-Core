package dev.onlydarknesss.ODSCore.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.onlydarknesss.ODSCore.Database.Database;
import dev.onlydarknesss.ODSCore.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDetailsHandler implements HttpHandler {
    private final Main plugin;
    private final Gson gson = new Gson();
    private final Database database;

    public PlayerDetailsHandler(Main plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String username = getQueryParam(query, "username");

        plugin.getLogger().info("[PlayerDetailsHandler] Incoming request for username: " + username);

        if (username == null || username.isBlank()) {
            plugin.getLogger().warning("[PlayerDetailsHandler] Missing username parameter.");
            sendJsonResponse(exchange, 400, "Missing username parameter");
            return;
        }

        try {
            if (!database.isConnected()) {
                database.connect();
                plugin.getLogger().info("[PlayerDetailsHandler] Database connected.");
            }

            try (Connection con = database.getCon()) {
                plugin.getLogger().info("[PlayerDetailsHandler] Using database connection.");

                // players tablosundan kullanıcı bilgisi
                try (PreparedStatement ps = con.prepareStatement("SELECT * FROM players WHERE username = ?")) {
                    ps.setString(1, username);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            plugin.getLogger().info("[PlayerDetailsHandler] Player not found in 'players' table.");
                            sendJsonResponse(exchange, 404, "Player not found");
                            return;
                        }

                        String uuid = rs.getString("uuid");
                        String perm = rs.getString("perm");

                        plugin.getLogger().info("[PlayerDetailsHandler] Found player UUID: " + uuid);

                        // users tablosundan email ve permlevel bilgisi
                        String email = "";
                        int permLevel = -1;
                        try (PreparedStatement userInfo = con.prepareStatement("SELECT email, permlevel FROM users WHERE username = ?")) {
                            userInfo.setString(1, username);

                            try (ResultSet userRS = userInfo.executeQuery()) {
                                if (userRS.next()) {
                                    email = userRS.getString("email");
                                    permLevel = userRS.getInt("permlevel");
                                    plugin.getLogger().info("[PlayerDetailsHandler] User email: " + email + ", PermLevel: " + permLevel);
                                } else {
                                    plugin.getLogger().info("[PlayerDetailsHandler] No matching user found in 'users' table.");
                                }
                            }
                        }

                        // player_permissions tablosundan izinler
                        JsonArray permissions = new JsonArray();
                        try (PreparedStatement permStmt = con.prepareStatement("SELECT permission FROM player_permissions WHERE player_uuid = ?")) {
                            permStmt.setString(1, uuid);

                            try (ResultSet permRS = permStmt.executeQuery()) {
                                while (permRS.next()) {
                                    String permission = permRS.getString("permission");
                                    permissions.add(permission);
                                    plugin.getLogger().info("[PlayerDetailsHandler] Found permission: " + permission);
                                }
                            }
                        }

                        // JSON nesnesi oluşturma
                        JsonObject obj = new JsonObject();
                        obj.addProperty("uuid", uuid);
                        obj.addProperty("username", username);
                        obj.addProperty("email", email);
                        obj.addProperty("perm", perm);
                        obj.addProperty("permlevel", permLevel);
                        obj.addProperty("balance", 0); // balance veri tabanında yoksa default 0
                        obj.add("permissions", permissions);

                        // JSON yanıt gönder
                        sendJson(exchange, obj);
                    }
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("[PlayerDetailsHandler] DB error: " + e.getMessage());
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "Database error");
        }
    }

    private void sendJson(HttpExchange exchange, JsonObject obj) throws IOException {
        String json = gson.toJson(obj);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("error", message);
        byte[] data = gson.toJson(obj).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase(key)) {
                try {
                    return URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    plugin.getLogger().warning("[PlayerDetailsHandler] URL decode error: " + e.getMessage());
                    return parts[1];
                }
            }
        }
        return null;
    }
}
