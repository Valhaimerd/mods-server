# Armor enchantment policy

Applied 23 October 2026 to Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.

The existing `penchant-neoforge-26.2.0-1.6-2.5x-progress.jar` is repacked under
the same filename. Exactly three data resources change; all Java classes,
metadata, optional modules and the earlier 2.5x progression repair are preserved.
No extra mod or required dependency is introduced.

## Hidden enchantments

The base `penchant:disabled` tag now includes these optional IDs:

| Enchantment | ID |
| --- | --- |
| Protection Grace | `protection_grace_enchantment:protection_grace` |
| Antidote | `nova_structures:antidote` |
| Traveler | `nova_structures:traveler` |
| Soulbound | `enderzoology:soulbound` |
| Air Jump | `air_jump_enchantment:air_jump` |

Penchant's existing hooks reject normal application and exclude disabled
enchantments from random generation and creative books. Its existing
`c:hidden_from_recipe_viewers` tag includes `#penchant:disabled`. Optional
entries allow Penchant to continue loading without those providers.

Definitions remain registered for save compatibility. This does not remove
stored enchantments from old equipment/books, disable every legacy effect, or
prevent administrators from constructing explicit item components. Direct
debug loot tables and already-created villager offers are not purged.

## Same-piece exclusions

| First enchantment | Cannot combine with |
| --- | --- |
| Thorns | Fire Protection, Blast Protection, Fangs |
| Frost Walker | Soul Speed |

`data/minecraft/tags/enchantment/exclusive_set/thorns.json` and
`exclusive_set/frost_walker.json` use Universal Enchants' installed tag hook.
It adds these exclusions to existing rules without replacing whole enchantment
definitions. Native compatibility checks reject either application order.
Frost Walker/Soul Speed applies to the same boots, not to different players.
Previously forced combinations are not automatically stripped.

## Rebuild and installation

From the repository root:

```powershell
python docs/patches/armor-enchantments/build.py
python docs/patches/armor-enchantments/verify.py
```

The builder checks the exact pre-change archive hash recorded in
`upstream.json`, then writes a candidate under `tmp/armor-enchantments/`.
The input is preserved under ignored
`mod fixing/retired packs/armor-enchantments-2026-10-23/`.
`artifacts.json` records the input/output hashes and changed entries.
The builder does not install automatically. The tested final candidate was
copied to `mods/` and `mod fixing/1 fixed packs/` after runtime checks passed.
The installer detects the changed content under the unchanged filename.

## Verification

The isolated dedicated-server fixture used the installed versions of Penchant
26.2.0-1.6, Patchouli 26.2-95, Universal Enchants 26.2.0, Puzzles Lib 26.2.3,
Ender Zoology 26.2.0, Dungeons and Taverns 5.3.2, Air Jump 2.0.0,
Protection Grace 1.0.0 and Fangs 1.0.0.

The pre-change archive failed the new test at Protection Grace's disabled tag.
The final archive passed **142 native checks**:

- All five hidden IDs remain registered, belong to both disabled/viewer tags,
  and reject normal `ItemStack.enchant` application.
- Twenty forced-option random-loot samples for each hidden ID produce no book
  carrying it, exercising explicit options rather than relying on low odds.
- All creative-tab search contents omit the five hidden enchanted books.
- All four incompatible pairs fail native compatibility and actual anvil
  combination in both orders.
- Unrelated compatibility remains; an actual Protection + Thorns anvil
  combination succeeds as a control.

`tests/ArmorProbe.java` is packaged only into a temporary fixture mod.
`tests/prepare.py` compiles it with Java 25 using the existing local
`tmp/bows-balance/classpath.txt` and prepares the minimal server under
`tmp/armor-enchantments/server/`. It disables Penchant's optional No Anvil Books
module for a fresh fixture world, so rejected anvil results cannot pass merely
because all book application is disabled. Existing fixture worlds retain
their saved module selection.

```powershell
python docs/patches/armor-enchantments/tests/prepare.py --candidate tmp/armor-enchantments/penchant-neoforge-26.2.0-1.6-2.5x-progress.jar
```

The fixture needs the local NeoForge 26.2.0.90 libraries; this run used a
junction to `tmp/crossarms-firing/server/libraries`. Launch from the fixture
server directory with Java 25+:

```powershell
java -Xmx2G '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt' nogui
```

The probe writes `result.txt` and stops the server. This run's evidence is in
`tmp/armor-enchantments/baseline.log`, `patched.log`, and `server/result.txt`.
No client JEI screen or every structure-specific villager offer was manually
tested. Personal Minecraft installations, the separate desktop server and
player worlds were not changed.

The handbook PDF was rebuilt from `docs/MODLIST.md`. All 74 pages were rendered
and reviewed, including a full-size check of policy page 26. All 461 internal
destinations and printed contents page numbers were validated; root/output
PDF copies match. Evidence is under `tmp/armor-enchantments/pdf-review/` and
`pdf-report.json`.

## Registry retirement follow-up (2026-10-24)

The [enchantment retirement](../enchantment-retirement/README.md) supersedes the hidden modded enchantment and Life Steal policy recorded here. Its hashes identify the current active repacks; these original build checks remain historical. Vanilla Sweeping Edge stays hidden.
