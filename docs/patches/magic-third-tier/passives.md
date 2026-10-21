# Wizard passive pairs

21 October 2026. Current repack values. P is the caster's matching school power; damage and healing are raw before criticals, defenses and PvP retention. Choose one node from each pair. All eighteen nodes explicitly allow haste to reduce their cooldowns; haste does not shorten their effect durations. A zero cooldown adds no delay beyond the existing roll or spell itself.

| Arcane pair | Node | Trigger and effect | Base cooldown |
| --- | --- | --- | --- |
| 1 | Fissile Magic | Active Arcane damage: 25% chance of 40% P explosion, radius 5. Excludes the original target; distance falloff retained. Chance batching retained. | 1 s |
| 1 | Arcane Radiance | Active Arcane damage: guaranteed 10% P self-heal. | 3 s |
| 2 | Arcane Trap | Roll leaves a 5 s trap, radius 1.5: 40% P and Weakness II for 6 s. | 0 |
| 2 | Phase Shift | Roll grants exactly 6 ticks / 0.3 s native damage immunity. Uses a direct immunity action rather than the legacy status effect's repeating immunity tail. | 0 |
| 3 | Presence of Mind | Burst Blink/Evocation guarantee a 5 s readiness buff. The next Arcane spell's damaging hits apply non-stacking +20% Arcane damage vulnerability for 6 s. No instant cast. | 16 s |
| 3 | Arcane Ward | Active Arcane cast guarantees an 8 s absorption ward: 2 x (1 + floor(0.2P)) HP. | 16 s |

| Fire pair | Node | Trigger and effect | Base cooldown |
| --- | --- | --- | --- |
| 1 | Scorching Flames | Active Fire damage: 50% chance to add Fire Vulnerability for 2 s, +4% damage per stack, up to five stacks / +20%. | 0 |
| 1 | Hot Impact | Fire damage: 20% chance to stun for 2 s. Eligibility includes living bosses and normally effect-immune entities. | 8 s |
| 2 | Flame Trap | Roll leaves a 5 s trap, radius 1.5: 30% P and exactly 4 s burning. Replaces its former vulnerability application. | 0 |
| 2 | Blazing Speed | Roll guarantees +50% base movement speed for 2 s. | 4 s |
| 3 | Eruption | Taking damage guarantees 30% P explosion, radius 5; distance falloff and 1.2 knockback retained. | 8 s |
| 3 | Flame Ward | Active Fire cast guarantees an 8 s absorption ward: 2 x (1 + floor(0.1P)) HP. Retains 30% P retaliation against attackers. | 16 s |

| Frost pair | Node | Trigger and effect | Base cooldown |
| --- | --- | --- | --- |
| 1 | Winter's Chill | Active Frost damage guarantees the 2 s debuff: +8 percentage points of Frost critical-damage vulnerability per stack, up to five stacks / +40 points. | 0 |
| 1 | Frostbite | Active Frost damage guarantees Frozen for 3 s. Eligible direct damage can break Frozen early. | 12 s |
| 2 | Frost Trap | Roll leaves a 5 s trap, radius 1.5: Frozen for 4 s, no damage. | 0 |
| 2 | Arctic Reflex | Roll guarantees a 5 s readiness buff. The next Frost spell deals 1.2x damage across its deliveries and targets. No instant cast. | 10 s |
| 3 | Cold Snap | Taking damage guarantees a Frost cooldown reset, excluding Blizzard and Frost Elemental. Other schools are unaffected. | 30 s |
| 3 | Frost Ward | Active Frost cast guarantees an 8 s absorption ward: 2 x (1 + floor(0.1P)) HP. Retains 5 s stacking Frost Slowness against attackers, initially -30% base movement speed. | 16 s |

All traps check every four ticks and disappear after the first successful area activation. Friendly party players and owned pets cannot trigger harmful impacts. Burning remains subject to native Fire Resistance and fire immunity.

## Ward sizes

Absorption is temporary HP, shown as golden hearts. Existing absorption is preserved if larger; ward amounts are not added together. Wards end when depleted or after eight seconds.

| School power | Arcane Ward | Flame / Frost Ward |
| --- | --- | --- |
| 20 | 10 HP / 5 hearts | 6 HP / 3 hearts |
| 40 | 18 HP / 9 hearts | 10 HP / 5 hearts |
| 60 | 26 HP / 13 hearts | 14 HP / 7 hearts |
| 100 | 42 HP / 21 hearts | 22 HP / 11 hearts |

## Runtime implementation and validation

`passives_data.py` updates the eighteen definitions and tooltips without changing skill IDs, allocations or exclusions. The SkillEffects registration hook sets Fire Vulnerability to 0.04 and Frost critical vulnerability to 0.08 per stack, registers a separate 0.20 Arcane Vulnerability, and removes only the two superseded InstantCast registrations. The native Beam Exposure effect remains separate.

Presence/Reflex readiness uses the existing effect IDs with direct self-status delivery. `PassiveRules` consumes readiness when the next matching active spell actually delivers. A channel is identified by spell ID and process start; a mid-channel roll cannot empower a channel already in progress. A serializable marker in a copied impact context carries the selected spell's effect into delayed projectiles, clouds and other targets without changing shared registry definitions. Existing charge modifiers are copied and retained. Reflex multiplies damage coefficients only, after the earlier per-spell rules; it does not multiply healing or summon attributes. A non-damaging matching spell still consumes readiness, and an unused buff expires after five seconds.

The shared party guard runs for appended vulnerability and all stun impacts. Universal stun application bypasses health/boss/type eligibility gates, preserves native status callbacks/action impairment, and uses the existing physical movement hold alongside Frozen. Non-living blocks, items and projectiles do not have living-entity status effects. Scripted teleports and bespoke boss attacks are not claimed to be disabled by the movement hold alone.

Verification: 128 archive/resource checks and 855 isolated dedicated-server checks, including the preceding spell, party, Hydra and wall cases. New fixtures cover all eighteen haste cooldowns, actual ward absorption at P40, native Blink/roll grants, removal of instant casting, real Explosion vulnerability and its damage multiplier, whole-channel and delayed-Lance Reflex behavior, following-cast isolation, Cold Snap exclusions, six-tick immunity, actual trap damage/Weakness/burn, Fire Vulnerability damage and Radiance healing/cooldown. Party fixtures check stun immunity bypass, Ender Dragon movement/recovery and party/pet protection. The patched wall client input test also passes with JVM verification. These are isolated pipeline tests, not a full-pack manual playtest.
