# PVP Flagging — 26.2 port and 50% PvP damage

Installed filename: `pvp_flagging-26.2-1.1.2.jar`, Minecraft **26.2**,
NeoForge **26.2.0.90+**, Java **25+**. Install the same archive on clients and server.

Based on [the upstream 26.1.2 branch](https://github.com/Redspace-io/pvp-flagging/tree/26.1.2),
commit `80e2434f48860a16dbe94197fd3a622a4d2eb0fe`. Original archive SHA-256:
`dbc1313dd4328c0ebc39702f7d940ca2bb78b1d27f5f8219cfb3f7a1fc97b64e`.
Upstream authors are Iron431 and Smalls; upstream license metadata and resources
are retained. This is a local port, not an upstream 26.2 release.

## Player behavior

- `/pvpFlag` toggles PvP; `/pvpFlag on`, `/pvpFlag off`, and `/pvpFlag status`
  explicitly manage it. Both players must be flagged before player combat works.
- Once allowed, attributed player-to-player damage is multiplied by **0.5**.
  The same rule covers melee, arrows and standard Spell Engine school damage.
  This does not halve PvE damage or change spell/weapon attributes.
- Team rules still apply after flag eligibility. Friendly fire disabled on an
  allied team blocks combat. Team membership cannot bypass an unflagged player.
- Unflagging retains the upstream **15-second** default wait and combat
  cancellation behavior; server operators can configure PvP zones using
  `/pvpZone`. Existing flags and zones retain their saved-data format.
- HUD and nametag indicators show the flag state. No new keybind is added.

In the server config `pvp_flagging-server.toml`, `[PVP_FLAGGING]` contains
`pvp_damage_multiplier = 0.5`. The pack fixes this setting to half damage:
older values are corrected on config load and the damage handler enforces 0.5
even if another mod writes a different value during the session. Other settings
remain configurable.

Damage must retain a `ServerPlayer` attacker. Ownerless status damage and attacks
from pet entities are not reclassified as player hits by this port. The multiplier
uses NeoForge's `LivingDamageEvent.Pre` damage; armor, enchantments, resistances,
and other event handlers can affect the final observed damage.

## Port details

All Java sources are compiled against 26.2. Client overlay messages now use
`Minecraft.gui.hud`, and HUD visibility uses `Hud.isHidden()`. Metadata targets
Minecraft `[26.2,26.3)` and NeoForge `[26.2.0.90,)`; mixin descriptors retain
official names without the absent upstream refmap. The flag-check mixin also fixes
the upstream team branch that permitted attacks without both players opting in.

## Rebuild

Run `python docs/patches/pvp-flagging-26.2/build.py`. The script uses the cached
NeoForge-patched client/common classpath in `tmp/artifacts-balance/classpath.txt`
and the cached JetBrains annotations library. Java release 25 is used.
Prepare those dependencies for a new workspace before rebuilding.

Place the hash-pinned original under `tmp/pvp-26.2/upstream.jar`, or retain the
original pending input at `mod fixing/1 pending packs/pvp_flagging-26.1.2-1.1.2.jar`.
The script verifies its hash, preserves upstream resources and metadata notices,
compiles `src/`, and produces a candidate under ignored `tmp/pvp-26.2/`.
Probe classes under `tests/` are never included in production. Verify candidates
before copying to `mods/` and the ignored fixed-packs folder.

## Verification

Isolated Minecraft **26.2 / NeoForge 26.2.0.90 / Java 26**, with Spell Engine
**1.10.5+26.2**, Spell Power **1.6.1+26.2**, and the skill-test dependencies.
The 100% baseline failed the expected multiplier test. The upstream team behavior
failed the unflagged-team protection test before the universal guard was added.

The final dedicated-server probe passes **27 checks** using ordinary `ServerPlayer`
entities with test connections, not NeoForge `FakePlayer` (which refuses PvP).
For controlled 20-point inputs, actual melee, projectile and Arcane/Fire/Frost
damage remove **10 health** from a flagged player; the tested PvE targets lose
**20**. Checks also cover unflagged attackers/targets, team eligibility, friendly
fire, config enforcement and flag-state save/load roundtrip.

An isolated client with an integrated server passes flag synchronization,
unflag-request/cancellation packets and their HUD-message paths, tab-name flag
decoration, and skill title checks, and shuts down normally. HUD rendering and
the skill screen were captured. These are focused checks, not a public-server
load test or a claim that every modded damage source preserves ownership.

Test sources: `tests/PvpProbe.java` and the combined
`../skill-overhaul/ClientProbe.java`. Local logs and screenshots remain ignored
under `tmp/pvp-26.2/` and `tmp/skill-overhaul/client/`.
