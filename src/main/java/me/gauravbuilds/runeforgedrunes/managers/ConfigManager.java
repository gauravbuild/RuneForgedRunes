package me.gauravbuilds.runeforgedrunes.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public double getDropRate(String path) {
        return config.getDouble("drop-rates." + path, 0.0);
    }
    
    public String getMessage(String path) {
        return config.getString("messages." + path, "");
    }
}
