//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.managers.ConfigManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

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

    private RuneRegistry.Definition getRandomRune(RuneRarity rarity) {
        List<RuneRegistry.Definition> list = new ArrayList<>();
        for (RuneRegistry.Definition definition : plugin.getRuneRegistry().all()) {
            if (rarity == null || definition.rarity().equalsIgnoreCase(rarity.name())) list.add(definition);
        }
        return list.isEmpty() ? null : list.get(random.nextInt(list.size()));
    }

    private void dropRune(Location loc, RuneRarity rarity) {
        RuneRegistry.Definition rune = this.getRandomRune(rarity);
        if (rune != null) loc.getWorld().dropItemNaturally(loc, this.runeManager.createRune(rune.id()));
    }

    private void dropRandomRune(Location loc) {
        RuneRegistry.Definition rune = this.getRandomRune(null);
        if (rune != null) loc.getWorld().dropItemNaturally(loc, this.runeManager.createRune(rune.id()));
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            EntityType type = event.getEntityType();
            if (type != EntityType.WITHER && type != EntityType.ENDER_DRAGON) {
                if (this.random.nextDouble() < this.configManager.getDropRate("global-mobs")) {
                    this.dropRandomRune(event.getEntity().getLocation());
                }
            } else if (this.random.nextDouble() < this.configManager.getDropRate("bosses")) {
                RuneRarity r = this.random.nextBoolean() ? RuneRarity.LEGENDARY : RuneRarity.MYTHIC;
                this.dropRune(event.getEntity().getLocation(), r);
            }

        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        if (event.isCancelled() || plugin.getPlacedLogTracker().isPlayerPlaced(event.getBlock())) return;
        Material type = event.getBlock().getType();
        if ((type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE || type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) && this.random.nextDouble() < this.configManager.getDropRate("mining")) {
            this.dropRandomRune(event.getBlock().getLocation());
        }

        if (event.getBlock().getBlockData() instanceof Ageable) {
            Ageable age = (Ageable)event.getBlock().getBlockData();
            if (age.getAge() == age.getMaximumAge() && this.random.nextDouble() < this.configManager.getDropRate("farming")) {
                this.dropRandomRune(event.getBlock().getLocation());
            }
        }

    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == State.CAUGHT_FISH && this.random.nextDouble() < this.configManager.getDropRate("fishing") && event.getCaught() != null) {
            this.dropRandomRune(event.getCaught().getLocation());
        }

    }
}
