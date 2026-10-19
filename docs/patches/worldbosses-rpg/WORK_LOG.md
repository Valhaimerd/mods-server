# World Bosses RPG implementation ledger

Scope: implement the latest local plan; no commit, push or personal-server changes.

- Baseline upstream World Bosses 2.0.0 SHA-256: `20b63910decdb07caabf90dc0c0620aa6fdd93982b1a44ec84f140a39b481dc7`.
- Preserve all pre-existing tracked edits and user-added JARs/emote ZIP.
- Ruling: keep the native armor ceiling of 30; proposed party armor is clamped rather than changing armor limits globally.
- Ruling: raise only the permitted maximum-health range, without changing other entities' actual health. Both distributions need the same health handling.
- Ruling: work in a dedicated ignored build/test directory within the current workspace; no checkout or staging that could disturb the existing mixed changes.
- RED: arithmetic/burst test fails before `RaidMath` exists.
- Implemented: one-time config migration, all eleven boss profiles, additive party scaling, combat regeneration, source-owned burst limits, warned terrain responses, safe movement, gear retirement and personal rewards.
- Review corrections verified: preserve wipe home position, defer advancements with rewards, validate Shadow Step destinations, cancel stale terrain warnings, and guard boss-forced movement against environmental deaths.
- GREEN: pure arithmetic/burst checks and 248 isolated NeoForge server checks passed on the final candidate. Includes unrelated magic/Frenzy-style self-damage, every cache tier, legacy gear conversion, saved state and reward-ledger reloads.
- Updated README and player guide; rebuilt the 74-page handbook. Reviewed the changed pages 36 and 37 visually with Poppler rendering; no clipped tables or orphaned cooldown text.
- Final archive hashes and verification outcomes are recorded in `verification.json`. The tested candidate is installed under the existing active filename and copied to the ignored fixed-pack folder; upstream is retained in ignored retired packs.
- Limits: no full-pack human raid, client visual/animation test or measured fight-duration result. No personal server/world writes, commit or push.
