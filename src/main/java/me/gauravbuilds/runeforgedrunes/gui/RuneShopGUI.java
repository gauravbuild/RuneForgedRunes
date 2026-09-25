package me.gauravbuilds.runeforgedrunes.gui;

import com.cryptomorin.xseries.XMaterial;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class RuneShopGUI implements Listener {
    private final RuneForgedRunes plugin;
    private final Map<UUID, Long> lastPurchase = new HashMap<>();

    public RuneShopGUI(RuneForgedRunes plugin) { this.plugin = plugin; }

    private record Offer(String rune, ConfigurationSection settings) { }

    private static class ShopHolder implements InventoryHolder {
        Inventory inventory;
        final int page;
        final List<Offer> offers;
        ShopHolder(int page, List<Offer> offers) { this.page = page; this.offers = offers; }
        @Override public Inventory getInventory() { return inventory; }
    }

    private Material material(String path, String fallback) {
        return XMaterial.matchXMaterial(plugin.getConfig().getString(path, fallback)).map(XMaterial::parseMaterial).orElseGet(() -> XMaterial.matchXMaterial(fallback).map(XMaterial::parseMaterial).orElse(Material.AIR));
    }

    private ItemStack item(Material material, String name, List<String> lines) {
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ColorUtil.parse(name));
        meta.setLore(lines.stream().map(ColorUtil::parse).toList());
        icon.setItemMeta(meta);
        return icon;
    }

    private List<Offer> offers() {
        File file = new File(plugin.getDataFolder(), "rune-shop.yml");
        if (!file.exists()) plugin.saveResource("rune-shop.yml", false);
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file).getConfigurationSection("items");
        List<Offer> offers = new ArrayList<>();
        if (root != null) for (String id : root.getKeys(false)) {
            ConfigurationSection entry = root.getConfigurationSection(id);
            if (entry != null && entry.getBoolean("enabled", true)) {
                String rune = entry.getString("rune", id);
                Offer offer = new Offer(rune, entry);
                if (plugin.getRuneRegistry().get(rune) != null && !payment(offer).getString("mode", "VAULT").equalsIgnoreCase("DISABLED")) offers.add(offer);
            }
        }
        return offers;
    }

    public void open(Player player, int requestedPage) {
        List<Offer> offers = offers();
        int configured = plugin.getConfig().getInt("gui.shop.size", 45);
        int size = configured >= 18 && configured <= 54 && configured % 9 == 0 ? configured : 45;
        int perPage = size - 9;
        int page = Math.max(0, Math.min(requestedPage, Math.max(0, (offers.size() - 1) / perPage)));
        ShopHolder holder = new ShopHolder(page, offers);
        Inventory inv = Bukkit.createInventory(holder, size, ColorUtil.parse(plugin.getConfig().getString("gui.shop.title", "&8Rune Shop &7({page})").replace("{page}", String.valueOf(page + 1))));
        holder.inventory = inv;
        ItemStack filler = item(material("gui.shop.filler-material", "BLACK_STAINED_GLASS_PANE"), " ", List.of());
        ItemStack bottom = item(material("gui.shop.bottom.filler-material", "GRAY_STAINED_GLASS_PANE"), " ", List.of());
        for (int i = 0; i < size; i++) inv.setItem(i, i >= perPage ? bottom : filler);
        for (int i = 0; i < perPage && page * perPage + i < offers.size(); i++) {
            Offer offer = offers.get(page * perPage + i);
            RuneRegistry.Definition rune = plugin.getRuneRegistry().get(offer.rune());
            if (rune == null) continue;
            ItemStack icon = plugin.getRuneManager().createRune(rune.id());
            if (icon == null) continue;
            ItemMeta meta = icon.getItemMeta();
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : List.of());
            ConfigurationSection payment = payment(offer);
            lore.add("");
            lore.add(ColorUtil.parse("&eClick to purchase: &f" + payment.getDouble("price", 100) + " " + (payment.getString("mode", "VAULT").equalsIgnoreCase("VAULT") ? "money" : "currency")));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inv.setItem(i, icon);
        }
        if (page > 0) inv.setItem(perPage, control("previous", "ARROW", "&ePrevious Page", List.of()));
        if ((page + 1) * perPage < offers.size()) inv.setItem(size - 1, control("next", "ARROW", "&eNext Page", List.of()));
        inv.setItem(size - 5, control("info", "BOOK", "&dRune Shop", List.of("&7Select a rune to purchase it.")));
        player.openInventory(inv);
    }

    private ConfigurationSection payment(Offer offer) {
        ConfigurationSection configured = plugin.getRuneRegistry().configuration().getConfigurationSection("runes." + offer.rune() + ".purchase");
        return offer.settings().contains("mode") ? offer.settings() : configured == null ? new YamlConfiguration().createSection("purchase") : configured;
    }

    private ItemStack control(String id, String fallback, String name, List<String> lore) {
        String path = "gui.shop.bottom." + id;
        return item(material(path + ".material", fallback), plugin.getConfig().getString(path + ".name", name), plugin.getConfig().getStringList(path + ".lore").isEmpty() ? lore : plugin.getConfig().getStringList(path + ".lore"));
    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ShopHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() != holder.inventory) return;
        int size = holder.inventory.getSize(), perPage = size - 9, slot = event.getRawSlot();
        if (slot == perPage && holder.page > 0) { open(player, holder.page - 1); return; }
        if (slot == size - 1 && (holder.page + 1) * perPage < holder.offers.size()) { open(player, holder.page + 1); return; }
        int index = holder.page * perPage + slot;
        if (slot < 0 || slot >= perPage || index >= holder.offers.size() || player.getInventory().firstEmpty() < 0) {
            if (slot >= 0 && slot < perPage && index < holder.offers.size()) player.sendMessage(ColorUtil.parse("&cMake room in your inventory first."));
            return;
        }
        if (System.currentTimeMillis() - lastPurchase.getOrDefault(player.getUniqueId(), 0L) < 500) return;
        Offer offer = holder.offers.get(index);
        RuneRegistry.Definition rune = plugin.getRuneRegistry().get(offer.rune());
        if (rune == null || !new RuneGUI(plugin).purchase(player, payment(offer))) return;
        ItemStack granted = plugin.getRuneManager().createRune(rune.id());
        if (granted != null) player.getInventory().addItem(granted);
        lastPurchase.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ColorUtil.parse("&aPurchased " + rune.name() + "&a!"));
    }

    @EventHandler
    public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ShopHolder && event.getRawSlots().stream().anyMatch(slot -> slot < event.getView().getTopInventory().getSize())) event.setCancelled(true);
    }
}