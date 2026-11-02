package net.spell_engine.internals;
import java.util.Set;import net.minecraft.core.Holder;import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.damagesource.DamageTypes;import net.spell_engine.api.spell.Spell;
public final class WardHitRules {
 private static final Set<String> WARDS=Set.of("skill_tree_rpgs:arcane_tier_3_passive_2","skill_tree_rpgs:fire_tier_3_passive_2","skill_tree_rpgs:frost_tier_3_passive_2");
 public static boolean allowed(Holder<Spell> spell,SpellTriggers.Event event){
  if(!WARDS.contains(spell.unwrapKey().orElseThrow().identifier().toString()))return true;
  var source=event.damageSource;
  return event.type==Spell.Trigger.Type.DAMAGE_TAKEN&&event.stage==Spell.Trigger.Stage.POST&&event.damageAmount>0&&source!=null&&source.getEntity() instanceof LivingEntity enemy&&source.getDirectEntity()!=null&&enemy!=event.player&&PartyRules.harmfulAllowed(enemy,event.player)&&!source.is(DamageTypes.ON_FIRE)&&!source.is(DamageTypes.WITHER)&&!source.is(DamageTypes.FALL)&&!source.is(DamageTypes.IN_FIRE)&&!source.is(DamageTypes.LAVA)&&!source.is(DamageTypes.FREEZE)&&!source.is(DamageTypes.DROWN);
 }
}
