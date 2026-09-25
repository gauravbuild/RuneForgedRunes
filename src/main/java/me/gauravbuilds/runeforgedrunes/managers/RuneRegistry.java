package me.gauravbuilds.runeforgedrunes.managers;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneTarget;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuneRegistry {
    private final RuneForgedRunes plugin;
    private final File file;
    private final Map<String, Definition> runes = new LinkedHashMap<>();
    private YamlConfiguration yaml;

    public RuneRegistry(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "runes.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) plugin.saveResource("runes.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStreamReader reader = new InputStreamReader(plugin.getResource("runes.yml"), StandardCharsets.UTF_8)) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
            ConfigurationSection shipped = defaults.getConfigurationSection("runes");
            if (shipped != null && !yaml.getBoolean("default-abilities-migrated", false)) for (String id : shipped.getKeys(false)) {
                String path = "runes." + id + ".abilities";
                ConfigurationSection current = yaml.getConfigurationSection(path);
                if (yaml.isConfigurationSection("runes." + id) && (current == null || current.getKeys(false).isEmpty()) && defaults.isConfigurationSection(path)) {
                    yaml.set(path, defaults.getConfigurationSection(path));
                }
            }
        } catch (IOException e) { plugin.getLogger().warning("Could not read bundled rune defaults: " + e.getMessage()); }
        if (!yaml.getBoolean("default-abilities-migrated", false)) {
            yaml.set("default-abilities-migrated", true);
            save();
        }
        runes.clear();
        ConfigurationSection root = yaml.getConfigurationSection("runes");
        if (root == null) return;
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;
            List<Ability> abilities = new ArrayList<>();
            ConfigurationSection abilitySection = section.getConfigurationSection("abilities");
            if (abilitySection != null) for (String name : abilitySection.getKeys(false)) {
                ConfigurationSection entry = abilitySection.getConfigurationSection(name);
                if (entry == null) continue;
                ConfigurationSection conditions = entry.getConfigurationSection("conditions");
                abilities.add(new Ability(name, entry.getString("trigger", ""), conditions == null ? Collections.emptyMap() : conditions.getValues(false), entry.getStringList("effects"), entry.getStringList("description"), entry.getString("action-bar", "")));
            }
            runes.put(id.toUpperCase(java.util.Locale.ROOT), new Definition(id, section.getString("display-name", id), section.getString("category", "CUSTOM"), section.getString("rarity", "COMMON"), section.getString("base-item", "NETHER_STAR"), section.getInt("custom-model-data", 0), section.getBoolean("glow"), section.getStringList("lore"), section.getStringList("target-items"), section.getDouble("default-success-rate", 50), section.getDouble("default-destroy-rate", 0), abilities));
        }
    }

    public void save() {
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().severe("Could not save runes.yml: " + e.getMessage()); }
    }

    public YamlConfiguration configuration() { return yaml; }
    public Definition get(String id) { return id == null ? null : runes.get(id.toUpperCase(java.util.Locale.ROOT)); }
    public Collection<Definition> all() { return Collections.unmodifiableCollection(runes.values()); }

    public record Ability(String id, String trigger, Map<String, Object> conditions, List<String> effects, List<String> description, String actionBar) {
        public double number(String key, double fallback) {
            Object value = conditions.get(key);
            if (value instanceof Number number) return number.doubleValue();
            try { return value == null ? fallback : Double.parseDouble(value.toString()); } catch (NumberFormatException e) { return fallback; }
        }
        public String text(String key) { return String.valueOf(conditions.getOrDefault(key, "")); }
    }

    public record Definition(String id, String name, String category, String rarity, String material, int modelData, boolean glow, List<String> lore, List<String> targets, double success, double destroy, List<Ability> abilities) {
        public boolean accepts(ItemStack item) {
            if (item == null) return false;
            String materialName = item.getType().name();
            for (String target : targets) {
                try { if (RuneTarget.valueOf(target.toUpperCase(java.util.Locale.ROOT)).includes(item)) return true; }
                catch (IllegalArgumentException ignored) { }
                String name = target.toUpperCase(java.util.Locale.ROOT);
                if (name.equals(materialName) || (name.equals("SWORDS") && materialName.endsWith("_SWORD")) || (name.equals("AXES") && materialName.endsWith("_AXE")) || (name.equals("PICKAXES") && materialName.endsWith("_PICKAXE")) || (name.equals("ARMOR") && (materialName.endsWith("_HELMET") || materialName.endsWith("_CHESTPLATE") || materialName.endsWith("_LEGGINGS") || materialName.endsWith("_BOOTS")))) return true;
            }
            return false;
        }
    }
}