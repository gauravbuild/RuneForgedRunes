//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum RuneRarity {
    COMMON("Common", NamedTextColor.GRAY, (double)60.0F),
    RARE("Rare", NamedTextColor.BLUE, (double)40.0F),
    LEGENDARY("Legendary", NamedTextColor.GOLD, (double)20.0F),
    MYTHIC("Mythic", NamedTextColor.LIGHT_PURPLE, (double)5.0F),
    CUSTOM("Custom", NamedTextColor.AQUA, (double)50.0F);

    private final String displayName;
    private final TextColor color;
    private final double defaultChance;

    private RuneRarity(String displayName, TextColor color, double defaultChance) {
        this.displayName = displayName;
        this.color = color;
        this.defaultChance = defaultChance;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public TextColor getColor() {
        return this.color;
    }

    public double getDefaultChance() {
        return this.defaultChance;
    }
}
