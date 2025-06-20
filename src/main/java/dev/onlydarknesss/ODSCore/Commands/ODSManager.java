package dev.onlydarknesss.ODSCore.Commands;

import dev.onlydarknesss.ODSCore.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ODSManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ods.default")) {
            sender.sendMessage(Main.PREFIX + " §cYou don't have permission to use this command.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.PREFIX + " §cYou can't use this command from the console.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Main.PREFIX + " §eUsage: /ods <help|admin|settings|maintenance>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sender.sendMessage(Main.PREFIX + " §eSOON");

            case "admin" -> {
                if (sender.hasPermission("ods.admin") || sender.hasPermission("ods.bypass")) {
                    sender.sendMessage(Main.PREFIX + " §eSOON");
                } else {
                    sender.sendMessage(Main.PREFIX + " §cYou don't have permission to use this command.");
                }
            }

            case "settings" -> {
                if (sender.hasPermission("ods.settings") || sender.hasPermission("ods.bypass")) {
                    sender.sendMessage(Main.PREFIX + " §eSOON");
                } else {
                    sender.sendMessage(Main.PREFIX + " §cYou don't have permission to use this command.");
                }
            }

            case "maintenance" -> {
                if (sender.hasPermission("ods.admin") || sender.hasPermission("ods.bypass")) {
                    if (args.length > 1) {
                        if (args[1].equalsIgnoreCase("on")) {
                            Main.setMaintenanceStatus("on");
                            sender.sendMessage(Main.PREFIX + " §aMaintenance mode has been §lENABLED§r§a.");
                        } else if (args[1].equalsIgnoreCase("off")) {
                            Main.setMaintenanceStatus("off");
                            sender.sendMessage(Main.PREFIX + " §cMaintenance mode has been §lDISABLED§r§c.");
                        } else {
                            sender.sendMessage(Main.PREFIX + " §eCurrent maintenance status: §b" + Main.getMaintenanceStatus().toUpperCase());
                        }
                    } else {
                        sender.sendMessage(Main.PREFIX + " §eCurrent maintenance status: §b" + Main.getMaintenanceStatus().toUpperCase());
                        sender.sendMessage(Main.PREFIX + " §7Usage: /ods maintenance <on | off>");
                    }
                } else {
                    sender.sendMessage(Main.PREFIX + " §cYou don't have permission to use this command.");
                }
            }


            default -> sender.sendMessage(Main.PREFIX + " §cUnknown subcommand. Use /ods help for usage.");
        }

        return true;
    }
}