package me.gauravbuilds.runeforgedrunes;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public enum RuneRarity {
    COMMON("Common", NamedTextColor.GRAY, 60.0),
    RARE("Rare", NamedTextColor.BLUE, 40.0),
    LEGENDARY("Legendary", NamedTextColor.GOLD, 20.0),
    MYTHIC("Mythic", NamedTextColor.LIGHT_PURPLE, 5.0);

    private final String displayName;
    private final TextColor color;
    private final double defaultChance;

    RuneRarity(String displayName, TextColor color, double defaultChance) {
        this.displayName = displayName;
        this.color = color;
        this.defaultChance = defaultChance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TextColor getColor() {
        return color;
    }
    
    public double getDefaultChance() {
        return defaultChance;
    }
}
