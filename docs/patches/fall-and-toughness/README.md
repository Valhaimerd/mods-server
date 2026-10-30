# Feather Falling reduction and Toughness retirement

Feather Falling now grants **6% fall-damage reduction per level**, reaching **24% at IV**, using 1.5 native protection points per level. This is the enchantment layer; other fall-protection sources can still contribute through Minecraft?s normal calculation. The preceding five protection-enchantment changes remain intact.

Removed `toughness-enchantment-v1.0.2.jar`, the standalone [Toughness Enchantment](https://modrinth.com/datapack/toughness-enchantment) data mod. Its sole enchantment, acquisition tags and three book loot tables leave the active pack together. No other installed mod has a data or metadata reference to its registry ID. Equipment?s ordinary armor toughness is unaffected.

Penchant retains its active filename and receives the Feather Falling override plus the existing retirement-codec extension. Both equipped and stored enchantment components, along with Penchant progress, discard `toughness_enchantment:toughness` on load while preserving other entries and the item. Books containing only Toughness retain the book item without that enchantment. This takes effect when saved items are decoded with the updated pack; personal worlds were not modified.

Verification: **21/21 native NeoForge 26.2.0.90 server checks pass**, covering Feather Falling I?IV damage protection, no protection against generic damage, missing Toughness registry/mod, absent Toughness creative books, and JSON/NBT cleanup of saved gear/books and progress. `artifacts.json` records installed hashes and archive changes. Originals are retained in ignored `mod fixing/retired packs/fall-and-toughness-2026-10-30/`.
