# Reactive Mage Wards, Fangs retirement and Scalar removal

2 November 2026. Approved design: all three mage Wards activate **after direct enemy damage**, rather than casting spells, with a **12s haste-affected base cooldown**. Their **6s duration**, **2 absorption HP per complete 6 matching power**, shared-capacity safeguards and secondary effects remain. The triggering hit lands normally. Self-damage, environmental damage and ongoing burning/Wither do not activate Wards. Melee, projectiles and sourced offensive spells qualify; party damage does not.

Fangs (`fangs_enchantment:fangs`) is retired from the registry, not merely hidden. Its standalone JAR is removed and its optional Thorns compatibility entry is cleaned. Existing Penchant retirement codecs are extended to strip only Fangs from equipped/stored enchantments and progress, preserving the item and its remaining enchantments. Existing enchantment retirement and Unbreaking X changes remain.

Scalar RPG Utils (`scalarrpgutils-26.2-1.2.1+pack.1.jar`) is removed. Its burnishers, components and Curios gloves/necklaces/rings are no longer available, including old items in saved worlds. No declared installed dependents or external item/recipe references were found. RPG Series class, skill, weapon, armor and jewelry mods remain; Curios itself remains.

Same-filename repacks update Skill Tree, Spell Engine and Penchant. Removed providers and original repacks are retained under ignored `mod fixing/retired packs/ward-hit-retirement-20261102`. Focused native checks cover Ward hits/exclusions, migrated JSON/NBT gear/books/progress and provider absence. No personal installation or live world is modified. Clients and server need matching files and a restart.
