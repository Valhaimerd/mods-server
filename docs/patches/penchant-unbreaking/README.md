# Penchant Unbreaking X, still breakable

25 October 2026. Same-filename data overlay for `penchant-neoforge-26.2.0-1.6-2.5x-progress.jar`.

Unbreaking progresses to **X**. It reduces durability loss and does not make an ordinary item permanently unbreakable, including at its maximum level. Tools/weapons use normal Minecraft Unbreaking protection (at X, a 1-in-11 durability-loss chance); armor retains Minecraft's smaller armor-specific benefit. Individual results vary. Items explicitly marked Unbreakable by another system remain unbreakable.

## Changes

- Add `data/minecraft/enchantment/unbreaking.json`: the installed Minecraft 26.2 definition, with only `max_level` changed from 3 to 10.
- Change `resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json`: maximum 5 -> 10, remove `penchant:unbreakable`, use the same native conditional tool/armor durability effects. Preserve its original costs, supported items, slots and weight.
- Correct the English Tome of Penchant Durability Rework entry. Remove its obsolete Unbreaking V/Unbreakable screenshot page and replace the permanent-unbreakability promise with accurate text. The old texture is retained for archive/save compatibility but is no longer used by this entry.

Penchant's prior maximum-level special effect both prevented all damage at the cap and used a chance curve reaching 100% durability protection. Removing the special effect selects the normal native `minecraft:item_damage` path instead; simply raising its old linear curve would leave the maximum unbreakable.

Every existing Java class is byte-identical, including the 2.5x progression patch, compatibility repairs, restrictions and hidden-enchantment policies. Mending and other Durability Rework features are unchanged. No automatic leveling or reset is applied to existing equipment; lower levels continue progressing normally. Both enabled and disabled module worlds receive a level-X cap from the corresponding resource. Restart or reload resources to update an existing world. Personal instances/worlds and the separate desktop server were not modified.

## Verification

```powershell
python docs/patches/penchant-unbreaking/build.py
python docs/patches/penchant-unbreaking/run_server.py --baseline
python docs/patches/penchant-unbreaking/run_server.py
python docs/patches/penchant-unbreaking/run_server.py --module-off
python docs/patches/penchant-unbreaking/verify.py --installed
```

An isolated Minecraft 26.2 / NeoForge 26.2.0.90 dedicated server uses the active patched Patchouli 26.2-95 dependency. Two fresh-world runs explicitly verify the Durability Rework module is enabled/disabled as intended. Each passes 18 native checks: registered and Penchant maximum, absence of the unbreakable effect, actual reduced durability damage and eventual breakage for a netherite pickaxe and chestplate at X, IX -> X progression, stopping at X and preserving explicit Unbreakable components. The original build fails the cap-X check. Exact dependency hashes and results are adjacent. This is a focused native server test, not a full-pack multiplayer playthrough.

The final same-filename JAR is installed in repository `mods` and ignored fixed packs. The previous JAR is retained under `mod fixing/retired packs/penchant-unbreaking-2026-10-25`. The handbook/PDF is updated. Unrelated pending Pet Vault and PatPat changes remain intact; nothing was pushed.

## Follow-up: 26 October 2026

The later [wolf armor compatibility overlay](../wolf-armor-balance/README.md) preserves these Unbreaking curves/cap and extends supported items plus native wear progression to owned wolf armor. Its narrow Java hook is layered over this original data-only patch.

The later [pending Unbreaking repair](../unbreaking-progression/README.md) reconciles existing earned full bars on carried/worn gear without another wear event. It preserves the existing X cap, durability curves and wolf integration. Retain that overlay when rebuilding the active pack.
