package me.gauravbuilds.runeforgedrunes;

public enum RuneType {

    // --- Category 1: Combat ---
    VAMPIRE("Vampire", RuneCategory.COMBAT, RuneRarity.MYTHIC, RuneTarget.MELEE_WEAPON, "Chance to heal 1-2 HP on hit."),
    EXECUTIONER("Executioner", RuneCategory.COMBAT, RuneRarity.LEGENDARY, RuneTarget.MELEE_WEAPON, "Deal +50% damage if enemy HP < 20%."),
    VENOM("Venom", RuneCategory.COMBAT, RuneRarity.RARE, RuneTarget.MELEE_WEAPON, "Apply Poison II (3s) on hit."),
    FROSTBITE("Frostbite", RuneCategory.COMBAT, RuneRarity.RARE, RuneTarget.MELEE_WEAPON, "Apply Slowness II (3s) on hit."),
    THUNDERSTRIKE("Thunderstrike", RuneCategory.COMBAT, RuneRarity.LEGENDARY, RuneTarget.MELEE_WEAPON, "10% chance to strike lightning on hit."),
    TANK("Tank", RuneCategory.COMBAT, RuneRarity.RARE, RuneTarget.ARMOR, "-5% incoming damage."),
    REFLECT("Reflect", RuneCategory.COMBAT, RuneRarity.LEGENDARY, RuneTarget.ARMOR, "Attacker takes 10% of damage dealt."),
    ADRENALINE("Adrenaline", RuneCategory.COMBAT, RuneRarity.RARE, RuneTarget.ARMOR, "Gain Speed II (5s) when HP drops below 3 hearts."),
    SNIPER("Sniper", RuneCategory.COMBAT, RuneRarity.RARE, RuneTarget.BOW, "Arrows deal +20% damage."),
    PARRY("Parry", RuneCategory.COMBAT, RuneRarity.COMMON, RuneTarget.MELEE_WEAPON, "5% chance to block an attack (0 damage)."),

    // --- Category 2: Mining ---
    VEIN_MINER("Vein Miner", RuneCategory.MINING, RuneRarity.MYTHIC, RuneTarget.PICKAXE, "Break one ore, break the whole vein (Sneak to activate)."),
    SMELTER("Smelter", RuneCategory.MINING, RuneRarity.RARE, RuneTarget.PICKAXE, "Auto-smelt Iron and Gold ore drops."),
    HASTE("Haste", RuneCategory.MINING, RuneRarity.LEGENDARY, RuneTarget.PICKAXE, "Passive Haste II while holding item."),
    GEM_FINDER("Gem Finder", RuneCategory.MINING, RuneRarity.MYTHIC, RuneTarget.PICKAXE, "0.1% chance to drop a Diamond when mining Stone."),
    LIGHT("Light", RuneCategory.MINING, RuneRarity.COMMON, RuneTarget.PICKAXE, "Passive Night Vision while holding."),
    OBSIDIAN_BREAKER("Obsidian Breaker", RuneCategory.MINING, RuneRarity.RARE, RuneTarget.PICKAXE, "Break Obsidian 3x faster."),
    EXPERIENCE("Experience", RuneCategory.MINING, RuneRarity.RARE, RuneTarget.PICKAXE, "Multiplies XP drops from ores by 2x."),
    MAGNET("Magnet", RuneCategory.MINING, RuneRarity.RARE, RuneTarget.PICKAXE, "Mined items go directly to inventory."),
    TUNNEL("Tunnel", RuneCategory.MINING, RuneRarity.LEGENDARY, RuneTarget.PICKAXE, "3x3 Mining mode (Cooldown: 10s, Sneak+Click)."),
    DURABILITY("Durability", RuneCategory.MINING, RuneRarity.COMMON, RuneTarget.PICKAXE, "10% chance to ignore durability loss."),

