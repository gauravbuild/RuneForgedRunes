package me.gauravbuilds.runeforgedrunes.tasks;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PassiveRuneTask extends BukkitRunnable {

    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;

    public PassiveRuneTask(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkHand(player);
            checkArmor(player);
        }
    }

    private void checkHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        List<RuneType> runes = slotManager.getAppliedRunes(item);

        if (runes.contains(RuneType.HASTE)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }
        if (runes.contains(RuneType.LIGHT)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 240, 0, false, false));
        }
        if (runes.contains(RuneType.SPEED_CHOP)) {
             player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }
        
        // Insight (Action Bar)
        if (runes.contains(RuneType.INSIGHT)) {
             // Raytrace
             var target = player.getTargetEntity(10);
             if (target instanceof org.bukkit.entity.LivingEntity) {
                 var living = (org.bukkit.entity.LivingEntity) target;
                 player.sendActionBar(ColorUtil.parse("<red>HP: " + String.format("%.1f", living.getHealth()) + "/" + living.getAttribute(Attribute.MAX_HEALTH).getValue()));
             }
        }
        
        // Glow, Luck, etc handled similarly if they are hand-held
    }

    private void checkArmor(Player player) {
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            List<RuneType> runes = slotManager.getAppliedRunes(piece);
            
            if (runes.contains(RuneType.HERMES)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
            }
            if (runes.contains(RuneType.SPRING)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false));
            }
            if (runes.contains(RuneType.DOLPHIN) && player.isInWater()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
            }
            if (runes.contains(RuneType.OXYGEN)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
            }
            if (runes.contains(RuneType.FIREWALKER)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
            }
            if (runes.contains(RuneType.SATURATION)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 40, 0, false, false));
            }
            if (runes.contains(RuneType.NIGHT_EYE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 240, 0, false, false));
            }
            if (runes.contains(RuneType.GLOW)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
            }
            if (runes.contains(RuneType.INVISIBILITY) && player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false));
            }
            
            // Dragon Fly
            if (runes.contains(RuneType.DRAGON_FLY)) {
                if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                    if (player.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                        player.setAllowFlight(true);
                    }
                }
            }
        }
    }
}
