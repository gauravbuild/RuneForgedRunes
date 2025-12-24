package me.gauravbuilds.runeforgedrunes.commands;

import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RuneCommand implements CommandExecutor, TabCompleter {

    private final RuneForgedRunes plugin;

    public RuneCommand(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("runeforged.admin")) {
            sender.sendMessage(ColorUtil.parse("<red>No permission."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("usage")));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            plugin.getConfigManager().reload();
            sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("config-reloaded")));
            return true;
        }

        if (sub.equals("list")) {
            sender.sendMessage(ColorUtil.parse("<gold>--- Rune List ---"));
            for (RuneType type : RuneType.values()) {
                sender.sendMessage(ColorUtil.parse("<gray>- " + type.getCategory().name() + ": " + type.name()));
            }
            return true;
        }

        if (sub.equals("give")) {
            // Usage: /rune give <player> <category> <rune> <chance> <amount>
            if (args.length < 5) {
                sender.sendMessage(ColorUtil.parse("<red>Usage: /rune give <player> <category> <rune> <chance> [amount]"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("invalid-player")));
                return true;
            }

            // Category check (Arg 2) - Just for validation, we find rune in next arg
            String categoryStr = args[2].toUpperCase();
            try {
                RuneCategory.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ColorUtil.parse("<red>Invalid category."));
                return true;
            }

            // Rune check (Arg 3)
            RuneType type;
            try {
                type = RuneType.valueOf(args[3].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("invalid-rune")));
                return true;
            }

            // Validate category matches rune
            if (type.getCategory() != RuneCategory.valueOf(categoryStr)) {
                sender.sendMessage(ColorUtil.parse("<red>That rune does not belong to the " + categoryStr + " category!"));
                return true;
            }

            // Chance (Arg 4)
            double chance;
            try {
                chance = Double.parseDouble(args[4]);
                if (chance < 0 || chance > 100) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.parse("<red>Chance must be a number between 0 and 100."));
                return true;
            }

            // Amount (Arg 5) - Optional, default 1
            int amount = 1;
            if (args.length > 5) {
                try {
                    amount = Integer.parseInt(args[5]);
                } catch (NumberFormatException ignored) {}
            }

            ItemStack item = plugin.getRuneManager().createRune(type, chance);
            item.setAmount(amount);
            target.getInventory().addItem(item);
            sender.sendMessage(ColorUtil.parse("<green>Given " + amount + " " + type.getDisplayName() + " Rune (" + chance + "%) to " + target.getName()));
            return true;
        }

        sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("usage")));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "list", "reload");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null; // Player list
        }
        // Arg 2: Category
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(RuneCategory.values()).map(Enum::name).collect(Collectors.toList());
        }
        // Arg 3: Rune (Filtered by Category)
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            try {
                RuneCategory cat = RuneCategory.valueOf(args[2].toUpperCase());
                return Arrays.stream(RuneType.values())
                        .filter(r -> r.getCategory() == cat)
                        .map(Enum::name)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
        }
        // Arg 4: Chance
        if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("10", "25", "50", "75", "100");
        }
        // Arg 5: Amount
        if (args.length == 6 && args[0].equalsIgnoreCase("give")) {
            return Arrays.asList("1", "64");
        }
        return new ArrayList<>();
    }
}