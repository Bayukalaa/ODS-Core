package dev.onlydarknesss.ODSCore.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenStorage {
    private static final Map<String, String> tokenToUser = new ConcurrentHashMap<>();

    public static void storeToken(String token, String username) {
        tokenToUser.put(token, username);
    }

    public static String getUsername(String token) {
        return tokenToUser.get(token);
    }

    public static void invalidateToken(String token) {
        tokenToUser.remove(token);
    }

    public static boolean isValid(String token) {
        return tokenToUser.containsKey(token);
    }
}