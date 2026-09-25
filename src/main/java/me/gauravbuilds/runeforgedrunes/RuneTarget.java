//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum RuneTarget {
    SWORD("Sword", new Material[]{Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD}),
    AXE("Axe", new Material[]{Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE}),
    PICKAXE("Pickaxe", new Material[]{Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE}),
    SHOVEL("Shovel", new Material[]{Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL}),
    HOE("Hoe", new Material[]{Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE}),
    BOW("Bow/Crossbow", new Material[]{Material.BOW, Material.CROSSBOW}),
    FISHING_ROD("Fishing Rod", new Material[]{Material.FISHING_ROD}),
    HELMET("Helmet", new Material[]{Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET}),
    CHESTPLATE("Chestplate", new Material[]{Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.ELYTRA}),
    LEGGINGS("Leggings", new Material[]{Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS}),
    BOOTS("Boots", new Material[]{Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS}),
    MELEE_WEAPON("Melee Weapon", new Material[0]),
    AXE_OR_HOE("Axe/Hoe", new Material[0]),
    ARMOR("Armor", new Material[0]),
    TOOL("Tool", new Material[0]),
    ALL("Standard Gear", new Material[0]);

    private final String displayName;
    private final List<Material> materials;

    private RuneTarget(String displayName, Material... materials) {
        this.displayName = displayName;
        this.materials = Arrays.asList(materials);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean includes(ItemStack item) {
        if (item == null) {
            return false;
        } else {
            Material type = item.getType();
            if (this == ALL) {
                return SWORD.includes(item) || AXE.includes(item) || PICKAXE.includes(item) || SHOVEL.includes(item) || HOE.includes(item) || BOW.includes(item) || FISHING_ROD.includes(item) || ARMOR.includes(item);
            } else if (this == MELEE_WEAPON) {
                return SWORD.includes(item) || AXE.includes(item);
            } else if (this == AXE_OR_HOE) {
                return AXE.includes(item) || HOE.includes(item);
            } else if (this == ARMOR) {
                return HELMET.includes(item) || CHESTPLATE.includes(item) || LEGGINGS.includes(item) || BOOTS.includes(item);
            } else if (this != TOOL) {
                return this.materials.contains(type);
            } else {
                return PICKAXE.includes(item) || AXE.includes(item) || SHOVEL.includes(item) || HOE.includes(item);
            }
        }
    }
}
