package me.gauravbuilds.runeforgedrunes;

import com.cryptomorin.xseries.particles.XParticle;
import com.cryptomorin.xseries.XPotion;
import me.gauravbuilds.runeforgedrunes.commands.DebugCommand;
import me.gauravbuilds.runeforgedrunes.commands.RuneCommand;
import me.gauravbuilds.runeforgedrunes.gui.ForgeGui;
import me.gauravbuilds.runeforgedrunes.gui.RuneGUI;
import me.gauravbuilds.runeforgedrunes.gui.RuneShopGUI;
import me.gauravbuilds.runeforgedrunes.gui.RuneEditor;
import me.gauravbuilds.runeforgedrunes.gui.SalvageGui;
import me.gauravbuilds.runeforgedrunes.listeners.ForgeListener;
import me.gauravbuilds.runeforgedrunes.listeners.LootListener;
import me.gauravbuilds.runeforgedrunes.listeners.PlacedLogTracker;
import me.gauravbuilds.runeforgedrunes.listeners.SalvageListener;
import me.gauravbuilds.runeforgedrunes.listeners.OrbListener;
import me.gauravbuilds.runeforgedrunes.listeners.RuneEngine;
import me.gauravbuilds.runeforgedrunes.listeners.InventoryDragDropListener;
import me.gauravbuilds.runeforgedrunes.managers.CatalystManager;
import me.gauravbuilds.runeforgedrunes.managers.ConfigManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.managers.OrbManager;
import me.gauravbuilds.runeforgedrunes.tasks.PassiveRuneTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;

public class RuneForgedRunes extends JavaPlugin {
    private ConfigManager configManager;
    private RuneManager runeManager;
    private SlotManager slotManager;
    private CatalystManager catalystManager;
    private PlacedLogTracker placedLogTracker;
    private OrbManager orbManager;
    private RuneRegistry runeRegistry;
    private YamlConfiguration locale;
    private RuneEditor runeEditor;
    private RuneEngine runeEngine;
    private RuneShopGUI runeShopGUI;

    public void onEnable() {
        this.saveDefaultConfig();
        File effectsFile = new File(getDataFolder(), "effects-list.yml");
        if (!effectsFile.exists()) saveResource("effects-list.yml", false);
        YamlConfiguration effectsList = YamlConfiguration.loadConfiguration(effectsFile);
        YamlConfiguration effectDefaults = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("effects-list.yml"), StandardCharsets.UTF_8));
        effectsList.addDefaults(effectDefaults);
        effectsList.options().copyDefaults(true);
        effectsList.set("triggers", effectDefaults.getStringList("triggers"));
        effectsList.set("potions", Arrays.stream(XPotion.values()).filter(p -> p.get() != null).map(Enum::name).toList());
        try { effectsList.save(effectsFile); } catch (IOException e) { getLogger().warning("Could not update effects-list.yml: " + e.getMessage()); }
        File particlesFile = new File(getDataFolder(), "partciles-list.yml");
        if (!particlesFile.exists()) saveResource("partciles-list.yml", false);
        YamlConfiguration particleList = YamlConfiguration.loadConfiguration(particlesFile);
        List<String> availableParticles = Arrays.stream(XParticle.values()).filter(p -> p.get() != null && supportedParticleData(p.get())).map(Enum::name).toList();
        particleList.set("particles", availableParticles);
        try { particleList.save(particlesFile); } catch (IOException e) { getLogger().warning("Could not update partciles-list.yml: " + e.getMessage()); }
        this.loadLocale();
        this.configManager = new ConfigManager(this);
        this.runeRegistry = new RuneRegistry(this);
        this.runeManager = new RuneManager(this);
        this.slotManager = new SlotManager(this);
        this.catalystManager = new CatalystManager(this);
        this.placedLogTracker = new PlacedLogTracker(this);
        this.orbManager = new OrbManager(this);
        
        this.getServer().getPluginManager().registerEvents(this.placedLogTracker, this);
        this.getServer().getPluginManager().registerEvents(new LootListener(this), this);
        this.getServer().getPluginManager().registerEvents(new SalvageListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ForgeListener(this), this);
        this.getServer().getPluginManager().registerEvents(new RuneGUI(this), this);
        this.runeShopGUI = new RuneShopGUI(this);
        this.getServer().getPluginManager().registerEvents(this.runeShopGUI, this);
        this.getServer().getPluginManager().registerEvents(new OrbListener(this), this);
        this.runeEngine = new RuneEngine(this);
        this.getServer().getPluginManager().registerEvents(this.runeEngine, this);
        this.getServer().getPluginManager().registerEvents(new InventoryDragDropListener(this), this);
        this.runeEditor = new RuneEditor(this);
        this.getServer().getPluginManager().registerEvents(this.runeEditor, this);
        
        this.getCommand("rune").setExecutor(new RuneCommand(this));
        this.getCommand("rune").setTabCompleter(new RuneCommand(this));
        this.getCommand("runedebug").setExecutor(new DebugCommand(this));
        this.getCommand("runeshop").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) runeShopGUI.open(player, 0);
            else sender.sendMessage(getMessage("only-players"));
            return true;
        });
        this.getCommand("runeforge").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                (new ForgeGui(this)).open((Player)sender);
            }

            return true;
        });
        this.getCommand("runesalvage").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                (new SalvageGui(this)).open((Player)sender);
            }

            return true;
        });
        (new PassiveRuneTask(this)).runTaskTimer(this, 20L, 20L);
        this.getLogger().info("RuneForgedRunes has been enabled!");
    }

    public boolean supportedParticleData(Particle particle) {
        Class<?> type = particle.getDataType();
        return type == Void.class || type == Particle.DustOptions.class || type == Particle.DustTransition.class
                || type == Particle.Trail.class || type == Vibration.class || type == Color.class
                || type == Float.class || type == Integer.class || BlockData.class.isAssignableFrom(type)
                || ItemStack.class.isAssignableFrom(type);
    }

    public void onDisable() {
        this.getLogger().info("RuneForgedRunes has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public RuneManager getRuneManager() {
        return this.runeManager;
    }

    public SlotManager getSlotManager() {
        return this.slotManager;
    }

    public CatalystManager getCatalystManager() {
        return this.catalystManager;
    }

    public PlacedLogTracker getPlacedLogTracker() {
        return this.placedLogTracker;
    }
    
    public OrbManager getOrbManager() {
        return this.orbManager;
    }

    public RuneRegistry getRuneRegistry() {
        return this.runeRegistry;
    }

    public RuneEditor getRuneEditor() {
        return runeEditor;
    }

    public RuneEngine getRuneEngine() { return runeEngine; }

    public RuneShopGUI getRuneShopGUI() { return runeShopGUI; }

    public @NotNull Component getMessage(String key) {
        String msg = locale.getString("messages." + key, this.getConfig().getString("messages." + key, key));
        return LegacyComponentSerializer.legacySection().deserialize(ColorUtil.parse(msg));
    }

    public void loadLocale() {
        String[] languages = {"en", "ru", "ja", "zh", "fr", "es", "ko", "de", "pt", "pl", "ar"};
        for (String language : languages) {
            File file = new File(getDataFolder(), "locale/" + language + ".yml");
            if (!file.exists()) saveResource("locale/" + language + ".yml", false);
        }
        String language = getConfig().getString("locale", "en");
        File selected = new File(getDataFolder(), "locale/" + language + ".yml");
        locale = YamlConfiguration.loadConfiguration(selected.isFile() ? selected : new File(getDataFolder(), "locale/en.yml"));
    }
}