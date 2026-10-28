# Enhanced Cats replacement

29 October2026 ? Minecraft26.2 ? NeoForge26.2.0.90 ? Java25.

The supplied `EnhancedCats-NeoForge-26.2-1.2.1.jar` replaces `cutecats-neoforge-26.2-1.0.jar`. The removed archive identified itself as `kingdomcats` / Kingdom Cats Replacer1.2.2-neoforge.1, despite its filename. It is retained only in ignored retired packs. Enhanced Cats remains in `mods/` because it contains common cat behavior/networking plus client rendering; YACL remains in `mod store/`, GeckoLib remains installed for its existing consumers.

- Enhanced Cats SHA-256: `9d14cf4397733fcce03f524f66d74ffeb1a0c3bbd0cea3095a2e59fd38ef125f`; supplied bytes unchanged.
- Retired Cute Cats SHA-256: `b18b31a1f6d06dba251c540f5f181b7e3532b5fa2419ef52dea02182daea1adb`.
- Metadata requires NeoForge?26.2.0.88, Minecraft26.2 and client YACL. Its YACL range contains the literal `[YACL_VERSION_HERE,)`; this was inspected and did not prevent the isolated current client from loading with YACL3.9.5. No speculative metadata repack was made.
- Dedicated-server fixtures explicitly load Enhanced Cats without client YACL or Cute Cats. The actual client spawns and synchronizes a tamed cat and confirms the Enhanced Cats `CatGeoRender` renderer. These checks establish loading/rendering compatibility; they do not exhaustively test every cat behavior.

Official references: [Enhanced Cats](https://modrinth.com/mod/enhanced-cats), [exact NeoForge26.2 file](https://www.curseforge.com/minecraft/mc-mods/enhanced-cats/files/9231518). The handbook uses the author icon extracted unchanged from this exact JAR; provenance is recorded in its icon manifest. Final distribution remains307 synchronized JARs and23 client extras.
