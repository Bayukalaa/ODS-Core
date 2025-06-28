package dev.onlydarknesss.ODSCore.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    private static JavaPlugin plugin;

    private static final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void attachPlayer(Player player) {
        if (plugin == null) {
            throw new IllegalStateException("PermissionManager not initialized with plugin!");
        }
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachments.put(player.getUniqueId(), attachment);
    }

    public static void removePermission(UUID uuid, String permission) {
        PermissionAttachment attachment = attachments.get(uuid);
        if (attachment != null) {
            attachment.unsetPermission(permission);
        }
    }

    public static void removePermission(Player player, String permission) {
        removePermission(player.getUniqueId(), permission);
    }

    public static void cleanup(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }

    public static void addPermission(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = attachments.computeIfAbsent(uuid, k ->
                player.addAttachment(plugin));
        attachment.setPermission(permission, true);
        player.recalculatePermissions();
    }
}
