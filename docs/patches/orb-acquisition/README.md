# Orb of Oblivion acquisition and level cost

Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 bytecode. Same-version Skill Tree 1.6.1 repack, retaining its active filename and all preceding balance changes.

## Player behavior

Craft one Orb through the ordinary 3x3 crafting recipe:

```
Enchanted Golden Apple | Totem of Undying | Enchanted Golden Apple
Totem of Undying      | Netherite Ingot  | Totem of Undying
Enchanted Golden Apple | Totem of Undying | Enchanted Golden Apple
```

The existing `skill_tree_rpgs:orb_of_oblivion` recipe ID is replaced; the former diamond/experience-bottle recipe is absent. Picking up a netherite ingot unlocks its recipe-book entry. The standard shaped recipe and display remain available through native recipe APIs for JEI and other recipe viewers.

Every successful Survival reset requires and spends **30 experience levels**, including race-only and skill-only resets. Under 30 levels, neither skills nor race change and the Orb is preserved. A no-op costs nothing. Native Creative bypass and item durability handling remain.

## Implementation and rebuild

`src/OrbLevelCostMixin.java` adds a cancellable HEAD level guard and a RETURN debit only for successful server use. It deliberately leaves the existing Iourus `OrbRaceResetMixin` redirect intact, so one native use still resets both skills and race. It adds no redirect and does not modify the separate Puffish compatibility JAR.

Run from the repository root:

```powershell
python docs/patches/orb-acquisition/build.py
python docs/patches/orb-acquisition/run_client.py
```

The builder captures the active Skill Tree JAR once under ignored `tmp/orb-acquisition/before-*`, compiles only the added Mixin with `--release 25`, and stages the finished JAR under `tmp/orb-acquisition/built/`. It does not install into `mods/`. `artifacts.json` records input/output SHA-256 hashes and the exact changed entries; all other uncompressed entry bytes are verified unchanged. The build uses the existing local compiler and dependency classpath recorded in `tmp/rogue-tier-two/client/compile.args`.

## Isolated verification

`tests/OrbAcquisitionProbe.java` runs against actual modded server-player use and the integrated server recipe manager, with the current installed Iourus and Puffish compatibility JARs. `run_client.py` clones a disposable test world and isolated mods directory; personal worlds, the desktop server and `.minecraft` settings are untouched. `.minecraft` is used read-only for existing launcher libraries and assets.

The pre-patch baseline fails `29 levels block reset`, proving the new guard is exercised. `client-results.txt` and `client-checks.txt` contain the final native checks; `runtime-tested-mods.json` records exact participating JAR hashes. Checks cover insufficient levels preserving both resets and Orb, exact successful debits, race-only and skill-only resets, free no-op, Creative behavior, native shaped recipe lookup/output/display, rejection of the former craft, and the netherite recipe-book unlock. This is a scripted native runtime test, not a manual JEI screen or full-pack multiplayer session.
