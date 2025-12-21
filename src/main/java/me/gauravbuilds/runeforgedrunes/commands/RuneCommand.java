package me.gauravbuilds.runeforgedrunes.commands;

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
                sender.sendMessage(ColorUtil.parse("<gray>- " + type.name() + " (" + type.getDisplayName() + ")"));
            }
            return true;
        }

        if (sub.equals("give")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.parse("<red>Usage: /rune give <player> <rune> [amount]"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("invalid-player")));
                return true;
            }

            RuneType type;
            try {
                type = RuneType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("invalid-rune")));
                return true;
            }

            int amount = 1;
            if (args.length > 3) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {}
            }

            ItemStack item = plugin.getRuneManager().createRune(type);
            item.setAmount(amount);
            target.getInventory().addItem(item);
            sender.sendMessage(ColorUtil.parse("<green>Given " + amount + " " + type.getDisplayName() + " Rune(s) to " + target.getName()));
            target.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("rune-received").replace("<rune_name>", type.getDisplayName())));
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
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(RuneType.values()).map(Enum::name).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