    // --- Category 3: Farming ---
    HARVESTER("Harvester", RuneCategory.FARMING, RuneRarity.LEGENDARY, RuneTarget.HOE, "Harvest crops in a 3x3 area."),
    REPLANT("Replant", RuneCategory.FARMING, RuneRarity.COMMON, RuneTarget.HOE, "Auto-replant seeds after harvesting fully grown crops."),
    FERTILIZER("Fertilizer", RuneCategory.FARMING, RuneRarity.RARE, RuneTarget.HOE, "Right-click crops to apply bonemeal effect (Uses bonemeal from inv)."),
    GREEN_THUMB("Green Thumb", RuneCategory.FARMING, RuneRarity.RARE, RuneTarget.HOE, "+20% crop yield."),
    SICKLE("Sickle", RuneCategory.FARMING, RuneRarity.RARE, RuneTarget.HOE, "Breaks leaves/crops in a 3 block radius."),
    PUMPKIN_KING("Pumpkin King", RuneCategory.FARMING, RuneRarity.RARE, RuneTarget.HOE, "+50% drops from Pumpkins/Melons."),
    LIVESTOCK("Livestock", RuneCategory.FARMING, RuneRarity.LEGENDARY, RuneTarget.HOE, "10% chance for animals to drop 2x loot."),
    HYDRATION("Hydration", RuneCategory.FARMING, RuneRarity.COMMON, RuneTarget.HOE, "Farmland never turns to dirt (trample protection)."),
    SUNLIGHT("Sunlight", RuneCategory.FARMING, RuneRarity.RARE, RuneTarget.HOE, "Crops grow faster in a 5 block radius (Passive tick)."),
    GOLD_RUSH("Gold Rush", RuneCategory.FARMING, RuneRarity.MYTHIC, RuneTarget.HOE, "0.05% chance to find Golden Nuggets when harvesting crops."),

    // --- Category 4: Foraging ---
    TIMBER("Timber", RuneCategory.FORAGING, RuneRarity.MYTHIC, RuneTarget.AXE, "Treecapitator effect (Break bottom log, whole tree falls)."),
    LEAF_BLOWER("Leaf Blower", RuneCategory.FORAGING, RuneRarity.RARE, RuneTarget.AXE, "Instantly break leaves in 3x3 area."),
    APPLE_FINDER("Apple Finder", RuneCategory.FORAGING, RuneRarity.COMMON, RuneTarget.AXE, "+10% chance for Apples from Oak Leaves."),
    SAPLING_PLANTER("Sapling Planter", RuneCategory.FORAGING, RuneRarity.COMMON, RuneTarget.AXE, "Auto-replant sapling when tree breaks."),
    WOOD_CHIPPER("Wood Chipper", RuneCategory.FORAGING, RuneRarity.RARE, RuneTarget.AXE, "10% chance for double log drops."),
    SPEED_CHOP("Speed Chop", RuneCategory.FORAGING, RuneRarity.RARE, RuneTarget.AXE, "Haste II while holding Axe."),
    SILK_TOUCH("Silk Touch", RuneCategory.FORAGING, RuneRarity.LEGENDARY, RuneTarget.AXE, "Silk Touch effect (stacks with vanilla)."),
    CHARCOAL("Charcoal", RuneCategory.FORAGING, RuneRarity.RARE, RuneTarget.AXE, "Drops Charcoal instead of Logs."),
    NATURES_GIFT("Nature's Gift", RuneCategory.FORAGING, RuneRarity.LEGENDARY, RuneTarget.AXE, "Chance to drop random flowers while chopping."),
    BARK_STRIPPER("Bark Stripper", RuneCategory.FORAGING, RuneRarity.COMMON, RuneTarget.AXE, "Shift+Right Click strips all logs in 3x3 area."),

