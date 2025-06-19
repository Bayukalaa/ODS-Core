package dev.onlydarknesss.ODSCore.Utils;

import dev.onlydarknesss.ODSCore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WhiteListManager implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("odscore.whitelist")) {
            sender.sendMessage(Main.PREFIX + " §cYou don't have permission to use this command.");
            return true;
        }

        if (args.length != 2 || !args[0].equalsIgnoreCase("add")) {
            sender.sendMessage(Main.PREFIX + " §eUsage: /wl add <playerName>");
            return true;
        }

        String playerName = args[1];
        Main plugin = Main.getInstance();

        List<String> whitelist = plugin.getConfig().getStringList("system.white-list");

        if (whitelist.contains(playerName)) {
            sender.sendMessage(Main.PREFIX + " §e" + playerName + " is already on the whitelist.");
            return true;
        }

        whitelist.add(playerName);
        plugin.getConfig().set("system.white-list", whitelist);
        plugin.saveConfig();

        sender.sendMessage(Main.PREFIX + " §a" + playerName + " has been added to the whitelist.");
        return true;
    }
}
