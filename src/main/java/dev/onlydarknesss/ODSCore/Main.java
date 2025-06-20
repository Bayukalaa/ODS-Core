package dev.onlydarknesss.ODSCore;

import dev.onlydarknesss.ODSCore.Commands.ODSManager;
import dev.onlydarknesss.ODSCore.Utils.ChatListener;
import dev.onlydarknesss.ODSCore.Utils.WhiteListManager;
import dev.onlydarknesss.ODSCore.WebDashboard.WebServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;
    public static boolean DEV_MODE;
    public static String VERSION;
    public static String PREFIX;

    private List<File> filesToCopy;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        DEV_MODE = config.getBoolean("dev-mode", false);
        VERSION = config.getString("pre-alpha", "pre-alpha");
        PREFIX = config.getString("system.prefix", "[ODS-Core]");

        WebServer webServer = new WebServer(this);
        webServer.start();

        getCommand("wl").setExecutor(new WhiteListManager());
        getCommand("ods").setExecutor(new ODSManager());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        filesToCopy = List.of(
                new File(getDataFolder(), "config.yml")
        );

        if (DEV_MODE) {
            getLogger().info("Server is on maintenance, if you think there was an error, check config file or contact the developer.");
            maintenance(true);
        }
    }

    public static String getMaintenanceStatus() {
        return getInstance().getConfig().getBoolean("dev-mode") ? "on" : "off";
    }

    public static String getVERSION(){
        return VERSION;
    }

    public static String getPREFIX(){
        return PREFIX;
    }

    public static void setMaintenanceStatus(String devMode) {
        Main instance = getInstance();
        boolean value = devMode.equalsIgnoreCase("on");
        instance.getConfig().set("dev-mode", value);
        instance.saveConfig();
        instance.reloadConfig();
    }


    private void maintenance(boolean devMode) {
        File logFolder = new File(getDataFolder(), "backups");

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        if (devMode) {
            File timeStampedFolder = new File(logFolder, timeStamp);
            if (!timeStampedFolder.exists()) {
                timeStampedFolder.mkdirs();
            }

            for (File srcFile : filesToCopy) {
                if (srcFile.exists()) {
                    File destFile = new File(timeStampedFolder, srcFile.getName());
                    try {
                        copyFile(srcFile, destFile);
                        getLogger().info("Copied: " + srcFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    getLogger().warning("File not found: " + srcFile.getAbsolutePath());
                }
            }
        }
    }

    private void copyFile(File source, File destination) throws IOException {
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination)
        ) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    public static Main getInstance() {
        return instance;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Main plugin = Main.getInstance();

        boolean devMode = plugin.getConfig().getBoolean("dev-mode", false);
        List<String> whitelist = plugin.getConfig().getStringList("system.white-list");

        if (devMode) {
            String playerName = event.getPlayer().getName();

            if (!whitelist.contains(playerName)) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                        "Server is currently under maintenance.\nYou are not whitelisted.");
            }
        }
    }
}
