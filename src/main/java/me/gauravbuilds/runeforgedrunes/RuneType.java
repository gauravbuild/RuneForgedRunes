package me.gauravbuilds.runeforgedrunes;

public enum RuneType {

    // --- Category 1: Combat ---
    VAMPIRE("Vampire", RuneRarity.MYTHIC, RuneTarget.MELEE_WEAPON, "Chance to heal 1-2 HP on hit."),
    EXECUTIONER("Executioner", RuneRarity.LEGENDARY, RuneTarget.MELEE_WEAPON, "Deal +50% damage if enemy HP < 20%."),
    VENOM("Venom", RuneRarity.RARE, RuneTarget.MELEE_WEAPON, "Apply Poison II (3s) on hit."),
    FROSTBITE("Frostbite", RuneRarity.RARE, RuneTarget.MELEE_WEAPON, "Apply Slowness II (3s) on hit."),
    THUNDERSTRIKE("Thunderstrike", RuneRarity.LEGENDARY, RuneTarget.MELEE_WEAPON, "10% chance to strike lightning on hit."),
    TANK("Tank", RuneRarity.RARE, RuneTarget.ARMOR, "-5% incoming damage."),
    REFLECT("Reflect", RuneRarity.LEGENDARY, RuneTarget.ARMOR, "Attacker takes 10% of damage dealt."),
    ADRENALINE("Adrenaline", RuneRarity.RARE, RuneTarget.ARMOR, "Gain Speed II (5s) when HP drops below 3 hearts."),
    SNIPER("Sniper", RuneRarity.RARE, RuneTarget.BOW, "Arrows deal +20% damage."),
    PARRY("Parry", RuneRarity.COMMON, RuneTarget.MELEE_WEAPON, "5% chance to block an attack (0 damage)."),

    // --- Category 2: Mining ---
    VEIN_MINER("Vein Miner", RuneRarity.MYTHIC, RuneTarget.PICKAXE, "Break one ore, break the whole vein (Sneak to activate)."),
    SMELTER("Smelter", RuneRarity.RARE, RuneTarget.PICKAXE, "Auto-smelt Iron and Gold ore drops."),
    HASTE("Haste", RuneRarity.LEGENDARY, RuneTarget.PICKAXE, "Passive Haste II while holding item."),
    GEM_FINDER("Gem Finder", RuneRarity.MYTHIC, RuneTarget.PICKAXE, "0.1% chance to drop a Diamond when mining Stone."),
    LIGHT("Light", RuneRarity.COMMON, RuneTarget.PICKAXE, "Passive Night Vision while holding."),
    OBSIDIAN_BREAKER("Obsidian Breaker", RuneRarity.RARE, RuneTarget.PICKAXE, "Break Obsidian 3x faster."),
    EXPERIENCE("Experience", RuneRarity.RARE, RuneTarget.PICKAXE, "Multiplies XP drops from ores by 2x."),
    MAGNET("Magnet", RuneRarity.RARE, RuneTarget.PICKAXE, "Mined items go directly to inventory."),
    TUNNEL("Tunnel", RuneRarity.LEGENDARY, RuneTarget.PICKAXE, "3x3 Mining mode (Cooldown: 10s, Sneak+Click)."),
    DURABILITY("Durability", RuneRarity.COMMON, RuneTarget.PICKAXE, "10% chance to ignore durability loss."),

    // --- Category 3: Farming ---
    HARVESTER("Harvester", RuneRarity.LEGENDARY, RuneTarget.HOE, "Harvest crops in a 3x3 area."),
    REPLANT("Replant", RuneRarity.COMMON, RuneTarget.HOE, "Auto-replant seeds after harvesting fully grown crops."),
    FERTILIZER("Fertilizer", RuneRarity.RARE, RuneTarget.HOE, "Right-click crops to apply bonemeal effect (Uses bonemeal from inv)."),
    GREEN_THUMB("Green Thumb", RuneRarity.RARE, RuneTarget.HOE, "+20% crop yield."),
    SICKLE("Sickle", RuneRarity.RARE, RuneTarget.HOE, "Breaks leaves/crops in a 3 block radius."),
    PUMPKIN_KING("Pumpkin King", RuneRarity.RARE, RuneTarget.HOE, "+50% drops from Pumpkins/Melons."),
    LIVESTOCK("Livestock", RuneRarity.LEGENDARY, RuneTarget.HOE, "10% chance for animals to drop 2x loot."),
    HYDRATION("Hydration", RuneRarity.COMMON, RuneTarget.HOE, "Farmland never turns to dirt (trample protection)."),
    SUNLIGHT("Sunlight", RuneRarity.RARE, RuneTarget.HOE, "Crops grow faster in a 5 block radius (Passive tick)."),
    GOLD_RUSH("Gold Rush", RuneRarity.MYTHIC, RuneTarget.HOE, "0.05% chance to find Golden Nuggets when harvesting crops."),

