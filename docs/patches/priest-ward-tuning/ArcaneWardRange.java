package net.spell_engine.internals;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
public final class ArcaneWardRange {
 public static boolean eligible(LivingEntity attacker, Entity caster) {
  return attacker.distanceToSqr(caster) <= 16 && PartyRules.harmfulAllowed(attacker, caster);
 }
}
