# Priest passive tiers I/II - 26 October 2026

Same-filename overlays for Skill Tree 1.6.1 and Spell Engine 1.10.5, applied on top of the installed Priest rework. Both sides need matching updated archives and a restart. Saved skill IDs and tree placement remain unchanged.

| Passive | Current behavior |
|---|---|
| Healing Focus | Successful active-spell healing grants the healed recipient +4% healing and absorption received per stack, five stacks / 20%, for 6s refreshed. Base stack-trigger interval 0.25s, retaining native haste scaling. |
| Incanters' Cadence | Active-spell damage has a 100% chance to grant caster +2% spell haste per stack, five stacks / 10%, for 6s refreshed. Base stack-trigger interval 0.25s, retaining native haste scaling. |
| Fade | Combat roll clears nearby pursuing mob aggression and conceals the Priest for 2s, including body, armor, held items and first-person hands. Base cooldown 6s, haste affected. Works without nearby enemies. |
| Divine Favor | Combat roll has a 100% chance to ready a counter for 15s; base cooldown 15s, haste affected. The next qualifying damaging attack consumes readiness, stuns the attacker for 1s even outside the secondary radius, and stuns eligible enemies within two blocks of the Priest for 1s. Damage absorbed by a shield also qualifies; fully blocked/canceled attacks and environmental damage without an eligible living attacker do not consume it. No spell critical-chance bonus or cast-consumption behavior remains. |

## Healing and absorption

The existing healing trigger is preserved; receiving absorption alone does not build Healing Focus stacks. The installed engine emitted its heal event but lacked the matching post-heal passive trigger. The patch bridges that successful-heal event into the native post-heal trigger path. The native cooldown manager supplies the stack intervals. The received-healing attribute is 4% per stack.

Absorption scales only HP earned by a new grant: a 1 HP grant becomes 1.2 HP at five Focus stacks. A capped stack refresh still grants zero. Existing consumed shield HP is never reconstructed. Extra maximum-absorption capacity is tracked per source effect so the bonus fits without overwriting other shields; it is removed when that source effect ends. Both the prior custom shield handlers and vanilla absorption's start hook are covered. Priest tier-I/II active mechanics, coefficient normalization, binding tiers and party rules remain intact.

## Full concealment

Vanilla Invisibility is paired with a synchronized Fade marker. Client-only mixins capture that marker in the living render state and cancel its full submission, covering armor and held-item layers as well as the body. The first-person hand submission is canceled separately. No gameplay code loads client renderer classes on a dedicated server. Existing Rogue stealth and ultimate render hooks are preserved.

## Packaging and evidence

```powershell
python docs/patches/priest-passives/build.py
python docs/patches/priest-passives/run_probe.py
python docs/patches/priest-passives/verify.py --installed
python docs/patches/priest-reference/export.py
```

Build uses exact current snapshots in ignored `tmp/priest-passives/before/`, writes candidates under `built/` and records hashes/changed entries in `artifacts.json`. It recompiles only the current tracked Priest helper plus the new passive/render helpers. Every unrelated archive entry is preserved. Predecessors are backed up under ignored `mod fixing/retired packs/priest-passives-20261026/`. Active and fixed copies match.

**526 isolated Priest/party checks passed**, including the previous active-spell regressions, native Healing Focus triggering, stack caps/intervals, native healing scaling, new-shield scaling without refills, capacity cleanup, Cadence chance/haste, Fade without enemies, Divine readiness/critical removal, targeted/radial stun and native damage-event consumption. **3,596 archive/data/client-isolation checks passed.** The dedicated-server run loads the common absorption mixin with `-Xverify:all`; client sources compile against the mapped client classes and exact renderer descriptors were inspected. No connected-client visual playtest is claimed.

Concise effect descriptions reserve cooldown/trigger details for the native yellow panel. The Priest reference and handbook PDF are synchronized. Personal installations and worlds are untouched; no commit or GitHub push was requested.

Latest installed follow-up: [Holy Staff and Priest tuning](../priest-tuning/README.md). Historical source/output hashes above are superseded for Paladins, Skill Tree and Spell Engine; do not reinstall earlier builder outputs over the current overlay.
