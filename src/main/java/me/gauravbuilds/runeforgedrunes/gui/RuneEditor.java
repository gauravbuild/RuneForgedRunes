package me.gauravbuilds.runeforgedrunes.gui;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.particles.XParticle;
import me.gauravbuilds.runeforgedrunes.RuneCategory;
import me.gauravbuilds.runeforgedrunes.RuneForgedRunes;
import me.gauravbuilds.runeforgedrunes.managers.RuneRegistry;
import me.gauravbuilds.runeforgedrunes.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RuneEditor implements Listener {
    private final RuneForgedRunes plugin;
    private final Map<UUID, String> rename = new ConcurrentHashMap<>();
    private final Map<UUID, String> create = new ConcurrentHashMap<>();
    private final Map<UUID, String> newAbility = new ConcurrentHashMap<>();
    private final Map<UUID, String> modelInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> materialInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> priceInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> balanceInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> commandInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> effectEdit = new ConcurrentHashMap<>();
    private final Map<UUID, String> loreInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> abilityDescriptionInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> abilityActionBarInput = new ConcurrentHashMap<>();
    private static final String[] TRIGGERS = {"ATTACK_ENTITY", "TAKE_DAMAGE", "KILL_ENTITY", "MINE_BLOCK", "BLOCK_DROPS", "ITEM_DAMAGE", "GAIN_XP", "FARMLAND_TRAMPLE", "DEATH", "ANVIL", "BOW_HIT", "HUNGER_CHANGE", "PASSIVE", "BOW_SHOOT", "RIGHT_CLICK", "FISH_CAST", "FISH_REEL", "FISH_CAUGHT", "SWORD_SWING", "DOUBLE_JUMP", "SNEAK"};
    private static final String[] EFFECTS = {
            "HEAL:amount=2", "FREEZE:ticks=60", "BLEED:ticks=60,damage=1", "VORTEX_PULL:radius=5,speed=0.5",
            "BLACK_HOLE:radius=5,duration=3", "CHAIN_LIGHTNING:damage=3,bounces=3,range=6", "METEOR_STRIKE:radius=4,damage=6",
            "VEIN_MINE:max_blocks=16", "TIMBER:max_blocks=16", "PLAY_SOUND:sound=ENTITY_PLAYER_LEVELUP",
            "COMMAND:console:say {player} forged a rune", "DAMAGE_BOOST:multiplier=1.5", "DAMAGE_REDUCTION:percent=5",
            "REFLECT_DAMAGE:percent=10", "CANCEL_DAMAGE:", "LIGHTNING:damage=2", "EXTRA_DROP:material=DIAMOND,block=STONE",
            "MULTIPLY_XP:multiplier=2", "SMELT_DROP:", "MAGNET_DROPS:", "TUNNEL:radius=1", "HARVEST:radius=1",
            "REPLANT:", "FERTILIZE:", "CROP_YIELD:percent=20", "DOUBLE_LOOT:animals=true", "CANCEL_TRAMPLE:",
            "GROW_CROPS:radius=5", "GOLD_FROM_CROPS:", "CLEAR_LEAVES:radius=1", "PLANT_SAPLING:", "DOUBLE_LOG:",
            "SILK_DROP:", "CHARCOAL_DROP:", "NATURE_DROP:", "STRIP_LOGS:radius=2", "SOULBOUND:",
            "PRESERVE_ITEM:", "REPAIR_WITH_XP:amount=2", "ORE_POTION:", "SHOW_HEALTH:",
            "ANVIL_DISCOUNT:percent=20", "PREVENT_FALL:", "WATER_POTION:type=DOLPHINS_GRACE,ticks=40", "CANCEL_HUNGER:"
    };
    private static final String[] PROJECTILES = {"SNOWBALL", "ARROW", "FIREBALL"};
    private static final String[] RARITIES = {"COMMON", "RARE", "LEGENDARY", "MYTHIC"};
    private static final String[] TARGETS = {"SWORD", "AXE", "PICKAXE", "SHOVEL", "HOE", "BOW", "FISHING_ROD", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "MELEE_WEAPON", "AXE_OR_HOE", "ARMOR", "TOOL", "ALL"};

    public RuneEditor(RuneForgedRunes plugin) { this.plugin = plugin; }

    private int size(String path, int fallback, int minimum) {
        int value = plugin.getConfig().getInt(path, fallback);
        return value >= minimum && value <= 54 && value % 9 == 0 ? value : fallback;
    }

    private Inventory menu(Menu holder, int size, String title) {
        Inventory inv = Bukkit.createInventory(holder, size, ColorUtil.parse(title));
        holder.inventory = inv;
        Material filler = XMaterial.matchXMaterial(plugin.getConfig().getString("gui.editor.filler-material", "BLACK_STAINED_GLASS_PANE"))
                .map(XMaterial::parseMaterial).orElse(Material.PAPER);
        for (int i = 0; i < size; i++) inv.setItem(i, button(filler, " "));
        return inv;
    }

    private static class Menu implements InventoryHolder {
        final String id;
        final int page;
        Inventory inventory;
        Menu(String id, int page) { this.id = id; this.page = page; }
        @Override public Inventory getInventory() { return inventory; }
    }

    public void open(Player player, int page) {
        Menu menu = new Menu(null, page);
        int size = size("gui.editor.size", 54, 18);
        int perPage = size - 9;
        Inventory inv = menu(menu, size, plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· " + (page + 1));
        List<RuneRegistry.Definition> runes = new ArrayList<>(plugin.getRuneRegistry().all());
        for (int i = 0; i < perPage && i + page * perPage < runes.size(); i++) inv.setItem(i, plugin.getRuneManager().createRune(runes.get(i + page * perPage).id()));
        inv.setItem(perPage, button(Material.ARROW, "&ePrevious page"));
        inv.setItem(size - 5, button(Material.EMERALD, "&aCreate rune", "&7Enter an ID in chat"));
        inv.setItem(size - 1, button(Material.ARROW, "&eNext page"));
        player.openInventory(inv);
    }

    public void edit(Player player, String id) {
        RuneRegistry.Definition rune = plugin.getRuneRegistry().get(id);
        if (rune == null) return;
        Menu menu = new Menu(rune.id(), 0);
        Inventory inv = menu(menu, size("gui.editor.edit-size", 27, 27), plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· " + rune.id());
        inv.setItem(1, button(XMaterial.matchXMaterial(rune.material()).map(XMaterial::parseMaterial).orElse(Material.NETHER_STAR), "&eBase Item & Model", "&7Base: " + rune.material(), "&7Left click to type a material in chat", "&7Model: " + rune.modelData(), "&7Shift click to set model data in chat"));
        inv.setItem(2, button(Material.PAPER, "&eSuccess / Destroy", "&7Success: " + rune.success() + "%", "&7Destroy: " + rune.destroy() + "%", "&aLeft/Right: success +/- 5", "&cShift left/right: destroy +/- 5"));
        inv.setItem(3, button(Material.BOOK, "&eAbilities", "&7Edit the abilities section in runes.yml", "&7Loaded abilities: " + rune.abilities().size()));
        inv.setItem(4, button(Material.NAME_TAG, "&eRename", "&7Current: " + rune.name(), "&7Click and type the name in chat", "&7Hex colors supported: &#RRGGBB"));
        inv.setItem(5, button(Material.NETHER_STAR, "&eOrb tier", "&7" + rune.rarity(), "&7Click to cycle"));
        inv.setItem(6, button(Material.IRON_SWORD, "&eTarget equipment", "&7" + String.join(", ", rune.targets()), "&7Click to cycle"));
        inv.setItem(7, button(Material.CHEST, "&eRepository category", "&7" + rune.category(), "&7Click to cycle"));
        String purchase = "runes." + rune.id() + ".purchase";
        inv.setItem(8, button(Material.EMERALD, "&ePurchase mode", "&7" + plugin.getRuneRegistry().configuration().getString(purchase + ".mode", "VAULT"), "&7Click to cycle VAULT / COMMANDS / DISABLED"));
        inv.setItem(9, button(Material.GOLD_INGOT, "&ePurchase price", "&7" + plugin.getRuneRegistry().configuration().getDouble(purchase + ".price", 100), "&7Click to enter price in chat"));
        inv.setItem(10, button(Material.PAPER, "&eCurrency balance placeholder", "&7" + plugin.getRuneRegistry().configuration().getString(purchase + ".balance-placeholder", ""), "&7Required for COMMANDS mode", "&7Click to enter e.g. %credits_balance%"));
        inv.setItem(11, button(Material.COMMAND_BLOCK, "&ePurchase commands", "&7" + String.join(", ", plugin.getRuneRegistry().configuration().getStringList(purchase + ".commands")), "&7Left click to add a console command in chat", "&7Right click to remove the last command", "&7Use %player% and %price% in commands"));
        List<String> lorePreview = new ArrayList<>(rune.lore());
        lorePreview.add("&eLeft: add a line in chat");
        lorePreview.add("&eRight: remove the last line");
        inv.setItem(12, button(XMaterial.matchXMaterial("WRITABLE_BOOK").map(XMaterial::parseMaterial).orElse(Material.BOOK), "&eEdit Lore", lorePreview.toArray(new String[0])));
        inv.setItem(22, button(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    private void abilities(Player player, String id) {
        RuneRegistry.Definition rune = plugin.getRuneRegistry().get(id);
        if (rune == null) return;
        Menu menu = new Menu(id + ":abilities", 0);
        Inventory inv = menu(menu, size("gui.editor.abilities-size", 27, 27), plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· Abilities · " + id);
        int slot = 0;
        for (RuneRegistry.Ability ability : rune.abilities()) {
            if (slot >= 18) break;
            inv.setItem(slot++, button(Material.BOOK, "&d" + ability.id(), "&7Trigger: " + ability.trigger(), "&7Chance: " + ability.number("chance", 100) + "%", "&7Cooldown: " + ability.number("cooldown", 0) + "s", "&7Effects: " + ability.effects().size(), "&eClick to edit"));
        }
        inv.setItem(22, button(Material.ARROW, "&eBack"));
        inv.setItem(26, button(Material.EMERALD, "&aAdd ability", "&7Enter its ID in chat"));
        player.openInventory(inv);
    }

    private void ability(Player player, String id, String abilityId) {
        String path = "runes." + id + ".abilities." + abilityId;
        Menu menu = new Menu(id + ":ability:" + abilityId, 0);
        Inventory inv = menu(menu, size("gui.editor.ability-size", 54, 54), plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· " + abilityId);
        inv.setItem(1, button(Material.COMPASS, "&eTrigger", "&7" + plugin.getRuneRegistry().configuration().getString(path + ".trigger", "ATTACK_ENTITY"), "&7Click to cycle"));
        inv.setItem(2, button(Material.PAPER, "&eChance", "&7" + plugin.getRuneRegistry().configuration().getDouble(path + ".conditions.chance", 100) + "%", "&7Left/Right: +/- 5"));
        inv.setItem(3, button(Material.CLOCK, "&eCooldown", "&7" + plugin.getRuneRegistry().configuration().getDouble(path + ".conditions.cooldown", 0) + " seconds", "&7Left/Right: +/- 1"));
        List<String> effects = plugin.getRuneRegistry().configuration().getStringList(path + ".effects");
        inv.setItem(4, button(Material.BLAZE_POWDER, "&eEffects", "&7Left click for Effect Catalog", "&7Right click to remove the last effect"));
        List<String> description = new ArrayList<>(plugin.getRuneRegistry().configuration().getStringList(path + ".description"));
        description.add("&eLeft: add a description line in chat");
        description.add("&eRight: remove the last line");
        inv.setItem(5, button(Material.BOOK, "&eAbility description", description.toArray(new String[0])));
        inv.setItem(6, button(Material.NAME_TAG, "&eAction bar", "&7" + plugin.getRuneRegistry().configuration().getString(path + ".action-bar", ""), "&eLeft: type message in chat", "&eRight: clear message"));
        for (int i = 0; i < effects.size() && i < 36; i++) inv.setItem(9 + i, button(Material.PAPER, "&d" + (i + 1), "&7" + effects.get(i), "&eLeft: edit parameters / raw effect", "&eRight: cycle particle shape / potion target"));
        inv.setItem(49, button(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    private void catalog(Player player, String id, String abilityId) {
        Menu menu = new Menu(id + ":catalog:" + abilityId, 0);
        Inventory inv = menu(menu, size("gui.editor.catalog-size", 54, 54), plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· Effect Catalog");
        String[] icons = {"GOLDEN_APPLE", "BLUE_ICE", "REDSTONE", "ENDER_PEARL", "ENDER_EYE", "LIGHTNING_ROD", "BLAZE_POWDER", "IRON_PICKAXE", "IRON_AXE", "NOTE_BLOCK", "COMMAND_BLOCK"};
        for (int i = 0; i < EFFECTS.length; i++) {
            Material icon = XMaterial.matchXMaterial(i < icons.length ? icons[i] : "PAPER").map(XMaterial::parseMaterial).orElse(Material.PAPER);
            inv.setItem(i, button(icon, "&d" + EFFECTS[i].split(":")[0], "&7" + EFFECTS[i], "&eClick to add"));
        }
        inv.setItem(45, button(Material.POTION, "&dPOTION", "&7Choose a potion type"));
        inv.setItem(46, button(Material.FIREWORK_STAR, "&dPARTICLE", "&7Choose a particle type"));
        inv.setItem(47, button(Material.SNOWBALL, "&dPROJECTILE", "&7Choose a projectile type"));
        inv.setItem(49, button(Material.ARROW, "&eBack"));
        player.openInventory(inv);
    }

    private void options(Player player, String id, String abilityId, String type, int page) {
        Menu menu = new Menu(id + ":options:" + abilityId + ":" + type, page);
        Inventory inv = menu(menu, size("gui.editor.options-size", 54, 54), plugin.getConfig().getString("gui.editor.title", "&8✦ Rune Editor") + " &7· " + type + " · " + (page + 1));
        List<String> choices = choices(type);
        for (int i = 0; i < 45 && page * 45 + i < choices.size(); i++) {
            String choice = choices.get(page * 45 + i);
            inv.setItem(i, button(type.equals("POTION") ? Material.POTION : type.equals("PARTICLE") ? Material.FIREWORK_STAR : Material.SNOWBALL, "&d" + choice, "&eClick to add"));
        }
        inv.setItem(45, button(Material.ARROW, "&ePrevious"));
        inv.setItem(49, button(Material.BARRIER, "&cBack to catalog"));
        inv.setItem(53, button(Material.ARROW, "&eNext"));
        player.openInventory(inv);
    }

    private List<String> choices(String type) {
        if (type.equals("PROJECTILE")) return List.of(PROJECTILES);
        if (type.equals("POTION")) return java.util.Arrays.stream(XPotion.values()).filter(p -> p.get() != null).map(Enum::name).sorted().toList();
        return java.util.Arrays.stream(XParticle.values()).filter(p -> p.get() != null && plugin.supportedParticleData(p.get())).map(Enum::name).sorted().toList();
    }

    private void addEffect(Player player, String id, String abilityId, String effect) {
        String path = "runes." + id + ".abilities." + abilityId + ".effects";
        List<String> effects = plugin.getRuneRegistry().configuration().getStringList(path);
        if (effects.size() >= 36) { player.sendMessage(ColorUtil.parse("&cAn ability can have at most 36 effects.")); return; }
        effects.add(effect);
        plugin.getRuneRegistry().configuration().set(path, effects);
        plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload(); ability(player, id, abilityId);
    }

    private ItemStack button(Material material, String name, String... lines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.parse(name));
        List<String> lore = new ArrayList<>();
        for (String line : lines) lore.add(ColorUtil.parse(line));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !player.hasPermission("runeforged.admin") || event.getClickedInventory() != event.getView().getTopInventory()) return;
        int slot = event.getRawSlot();
        if (menu.id != null && menu.id.contains(":options:")) {
            String[] ids = menu.id.split(":options:", 2);
            String[] option = ids[1].split(":", 2);
            if (slot == 49) catalog(player, ids[0], option[0]);
            else if (slot == 45 && menu.page > 0) options(player, ids[0], option[0], option[1], menu.page - 1);
            else if (slot == 53 && (menu.page + 1) * 45 < choices(option[1]).size()) options(player, ids[0], option[0], option[1], menu.page + 1);
            else if (slot < 45 && menu.page * 45 + slot < choices(option[1]).size()) {
                String choice = choices(option[1]).get(menu.page * 45 + slot);
                String effect = switch (option[1]) {
                    case "POTION" -> "POTION:type=" + choice + ",ticks=100,level=1,target=SELF";
                    case "PARTICLE" -> "PARTICLE:type=" + choice + ",count=10,shape=POINT,radius=1";
                    default -> "PROJECTILE:type=" + choice + ",speed=1.5";
                };
                addEffect(player, ids[0], option[0], effect);
            }
            return;
        }
        if (menu.id != null && menu.id.contains(":catalog:")) {
            String[] ids = menu.id.split(":catalog:", 2);
            if (slot == 49) ability(player, ids[0], ids[1]);
            else if (slot >= 0 && slot < EFFECTS.length) addEffect(player, ids[0], ids[1], EFFECTS[slot]);
            else if (slot == 45) options(player, ids[0], ids[1], "POTION", 0);
            else if (slot == 46) options(player, ids[0], ids[1], "PARTICLE", 0);
            else if (slot == 47) options(player, ids[0], ids[1], "PROJECTILE", 0);
            return;
        }
        if (menu.id != null && menu.id.contains(":ability:")) {
            String[] parts = menu.id.split(":ability:", 2);
            String path = "runes." + parts[0] + ".abilities." + parts[1];
            if (slot == 49) { abilities(player, parts[0]); return; }
            if (slot == 1) {
                String old = plugin.getRuneRegistry().configuration().getString(path + ".trigger", TRIGGERS[0]);
                int index = java.util.Arrays.asList(TRIGGERS).indexOf(old);
                plugin.getRuneRegistry().configuration().set(path + ".trigger", TRIGGERS[(index + 1) % TRIGGERS.length]);
            } else if (slot == 2 || slot == 3) {
                String key = slot == 2 ? "chance" : "cooldown";
                double current = plugin.getRuneRegistry().configuration().getDouble(path + ".conditions." + key, slot == 2 ? 100 : 0);
                plugin.getRuneRegistry().configuration().set(path + ".conditions." + key, Math.max(0, Math.min(slot == 2 ? 100 : 3600, current + (event.isLeftClick() ? slot == 2 ? 5 : 1 : slot == 2 ? -5 : -1))));
            } else if (slot == 4 && event.isRightClick()) {
                List<String> effects = plugin.getRuneRegistry().configuration().getStringList(path + ".effects");
                if (!effects.isEmpty()) effects.remove(effects.size() - 1);
                plugin.getRuneRegistry().configuration().set(path + ".effects", effects);
            } else if (slot == 4) {
                catalog(player, parts[0], parts[1]);
                return;
            } else if (slot == 5 && event.isRightClick()) {
                List<String> description = plugin.getRuneRegistry().configuration().getStringList(path + ".description");
                if (!description.isEmpty()) description.remove(description.size() - 1);
                plugin.getRuneRegistry().configuration().set(path + ".description", description);
            } else if (slot == 5) {
                abilityDescriptionInput.put(player.getUniqueId(), path);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse("&eType a new ability description line in chat:"));
                return;
            } else if (slot == 6 && event.isRightClick()) {
                plugin.getRuneRegistry().configuration().set(path + ".action-bar", "");
            } else if (slot == 6) {
                abilityActionBarInput.put(player.getUniqueId(), path);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse("&eType the action bar message in chat (supports hex colors):"));
                return;
            } else if (slot >= 9 && slot < 45) {
                List<String> effects = plugin.getRuneRegistry().configuration().getStringList(path + ".effects");
                if (slot - 9 >= effects.size()) return;
                String effect = effects.get(slot - 9);
                if (event.isRightClick()) {
                    if (effect.startsWith("PARTICLE:") && effect.contains("shape=")) {
                        String[] shapes = {"POINT", "CIRCLE", "SPHERE", "SPIRAL"};
                        int shapeIndex = java.util.Arrays.asList(shapes).indexOf(effect.replaceAll(".*shape=([A-Z_]+).*", "$1"));
                        effects.set(slot - 9, effect.replaceFirst("shape=[A-Z_]+", "shape=" + shapes[(shapeIndex + 1) % shapes.length]));
                    } else if (effect.startsWith("POTION:") && effect.contains("target=")) {
                        effects.set(slot - 9, effect.contains("target=VICTIM") ? effect.replace("target=VICTIM", "target=SELF") : effect.replace("target=SELF", "target=VICTIM"));
                    } else return;
                    plugin.getRuneRegistry().configuration().set(path + ".effects", effects);
                    plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload(); ability(player, parts[0], parts[1]);
                    return;
                }
                String numeric = java.util.Arrays.stream(effect.substring(effect.indexOf(':') + 1).split(","))
                        .filter(p -> p.contains("=") && p.substring(p.indexOf('=') + 1).matches("[0-9]+(?:\\.[0-9]+)?"))
                        .map(p -> p.substring(0, p.indexOf('='))).collect(java.util.stream.Collectors.joining(", "));
                effectEdit.put(player.getUniqueId(), menu.id + ":" + (slot - 9));
                player.closeInventory();
                player.sendMessage(ColorUtil.parse(numeric.isEmpty() ? "&eType the replacement effect string in chat:" : "&eNumeric parameters: " + numeric + ". Type a parameter and value, e.g. radius 5"));
                return;
            } else return;
            plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload(); ability(player, parts[0], parts[1]);
            return;
        }
        if (menu.id != null && menu.id.endsWith(":abilities")) {
            String id = menu.id.substring(0, menu.id.length() - ":abilities".length());
            if (slot == 22) edit(player, id);
            else if (slot == 26) {
                newAbility.put(player.getUniqueId(), id);
                player.closeInventory();
                player.sendMessage(ColorUtil.parse("&eEnter a new ability ID in chat:"));
            } else {
                RuneRegistry.Definition rune = plugin.getRuneRegistry().get(id);
                if (rune != null && slot < rune.abilities().size()) ability(player, id, rune.abilities().get(slot).id());
            }
            return;
        }
        if (menu.id == null) {
            int perPage = menu.inventory.getSize() - 9;
            if (slot == perPage && menu.page > 0) open(player, menu.page - 1);
            else if (slot == menu.inventory.getSize() - 1 && (menu.page + 1) * perPage < plugin.getRuneRegistry().all().size()) open(player, menu.page + 1);
            else if (slot == menu.inventory.getSize() - 5) { create.put(player.getUniqueId(), "new"); player.closeInventory(); player.sendMessage(ColorUtil.parse("&eEnter a new rune ID (letters, digits and underscores):")); }
            else if (slot < perPage) {
                String id = plugin.getRuneManager().getRuneId(event.getCurrentItem());
                if (id != null) edit(player, id);
            }
            return;
        }
        RuneRegistry.Definition rune = plugin.getRuneRegistry().get(menu.id);
        if (rune == null) return;
        String path = "runes." + rune.id();
        if (slot == 22) { open(player, 0); return; }
        if (slot == 1 && event.isShiftClick()) {
            modelInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eEnter custom model data (0 to disable):"));
            return;
        }
        if (slot == 1 && event.isLeftClick()) {
            materialInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eType the base item material in chat, e.g. DIAMOND:"));
            return;
        } else if (slot == 2) {
            String stat = event.isShiftClick() ? "default-destroy-rate" : "default-success-rate";
            double rate = plugin.getRuneRegistry().configuration().getDouble(path + "." + stat);
            plugin.getRuneRegistry().configuration().set(path + "." + stat, Math.max(0, Math.min(100, rate + (event.isLeftClick() ? 5 : -5))));
        } else if (slot == 4) {
            rename.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eType the new display name in chat (supports &#RRGGBB):"));
            return;
        } else if (slot == 3) {
            abilities(player, rune.id());
            return;
        } else if (slot == 5) {
            int index = java.util.Arrays.asList(RARITIES).indexOf(rune.rarity().toUpperCase(java.util.Locale.ROOT));
            plugin.getRuneRegistry().configuration().set(path + ".rarity", RARITIES[(index + 1) % RARITIES.length]);
        } else if (slot == 6) {
            String old = rune.targets().isEmpty() ? "" : rune.targets().get(0).toUpperCase(java.util.Locale.ROOT);
            int index = java.util.Arrays.asList(TARGETS).indexOf(old);
            plugin.getRuneRegistry().configuration().set(path + ".target-items", List.of(TARGETS[(index + 1) % TARGETS.length]));
        } else if (slot == 7) {
            RuneCategory[] categories = RuneCategory.values();
            int index = java.util.Arrays.stream(categories).map(Enum::name).toList().indexOf(rune.category().toUpperCase(java.util.Locale.ROOT));
            plugin.getRuneRegistry().configuration().set(path + ".category", categories[(index + 1) % categories.length].name());
        } else if (slot == 8) {
            String[] modes = {"VAULT", "COMMANDS", "DISABLED"};
            int index = java.util.Arrays.asList(modes).indexOf(plugin.getRuneRegistry().configuration().getString(path + ".purchase.mode", "VAULT").toUpperCase(java.util.Locale.ROOT));
            plugin.getRuneRegistry().configuration().set(path + ".purchase.mode", modes[(index + 1) % modes.length]);
        } else if (slot == 9) {
            priceInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eEnter the purchase price in chat (0 or greater):"));
            return;
        } else if (slot == 10) {
            balanceInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eEnter a numeric PlaceholderAPI balance placeholder (e.g. %credits_balance%):"));
            return;
        } else if (slot == 11 && event.isRightClick()) {
            List<String> commands = plugin.getRuneRegistry().configuration().getStringList(path + ".purchase.commands");
            if (!commands.isEmpty()) commands.remove(commands.size() - 1);
            plugin.getRuneRegistry().configuration().set(path + ".purchase.commands", commands);
        } else if (slot == 11 && event.isLeftClick()) {
            commandInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eEnter one console charge command, e.g. credit take %player% 1:"));
            return;
        } else if (slot == 12 && event.isRightClick()) {
            List<String> lore = plugin.getRuneRegistry().configuration().getStringList(path + ".lore");
            if (!lore.isEmpty()) lore.remove(lore.size() - 1);
            plugin.getRuneRegistry().configuration().set(path + ".lore", lore);
        } else if (slot == 12 && event.isLeftClick()) {
            loreInput.put(player.getUniqueId(), rune.id());
            player.closeInventory();
            player.sendMessage(ColorUtil.parse("&eType the next lore line in chat (use & codes or &#RRGGBB):"));
            return;
        } else return;
        plugin.getRuneRegistry().save();
        plugin.getRuneRegistry().reload();
        edit(player, rune.id());
    }

    @EventHandler public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String id = rename.remove(uuid);
        boolean creating = create.remove(uuid) != null;
        String parent = newAbility.remove(uuid);
        String editingEffect = effectEdit.remove(uuid);
        String modelId = modelInput.remove(uuid);
        String materialId = materialInput.remove(uuid);
        String priceId = priceInput.remove(uuid);
        String balanceId = balanceInput.remove(uuid);
        String commandId = commandInput.remove(uuid);
        String loreId = loreInput.remove(uuid);
        String descriptionPath = abilityDescriptionInput.remove(uuid);
        String actionBarPath = abilityActionBarInput.remove(uuid);
        if (id == null && !creating && parent == null && editingEffect == null && modelId == null && materialId == null && priceId == null && balanceId == null && commandId == null && loreId == null && descriptionPath == null && actionBarPath == null) return;
        event.setCancelled(true);
        String text = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (editingEffect != null) {
                String[] ids = editingEffect.split(":ability:|:", 3);
                String[] indexParts = editingEffect.substring(editingEffect.indexOf(":ability:") + 9).split(":", 2);
                String path = "runes." + ids[0] + ".abilities." + indexParts[0] + ".effects";
                List<String> effects = plugin.getRuneRegistry().configuration().getStringList(path);
                int index = Integer.parseInt(indexParts[1]);
                if (index >= effects.size()) return;
                String old = effects.get(index);
                if (java.util.Arrays.stream(old.substring(old.indexOf(':') + 1).split(",")).noneMatch(p -> p.matches("[a-z_]+=[0-9]+(?:\\.[0-9]+)?"))) {
                    if (text.isBlank() || !text.contains(":") || text.length() > 240) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a valid effect string (TYPE:arguments).")); return; }
                    effects.set(index, text.trim());
                } else {
                    String[] change = text.trim().split("\\s+", 2);
                    if (change.length != 2 || !change[0].matches("[a-z_]+")) { event.getPlayer().sendMessage(ColorUtil.parse("&cUse parameter value, e.g. radius 5.")); return; }
                    double value;
                    try { value = Double.parseDouble(change[1]); } catch (NumberFormatException e) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a number.")); return; }
                    if (!Double.isFinite(value) || value < 0) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a nonnegative finite number.")); return; }
                    if (!old.matches(".*(?:^|[:,])" + change[0] + "=[0-9]+(?:\\.[0-9]+)?.*")) { event.getPlayer().sendMessage(ColorUtil.parse("&cThat numeric parameter is not on this effect.")); return; }
                    effects.set(index, old.replaceFirst("(?<=[: ,])" + change[0] + "=[0-9]+(?:\\.[0-9]+)?", change[0] + "=" + change[1]));
                }
                plugin.getRuneRegistry().configuration().set(path, effects);
                plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload(); ability(event.getPlayer(), ids[0], indexParts[0]);
            } else if (loreId != null) {
                if (text.length() > 200) { event.getPlayer().sendMessage(ColorUtil.parse("&cLore lines may be at most 200 characters.")); return; }
                List<String> lore = plugin.getRuneRegistry().configuration().getStringList("runes." + loreId + ".lore");
                lore.add(text);
                plugin.getRuneRegistry().configuration().set("runes." + loreId + ".lore", lore);
                idForEdit(event.getPlayer(), loreId);
            } else if (descriptionPath != null || actionBarPath != null) {
                if (text.length() > 200) { event.getPlayer().sendMessage(ColorUtil.parse("&cText may be at most 200 characters.")); return; }
                String path = descriptionPath != null ? descriptionPath : actionBarPath;
                if (descriptionPath != null) {
                    List<String> description = plugin.getRuneRegistry().configuration().getStringList(path + ".description");
                    description.add(text);
                    plugin.getRuneRegistry().configuration().set(path + ".description", description);
                } else plugin.getRuneRegistry().configuration().set(path + ".action-bar", text);
                plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload();
                String[] parts = path.substring("runes.".length()).split("\\.abilities\\.", 2);
                ability(event.getPlayer(), parts[0], parts[1]);
            } else if (priceId != null) {
                try {
                    double price = Double.parseDouble(text.trim());
                    if (!Double.isFinite(price) || price < 0) throw new NumberFormatException();
                    plugin.getRuneRegistry().configuration().set("runes." + priceId + ".purchase.price", price);
                    idForEdit(event.getPlayer(), priceId);
                } catch (NumberFormatException e) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a nonnegative finite price.")); }
            } else if (balanceId != null) {
                if (!text.matches("%[A-Za-z0-9_]+%")) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a placeholder like %credits_balance%.")); return; }
                plugin.getRuneRegistry().configuration().set("runes." + balanceId + ".purchase.balance-placeholder", text);
                idForEdit(event.getPlayer(), balanceId);
            } else if (commandId != null) {
                String command = text.trim().replaceFirst("^/", "");
                if (!command.matches("[A-Za-z0-9_:-]+(?: .*)?") || command.length() > 240) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a valid console command.")); return; }
                List<String> commands = plugin.getRuneRegistry().configuration().getStringList("runes." + commandId + ".purchase.commands");
                commands.add(command);
                plugin.getRuneRegistry().configuration().set("runes." + commandId + ".purchase.commands", commands);
                idForEdit(event.getPlayer(), commandId);
            } else if (materialId != null) {
                String name = text.trim().toUpperCase(java.util.Locale.ROOT);
                Material material = XMaterial.matchXMaterial(name).map(XMaterial::parseMaterial).orElse(null);
                if (material == null || !material.isItem() || material == Material.AIR) {
                    event.getPlayer().sendMessage(ColorUtil.parse("&cInvalid item material. Use a name like DIAMOND."));
                    return;
                }
                plugin.getRuneRegistry().configuration().set("runes." + materialId + ".base-item", material.name());
                idForEdit(event.getPlayer(), materialId);
            } else if (modelId != null) {
                try {
                    int model = Integer.parseInt(text);
                    if (model < 0) throw new NumberFormatException();
                    plugin.getRuneRegistry().configuration().set("runes." + modelId + ".custom-model-data", model);
                    idForEdit(event.getPlayer(), modelId);
                } catch (NumberFormatException e) { event.getPlayer().sendMessage(ColorUtil.parse("&cEnter a nonnegative number.")); }
            } else if (parent != null) {
                if (!text.matches("[A-Za-z][A-Za-z0-9_]{0,40}") || plugin.getRuneRegistry().configuration().contains("runes." + parent + ".abilities." + text)) {
                    event.getPlayer().sendMessage(ColorUtil.parse("&cInvalid or duplicate ability ID.")); return;
                }
                plugin.getRuneRegistry().configuration().set("runes." + parent + ".abilities." + text + ".trigger", TRIGGERS[0]);
                plugin.getRuneRegistry().configuration().set("runes." + parent + ".abilities." + text + ".effects", List.of());
                plugin.getRuneRegistry().configuration().set("runes." + parent + ".abilities." + text + ".description", List.of());
                plugin.getRuneRegistry().configuration().set("runes." + parent + ".abilities." + text + ".action-bar", "");
                plugin.getRuneRegistry().save(); plugin.getRuneRegistry().reload(); ability(event.getPlayer(), parent, text);
            } else if (creating) {
                if (!text.matches("[A-Za-z][A-Za-z0-9_]{0,40}") || plugin.getRuneRegistry().get(text) != null) { event.getPlayer().sendMessage(ColorUtil.parse("&cInvalid or duplicate rune ID.")); return; }
                String path = "runes." + text;
                plugin.getRuneRegistry().configuration().set(path + ".display-name", text);
                plugin.getRuneRegistry().configuration().set(path + ".base-item", "NETHER_STAR");
                plugin.getRuneRegistry().configuration().set(path + ".rarity", "COMMON");
                plugin.getRuneRegistry().configuration().set(path + ".category", "CUSTOM");
                plugin.getRuneRegistry().configuration().set(path + ".target-items", List.of("SWORD"));
                plugin.getRuneRegistry().configuration().set(path + ".lore", List.of("&7A newly forged rune", "&aSuccess: {success}% &cDestroy: {destroy}%"));
                plugin.getRuneRegistry().configuration().set(path + ".default-success-rate", 50);
                plugin.getRuneRegistry().configuration().set(path + ".default-destroy-rate", 0);
                plugin.getRuneRegistry().configuration().set(path + ".purchase.mode", "VAULT");
                plugin.getRuneRegistry().configuration().set(path + ".purchase.price", 100);
                int nextModel = plugin.getRuneRegistry().all().stream().mapToInt(RuneRegistry.Definition::modelData).max().orElse(1000) + 1;
                plugin.getRuneRegistry().configuration().set(path + ".custom-model-data", nextModel);
                idForEdit(event.getPlayer(), text);
            } else {
                plugin.getRuneRegistry().configuration().set("runes." + id + ".display-name", text);
                idForEdit(event.getPlayer(), id);
            }
        });
    }

    private void idForEdit(Player player, String id) {
        plugin.getRuneRegistry().save();
        plugin.getRuneRegistry().reload();
        edit(player, id);
    }
}