package me.gauravbuilds.runeforgedrunes.gui;

import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;

public class ForgeGui {

    public static final String TITLE = ColorUtil.parse("&8[&4Rune Forge&8]");

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, TITLE);

        // Fillers
        ItemStack black = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack red = createItem(Material.RED_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 45; i++) inv.setItem(i, black);

        int[] redSlots = {0, 8, 36, 44, 10, 16, 28, 34};
        for (int slot : redSlots) inv.setItem(slot, red);

        // Input Slots
        inv.setItem(20, null); // Weapon
        inv.setItem(22, null); // Rune
        inv.setItem(24, null); // Catalyst

        // Icons
        inv.setItem(11, createItem(Material.IRON_SWORD, "&c&lWeapon", "&7Place Equipment Here"));
        inv.setItem(13, createItem(Material.MAGMA_CREAM, "&5&lRune", "&7Place Rune Here"));
        inv.setItem(15, createItem(Material.GLOWSTONE_DUST, "&e&lCatalyst", "&7(Optional) Soul Dust"));

        // Button
        inv.setItem(40, createItem(Material.ANVIL, "&c&lFORGE RUNE", "&7Waiting for items..."));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.parse(name));
        meta.setLore(Arrays.stream(lore).map(ColorUtil::parse).toList());
        item.setItemMeta(meta);
        return item;
    }
}