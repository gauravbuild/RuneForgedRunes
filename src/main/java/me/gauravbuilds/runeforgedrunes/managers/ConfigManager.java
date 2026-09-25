//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.managers;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        this.plugin.reloadConfig();
        this.config = this.plugin.getConfig();
    }

    public double getDropRate(String path) {
        return this.config.getDouble("drop-rates." + path, (double)0.0F);
    }

    public String getMessage(String path) {
        return this.config.getString("messages." + path, "");
    }

    public String getString(String path, String defaultValue) {
        return this.config.getString(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public List<Integer> getIntegerList(String path) {
        return this.config.getIntegerList(path);
    }
}
