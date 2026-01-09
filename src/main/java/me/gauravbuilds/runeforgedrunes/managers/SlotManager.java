package me.gauravbuilds.runeforgedrunes.managers;

import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SlotManager {

    private final JavaPlugin plugin;
    private final NamespacedKey runesKey;
    private static final int MAX_SLOTS = 3;

    // --- YOUR CUSTOM DESIGN ---
    private static final String HEADER = ColorUtil.parse("&f&m---------------");
    private static final String TITLE = ColorUtil.parse("&9&lApplied Runes:");
    private static final String BULLET = ColorUtil.parse("&f- ");
    private static final String EMPTY = ColorUtil.parse("&f- None");

    public SlotManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.runesKey = new NamespacedKey(plugin, "applied_runes");
    }

    public List<RuneType> getAppliedRunes(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Collections.emptyList();
        String data = item.getItemMeta().getPersistentDataContainer().get(runesKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return Collections.emptyList();

        List<RuneType> runes = new ArrayList<>();
        for (String s : data.split(",")) {
            try {
                runes.add(RuneType.valueOf(s));
            } catch (IllegalArgumentException ignored) {}
        }
        return runes;
    }

    public boolean hasEmptySlot(ItemStack item) {
        return getAppliedRunes(item).size() < MAX_SLOTS;
    }

    public boolean applyRune(ItemStack item, RuneType rune) {
        if (!hasEmptySlot(item)) return false;

        List<RuneType> currentRunes = new ArrayList<>(getAppliedRunes(item));
        currentRunes.add(rune);

        saveRunes(item, currentRunes);
        updateLore(item, currentRunes);
        return true;
    }

    // --- THE MISSING METHOD (ADDED HERE) ---
    public void forceUpdateRunes(ItemStack item, List<RuneType> newRunes) {
        saveRunes(item, newRunes);
        updateLore(item, newRunes);
    }
    // ----------------------------------------

    public void ensureSlots(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (isTargetable(item)) {
            ItemMeta meta = item.getItemMeta();
            if (!meta.getPersistentDataContainer().has(runesKey, PersistentDataType.STRING)) {
                meta.getPersistentDataContainer().set(runesKey, PersistentDataType.STRING, "");
                item.setItemMeta(meta);
                updateLore(item, new ArrayList<>());
            }
        }
    }

    private void updateLore(ItemStack item, List<RuneType> runes) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        removeRuneBlock(lore);
        cleanUpLore(lore);

        if (!lore.isEmpty()) {
            lore.add("");
        }

        List<String> runeBlock = new ArrayList<>();
        runeBlock.add(HEADER);
        runeBlock.add("");
        runeBlock.add(TITLE);

        for (int i = 0; i < MAX_SLOTS; i++) {
            if (i < runes.size()) {
                runeBlock.add(BULLET + ColorUtil.parse(runes.get(i).getDisplayName()));
            } else {
                runeBlock.add(EMPTY);
            }
        }
        runeBlock.add("");
        runeBlock.add(HEADER);

        lore.addAll(runeBlock);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private void removeRuneBlock(List<String> lore) {
        if (lore.isEmpty()) return;
        lore.removeIf(line ->
                line.equals(HEADER) || line.equals(TITLE) ||
                        line.startsWith(BULLET) || line.equals(EMPTY)
        );
    }

    private void cleanUpLore(List<String> lore) {
        while (!lore.isEmpty() && lore.get(lore.size() - 1).isEmpty()) {
            lore.remove(lore.size() - 1);
        }
    }

    private void saveRunes(ItemStack item, List<RuneType> runes) {
        ItemMeta meta = item.getItemMeta();
        String data = runes.stream().map(Enum::name).collect(Collectors.joining(","));
        meta.getPersistentDataContainer().set(runesKey, PersistentDataType.STRING, data);
        item.setItemMeta(meta);
    }

    private boolean isTargetable(ItemStack item) {
        String t = item.getType().name();
        return t.endsWith("_SWORD") || t.endsWith("_AXE") || t.endsWith("_PICKAXE") ||
                t.endsWith("_SHOVEL") || t.endsWith("_HOE") || t.endsWith("_HELMET") ||
                t.endsWith("_CHESTPLATE") || t.endsWith("_LEGGINGS") || t.endsWith("_BOOTS") ||
                t.equals("BOW") || t.equals("CROSSBOW");
    }
}