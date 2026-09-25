package me.gauravbuilds.runeforgedrunes.tasks;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Random;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.SlotManager;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import me.gauravbuilds.runeforgedrunes.utils.RuneCooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PassiveRuneTask extends BukkitRunnable {
    private final RuneForgedRunes plugin;
    private final SlotManager slotManager;
    private final Random random = new Random();
    private final RuneCooldownManager cooldowns = new RuneCooldownManager();
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public PassiveRuneTask(RuneForgedRunes plugin) {
        this.plugin = plugin;
        this.slotManager = plugin.getSlotManager();
    }

    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.checkHand(player);
            this.checkArmor(player);
            plugin.getRuneEngine().passive(player);
        }
        this.restorePlayersWithoutInvisibility();
    }

    private void checkHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        List<String> runes = this.slotManager.getRuneIds(item);
        
        if (runes.contains("HASTE")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }

        if (runes.contains("LIGHT")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 240, 0, false, false));
        }

        if (runes.contains("SPEED_CHOP")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 1, false, false));
        }

        if (runes.contains("INSIGHT")) {
            Entity target = player.getTargetEntity(10);
            if (target instanceof LivingEntity living) {
                AttributeInstance maxHealth = living.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null && this.cooldowns.isReady(player, "INSIGHT", 1000L)) {
                    String currentHealth = String.format("%.1f", living.getHealth());
                    player.sendActionBar(ColorUtil.parse("&cHP: " + currentHealth + "/" + maxHealth.getValue()));
                }
            }
        }

        if (runes.contains("GLOW")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0, false, false));
        }

        if (runes.contains("SUNLIGHT")) {
            if (this.growNearbyCrops(player) && this.cooldowns.isReady(player, "SUNLIGHT", 5000L)) {
                player.sendActionBar(ColorUtil.parse("<gold>Sunlight accelerated nearby crops!"));
            }
        }

        this.updateLuckAttribute(player, runes.contains("LUCK"));
    }

    private boolean growNearbyCrops(Player player) {
        Block center = player.getLocation().getBlock();
        for (int x = -5; x <= 5; ++x) {
            for (int z = -5; z <= 5; ++z) {
                for (int y = -1; y <= 1; ++y) {
                    Block rel = center.getRelative(x, y, z);
                    if (rel.getBlockData() instanceof Ageable age) {
                        if (age.getAge() < age.getMaximumAge() && this.random.nextDouble() < 0.05) {
                            age.setAge(age.getAge() + 1);
                            rel.setBlockData(age);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void updateLuckAttribute(Player player, boolean shouldHaveLuck) {
        AttributeInstance inst = player.getAttribute(Attribute.LUCK);
        if (inst == null) return;
        NamespacedKey key = new NamespacedKey(this.plugin, "rune_luck");
        AttributeModifier existing = inst.getModifiers().stream()
                .filter(m -> m.getKey() != null && m.getKey().equals(key))
                .findFirst().orElse(null);

        if (shouldHaveLuck && existing == null) {
            inst.addModifier(new AttributeModifier(key, 1.0, AttributeModifier.Operation.ADD_NUMBER));
        } else if (!shouldHaveLuck && existing != null) {
            inst.removeModifier(existing);
        }
    }

    private void checkArmor(Player player) {
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            List<String> runes = this.slotManager.getRuneIds(piece);
            if (runes.contains("HERMES")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
            }

            if (runes.contains("SPRING")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false));
            }

            if (runes.contains("DOLPHIN") && player.isInWater()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 40, 0, false, false));
            }

            if (runes.contains("OXYGEN")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 40, 0, false, false));
            }

            if (runes.contains("FIREWALKER")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 0, false, false));
            }


            if (runes.contains("NIGHT_EYE")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 240, 0, false, false));
            }

        }
    }

    private void restorePlayersWithoutInvisibility() {
        for (UUID playerId : new HashSet<>(this.hiddenPlayers)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !this.hasSneakingInvisibilityRune(player)) {
                if (player != null) {
                    for (Player viewer : Bukkit.getOnlinePlayers()) {
                        viewer.showPlayer(this.plugin, player);
                    }
                }
                this.hiddenPlayers.remove(playerId);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.hasSneakingInvisibilityRune(player) && this.hiddenPlayers.add(player.getUniqueId())) {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.getUniqueId().equals(player.getUniqueId())) {
                        viewer.hidePlayer(this.plugin, player);
                    }
                }
                player.sendActionBar(ColorUtil.parse("&7Invisibility Rune concealed you."));
            }
        }
    }

    private boolean hasSneakingInvisibilityRune(Player player) {
        return player.isSneaking() && this.slotManager.getRuneIds(player.getInventory().getChestplate()).contains("INVISIBILITY");
    }
}