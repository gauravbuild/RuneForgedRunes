package me.gauravbuilds.runeforgedrunes.listeners;

import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneType;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class CombatListener implements Listener {
    
    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();

    public CombatListener(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        
        // Attacker Logic
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            List<RuneType> runes = slotManager.getAppliedRunes(weapon);

            if (runes.contains(RuneType.VAMPIRE)) {
                if (random.nextDouble() < 0.25) { // Chance not specified, assuming 25% or always? "Chance to heal"
                    double heal = 1 + random.nextInt(2); // 1-2 HP
                    double max = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
                    attacker.setHealth(Math.min(max, attacker.getHealth() + heal));
                }
            }
            
            if (runes.contains(RuneType.EXECUTIONER)) {
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity victim = (LivingEntity) event.getEntity();
                    double maxHp = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
                    if (victim.getHealth() / maxHp < 0.2) {
                        event.setDamage(event.getDamage() * 1.5);
                    }
                }
            }
            
            if (runes.contains(RuneType.VENOM)) {
                 if (event.getEntity() instanceof LivingEntity) {
                     ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
                 }
            }
            
            if (runes.contains(RuneType.FROSTBITE)) {
                 if (event.getEntity() instanceof LivingEntity) {
                     ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                 }
            }
            
            if (runes.contains(RuneType.THUNDERSTRIKE)) {
                if (random.nextDouble() < 0.10) {
                    event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
                }
            }
        }
        
        // Bow Logic (Sniper)
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                 Player shooter = (Player) arrow.getShooter();
                 // Check bow used? Hard to track without metadata on arrow.
                 // We can assume if they are holding a bow, it might be the one.
                 // Better: Use EntityShootBowEvent to tag arrow. 
                 // For simplicity, we'll check main/off hand of shooter.
                 ItemStack main = shooter.getInventory().getItemInMainHand();
                 ItemStack off = shooter.getInventory().getItemInOffHand();
                 List<RuneType> runes = slotManager.getAppliedRunes(main);
                 if (runes.isEmpty()) runes = slotManager.getAppliedRunes(off);
                 
                 if (runes.contains(RuneType.SNIPER)) {
                     event.setDamage(event.getDamage() * 1.2);
                 }
            }
        }

        // Defender Logic (Armor)
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            ItemStack[] armor = victim.getInventory().getArmorContents();
            
            for (ItemStack piece : armor) {
                List<RuneType> runes = slotManager.getAppliedRunes(piece);
                
                if (runes.contains(RuneType.TANK)) {
                    event.setDamage(event.getDamage() * 0.95);
                }
                
                if (runes.contains(RuneType.REFLECT)) {
                    if (event.getDamager() instanceof LivingEntity) {
                        ((LivingEntity) event.getDamager()).damage(event.getDamage() * 0.10, victim);
                    }
                }
                
                if (runes.contains(RuneType.PARRY)) {
                     // 5% chance per piece? Or total? Assuming per piece stack or check once?
                     // "5% chance to block". Usually implies total check.
                     // But if multiple pieces have it? Let's assume stacks.
                     if (random.nextDouble() < 0.05) {
                         event.setDamage(0);
                         victim.sendMessage(ColorUtil.parse("<green>Parried!"));
                         event.setCancelled(true);
                         return;
                     }
                }
            }
        }
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        
        if (player.getHealth() - event.getFinalDamage() < 6.0) { // < 3 hearts
             // Check armor for Adrenaline
            for (ItemStack piece : player.getInventory().getArmorContents()) {
                if (slotManager.getAppliedRunes(piece).contains(RuneType.ADRENALINE)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
                    break; // Apply once
                }
            }
        }
    }
}
