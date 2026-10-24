# Player and RPG summon damage retention ? 24 October 2026

The same-name `pvp_flagging-26.2-1.1.2.jar` uses one damage-event layer:

| Attacker | Player victim | Other entity victim |
| --- | --- | --- |
| Player | 35% | 80% |
| Player-owned Spirit Wolf, Arcane Barrage emitter, Fire Hydra or Frost Elemental | 35% | 80% |
| Ordinary entity or unowned summon | 100% | 100% |

These are retained amounts of the damage reaching `LivingDamageEvent.Pre`, after native defenses. The PvP amount is 0.35 directly, **not 0.8 multiplied by 0.35**. Ordinary attacks and player/summon-attributed spells and projectiles share the same rule. Healing and class spell coefficients remain unchanged. Direct health assignment and ownerless damage do not pass through player ownership scaling.

## Ownership and party protection

`playerSource` resolves a player attacker directly, follows projectile owners, and follows `OwnableEntity` ownership for only these exact entity types: `archers:spirit_wolf`, `wizards:arcane_emitter`, `wizards:fire_hydra`, `wizards:frost_elemental`. Ordinary pets/mobs are not broadly reclassified as player attacks. Ownership must resolve to a current server player.

Before applying the multiplier, resolved owners are checked against the victim player or pet's owner using vanilla team rules and Better Party's `blocksFriendlyFire`. The optional bridge is guarded by the installed `better_party` mod ID. This closes the native summon-melee route that did not reach Better Party's player-source check. Own pets/owners remain protected. Existing spell acquisition/impact party guards stay intact; the handler never uncancels blocked damage. Friendly-fire settings remain authoritative where the native spell path permits a hit.

## Removing the former attribute policy

The hidden -20% attribute penalties are removed. Entity join, death clone and periodic reconciliation now remove only `pvp_flagging:player_power_balance` from the prior seven attributes. They never add penalties or alter base values, gear modifiers or skill bonuses. No icon, potion effect, flag commands, zones or custom protocol is added. Summons created while the previous attribute policy was active may retain their captured stats until replaced; summon new ones for comparison.

## Build and proof

`build.py` compiles two production classes using Java-25 bytecode and preserves upstream identity/notices/icon. It writes a candidate and does not install automatically. The Better Party archive is a compile input for the guarded integration. `artifacts.json` pins the final hashes; `runtime-results.txt` records the isolated test result. Previous active/fixed copies are retained locally under ignored retired packs.

`run_probe.py --candidate` creates a new isolated test world on every run, avoiding stale party/team fixtures after failures. It checks full attributes after legacy modifier removal, real Spell Engine spell impacts, direct player damage, all four summons' melee/spell/arrow sources, unowned summons, ordinary entity combat, healing, party members/pets and team friendly fire. Personal worlds and the separate server are untouched. Sync/restart both sides for the same-name replacement; no GitHub push requested.
