package me.gauravbuilds.runeforgedrunes.utils;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    // Pattern for Hex Colors: &#RRGGBB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String parse(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            // Convert &#RRGGBB to BungeeChatColor (Hex)
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }

        // Convert standard &a, &c codes
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}