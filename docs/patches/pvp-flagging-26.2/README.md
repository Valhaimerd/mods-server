# PvP damage retention — 26.2 pack replacement

The archive remains `pvp_flagging-26.2-1.1.2.jar` and retains mod ID
`pvp_flagging` so the installer replaces the previous build. The implementation
now has one purpose: **retain 50% of attributed player-to-player damage**.

## Player behavior

- Everyone can fight by default. There is **no flagging or unflagging**, no PvP
  cooldown, no zones, no flag commands, and no flag icons, HUD, or nametag changes.
- Better Party decides whether party members may damage one another. With
  party **Friendly Fire off**, protected hits are blocked. With it on, allowed
  player hits retain **50%** damage. The server must allow Better Party's
  friendly-fire protection setting; the installed defaults do.
- Minecraft's normal combat permissions remain in force. Keep `pvp=true` for
  normal player combat; vanilla team friendly-fire restrictions are respected.
  This replacement does not forcibly enable combat against those protections.
- Standard melee, arrows and caster-attributed Arcane, Fire and Frost hits are
  halved. PvE and damage with no player attacker are unchanged. Pet ownership is
  not converted into a player source, and ownerless DOT is not reattributed.

The damage handler runs on `LivingDamageEvent.Pre`, after Better Party's incoming
damage cancellation. It changes only the amount; it never uncancels an event or
changes party/team membership. Other combat modifiers can still affect final damage.

## Removed systems and existing worlds

All upstream flag managers, configs, commands, client code, zones, network
payloads, saved-data handlers, and mixins are absent from the built JAR. Only
two production classes remain: the entry point and the damage handler. No config
file is registered; **0.5** is the fixed pack policy.

Old `pvp_flagging-server.toml`, client settings, flags and zone saved data are
ignored, not deleted. Existing worlds need no manual unflagging or migration.
Better Party data is untouched. The simplified mod has no client-only references
or custom networking; it is server-functional without client integration, and
the pack continues distributing the same JAR to both sides. Restart Minecraft
and the server after replacement. Personal installations were not changed.

## Rebuild

Run `python docs/patches/pvp-flagging-26.2/build.py` with Java 25+ and Python.
The script uses `tmp/artifacts-balance/classpath.txt`, a cached classpath containing
the NeoForge-patched Minecraft 26.2 client/common JAR and NeoForge/FML libraries.
Prepare equivalent development dependencies in a new workspace.

The preserved original lives at `tmp/pvp-26.2/upstream.jar`, SHA-256
`dbc1313dd4328c0ebc39702f7d940ca2bb78b1d27f5f8219cfb3f7a1fc97b64e`.
It was sourced from [PVP Flagging's 26.1.2 branch](https://github.com/Redspace-io/pvp-flagging/tree/26.1.2),
commit `80e2434f48860a16dbe94197fd3a622a4d2eb0fe`. The hash is checked before
building. Original metadata attribution, notices and project icon are preserved;
the original mixin registration and feature description are replaced. No upstream
flagging classes, language assets or client resources are copied into production.
The candidate is written under ignored `tmp/pvp-26.2/`; install only after verification.

Source and rebuilt archive preserve the existing archive version/filename because
this is another local repack of the same port. Changed bytes are picked up by the
installer's Git hash comparison; no installer rebuild is needed.

## Verification — 18 October 2026

Isolated Minecraft **26.2**, NeoForge **26.2.0.90**, Java **26**, Better Party
**1.1.7**, Spell Engine **1.10.5+26.2**, Spell Power **1.6.1+26.2**.

The old flagging build failed the new no-flags eligibility check. The replacement
passed **29 dedicated-server checks** with ordinary `ServerPlayer` test entities:

- strangers can fight without any flag state; 20-point melee, arrow and each
  Arcane/Fire/Frost input remove **10 health**;
- the same PvE inputs and ownerless damage remove **20 health**;
- real Better Party membership and its incoming-damage handler block all five
  sources when party friendly fire is off, allow **10 health** when on, and
  restore normal half-damage PvP after leaving the party;
- vanilla team protection still blocks hits; allowed team hits are halved;
- `/pvpFlag` and `/pvpZone` are absent.

The test inserts party membership into Better Party's manager directly to avoid
simulating invitation UI/network sessions; it exercises actual damage events and
the installed Better Party protection code, not a mocked permission function.
An isolated integrated client loads both mods and verifies the old commands/HUD
classes are absent. The client shuts down normally, with a clean-HUD screenshot.
No public-server load or exhaustive modded-damage-source test is claimed.

Tests: `tests/PvpProbe.java`, `tests/RetentionClientProbe.java`. Build hash report:
`artifacts.json`. Local logs/screenshots remain ignored under `tmp/pvp-26.2/`
and `tmp/skill-overhaul/client/`.
