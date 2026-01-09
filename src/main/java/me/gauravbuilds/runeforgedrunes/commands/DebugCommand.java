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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) {
            player.sendMessage(ChatColor.RED + "Hold an item with Runes on it.");
            return true;
        }

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "applied_runes");

        player.sendMessage(ChatColor.GOLD + "--- Item NBT Debug ---");

        if (pdc.has(key, PersistentDataType.STRING)) {
            String data = pdc.get(key, PersistentDataType.STRING);
            player.sendMessage(ChatColor.GREEN + "Found Key: " + key.toString());
            player.sendMessage(ChatColor.YELLOW + "Value: " + data);
        } else {
            player.sendMessage(ChatColor.RED + "❌ No 'applied_runes' NBT tag found!");
            player.sendMessage(ChatColor.GRAY + "Expected Key: " + key.toString());
        }

        return true;
    }
}