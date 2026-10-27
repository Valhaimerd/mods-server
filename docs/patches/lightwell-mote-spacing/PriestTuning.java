package net.spell_engine.internals;
import java.util.*;
import com.google.gson.Gson;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.internals.delivery.ProjectileLauncher;

/** Small overlay over the installed Priest hooks; no other class rules change. */
public final class PriestTuning {
 private static final Gson JSON=new Gson();
 public static Spell.Impact adjust(Spell.Impact impact,LivingEntity caster,Holder<Spell> spell){
  boolean penance=PriestRules.id(spell,"paladins:penance"),holy=PriestRules.id(spell,"paladins:holy_beam");
  if(!penance&&!holy)return impact;
  if(impact.action.type!=Spell.Impact.Action.Type.DAMAGE&&impact.action.type!=Spell.Impact.Action.Type.HEAL)return impact;
  var out=JSON.fromJson(JSON.toJson(impact),Spell.Impact.class);
  if(penance&&out.action.type==Spell.Impact.Action.Type.DAMAGE)out.action.damage.spell_power_coefficient=PriestRules.node(caster,"tier_3_spell_2_root")?.20f:.15f;
  if(holy){
   boolean improved=PriestRules.node(caster,"tier_2_spell_1_root"),grace=PriestRules.node(caster,"tier_2_spell_1_modifier_1"),searing=PriestRules.node(caster,"tier_2_spell_1_modifier_2");
   if(out.action.type==Spell.Impact.Action.Type.HEAL)out.action.heal.spell_power_coefficient=grace?.20f:improved?.15f:.10f;
   if(out.action.type==Spell.Impact.Action.Type.DAMAGE)out.action.damage.spell_power_coefficient=searing?.35f:improved?.25f:.20f;
  }
  return out;
 }
 public static float cooldown(LivingEntity caster,Holder<Spell> spell,float progress){
  if(PriestRules.id(spell,"paladins:penance"))return SpellParameters.getCooldownDuration(caster,spell)*(2f/3f+Math.clamp(progress,0,1)/3f);
  return PriestRules.cooldown(caster,spell,progress);
 }
 public static boolean projectile(LivingEntity caster,Entity projectile,Entity hit,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(!PriestRules.id(spell,"paladins:penance"))return PriestUltimates.moteProjectile(caster,projectile,hit,spell,context);
  if(caster.level().isClientSide())return false;
  Vec3 point=context.position()!=null?context.position():projectile.position();
  boolean livingContact=hit instanceof LivingEntity living&&living.isAlive();
  var recipients=PriestRules.nearby(caster,point,4);
  for(var target:recipients)PriestRules.penanceTarget(caster,target,projectile,spell,context.position(point));
  // A living ally/enemy contact grants owner support once, even outside the blast.
  // Terrain-only impacts do not grant remote buffs.
  if(livingContact&&!recipients.contains(caster))
   PriestRules.penanceTarget(caster,caster,projectile,spell,context.position(point));
  if(caster.level() instanceof ServerLevel world)world.sendParticles(ParticleTypes.END_ROD,point.x,point.y,point.z,18,1.1,.5,1.1,.12);
  return true;
 }
 public static List<LivingEntity> candidates(SummonedEntity well,LivingEntity owner){
  boolean cleanse=PriestRules.node(owner,"tier_4_spell_1_modifier_1"),hearts=PriestRules.node(owner,"tier_4_spell_1_modifier_2");
  var eligible=PriestRules.nearby(owner,well.position(),16).stream()
   .filter(e->e!=well&&e.isAlive()&&PartyRules.allies(owner,e)&&well.hasLineOfSight(e)).toList();
  var injured=eligible.stream().filter(PriestUltimates::injured)
   .sorted(Comparator.comparingDouble(LivingEntity::getHealth).thenComparingDouble(well::distanceToSqr).thenComparingInt(Entity::getId)).toList();
  if(!injured.isEmpty())return injured;
  return eligible.stream().filter(e->(cleanse&&PriestUltimates.debuffed(e))||(hearts&&PriestUltimates.levels(e)<2))
   .sorted(Comparator.<LivingEntity>comparingDouble(well::distanceToSqr).thenComparingInt(Entity::getId)).toList();
 }
 public static LivingEntity select(SummonedEntity well,LivingEntity owner){return candidates(well,owner).stream().findFirst().orElse(null);}
 public static List<LivingEntity> pair(SummonedEntity well,LivingEntity owner){
  var options=candidates(well,owner);if(options.isEmpty())return List.of();
  return List.of(options.getFirst(),options.size()>1?options.get(1):options.getFirst());
 }
 public static void shoot(Level world,LivingEntity caster,Entity target,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(!(caster instanceof SummonedEntity well)||!PriestUltimates.well(well)||!PriestRules.id(spell,"paladins:lightwell_orb")){
   ProjectileLauncher.shootProjectile(world,caster,target,spell,context);return;
  }
  var owner=well.getOwner();if(owner==null||!owner.isAlive()||owner.level()!=world)return;
  // One firing cycle: a fixed visual offset only when both motes share a recipient.
  var recipients=pair(well,owner);if(recipients.isEmpty())return;
  var first=recipients.getFirst();var second=recipients.get(1);
  ProjectileLauncher.shootProjectile(world,caster,first,spell,context.position(first.position()));
  if(first!=second){ProjectileLauncher.shootProjectile(world,caster,second,spell,context.position(second.position()));return;}
  ((net.spell_engine.utils.WorldScheduler)world).schedule(6,()->{
   if(well.isRemoved()||!well.isAlive()||well.level()!=world||well.getOwner()!=owner||owner.isRemoved()||!owner.isAlive()||owner.level()!=world
     ||second.isRemoved()||!second.isAlive()||second.level()!=world||!PartyRules.allies(owner,second)
     ||second.getBoundingBox().distanceToSqr(well.position())>256||!well.hasLineOfSight(second))return;
   ProjectileLauncher.shootProjectile(world,caster,second,spell,context.position(second.position()));
  });
 }
}
