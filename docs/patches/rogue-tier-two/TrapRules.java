package net.spell_engine.internals;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellCloud;
import net.spell_engine.internals.impact.SpellImpacts;
public final class TrapRules {
 public static final String DISRUPTION="rogues:tracking_disruption";
 private static boolean listening;
 public static void init(){if(listening)return;listening=true;
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event)->{
   var effect=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(DISRUPTION));
   if(event.getEntity() instanceof Mob&&effect.isPresent()&&event.getEntity().hasEffect(effect.get()))event.setNewAboutToBeSetTarget(null);
  });
 }
 public static boolean trap(SpellCloud cloud){var h=cloud.getSpellEntry();return h!=null&&h.is(Identifier.parse("rogues:bear_trap"));}
 public static EntityDimensions dimensions(float width,float height,SpellCloud cloud){return trap(cloud)?EntityDimensions.scalable(width,2.5f):WallRules.dimensions(width,height,cloud);}
 public static boolean hidden(SpellCloud cloud){return !cloud.isSpawning() && (!(cloud instanceof net.rogues.entity.BearTrapEntity trap)||!trap.isSprung());}
 public static void disrupt(LivingEntity caster){
  var effect=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(DISRUPTION)).orElseThrow();
  for(var e:caster.level().getEntities(caster,caster.getBoundingBox().inflate(4),e->e instanceof Mob&&e.isAlive()))
   if(e.distanceToSqr(caster)<=16&&PartyRules.harmfulAllowed(caster,e)){
    var mob=(Mob)e;PartyRules.addEffect(mob,new MobEffectInstance(effect,30),caster);mob.setTarget(null);
   }
 }
 private static boolean eligible(LivingEntity caster,Entity target,Vec3 origin,double radius){
  return target instanceof LivingEntity&&target.isAlive()&&!target.isSpectator()&&PartyRules.harmfulAllowed(caster,target)
   &&WallRules.intersects(target.getBoundingBox(),origin,(float)radius,2.5f);
 }
 public static boolean area(Spell.AreaImpact area,Holder<Spell> spell,LivingEntity caster,Entity exclude,Entity source,List<Spell.Impact> impacts,SpellExecution.ImpactContext context,boolean additional,Float radius){
  if(!(source instanceof SpellCloud cloud)||!trap(cloud))return SpellImpacts.lookupAndPerformAreaImpact(area,spell,caster,exclude,source,impacts,context,additional,radius);
  double hit=radius!=null?radius:area.combinedRadius(context.power().baseValue()),activation=hit*(2.0/3.0);Vec3 origin=cloud.position();
  var targets=cloud.level().getEntities(cloud,new AABB(origin.x-hit,origin.y,origin.z-hit,origin.x+hit,origin.y+2.5,origin.z+hit),e->eligible(caster,e,origin,hit));
  if(targets.stream().noneMatch(e->eligible(caster,e,origin,activation)))return false;
  boolean any=false;
  for(var target:targets)any|=SpellImpacts.performImpacts(cloud.level(),caster,target,cloud,spell,impacts,context.target(net.spell_engine.internals.target.SpellTarget.FocusMode.AREA),false,null);
  return any;
 }
}
