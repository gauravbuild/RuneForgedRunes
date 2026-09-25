package me.gauravbuilds.runeforgedrunes.listeners;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlacedLogTracker implements Listener {
    private final RuneForgedRunes plugin;
    private final File storageFile;
    private final Set<String> placedLogs = new HashSet<>();

    public PlacedLogTracker(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "placed-logs.yml");
        this.load();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (this.isTracked(event.getBlock().getType())) {
            this.placedLogs.add(this.getKey(event.getBlock()));
            this.save();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        String key = this.getKey(block);
        if (this.placedLogs.contains(key)) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                if (block.getType().isAir()) {
                    this.placedLogs.remove(key);
                    this.save();
                }
            });
        }
    }

    public boolean isPlayerPlaced(Block block) {
        return this.placedLogs.contains(this.getKey(block));
    }

    private void load() {
        if (!this.storageFile.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.storageFile);
        this.placedLogs.addAll(config.getStringList("placed-logs"));
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("placed-logs", this.placedLogs.stream().sorted().toList());
        try {
            config.save(this.storageFile);
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Could not save placed log data: " + exception.getMessage());
        }
    }

    private String getKey(Block block) {
        UUID worldId = block.getWorld().getUID();
        return worldId + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
    }

    private boolean isTracked(Material material) {
        String name = material.name();
        return name.endsWith("_LOG") || name.endsWith("_ORE") || name.equals("ANCIENT_DEBRIS");
    }
}