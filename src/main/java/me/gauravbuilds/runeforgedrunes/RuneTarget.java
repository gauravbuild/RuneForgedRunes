package me.gauravbuilds.runeforgedrunes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum RuneTarget {
    SWORD("Sword", Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),
    AXE("Axe", Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE),
    PICKAXE("Pickaxe", Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
    SHOVEL("Shovel", Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL),
    HOE("Hoe", Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE),
    BOW("Bow/Crossbow", Material.BOW, Material.CROSSBOW),
    HELMET("Helmet", Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET),
    CHESTPLATE("Chestplate", Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.ELYTRA),
    LEGGINGS("Leggings", Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
    BOOTS("Boots", Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),
    
    // Composite types
    MELEE_WEAPON("Melee Weapon"), // Special handling
    ARMOR("Armor"), // Special handling
    TOOL("Tool"), // Special handling
    ALL("Any Item"); // Special handling

    private final String displayName;
    private final List<Material> materials;

    RuneTarget(String displayName, Material... materials) {
        this.displayName = displayName;
        this.materials = Arrays.asList(materials);
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean includes(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();

        if (this == ALL) return true;
        
        if (this == MELEE_WEAPON) {
            return SWORD.includes(item) || AXE.includes(item);
        }
        if (this == ARMOR) {
            return HELMET.includes(item) || CHESTPLATE.includes(item) || LEGGINGS.includes(item) || BOOTS.includes(item);
        }
        if (this == TOOL) {
            return PICKAXE.includes(item) || AXE.includes(item) || SHOVEL.includes(item) || HOE.includes(item);
        }

        return materials.contains(type);
    }
}
