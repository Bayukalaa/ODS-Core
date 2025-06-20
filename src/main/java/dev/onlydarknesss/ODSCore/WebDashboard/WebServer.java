package dev.onlydarknesss.ODSCore.WebDashboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import dev.onlydarknesss.ODSCore.Main;
import dev.onlydarknesss.ODSCore.Utils.ChatApiHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class WebServer {
    private final Main plugin;

    public WebServer(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/info", new InfoApiHandler(plugin));
            server.createContext("/api/chat", new ChatApiHandler());

            server.setExecutor(null);
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
        private final Gson gson = new Gson();

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

            double[] tpsArr = plugin.getServer().getTPS();
            double tps = tpsArr.length > 0 ? tpsArr[0] : -1;


            int pingSum = 0;
            int count = 0;
            for (var player : plugin.getServer().getOnlinePlayers()) {
                pingSum += player.getPing();
                count++;
            }
            int avgPing = count > 0 ? pingSum / count : -1;

            double cpuUsage = getCpuUsagePercent();
            long memoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

            JsonArray playerArray = new JsonArray();
            for (String p : plugin.getServer().getOnlinePlayers().stream().map(player -> player.getName()).toList()) {
                playerArray.add(p);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("players", players);
            jsonObject.addProperty("version", version);
            jsonObject.addProperty("motd", motd);
            jsonObject.addProperty("devmode", devmode);
            jsonObject.add("playerNames", playerArray);
            jsonObject.addProperty("apiVersion", apiV);
            jsonObject.addProperty("ping", avgPing);
            jsonObject.addProperty("tps", tps);
            jsonObject.addProperty("cpuUsage", cpuUsage);
            jsonObject.addProperty("memoryUsage", memoryUsage);

            String json = gson.toJson(jsonObject);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private double getCpuUsagePercent() {
            try {
                com.sun.management.OperatingSystemMXBean osBean =
                        (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
                return osBean.getProcessCpuLoad() * 100;
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
