package dev.onlydarknesss.ODSCore.Lang;

import dev.onlydarknesss.ODSCore.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Lang {
    private static final Map<String, String> msg = new HashMap<>();

    public static void init(JavaPlugin pl){
        String lang = pl.getConfig().getString("system.lang", "en_US");
        File langF = new File(pl.getDataFolder(), "Lang/" + lang + ".yml");

        if (!langF.exists()){
            pl.saveResource("Lang/" + lang + ".yml", false);
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(langF);

        InputStreamReader inputStreamReader = new InputStreamReader(
                pl.getResource("Lang/" + lang + ".yml"),
                StandardCharsets.UTF_8
        );

        if(inputStreamReader != null){
            conf.setDefaults(YamlConfiguration.loadConfiguration(langF));
        }

        for(String key : conf.getKeys(true)){
            if (conf.isString(key)){
                msg.put(key, conf.getString(key));
            }
        }
    }

    public static String get(String key){
        return ChatColor.translateAlternateColorCodes('&', Main.getPREFIX() + getRaw(key));
    }

    public static String get(String key, Map<String, String> placeholders) {
        String msg = getRaw(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', Main.getPREFIX() + msg);
    }

    private static String getRaw(String key) {
        return msg.getOrDefault(key, "&c[" + key + " not found]");
    }
}
