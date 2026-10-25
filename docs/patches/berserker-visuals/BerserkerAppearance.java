package net.spell_engine.entity;

import net.spell_engine.api.effect.EntityTints;

/** The existing synchronized status-effect tint controls its own lifetime. */
public final class BerserkerAppearance {
 public static final int RED_TINT=EntityTints.scaled(0xFFFF0000,.5f);
 public static void register() {
  var entry=net.skill_tree_rpgs.effect.SkillEffects.entries.stream()
   .filter(e->e.id.toString().equals(WarriorUltimates.BERSERKER)).findFirst().orElseThrow();
  EntityTints.register(entry.effect,RED_TINT);
 }
 public static boolean sparkAt(long age) {
  int phase=(int)Math.floorMod(age,13);
  return phase==0||phase==4||phase==8;
 }
}
