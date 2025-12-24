package me.gauravbuilds.runeforgedrunes.managers;

import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.RuneType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class RuneManager {

    private final JavaPlugin plugin;
    private final NamespacedKey runeKey;
    private final NamespacedKey chanceKey;

    public RuneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runeKey = new NamespacedKey(plugin, "rune_type");
        this.chanceKey = new NamespacedKey(plugin, "rune_chance");
    }

    public ItemStack createRune(RuneType type) {
        // Default chance if not specified
        return createRune(type, type.getRarity().getDefaultChance());
    }

    public ItemStack createRune(RuneType type, double chance) {
        Material mat = getMaterialForRarity(type.getRarity());
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Component name = Component.text("♦ ", type.getRarity().getColor())
                    .append(Component.text(type.getDisplayName() + " Rune", type.getRarity().getColor()))
                    .decoration(TextDecoration.ITALIC, false);

            meta.displayName(name);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Tier: ", NamedTextColor.GRAY)
                    .append(Component.text(type.getRarity().getDisplayName(), type.getRarity().getColor()))
                    .decoration(TextDecoration.ITALIC, false));

            lore.add(Component.text("Success Chance: ", NamedTextColor.GRAY)
                    .append(Component.text((int) chance + "%", NamedTextColor.GREEN))
                    .decoration(TextDecoration.ITALIC, false));

            lore.add(Component.text("Applicable to: ", NamedTextColor.GRAY)
                    .append(Component.text(type.getTarget().getDisplayName(), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));

            lore.add(Component.empty());
            lore.add(Component.text("Description:", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(type.getDescription(), NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            meta.getPersistentDataContainer().set(runeKey, PersistentDataType.STRING, type.name());
            meta.getPersistentDataContainer().set(chanceKey, PersistentDataType.DOUBLE, chance);

            item.setItemMeta(meta);
        }
        return item;
    }

    public RuneType getRuneFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String typeName = item.getItemMeta().getPersistentDataContainer().get(runeKey, PersistentDataType.STRING);
        if (typeName == null) return null;
        try {
            return RuneType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public double getChanceFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0.0;
        Double chance = item.getItemMeta().getPersistentDataContainer().get(chanceKey, PersistentDataType.DOUBLE);
        return chance != null ? chance : 0.0;
    }

    private Material getMaterialForRarity(RuneRarity rarity) {
        switch (rarity) {
            case MYTHIC: return Material.NETHER_STAR;
            case LEGENDARY: return Material.GOLD_NUGGET;
            case RARE: return Material.LAPIS_LAZULI;
            case COMMON: return Material.IRON_NUGGET;
            default: return Material.CLAY_BALL;
        }
    }
}