//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.listeners;

import java.util.ArrayList;
import java.util.List;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.gui.SalvageGui;
import com.cryptomorin.xseries.XMaterial;
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

public class SalvageListener implements Listener {
    private final RuneForgedRunes plugin;
    private final int XP_COST = 5;

    public SalvageListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(SalvageGui.getTitle(this.plugin))) {
            Inventory inv = event.getInventory();
            Player player = (Player)event.getWhoClicked();
            if (event.getClickedInventory() == inv) {
                if (event.getSlot() == this.inputSlot()) {
                    Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateGui(inv, player), 1L);
                } else {
                    event.setCancelled(true);
                    ItemStack clicked = event.getCurrentItem();
                    if (event.getSlot() >= 27 && event.getSlot() < 27 + this.plugin.getSlotManager().getRuneIds(inv.getItem(this.inputSlot())).size()) this.handleSalvage(player, inv, event.getSlot() - 27);

                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(SalvageGui.getTitle(this.plugin))) {
            if (event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize() && slot != inputSlot())) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateGui(event.getInventory(), (Player)event.getWhoClicked()), 1L);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(SalvageGui.getTitle(this.plugin))) {
            ItemStack item = event.getInventory().getItem(this.inputSlot());
            if (item != null && item.getType() != Material.AIR) {
                event.getPlayer().getInventory().addItem(new ItemStack[]{item});
            }
        }

    }

    private void updateGui(Inventory inv, Player player) {
        ItemStack item = inv.getItem(this.inputSlot());

        Material filler = XMaterial.matchXMaterial(plugin.getConfig().getString("gui.salvage.filler-material", "BLACK_STAINED_GLASS_PANE"))
                .map(XMaterial::parseMaterial).orElse(Material.BLACK_STAINED_GLASS_PANE);
        for(int i = 27; i < 45; ++i) {
            inv.setItem(i, new ItemStack(filler));
        }

        if (item != null && item.getType() != Material.AIR) {
            List<String> runes = this.plugin.getSlotManager().getRuneIds(item);
            if (!runes.isEmpty()) {
                int slot = 27;

                for(String rune : runes) {
                    ItemStack btn = this.plugin.getRuneManager().createRune(rune);
                    if (btn == null) { ++slot; continue; }
                    ItemMeta meta = btn.getItemMeta();
                    List<String> lore = (List<String>)(meta.hasLore() ? meta.getLore() : new ArrayList());
                    lore.add("");
                    lore.add(ColorUtil.parse("&c&l[REMOVE RUNE]"));
                    lore.add(ColorUtil.parse("&7Cost: &b5 XP Levels"));
                    lore.add(ColorUtil.parse("&eClick to extract safely."));
                    meta.setLore(lore);
                    btn.setItemMeta(meta);
                    inv.setItem(slot, btn);
                    ++slot;
                }

            }
        }
    }

    private void handleSalvage(Player player, Inventory inv, int index) {
        ItemStack item = inv.getItem(this.inputSlot());
        if (item != null) {
            List<String> runes = this.plugin.getSlotManager().getRuneIds(item);
            if (index >= 0 && index < runes.size() && this.plugin.getRuneRegistry().get(runes.get(index)) != null) {
                if (player.getLevel() < 5) {
                    player.sendMessage(ColorUtil.parse("&cYou need 5 XP Levels to remove this!"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    return;
                }

                player.setLevel(player.getLevel() - 5);
                String target = runes.remove(index);
                this.plugin.getSlotManager().updateRuneIds(item, runes);
                ItemStack refund = this.plugin.getRuneManager().createRune(target);
                player.getInventory().addItem(refund).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));

                player.sendMessage(ColorUtil.parse("&aRune extracted successfully!"));
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.3F);
                this.updateGui(inv, player);
            } else {
                player.sendMessage(ColorUtil.parse("&cError identifying rune. Try taking the item out and putting it back."));
            }

        }
    }

    private int inputSlot() {
        return SalvageGui.getInputSlot(this.plugin);
    }
}
