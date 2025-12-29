package me.gauravbuilds.runeforgedrunes.gui;

import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RuneGUI implements Listener {

    private final RuneForgedRunes plugin;
    private final Map<UUID, Long> adminCooldowns = new HashMap<>();

    public RuneGUI(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Rune Repository", NamedTextColor.DARK_GRAY));

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, border);
        }

        inv.setItem(10, createCategoryItem(Material.NETHERITE_SWORD, "Combat", RuneCategory.COMBAT));
        inv.setItem(11, createCategoryItem(Material.NETHERITE_PICKAXE, "Mining", RuneCategory.MINING));
        inv.setItem(12, createCategoryItem(Material.NETHERITE_HOE, "Farming", RuneCategory.FARMING));
        inv.setItem(14, createCategoryItem(Material.NETHERITE_AXE, "Foraging", RuneCategory.FORAGING));
        inv.setItem(15, createCategoryItem(Material.ENCHANTING_TABLE, "Enchanting", RuneCategory.ENCHANTING));
        inv.setItem(16, createCategoryItem(Material.BEACON, "Sorcery", RuneCategory.SORCERY));

        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, RuneCategory category) {
        String titleName = category.name().charAt(0) + category.name().substring(1).toLowerCase();
        Inventory inv = Bukkit.createInventory(null, 45, Component.text("Category: " + titleName, NamedTextColor.DARK_GRAY));

        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }

        ItemStack back = createItem(Material.ARROW, "&cGo Back");
        inv.setItem(36, back);

        List<RuneType> runes = getRunesByCategory(category);
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};

        int index = 0;
        for (RuneType type : runes) {
            if (index >= slots.length) break;

            // Uses getRuneItem which we forced to 100% in Manager
            ItemStack runeItem = plugin.getRuneManager().getRuneItem(type);

            if (player.hasPermission("runeforged.admin")) {
                ItemMeta meta = runeItem.getItemMeta();
                List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Admin Action:", NamedTextColor.RED, TextDecoration.BOLD));
                lore.add(Component.text("Click to Spawn Item", NamedTextColor.YELLOW));
                meta.lore(lore);
                runeItem.setItemMeta(meta);
            }

            inv.setItem(slots[index++], runeItem);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        String title = event.getView().getTitle(); // Legacy string check
        if (title == null) return;

        boolean isMainMenu = title.contains("Rune Repository");
        boolean isCategoryMenu = title.contains("Category:");

        if (!isMainMenu && !isCategoryMenu) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (isMainMenu) {
            if (clicked.getType() == Material.NETHERITE_SWORD) openCategoryMenu(player, RuneCategory.COMBAT);
            else if (clicked.getType() == Material.NETHERITE_PICKAXE) openCategoryMenu(player, RuneCategory.MINING);
            else if (clicked.getType() == Material.NETHERITE_HOE) openCategoryMenu(player, RuneCategory.FARMING);
            else if (clicked.getType() == Material.NETHERITE_AXE) openCategoryMenu(player, RuneCategory.FORAGING);
            else if (clicked.getType() == Material.ENCHANTING_TABLE) openCategoryMenu(player, RuneCategory.ENCHANTING);
            else if (clicked.getType() == Material.BEACON) openCategoryMenu(player, RuneCategory.SORCERY);
        }

        if (isCategoryMenu) {
            if (clicked.getType() == Material.ARROW) {
                openMainMenu(player);
                return;
            }

            // Admin Logic
            if (player.hasPermission("runeforged.admin")) {
                RuneType type = plugin.getRuneManager().getRuneFromItem(clicked);

                if (type != null) {
                    if (adminCooldowns.containsKey(player.getUniqueId())) {
                        if (System.currentTimeMillis() - adminCooldowns.get(player.getUniqueId()) < 1000) {
                            return;
                        }
                    }

                    // GIVE 100% CHANCE RUNE TO ADMIN
                    player.getInventory().addItem(plugin.getRuneManager().createRune(type, 100.0));

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    adminCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    private List<RuneType> getRunesByCategory(RuneCategory cat) {
        List<RuneType> list = new ArrayList<>();
        for (RuneType type : RuneType.values()) {
            if (type.getCategory() == cat) list.add(type);
        }
        return list;
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (name != null && !name.equals(" ")) {
            meta.displayName(Component.text(name.replace("&", "§")));
        } else {
            meta.displayName(Component.text(" "));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCategoryItem(Material mat, String name, RuneCategory cat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§l" + name));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Click to view runes"));
        lore.add(Component.text("§7for this category."));
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
}