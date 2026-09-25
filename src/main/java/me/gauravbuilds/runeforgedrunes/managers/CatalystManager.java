//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.managers;

import java.util.ArrayList;
import java.util.List;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import com.cryptomorin.xseries.XMaterial;

public class CatalystManager {
    private final RuneForgedRunes plugin;
    private final NamespacedKey catalystKey;
    private YamlConfiguration config;

    public CatalystManager(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.catalystKey = new NamespacedKey(plugin, "is_catalyst");
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "catalysts.yml");
        if (!file.exists()) plugin.saveResource("catalysts.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public ItemStack getCatalystItem(int amount) {
        Material material = XMaterial.matchXMaterial(config.getString("catalysts.stardust.base-item", "NETHER_STAR")).map(XMaterial::parseMaterial).orElse(Material.NETHER_STAR);
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.parse(config.getString("catalysts.stardust.display-name", "&d&l✧ Stardust ✧")));
        List<String> lore = new ArrayList();
        for (String line : config.getStringList("catalysts.stardust.lore")) lore.add(ColorUtil.parse(line.replace("{bonus}", String.valueOf((int) getBoostAmount()))));
        meta.setLore(lore);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        meta.getPersistentDataContainer().set(this.catalystKey, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isCatalyst(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            return !item.hasItemMeta() ? false : item.getItemMeta().getPersistentDataContainer().has(this.catalystKey, PersistentDataType.BYTE);
        } else {
            return false;
        }
    }

    public double getBoostAmount() {
        return config.getDouble("catalysts.stardust.success-bonus", 15);
    }
}
