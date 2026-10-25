package local.audit;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.*;
import net.paladins.entity.BarrierEntity;
import net.spell_engine.entity.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.summon.SpellSummoned;
import net.spell_engine.internals.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_power.api.*;
public class UltimatesProbe {
 static PriestProbe a;
 static SummonedEntity summon(){
  var h=a.spell("paladins:lightwell");var d=h.value().impacts.getFirst().action.summon;
  var e=(SummonedEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("paladins:lightwell")).create(a.world,EntitySpawnReason.MOB_SUMMONED);
  e.onSummonedBySpell(new SpellSummoned.Args(a.p,h,d.behaviour,d.attribute_scaling,a.context(h,0)));e.setPos(a.p.position());a.world.addFreshEntity(e);return e;
 }
 static BarrierEntity barrier(){
  var h=a.spell("paladins:barrier");SpellImpacts.performImpacts(a.world,a.p,a.p,a.p,h,h.value().impacts,a.context(h,0),false,null);
  var scheduler=(net.spell_engine.utils.WorldScheduler)a.world;scheduler.updateScheduledTasks();for(var task:Map.copyOf(scheduler.getScheduledTasks()).entrySet())if(task.getKey()<=a.world.getGameTime()+1){scheduler.getScheduledTasks().remove(task.getKey());List.copyOf(task.getValue()).forEach(Runnable::run);}
  return a.world.getEntitiesOfClass(BarrierEntity.class,a.p.getBoundingBox().inflate(20)).stream().max(Comparator.comparingInt(Entity::getId)).orElseThrow();
 }
 static void mote(SummonedEntity well,LivingEntity target)throws Exception{
  var h=a.spell("paladins:lightwell_orb");var context=new SpellExecution.ImpactContext(1,1,target.position(),new SpellPower.Result(h.value().school,2,0,1.5),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0).effectiveCaster(a.p);
  var shot=new SpellProjectile(a.world,well,well.getX(),well.getEyeY(),well.getZ(),SpellProjectile.Behaviour.FLY,h,context,new Spell.ProjectileData.Perks());
  var method=SpellProjectile.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);method.setAccessible(true);method.invoke(shot,new EntityHitResult(target,target.getBoundingBox().getCenter()));shot.discard();
 }
 public static void run(PriestProbe probe)throws Exception{
  a=probe;a.eq("Lightwell requested cooldown",a.spell("paladins:lightwell").value().cost.cooldown.duration,80);
  var healing=a.p.getAttribute(SpellSchools.HEALING.getAttributeEntry());double saved=healing.getBaseValue();boolean added=!a.world.players().contains(a.p);if(added)a.world.addNewPlayer(a.p);
  try{healing.setBaseValue(40);a.p.removeAllEffects();a.p.setHealth(100);a.equip();well();barriers();}
  finally{healing.setBaseValue(saved);a.p.removeAllEffects();a.p.setHealth(a.p.getMaxHealth());a.equip();if(added)a.world.players().remove(a.p);}
 }
 static void well()throws Exception{
  var h=a.spell("paladins:lightwell");var orb=a.spell("paladins:lightwell_orb");a.eq("Mote range",orb.value().range,16);a.eq("Mote velocity",orb.value().deliver.projectile.launch_properties.velocity,.6);a.eq("Mote own power coefficient",orb.value().impacts.getFirst().action.heal.spell_power_coefficient,1);a.eq("Mote interval",orb.value().cost.cooldown.duration,.8);
  var well=summon();a.eq("Well inherited power",SpellPower.getSpellPower(SpellSchools.HEALING,well).nonCriticalValue(),2);a.eq("Well cannot be pushed",well.isPushable()?1:0,0);a.eq("Well cannot be attacked",well.isAttackable()?1:0,0);
  a.p.setPos(3,80,2);PriestUltimates.wellTick(well);a.eq("Well tracks owner x",well.getX(),3);a.eq("Well tracks head y",well.getY(),a.p.getY()+a.p.getBbHeight()+.45);
  var pet=EntityTypes.WOLF.create(a.world,EntitySpawnReason.COMMAND);pet.setOwnerReference(EntityReference.of(a.p.getUUID()));pet.setPos(4,80,3);pet.setNoAi(true);a.world.addFreshEntity(pet);pet.setHealth(2);
  var enemy=a.mob(3,4);enemy.setHealth(1);PriestUltimates.wellTick(well);a.eq("Well prioritizes injured owned pet",well.getTarget()==pet?1:0,1);pet.setHealth(pet.getMaxHealth());a.p.setHealth(98);PriestUltimates.wellTick(well);a.out.add("Well caster target diagnostic target="+well.getTarget()+" hp="+a.p.getHealth()+" max="+a.p.getMaxHealth()+" allies="+PartyRules.allies(a.p,a.p)+" sight="+well.hasLineOfSight(a.p)+" directSelection="+PriestUltimates.select(well,a.p));a.eq("Well retargets injured caster",well.getTarget()==a.p?1:0,1);
  mote(well,a.p);a.eq("Native mote heals 100 percent own power",a.p.getHealth(),100);
  a.equip("skill_tree_rpgs:priest_tier_4_spell_1_modifier_1");a.p.addEffect(new MobEffectInstance(MobEffects.POISON,200));a.p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200));PriestUltimates.wellTick(well);a.eq("Full-health debuffed caster eligible",well.getTarget()==a.p?1:0,1);mote(well,a.p);a.eq("First mote removes one debuff",a.p.getActiveEffects().stream().filter(e->e.getEffect().value().getCategory()==MobEffectCategory.HARMFUL).count(),1);mote(well,a.p);a.eq("Second mote cleanses without hidden cooldown",a.p.getActiveEffects().stream().filter(e->e.getEffect().value().getCategory()==MobEffectCategory.HARMFUL).count(),0);
  a.p.removeAllEffects();a.equip("skill_tree_rpgs:priest_tier_4_spell_1_modifier_2");a.p.setHealth(90);PriestUltimates.grantHeart(a.p,a.p);a.eq("Capacity grant unfilled",a.p.getHealth(),90);a.eq("One new heart",a.p.getMaxHealth(),102);mote(well,a.p);a.eq("Second heart capacity",a.p.getMaxHealth(),104);a.eq("Mote alone heals new capacity",a.p.getHealth(),92);a.p.setHealth(90);PriestUltimates.grantHeart(a.p,a.p);a.eq("Capped refresh does not heal",a.p.getHealth(),90);a.eq("Capped hearts",a.p.getMaxHealth(),104);a.eq("Hearts refresh six seconds",a.p.getEffect(PriestRules.effect(PriestUltimates.VITALITY)).getDuration(),120);
  mote(well,pet);a.eq("Pet independent heart stack",PriestUltimates.levels(pet),1);a.eq("Owner stacks unaffected by pet",PriestUltimates.levels(a.p),2);a.p.removeEffect(PriestRules.effect(PriestUltimates.VITALITY));a.eq("Heart removal restores max",a.p.getMaxHealth(),100);
  var extra=Identifier.parse("probe:other_health");pet.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(extra,6,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));float before=pet.getMaxHealth()-2;pet.setHealth(pet.getMaxHealth());
  for(int i=0;i<121;i++)pet.tick();a.eq("Natural six-second heart expiry removes levels",PriestUltimates.levels(pet),0);a.eq("Heart expiry preserves other health modifier",pet.getMaxHealth(),before);a.eq("Heart expiry clamps health without damage",pet.getHealth()<=pet.getMaxHealth()?1:0,1);pet.getAttribute(Attributes.MAX_HEALTH).removeModifier(extra);
  enemy.discard();pet.discard();a.p.setHealth(100);a.equip();PriestUltimates.wellTick(well);a.eq("Well idles with no useful ally",well.getTarget()==null?1:0,1);
  a.world.players().remove(a.p);PriestUltimates.wellTick(well);a.eq("Orphan well discarded",well.isRemoved()?1:0,1);a.world.players().add(a.p);
 }
 static void barriers()throws Exception{
  a.p.removeAllEffects();a.p.setPos(0,80,0);a.equip();var h=a.spell("paladins:barrier");a.eq("Barrier cast one second",h.value().active.cast.duration,1);a.eq("Barrier cooldown",h.value().cost.cooldown.duration,60);a.eq("Barrier radius",h.value().range,10);
  var b=barrier();a.p.setPos(b.getX(),b.getY(),b.getZ());a.eq("Barrier base lifetime",b.getTimeToLive(),200);a.eq("Barrier protects current ally",PriestUltimates.protectedNow(a.p)?1:0,1);
  var pet=EntityTypes.WOLF.create(a.world,EntitySpawnReason.COMMAND);pet.setOwnerReference(EntityReference.of(a.p.getUUID()));pet.setPos(b.getX()+1,b.getY(),b.getZ());a.world.addFreshEntity(pet);a.eq("Barrier protects owner pet",PriestUltimates.protectedNow(pet)?1:0,1);
  for(var source:List.of(a.world.damageSources().magic(),a.world.damageSources().onFire(),a.world.damageSources().wither(),a.world.damageSources().freeze(),a.world.damageSources().fall())){
   pet.setHealth(pet.getMaxHealth());pet.invulnerableTime=0;float hp=pet.getHealth();pet.hurtServer(a.world,source,3);a.eq("Barrier ordinary damage blocked "+source.typeHolder().unwrapKey(),pet.getHealth(),hp);
  }
  pet.setHealth(pet.getMaxHealth());hpReset(pet);float dotHp=pet.getHealth();MobEffects.POISON.value().applyEffectTick(a.world,pet,0);a.eq("Actual poison tick blocked",pet.getHealth(),dotHp);
  hpReset(pet);PriestRules.effect("spell_engine:bleed").value().applyEffectTick(a.world,pet,0);a.eq("Actual Bleed tick blocked",pet.getHealth(),dotHp);
  b.tick();a.eq("No lingering native immunity lease",((net.spell_engine.api.entity.LivingEntityImmunity.Owner)pet).getImmunities().size(),0);
  pet.setPos(b.getX()+11,b.getY(),b.getZ());pet.invulnerableTime=0;pet.setHealth(pet.getMaxHealth());float hp=pet.getHealth();pet.hurtServer(a.world,a.world.damageSources().magic(),3);a.eq("Outside barrier damage restored",hp-pet.getHealth(),3);
  pet.setPos(b.getX()+1,b.getY(),b.getZ());pet.setHealth(pet.getMaxHealth());pet.invulnerableTime=0;hp=pet.getHealth();pet.hurtServer(a.world,a.world.damageSources().fellOutOfWorld(),3);a.eq("Void damage is not protected",hp-pet.getHealth(),3);
  a.p.setHealth(90);a.p.setHealth(a.p.getHealth()-1);a.eq("Intentional health cost not canceled",a.p.getHealth(),89);
  var enemy=a.mob(b.getX()+2,b.getZ());a.eq("Barrier does not protect enemy",PriestUltimates.protectedNow(enemy)?1:0,0);
  for(int i=0;i<4;i++)b.tick();a.eq("Barrier repeatedly pushes hostile entity",enemy.getDeltaMovement().horizontalDistanceSqr()>0?1:0,1);
  a.eq("Projectile square corner not intercepted",PriestUltimates.entryTime(b,new Vec3(b.getX()+9,b.getY()+1,b.getZ()+9),new Vec3(b.getX()+9.5,b.getY()+1,b.getZ()+9.5))<0?1:0,1);
  a.equip("skill_tree_rpgs:priest_tier_4_spell_2_modifier_2");var shot=(Projectile)EntityTypes.SNOWBALL.create(a.world,EntitySpawnReason.COMMAND);shot.setOwner(enemy);shot.setPos(b.getX()+11,b.getY()+1,b.getZ());shot.setDeltaMovement(-2,0,0);enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);enemy.setHealth(1000);enemy.invulnerableTime=0;PriestUltimates.sweep(shot);a.eq("Hostile projectile intercepted",shot.isRemoved()?1:0,1);a.eq("Retaliation native 25 percent power",1000-enemy.getHealth(),10);PriestUltimates.intercept(b,shot);a.eq("Repeated projectile contact no extra retaliation",1000-enemy.getHealth(),10);
  var second=(Projectile)EntityTypes.SNOWBALL.create(a.world,EntitySpawnReason.COMMAND);second.setOwner(enemy);second.setPos(shot.position());second.setDeltaMovement(-2,0,0);PriestUltimates.sweep(second);a.eq("Distinct projectiles each retaliate",1000-enemy.getHealth(),20);
  var friendly=(Projectile)EntityTypes.SNOWBALL.create(a.world,EntitySpawnReason.COMMAND);friendly.setOwner(a.p);friendly.setPos(b.getX()+11,b.getY()+1,b.getZ());friendly.setDeltaMovement(-2,0,0);PriestUltimates.sweep(friendly);a.eq("Friendly projectile unaffected",friendly.isRemoved()?1:0,0);friendly.discard();
  a.eq("Native square projectile collider disabled",b.canBeHitByProjectile()?1:0,0);
  for(boolean allied:new boolean[]{true,false}){
   var arrow=(Projectile)EntityTypes.ARROW.create(a.world,EntitySpawnReason.COMMAND);arrow.setOwner(allied?a.p:enemy);arrow.setNoGravity(true);arrow.setPos(b.getX()+11,b.getY()+2,b.getZ()+(allied?0:9));arrow.setDeltaMovement(-1,0,0);a.world.addFreshEntity(arrow);
   for(int i=0;i<3;i++)arrow.tick();a.eq("Actual arrow native collision excludes "+(allied?"friendly":"square corner"),arrow.isRemoved()?1:0,0);a.eq("Actual arrow crosses AABB face "+allied,arrow.getX()<b.getX()+10?1:0,1);arrow.discard();
  }
  b.discard();a.eq("Removed barrier stops protection",PriestUltimates.protectedNow(a.p)?1:0,0);a.equip("skill_tree_rpgs:priest_tier_4_spell_2_root");b=barrier();a.eq("Improved barrier lasts twelve seconds",b.getTimeToLive(),240);a.eq("Improved barrier no cooldown deduction",SpellParameters.getCooldownDuration(a.p,h),60);b.discard();enemy.discard();pet.discard();a.p.setHealth(100);a.equip();
  var haste=a.p.getAttribute(net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:haste").orElseThrow());double old=haste.getBaseValue();
  try{haste.setBaseValue(100);double normal=SpellParameters.getCooldownDuration(a.p,a.spell("paladins:lightwell"));haste.setBaseValue(200);a.eq("Lightwell cooldown responds to haste",SpellParameters.getCooldownDuration(a.p,a.spell("paladins:lightwell")),normal/2);a.eq("Barrier haste cast",SpellParameters.getCastDuration(a.p,h.value()),.5);a.eq("Haste does not shorten well active lifetime",a.spell("paladins:lightwell").value().impacts.getFirst().action.summon.behaviour.lifespan.active_seconds,16);}finally{haste.setBaseValue(old);}
 }
 static void hpReset(LivingEntity target){target.invulnerableTime=0;}
}
