package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.ConfigManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootListener implements Listener {

    private final RuneForgedRunes plugin;
    private final RuneManager runeManager;
    private final ConfigManager configManager;
    private final Random random = new Random();

    public LootListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.configManager = plugin.getConfigManager();
    }

    private RuneType getRandomRune(RuneRarity rarity) {
        List<RuneType> list = new ArrayList<>();
        for (RuneType t : RuneType.values()) {
            if (t.getRarity() == rarity) list.add(t);
        }
        if (list.isEmpty()) return RuneType.values()[0];
        return list.get(random.nextInt(list.size()));
    }
    
    private RuneType getRandomRune() {
        return RuneType.values()[random.nextInt(RuneType.values().length)];
    }
    
    private void dropRune(org.bukkit.Location loc, RuneRarity rarity) {
        RuneType rune = getRandomRune(rarity);
        loc.getWorld().dropItemNaturally(loc, runeManager.createRune(rune));
    }
    
    private void dropRandomRune(org.bukkit.Location loc) {
        RuneType rune = getRandomRune();
        loc.getWorld().dropItemNaturally(loc, runeManager.createRune(rune));
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        
        EntityType type = event.getEntityType();
        if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON) {
            if (random.nextDouble() < configManager.getDropRate("bosses")) {
                // Legendary or Mythic
                RuneRarity r = random.nextBoolean() ? RuneRarity.LEGENDARY : RuneRarity.MYTHIC;
                dropRune(event.getEntity().getLocation(), r);
            }
        } else {
             if (random.nextDouble() < configManager.getDropRate("global-mobs")) {
                 dropRandomRune(event.getEntity().getLocation());
             }
        }
    }
    
    @EventHandler
    public void onMine(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE || 
            type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) {
            if (random.nextDouble() < configManager.getDropRate("mining")) {
                dropRandomRune(event.getBlock().getLocation());
            }
        }
        
        // Farming drops
        if (event.getBlock().getBlockData() instanceof Ageable) {
            Ageable age = (Ageable) event.getBlock().getBlockData();
            if (age.getAge() == age.getMaximumAge()) {
                if (random.nextDouble() < configManager.getDropRate("farming")) {
                    dropRandomRune(event.getBlock().getLocation());
                }
            }
        }
    }
    
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (random.nextDouble() < configManager.getDropRate("fishing")) {
                if (event.getCaught() != null)
                   dropRandomRune(event.getCaught().getLocation());
            }
        }
    }
}
