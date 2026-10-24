# Enchantment retirement — 2026-10-24

Barricades and Life Steal are removed. Air Jump, Protection Grace and Tense were also standalone providers of retired enchantments and are removed. The active pack contains 304 server-compatible JARs and 24 client extras.

The 14 formerly hidden modded enchantments are removed from the enchantment registry, not merely hidden: Air Jump, Protection Grace, Tense, Antidote, Traveler, Ghasted, Spiteful, Soulbound, Withering, Decay, Penetrating Strike, Bombard, Deflector and Recycling. Life Steal is retired too. Vanilla Sweeping Edge remains registered and hidden by Penchant.

Five same-name repacks update Better Archeology, CrossArms, Dungeons and Taverns, Ender Zoology and Penchant. They remove enchantment definitions and acquisition references, including explicit loot enchantments and tags. Penetrating Strike's creative book and damage hook are disabled. Ender Zoology's death hook no longer looks up Soulbound. Function IDs are separate from enchantment IDs: Gravity's `nova_structures:ghasted` function remains intact.

Penchant strips only these 15 retired IDs when reading equipped or stored enchantment components. Existing armor, weapons and books retain their other enchantments. Penchant enchantment progress receives the same cleanup. This covers the shared JSON/NBT component codec, including saved inventories and trade outputs. Unknown unrelated IDs still use Minecraft's normal validation. The active client and server must both receive the matching repacks. Removed Barricades blocks/items are not migrated into replacement blocks/items.

`artifacts.json` records the exact removed JARs, before/after hashes and resource changes. Originals are retained locally under ignored retired packs. `build.py` builds candidates in `tmp/enchantment-retirement/built`; it never installs them automatically. `RetiredEnchantmentsMixin.java` implements the narrow saved-item migration. `PatchRetirement.java` removes the two obsolete Java hooks.

Run `run_probe.py` before installation to serialize gear with the original registry, then `run_probe.py --candidate` against the replacement set. The isolated NeoForge 26.2.0.90 server checks registry removal, retained/hidden Sweeping Edge, legacy armor/book JSON and NBT decoding, creative tabs, the Soulbound death hook and affected loot tables. See `runtime-results.txt` for the final observed result. Personal worlds and the separate server are not modified.

Historical armor/ranged/melee enchantment reports describe their original builds; this retirement supersedes their hidden-mod-enchantment and Life Steal policy. Unrelated enchantment conflicts and CrossArms Frenzy mechanics are retained.
