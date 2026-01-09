package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MiningListener implements Listener {

    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();

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

        // --- DEBUG PRINT ---
        if (!runes.isEmpty()) {
            plugin.getLogger().info("Player " + player.getName() + " broke block with Runes: " + runes.toString());
        }
        // -------------------

        if (runes.isEmpty()) return;

        Block block = event.getBlock();

        // Gem Finder
        if (runes.contains(RuneType.GEM_FINDER) && block.getType() == Material.STONE) {
            // Increased chance to 50% just for TESTING. Change back to 0.005 later.
            if (random.nextDouble() < 0.50) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND));
                player.sendMessage("💎 Gem Finder Triggered!");
            }
        }

        // Vein Miner
        if (runes.contains(RuneType.VEIN_MINER) && player.isSneaking() && isOre(block.getType())) {
            player.sendMessage("⛏️ Vein Miner Triggered!");
            isBreaking.add(player.getUniqueId());
            try {
                breakVein(block, block.getType(), player, tool, 0);
            } finally {
                isBreaking.remove(player.getUniqueId());
            }
        }

        // Tunnel
        if (runes.contains(RuneType.TUNNEL) && player.isSneaking()) {
            player.sendMessage("💥 Tunnel Rune Triggered!");
            isBreaking.add(player.getUniqueId());
            try {
                breakArea(block, player, tool);
            } finally {
                isBreaking.remove(player.getUniqueId());
            }
        }
    }

    // ... (Keep your onDrop, onBlockBreakXP, onDurability methods the same) ...

    private void breakArea(Block center, Player player, ItemStack tool) {
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