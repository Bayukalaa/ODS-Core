package dev.onlydarknesss.ODSCore.Utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatListener implements Listener {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String time = timeFormat.format(new Date());
        String message = String.format("[%s] %s: %s", time, event.getPlayer().getName(), event.getMessage());
        ChatLogger.addMessage(message);
    }
}
