package dev.onlydarknesss.ODSCore.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatLogger {
    private static final CopyOnWriteArrayList<String> chatMessages = new CopyOnWriteArrayList<>();
    private static final int MAX_MESSAGES = 500;

    public static void addMessage(String message) {
        chatMessages.add(message);
        if (chatMessages.size() > MAX_MESSAGES) {
            chatMessages.remove(0);
        }
    }

    public static List<String> getMessages() {
        return List.copyOf(chatMessages);
    }
}