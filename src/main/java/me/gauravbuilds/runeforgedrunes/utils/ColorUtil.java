package me.gauravbuilds.runeforgedrunes.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component parse(String text) {
        return mm.deserialize(text);
    }
    
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
