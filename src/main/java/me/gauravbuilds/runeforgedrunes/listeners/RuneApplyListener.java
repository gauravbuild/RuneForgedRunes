package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneManager;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import org.bukkit.event.Listener;

import java.util.Random;

public class RuneApplyListener implements Listener {

    private final RuneForgedRunes plugin;
    private final RuneManager runeManager;
    private final SlotManager slotManager;
    private final Random random = new Random();

    public RuneApplyListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.runeManager = plugin.getRuneManager();
        this.slotManager = plugin.getSlotManager();
    }

    /*
     * -----------------------------------------------------------------------------------
     * DISABLED: All Rune application logic is now handled by the 'RuneForgedEnchanter'
     * plugin via the Soulbinder NPC (The Forge).
     * -----------------------------------------------------------------------------------
     */

    /*
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor == null || cursor.getType() == Material.AIR || current == null || current.getType() == Material.AIR) {
            return;
        }

        RuneType rune = runeManager.getRuneFromItem(cursor);
        if (rune == null) return;

        if (!rune.getTarget().includes(current)) {
            return;
        }

        // We disable SlotManager ensureSlots here so it doesn't add the old lore
        // slotManager.ensureSlots(current);

        if (!slotManager.hasEmptySlot(current)) {
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("no-slots")));
            return;
        }

        // BLOCK THE INTERACTION (Redirect to Forge)
        event.setCancelled(true);
        player.sendMessage(ColorUtil.parse("&cThe Rune's energy is too volatile! Visit &5The Soulbinder &cat Spawn to fuse this."));

        // OLD APPLICATION LOGIC (DISABLED)

        if (cursor.getAmount() > 1) {
            cursor.setAmount(cursor.getAmount() - 1);
        } else {
            event.getView().setCursor(null);
        }

        double chance = runeManager.getChanceFromItem(cursor);
        if (chance <= 0) chance = rune.getRarity().getDefaultChance();

        double roll = random.nextDouble() * 100;

        if (roll <= chance) {
            slotManager.applyRune(current, rune);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("rune-applied")
                    .replace("<rune_name>", rune.getDisplayName())));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getMessage("rune-failed")));
        }

    }
    */

    /*
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        // Disabled to prevent "Empty Rune Slot" lore from being added automatically
        // ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        // slotManager.ensureSlots(item);
    }
    */

    /*
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        // Disabled to prevent "Empty Rune Slot" lore from being added automatically
        // if (event.getEntity() instanceof Player) {
        //     slotManager.ensureSlots(event.getItem().getItemStack());
        // }
    }
    */
}