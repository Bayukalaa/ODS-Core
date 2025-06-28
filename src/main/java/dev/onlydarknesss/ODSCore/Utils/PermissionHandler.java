package dev.onlydarknesss.ODSCore.Utils;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.onlydarknesss.ODSCore.Database.Database;
import dev.onlydarknesss.ODSCore.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PermissionHandler implements HttpHandler {
    private final Main plugin;
    private final Database database;

    public PermissionHandler(Main plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("POST".equalsIgnoreCase(method)) {
            handleAdd(exchange);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            handleRemove(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private void handleAdd(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String uuidStr = getQueryParam(query, "uuid");
        String perm = getQueryParam(query, "perm");

        if (uuidStr == null || perm == null) {
            sendJson(exchange, 400, "Missing uuid or perm");
            return;
        }

        try {
            if (!database.isConnected()) {
                database.connect();
            }
        } catch (Exception e) {
            sendJson(exchange, 500, "Database connection failed");
            return;
        }

        try (Connection con = database.getCon()) {
            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO player_permissions (player_uuid, permission) VALUES (?, ?)")) {
                stmt.setString(1, uuidStr);
                stmt.setString(2, perm);
                stmt.executeUpdate();
            }

            // Oyuncu çevrimiçiyse perm'i anlık ver
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    PermissionManager.addPermission(player, perm);
                    plugin.getLogger().info("[PermissionHandler] Added permission '" + perm + "' to player: " + player.getName());
                }
            } catch (IllegalArgumentException ignored) {}

            sendJson(exchange, 200, "Permission added");
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "Failed to add permission");
        }
    }

    private void handleRemove(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String uuidStr = getQueryParam(query, "uuid");
        String perm = getQueryParam(query, "perm");

        if (uuidStr == null || perm == null) {
            sendJson(exchange, 400, "Missing uuid or perm");
            return;
        }

        try {
            if (!database.isConnected()) {
                database.connect();
            }
        } catch (SQLException e) {
            sendJson(exchange, 500, "Database connection failed");
            return;
        }

        try (Connection con = database.getCon()) {
            if (con == null) {
                sendJson(exchange, 500, "Internal server error");
                return;
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "DELETE FROM player_permissions WHERE player_uuid = ? AND permission = ?")) {
                stmt.setString(1, uuidStr);
                stmt.setString(2, perm);

                int affected = stmt.executeUpdate();

                if (affected > 0) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            PermissionManager.removePermission(player, perm);
                            plugin.getLogger().info("[PermissionHandler] Removed permission '" + perm + "' from player: " + player.getName());
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("[PermissionHandler] Invalid UUID format: " + uuidStr);
                    }

                    sendJson(exchange, 200, "Permission removed");
                } else {
                    sendJson(exchange, 404, "Permission not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, "Internal server error");
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("message", message);
        byte[] data = response.toString().getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private String getQueryParam(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(key)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
