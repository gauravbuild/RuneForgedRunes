package me.gauravbuilds.runeforgedrunes;

import me.gauravbuilds.runeforgedrunes.commands.RuneCommand;
import me.gauravbuilds.runeforgedrunes.listeners.*;
import me.gauravbuilds.runeforgedrunes.managers.ConfigManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.tasks.PassiveRuneTask;
import org.bukkit.plugin.java.JavaPlugin;

public class RuneForgedRunes extends JavaPlugin {

    private ConfigManager configManager;
    private RuneManager runeManager;
    private SlotManager slotManager;

    @Override
    public void onEnable() {
        // Managers
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.runeManager = new RuneManager(this);
        this.slotManager = new SlotManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new RuneApplyListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);
        getServer().getPluginManager().registerEvents(new FarmingForagingListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantingSorceryListener(this), this);
        getServer().getPluginManager().registerEvents(new LootListener(this), this);

        // Commands
        getCommand("rune").setExecutor(new RuneCommand(this));
        getCommand("rune").setTabCompleter(new RuneCommand(this));

        // Tasks
        new PassiveRuneTask(this).runTaskTimer(this, 20L, 20L); // 1 second

        getLogger().info("RuneForgedRunes has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RuneForgedRunes has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RuneManager getRuneManager() {
        return runeManager;
    }

    public SlotManager getSlotManager() {
        return slotManager;
    }
}
