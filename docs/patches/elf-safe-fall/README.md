# Elf native safe fall distance

Replaces Elf slow falling and its resulting fall immunity with **+5 safe fall distance**, through Minecraft?s native `minecraft:safe_fall_distance` attribute. The ordinary safe distance becomes 8 blocks; longer falls deal normal fall damage. Falling velocity is untouched.

Both legacy fall handlers are removed, including the unused registration target that still contained an unconditional fall-distance reset. The named transient modifier is idempotent, reapplied for existing Elves through normal race reconciliation, and removed on race change or explicit attribute cleanup. Other race attributes and leaf climbing remain unchanged.

`build.py` repacks the installed `iourusraces-1.1.0.jar` without changing its filename. It changes three classes and the English/Spanish Elf descriptions. Candidate checks use an isolated NeoForge 26.2.0.90 server and native player fall damage; no personal worlds or installations are modified.

Verification: **8/8 native checks pass** for the +5 attribute, idempotence, untouched descending velocity/fall distance, an 8-block safe fall, a 12-block fall dealing 4 HP, and race-change/reset cleanup. Active and fixed-pack JARs match the runtime-tested SHA-256 in `artifacts.json`.

The same checks fail on the previous JAR in six relevant cases. The revised handbook retains 79 pages and 51 matching table-of-contents links; all pages render and the race page was visually reviewed.
