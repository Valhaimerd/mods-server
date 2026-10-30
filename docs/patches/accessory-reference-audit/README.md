# Accessory reference refresh — 30 October 2026

The original documentation-only audit covered the installed Nameless Trinkets1.2.4+26.2.1 and Artifacts15.1.3+26.2.1. That initial pass included no balance or runtime changes. The later [capped RPG balance](../accessory-rpg-balance/README.md) replaces both archives and updates these coverage inputs; its dedicated record contains current hashes and 171/181 isolated-server results.

- [Nameless effects](../nameless-balance/effects.md):42 retained wearable trinkets,12 disabled legacy items and6 supporting items. Owner copy: `mod fixing/Nameless Trinkets - effects.md`.
- [Artifacts effects](../artifacts-balance/effects.md):45 wearables,3 handheld/food items and the separate Mimic Spawn Egg. Owner copy: `mod fixing/Artifacts - effects.md`.
- Corrected stale inspection dates/archive hashes. Artifacts leading descriptions now show effective pack values instead of obsolete upstream values followed by “Planned” changes. Unchanged table cells explicitly show the default still applies. Removed obsolete skill-tree balance assumptions from the historical recommendations.
- Both installed hashes match the21 October shrinking/slot repair evidence:228 prior Nameless and67 prior Artifacts isolated-server checks. Those historical checks are not a new playtest of every item.

Run `python docs/patches/accessory-reference-audit/verify.py` from the repository root. It checks current archive identities/CRC, exact reference coverage, removed item policy, Artifacts packaged overrides and the restored Feet mapping. `audit.json` records the inputs. Unchanged settings describe bundled defaults, not a live server configuration dump.

The initial reference-only pass did not require a handbook rebuild. The subsequent capped-balance revision updates the handbook and PDF; no installer rebuild is needed. References remain separate from the player handbook to keep its accessory entries brief. Nothing is pushed unless requested.
