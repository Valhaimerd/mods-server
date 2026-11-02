# Mage Ward tuning - 2 November 2026

Arcane, Flame and Frost Ward now last **6s**, retain their **15s haste-affected base cooldown**, and grant **2 absorption HP per complete 6 matching spell power**. For example, 24 power grants 8 absorption HP. Their other effects and the existing no-refill/shared-capacity absorption handling remain unchanged.

The same-filename Skill Tree replacement is installed in repository `mods/` and ignored fixed packs. Original: ignored `mod backup/mage-ward-tuning-2026-11-02`. [Exact hashes and focused verification](verification.json). Both client and server need the matching JAR and a restart; no configuration deletion is needed.

This is a four-entry JSON/localization overlay. Checks cover JSON values, native float scaling thresholds, ZIP integrity and preservation of every other entry, including all classes. No extended game tests were run, as requested. Earlier combat phase reports describe their historical builds; this tuning supersedes their Ward duration and power divisor.
