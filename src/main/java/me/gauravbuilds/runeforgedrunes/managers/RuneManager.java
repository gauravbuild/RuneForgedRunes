package me.gauravbuilds.runeforgedrunes.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XItemFlag;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RuneManager {
    private final JavaPlugin plugin;
    private final NamespacedKey runeKey;
    private final NamespacedKey chanceKey;
    private final NamespacedKey destroyKey;
    private final NamespacedKey instanceKey;

    public RuneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runeKey = new NamespacedKey(plugin, "rune_type");
        this.chanceKey = new NamespacedKey(plugin, "rune_chance");
        this.destroyKey = new NamespacedKey(plugin, "rune_destroy");
        this.instanceKey = new NamespacedKey(plugin, "rune_instance");
    }

    public ItemStack createRune(String id) {
        RuneRegistry.Definition definition = ((RuneForgedRunes) plugin).getRuneRegistry().get(id);
        return definition == null ? null : createRune(id, definition.success());
    }

    public ItemStack createRune(String id, double chance) {
        RuneRegistry.Definition definition = ((RuneForgedRunes) plugin).getRuneRegistry().get(id);
        if (definition == null) return null;
        Material mat = XMaterial.matchXMaterial(definition.material()).map(XMaterial::parseMaterial).orElse(null);
        if (mat == null) mat = XMaterial.matchXMaterial("NETHER_STAR").map(XMaterial::parseMaterial).orElseThrow();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (definition.modelData() > 0) meta.setCustomModelData(definition.modelData());
            meta.setDisplayName(ColorUtil.parse(definition.name()));
            meta.setLore(buildLore(definition, chance, definition.destroy()));
            if (definition.glow()) {
                XEnchantment.matchXEnchantment("UNBREAKING").ifPresent(enchantment -> meta.addEnchant(enchantment.getEnchant(), 1, true));
                XItemFlag.of("HIDE_ENCHANTS").ifPresent(flag -> flag.set(meta));
            }
            meta.getPersistentDataContainer().set(this.runeKey, PersistentDataType.STRING, definition.id());
            meta.getPersistentDataContainer().set(this.chanceKey, PersistentDataType.DOUBLE, chance);
            meta.getPersistentDataContainer().set(this.destroyKey, PersistentDataType.DOUBLE, definition.destroy());
            meta.getPersistentDataContainer().set(this.instanceKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            item.setItemMeta(meta);
        }
        return item;
    }

    public String getRuneId(ItemStack item) {
        return item == null || !item.hasItemMeta() ? null : item.getItemMeta().getPersistentDataContainer().get(runeKey, PersistentDataType.STRING);
    }

    public double getDestroyFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        Double value = item.getItemMeta().getPersistentDataContainer().get(destroyKey, PersistentDataType.DOUBLE);
        RuneRegistry.Definition def = ((RuneForgedRunes) plugin).getRuneRegistry().get(getRuneId(item));
        return value == null ? def == null ? 0 : def.destroy() : value;
    }

    public void updateChance(ItemStack item, double chance) {
        if (item == null || !item.hasItemMeta()) return;
        RuneRegistry.Definition def = ((RuneForgedRunes) plugin).getRuneRegistry().get(getRuneId(item));
        if (def == null) return;
        double increase = Math.max(0, chance - getChanceFromItem(item));
        double destroy = Math.max(0, getDestroyFromItem(item) - increase);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(chanceKey, PersistentDataType.DOUBLE, chance);
        meta.getPersistentDataContainer().set(destroyKey, PersistentDataType.DOUBLE, destroy);
        meta.setLore(buildLore(def, chance, destroy));
        item.setItemMeta(meta);
    }

    private List<String> buildLore(RuneRegistry.Definition def, double chance, double destroy) {
        List<String> lore = new ArrayList<>();
        for (String line : def.lore()) lore.add(ColorUtil.parse(line.replace("{success}", String.valueOf((int) chance)).replace("{destroy}", String.valueOf((int) destroy))));
        if (!def.abilities().isEmpty()) {
            lore.add("");
            for (RuneRegistry.Ability ability : def.abilities()) {
                lore.add(ColorUtil.parse("&d✦ " + ability.id().replace('_', ' ') + " &8[" + ability.trigger() + "]"));
                for (String line : ability.description()) lore.add(ColorUtil.parse("&7  " + line));
            }
        }
        lore.add(ColorUtil.parse("&7Appliable to: &f" + String.join(", ", def.targets()).replace('_', ' ')));
        return lore;
    }

    public double getChanceFromItem(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            Double chance = (Double)item.getItemMeta().getPersistentDataContainer().get(this.chanceKey, PersistentDataType.DOUBLE);
            if (chance != null) return chance;
            RuneRegistry.Definition definition = ((RuneForgedRunes) plugin).getRuneRegistry().get(getRuneId(item));
            return definition == null ? 0 : definition.success();
        } else {
            return 0.0D;
        }
    }
}