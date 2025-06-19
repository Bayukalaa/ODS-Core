package dev.onlydarknesss.ODSCore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class Main extends JavaPlugin {

    private static Main instance;
    private static boolean DEV_MODE = false;
    private static String VERSION = "pre-alpha";

    private List<File> filesToCopy;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        DEV_MODE = config.getBoolean("dev-mode", false);
        VERSION = config.getString("pre-alpha", "pre-alpha");

        filesToCopy = List.of(
                new File(getDataFolder(), "config.yml")
        );

        if (DEV_MODE) {
            getLogger().info("Server is on maintenance, if you think there was an error, check config file or contact the developer.");
            maintenance(true);
        }
    }

    private void maintenance(boolean devMode) {
        File logFolder = new File(getDataFolder(), "Logs");

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
}
