package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.gui.ForgeGui;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
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

import java.util.Collections;
import java.util.Random;

public class ForgeListener implements Listener {

    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();

    // Slots
    private final int SLOT_WEAPON = 20;
    private final int SLOT_RUNE = 22;
    private final int SLOT_CATALYST = 24;
    private final int SLOT_BUTTON = 40;

    public ForgeListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ForgeGui.TITLE)) return;

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            int slot = event.getSlot();
            if (slot != SLOT_WEAPON && slot != SLOT_RUNE && slot != SLOT_CATALYST) {
                event.setCancelled(true);
            }
            if (slot == SLOT_BUTTON) {
                handleForge((Player) event.getWhoClicked(), event.getInventory());
                return;
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateButton(event.getView().getTopInventory()), 1L);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(ForgeGui.TITLE)) {
            for (int slot : event.getRawSlots()) {
                if (slot < 45 && slot != SLOT_WEAPON && slot != SLOT_RUNE && slot != SLOT_CATALYST) {
                    event.setCancelled(true);
                    return;
                }
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> updateButton(event.getView().getTopInventory()), 1L);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ForgeGui.TITLE)) {
            returnItem((Player) event.getPlayer(), event.getInventory(), SLOT_WEAPON);
            returnItem((Player) event.getPlayer(), event.getInventory(), SLOT_RUNE);
            returnItem((Player) event.getPlayer(), event.getInventory(), SLOT_CATALYST);
        }
    }

    private void handleForge(Player player, Inventory inv) {
        ItemStack weapon = inv.getItem(SLOT_WEAPON);
        ItemStack runeItem = inv.getItem(SLOT_RUNE);

        // 1. VALIDATE RUNE (Direct Check)
        RuneType rune = plugin.getRuneManager().getRuneFromItem(runeItem);

        if (weapon == null || rune == null) {
            player.sendMessage(ColorUtil.parse("&cInvalid items! You need a Weapon and a Rune."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // 2. CHECK TARGET (Can this rune go on this item?)
        if (!rune.getTarget().includes(weapon)) {
            player.sendMessage(ColorUtil.parse("&cThis Rune cannot be applied to this item type!"));
            return;
        }

        // 3. CHECK SLOTS
        if (!slotManager.hasEmptySlot(weapon)) {
            player.sendMessage(ColorUtil.parse("&cThis item has no empty Rune Slots!"));
            return;
        }

        // 4. CALCULATE CHANCE
        double chance = plugin.getRuneManager().getChanceFromItem(runeItem);
        if (chance <= 0) chance = rune.getRarity().getDefaultChance();

        // Add Catalyst logic here if needed (check SLOT_CATALYST)
        // For now, simple roll:
        if (random.nextDouble() * 100 <= chance) {
            // SUCCESS
            slotManager.applyRune(weapon, rune); // Use SlotManager to apply!
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
            player.sendMessage(ColorUtil.parse("&aSuccess! Rune applied."));

            inv.setItem(SLOT_RUNE, null); // Remove rune
            updateButton(inv);
        } else {
            // FAIL
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            player.sendMessage(ColorUtil.parse("&cThe Rune shattered..."));
            inv.setItem(SLOT_RUNE, null); // Remove rune
            updateButton(inv);
        }
    }

    private void updateButton(Inventory inv) {
        ItemStack weapon = inv.getItem(SLOT_WEAPON);
        ItemStack runeItem = inv.getItem(SLOT_RUNE);
        ItemStack button = inv.getItem(SLOT_BUTTON);
        ItemMeta meta = button.getItemMeta();

        RuneType rune = plugin.getRuneManager().getRuneFromItem(runeItem);

        if (weapon != null && rune != null) {
            meta.setDisplayName(ColorUtil.parse("&a&lCLICK TO FORGE"));
            meta.setLore(Collections.singletonList(ColorUtil.parse("&7Click to apply " + rune.getDisplayName())));
            button.setType(Material.DAMAGED_ANVIL);
        } else {
            meta.setDisplayName(ColorUtil.parse("&c&lFORGE RUNE"));
            meta.setLore(Collections.singletonList(ColorUtil.parse("&7Waiting for items...")));
            button.setType(Material.ANVIL);
        }
        button.setItemMeta(meta);
    }

    private void returnItem(Player p, Inventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item != null && item.getType() != Material.AIR) p.getInventory().addItem(item);
    }
}