    // --- Category 5: Enchanting ---
    SOULBOUND("Soulbound", RuneCategory.ENCHANTING, RuneRarity.MYTHIC, RuneTarget.ALL, "Keep item on death (Rune is consumed on death)."),
    UNBREAKING_IV("Unbreaking IV", RuneCategory.ENCHANTING, RuneRarity.LEGENDARY, RuneTarget.ALL, "Adds 'Unbreaking IV' equivalent (Item lasts longer)."),
    KNOWLEDGE("Knowledge", RuneCategory.ENCHANTING, RuneRarity.RARE, RuneTarget.ALL, "+50% XP from all sources while holding."),
    REPAIR("Repair", RuneCategory.ENCHANTING, RuneRarity.LEGENDARY, RuneTarget.ALL, "Auto-repair item using XP (Mending style, independent of vanilla)."),
    EXTRACTION("Extraction", RuneCategory.ENCHANTING, RuneRarity.RARE, RuneTarget.PICKAXE, "10% Chance to mine a Spawner and get the item."),
    INSIGHT("Insight", RuneCategory.ENCHANTING, RuneRarity.RARE, RuneTarget.ALL, "Display mob health in Action Bar when looking at them."),
    GLOW("Glow", RuneCategory.ENCHANTING, RuneRarity.COMMON, RuneTarget.ALL, "Player glows (Visual effect)."),
    LUCK("Luck", RuneCategory.ENCHANTING, RuneRarity.RARE, RuneTarget.ALL, "+1 Luck attribute."),
    SCHOLAR("Scholar", RuneCategory.ENCHANTING, RuneRarity.COMMON, RuneTarget.ALL, "Reduces Anvil repair costs by 20%."),
    PRESERVATION("Preservation", RuneCategory.ENCHANTING, RuneRarity.LEGENDARY, RuneTarget.ALL, "If item breaks, it stays at 1 durability and Rune breaks instead."),

    // --- Category 6: Sorcery ---
    HERMES("Hermes", RuneCategory.SORCERY, RuneRarity.MYTHIC, RuneTarget.BOOTS, "Passive Speed II."),
    SPRING("Spring", RuneCategory.SORCERY, RuneRarity.RARE, RuneTarget.BOOTS, "Passive Jump Boost II."),
    FEATHERWEIGHT("Featherweight", RuneCategory.SORCERY, RuneRarity.RARE, RuneTarget.BOOTS, "No Fall Damage."),
    DOLPHIN("Dolphin", RuneCategory.SORCERY, RuneRarity.RARE, RuneTarget.HELMET, "Dolphin's Grace in water."),
    OXYGEN("Oxygen", RuneCategory.SORCERY, RuneRarity.LEGENDARY, RuneTarget.HELMET, "Water Breathing."),
    FIREWALKER("Firewalker", RuneCategory.SORCERY, RuneRarity.MYTHIC, RuneTarget.LEGGINGS, "Fire Resistance."),
    SATURATION("Saturation", RuneCategory.SORCERY, RuneRarity.LEGENDARY, RuneTarget.HELMET, "Hunger drains 50% slower."),
    INVISIBILITY("Invisibility", RuneCategory.SORCERY, RuneRarity.RARE, RuneTarget.CHESTPLATE, "Shift to become Invisible (Armor hidden)."),
    NIGHT_EYE("Night Eye", RuneCategory.SORCERY, RuneRarity.COMMON, RuneTarget.HELMET, "Night Vision."),
    DRAGON_FLY("Dragon Fly", RuneCategory.SORCERY, RuneRarity.MYTHIC, RuneTarget.BOOTS, "Double-Jump to launch forward (Cooldown: 10s).");

    private final String displayName;
    private final RuneCategory category;
    private final RuneRarity rarity;
    private final RuneTarget target;
    private final String description;

    RuneType(String displayName, RuneCategory category, RuneRarity rarity, RuneTarget target, String description) {
        this.displayName = displayName;
        this.category = category;
        this.rarity = rarity;
        this.target = target;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public RuneCategory getCategory() {
        return category;
    }

    public RuneRarity getRarity() {
        return rarity;
    }

    public RuneTarget getTarget() {
        return target;
    }

    public String getDescription() {
        return description;
    }
}