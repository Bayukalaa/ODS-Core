package dev.onlydarknesss.ODSCore.WebDashboard;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dev.onlydarknesss.ODSCore.Main;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WebServer {
    public static void start(Main plugin) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/info", new InfoApiHandler(plugin));
            server.setExecutor(null); // default executor
            server.start();
            plugin.getLogger().info("Web dashboard started on http://localhost:8080");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            InputStream in = WebServer.class.getResourceAsStream("/web" + path);
            if (in == null) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            byte[] data = in.readAllBytes();
            String contentType;
            if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            } else if (path.endsWith(".html")) {
                contentType = "text/html";
            } else if (path.endsWith(".png")) {
                contentType = "image/png";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (path.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else {
                contentType = "application/octet-stream";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }

    static class InfoApiHandler implements HttpHandler {
        private final Main plugin;

        public InfoApiHandler(Main plugin) {
            this.plugin = plugin;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int players = plugin.getServer().getOnlinePlayers().size();
            String version = plugin.getServer().getVersion();
            String motd = plugin.getServer().getMotd();
            String apiV = Main.getVERSION();
            boolean devmode = plugin.getConfig().getBoolean("dev-mode");

            List<String> playerNames = plugin.getServer().getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .toList();

            String json = String.format(
                    "{\"players\":%d,\"version\":\"%s\",\"motd\":\"%s\",\"devmode\":%b,\"playerNames\":%s,\"apiVersion\":\"%s\"}",
                    players, escapeJson(version), escapeJson(motd), devmode, toJsonArray(playerNames), escapeJson(apiV)
            );


            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }


        private String toJsonArray(List<String> list) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append("\"").append(escapeJson(list.get(i))).append("\"");
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }

        private String escapeJson(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}
