<div align="center">
  <h1>✦ RuneForgedRunes ✦</h1>
  <p><b>Build custom rune abilities in-game for your Minecraft server.</b></p>
</div>

RuneForgedRunes lets players socket magical runes into equipment. Admins can create runes, assign their triggers and effects, and manage the player shop without writing Java. The plugin ships with 60 editable runes across Combat, Mining, Farming, Foraging, Enchanting, and Sorcery, plus a Custom category for your own creations.

## What can runes do?

- React to sword swings, attacks, damage, mining, bow hits, fishing casts and reels, right-clicks, sneaking, double-jumps, and more.
- Create effects such as chain lightning, a black hole, a meteor strike, vein mining, timber chopping, projectiles, potions, particles, and sounds.
- Show readable ability descriptions on rune items and colored action-bar feedback when abilities activate.
- Support custom ability triggers, conditions, cooldowns, and effect parameters in `runes.yml`.

To apply a rune, pick it up in your inventory and click it onto compatible equipment. A Stardust Catalyst can improve an unapplied rune's success rate. The Forge and Salvage menus offer additional ways to manage rune equipment.

## Getting started

1. Put the plugin JAR in your server's `plugins/` folder and restart the server.
2. Open `/runeshop` as a player or `/rune editor` as an admin.
3. Edit `plugins/RuneForgedRunes/rune-shop.yml` to select the runes sold in the shop. Changes take effect the next time the shop opens.
4. Customize rune abilities and per-rune purchase settings in `runes.yml`, and menu presentation in `config.yml`.
5. Install Vault and an economy provider for money purchases. PlaceholderAPI is needed for command-based custom currencies.

For custom currencies, configure the offer or rune with `mode: COMMANDS`, `price`, a numeric `balance-placeholder`, and console charge commands. For example, `credit take %player% %price%` deducts the configured cost. Make sure the placeholder and charge command refer to the same currency.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/runeshop` | Open the paginated player shop. | None |
| `/runes`, `/rune` | Open the shop for players or the repository for admins. | None |
| `/runes open <category>` | Open an admin repository category. | `runeforged.admin` |
| `/rune editor` | Create and edit runes in-game. | `runeforged.admin` |
| `/rune list` | Open the admin repository (lists runes in console). | `runeforged.admin` |
| `/rune give <player> <category> <rune> <chance> <amount>` | Give runes. | `runeforged.admin` |
| `/rune giveorb <player> <rarity> <amount>` | Give rune orbs; `CUSTOM` rolls Custom-category runes. | `runeforged.admin` |
| `/runeforge` | Open the Forge. | None |
| `/runesalvage` | Open Salvage. | None |

Applying runes directly through inventory clicks requires the `runeforgedrunes.apply.dragdrop` permission (enabled by default). The admin repository gives runes directly, without charging shop prices.

The plugin includes English, Russian, Japanese, Chinese, French, Spanish, Korean, German, Portuguese, Polish, and Arabic locale files. Select a language with `locale` in `config.yml`.
