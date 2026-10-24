# Orc starting bonuses and draw-speed penalty

24 October 2026. Follow-up to [race balance](../race-balance/README.md).

Orc always starts with **+2 melee damage and +1 ranged damage**, including full health. Bonuses rise at 40/30/20% HP to **+3/+4/+5 melee** and **+1.5/+2/+2.5 ranged**, capped below 20%. The former absence of bonuses above 50% is removed; 50% is not a required activation threshold.

Orc retains 90% attack speed and now also retains **90% bow/crossbow draw speed** through `ranged_weapon:haste`. This is a total multiplier after gear modifiers, not a fixed subtraction from a base percentage. It is a speed reduction, so reaching the same draw progress generally takes about 11.1% longer, subject to tick rounding. Race changes remove both penalties.

Only `RaceBalanceRules.class` and the Orc strings in the existing two language files change in the same-filename Iourus JAR. Elf/Goblin/Dwarf/Asgardian settings, slow-fall code, metadata, network/save IDs and the legacy Orc migration remain preserved. The source generator applies this small follow-up to the previous helper, then compiles Java-25 bytecode against a pinned snapshot of the installed JAR.

```powershell
python docs/patches/orc-followup/build.py
python docs/patches/orc-followup/run_server.py
python docs/patches/orc-followup/verify.py tmp/orc-followup/built
# After installing matching candidate/fixed copies:
python docs/patches/orc-followup/verify.py
```

**170 native server measurements passed**, including full-health and 51%-HP bonuses, all low-health thresholds, native/Better Combat/RPG attack-speed integration, ranged power, bow and crossbow use-tick slowdown, gear stacking and race-change cleanup. This reuses the earlier race/persistence checks; it does not claim a new connected-client falling test because that code is unchanged.

`artifacts.json` records exact input/output hashes and changed entries. Input and output archives stay ignored under `tmp/orc-followup/`. Previous active/fixed copies are retained under ignored `mod fixing/retired packs/orc-followup-2026-10-24/`. No personal installation/world changes and no commit/push requested.

Handbook verification: all 77 pages rendered; only page 16 changed from the previous reviewed PDF and was visually checked. All 51 contents destinations remain correct; root/output PDF copies match.
