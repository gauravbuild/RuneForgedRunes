package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RuneApplyListener implements Listener {

    private final RuneForgedRunes plugin;
    private final RuneManager runeManager;
    private final SlotManager slotManager;
    private final Random random = new Random();

    public RuneApplyListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || cursor.getType() == Material.AIR || current == null || current.getType() == Material.AIR) {
            return;
        }

        RuneType rune = runeManager.getRuneFromItem(cursor);
        if (rune == null) return;

        if (!rune.getTarget().includes(current)) {
            return;
        }

        slotManager.ensureSlots(current);

        if (!slotManager.hasEmptySlot(current)) {
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("no-slots")));
            return;
        }

        event.setCancelled(true);

        if (cursor.getAmount() > 1) {
            cursor.setAmount(cursor.getAmount() - 1);
        } else {
            event.getView().setCursor(null);
        }

        // Get custom chance from the specific item (not default!)
        double chance = runeManager.getChanceFromItem(cursor);
        // Fallback if missing
        if (chance <= 0) chance = rune.getRarity().getDefaultChance();

        double roll = random.nextDouble() * 100;

        if (roll <= chance) {
            slotManager.applyRune(current, rune);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("rune-applied")
                    .replace("<rune_name>", rune.getDisplayName())));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("rune-failed")));
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        slotManager.ensureSlots(item);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            slotManager.ensureSlots(event.getItem().getItemStack());
        }
    }
}