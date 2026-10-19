# Implementation ledger

- Completed: data transformation, migration/pet support, isolated validation and handbook/PDF verification on 2026-10-18.
- Ruling: extend the existing Orb compatibility mod for pet bonuses and migration; no extra pack entry.
- Ruling: once per player, refund Adventurer allocations with category XP and point budget preserved. RPG category/skill IDs are retained.
- Ruling: new health/healing side nodes use the normal one-point cost.
- Ruling: Martial Prowess/Hunter damage terminals use the explicit across-the-tree 1% target; keep their other requested stats.
- Ruling: native critical modifiers were insufficient for fixed percentage points with additive gear; the bridge compensates for existing multipliers. Bare/geared cases pass.
- Runtime: 54 checks pass, including controlled school damage, native wolf attacks, knockback occurrence, incoming damage and bow/crossbow drawing timing. Client skill screen and zoomed tree render successfully.
- XP A/B: real loaded kill-XP calculations change 70 to 94.5 and 120 to 162 (both RPG categories), exactly 1.35x; original/modified runs use separate worlds. Skill XP awards retain framework integer rounding.
- Review: independent code review found no remaining blocker after the fixed-critical-point correction. Knockback distance and every modded pet AI were not quantitatively tested.
- Packaging: three active same-filename JARs match tested candidates and fixed-pack copies. Originals preserved in ignored retired packs. This balance revision does not require an installer rebuild.
- Documentation: guide updated; rebuilt 71-page PDF, contents destinations checked, all pages rendered/reviewed and handbook checks passed. Duplicate Better Party closing prose consolidated to avoid a nearly empty page.

- Balance revision: Martial/Hunter 5% damage, requested secondary bonuses; Magistrate 20% power/10 critical points with ordinary 5% power/2.5 critical points/3% haste; Aegis 15%/15%; Colossus six hearts; blue titles and requested icons. RPG three-school power is 5%; Priest remains 3% and Paladin is unchanged. Same IDs/migration marker preserve existing choices.
- Rebalance verification: 54 dedicated-server checks pass; all nine titles parse as blue in the client. Active/server/client/fixed JAR hashes match and all archives pass CRC checks. Rebuilt PDF remains 71 pages; only page 17 changes visually, with contents destinations unchanged.

- 20 October physical revision: Double Hit removed; Adventurer/RPG ordinary melee 3% and ranged 2%; Hunter ranged 6%, Hunter and ordinary projectile speed 15%. Same IDs, unchanged bridge and XP. Structural checks and 78 isolated server checks passed. No new client visual test.

- Latest projectile follow-up: ordinary/Hunter projectile speed restored to 25%. Voltaic Shot definition, functions, tag, translations and five loot outcomes removed from the existing registry-fixed Dungeons Arise. 79 isolated server checks passed, including loaded-registry absence. Unrelated ensnaring tag/predicate data warning is unchanged.
