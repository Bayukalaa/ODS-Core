package dev.onlydarknesss.ODSCore;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static Boolean DEV_MODE = null;
    private static String VERSION = null;

    @Override
    public void onEnable(){
        saveDefaultConfig();
        instance = this;
        File config = new File(String.valueOf(getConfig()));

        if(config.exists()){
            DEV_MODE = getConfig().getBoolean("dev-mode", false);
            VERSION = getConfig().getString("pre-alpha", "pre-alpha");
        } else {
            saveDefaultConfig();
        }

        if (DEV_MODE == true){
            getLogger().info("Server is on maintenance, if you think there was an error, check config file or contact to developer");
        }
    }

    public static Main getInstance(){return instance;}
}