    // --- Category 4: Foraging ---
    TIMBER("Timber", RuneRarity.MYTHIC, RuneTarget.AXE, "Treecapitator effect (Break bottom log, whole tree falls)."),
    LEAF_BLOWER("Leaf Blower", RuneRarity.RARE, RuneTarget.AXE, "Instantly break leaves in 3x3 area."),
    APPLE_FINDER("Apple Finder", RuneRarity.COMMON, RuneTarget.AXE, "+10% chance for Apples from Oak Leaves."),
    SAPLING_PLANTER("Sapling Planter", RuneRarity.COMMON, RuneTarget.AXE, "Auto-replant sapling when tree breaks."),
    WOOD_CHIPPER("Wood Chipper", RuneRarity.RARE, RuneTarget.AXE, "10% chance for double log drops."),
    SPEED_CHOP("Speed Chop", RuneRarity.RARE, RuneTarget.AXE, "Haste II while holding Axe."),
    SILK_TOUCH("Silk Touch", RuneRarity.LEGENDARY, RuneTarget.AXE, "Silk Touch effect (stacks with vanilla)."),
    CHARCOAL("Charcoal", RuneRarity.RARE, RuneTarget.AXE, "Drops Charcoal instead of Logs."),
    NATURES_GIFT("Nature's Gift", RuneRarity.LEGENDARY, RuneTarget.AXE, "Chance to drop random flowers while chopping."),
    BARK_STRIPPER("Bark Stripper", RuneRarity.COMMON, RuneTarget.AXE, "Shift+Right Click strips all logs in 3x3 area."),

    // --- Category 5: Enchanting ---
    SOULBOUND("Soulbound", RuneRarity.MYTHIC, RuneTarget.ALL, "Keep item on death (Rune is consumed on death)."),
    UNBREAKING_IV("Unbreaking IV", RuneRarity.LEGENDARY, RuneTarget.ALL, "Adds 'Unbreaking IV' equivalent (Item lasts longer)."),
    KNOWLEDGE("Knowledge", RuneRarity.RARE, RuneTarget.ALL, "+50% XP from all sources while holding."),
    REPAIR("Repair", RuneRarity.LEGENDARY, RuneTarget.ALL, "Auto-repair item using XP (Mending style, independent of vanilla)."),
    EXTRACTION("Extraction", RuneRarity.RARE, RuneTarget.PICKAXE, "10% Chance to mine a Spawner and get the item."),
    INSIGHT("Insight", RuneRarity.RARE, RuneTarget.ALL, "Display mob health in Action Bar when looking at them."),
    GLOW("Glow", RuneRarity.COMMON, RuneTarget.ALL, "Player glows (Visual effect)."),
    LUCK("Luck", RuneRarity.RARE, RuneTarget.ALL, "+1 Luck attribute."),
    SCHOLAR("Scholar", RuneRarity.COMMON, RuneTarget.ALL, "Reduces Anvil repair costs by 20%."),
    PRESERVATION("Preservation", RuneRarity.LEGENDARY, RuneTarget.ALL, "If item breaks, it stays at 1 durability and Rune breaks instead."),

    // --- Category 6: Sorcery ---
    HERMES("Hermes", RuneRarity.MYTHIC, RuneTarget.BOOTS, "Passive Speed II."),
    SPRING("Spring", RuneRarity.RARE, RuneTarget.BOOTS, "Passive Jump Boost II."),
    FEATHERWEIGHT("Featherweight", RuneRarity.RARE, RuneTarget.BOOTS, "No Fall Damage."),
    DOLPHIN("Dolphin", RuneRarity.RARE, RuneTarget.HELMET, "Dolphin's Grace in water."),
    OXYGEN("Oxygen", RuneRarity.LEGENDARY, RuneTarget.HELMET, "Water Breathing."),
    FIREWALKER("Firewalker", RuneRarity.MYTHIC, RuneTarget.LEGGINGS, "Fire Resistance."),
    SATURATION("Saturation", RuneRarity.LEGENDARY, RuneTarget.HELMET, "Hunger drains 50% slower."),
    INVISIBILITY("Invisibility", RuneRarity.RARE, RuneTarget.CHESTPLATE, "Shift to become Invisible (Armor hidden)."),
    NIGHT_EYE("Night Eye", RuneRarity.COMMON, RuneTarget.HELMET, "Night Vision."),
    DRAGON_FLY("Dragon Fly", RuneRarity.MYTHIC, RuneTarget.BOOTS, "Double-Jump to launch forward (Cooldown: 10s).");

    private final String displayName;
    private final RuneRarity rarity;
    private final RuneTarget target;
    private final String description;

    RuneType(String displayName, RuneRarity rarity, RuneTarget target, String description) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.target = target;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
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
