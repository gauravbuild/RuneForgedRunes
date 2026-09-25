package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class InventoryDragDropListener implements Listener {
    private final RuneForgedRunes plugin;
    public InventoryDragDropListener(RuneForgedRunes plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true, priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || event.getView().getTopInventory().getType() != InventoryType.CRAFTING || event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER) return;
        InventoryAction action = event.getAction();
        if (action != InventoryAction.PLACE_ALL && action != InventoryAction.PLACE_ONE && action != InventoryAction.PLACE_SOME && action != InventoryAction.SWAP_WITH_CURSOR) return;
        ItemStack cursor = event.getCursor();
        ItemStack target = event.getCurrentItem();
        if (cursor == null || cursor.getType() == Material.AIR || target == null || target.getType() == Material.AIR) return;
        if (plugin.getCatalystManager().isCatalyst(cursor) && plugin.getRuneRegistry().get(plugin.getRuneManager().getRuneId(target)) != null) {
            event.setCancelled(true);
            double next = Math.min(100, plugin.getRuneManager().getChanceFromItem(target) + plugin.getCatalystManager().getBoostAmount());
            plugin.getRuneManager().updateChance(target, next);
            event.setCurrentItem(target);
            consume(player, cursor);
            player.sendMessage(ColorUtil.parse("&dRune empowered! &a" + (int) next + "% success &7/ &c" + (int) plugin.getRuneManager().getDestroyFromItem(target) + "% destroy"));
            return;
        }
        RuneRegistry.Definition rune = plugin.getRuneRegistry().get(plugin.getRuneManager().getRuneId(cursor));
        if (rune == null) return;
        event.setCancelled(true);
        if (!player.hasPermission("runeforgedrunes.apply.dragdrop")) {
            player.sendMessage(ColorUtil.parse("&cYou cannot apply runes this way. Use /forge."));
            return;
        }
        if (!rune.accepts(target) || !plugin.getSlotManager().hasEmptySlot(target) || plugin.getSlotManager().getRuneIds(target).stream().anyMatch(id -> id.equalsIgnoreCase(rune.id()))) {
            player.sendMessage(ColorUtil.parse("&cWrong equipment, full sockets, or rune already applied."));
            return;
        }
        double chance = plugin.getRuneManager().getChanceFromItem(cursor);
        if (ThreadLocalRandom.current().nextDouble(100) < chance) {
            plugin.getSlotManager().applyRune(target, rune.id());
            event.setCurrentItem(target);
            player.sendMessage(ColorUtil.parse("&a✦ " + rune.name() + " forged into your equipment!"));
            consume(player, cursor);
        } else if (ThreadLocalRandom.current().nextDouble(100) < plugin.getRuneManager().getDestroyFromItem(cursor)) {
            player.sendMessage(ColorUtil.parse("&cThe rune shattered. Your equipment survived."));
            consume(player, cursor);
        } else player.sendMessage(ColorUtil.parse("&eForging failed, but the rune and equipment survived."));
    }

    private void consume(Player player, ItemStack cursor) {
        if (cursor.getAmount() <= 1) player.setItemOnCursor(null);
        else {
            ItemStack remaining = cursor.clone();
            remaining.setAmount(cursor.getAmount() - 1);
            player.setItemOnCursor(remaining);
        }
    }
}