package net.spell_engine.internals;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.container.SpellContainerSource;
/** Per-projectile steering; never mutate a shared spell registry definition. */
public final class ArcherProjectileRules {
 public static float homing(Spell.ProjectileData data,SpellProjectile projectile){
  var spell=projectile.getSpellEntry();
  if(spell!=null&&spell.is(net.minecraft.resources.Identifier.parse("archers:magic_arrow"))
    &&projectile.getOwner() instanceof LivingEntity owner
    &&owner instanceof SpellContainerSource.Owner source
    &&source.getSpellContainers().modifiers().stream().anyMatch(h->h.is(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:archer_tier_4_spell_2_modifier_1"))))return 180;
  return data.homing_angle;
 }
}
