package me.gauravbuilds.runeforgedrunes.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.gui.RuneGUI;
import me.gauravbuilds.runeforgedrunes.gui.RuneShopGUI;
import me.gauravbuilds.runeforgedrunes.gui.RuneEditor;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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

public class RuneCommand implements CommandExecutor, TabCompleter {
    private final RuneForgedRunes plugin;

    public RuneCommand(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("runeforged.admin")) new RuneGUI(plugin).openMainMenu(player);
                else plugin.getRuneShopGUI().open(player, 0);
                return true;
            } else {
                sender.sendMessage(this.plugin.getMessage("only-players"));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("open")) {
            if (!sender.hasPermission("runeforged.admin")) { sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED)); return true; }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(this.plugin.getMessage("only-players"));
                return true;
            }
            if (args.length != 2) {
                player.sendMessage(Component.text("Usage: /runes open <category>", NamedTextColor.RED));
                return true;
            }
            try {
                RuneCategory category = RuneCategory.valueOf(args[1].toUpperCase(Locale.ROOT));
                if (!plugin.getConfig().getBoolean("gui.repository.categories." + category.name().toLowerCase(Locale.ROOT) + ".enabled", true)) {
                    player.sendMessage(Component.text("This category is disabled.", NamedTextColor.RED));
                } else {
                    new RuneGUI(plugin).openCategoryMenu(player, category);
                }
            } catch (IllegalArgumentException ex) {
                player.sendMessage(Component.text("Unknown rune category.", NamedTextColor.RED));
            }
            return true;
        } else if (!sender.hasPermission("runeforged.admin")) {
            sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadConfig();
            this.plugin.loadLocale();
            this.plugin.getConfigManager().reload();
            this.plugin.getRuneRegistry().reload();
            this.plugin.getCatalystManager().reload();
            sender.sendMessage(this.plugin.getMessage("config-reloaded"));
            return true;
        } else if (args[0].equalsIgnoreCase("editor")) {
            if (sender instanceof Player player) plugin.getRuneEditor().open(player, 0);
            else sender.sendMessage(this.plugin.getMessage("only-players"));
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            if (sender instanceof Player player) {
                new RuneGUI(plugin).openMainMenu(player);
                return true;
            }
            sender.sendMessage(Component.text("=== Rune Repository ===", NamedTextColor.GOLD));
            for(RuneCategory cat : RuneCategory.values()) {
                sender.sendMessage(Component.text("Category: " + cat.name(), NamedTextColor.AQUA));
                String runes = this.plugin.getRuneRegistry().all().stream().filter(r -> r.category().equalsIgnoreCase(cat.name())).map(RuneRegistry.Definition::id).collect(Collectors.joining(", "));
                sender.sendMessage(Component.text(" " + runes, NamedTextColor.YELLOW));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("giveorb")) {
            // /rune giveorb <player> <rarity> <amount>
            if (args.length < 4) {
                sender.sendMessage(Component.text("Usage: /rune giveorb <player> <rarity> <amount>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(this.plugin.getMessage("invalid-player"));
                return true;
            }
            RuneRarity rarity;
            try {
                rarity = RuneRarity.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid Rarity. Use COMMON, RARE, LEGENDARY, MYTHIC, or CUSTOM.", NamedTextColor.RED));
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
                return true;
            }

            ItemStack orb = this.plugin.getOrbManager().createOrb(rarity);
            for (int i = 0; i < amount; i++) {
                target.getInventory().addItem(orb);
            }
            target.sendMessage(Component.text("You received " + amount + "x " + rarity.getDisplayName() + " Rune Orbs!", rarity.getColor()));
            sender.sendMessage(Component.text("Gave Orbs to " + target.getName(), NamedTextColor.GREEN));
            return true;

        } else if (args[0].equalsIgnoreCase("givecatalyst")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Usage: /rune givecatalyst <player> <amount>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(this.plugin.getMessage("invalid-player"));
                return true;
            }
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException var14) {
                sender.sendMessage(Component.text("Invalid amount.", NamedTextColor.RED));
                return true;
            }
            ItemStack catalyst = this.plugin.getCatalystManager().getCatalystItem(amount);
            target.getInventory().addItem(catalyst);
            target.sendMessage(Component.text("You received " + amount + "x Stardust!", NamedTextColor.LIGHT_PURPLE));
            sender.sendMessage(Component.text("Gave Stardust to " + target.getName(), NamedTextColor.GREEN));
            return true;

        } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 6) {
                sender.sendMessage(Component.text("Usage: /rune give <player> <category> <rune> <chance> <amount>", NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(this.plugin.getMessage("invalid-player"));
                return true;
            }
            String catName = args[2].toUpperCase();
            String runeName = args[3].toUpperCase();
            RuneRegistry.Definition type = this.plugin.getRuneRegistry().get(runeName);
            if (type == null || !type.category().equalsIgnoreCase(catName)) {
                sender.sendMessage(Component.text("Invalid Category or Rune Name.", NamedTextColor.RED));
                return true;
            }
            int amount;
            double chance;
            try {
                chance = Double.parseDouble(args[4]);
                amount = Integer.parseInt(args[5]);
            } catch (NumberFormatException var15) {
                sender.sendMessage(Component.text("Invalid numbers.", NamedTextColor.RED));
                return true;
            }
            if (amount < 1 || amount > 2304) { sender.sendMessage(Component.text("Amount must be between 1 and 2304.", NamedTextColor.RED)); return true; }
            for (int i = 0; i < amount; i++) {
                ItemStack rune = this.plugin.getRuneManager().createRune(type.id(), chance);
                target.getInventory().addItem(rune).values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
            }
            Component msg = this.plugin.getMessage("rune-received").replaceText((TextReplacementConfig)TextReplacementConfig.builder().matchLiteral("<rune_name>").replacement(type.name()).build());
            target.sendMessage(msg);
            sender.sendMessage(Component.text("Gave " + type.name(), NamedTextColor.GREEN));
            return true;
        }
        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return sender.hasPermission("runeforged.admin") ? Arrays.asList("open", "give", "giveorb", "list", "reload", "givecatalyst", "editor") : Collections.emptyList();
        }
        if (args[0].equalsIgnoreCase("open") && args.length == 2) {
            if (!sender.hasPermission("runeforged.admin")) return Collections.emptyList();
            return Arrays.stream(RuneCategory.values()).filter(category -> plugin.getConfig().getBoolean("gui.repository.categories." + category.name().toLowerCase(Locale.ROOT) + ".enabled", true)).map(category -> category.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
        }
        if (!sender.hasPermission("runeforged.admin")) return Collections.emptyList();
        if (args[0].equalsIgnoreCase("giveorb")) {
            if (args.length == 2) return null; // Player names
            if (args.length == 3) return Arrays.stream(RuneRarity.values()).map(Enum::name).collect(Collectors.toList());
            if (args.length == 4) return Arrays.asList("1", "5", "10", "64");
        } else if (args[0].equalsIgnoreCase("givecatalyst")) {
            if (args.length == 2) return null;
            if (args.length == 3) return Arrays.asList("1", "16", "32");
        } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) return null;
            if (args.length == 3) return Arrays.stream(RuneCategory.values()).map(Enum::name).collect(Collectors.toList());
            if (args.length == 4) return this.plugin.getRuneRegistry().all().stream().map(RuneRegistry.Definition::id).collect(Collectors.toList());
            if (args.length == 5) return Arrays.asList("25", "50", "75", "100");
            if (args.length == 6) return Arrays.asList("1", "16", "64");
        }
        return Collections.emptyList();
    }
}