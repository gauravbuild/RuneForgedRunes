package me.gauravbuilds.runeforgedrunes.managers;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class OrbManager {

    private final RuneForgedRunes plugin;
    private final NamespacedKey orbKey;

    public OrbManager(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.orbKey = new NamespacedKey(plugin, "rune_orb_rarity");
    }

    public ItemStack createOrb(RuneRarity rarity) {
        Material mat;
        String name;
        String desc1;
        String desc2;
        String rate;

        switch (rarity) {
            case COMMON -> {
                mat = Material.CLAY_BALL;
                name = "&f\u25C6 Common Rune Orb";
                desc1 = "&7A dense, stone-like sphere containing";
                desc2 = "&7a dormant fragment of early sorcery";
                rate = "&a50% - 90%";
            }
            case RARE -> {
                mat = Material.LAPIS_LAZULI;
                name = "&b\u25C6 Rare Rune Orb";
                desc1 = "&7An ocean-colored crystal orb pulsing";
                desc2 = "&7with standard enchantments!";
                rate = "&a40% - 75%";
            }
            case LEGENDARY -> {
                mat = Material.GOLD_NUGGET;
                name = "&6\u25C6 Legendary Rune Orb";
                desc1 = "&7A heavy, metallic cluster containing";
                desc2 = "&7sealed mythical capabilities.";
                rate = "&a25% - 60%";
            }
            case MYTHIC -> {
                mat = Material.REDSTONE;
                name = "&d\u25C6 Mythic Rune Orb";
                desc1 = "&7A blind remnant of ancient stardust.";
                desc2 = "&7Extremely volatile and rare!";
                rate = "&a15% - 45%";
            }
            case CUSTOM -> {
                mat = XMaterial.matchXMaterial("NETHER_STAR").map(XMaterial::parseMaterial).orElse(Material.CLAY_BALL);
                name = "&b\u25C6 Custom Rune Orb";
                desc1 = "&7Contains a rune from the Custom category.";
                desc2 = "&7Made by your server's rune smiths.";
                rate = "&a50% - 90%";
            }
            default -> {
                mat = Material.CLAY_BALL;
                name = "Rune Orb";
                desc1 = ""; desc2 = ""; rate = "";
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ColorUtil.parse(name));
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtil.parse("&7&m---------------------------------"));
        lore.add(ColorUtil.parse(desc1));
        lore.add(ColorUtil.parse(desc2));
        lore.add("");
        lore.add(ColorUtil.parse("&6\u26A1UNBOX DETAILS:"));
        lore.add(ColorUtil.parse("&7>> Guaranteed Tier: " + (rarity == RuneRarity.CUSTOM ? "&b" : "&d") + rarity.getDisplayName() + " Rune"));
        lore.add(ColorUtil.parse("&7>> Potential Success Rate: " + rate));
        lore.add("");
        lore.add(ColorUtil.parse("&e&lRight click to shatter and reveal your fate!"));
        lore.add(ColorUtil.parse("&7&m---------------------------------"));
        meta.setLore(lore);

        // Add glowing effect
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Tag it as an orb
        meta.getPersistentDataContainer().set(orbKey, PersistentDataType.STRING, rarity.name());
        
        item.setItemMeta(meta);
        return item;
    }

    public RuneRarity getOrbRarity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String rarityStr = item.getItemMeta().getPersistentDataContainer().get(orbKey, PersistentDataType.STRING);
        if (rarityStr == null) return null;
        try {
            return RuneRarity.valueOf(rarityStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}