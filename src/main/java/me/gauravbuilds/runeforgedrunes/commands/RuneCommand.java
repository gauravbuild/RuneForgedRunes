package me.gauravbuilds.runeforgedrunes.commands;

import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.gui.RuneGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RuneCommand implements CommandExecutor, TabCompleter {

    private final RuneForgedRunes plugin;

    public RuneCommand(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // --- 1. OPEN GUI (For Everyone) ---
        if (args.length == 0) {
            if (sender instanceof Player) {
                new RuneGUI(plugin).openMainMenu((Player) sender);
                return true;
            } else {
                sender.sendMessage(plugin.getMessage("only-players"));
                return true;
            }
        }

        // --- ADMIN CHECK FOR SUBCOMMANDS ---
        if (!sender.hasPermission("runeforged.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use admin subcommands.", NamedTextColor.RED));
            return true;
        }

        // --- 2. RELOAD ---
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(plugin.getMessage("config-reloaded"));
            return true;
        }

        // --- 3. LIST (Category Wise) ---
        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(Component.text("=== Rune Repository ===", NamedTextColor.GOLD));
            for (RuneCategory cat : RuneCategory.values()) {
                sender.sendMessage(Component.text("Category: " + cat.name(), NamedTextColor.AQUA));
                String runes = Arrays.stream(RuneType.values())
                        .filter(r -> r.getCategory() == cat)
                        .map(RuneType::name)
                        .collect(Collectors.joining(", "));
                sender.sendMessage(Component.text(" " + runes, NamedTextColor.YELLOW));
            }
            return true;
        }

        // --- 4. GIVE COMMAND ---
        // Usage: /runes give <player> <category> <rune> <chance> <amount>
        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 6) {
                sender.sendMessage(Component.text("Usage: /runes give <player> <category> <rune> <chance> <amount>", NamedTextColor.RED));
                return true;
            }

            // 1. Target
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("invalid-player"));
                return true;
            }

            // 2. Category (Just for validation/sanity check)
            String catName = args[2].toUpperCase();
            try {
                RuneCategory.valueOf(catName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid category. Use Tab Completion.", NamedTextColor.RED));
                return true;
            }

            // 3. Rune Name
            String runeName = args[3].toUpperCase();
            RuneType type;
            try {
                type = RuneType.valueOf(runeName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid rune name.", NamedTextColor.RED));
                return true;
            }

            // Verify Rune belongs to Category (Optional strictness)
            if (!type.getCategory().name().equals(catName)) {
                sender.sendMessage(Component.text("Warning: " + runeName + " is not in " + catName + ", but giving anyway...", NamedTextColor.YELLOW));
            }

            // 4. Chance
            double chance;
            try {
                chance = Double.parseDouble(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid chance number.", NamedTextColor.RED));
                return true;
            }

            // 5. Amount
            int amount;
            try {
                amount = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount number.", NamedTextColor.RED));
                return true;
            }

            // Give Item
            ItemStack rune = plugin.getRuneManager().createRune(type, chance);
            rune.setAmount(amount);
            target.getInventory().addItem(rune);

            String msg = plugin.getMessage("rune-received").replace("<rune_name>", type.getDisplayName());
            target.sendMessage(msg);
            sender.sendMessage(Component.text("Gave " + amount + "x " + type.getDisplayName() + " (" + chance + "%) to " + target.getName(), NamedTextColor.GREEN));
            return true;
        }

        return true;
    }

    // --- TAB COMPLETER ---
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("runeforged.admin")) return Collections.emptyList();

        // Arg 0: Subcommands
        if (args.length == 1) {
            return Arrays.asList("give", "list", "reload");
        }

        if (args[0].equalsIgnoreCase("give")) {
            // Arg 1: Players
            if (args.length == 2) {
                return null; // Return null to let Bukkit handle player names
            }
            // Arg 2: Category
            if (args.length == 3) {
                return Arrays.stream(RuneCategory.values()).map(Enum::name).collect(Collectors.toList());
            }
            // Arg 3: Rune Name (Filtered by Category if valid)
            if (args.length == 4) {
                String catInput = args[2].toUpperCase();
                try {
                    RuneCategory cat = RuneCategory.valueOf(catInput);
                    return Arrays.stream(RuneType.values())
                            .filter(r -> r.getCategory() == cat)
                            .map(Enum::name)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    // If category is invalid, list all runes
                    return Arrays.stream(RuneType.values()).map(Enum::name).collect(Collectors.toList());
                }
            }
            // Arg 4: Chance suggestions
            if (args.length == 5) {
                return Arrays.asList("10", "25", "50", "75", "100");
            }
            // Arg 5: Amount suggestions
            if (args.length == 6) {
                return Arrays.asList("1", "16", "32", "64");
            }
        }
        return Collections.emptyList();
    }
}