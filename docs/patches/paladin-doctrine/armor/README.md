# Per-set plate Healing Power overlay

28 October 2026. These are armor-only candidates for integration into the Paladin doctrine patch; no active/fixed JAR is installed by these scripts.

The already-hooked `HealingArmorBalance` helper maps `Armor.Entry.name()` to Paladin +0.75, Crusader +1, Netherite Crusader +1.15, Justicar/Lightbringer +1.25 per piece. The five full sets grant +3/+4/+4.6/+5/+5. Only flat `spell_power:healing` `ADD_VALUE` modifiers in those sets are adjusted, in both defaults and loaded equipment configs. Other armor entries are skipped.

Inputs are immutable copies in `tmp/paladin-doctrine/before/`, not the older healing-armor-balance inputs. Each candidate replaces exactly the existing helper class and retains every other archive entry, including the original registration hook, robes, spells, resources and prior fixes. Java class version remains 69 (Java 25). `artifacts.json` records exact input/output hashes.

## Reproduce

```powershell
python docs/patches/paladin-doctrine/armor/build.py
python docs/patches/paladin-doctrine/armor/run_server.py --baseline
python docs/patches/paladin-doctrine/armor/run_server.py --baseline --legacy
python docs/patches/paladin-doctrine/armor/run_server.py
python docs/patches/paladin-doctrine/armor/run_server.py --legacy
python docs/patches/paladin-doctrine/armor/verify.py
```

Both baseline modes fail all twenty requested per-piece values and all five set totals (the installed baseline grants +0.25 per piece). The fresh candidate and loaded legacy-config candidate each pass 29 native checks, covering all twenty actual registered item attribute components, all five set totals, equality of all other native modifiers across both mods, and explicit Avatar/Absolution percentage-Healing-Power preservation. Baseline snapshots and generated +0.25 legacy equipment configs are alongside this file. Fresh baseline has 28 checks because it creates the preservation reference; legacy baseline has 29.

Tests use isolated disposable flat-world NeoForge 26.2.0.90 servers, the recorded installed dependency JARs, `-Xverify:all`, and Eclipse Adoptium Java 25 from `C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin/java.exe`. Compilation uses JDK 26 with `--release 25`. Tested dependency hashes are in `candidate-tested-mods.json` and `legacy-tested-mods.json`; Skill Tree is pinned to the task input. Native result/check files are alongside this note; full logs/worlds are ignored under `tmp/paladin-doctrine/armor/`.

Outputs: `tmp/paladin-doctrine/armor/built/paladins-neoforge-3.1.2+26.2.jar` and `tmp/paladin-doctrine/armor/built/armory-neoforge-1.5.2+26.2.jar`. Parent integration must retain these helper bytes when composing other Paladins changes. This verifies registration/config behavior, not connected-client tooltips, equipped player stats or a full-pack playthrough. Explicit custom ItemStack attribute overrides are outside the default-item balance. No personal instance/world is used, and no commit/push is made.
