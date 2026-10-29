Current Paladin follow-up values and archive identities supersede the affected earlier records: [Purgatory, Oath and worn Banner](../paladin-followup/README.md). Earlier tests/hashes below describe their original build.

# Class visuals and status icons

Minecraft26.2 / NeoForge26.2.0.90 / Java25. Same-version, same-filename repacks of Archer, Rogue/Warrior, Wizard, Paladin/Priest, Skill Tree and Spell Engine. See `artifacts.json` for exact before/after identities and archive entries.

## Changes

- Repaired Improved Stealth, Tracking Disruption and Cheat Death Evasion sprites using existing native skill/status art. Official Stun, Root and Frozen art is unchanged.
- Retouched95 native spell presentation resources across Rogue, Archer, Warrior, Priest, Arcane, Fire and Frost. Class palettes and restrained cast/travel bursts retain native models, clips, geometry, sounds and delivery cadence.
- New earned-stack/shield-gain accents use the existing status-details update. Equal-level refreshes and spent shield HP do not trigger a new gain accent. Concealed actors receive no new public stack/recovery trails; Rogue gain packets are owner-only.
- Native weapon readiness glints cover Supercharge, Prepared Counter, Concussion Blow, Divine Favor, Presence of Mind and Arctic Reflex. Existing native status/charge details and charge-consumption effects remain authoritative.
- Selected area impacts use the center/radius already resolved by the damage path. Penance uses its actual4-block blast. Recovery feedback follows actual HP gained, while no-op healing retains its gameplay events without a false recovery flash/sound.
- Successful Vanish clears existing Glowing, Hunter's Mark and Sinful Mark. Other harmful effects remain; later marks can apply. This is an explicitly requested gameplay change, separate from the icon repair.
- Fixed a tooltip crash encountered in native Frost casting: Spell Power permits a null school for general vulnerability. Received details now label that case `Spell`, preserving actual stacked percentages and school-specific labels.

## Verification

`python docs/patches/class-visual-retouch/build.py` builds from locally retained pinned inputs in `tmp/class-visual-retouch/before`. `verify.py --installed --runtime` checks final active/fixed/tested identities, metadata, skill graphs, original archive entries and360 unchanged gameplay spell documents. `verify_assets.py mods` validates109 nonempty native status sprites.

Fresh and stale-config dedicated-server fixtures each pass229 checks under `-Xverify:all`. Most inherited checks guard existing Paladin behavior; new assertions cover mark cleansing, gain/refresh/concealment/absorption distinctions, resolved ring geometry, actual recovery and nullable-school received values. They do not prove every particle packet count or class lifecycle.

The actual isolated client completed50 base/branch casts and166 assertions, including110 atlas checks and Vanish equipment/reveal checks.100 captures were inspected. Separate reduced-particle/display checks pass136/136; the final isolated display/render run passes129/129. Native HUD/inventory captures and observed-RemotePlayer rendering evidence are recorded under `client/`. The three repaired statuses were synchronized from the server for display; this display fixture does not claim to reproduce each natural proc. The remote render fixture models synchronized invisibility flags and tests actual Fade submission cancellation with readied equipment. It is not a second networked player.

Native models, stances, summon variants and existing counters were reused rather than duplicated. Some short-lived events can fall between still captures. Warrior front-camera casts were obscured by the arena target, so those captures do not establish stance quality. Crowded party/hostile multi-client playback and every death/dispel/teleport/dimension/logout/respec path remain unverified; do not infer those from compilation or cast-success checks.

Personal worlds and the separate desktop server were not modified. No GitHub synchronization is part of this pass.

The synchronized handbook was rebuilt and visually reviewed:79pages,51matching contents links,184alphabetical entries,307main/23client JARs. `handbook-verification.json` records its hash.
