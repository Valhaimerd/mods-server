# Wolf armor and Companion resistance

26 October 2026. Approved [design](../../superpowers/specs/2026-10-26-wolf-armor-balance-design.md). Minecraft 26.2 / NeoForge 26.2.0.90; Java 25 bytecode. Four same-filename archive overlays: Iron Wolf Armor, Adventurer Skills, Orb of Oblivion Puffish Compat (the existing pet bridge), and Penchant. Both client and server need matching archives and a restart.

## Protection

| Armor | Reduction |
| --- | ---: |
| Leather | 10% |
| Copper / Gold | 15% |
| Vanilla armadillo / Chainmail | 20% |
| Iron | 25% |
| Diamond | 35% |
| Netherite | 45% |

The installed mod explicitly opted its armor into vanilla Wolf full-absorption behavior. Lowering armor points would leave that branch intact. The replacement MixinWolf returns false for full absorption on these eight supported items. A narrow NeoForge incoming-damage listener applies the tier once, before native durability wear and other defenses; native wolf-armor-bypass sources remain unprotected. This also covers the armor-breaking hit. Unsupported items retain vanilla eligibility.

ModifyDefaultComponentsEvent removes only ARMOR and ARMOR_TOUGHNESS from supported armor defaults; knockback resistance, durability, repairs, dyeing, equip rules, enchantability and Netherite item fire resistance remain. The item tooltip shows its replacement damage-reduction percentage. Other armor and non-wolf entities are untouched. Deliberate custom item attribute overrides are outside this default-item balance.

Ordinary Companion resistance nodes grant 3% instead of subtracting 1 HP. Pack Leader grants 5%; Guardian Stead 10%. Their other rewards remain. The pet bridge caps the combined reduction at 25%, with no flat subtraction. Existing node IDs and allocations remain; there is no new refund/migration. A 20-damage hit becomes 11 with Netherite, then 8.25 at the pet-resistance cap, before unrelated defenses. Tiny hits are reduced proportionally instead of erased. Other pet types receive the same Companion-node rebalance.

## Unbreaking

Both Penchant Unbreaking definitions use dedicated supported-items and armor-curve tags that include vanilla and Iron Wolf Armor. The general armor/durability tags are not expanded: this does not grant Protection or Mending compatibility as a side effect. Other existing supported items remain.

Unbreaking stays capped at X, remains breakable and uses the existing native armor curve in either Durability Rework module state (about 20% wear prevented at I, 36.36% at X). It changes durability only. No permanent unbreakability effect is added.

Penchant previously advanced wear progression only for ServerPlayer equipment. Its existing ItemStack mixin now also admits owned wolves, forwarding supported wolf-armor wear to the native progression method with the resolved owner. Player progression is unchanged; unowned wolves and unrelated items held by mobs do not gain progression. No new Penchant dependency is added to Iron Wolf Armor.

## Reproduction and verification

```powershell
python docs/patches/wolf-armor-balance/build.py
python docs/patches/wolf-armor-balance/run_server.py --baseline
python docs/patches/wolf-armor-balance/run_server.py
python docs/patches/wolf-armor-balance/run_server.py --module-off
python docs/patches/wolf-armor-balance/verify.py --installed --runtime
```

The ignored `tmp/wolf-armor-balance/before/` holds exact inputs; `built/` contains candidates. `artifacts.json` records input/output hashes and the exact changed/added entries. The builder never rebuilds historical pet or Penchant sources wholesale; it patches only the current methods/resources.

Baseline runtime reproduced a Warden melee source with 20 damage causing zero HP loss through Netherite armor. Candidate results are recorded alongside this document. This is an isolated dedicated-server test, not a full-pack multiplayer playthrough. Personal installations/worlds are untouched. No commit/push requested.

**Runtime results:** 84 native checks pass with Durability Rework enabled, and 84 with it disabled. Coverage includes all tier coefficients, nonzero small hits, vanilla armor, removed armor/toughness, owner protection, native bypasses, pet cap/stacking/reset, all-tier Unbreaking support, no added Protection eligibility, armor durability curve, native equipped-wolf progression, player progression, enchanted breakage and the armor-breaking hit. Focused read-only review found no blocking issue. Active/fixed copies match the tested hashes; originals are archived under ignored `mod fixing/retired packs/wolf-armor-balance-20261026/`.

Handbook verification: 77 rendered pages reviewed, with detailed inspection of changed pages 19, 37 and 38. All 51 contents destinations/page numbers and rendered internal links resolve; both PDF copies match, and all 172 downloaded project icons render. No catalog, JAR-count, keybind or icon-source changes were needed.
