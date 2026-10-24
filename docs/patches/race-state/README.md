# Race switching, leaf collision and vision cleanup

24 October 2026. Same-filename Iourus overlay after [Orc follow-up](../orc-followup/README.md). No race IDs, equipment bonuses or character allocations are reset.

## Findings and fixes

- `PlayerRaceData.setRace` previously changed saved identity and sent a packet without applying attributes. Standard commands separately applied them, but another admin/mod calling the setter could leave the old attributes. The setter now applies cleanup/current attributes before syncing, and reset/clone use the same path.
- Melfork water/land attack modifiers previously waited for the next ability tick to clear. Core race cleanup now removes both immediately and applies the current water/land state when becoming Melfork. Water breathing, underwater-only vision and +1 swimming efficiency are retained; no potion-based vision is introduced.
- A server pre-tick reconciliation detects a direct SavedData race edit and reapplies/syncs the authoritative race, covering admin tools that bypass the setter. Login/respawn still use native reapplication. This removes named racial modifiers, not unrelated gear/class modifiers.
- Client race identity now resets on logout, preventing race vision/collision from a prior connection appearing before the next sync.
- **Elf has no racial night vision. Goblin retains it; Melfork receives it only in water.** The lightmap derives this from current identity/water state every frame and returns the original contribution otherwise, preserving potion/other lighting contributions.
- **Elf cannot pass through leaves and has solid leaf footing.** Non-Elf players pass through leaf collision under this mod's existing policy. Detection now includes the standard leaves tag and `LeavesBlock` subclasses, covering typical untagged modded leaves. A custom block that is neither tagged as leaves nor a LeavesBlock subclass cannot safely be inferred as foliage by its name.

This does not assign a speculative mod-specific leaves tag without knowing which block is affected. The owner was asked for the mod name; no answer was required for the standard-tag/subclass fix. A synthetic untagged/no-collision LeavesBlock fixture tests the fallback in isolation.

Old Orc raw-base writes have no reliable provenance when someone changed away from Orc in an older version. This fix preserves the earlier backed-up Orc migration and does not erase arbitrary non-Orc administrator base-attribute customizations. Named racial modifiers and all current transitions are covered; manually corrupted unknown base values need an explicit targeted repair rather than resetting equipment/player attributes broadly.

## Verification

The native server suite exercises every one of the **81 from/to enum-state combinations**, including NONE, with HP, movement, size, jump, armor, attack speed, melee damage and vanilla/modded leaf collision checks. It also checks immediate setter cleanup, direct SavedData edits, Melfork water/land transitions and air supply, vision policy, and preservation of unrelated gear. Its controlled FakePlayer immersion flag tests the server effect path; the connected client independently enters real water.

The connected client switches Elf -> Goblin -> Human -> Melfork in real water -> Human underwater, checking synchronized identity/stats, vision eligibility, water strength/air and cleanup. It invokes the actual injected lightmap hook for Human and the logout reset event. No full shader compatibility or unknown mod-specific foliage playthrough is claimed.

```powershell
python docs/patches/race-state/build.py
python docs/patches/race-state/run_server.py
python docs/patches/race-state/run_client.py
# Wait for tmp/race-state/client/race-client-result.txt.
python docs/patches/race-state/verify.py tmp/race-state/built
```

Build uses the prior race attribute source plus focused replacements; `artifacts.json` pins the installed input and exact archive differences. Previous active/fixed archives are retained under ignored `mod fixing/retired packs/race-state-2026-10-24/`. Both sides need the same replacement. Personal worlds and the separate desktop server remain untouched. No commit or push requested.

Handbook review: all 77 pages rendered; changed pages 16, 28, 29 and 30 were visually reviewed against the prior edition. All 51 contents destinations remain correct and both PDF copies match. Concurrent unrelated handbook edits were preserved.
