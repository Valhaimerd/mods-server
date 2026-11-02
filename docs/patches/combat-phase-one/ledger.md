# Combat phase one implementation ledger

Status: phase one implemented and verified; repository installation is recorded in `completion.json`. Inputs are pinned in `baseline.json`, preserving unrelated grave, accessory and presentation repairs. Phase two remains deferred.

## Rulings

- Wards grant complete groups of eight matching power: 2 HP at 8, 4 HP at 16, no free base heart. This follows the planned whole-heart interpretation; it costs a discontinuity at each eight-power boundary.
- Echoing recharge uses the native saved cooldown clock, which pauses offline. Haste is sampled when each serial recharge begins. This keeps relogging from granting charges and avoids an additional packet/persistence system; offline recovery is consequently not provided.
- Sinful Retaliation retains the existing player-source damage policy (35% against players, 80% against other entities), with absorption consumed before health. Its stated 1 HP is the raw pulse before this policy. No critical or haste scaling.
- Work remains in the existing authorized pack workspace so its pending tested fixes are retained. Candidates and baseline copies are isolated under ignored `tmp/combat-phase-one`; no personal installation or shared branch is changed.

## Progress

- Inputs snapshotted; narrow current-archive overlays preserve unrelated entries. No older October builder was installed.
- RED: resource baseline has20 failing contracts; native Missiles damage reproduces8.32HP rather than revised6.72HP at40 power with80% retention.
- GREEN:196 dedicated checks,10 integrated-client checks,116 resource/archive checks. Baseline/candidate results and exact tested hashes are adjacent.
- Fresh-context review completed by `phase_one_review`. Important findings addressed: charge copy on respawn, preserving the native timer after a first Frenzy hit, visible Sinful debuff, explicit invulnerability and native totem protection.
- Real client testing caught two synchronization defects missed by server-only checks: incremental packets ignore non-registry cooldown keys; the client recreates its cooldown manager after the clone event. Full native sync after spending/queue rotation and after recreation fixes both. Client cast/third-cast rejection/death/respawn/recovery pass.
- Ward modifiers and granted HP match the new capacity, refreshes never refill spent HP, and overlapping school Wards use a persisted shared capacity ledger.
- Raw references point to the current complete mechanical resource snapshot; historical exports are clearly superseded. Concise English descriptions, handbook, TOC and PDF synchronized.
- No commit/push or personal/live-server installation. Phase two remains unimplemented.
