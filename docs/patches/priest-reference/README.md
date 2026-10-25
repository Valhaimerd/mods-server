# Priest reference extraction

Read-only extraction of the installed Paladins & Priests, Skill Tree, Spell Engine and Spell Power archives on 25 October 2026. Run `python docs/patches/priest-reference/export.py` from the repository root to regenerate `mod fixing/Priest skills - raw attributes and nodes.md`.

Coverage: six class actives, eighteen upgrades, six passives, seven base/weapon/helper resources, both class/weapon tree definitions, placements/connections and current archive SHA-256 hashes. The exporter asserts all thirty class resources exist and retains all mechanical JSON fields. Presentation assets alone are omitted. The owner reference is intentionally ignored under the repository's `mod fixing/` rules; the readable introduction and exporter are retained here.

The tier-I readable tables additionally use inspected registrations in `SkillEffects` and `PaladinEffects`, Levitate's expiry handler and Spell Engine's cast/cooldown/fire defaults. No JARs, gameplay, player tooltips, handbook catalog or personal installations are modified; no runtime test or push is claimed.

Latest reference: [installed Priest rework](../priest-rework/README.md), 26 October 2026. The exporter now groups Penance at tier I and Ascension at tier II while preserving their original upgrade IDs. The original read-only extraction statement above describes the initial inspection.

Latest passive values: [Healing Focus, Cadence, Fade and Divine Favor](../priest-passives/README.md), 26 October 2026.

Latest tier III: [mobile Lightwell, Barrier protection and Deflective Shield](../priest-tier-three/README.md), 26 October 2026. The extraction includes the hidden native retaliation spell and current helper behavior in the readable tables.
