package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.gui.SalvageGui;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SalvageListener implements Listener {

    private final RuneForgedRunes plugin;
    private final int SLOT_INPUT = 13;
    private final int XP_COST = 5;

    public SalvageListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(SalvageGui.TITLE)) return;
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() != inv) return;

        if (event.getSlot() == SLOT_INPUT) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateGui(inv, player), 1L);
            return;
        }

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();

        // --- FIXED CHECK HERE ---
        if (clicked != null && clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
            List<String> lore = clicked.getItemMeta().getLore();
            // We strip color to match the text safely
            boolean isRuneButton = lore.stream()
                    .anyMatch(line -> ChatColor.stripColor(line).contains("[REMOVE RUNE]"));

            if (isRuneButton) {
                handleSalvage(player, inv, clicked);
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(SalvageGui.TITLE)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateGui(event.getInventory(), (Player) event.getWhoClicked()), 1L);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(SalvageGui.TITLE)) {
            ItemStack item = event.getInventory().getItem(SLOT_INPUT);
            if (item != null && item.getType() != Material.AIR) {
                event.getPlayer().getInventory().addItem(item);
            }
        }
    }

    private void updateGui(Inventory inv, Player player) {
        ItemStack item = inv.getItem(SLOT_INPUT);
        for (int i = 27; i < 45; i++) inv.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));

        if (item == null || item.getType() == Material.AIR) return;

        List<RuneType> runes = plugin.getSlotManager().getAppliedRunes(item);
        if (runes.isEmpty()) return;

        int slot = 27;
        for (RuneType rune : runes) {
            ItemStack btn = plugin.getRuneManager().getRuneItem(rune);
            ItemMeta meta = btn.getItemMeta();

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add(ColorUtil.parse("&c&l[REMOVE RUNE]")); // Matches the check above
            lore.add(ColorUtil.parse("&7Cost: &b" + XP_COST + " XP Levels"));
            lore.add(ColorUtil.parse("&eClick to extract safely."));
            meta.setLore(lore);

            btn.setItemMeta(meta);
            inv.setItem(slot, btn);
            slot++;
        }
    }

    private void handleSalvage(Player player, Inventory inv, ItemStack btn) {
        ItemStack item = inv.getItem(SLOT_INPUT);
        if (item == null) return;

        String btnName = btn.getItemMeta().getDisplayName();

        List<RuneType> runes = plugin.getSlotManager().getAppliedRunes(item);
        RuneType target = null;

        for (RuneType r : runes) {
            String realName = plugin.getRuneManager().getRuneItem(r).getItemMeta().getDisplayName();
            if (btnName.equals(realName)) {
                target = r;
                break;
            }
        }

        if (target != null) {
            if (player.getLevel() < XP_COST) {
                player.sendMessage(ColorUtil.parse("&cYou need " + XP_COST + " XP Levels to remove this!"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            player.setLevel(player.getLevel() - XP_COST);

            runes.remove(target);
            plugin.getSlotManager().forceUpdateRunes(item, runes);

            ItemStack refund = plugin.getRuneManager().getRuneItem(target);
            if (!player.getInventory().addItem(refund).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), refund);
            }

            player.sendMessage(ColorUtil.parse("&aRune extracted successfully!"));
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);

            updateGui(inv, player);
        } else {
            player.sendMessage(ColorUtil.parse("&cError identifying rune. Try taking the item out and putting it back."));
        }
    }
}