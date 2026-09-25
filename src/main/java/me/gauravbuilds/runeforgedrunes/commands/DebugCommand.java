//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.commands;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements CommandExecutor {
    private final RuneForgedRunes plugin;

    public DebugCommand(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        } else {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.hasItemMeta()) {
                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(this.plugin, "applied_runes");
                player.sendMessage(String.valueOf(ChatColor.GOLD) + "--- Item NBT Debug ---");
                if (pdc.has(key, PersistentDataType.STRING)) {
                    String data = (String)pdc.get(key, PersistentDataType.STRING);
                    String var10001 = String.valueOf(ChatColor.GREEN);
                    player.sendMessage(var10001 + "Found Key: " + key.toString());
                    var10001 = String.valueOf(ChatColor.YELLOW);
                    player.sendMessage(var10001 + "Value: " + data);
                } else {
                    player.sendMessage(String.valueOf(ChatColor.RED) + "❌ No 'applied_runes' NBT tag found!");
                    String var11 = String.valueOf(ChatColor.GRAY);
                    player.sendMessage(var11 + "Expected Key: " + key.toString());
                }

                return true;
            } else {
                player.sendMessage(String.valueOf(ChatColor.RED) + "Hold an item with Runes on it.");
                return true;
            }
        }
    }
}
