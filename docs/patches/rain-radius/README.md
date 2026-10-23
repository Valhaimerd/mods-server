# Rain of Arrows: six-block scatter

Rain of Arrows, Torrential Arrows and Endless Volley now scatter within a six-block radius instead of eight. Each arrow still splashes within 2.5 blocks; scatter describes landing positions, so splash can reach beyond the scatter disk. Base/Torrential/Endless retain 40/40/60 arrows, five/two/five-tick intervals, 40% ranged damage per impact and the 30-second haste-affected base cooldown. Native moving-target following and party protection remain intact.

Only one spell JSON and the affected English descriptions change in the current Archers and Skill Tree JARs. All classes and unrelated archive entries are preserved byte-for-byte. Replacements keep their active filenames; prior copies are ignored under `mod fixing/retired packs/rain-radius-2026-10-23/`.

The configuration assertion failed against the old eight-block value, then passed for all three variants. The isolated native launch/combat suite passed 2,842 Archer/Rogue/party measurements against the six-block candidates, including actual scheduled arrow positions around moving targets. This was not a client visual playthrough.

```
python docs/patches/rain-radius/build.py
python docs/patches/rain-radius/verify.py tmp/rain-radius/built
python docs/patches/rogue-tier-three/run_probe.py --candidates tmp/rain-radius/built
python docs/patches/rain-radius/verify.py
```

`artifacts.json` pins the input/output hashes and exact changed entries. Build inputs remain ignored in `tmp/rain-radius/before/`. The Archer source builder, native branch-radius assertions, raw skill reference, player handbook and PDF are synchronized. No commit or push was requested.
