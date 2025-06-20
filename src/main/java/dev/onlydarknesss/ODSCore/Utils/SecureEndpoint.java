package dev.onlydarknesss.ODSCore.Utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class SecureEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        if (!TokenStorage.isValid(token)) {
            exchange.sendResponseHeaders(403, -1);
            return;
        }

        String username = TokenStorage.getUsername(token);
        String response = "Welcome, " + username;
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
