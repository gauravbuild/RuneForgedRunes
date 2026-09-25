package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneRarity;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import com.cryptomorin.xseries.XSound;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class OrbListener implements Listener {

    private final RuneForgedRunes plugin;
    private final Random random = new Random();
    private final Set<UUID> currentlyUnboxing = new HashSet<>();

    public OrbListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOrbRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        RuneRarity rarity = plugin.getOrbManager().getOrbRarity(item);
        if (rarity == null) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        
        event.setCancelled(true);

        if (currentlyUnboxing.contains(player.getUniqueId())) {
            player.sendMessage(ColorUtil.parse("&cYou are already opening an Orb!"));
            return;
        }

        List<RuneRegistry.Definition> pool = getPool(rarity);
        if (pool.isEmpty()) {
            player.sendMessage(ColorUtil.parse("&cThere are no " + rarity.name() + " runes in runes.yml."));
            return;
        }
        // Consume 1 Orb
        if (item.getAmount() == 1) player.getInventory().setItemInMainHand(null);
        else item.setAmount(item.getAmount() - 1);
        startUnboxAnimation(player, rarity, pool);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Shattering Orb...")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Prevent closing during animation. Re-open if they try to escape!
        if (event.getView().getTitle().contains("Shattering Orb...") && currentlyUnboxing.contains(event.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> event.getPlayer().openInventory(event.getInventory()), 1L);
        }
    }

    private List<RuneRegistry.Definition> getPool(RuneRarity rarity) {
        List<RuneRegistry.Definition> pool = new ArrayList<>();
        for (RuneRegistry.Definition definition : plugin.getRuneRegistry().all()) {
            if (rarity == RuneRarity.CUSTOM ? definition.category().equalsIgnoreCase("CUSTOM") : definition.rarity().equalsIgnoreCase(rarity.name())) pool.add(definition);
        }
        return pool;
    }

    private void startUnboxAnimation(Player player, RuneRarity rarity, List<RuneRegistry.Definition> possibleRunes) {
        currentlyUnboxing.add(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.parse("&8Shattering Orb..."));
        
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fMeta = filler.getItemMeta();
        fMeta.setDisplayName(" ");
        filler.setItemMeta(fMeta);

        ItemStack pointer = new ItemStack(Material.HOPPER);
        ItemMeta pMeta = pointer.getItemMeta();
        pMeta.setDisplayName(ColorUtil.parse("&e\u2193 Winning Rune \u2193"));
        pointer.setItemMeta(pMeta);

        for (int i = 0; i < 27; i++) inv.setItem(i, filler);
        inv.setItem(4, pointer); // Pointer above the center

        player.openInventory(inv);

        // Get all runes matching this rarity

        new BukkitRunnable() {
            int ticks = 0;
            int delay = 1;
            int nextTick = 1;
            
            // Generate items for the scroll row (slots 9 to 17)
            LinkedList<ItemStack> scrollItems = new LinkedList<>();

            @Override
            public void run() {
                if (ticks == 0) {
                    for (int i = 0; i < 9; i++) scrollItems.add(generateRandomRune(possibleRunes, rarity));
                }

                if (ticks >= nextTick) {
                    // Play tick sound
                    XSound.matchXSound("BLOCK_NOTE_BLOCK_HAT").ifPresent(sound -> sound.play(player));
                    
                    // Shift items left
                    scrollItems.removeFirst();
                    scrollItems.addLast(generateRandomRune(possibleRunes, rarity));

                    for (int i = 0; i < 9; i++) {
                        inv.setItem(i + 9, scrollItems.get(i));
                    }

                    // Slow down math
                    if (ticks > 40) delay = 3;
                    if (ticks > 60) delay = 6;
                    if (ticks > 75) delay = 10;
                    if (ticks > 90) delay = 15;

                    nextTick += delay;
                }

                if (ticks >= 105) { // Stop the animation
                    ItemStack wonItem = scrollItems.get(4); // Center slot (slot 13)
                    inv.setItem(13, wonItem);
                    
                    XSound.matchXSound("ENTITY_PLAYER_LEVELUP").ifPresent(sound -> sound.play(player));
                    player.sendMessage(ColorUtil.parse("&a\u2728 You unboxed a " + wonItem.getItemMeta().getDisplayName() + "&a!"));
                    player.getInventory().addItem(wonItem).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
                    
                    currentlyUnboxing.remove(player.getUniqueId());
                    Bukkit.getScheduler().runTaskLater(plugin, () -> player.closeInventory(), 30L);
                    cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ItemStack generateRandomRune(List<RuneRegistry.Definition> possibleRunes, RuneRarity rarity) {
        RuneRegistry.Definition type = possibleRunes.get(random.nextInt(possibleRunes.size()));
        
        // Generate random chance based on rarity bounds
        double min = 50.0, max = 90.0;
        switch (rarity) {
            case COMMON -> { min = 50.0; max = 90.0; }
            case RARE -> { min = 40.0; max = 75.0; }
            case LEGENDARY -> { min = 25.0; max = 60.0; }
            case MYTHIC -> { min = 15.0; max = 45.0; }
            case CUSTOM -> { min = 50.0; max = 90.0; }
        }
        
        double randomChance = min + (max - min) * random.nextDouble();
        // Round to 1 decimal place
        randomChance = Math.round(randomChance * 10.0) / 10.0;

        return plugin.getRuneManager().createRune(type.id(), randomChance);
    }
}