# Priest tier III rework

26 October 2026. Implements the approved [Lightwell and Barrier design](../../superpowers/specs/2026-10-26-priest-tier-three-design.md) over the current [Priest tier-I/II rework](../priest-rework/README.md) and [passive fixes](../priest-passives/README.md). Reuses the active Paladins, Skill Tree and Spell Engine filenames, saved spell/node IDs, class/weapon placements and party eligibility. Both client and server need the matching three archives and a restart.

## Lightwell

- Instant summon, **80s base cooldown**, affected by haste; **16s active lifetime**, **20s with Improved**. Existing one-second spawn/despawn phases remain outside the active lifetime.
- Floats above the caster's scaled head and follows their position. It is unattackable, unpushable and collision-free. Missing/dead/removed/cross-dimension owners cause the well to disappear.
- Exactly **5% of caster Healing Power** is captured at summon. The native extra one-point Healing Power base is removed. Holy Motes heal for **100% of the well's own power**, use **0.6 launch speed**, **16-block range**, and a **0.8s base cooldown** reduced by the well's inherited haste. Mote bounce is disabled so one projectile cannot apply repeated heart/cleanse grants.
- Selects eligible injured allies first, prioritizing missing-health percentage and then distance, respecting line of sight and current party/pet ownership. Full-health allies can be selected when cleanse or a new maximum-heart stack would help; otherwise the well idles. Targets are reevaluated every tick, without letting a stale full-health target monopolize firing.
- **Cleansing Light:** each successful allied mote impact removes one harmful effect, excluding Trial Omen. The old separate cleanse action/cooldown is removed. Misses and blocked projectiles grant nothing.
- **Empowered Well:** a mote grants **one maximum heart**, capped at **two per recipient**, lasting **6s**, refreshed. The cap is shared across wells. The capacity is applied before normal mote healing but arrives unfilled; cap refreshes do not directly heal or add hearts. Removal/expiry removes only this effect's health modifier and clamps excess current HP, preserving other health bonuses. The former extra Healing Power scaling is removed.

All Priest active casts and configured skill/passive/helper cooldowns explicitly enable native haste scaling. Fixed buff lifetimes do not shrink with haste. Other classes' resources are untouched.

## Barrier

- **1s cast**, **60s base cooldown**, haste affected; **10-block radius**, **10s duration**. Improved Barrier grants **+2s**, making **12s**, replacing its old cooldown deduction.
- Keeps its native stationary visual and repeated hostile knockback. Protection uses the circular horizontal footprint and native ten-block vertical extent, not square hitbox corners.
- Caster/party allies and eligible protected pets inside are protected against ordinary damage events, including melee, magic, projectiles, fire, poison and Bleed. Void, administrative `generic_kill` and deliberate direct health costs are not intercepted. This is not a blanket patch to arbitrary mods that directly assign/subtract health.
- The former five-tick immunity lease is disabled. Protection is checked at the incoming-damage event against current live barriers and current location, so stepping outside or losing the barrier immediately restores damage eligibility.
- **Sacred Refuge is unchanged:** separate four-block cloud, ten-second lifetime, 10% Healing Power healing and cleanse every two seconds. It does not inherit Barrier's increased radius or Improved duration.
- **Deflective Shield** replaces Barrier Duration while retaining its saved node ID. Each intercepted hostile projectile retaliates against its eligible living attacker for **25% caster Healing Power** through native Priest spell damage. Native defenses, criticals and pack PvP/PvE scaling remain. There is no recursive reflected projectile. Unowned shots can be blocked but have no attacker to retaliate against.
- One shared weak projectile set prevents duplicate retaliation from repeat contacts or overlapping barriers. Friendly projectiles are excluded. The authoritative segment/cylinder sweep handles interception; the old native square projectile collider is disabled so it cannot bypass these exclusions. Native hurt callbacks apply the same eligibility checks.

## Build and verification

```powershell
python docs/patches/priest-tier-three/build.py
python docs/patches/priest-tier-three/run_probe.py
python docs/patches/priest-tier-three/run_client.py
python docs/patches/priest-tier-three/verify.py --installed --runtime
python docs/patches/priest-reference/export.py
```

Build snapshots exact current inputs under ignored `tmp/priest-tier-three/before/`, writes candidates to `built/`, and records hashes/changed entries in `artifacts.json`. Narrow ASM hooks add the current helper without rebuilding old Priest or combat sources. A Lightwell-only launcher hook preserves physical mote ownership so the caster can receive motes; the native context retains player attribution. A Lightwell-only support-target lookup avoids the shared combat target setter clearing the healer's own caster; other summons retain their original target lookup. No new mixin or dependency is required.

**544 isolated server assertions passed** (594 recorded results including diagnostics), including current Priest tier-I/II and passive regressions, party/pet checks, native mote impacts, exact inheritance, retargeting, unfilled/capped/independent hearts, actual six-second expiry preserving another maximum-health modifier, damage/poison/Bleed protection, immediate outside damage, void/cost exceptions, repeated hostile knockback, cylinder corners, naturally ticked arrows, once-per-projectile retaliation, distinct projectiles, Improved duration and haste.

**14 connected-client checks passed.** The integrated-server fixture checks native healing-mote AI, exact inherited power, head positioning, movement synchronization, twenty-block Barrier diameter, ten-block height, inside/outside protection and natural lifetime cleanup. Screenshots are reviewed from the ignored fixture. Results and tested hashes are retained alongside the patch. This is a reduced-pack test, not a full-pack performance benchmark. Personal installations/worlds are untouched.

Baseline resource verification fails on Lightwell's original 45s cooldown. Early candidate probes exposed the extra one-point summon power and cleared caster target; both are corrected and covered. A focused reviewer found the native square-collider bypass; the final correction and arrow tests passed focused re-review.

English descriptions follow the approved concise rewrite: percentages and meaningful upgrade benefits, no formulas or duplicated cooldown prose. Deflective Shield's scroll name and tree title agree. The Priest reference/exporter, handbook and PDF are synchronized. Originals are preserved in ignored `mod fixing/retired packs/priest-tier-three-20261026/`; active/fixed copies match. No commit or push requested.

Handbook verification: 77 pages; only pages 17?18 changed from the pre-tier-III PDF. All pages rendered for visual review; updated pages checked at readable resolution. Table-of-contents destinations/page numbers, matching PDF copies and all 172 downloaded icon renderings passed.

Latest installed follow-up: [Holy Staff and Priest tuning](../priest-tuning/README.md). Historical source/output hashes above are superseded for Paladins, Skill Tree and Spell Engine; do not reinstall earlier builder outputs over the current overlay.

Current tier-III passives are superseded by [health-based Pain Suppression and five-charge Celestial Orbs](../priest-low-health/README.md). Rebuilding this historical patch alone does not include that overlay.

Current [support tuning](../support-visual-tuning/README.md) makes Sacred Refuge last 10s/12s with Barrier and explicitly verifies both motes follow a sole injured ally.
