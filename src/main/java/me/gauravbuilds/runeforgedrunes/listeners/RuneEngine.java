package me.gauravbuilds.runeforgedrunes.listeners;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.XParticle;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.RuneTarget;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;
import org.bukkit.block.data.Ageable;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RuneEngine implements Listener {
    private final RuneForgedRunes plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Set<UUID> jumpFlight = new HashSet<>();
    private final Map<UUID, ItemStack> launchedBows = new HashMap<>();
    private final Map<UUID, List<ItemStack>> soulboundItems = new HashMap<>();
    private boolean breakingChain;

    public RuneEngine(RuneForgedRunes plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void attack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) dispatch(player, event.getEntity(), "ATTACK_ENTITY", List.of(player.getInventory().getItemInMainHand()), null, event);
        if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            ItemStack bow = launchedBows.get(projectile.getUniqueId());
            if (bow != null) dispatch(shooter, event.getEntity(), "BOW_HIT", List.of(bow), null, event);
        }
        if (event.getEntity() instanceof Player player) {
            dispatch(player, event.getDamager(), "TAKE_DAMAGE", armor(player), null, event);
            dispatch(player, event.getDamager(), "TAKE_DAMAGE", List.of(player.getInventory().getItemInMainHand()), null, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void damage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent) && event.getEntity() instanceof Player player) dispatch(player, null, "TAKE_DAMAGE", armor(player), null, event);
    }

    @EventHandler
    public void kill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) dispatch(killer, event.getEntity(), "KILL_ENTITY", List.of(killer.getInventory().getItemInMainHand()), null, event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void mine(BlockBreakEvent event) {
        if (breakingChain || plugin.getPlacedLogTracker().isPlayerPlaced(event.getBlock())) return;
        dispatch(event.getPlayer(), null, "MINE_BLOCK", List.of(event.getPlayer().getInventory().getItemInMainHand()), event.getBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void drops(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        dispatch(player, null, "BLOCK_DROPS", List.of(player.getInventory().getItemInMainHand()), event.getBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void wear(PlayerItemDamageEvent event) {
        dispatch(event.getPlayer(), null, "ITEM_DAMAGE", List.of(event.getItem()), null, event);
    }

    @EventHandler
    public void experience(PlayerExpChangeEvent event) {
        dispatch(event.getPlayer(), null, "GAIN_XP", List.of(event.getPlayer().getInventory().getItemInMainHand()), null, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void hunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && event.getFoodLevel() < player.getFoodLevel())
            dispatch(player, null, "HUNGER_CHANGE", armor(player), null, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void trample(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Player player && event.getBlock().getType().name().equals("FARMLAND") && event.getTo().name().equals("DIRT"))
            dispatch(player, null, "FARMLAND_TRAMPLE", List.of(player.getInventory().getItemInMainHand()), event.getBlock(), event);
    }

    @EventHandler
    public void die(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (ItemStack item : new java.util.ArrayList<>(event.getDrops()))
            dispatch(player, null, "DEATH", List.of(item), null, event);
    }

    @EventHandler
    public void respawn(PlayerRespawnEvent event) {
        List<ItemStack> kept = soulboundItems.remove(event.getPlayer().getUniqueId());
        if (kept != null) Bukkit.getScheduler().runTask(plugin, () -> {
            for (ItemStack item : kept) for (ItemStack leftover : event.getPlayer().getInventory().addItem(item).values())
                event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), leftover);
        });
    }

    @EventHandler
    public void anvil(PrepareAnvilEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player) || event.getResult() == null) return;
        for (int slot = 0; slot < 2; slot++) {
            ItemStack item = event.getInventory().getItem(slot);
            if (item != null) dispatch(player, null, "ANVIL", List.of(item), null, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void shoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && event.getBow() != null) {
            launchedBows.put(event.getProjectile().getUniqueId(), event.getBow().clone());
            Bukkit.getScheduler().runTaskLater(plugin, () -> launchedBows.remove(event.getProjectile().getUniqueId()), 1200L);
            dispatch(player, null, "BOW_SHOOT", List.of(event.getBow()));
        }
    }

    @EventHandler
    public void click(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (event.getAction().name().startsWith("LEFT_CLICK") && RuneTarget.SWORD.includes(item))
            dispatch(event.getPlayer(), null, "SWORD_SWING", List.of(item));
        if (event.getAction().name().startsWith("RIGHT_CLICK") && !event.isCancelled() && !RuneTarget.FISHING_ROD.includes(item))
            dispatch(event.getPlayer(), null, "RIGHT_CLICK", List.of(item), event.getClickedBlock(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void fish(PlayerFishEvent event) {
        ItemStack rod = event.getPlayer().getInventory().getItemInMainHand();
        if (!RuneTarget.FISHING_ROD.includes(rod)) return;
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            Player player = event.getPlayer();
            Entity hook = event.getHook();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && hook.isValid()) {
                    dispatch(player, hook, "FISH_CAST", List.of(rod));
                    dispatch(player, hook, "RIGHT_CLICK", List.of(rod));
                }
            }, 2L);
        } else if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY || event.getState() == PlayerFishEvent.State.IN_GROUND) {
            dispatch(event.getPlayer(), event.getCaught() != null ? event.getCaught() : event.getHook(), "FISH_REEL", List.of(rod));
        }
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            dispatch(event.getPlayer(), event.getCaught(), "FISH_CAUGHT", List.of(rod));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void prepareDoubleJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != org.bukkit.GameMode.SURVIVAL && player.getGameMode() != org.bukkit.GameMode.ADVENTURE) {
            jumpFlight.remove(player.getUniqueId());
            return;
        }
        if (player.isOnGround() && !player.getAllowFlight() && hasTrigger(player, "DOUBLE_JUMP")) {
            player.setAllowFlight(true);
            jumpFlight.add(player.getUniqueId());
        }
        if (jumpFlight.contains(player.getUniqueId()) && !hasTrigger(player, "DOUBLE_JUMP")) {
            player.setAllowFlight(false);
            jumpFlight.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void doubleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (!event.isFlying() || !jumpFlight.remove(player.getUniqueId())) return;
        event.setCancelled(true);
        player.setAllowFlight(false);
        boolean activated = dispatch(player, null, "DOUBLE_JUMP", armor(player));
        activated = dispatch(player, null, "DOUBLE_JUMP", List.of(player.getInventory().getItemInMainHand())) || activated;
        if (!activated) return;
        Vector forward = player.getLocation().getDirection().setY(0);
        if (forward.lengthSquared() > 0) forward.normalize().multiply(0.8);
        player.setVelocity(forward.setY(0.85));
    }

    @EventHandler public void leave(PlayerQuitEvent event) {
        if (jumpFlight.remove(event.getPlayer().getUniqueId())) event.getPlayer().setAllowFlight(false);
    }

    private boolean hasTrigger(Player player, String trigger) {
        List<ItemStack> items = armor(player);
        items.add(player.getInventory().getItemInMainHand());
        for (ItemStack item : items) for (String id : plugin.getSlotManager().getRuneIds(item)) {
            RuneRegistry.Definition rune = plugin.getRuneRegistry().get(id);
            if (rune != null) for (RuneRegistry.Ability ability : rune.abilities())
                if (ability.trigger().equalsIgnoreCase(trigger)) return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void sneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            Player player = event.getPlayer();
            dispatch(player, null, "SNEAK", armor(player));
            dispatch(player, null, "SNEAK", List.of(player.getInventory().getItemInMainHand()));
        }
    }

    private List<ItemStack> armor(Player player) {
        List<ItemStack> items = new java.util.ArrayList<>();
        for (ItemStack item : player.getInventory().getArmorContents()) if (item != null) items.add(item);
        return items;
    }

    private boolean dispatch(Player player, Entity target, String trigger, List<ItemStack> items) {
        return dispatch(player, target, trigger, items, null);
    }

    private boolean dispatch(Player player, Entity target, String trigger, List<ItemStack> items, Block block) {
        return dispatch(player, target, trigger, items, block, null);
    }

    public void passive(Player player) {
        dispatch(player, null, "PASSIVE", List.of(player.getInventory().getItemInMainHand()));
        dispatch(player, null, "PASSIVE", armor(player));
    }

    private boolean dispatch(Player player, Entity target, String trigger, List<ItemStack> items, Block block, Event event) {
        boolean activated = false;
        for (ItemStack item : items) for (String id : plugin.getSlotManager().getRuneIds(item)) {
            RuneRegistry.Definition rune = plugin.getRuneRegistry().get(id);
            if (rune == null) continue;
            for (RuneRegistry.Ability ability : rune.abilities()) {
                if (!ability.trigger().equalsIgnoreCase(trigger)) continue;
                if (ability.conditions().containsKey("is_sneaking") && Boolean.parseBoolean(ability.text("is_sneaking")) != player.isSneaking()) continue;
                if (ability.conditions().containsKey("target_type") && (target == null || !target.getType().name().equalsIgnoreCase(ability.text("target_type")))) continue;
                if (ability.conditions().containsKey("block_type") && (block == null || !block.getType().name().equalsIgnoreCase(ability.text("block_type")))) continue;
                if (ability.conditions().containsKey("block_suffix") && (block == null || !block.getType().name().endsWith(ability.text("block_suffix")))) continue;
                if (Boolean.parseBoolean(ability.text("is_mature")) && (block == null || !(block.getBlockData() instanceof Ageable age) || age.getAge() != age.getMaximumAge())) continue;
                if (ability.conditions().containsKey("health_below_percent") && (player.getMaxHealth() <= 0 || player.getHealth() * 100 / player.getMaxHealth() >= ability.number("health_below_percent", 100))) continue;
                String key = player.getUniqueId() + ":" + id + ":" + ability.id();
                long now = System.currentTimeMillis();
                if (now < cooldowns.getOrDefault(key, 0L) || ThreadLocalRandom.current().nextDouble(100) >= ability.number("chance", 100)) continue;
                cooldowns.put(key, now + Math.max(0, (long) (ability.number("cooldown", 0) * 1000)));
                activated = true;
                for (String effect : ability.effects()) apply(player, target, block, item, effect, event);
                if (!ability.actionBar().isEmpty()) player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ColorUtil.parse(ability.actionBar())));
            }
        }
        return activated;
    }

    private void apply(Player player, Entity target, Block block, ItemStack source, String raw, Event event) {
        String[] parts = raw.split(":", -1);
        if (parts.length < 2) return;
        String args = parts[1];
        Map<String, String> params = new HashMap<>();
        for (String part : args.split(",")) {
            int equals = part.indexOf('=');
            if (equals >= 0) params.put(part.substring(0, equals).trim().toLowerCase(), part.substring(equals + 1).trim());
        }
        LivingEntity victim = target instanceof LivingEntity living ? living : null;
        switch (parts[0].toUpperCase()) {
            case "REPLANT" -> {
                if (block != null && block.getBlockData() instanceof Ageable age && age.getAge() == age.getMaximumAge()) {
                    org.bukkit.block.data.BlockData young = block.getBlockData().clone();
                    ((Ageable) young).setAge(0);
                    Bukkit.getScheduler().runTask(plugin, () -> { if (block.getType().isAir()) block.setBlockData(young); });
                }
            }
            case "HARVEST", "CLEAR_LEAVES" -> {
                if (block == null || !(event instanceof BlockBreakEvent)) return;
                boolean leavesOnly = parts[0].equalsIgnoreCase("CLEAR_LEAVES");
                if (leavesOnly && !block.getType().name().endsWith("_LOG")) return;
                if (!leavesOnly && !(block.getBlockData() instanceof Ageable) && !block.getType().name().endsWith("_LEAVES")) return;
                int radius = Math.max(1, Math.min(3, (int) number(params, "radius", args, 1)));
                for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                    Block next = block.getRelative(x, 0, z);
                    if (next.equals(block)) continue;
                    if (leavesOnly) {
                        if (next.getType().name().endsWith("_LEAVES")) safeBreak(player, next);
                    } else if (next.getType() == block.getType() &&
                            (next.getType().name().endsWith("_LEAVES") || next.getBlockData() instanceof Ageable age && age.getAge() == age.getMaximumAge())) safeBreak(player, next);
                }
            }
            case "FERTILIZE" -> {
                if (block == null || !(block.getBlockData() instanceof Ageable age) || age.getAge() == age.getMaximumAge()) return;
                Material meal = XMaterial.matchXMaterial("BONE_MEAL").map(XMaterial::parseMaterial).orElse(null);
                if (meal == null || !player.getInventory().contains(meal)) return;
                player.getInventory().removeItem(new ItemStack(meal, 1));
                age.setAge(Math.min(age.getMaximumAge(), age.getAge() + 2));
                block.setBlockData(age);
            }
            case "CROP_YIELD", "GOLD_FROM_CROPS" -> {
                if (block == null || !(block.getBlockData() instanceof Ageable age) || age.getAge() != age.getMaximumAge()) return;
                if (parts[0].equalsIgnoreCase("GOLD_FROM_CROPS")) {
                    XMaterial.matchXMaterial("GOLD_NUGGET").map(XMaterial::parseItem).ifPresent(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));
                } else if (ThreadLocalRandom.current().nextDouble(100) < number(params, "percent", args, 20)) {
                    for (ItemStack drop : block.getDrops(player.getInventory().getItemInMainHand())) if (drop.getType() != block.getType()) {
                        drop.setAmount(1);
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                        break;
                    }
                }
            }
            case "DOUBLE_LOOT" -> {
                if (event instanceof EntityDeathEvent death && victim != null && victim instanceof org.bukkit.entity.Animals)
                    death.getDrops().addAll(death.getDrops().stream().map(ItemStack::clone).toList());
            }
            case "CANCEL_TRAMPLE" -> { if (event instanceof EntityChangeBlockEvent trampleEvent) trampleEvent.setCancelled(true); }
            case "GROW_CROPS" -> {
                int radius = Math.max(1, Math.min(Math.min(5, plugin.getConfig().getInt("ability-limits.max-radius", 12)), (int) number(params, "radius", args, 5)));
                Block center = player.getLocation().getBlock();
                for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                    Block crop = center.getRelative(x, 0, z);
                    if (crop.getBlockData() instanceof Ageable age && age.getAge() < age.getMaximumAge() && ThreadLocalRandom.current().nextInt(20) == 0) {
                        age.setAge(age.getAge() + 1);
                        crop.setBlockData(age);
                    }
                }
            }
            case "PLANT_SAPLING" -> {
                if (block == null || !block.getType().name().endsWith("_LOG") || plugin.getPlacedLogTracker().isPlayerPlaced(block)) return;
                String sapling = block.getType().name().replace("_LOG", "_SAPLING");
                Material mat = XMaterial.matchXMaterial(sapling).map(XMaterial::parseMaterial).orElse(null);
                if (mat != null) Bukkit.getScheduler().runTask(plugin, () -> {
                    if (block.getType().isAir() && block.getRelative(0, -1, 0).getType().isSolid()) block.setType(mat);
                });
            }
            case "DOUBLE_LOG", "CHARCOAL_DROP", "NATURE_DROP", "SILK_DROP" -> {
                if (block == null || plugin.getPlacedLogTracker().isPlayerPlaced(block)) return;
                String kind = parts[0].toUpperCase(java.util.Locale.ROOT);
                if (kind.equals("SILK_DROP")) {
                    if (!block.getType().name().endsWith("_LEAVES") || !(event instanceof BlockBreakEvent breakEvent)) return;
                    breakEvent.setDropItems(false);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
                } else if (block.getType().name().endsWith("_LOG")) {
                    String type = kind.equals("CHARCOAL_DROP") ? "CHARCOAL" : kind.equals("NATURE_DROP") ? "POPPY" : block.getType().name();
                    if (kind.equals("CHARCOAL_DROP") && event instanceof BlockBreakEvent breakEvent) breakEvent.setDropItems(false);
                    XMaterial.matchXMaterial(type).map(XMaterial::parseItem).ifPresent(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));
                }
            }
            case "STRIP_LOGS" -> {
                int radius = Math.max(1, Math.min(3, (int) number(params, "radius", args, 2)));
                Block center = block != null ? block : player.getLocation().getBlock();
                for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
                    Block log = center.getRelative(x, 0, z);
                    if (!log.getType().name().endsWith("_LOG") || plugin.getPlacedLogTracker().isPlayerPlaced(log)) continue;
                    Material stripped = XMaterial.matchXMaterial("STRIPPED_" + log.getType().name()).map(XMaterial::parseMaterial).orElse(null);
                    if (stripped != null) log.setType(stripped);
                }
            }
            case "SOULBOUND" -> {
                if (!(event instanceof PlayerDeathEvent death) || !death.getDrops().remove(source)) return;
                ItemStack kept = source.clone();
                List<String> runes = plugin.getSlotManager().getRuneIds(kept);
                runes.removeIf(id -> id.equalsIgnoreCase("SOULBOUND"));
                plugin.getSlotManager().updateRuneIds(kept, runes);
                soulboundItems.computeIfAbsent(player.getUniqueId(), id -> new java.util.ArrayList<>()).add(kept);
            }
            case "PRESERVE_ITEM" -> {
                if (!(event instanceof PlayerItemDamageEvent damage) || !(source.getItemMeta() instanceof Damageable meta)) return;
                if (meta.getDamage() + damage.getDamage() < source.getType().getMaxDurability()) return;
                damage.setCancelled(true);
                List<String> runes = plugin.getSlotManager().getRuneIds(source);
                runes.removeIf(id -> id.equalsIgnoreCase("PRESERVATION"));
                plugin.getSlotManager().updateRuneIds(source, runes);
                meta = (Damageable) source.getItemMeta();
                meta.setDamage(Math.max(0, source.getType().getMaxDurability() - 2));
                source.setItemMeta(meta);
            }
            case "REPAIR_WITH_XP" -> {
                if (!(event instanceof PlayerExpChangeEvent xp) || !(source.getItemMeta() instanceof Damageable meta)) return;
                if (meta.getDamage() < 1 || xp.getAmount() < 1) return;
                int used = Math.min(xp.getAmount(), (int) Math.ceil(meta.getDamage() / 2.0));
                meta.setDamage(Math.max(0, meta.getDamage() - used * 2));
                source.setItemMeta(meta);
                xp.setAmount(xp.getAmount() - used);
            }
            case "ORE_POTION" -> {
                if (block == null || !block.getType().name().contains("ORE")) return;
                XMaterial.matchXMaterial("POTION").map(XMaterial::parseItem).ifPresent(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));
            }
            case "SHOW_HEALTH" -> {
                Entity aimed = player.getTargetEntity(10);
                if (aimed instanceof LivingEntity living) player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ColorUtil.parse("&cHP: " + String.format("%.1f", living.getHealth()) + "/" + living.getMaxHealth())));
            }
            case "ANVIL_DISCOUNT" -> {
                if (event instanceof PrepareAnvilEvent anvilEvent) {
                    int cost = anvilEvent.getInventory().getRepairCost();
                    if (cost > 0) anvilEvent.getInventory().setRepairCost(Math.max(1, (int) Math.ceil(cost * (1 - number(params, "percent", args, 20) / 100))));
                }
            }
            case "PREVENT_FALL" -> { if (event instanceof EntityDamageEvent hit && hit.getCause() == EntityDamageEvent.DamageCause.FALL) hit.setCancelled(true); }
            case "WATER_POTION" -> {
                if (player.isInWater()) XPotion.matchXPotion(params.getOrDefault("type", args)).map(p -> p.buildPotionEffect(40, 0)).ifPresent(player::addPotionEffect);
            }
            case "DAMAGE_BOOST" -> {
                if (event instanceof EntityDamageByEntityEvent hit && victim != null && victim.getMaxHealth() > 0
                        && victim.getHealth() * 100 / victim.getMaxHealth() <= number(params, "health_below", "", 100))
                    hit.setDamage(hit.getDamage() + Math.min(plugin.getConfig().getDouble("ability-limits.max-damage", 20), hit.getDamage() * Math.max(0, number(params, "multiplier", args, 1) - 1)));
            }
            case "DAMAGE_REDUCTION" -> {
                if (event instanceof EntityDamageEvent hit) hit.setDamage(hit.getDamage() * (1 - Math.max(0, Math.min(100, number(params, "percent", args, 0))) / 100));
            }
            case "REFLECT_DAMAGE" -> {
                if (event instanceof EntityDamageByEntityEvent hit && victim != null && victim.isValid())
                    victim.damage(Math.max(0, Math.min(plugin.getConfig().getDouble("ability-limits.max-damage", 20), hit.getFinalDamage() * number(params, "percent", args, 10) / 100)));
            }
            case "CANCEL_DAMAGE" -> {
                if (event instanceof EntityDamageEvent hit && (!Boolean.parseBoolean(params.getOrDefault("attack_only", "false")) || hit instanceof EntityDamageByEntityEvent)) hit.setCancelled(true);
                if (event instanceof PlayerItemDamageEvent wear) wear.setCancelled(true);
            }
            case "LIGHTNING" -> {
                if (target != null) target.getWorld().strikeLightningEffect(target.getLocation());
                if (victim != null) victim.damage(Math.max(0, Math.min(plugin.getConfig().getDouble("ability-limits.max-damage", 20), number(params, "damage", "", 2))), player);
            }
            case "CANCEL_HUNGER" -> { if (event instanceof FoodLevelChangeEvent hungerEvent) hungerEvent.setCancelled(true); }
            case "MULTIPLY_XP" -> {
                double multiplier = Math.max(0, number(params, "multiplier", args, 2));
                if (event instanceof BlockBreakEvent mined && mined.getBlock().getType().name().contains("ORE")) mined.setExpToDrop((int) Math.min(10000, mined.getExpToDrop() * multiplier));
                if (event instanceof PlayerExpChangeEvent xp) xp.setAmount((int) Math.min(10000, xp.getAmount() * multiplier));
            }
            case "EXTRA_DROP" -> {
                if (block != null && block.getType().name().equalsIgnoreCase(params.getOrDefault("block", "STONE")))
                    XMaterial.matchXMaterial(params.getOrDefault("material", "DIAMOND")).map(XMaterial::parseItem).ifPresent(drop -> block.getWorld().dropItemNaturally(block.getLocation(), drop));
            }
            case "SMELT_DROP" -> {
                if (event instanceof BlockDropItemEvent dropsEvent) for (Item drop : dropsEvent.getItems()) {
                    String type = drop.getItemStack().getType().name();
                    String result = type.equals("RAW_IRON") ? "IRON_INGOT" : type.equals("RAW_GOLD") ? "GOLD_INGOT" : "";
                    if (!result.isEmpty()) XMaterial.matchXMaterial(result).map(XMaterial::parseMaterial).ifPresent(mat -> drop.setItemStack(new ItemStack(mat, drop.getItemStack().getAmount())));
                }
            }
            case "MAGNET_DROPS" -> {
                if (event instanceof BlockDropItemEvent dropsEvent) {
                    java.util.Iterator<Item> iterator = dropsEvent.getItems().iterator();
                    while (iterator.hasNext()) {
                        Item drop = iterator.next();
                        Map<Integer, ItemStack> leftover = player.getInventory().addItem(drop.getItemStack());
                        if (leftover.isEmpty()) iterator.remove();
                        else drop.setItemStack(leftover.values().iterator().next());
                    }
                }
            }
            case "TUNNEL" -> {
                if (block != null) {
                    org.bukkit.block.BlockFace face = player.getTargetBlockFace(5);
                    if (face == null) face = org.bukkit.block.BlockFace.UP;
                    int remaining = Math.max(0, plugin.getConfig().getInt("ability-limits.max-chain-blocks", 32) - 1);
                    for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) {
                        if (remaining <= 0) break;
                        Block next = face == org.bukkit.block.BlockFace.UP || face == org.bukkit.block.BlockFace.DOWN
                                ? block.getRelative(x, 0, y) : face == org.bukkit.block.BlockFace.EAST || face == org.bukkit.block.BlockFace.WEST
                                ? block.getRelative(0, x, y) : block.getRelative(x, y, 0);
                        if (next.equals(block) || !next.getType().isSolid()) continue;
                        safeBreak(player, next);
                        remaining--;
                    }
                }
            }
            case "HEAL" -> player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + Math.max(0, number(params, "amount", args, 1))));
            case "FREEZE" -> {
                if (victim == null) return;
                int ticks = Math.max(0, Math.min(1200, (int) number(params, "ticks", args, 60)));
                try {
                    LivingEntity.class.getMethod("setFreezeTicks", int.class).invoke(victim, ticks);
                } catch (ReflectiveOperationException exception) {
                    com.cryptomorin.xseries.XPotion.matchXPotion("SLOWNESS").map(p -> p.buildPotionEffect(ticks, 1)).ifPresent(victim::addPotionEffect);
                }
            }
            case "BLEED" -> {
                if (victim == null) return;
                int ticks = Math.max(0, Math.min(200, (int) number(params, "ticks", args, 60)));
                double damage = Math.max(0, number(params, "damage", parts.length > 2 ? parts[2] : "", 1));
                for (int t = 20; t <= ticks; t += 20) Bukkit.getScheduler().runTaskLater(plugin, () -> { if (victim.isValid() && !victim.isDead()) victim.damage(damage); }, t);
            }
            case "VORTEX_PULL" -> {
                double radius = Math.max(0, Math.min(16, number(params, "radius", args, 4)));
                double speed = Math.max(0, Math.min(2, number(params, "speed", parts.length > 2 ? parts[2] : "", 0.5)));
                for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
                    if (nearby.equals(player)) continue;
                    Vector direction = player.getLocation().toVector().subtract(nearby.getLocation().toVector());
                    if (direction.lengthSquared() > 0.01) nearby.setVelocity(direction.normalize().multiply(speed));
                }
            }
            case "BLACK_HOLE" -> {
                double radius = Math.max(1, Math.min(plugin.getConfig().getDouble("ability-limits.max-radius", 12), number(params, "radius", args, 5)));
                int ticks = Math.max(1, Math.min(plugin.getConfig().getInt("ability-limits.max-duration-seconds", 10), (int) number(params, "duration", parts.length > 2 ? parts[2] : "", 3))) * 20;
                Location center = effectLocation(player, target);
                new BukkitRunnable() {
                    int elapsed;
                    @Override public void run() {
                        if (elapsed++ >= ticks || center.getWorld() == null) { cancel(); return; }
                        XParticle.of("PORTAL").ifPresent(p -> { if (p.get() != null) center.getWorld().spawnParticle(p.get(), center, 6, 0.4, 0.4, 0.4); });
                        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                            if (!(entity instanceof LivingEntity living) || living.equals(player)) continue;
                            Vector pull = center.toVector().subtract(living.getLocation().toVector());
                            if (pull.lengthSquared() > 0.25 && pull.lengthSquared() <= radius * radius) living.setVelocity(pull.normalize().multiply(0.35));
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
            case "CHAIN_LIGHTNING" -> {
                double damage = Math.max(0, Math.min(plugin.getConfig().getDouble("ability-limits.max-damage", 20), number(params, "damage", args, 3)));
                int bounces = Math.max(0, Math.min(plugin.getConfig().getInt("ability-limits.max-bounces", 8), (int) number(params, "bounces", parts.length > 2 ? parts[2] : "", 3)));
                double range = Math.max(1, Math.min(plugin.getConfig().getDouble("ability-limits.max-radius", 12), number(params, "range", parts.length > 3 ? parts[3] : "", 6)));
                LivingEntity current = victim;
                if (current == null) current = nearest(player.getLocation(), player, Set.of(), range);
                Set<UUID> hit = new HashSet<>();
                Location previous = player.getEyeLocation();
                for (int i = 0; i <= bounces && current != null; i++) {
                    LivingEntity next = current;
                    if (!hit.add(next.getUniqueId())) break;
                    particleLine(previous, next.getLocation().add(0, 1, 0));
                    next.damage(damage, player);
                    previous = next.getLocation().add(0, 1, 0);
                    current = nearest(previous, player, hit, range);
                }
            }
            case "METEOR_STRIKE" -> {
                double radius = Math.max(1, Math.min(plugin.getConfig().getDouble("ability-limits.max-radius", 12), number(params, "radius", args, 4)));
                double damage = Math.max(0, Math.min(plugin.getConfig().getDouble("ability-limits.max-damage", 20), number(params, "damage", parts.length > 2 ? parts[2] : "", 6)));
                Location impact = effectLocation(player, target);
                new BukkitRunnable() {
                    int step;
                    @Override public void run() {
                        if (impact.getWorld() == null) { cancel(); return; }
                        if (step++ < 20) {
                            Location falling = impact.clone().add(0, 15 - step * 0.75, 0);
                            XParticle.of("FLAME").ifPresent(p -> { if (p.get() != null) falling.getWorld().spawnParticle(p.get(), falling, 18, 0.35, 0.35, 0.35); });
                            return;
                        }
                        XParticle.of("EXPLOSION").ifPresent(p -> { if (p.get() != null) impact.getWorld().spawnParticle(p.get(), impact, 3); });
                        XSound.matchXSound("ENTITY_GENERIC_EXPLODE").ifPresent(s -> s.play(player));
                        for (Entity entity : impact.getWorld().getNearbyEntities(impact, radius, radius, radius))
                            if (entity instanceof LivingEntity living && !living.equals(player) && living.getLocation().distanceSquared(impact) <= radius * radius) living.damage(damage, player);
                        cancel();
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
            case "VEIN_MINE", "TIMBER" -> {
                if (block == null) return;
                boolean timber = parts[0].equalsIgnoreCase("TIMBER");
                if (timber && !block.getType().name().endsWith("_LOG")) return;
                if (!timber && !block.getType().name().contains("ORE")) return;
                int limit = Math.max(1, Math.min(plugin.getConfig().getInt("ability-limits.max-chain-blocks", 32), (int) number(params, "max_blocks", args, 16)));
                Material originalType = block.getType();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline() && block.getType().isAir()) breakConnected(player, block, originalType, limit, timber);
                });
            }
            case "PROJECTILE" -> {
                double speed = Math.max(0.1, Math.min(plugin.getConfig().getDouble("ability-limits.max-projectile-speed", 4), number(params, "speed", parts.length > 2 ? parts[2] : "", 1.5)));
                String type = params.getOrDefault("type", args).toUpperCase(java.util.Locale.ROOT);
                if (type.equals("ARROW")) player.launchProjectile(Arrow.class, player.getLocation().getDirection().multiply(speed));
                else if (type.equals("SNOWBALL")) player.launchProjectile(Snowball.class, player.getLocation().getDirection().multiply(speed));
                else if (type.equals("FIREBALL")) {
                    Fireball fireball = player.launchProjectile(Fireball.class, player.getLocation().getDirection().multiply(speed));
                    fireball.setYield(0);
                    fireball.setIsIncendiary(false);
                }
            }
            case "POTION" -> {
                LivingEntity receiver = params.getOrDefault("target", "SELF").equalsIgnoreCase("VICTIM") ? victim : player;
                if (receiver == null) return;
                int ticks = Math.max(1, Math.min(72000, (int) number(params, "ticks", "", 100)));
                int amplifier = Math.max(0, Math.min(255, (int) number(params, "level", "", 1) - 1));
                String type = params.getOrDefault("type", args);
                XPotion.matchXPotion(type).filter(p -> p.get() != null).map(p -> p.buildPotionEffect(ticks, amplifier)).ifPresent(receiver::addPotionEffect);
            }
            case "PARTICLE" -> XParticle.of(params.getOrDefault("type", args)).ifPresent(p -> {
                Location location = target == null ? player.getLocation() : target.getLocation();
                int count = Math.max(0, Math.min(100, (int) number(params, "count", parts.length > 2 ? parts[2] : "", 10)));
                double radius = Math.max(0, Math.min(5, number(params, "radius", "", 1)));
                String shape = params.getOrDefault("shape", "POINT").toUpperCase(java.util.Locale.ROOT);
                if (p.get() == null || count == 0) return;
                Object data = particleData(p.get(), location);
                if (data == null && p.get().getDataType() != Void.class) return;
                if (shape.equals("POINT")) {
                    spawnParticle(p.get(), location, count, data);
                } else for (int i = 0; i < count; i++) {
                    double angle = 2 * Math.PI * i / count;
                    Location position = location.clone();
                    switch (shape) {
                        case "CIRCLE" -> position.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                        case "SPHERE" -> {
                            double y = 1 - 2 * (i + 0.5) / count;
                            double distance = Math.sqrt(1 - y * y);
                            double spiralAngle = i * Math.PI * (3 - Math.sqrt(5));
                            position.add(Math.cos(spiralAngle) * distance * radius, y * radius, Math.sin(spiralAngle) * distance * radius);
                        }
                        case "SPIRAL" -> position.add(Math.cos(i * Math.PI / 4) * radius, radius * i / count, Math.sin(i * Math.PI / 4) * radius);
                        default -> { return; }
                    }
                    spawnParticle(p.get(), position, 1, data);
                }
            });
            case "PLAY_SOUND" -> XSound.matchXSound(params.getOrDefault("sound", args)).ifPresent(s -> s.play(player));
            case "COMMAND" -> {
                if (parts.length < 3) return;
                if (!parts[1].equalsIgnoreCase("console") && !parts[1].equalsIgnoreCase("player")) return;
                String command = raw.substring(raw.indexOf(':', raw.indexOf(':') + 1) + 1).replace("{player}", player.getName());
                if (command.startsWith("/")) command = command.substring(1);
                CommandSender sender = parts[1].equalsIgnoreCase("player") ? player : Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(sender, command);
            }
            default -> { }
        }
    }

    private Location effectLocation(Player player, Entity target) {
        if (target != null) return target.getLocation().clone();
        Block looked = player.getTargetBlockExact(24);
        return looked != null ? looked.getLocation().add(0.5, 1, 0.5) : player.getLocation().add(player.getLocation().getDirection().multiply(5));
    }

    private LivingEntity nearest(Location center, Player owner, Set<UUID> excluded, double range) {
        LivingEntity nearest = null;
        double distance = range * range;
        for (Entity entity : center.getWorld().getNearbyEntities(center, range, range, range)) {
            if (!(entity instanceof LivingEntity living) || living.equals(owner) || excluded.contains(living.getUniqueId())) continue;
            double squared = living.getLocation().distanceSquared(center);
            if (squared < distance) { nearest = living; distance = squared; }
        }
        return nearest;
    }

    private void particleLine(Location from, Location to) {
        if (from.getWorld() != to.getWorld()) return;
        XParticle.of("ELECTRIC_SPARK").ifPresent(p -> {
            if (p.get() == null) return;
            Vector line = to.toVector().subtract(from.toVector());
            int steps = Math.max(1, Math.min(60, (int) (line.length() * 4)));
            for (int i = 0; i <= steps; i++) from.getWorld().spawnParticle(p.get(), from.clone().add(line.clone().multiply((double) i / steps)), 1);
        });
    }

    private void breakConnected(Player player, Block origin, Material originalType, int maxBlocks, boolean timber) {
        Set<Block> visited = new HashSet<>();
        ArrayDeque<Block> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);
        int broken = 0;
        while (!queue.isEmpty() && broken < maxBlocks - 1) {
            Block current = queue.remove();
            for (int x = -1; x <= 1 && broken < maxBlocks - 1; x++) for (int y = timber ? 0 : -1; y <= 1 && broken < maxBlocks - 1; y++) for (int z = -1; z <= 1 && broken < maxBlocks - 1; z++) {
                if (x == 0 && y == 0 && z == 0) continue;
                Block neighbor = current.getRelative(x, y, z);
                if (!visited.add(neighbor) || neighbor.getType() != originalType || plugin.getPlacedLogTracker().isPlayerPlaced(neighbor)) continue;
                BlockBreakEvent breakEvent = new BlockBreakEvent(neighbor, player);
                try {
                    breakingChain = true;
                    Bukkit.getPluginManager().callEvent(breakEvent);
                } finally { breakingChain = false; }
                if (breakEvent.isCancelled()) continue;
                neighbor.breakNaturally(player.getInventory().getItemInMainHand());
                broken++;
                queue.add(neighbor);
            }
        }
    }

    private void safeBreak(Player player, Block block) {
        if (plugin.getPlacedLogTracker().isPlayerPlaced(block)) return;
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        try {
            breakingChain = true;
            Bukkit.getPluginManager().callEvent(event);
        } finally { breakingChain = false; }
        if (!event.isCancelled()) block.breakNaturally(player.getInventory().getItemInMainHand());
    }

    private Object particleData(Particle particle, Location location) {
        Class<?> type = particle.getDataType();
        if (type == Void.class) return null;
        if (type == Particle.DustOptions.class) return new Particle.DustOptions(Color.WHITE, 1);
        if (type == Particle.DustTransition.class) return new Particle.DustTransition(Color.WHITE, Color.AQUA, 1);
        if (type == Particle.Trail.class) return new Particle.Trail(location.clone().add(0, 1, 0), Color.WHITE, 20);
        if (type == Vibration.class) return new Vibration(new Vibration.Destination.BlockDestination(location.getBlock()), 20);
        if (type == Color.class) return Color.WHITE;
        if (type == Float.class) return 0.0F;
        if (type == Integer.class) return 0;
        if (BlockData.class.isAssignableFrom(type)) return Bukkit.createBlockData(XMaterial.matchXMaterial("STONE").map(XMaterial::parseMaterial).orElseThrow());
        if (ItemStack.class.isAssignableFrom(type)) return XMaterial.matchXMaterial("DIAMOND").map(XMaterial::parseItem).orElse(null);
        return null;
    }

    private void spawnParticle(Particle particle, Location location, int count, Object data) {
        if (data == null) location.getWorld().spawnParticle(particle, location, count);
        else location.getWorld().spawnParticle(particle, location, count, data);
    }

    private double number(Map<String, String> params, String name, String fallback, double otherwise) {
        try { return Double.parseDouble(params.getOrDefault(name, fallback)); } catch (NumberFormatException e) { return otherwise; }
    }
}