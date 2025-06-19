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

        if (args.length < 1) {
            sender.sendMessage(Main.PREFIX + " §eUsage: /wl <add/remove/list/clear> [playerName]");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Main plugin = Main.getInstance();
        List<String> whitelist = plugin.getConfig().getStringList("system.white-list");

        switch (subCommand) {
            case "add" -> {
                if (args.length != 2) {
                    sender.sendMessage(Main.PREFIX + " §eUsage: /wl add <playerName>");
                    return true;
                }
                String playerToAdd = args[1];
                if (whitelist.contains(playerToAdd)) {
                    sender.sendMessage(Main.PREFIX + " §e" + playerToAdd + " is already on the whitelist.");
                    return true;
                }
                whitelist.add(playerToAdd);
                plugin.getConfig().set("system.white-list", whitelist);
                plugin.saveConfig();
                sender.sendMessage(Main.PREFIX + " §a" + playerToAdd + " has been added to the whitelist.");
            }

            case "remove" -> {
                if (args.length != 2) {
                    sender.sendMessage(Main.PREFIX + " §eUsage: /wl remove <playerName>");
                    return true;
                }
                String playerToRemove = args[1];
                if (!whitelist.contains(playerToRemove)) {
                    sender.sendMessage(Main.PREFIX + " §e" + playerToRemove + " is not in the whitelist.");
                    return true;
                }
                whitelist.remove(playerToRemove);
                plugin.getConfig().set("system.white-list", whitelist);
                plugin.saveConfig();
                sender.sendMessage(Main.PREFIX + " §c" + playerToRemove + " has been removed from the whitelist.");
            }

            case "list" -> {
                if (whitelist.isEmpty()) {
                    sender.sendMessage(Main.PREFIX + " §eWhitelist is currently empty.");
                } else {
                    sender.sendMessage(Main.PREFIX + " §aWhitelisted players:");
                    for (String name : whitelist) {
                        sender.sendMessage(" §f- " + name);
                    }
                }
            }

            case "clear" -> {
                whitelist.clear();
                plugin.getConfig().set("system.white-list", whitelist);
                plugin.saveConfig();
                sender.sendMessage(Main.PREFIX + " §cWhitelist has been cleared.");
            }

            default -> {
                sender.sendMessage(Main.PREFIX + " §eUnknown subcommand. Usage: /wl <add/remove/list/clear> [playerName]");
            }
        }

        return true;
    }
}
