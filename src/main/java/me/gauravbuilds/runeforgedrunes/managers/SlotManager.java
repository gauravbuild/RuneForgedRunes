package me.gauravbuilds.runeforgedrunes.managers;

import me.gauravbuilds.runeforgedrunes.RuneTarget;
import me.gauravbuilds.runeforgedrunes.RuneType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    private static final Component EMPTY_SLOT = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("Empty Rune Slot", NamedTextColor.GRAY))
            .append(Component.text("]", NamedTextColor.DARK_GRAY))
            .decoration(TextDecoration.ITALIC, false);

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

        // CRITICAL FIX: Wrap in ArrayList to ensure mutability
        List<RuneType> currentRunes = new ArrayList<>(getAppliedRunes(item));
        currentRunes.add(rune);

        saveRunes(item, currentRunes);
        updateLore(item, currentRunes);
        return true;
    }

    public void ensureSlots(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (!isTargetable(item)) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(runesKey, PersistentDataType.STRING)) {
            meta.getPersistentDataContainer().set(runesKey, PersistentDataType.STRING, "");
            item.setItemMeta(meta);
            updateLore(item, new ArrayList<>());
        }
    }

    private boolean isTargetable(ItemStack item) {
        return RuneTarget.MELEE_WEAPON.includes(item) ||
                RuneTarget.ARMOR.includes(item) ||
                RuneTarget.TOOL.includes(item) ||
                RuneTarget.BOW.includes(item);
    }

    private void saveRunes(ItemStack item, List<RuneType> runes) {
        ItemMeta meta = item.getItemMeta();
        String data = runes.stream().map(Enum::name).collect(Collectors.joining(","));
        meta.getPersistentDataContainer().set(runesKey, PersistentDataType.STRING, data);
        item.setItemMeta(meta);
    }

    private void updateLore(ItemStack item, List<RuneType> runes) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();

        // Remove existing rune/slot lines
        lore.removeIf(this::isRuneLine);

        // Add visual gap/spacer if lore exists and last line isn't empty
        if (!lore.isEmpty()) {
            Component lastLine = lore.get(lore.size() - 1);
            if (!lastLine.equals(Component.empty())) {
                lore.add(Component.empty());
            }
        }

        // Add applied runes
        for (RuneType rune : runes) {
            lore.add(Component.text("♦ " + rune.getDisplayName(), rune.getRarity().getColor())
                    .decoration(TextDecoration.ITALIC, false));
        }

        // Add empty slots
        int emptySlots = MAX_SLOTS - runes.size();
        for (int i = 0; i < emptySlots; i++) {
            lore.add(EMPTY_SLOT);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private boolean isRuneLine(Component c) {
        if (c.equals(EMPTY_SLOT)) return true;

        String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(c);
        if (text.startsWith("♦ ")) return true;
        if (text.contains("Empty Rune Slot")) return true;

        return false;
    }
}