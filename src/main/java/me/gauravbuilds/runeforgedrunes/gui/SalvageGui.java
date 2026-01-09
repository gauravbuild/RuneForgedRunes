package me.gauravbuilds.runeforgedrunes.gui;

import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Collections;

public class SalvageGui {
    public static final String TITLE = ColorUtil.parse("&8[&cRune Salvage&8]");

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        // Fillers
        ItemStack black = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = black.getItemMeta();
        meta.setDisplayName(" ");
        black.setItemMeta(meta);

        for (int i = 0; i < 45; i++) inv.setItem(i, black);

        // Input Slot 13
        inv.setItem(13, null);

        // Info
        ItemStack info = new ItemStack(Material.ANVIL);
        meta = info.getItemMeta();
        meta.setDisplayName(ColorUtil.parse("&cPlace Item Below"));
        meta.setLore(Collections.singletonList(ColorUtil.parse("&7To see applied Runes.")));
        info.setItemMeta(meta);
        inv.setItem(4, info);

        player.openInventory(inv);
    }
}