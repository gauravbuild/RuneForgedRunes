package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class FarmingForagingListener implements Listener {

    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();
    private final Set<UUID> isProcessing = new HashSet<>();

    public FarmingForagingListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (isProcessing.contains(player.getUniqueId())) return;
        
        ItemStack tool = player.getInventory().getItemInMainHand();
        List<RuneType> runes = slotManager.getAppliedRunes(tool);
        if (runes.isEmpty()) return;

        Block block = event.getBlock();
        Material mat = block.getType();

        // --- Farming ---
        if (runes.contains(RuneType.HARVESTER) || runes.contains(RuneType.SICKLE)) {
             if (isCrop(mat) || (runes.contains(RuneType.SICKLE) && isLeaf(mat))) {
                 isProcessing.add(player.getUniqueId());
                 breakArea(block, tool, 1);
                 isProcessing.remove(player.getUniqueId());
             }
        }
        
        if (runes.contains(RuneType.REPLANT) && isCrop(mat)) {
            if (block.getBlockData() instanceof Ageable) {
                Ageable age = (Ageable) block.getBlockData();
                if (age.getAge() == age.getMaximumAge()) {
                    // Schedule replant
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        block.setType(mat);
                    }, 2L);
                }
            }
        }
        
        if (runes.contains(RuneType.GOLD_RUSH) && isCrop(mat)) {
             if (random.nextDouble() < 0.0005) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_NUGGET));
             }
        }

        // --- Foraging ---
        if (runes.contains(RuneType.TIMBER) && isLog(mat)) {
            isProcessing.add(player.getUniqueId());
            breakTree(block, mat, tool, 0);
            isProcessing.remove(player.getUniqueId());
        }
        
        if (runes.contains(RuneType.LEAF_BLOWER) && isLeaf(mat)) {
            isProcessing.add(player.getUniqueId());
            breakArea(block, tool, 1);
            isProcessing.remove(player.getUniqueId());
        }
        
        if (runes.contains(RuneType.SAPLING_PLANTER) && isLog(mat)) {
            // Check if bottom log? Hard to know. Assumed yes.
            // Schedule sapling plant? Hard to know type.
            // Simplified: If sapling drops, plant it?
        }
        
        if (runes.contains(RuneType.NATURES_GIFT) && isLog(mat)) {
             if (random.nextDouble() < 0.05) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POPPY));
             }
        }
    }
    
    @EventHandler
    public void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        List<RuneType> runes = slotManager.getAppliedRunes(tool);

        // Green Thumb & Pumpkin King
        if (runes.contains(RuneType.GREEN_THUMB) && isCrop(event.getBlockState().getType())) {
             for (org.bukkit.entity.Item item : event.getItems()) {
                 item.getItemStack().setAmount((int)(item.getItemStack().getAmount() * 1.2));
             }
        }
        
        // Charcoal
        if (runes.contains(RuneType.CHARCOAL) && isLog(event.getBlockState().getType())) {
             for (org.bukkit.entity.Item item : event.getItems()) {
                 item.getItemStack().setType(Material.CHARCOAL);
             }
        }
        
        // Wood Chipper
        if (runes.contains(RuneType.WOOD_CHIPPER) && isLog(event.getBlockState().getType())) {
            if (random.nextDouble() < 0.10) {
                 for (org.bukkit.entity.Item item : event.getItems()) {
                     item.getItemStack().setAmount(item.getItemStack().getAmount() * 2);
                 }
            }
        }
        
        // Apple Finder
        if (runes.contains(RuneType.APPLE_FINDER) && event.getBlockState().getType() == Material.OAK_LEAVES) {
            if (random.nextDouble() < 0.10) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
            }
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        List<RuneType> runes = slotManager.getAppliedRunes(item);
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Fertilizer
        if (runes.contains(RuneType.FERTILIZER) && isCrop(block.getType())) {
             if (player.getInventory().contains(Material.BONE_MEAL)) {
                 if (block.applyBoneMeal(BlockFace.UP)) { // returns true if successful
                     player.getInventory().removeItem(new ItemStack(Material.BONE_MEAL, 1));
                 }
             }
        }
        
        // Hydration
        if (runes.contains(RuneType.HYDRATION) && event.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
        }
        
        // Bark Stripper
        if (runes.contains(RuneType.BARK_STRIPPER) && player.isSneaking() && isLog(block.getType())) {
            // Strip area
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
         if (event.getEntity().getKiller() != null) {
             Player killer = event.getEntity().getKiller();
             ItemStack item = killer.getInventory().getItemInMainHand();
             if (slotManager.getAppliedRunes(item).contains(RuneType.LIVESTOCK)) {
                 if (random.nextDouble() < 0.10) {
                     // Double drops
                     event.getDrops().forEach(drop -> 
                         event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), drop.clone())
                     );
                 }
             }
         }
    }

    private void breakArea(Block center, ItemStack tool, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue;
                Block t = center.getRelative(x, 0, z);
                if (t.getType() == center.getType()) {
                    t.breakNaturally(tool);
                }
            }
        }
    }
    
    private void breakTree(Block block, Material type, ItemStack tool, int count) {
        if (count > 200) return;
        
        for (int y = 0; y <= 1; y++) {
             for (int x = -1; x <= 1; x++) {
                 for (int z = -1; z <= 1; z++) {
                     if (x==0 && y==0 && z==0) continue;
                     Block rel = block.getRelative(x, y, z);
                     if (rel.getType() == type) {
                         rel.breakNaturally(tool);
                         breakTree(rel, type, tool, count + 1);
                     }
                 }
             }
        }
    }

    private boolean isCrop(Material mat) {
        return mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.BEETROOTS || mat == Material.NETHER_WART;
    }
    
    private boolean isLeaf(Material mat) {
        return mat.name().contains("LEAVES");
    }
    
    private boolean isLog(Material mat) {
        return mat.name().contains("_LOG");
    }
}
