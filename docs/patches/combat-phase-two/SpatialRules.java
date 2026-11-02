package net.spell_engine.internals;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.target.SpellTarget;

/** Target eligibility is separate from harmful impact eligibility. */
@net.neoforged.fml.common.EventBusSubscriber(modid="spell_engine") public final class SpatialRules {
 @net.neoforged.bus.api.SubscribeEvent public static void tickState(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event){if(event.getEntity() instanceof LivingEntity e&&!e.level().isClientSide())((SpatialState)e).spatial$vanished(e.hasEffect(PaladinRevamp.effect("rogues:stealth")));}
 public static boolean selectionAllowed(Spell spell,LivingEntity caster,Entity target){return spell.target.type==Spell.Target.Type.AREA||spell.target.type==Spell.Target.Type.NONE||targetAllowed(caster,target);}
 public static boolean targetAllowed(LivingEntity caster,Entity target){return !(target instanceof LivingEntity living)||!(living.hasEffect(PaladinRevamp.effect("rogues:stealth"))||(living.level().isClientSide()&&((SpatialState)living).spatial$vanished()))||PartyRules.allies(caster,target);}
 public static boolean enemyAllowed(LivingEntity caster,Entity target){
  if(!(target instanceof LivingEntity living)||!living.isAlive()||living.isSpectator()||!living.isAttackable()||!targetAllowed(caster,target)||!PartyRules.harmfulAllowed(caster,target))return false;
  return !(living instanceof Player p)||(!p.getAbilities().instabuild&&(!(p.level() instanceof net.minecraft.server.level.ServerLevel server)||server.isPvpAllowed())&&(!(caster instanceof Player owner)||owner.canHarmPlayer(p)));
 }
 public static SpellTarget.SearchResult filter(SpellTarget.SearchResult result,LivingEntity caster,Holder<Spell> spell){
  if(result==null||spell==null||spell.value().target==null||spell.value().target.type==Spell.Target.Type.AREA||spell.value().target.type==Spell.Target.Type.NONE)return result;
  return new SpellTarget.SearchResult(result.entities().stream().filter(e->targetAllowed(caster,e)).toList(),result.location());
 }
 public static Entity followed(Entity target,SpellProjectile projectile){return target!=null&&projectile.getOwner() instanceof LivingEntity caster&&!targetAllowed(caster,target)?null:target;}
 public static void beforeTick(SpellProjectile projectile){
  Entity old=projectile.getFollowedTarget();if(old!=null&&followed(old,projectile)==null)projectile.setFollowedTarget(null);
  if(projectile.level().isClientSide()||!projectile.getSpellEntry().is(Identifier.parse("wizards:fire_slash"))||!(projectile.getOwner() instanceof LivingEntity owner))return;
  var current=projectile.getFollowedTarget();if(current!=null&&(!enemyAllowed(owner,current)||current.distanceToSqr(projectile)>64||!visible(projectile,current)))projectile.setFollowedTarget(null);
  if(projectile.getFollowedTarget()!=null&&projectile.tickCount%4!=0)return;
  var nearest=projectile.level().getEntitiesOfClass(LivingEntity.class,projectile.getBoundingBox().inflate(8),e->enemyAllowed(owner,e)&&e.distanceToSqr(projectile)<=64&&visible(projectile,e)).stream().min(Comparator.<LivingEntity>comparingDouble(e->e.distanceToSqr(projectile)).thenComparingInt(Entity::getId)).orElse(null);
  projectile.setFollowedTarget(nearest);
 }
 private static boolean visible(Entity from,Entity to){return from.level().clip(new ClipContext(from.position(),to.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,from)).getType()==HitResult.Type.MISS;}
}

