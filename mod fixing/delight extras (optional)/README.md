# Delight Extras (Optional)

This folder contains optional add-ons for Farmer's Delight that are candidates for compatibility work on Minecraft 26.2 and NeoForge 26.2. They are a low-priority convenience, not a blocker for the main pack or for other requested mod fixes.

Do not start porting these add-ons while a user-requested urgent fix is in progress. Work on a specific add-on only when the user asks for it or makes these extras the current task. The JARs in this folder are inputs; check each JAR's embedded metadata and dependencies before choosing a source version or attempting a port. Do not copy an old input JAR into `mods/` as a 26.2-compatible release.

When an add-on is explicitly selected and its final 26.2 production JAR is complete, follow the replacement steps in [`../README.md`](../README.md): place the final JAR in `1 fixed packs`, remove the superseded active JAR from `mods/`, and install the final JAR there. Never promote a test compilation or intermediate build.

## Promoted ports - 27 October 2026

The supplied Ocean's Delight1.0.4, Large Meals1.0.1 and End's Delight2.6.1 inputs were source-ported and promoted to the active26.2 pack. Original1.21.1 inputs remain here for provenance, not direct installation. Current outputs/patches/tests: [Delight batch](../../docs/patches/delight-batch/README.md). Expanded Delight was already installed and is not one of these three.
