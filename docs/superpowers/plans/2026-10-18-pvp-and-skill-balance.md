# PvP 26.2 and skill rebalance

Follow-up implemented: the PvP archive is now a damage-only replacement. All
flagging, zones, cooldowns, commands, client indicators and networking were removed.
Better Party controls party friendly fire; vanilla combat permissions remain.
The final implementation passes 29 server checks with Better Party plus an
integrated-client check. The initial port plan below is retained as history.

User-approved implementation, inline execution; publish all resulting changes to main.

1. Revise structural tests, observe failure against installed skill JARs, then update
   `docs/patches/skill-overhaul/repack.py`. Ordinary Adventurer melee/ranged become
   2%/3%; caster school power 4%; Magistrate school power 12%; Aegis 15% magic,
   10% ranged, 6% movement. Martial's single ordinary penetration node becomes
   attack speed. Hunter adds 10% armor shred and 25% bow/crossbow projectile speed;
   replace its later ordinary projectile node with 5% armor shred, retain the earlier.
   Existing final Martial/Hunter damage bonuses remain 5%; ordinary damage changes
   also cover RPG class/weapon physical damage rewards. Priest/Paladin unchanged.
2. Compile matching upstream PvP Flagging 26.1.2 source against NeoForge 26.2, fix
   actual API incompatibilities, preserve resources/licenses, and rename for the
   actual game-version port. Enforce 50% attributed player-versus-player damage,
   default config 0.5; preserve flag/zone/network functionality. Keep original pending
   input ignored until verified final archive can move to fixed packs.
3. Test parsed skills and real player modifiers. Test actual attributed melee,
   projectile and Arcane/Fire/Frost damage on two server players, PvE unchanged,
   flags, team behavior, config persistence, and bow/crossbow armor penetration.
   Load the port in an isolated client to catch client linkage/mixin problems.
4. Synchronize design/planning, source docs, catalog, icons, counts and handbook PDF;
   visually inspect affected pages and verify contents links. Review final changes,
   commit and push main, verify remote commit. Personal installations untouched.

Attribute investigation: installed Puffish Attributes' armor wrapper uses the living
attacker from DamageSource.getEntity(), with no melee-only gate; arrows retain shooter
ownership. Verify with an armored target before reporting ranged support.
