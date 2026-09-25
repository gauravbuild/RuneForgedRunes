//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String parse(String message) {
        if (message == null) {
            return "";
        } else {
            message = message.replace("<black>", "&0").replace("<dark_blue>", "&1").replace("<dark_green>", "&2")
                    .replace("<dark_aqua>", "&3").replace("<dark_red>", "&4").replace("<dark_purple>", "&5")
                    .replace("<gold>", "&6").replace("<gray>", "&7").replace("<dark_gray>", "&8")
                    .replace("<blue>", "&9").replace("<green>", "&a").replace("<aqua>", "&b")
                    .replace("<red>", "&c").replace("<light_purple>", "&d").replace("<yellow>", "&e")
                    .replace("<white>", "&f");
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuilder buffer = new StringBuilder();

            while(matcher.find()) {
                matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
            }

            return org.bukkit.ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
        }
    }
}
