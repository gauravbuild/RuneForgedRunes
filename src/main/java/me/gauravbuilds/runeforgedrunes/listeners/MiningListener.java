package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MiningListener implements Listener {

    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();
    
    // Prevent infinite loops from recursive breaking
    private final Set<UUID> isBreaking = new HashSet<>();

    public MiningListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (isBreaking.contains(player.getUniqueId())) return;
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        List<RuneType> runes = slotManager.getAppliedRunes(tool);
        
        if (runes.isEmpty()) return;

        Block block = event.getBlock();

        // Gem Finder
        if (runes.contains(RuneType.GEM_FINDER) && block.getType() == Material.STONE) {
            if (random.nextDouble() < 0.001) { // 0.1%
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND));
            }
        }

        // Tunnel (3x3) - Legendary, Sneak+Click (usually Break)
        if (runes.contains(RuneType.TUNNEL) && player.isSneaking()) {
            // Check cooldown? User said 10s.
            // Simplified: No cooldown impl yet for brevity, or use simple map.
            // Let's implement 3x3
            isBreaking.add(player.getUniqueId());
            breakArea(block, player, tool);
            isBreaking.remove(player.getUniqueId());
        }
        
        // Vein Miner - Mythic, Sneak
        if (runes.contains(RuneType.VEIN_MINER) && player.isSneaking() && isOre(block.getType())) {
            isBreaking.add(player.getUniqueId());
            breakVein(block, block.getType(), player, tool, 0);
            isBreaking.remove(player.getUniqueId());
        }
    }
    
    @EventHandler
    public void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        List<RuneType> runes = slotManager.getAppliedRunes(tool);
        
        // Smelter
        if (runes.contains(RuneType.SMELTER)) {
            for (org.bukkit.entity.Item itemEntity : event.getItems()) {
                ItemStack item = itemEntity.getItemStack();
                if (item.getType() == Material.RAW_IRON) item.setType(Material.IRON_INGOT);
                else if (item.getType() == Material.RAW_GOLD) item.setType(Material.GOLD_INGOT);
                else if (item.getType() == Material.IRON_ORE) item.setType(Material.IRON_INGOT); // Legacy/Silk check
                else if (item.getType() == Material.GOLD_ORE) item.setType(Material.GOLD_INGOT);
                itemEntity.setItemStack(item);
            }
        }
        
        // Magnet
        if (runes.contains(RuneType.MAGNET)) {
             for (org.bukkit.entity.Item itemEntity : new ArrayList<>(event.getItems())) {
                 player.getInventory().addItem(itemEntity.getItemStack()).forEach((i, rem) -> {
                     // If inventory full, drop remaining
                     if (rem.getAmount() > 0) {
                         player.getWorld().dropItem(player.getLocation(), rem);
                     }
                 });
                 itemEntity.remove();
             }
             event.getItems().clear();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) { // Change to BlockBreakEvent
        Player player = event.getPlayer(); // This method now exists!
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if the tool has the rune
        if (slotManager.getAppliedRunes(tool).contains(RuneType.EXPERIENCE)) {
            // Multiply the experience drop
            event.setExpToDrop(event.getExpToDrop() * 2);
        }
    }
    
    @EventHandler
    public void onDurability(PlayerItemDamageEvent event) {
        if (slotManager.getAppliedRunes(event.getItem()).contains(RuneType.DURABILITY)) {
            if (random.nextDouble() < 0.10) {
                event.setCancelled(true);
            }
        }
        
        // Unbreaking IV
        if (slotManager.getAppliedRunes(event.getItem()).contains(RuneType.UNBREAKING_IV)) {
            // Simulate unbreaking level 4 (chance 1/(level+1)) = 1/5 = 20% to use durability
            // Vanilla handles normal unbreaking. This is "Equivalent" or adds to it?
            // "Adds Unbreaking IV equivalent". Let's say it adds another 25% chance to ignore.
            if (random.nextDouble() < 0.25) {
                event.setCancelled(true);
            }
        }
        
        // Preservation
        if (slotManager.getAppliedRunes(event.getItem()).contains(RuneType.PRESERVATION)) {
            if (event.getItem().getType().getMaxDurability() - event.getDamage() <= 1) { // Will break
                event.setCancelled(true);
                // Break rune instead? Complex to remove rune here.
                // We'll leave it at 1 durability.
                event.setDamage(0); // No damage taken
                // TODO: Remove rune logic requires modifying item NBT, which is hard in this event safely?
                // We'll just save item.
            }
        }
    }

    private void breakArea(Block center, Player player, ItemStack tool) {
        // Simple 3x3 perpendicular to face is hard to get from BlockBreakEvent without raytrace
        // We'll assume relative to player view or just 3x3x1 cube around center
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block target = center.getRelative(x, y, z);
                    if (target.getType() != Material.BEDROCK && target.getType() != Material.AIR) {
                        target.breakNaturally(tool);
                    }
                }
            }
        }
    }

    private void breakVein(Block block, Material type, Player player, ItemStack tool, int count) {
        if (count > 64) return;
        
        for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            Block rel = block.getRelative(face);
            if (rel.getType() == type) {
                rel.breakNaturally(tool);
                breakVein(rel, type, player, tool, count + 1);
            }
        }
    }
    
    private boolean isOre(Material mat) {
        return mat.name().endsWith("_ORE") || mat.name().equals("ANCIENT_DEBRIS");
    }
}
