//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.gui;

import java.util.Arrays;
import com.cryptomorin.xseries.XMaterial;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ForgeGui {
    private final RuneForgedRunes plugin;

    public ForgeGui(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    public static String getTitle(RuneForgedRunes plugin) {
        return ColorUtil.parse(plugin.getConfigManager().getString("gui.forge.title", "&8[&4Rune Forge&8]"));
    }

    public static int getSlot(RuneForgedRunes plugin, String name, int defaultSlot) {
        return plugin.getConfig().getInt("gui.forge.slots." + name, defaultSlot);
    }

    public void open(Player player) {
        int configured = plugin.getConfig().getInt("gui.forge.size", 45);
        int size = configured == 54 ? 54 : 45;
        Inventory inv = Bukkit.createInventory((InventoryHolder)null, size, getTitle(this.plugin));
        ItemStack black = this.createItem(this.getMaterial("gui.forge.filler-material", Material.BLACK_STAINED_GLASS_PANE), " ");
        ItemStack red = this.createItem(this.getMaterial("gui.forge.accent-material", Material.RED_STAINED_GLASS_PANE), " ");

        for(int i = 0; i < size; ++i) {
            inv.setItem(i, black);
        }

        int[] redSlots = new int[]{0, 8, 36, 44, 10, 16, 28, 34};

        for(int slot : redSlots) {
            inv.setItem(slot, red);
        }

        inv.setItem(getSlot(this.plugin, "weapon", 20), (ItemStack)null);
        inv.setItem(getSlot(this.plugin, "rune", 22), (ItemStack)null);
        inv.setItem(getSlot(this.plugin, "catalyst", 24), (ItemStack)null);
        inv.setItem(11, this.createItem(Material.IRON_SWORD, "&c&lWeapon", "&7Place Equipment Here"));
        inv.setItem(13, this.createItem(Material.MAGMA_CREAM, "&5&lRune", "&7Place Rune Here"));
        inv.setItem(15, this.createItem(Material.GLOWSTONE_DUST, "&e&lCatalyst", "&7(Optional) Soul Dust"));
        inv.setItem(getSlot(this.plugin, "button", 40), this.createItem(Material.ANVIL, "&c&lFORGE RUNE", "&7Waiting for items..."));
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

    private Material getMaterial(String path, Material fallback) {
        return XMaterial.matchXMaterial(this.plugin.getConfigManager().getString(path, fallback.name()))
                .map(XMaterial::parseMaterial).orElse(fallback);
    }
}
