//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.gui;

import java.util.Collections;
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

public class SalvageGui {
    private final RuneForgedRunes plugin;

    public SalvageGui(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    public static String getTitle(RuneForgedRunes plugin) {
        return ColorUtil.parse(plugin.getConfigManager().getString("gui.salvage.title", "&8[&cRune Salvage&8]"));
    }

    public static int getInputSlot(RuneForgedRunes plugin) {
        return plugin.getConfig().getInt("gui.salvage.input-slot", 13);
    }

    public void open(Player player) {
        int configured = plugin.getConfig().getInt("gui.salvage.size", 45);
        int size = configured == 54 ? 54 : 45;
        Inventory inv = Bukkit.createInventory((InventoryHolder)null, size, getTitle(this.plugin));
        Material fillerMaterial = XMaterial.matchXMaterial(this.plugin.getConfigManager().getString("gui.salvage.filler-material", "BLACK_STAINED_GLASS_PANE"))
                .map(XMaterial::parseMaterial).orElse(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack black = new ItemStack(fillerMaterial);
        ItemMeta meta = black.getItemMeta();
        meta.setDisplayName(" ");
        black.setItemMeta(meta);

        for(int i = 0; i < size; ++i) {
            inv.setItem(i, black);
        }

        inv.setItem(getInputSlot(this.plugin), (ItemStack)null);
        ItemStack info = new ItemStack(Material.ANVIL);
        meta = info.getItemMeta();
        meta.setDisplayName(ColorUtil.parse("&cPlace Item Below"));
        meta.setLore(Collections.singletonList(ColorUtil.parse("&7To see applied Runes.")));
        info.setItemMeta(meta);
        inv.setItem(4, info);
        player.openInventory(inv);
    }
}
