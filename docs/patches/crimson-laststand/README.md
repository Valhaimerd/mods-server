# Crimson Strikes and Last Stand nerfs — 24 October 2026

Crimson Strikes heals **10% of combined dual-wield power once per successful Mutilate cast**, rather than per enemy. The first melee hit that reduces an eligible enemy's health or absorption spends the cast's heal; later targets cannot heal again. Empty casts, invulnerable/rejected hits and protected targets do not heal. Each new cast gets its own allowance. The original leech sound and particles play once with that heal.

Last Stand no longer includes its **0.6-second damage-immunity pulse**. Its five health/healing pulses, Regeneration I, knockback immunity, durations, cooldowns, Juggernaut and Sentinel remain unchanged. Improved Last Stand extends duration without adding damage immunity.

Same-filename Rogues & Warriors, Skill Tree and Spell Engine overlays preserve all other entries byte-for-byte. The former per-target healing modifier is removed; two narrow method wrappers scope a temporary healing frame to the native melee delivery and check actual damage after each native attack. A `finally` block clears/restores the frame even on failure or nested delivery. No saved effect or persistent player map is added. Critical bonuses do not enlarge the fixed 10% healing.

`build.py` pins current originals under ignored `tmp/crimson-laststand/before`, compiles Java-25 classes and builds candidates. `artifacts.json` records hashes and exact changed resources. `run_probe.py --baseline` reproduced **12 HP healed from three targets instead of the required 4 HP at 40 dual power**. The candidate passes **908 isolated Warrior/shield/party checks**, including zero/one/three-target healing, rejected hits and five Last Stand pulses without immunity. This focused fixture excludes the separate global player-attribute/PvP mod so the earlier raw-power regression expectations remain comparable; that mod is not modified by this patch.

Both client and server require the matching updates and a restart. Previous active/fixed archives remain in ignored `retired packs/crimson-laststand-20261024`. Personal installations/worlds are untouched. No commit or push requested.
