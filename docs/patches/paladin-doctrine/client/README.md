# Isolated native client verification

The final candidate passed **49 checks** in an actual Minecraft 26.2 / NeoForge 26.2.0.90 client with an integrated server on 28 October 2026. The client used the cached Java 25 runtime, a 3 GiB heap, a 1280×800 native window, an offline test identity, and an isolated copy of the previous `Punchy Flat` test world. Personal game folders supplied read-only libraries, natives and assets only. The client shut down cleanly after writing its results.

## Exact tested builds

| JAR | SHA-256 |
| --- | --- |
| `skill_tree-neoforge-1.6.1+26.2.jar` | `9e435262b25641895be0ecbd0b59718c4fd53ad18fb96febe3ab5497000117ff` |
| `paladins-neoforge-3.1.2+26.2.jar` | `b24221ea3be187b82e62befa35f38484f262c5c099507a68404080a3fcdfa596` |
| `armory-neoforge-1.5.2+26.2.jar` | `de5fdf74c0faae30e9f2c461b9df4f8c1dc3999d0972f3ae20a062793b179944` |

The complete isolated runtime, including the test-only probe, is pinned in [runtime-manifest.json](runtime-manifest.json). The result and individual observations are in [client-result.txt](client-result.txt) and [client-checks.txt](client-checks.txt).

## Observed behavior

- The connected real player unlocked the actual Paladin class root through the Skills API. Client Healing Power changed from 10 to 10.25; an ordinary Paladin reward changed it to 10.5.
- A real client hotbar-selection packet selected a healing staff. The server automatically moved it back into storage, the selected hand became legal, exactly one staff remained, and client Healing Power excluded its weapon bonus.
- A healing wand inserted into the real player's offhand was removed by the normal server tick, synchronized to the client and preserved exactly once.
- With all main slots full, a staff and wand moved into storage while safe stone stacks moved into the hands. The client retained exactly one staff, one wand and all 2,240 stone.
- The native action bar displayed `Your Paladin oath forbids healing staves and wands.` Its timer fell from 39 to 14 while the weapons remained stored, demonstrating that ordinary subsequent ticks did not repeat the warning.
- After resetting skills and unlocking the actual Priest root, both healing weapons remained equipped and the client received their native Healing Power.
- The client received 0.75 flat Healing Power from one Paladin plate piece and 3 from its full set. The other full sets supplied flat item modifiers of Crusader 4, Netherite Crusader 4.6, Justicar 5 and Lightbringer 5.
- Justicar's existing two-piece **+2 Healing Power set bonus** remained. Thus, at a base of 10, an equipped full Justicar set produced **17 total**, comprising 5 from items and 2 from its preserved set bonus. The unchanged native definition is `data/armory_rpgs/equipment_set/justicar.json` inside Armory.
- The native Skill Tree screen and its synchronized definitions showed the 0.25 root and ordinary rewards. Hovering the unlocked Paladin root displayed the weapon restriction in its native tooltip.

## Native screenshots

All screenshots were captured with `Screenshot.grab`, then visually inspected at their original 1280×800 resolution.

- [Localized oath warning](screenshots/01-oath-warning.png)
- [Full inventory: weapons stored and safe items in both hands](screenshots/02-full-inventory-return.png)
- [Priest holding healing weapons after respec](screenshots/03-priest-respec.png)
- [Actual Paladin root tooltip](screenshots/04-paladin-root-tooltip.png)

## Reproduction

The probe source is [PaladinClientProbe.java](PaladinClientProbe.java). Keep the runtime isolated under `tmp/paladin-doctrine/client/`, populate its `mods/` with the pinned dependency set and candidate JARs, and provide `saves/Punchy Flat` from an isolated fixture. The fixture used here was copied from `tmp/delight-batch/client-all/saves/Punchy Flat`; the initially suggested Rogue fixture was absent.

From the repository root:

```powershell
python docs/patches/paladin-doctrine/client/build-client-probe.py
python docs/patches/paladin-doctrine/client/launch-isolated-client.py --world
```

The launcher resolves assets/libraries from the existing cached NeoForge installation but uses the isolated directory for all writable game state. It writes `launcher-output.log`, the PID, result files and screenshots there. The probe automatically executes its connected-player fixtures and exits. Class unlocks use the actual Skills API rather than simulated mouse clicks; hotbar selection uses the native client packet.

## Scope and environment notes

This is a focused client runtime test, not a full-pack multiplayer or dedicated-server session. All-forbidden reserve, death, relog and immediate casting/attribute edge cases belong to the separate native server probe. This client fixture checks representative registered staff/wand equipment and all five requested armor tiers.

The final log contained expected offline-identity authentication/Realms/profile-key failures and two NeoForge shader sampler warnings. It contained no probe failures or doctrine exceptions. Earlier investigative runs preserved under the isolated folder used outdated test expectations (first the old armor values, then an expectation omitting Justicar's original set bonus); those were test assertions, not production defects. The final report above uses the corrected assertions and final candidate.
