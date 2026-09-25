//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.gauravbuilds.runeforgedrunes.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import com.cryptomorin.xseries.XMaterial;
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
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RuneGUI implements Listener {
    private final RuneForgedRunes plugin;
    private final Map<UUID, Long> adminCooldowns = new HashMap();

    public RuneGUI(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    private String mainTitle() {
        return ColorUtil.parse(this.plugin.getConfigManager().getString("gui.repository.title", "&8Rune Repository"));
    }

    private String categoryTitle(RuneCategory category) {
        String categoryName = categoryName(category);
        return ColorUtil.parse(this.plugin.getConfigManager().getString("gui.repository.category-title", "&8Category: {category}").replace("{category}", categoryName));
    }

    private String categoryName(RuneCategory category) {
        return this.plugin.getConfig().getString("gui.repository.categories." + category.name().toLowerCase(Locale.ROOT) + ".name", category.name());
    }

    private Material configuredMaterial(String path, Material fallback) {
        return XMaterial.matchXMaterial(this.plugin.getConfigManager().getString(path, fallback.name()))
                .map(XMaterial::parseMaterial).orElse(fallback);
    }

    private static class RepositoryHolder implements InventoryHolder {
        private Inventory inventory;
        private final RuneCategory category;

        RepositoryHolder(RuneCategory category) { this.category = category; }

        @Override public Inventory getInventory() { return inventory; }
    }

    private int inventorySize(String path, int fallback) {
        int size = this.plugin.getConfig().getInt(path, fallback);
        return size >= 9 && size <= 54 && size % 9 == 0 ? size : fallback;
    }

    public void openMainMenu(Player player) {
        RepositoryHolder holder = new RepositoryHolder(null);
        Inventory inv = Bukkit.createInventory(holder, this.inventorySize("gui.repository.main-size", 27), this.mainTitle());
        holder.inventory = inv;
        ItemStack border = this.createItem(this.configuredMaterial("gui.repository.main-filler-material", Material.BLACK_STAINED_GLASS_PANE), " ");

        for(int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, border);
        }

        for (RuneCategory category : RuneCategory.values()) {
            String path = "gui.repository.categories." + category.name().toLowerCase(Locale.ROOT);
            if (!this.plugin.getConfig().getBoolean(path + ".enabled", true)) continue;
            int slot = this.plugin.getConfig().getInt(path + ".slot", -1);
            if (slot < 0 || slot >= inv.getSize()) continue;
            ItemStack item = this.createCategoryItem(this.configuredMaterial(path + ".material", Material.NETHER_STAR), this.categoryName(category), this.plugin.getConfig().getStringList(path + ".lore"));
            inv.setItem(slot, item);
        }
        player.openInventory(inv);
    }

    public void openCategoryMenu(Player player, RuneCategory category) {
        if (!this.plugin.getConfig().getBoolean("gui.repository.categories." + category.name().toLowerCase(Locale.ROOT) + ".enabled", true)) return;
        RepositoryHolder holder = new RepositoryHolder(category);
        Inventory inv = Bukkit.createInventory(holder, this.inventorySize("gui.repository.category-size", 45), this.categoryTitle(category));
        holder.inventory = inv;
        ItemStack border = this.createItem(this.configuredMaterial("gui.repository.category-filler-material", Material.GRAY_STAINED_GLASS_PANE), " ");

        for(int i = 0; i < inv.getSize(); ++i) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, border);
            }
        }

        int backSlot = this.plugin.getConfig().getInt("gui.repository.back-slot", 36);
        if (backSlot >= 0 && backSlot < inv.getSize()) inv.setItem(backSlot, this.createItem(this.configuredMaterial("gui.repository.back-material", Material.ARROW), this.plugin.getConfig().getString("gui.repository.back-name", "&cGo Back")));
        List<RuneRegistry.Definition> runes = this.getRunesByCategory(category);
        List<Integer> configuredSlots = this.plugin.getConfigManager().getIntegerList("gui.repository.category-rune-slots");
        if (configuredSlots.isEmpty()) {
            configuredSlots = java.util.Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
        }
        int index = 0;

        for(RuneRegistry.Definition type : runes) {
            if (index >= configuredSlots.size()) {
                break;
            }

            ItemStack runeItem = player.hasPermission("runeforged.admin") ? this.plugin.getRuneManager().createRune(type.id(), 100) : this.plugin.getRuneManager().createRune(type.id());
            if (runeItem == null) continue;
            if (player.hasPermission("runeforged.admin")) {
                ItemMeta meta = runeItem.getItemMeta();
                List<Component> lore = new ArrayList<>(meta.hasLore() ? meta.lore() : List.of());
                lore.add(Component.empty());
                lore.add(Component.text("Admin Action:", NamedTextColor.RED, new TextDecoration[]{TextDecoration.BOLD}));
                lore.add(Component.text("Click to Spawn Item", NamedTextColor.YELLOW));
                meta.lore(lore);
                runeItem.setItemMeta(meta);
            } else {
                ItemMeta meta = runeItem.getItemMeta();
                List<Component> lore = new ArrayList<>(meta.hasLore() ? meta.lore() : List.of());
                String path = "runes." + type.id() + ".purchase";
                String mode = this.plugin.getRuneRegistry().configuration().getString(path + ".mode", "VAULT");
                lore.add(Component.empty());
                if (mode.equalsIgnoreCase("DISABLED")) lore.add(Component.text("Not for sale", NamedTextColor.RED));
                else lore.add(Component.text("Click to purchase: " + this.plugin.getRuneRegistry().configuration().getDouble(path + ".price", 100) + (mode.equalsIgnoreCase("VAULT") ? " money" : " currency"), NamedTextColor.YELLOW));
                meta.lore(lore);
                runeItem.setItemMeta(meta);
            }

            int slot = configuredSlots.get(index++);
            if (slot < 0 || slot >= inv.getSize() || slot == backSlot) continue;
            inv.setItem(slot, runeItem);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RepositoryHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getRawSlot();
        if (holder.category == null) {
            for (RuneCategory category : RuneCategory.values()) {
                String path = "gui.repository.categories." + category.name().toLowerCase(Locale.ROOT);
                if (this.plugin.getConfig().getBoolean(path + ".enabled", true) && slot == this.plugin.getConfig().getInt(path + ".slot", -1)) {
                    this.openCategoryMenu(player, category);
                    return;
                }
            }
            return;
        }
        if (slot == this.plugin.getConfig().getInt("gui.repository.back-slot", 36)) {
            this.openMainMenu(player);
            return;
        }
        if (!this.plugin.getConfigManager().getIntegerList("gui.repository.category-rune-slots").contains(slot)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        RuneRegistry.Definition type = this.plugin.getRuneRegistry().get(this.plugin.getRuneManager().getRuneId(clicked));
        if (type == null || !type.category().equalsIgnoreCase(holder.category.name())) return;
        if (System.currentTimeMillis() - this.adminCooldowns.getOrDefault(player.getUniqueId(), 0L) < 1000L) return;
        if (!player.hasPermission("runeforged.admin") && player.getInventory().firstEmpty() < 0) {
            player.sendMessage(Component.text("Make room in your inventory before purchasing a rune.", NamedTextColor.RED));
            return;
        }
        if (!player.hasPermission("runeforged.admin") && !this.purchase(player, type)) return;
        ItemStack granted = player.hasPermission("runeforged.admin") ? this.plugin.getRuneManager().createRune(type.id(), 100) : this.plugin.getRuneManager().createRune(type.id());
        player.getInventory().addItem(granted).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        this.adminCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RepositoryHolder
                && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) {
            event.setCancelled(true);
        }
    }

    private boolean purchase(Player player, RuneRegistry.Definition type) {
        return purchase(player, plugin.getRuneRegistry().configuration().getConfigurationSection("runes." + type.id() + ".purchase"));
    }

    public boolean purchase(Player player, ConfigurationSection offer) {
        if (offer == null) { player.sendMessage(Component.text("Purchase is not configured.", NamedTextColor.RED)); return false; }
        String mode = offer.getString("mode", "VAULT").toUpperCase(Locale.ROOT);
        double price = offer.getDouble("price", 100);
        if (!Double.isFinite(price) || price < 0) {
            player.sendMessage(Component.text("This rune has an invalid price.", NamedTextColor.RED));
            return false;
        }
        if (mode.equals("VAULT")) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                player.sendMessage(Component.text("Vault economy is not available.", NamedTextColor.RED));
                return false;
            }
            RegisteredServiceProvider<Economy> service = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            Economy economy = service == null ? null : service.getProvider();
            if (economy == null || !economy.isEnabled()) {
                player.sendMessage(Component.text("Vault economy is not available.", NamedTextColor.RED));
                return false;
            }
            if (!economy.has(player, price)) {
                player.sendMessage(Component.text("You need " + economy.format(price) + " to buy this rune.", NamedTextColor.RED));
                return false;
            }
            EconomyResponse result = economy.withdrawPlayer(player, price);
            if (!result.transactionSuccess()) {
                player.sendMessage(Component.text("Purchase failed: " + result.errorMessage, NamedTextColor.RED));
                return false;
            }
            return true;
        }
        if (mode.equals("COMMANDS")) {
            String balancePlaceholder = offer.getString("balance-placeholder", "");
            List<String> commands = offer.getStringList("commands");
            if (commands.isEmpty() || !balancePlaceholder.matches("%[A-Za-z0-9_]+%") || !plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                player.sendMessage(Component.text("Command currency is not configured (balance placeholder and charge commands required).", NamedTextColor.RED));
                return false;
            }
            double balance;
            try {
                balance = Double.parseDouble(PlaceholderAPI.setPlaceholders(player, balancePlaceholder).replace(",", "").trim());
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Currency balance is unavailable.", NamedTextColor.RED));
                return false;
            }
            if (!Double.isFinite(balance) || balance < price) {
                player.sendMessage(Component.text("You need " + price + " currency to buy this rune.", NamedTextColor.RED));
                return false;
            }
            List<String> prepared = new ArrayList<>();
            String amount = price == Math.rint(price) ? Long.toString((long) price) : Double.toString(price);
            for (String raw : commands) {
                String command = raw.replace("%player%", player.getName()).replace("{player}", player.getName()).replace("%price%", amount).trim();
                if (command.startsWith("/")) command = command.substring(1);
                if (command.isEmpty()) {
                    player.sendMessage(Component.text("Purchase charge command is invalid.", NamedTextColor.RED));
                    return false;
                }
                prepared.add(command);
            }
            for (String command : prepared) {
                if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)) {
                    player.sendMessage(Component.text("Purchase charge command failed; contact an admin if you were charged.", NamedTextColor.RED));
                    return false;
                }
            }
            return true;
        }
        player.sendMessage(Component.text("This rune is not for sale.", NamedTextColor.RED));
        return false;
    }

    private List<RuneRegistry.Definition> getRunesByCategory(RuneCategory cat) {
        List<RuneRegistry.Definition> list = new ArrayList<>();

        for(RuneRegistry.Definition type : plugin.getRuneRegistry().all()) {
            if (type.category().equalsIgnoreCase(cat.name())) {
                list.add(type);
            }
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

        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCategoryItem(Material mat, String name, List<String> lines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(ColorUtil.parse(name)));
        List<Component> lore = new ArrayList<>();
        for (String line : lines) lore.add(Component.text(ColorUtil.parse(line)));
        meta.lore(lore);
        meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
        item.setItemMeta(meta);
        return item;
    }
}
