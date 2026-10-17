# Naturally Trimmed 3.5.0: appended trade range repair

The pack retains `mods/naturally_trimmed-3.5.0+26.1-neoforge.jar` and TradeView 1.0.5 for Minecraft 26.2 / NeoForge 26.2.0.90. This is a local same-version repair, not an upstream release. [Upstream source and LGPL-3.0 license](https://github.com/Imajo24I/Naturally-Trimmed) remain applicable.

## Cause and change

`VillagerTradesMixin.trimTradesWithoutDuplicates` originally called the trade generator and then trimmed `offers.subList(offers.size() - requestedCount, offers.size())`. The requested count is an upper bound: a small pool, duplicate filtering or a failed trade can append fewer offers. TradeView previews start with an empty list, so requesting two offers and receiving one produces `fromIndex = -1` and crashes the server tick. With existing offers, the same assumption can also trim older entries again.

The repair records `offers.size()` immediately before calling the generator and uses that saved size as the sublist start. Only newly appended offers are trimmed, including when none were added. Trade generation, duplicate filtering, trim chance, minimum level and other configuration are unchanged. Only `net/majo24/naturally_trimmed/mixin/VillagerTradesMixin.class` differs; metadata, mixin annotations, resources, license and all other classes are retained byte-for-byte.

## Rebuild

Preserve the original JAR as `tmp/naturally-trimmed-trades/upstream.jar`. Its SHA-256 must be `05497f47e977c6b10a2670a6b3484749ea66829680d56f15f95d8e2790deb63c` (also retained locally under `mod fixing/retired packs/naturally_trimmed-3.5.0+26.1-neoforge-before-trade-fix.jar`). Run from the repository root:

```powershell
python docs/patches/naturally-trimmed-trades/build.py
```

The builder uses Java 26 (emitting Java 25 bytecode) and cached ASM 9.10.1 from `.minecraft/libraries`. Override `TRADE_PATCH_JAVA` with the Java `bin` directory if needed. It fails on an unexpected input hash or bytecode shape, writes the same-name replacement under `tmp/naturally-trimmed-trades/`, and verifies exactly one changed class. The final replacement is also retained in `mod fixing/1 fixed packs/`; backups and test instances remain ignored.

## Runtime validation, 2026-10-17

An isolated dedicated server used Minecraft 26.2, NeoForge 26.2.0.90, Naturally Trimmed 3.5.0, TradeView 1.0.5 and the test-only `TradeProbe.java`. Its test JAR replaces the armorer level-two trade tag with a single vanilla chainmail-boots trade, forcing a real preview to return fewer trades than requested.

- Original JAR: normal initial villager trades work; actual `PreviewVillager.tradeview$preview()` reproduces `IndexOutOfBoundsException: fromIndex = -1` in Naturally Trimmed.
- Fixed JAR: **34 checks pass**. The actual preview returns its one available offer, restores the villager's original level, and preserves its current offers. The transformed mixin wrapper is also exercised with zero or two existing offers and zero, one, two or four appended offers. At 100% trim chance, new armor is trimmed and older armor remains unmodified. Both runs stop the test server normally after recording results.
- Baseline/fixed logs and result files are under `tmp/naturally-trimmed-trades/`. The test-only tag override, probe JAR and world are not part of the pack. Personal worlds and the live server were untouched. A full-pack multiplayer session was not run in this test.

`TradeProbe.java` is a NeoForge test mod entry point. Compile it against the cached mapped Minecraft/NeoForge, MixinExtras and the two target mod JARs. Package its classes with a `trade_probe` JavaFML mod descriptor and `data/minecraft/tags/villager_trade/armorer/level_2.json` containing `{"replace":true,"values":["minecraft:armorer/2/emerald_chainmail_boots"]}`. Run it only in an isolated server; it intentionally stops that server when finished.
