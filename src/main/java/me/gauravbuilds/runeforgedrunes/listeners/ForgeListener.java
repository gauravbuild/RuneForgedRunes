//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.listeners;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
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

public class ForgeListener implements Listener {
    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();
    public ForgeListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ForgeGui.getTitle(this.plugin))) {
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                int slot = event.getSlot();
                if (slot != this.slot("weapon", 20) && slot != this.slot("rune", 22) && slot != this.slot("catalyst", 24)) {
                    event.setCancelled(true);
                }

                if (slot == this.slot("button", 40)) {
                    this.handleForge((Player)event.getWhoClicked(), event.getInventory());
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateButton(event.getView().getTopInventory()), 1L);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(ForgeGui.getTitle(this.plugin))) {
            for(int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize() && slot != this.slot("weapon", 20) && slot != this.slot("rune", 22) && slot != this.slot("catalyst", 24)) {
                    event.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateButton(event.getView().getTopInventory()), 1L);
        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ForgeGui.getTitle(this.plugin))) {
            this.returnItem((Player)event.getPlayer(), event.getInventory(), this.slot("weapon", 20));
            this.returnItem((Player)event.getPlayer(), event.getInventory(), this.slot("rune", 22));
            this.returnItem((Player)event.getPlayer(), event.getInventory(), this.slot("catalyst", 24));
        }

    }

    private void handleForge(Player player, Inventory inv) {
        ItemStack weapon = inv.getItem(this.slot("weapon", 20));
        ItemStack runeItem = inv.getItem(this.slot("rune", 22));
        ItemStack catalystItem = inv.getItem(this.slot("catalyst", 24));
        RuneRegistry.Definition rune = this.plugin.getRuneRegistry().get(this.plugin.getRuneManager().getRuneId(runeItem));
        if (weapon != null && rune != null) {
            if (!rune.accepts(weapon)) {
                player.sendMessage(ColorUtil.parse("&cThis Rune cannot be applied to this item type!"));
            } else if (!this.slotManager.hasEmptySlot(weapon)) {
                player.sendMessage(ColorUtil.parse("&cThis item has no empty Rune Slots!"));
            } else if (this.slotManager.getRuneIds(weapon).stream().anyMatch(id -> id.equalsIgnoreCase(rune.id()))) {
                player.sendMessage(ColorUtil.parse("&cThis item already has the " + rune.name() + " Rune!"));
            } else {
                double chance = this.plugin.getRuneManager().getChanceFromItem(runeItem);
                double destroy = this.plugin.getRuneManager().getDestroyFromItem(runeItem);

                boolean usedCatalyst = false;
                if (this.plugin.getCatalystManager().isCatalyst(catalystItem)) {
                    double boost = this.plugin.getCatalystManager().getBoostAmount();
                    double increase = Math.max(0, Math.min(100, chance + boost) - chance);
                    chance += increase;
                    destroy = Math.max(0, destroy - increase);
                    usedCatalyst = true;
                }

                boolean consumeRune = false;
                if (this.random.nextDouble() * 100 < chance) {
                    this.slotManager.applyRune(weapon, rune.id());
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 0.8F);
                    player.sendMessage(ColorUtil.parse("&aSuccess! Rune applied."));
                    consumeRune = true;
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8F, 1.2F);
                    if (this.random.nextDouble() * 100 < destroy) {
                        consumeRune = true;
                        player.sendMessage(ColorUtil.parse("&cThe Rune shattered. Your equipment survived."));
                    } else player.sendMessage(ColorUtil.parse("&eForging failed, but the Rune and equipment survived."));
                }

                if (consumeRune) inv.setItem(this.slot("rune", 22), null);
                if (usedCatalyst) {
                    if (catalystItem.getAmount() > 1) {
                        catalystItem.setAmount(catalystItem.getAmount() - 1);
                    } else {
                        inv.setItem(this.slot("catalyst", 24), (ItemStack)null);
                    }
                }

                this.updateButton(inv);
            }
        } else {
            player.sendMessage(ColorUtil.parse("&cInvalid items! You need a Weapon and a Rune."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
        }
    }

    private void updateButton(Inventory inv) {
        ItemStack weapon = inv.getItem(this.slot("weapon", 20));
        ItemStack runeItem = inv.getItem(this.slot("rune", 22));
        ItemStack catalystItem = inv.getItem(this.slot("catalyst", 24));
        ItemStack button = inv.getItem(this.slot("button", 40));
        ItemMeta meta = button.getItemMeta();
        RuneRegistry.Definition rune = this.plugin.getRuneRegistry().get(this.plugin.getRuneManager().getRuneId(runeItem));
        if (weapon != null && rune != null) {
            double chance = this.plugin.getRuneManager().getChanceFromItem(runeItem);
            double destroy = this.plugin.getRuneManager().getDestroyFromItem(runeItem);

            String extraLore = "";
            if (this.plugin.getCatalystManager().isCatalyst(catalystItem)) {
                double boost = this.plugin.getCatalystManager().getBoostAmount();
                double increase = Math.max(0, Math.min(100, chance + boost) - chance);
                chance += increase;
                destroy = Math.max(0, destroy - increase);
                extraLore = " &d(+" + (int)boost + "% Boosted)";
            }

            meta.setDisplayName(ColorUtil.parse("&a&lCLICK TO FORGE"));
            meta.setLore(Arrays.asList(ColorUtil.parse("&7Applying: &f" + rune.name()), ColorUtil.parse("&7Success Chance: &a" + (int)chance + "%" + extraLore), ColorUtil.parse("&7Rune Destroy Chance: &c" + (int)destroy + "%"), "", ColorUtil.parse("&eClick to Forge!")));
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
        if (item != null && item.getType() != Material.AIR) {
            p.getInventory().addItem(new ItemStack[]{item});
        }

    }

    private int slot(String name, int defaultSlot) {
        return ForgeGui.getSlot(this.plugin, name, defaultSlot);
    }
}
