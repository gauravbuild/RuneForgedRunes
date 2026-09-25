package me.gauravbuilds.runeforgedrunes.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SlotManager {
    private final JavaPlugin plugin;
    private final NamespacedKey runesKey;
    private final NamespacedKey loreLinesKey;
    private static final String HEADER = ColorUtil.parse("&f&m---------------");
    private static final String TITLE = ColorUtil.parse("&9&lApplied Runes:");

    public SlotManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runesKey = new NamespacedKey(plugin, "applied_runes");
        this.loreLinesKey = new NamespacedKey(plugin, "rune_lore_lines");
    }

    public boolean hasEmptySlot(ItemStack item) {
        return getRuneIds(item).size() < plugin.getConfig().getInt("visual-formatting.max-sockets-per-item", 3);
    }

    public List<String> getRuneIds(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return new ArrayList<>();
        String data = item.getItemMeta().getPersistentDataContainer().get(runesKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(java.util.Arrays.asList(data.split(",")));
    }

    public boolean applyRune(ItemStack item, String id) {
        if (item == null || id == null || ((me.gauravbuilds.runeforgedrunes.RuneForgedRunes) plugin).getRuneRegistry().get(id) == null || !hasEmptySlot(item) || getRuneIds(item).stream().anyMatch(existing -> existing.equalsIgnoreCase(id))) return false;
        List<String> ids = getRuneIds(item);
        ids.add(id);
        updateRuneIds(item, ids);
        return true;
    }

    public void updateRuneIds(ItemStack item, List<String> ids) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        Integer oldLines = meta.getPersistentDataContainer().get(loreLinesKey, PersistentDataType.INTEGER);
        String previousIds = meta.getPersistentDataContainer().get(runesKey, PersistentDataType.STRING);
        if (oldLines != null && oldLines > 0 && oldLines <= lore.size()) {
            lore.subList(lore.size() - oldLines, lore.size()).clear();
        } else if (previousIds != null && lore.contains(TITLE)) {
            int title = lore.indexOf(TITLE);
            int start = title > 1 && lore.get(title - 2).equals(HEADER) ? title - 2 : title;
            while (start > 0 && lore.get(start - 1).isEmpty()) start--;
            lore.subList(start, lore.size()).clear();
        }
        meta.getPersistentDataContainer().set(runesKey, PersistentDataType.STRING, String.join(",", ids));
        int originalSize = lore.size();
        if (!ids.isEmpty()) {
            if (!lore.isEmpty()) lore.add("");
            lore.add(ColorUtil.parse(plugin.getConfig().getString("visual-formatting.applied-lore.header", "&9&lRunes:")));
            for (String id : ids) {
                RuneRegistry.Definition def = ((me.gauravbuilds.runeforgedrunes.RuneForgedRunes) plugin).getRuneRegistry().get(id);
                String color = def == null ? "&d" : switch (def.rarity().toUpperCase(java.util.Locale.ROOT)) {
                    case "COMMON" -> "&f";
                    case "RARE" -> "&b";
                    case "LEGENDARY" -> "&6";
                    default -> "&d";
                };
                lore.add(ColorUtil.parse(plugin.getConfig().getString("visual-formatting.applied-lore.rune-title", "&8▪ &d{rune_name}").replace("{rune_name}", def == null ? id : def.name()).replace("{rune_color}", color)));
            }
            int capacity = Math.max(0, plugin.getConfig().getInt("visual-formatting.max-sockets-per-item", 3));
            for (int i = ids.size(); i < capacity; i++) lore.add(ColorUtil.parse(plugin.getConfig().getString("visual-formatting.sockets.empty-socket", "&8[&7✧ Empty Socket&8]")));
        }
        meta.getPersistentDataContainer().set(loreLinesKey, PersistentDataType.INTEGER, lore.size() - originalSize);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void forceUpdateRunes(ItemStack item, List<String> newRunes) {
        updateRuneIds(item, newRunes);
    }

    public void ensureSlots(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            if (this.isTargetable(item)) {
                ItemMeta meta = item.getItemMeta();
                if (!meta.getPersistentDataContainer().has(this.runesKey, PersistentDataType.STRING)) {
                    meta.getPersistentDataContainer().set(this.runesKey, PersistentDataType.STRING, "");
                    item.setItemMeta(meta);
                }
            }
        }
    }

    private boolean isTargetable(ItemStack item) {
        String t = item.getType().name();
        return t.endsWith("_SWORD") || t.endsWith("_AXE") || t.endsWith("_PICKAXE") || t.endsWith("_SHOVEL") || t.endsWith("_HOE") || t.endsWith("_HELMET") || t.endsWith("_CHESTPLATE") || t.endsWith("_LEGGINGS") || t.endsWith("_BOOTS") || t.equals("BOW") || t.equals("CROSSBOW") || t.equals("FISHING_ROD");
    }
}