# Adventurer Healing Power

25 October 2026. Same-filename data overlay for `adventurerskills-EN-1.3-26.2-neoforge-fixed.jar`. Only four nodes in the Adventurer category definitions change. Existing saved IDs, allocations, connections, exclusions and other rewards remain intact.

| Node | Installed behavior | Player-facing label |
|---|---|---|
| Ordinary Healing | Existing +3% healing received, plus +3% Healing Power | +3% Healing |
| Colossus | Existing +6 hearts and +6% healing received, plus +6% Healing Power | +6 Hearts / +6% Healing |
| Ordinary shared caster power | Existing +3% Arcane, Fire and Frost Power, plus +3% Healing Power | +3% Magic Damage |
| Magistrate | Existing +10% Arcane, Fire and Frost Power, plus +10% Healing Power; retains +5% haste/resistance | +10% Magic Damage / +5% Spell Haste / +5% Magic Resistance |

The new rewards target `spell_power:healing` with `multiply_total`, matching the existing Adventurer percentage operation. They improve the Healing school used by outgoing healing spells, rather than generic power or the unrelated incoming-healing attribute. Existing `puffish_attributes:healing` rewards remain on Healing/Colossus. Self-healing can benefit from both outgoing power and incoming healing; they are separate bonuses. Health assignments that bypass `heal()` are not converted into healing spells. RPG Skills' separate school-specific nodes and the Spell Power enchantment are unchanged.

## Build and verification

```powershell
python docs/patches/adventurer-healing/build.py
python docs/patches/adventurer-healing/verify.py --installed
```

Build reads the pinned snapshot under ignored `tmp/adventurer-healing/before/` and writes a candidate to `built/`. `artifacts.json` records hashes, the one changed archive entry and four node IDs. Original active/fixed archives are retained under ignored `mod fixing/retired packs/adventurer-healing-20261025/`; installed active/fixed copies match.

**62 archive/node checks passed:** valid ZIP, identical archive inventory, byte-identical unrelated entries, unchanged other definitions, exact four Healing Power rewards, preserved existing rewards and requested labels. No Java classes, acquisition logic or dependencies change. This data-only revision has not received a fresh runtime/client test. The installed engine was inspected to confirm healing spells use the Healing school and recipients' `heal()` path applies Pufferfish healing separately.

The handbook explains that the requested Magic Damage label also covers Healing Power. Its PDF is rebuilt and reviewed. No commit or push was requested.
