package net.spell_engine.internals;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.paladins.entity.BarrierEntity;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_power.api.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Current-pack hooks for mobile Priest support and location-bound protection. */
public final class PriestUltimates {
 public static final String VITALITY="skill_tree_rpgs:lightwell_vitality";
 private static boolean installed;
 private static final Set<Projectile> INTERCEPTED=Collections.newSetFromMap(new WeakHashMap<>());
 private static final Map<SummonedEntity,LivingEntity> SUPPORT_TARGETS=new WeakHashMap<>();
 public static LivingEntity supportTarget(SummonedEntity well,LivingEntity normal){return well(well)?SUPPORT_TARGETS.get(well):normal;}
 static boolean well(Entity e){return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("paladins:lightwell");}
 public static void wellSummoned(SummonedEntity entity,net.spell_engine.api.spell.summon.SpellSummoned.Args args){
  if(!well(entity))return;
  var attribute=entity.getAttribute(SpellSchools.HEALING.getAttributeEntry());
  if(attribute!=null){for(var modifier:List.copyOf(attribute.getModifiers()))attribute.removeModifier(modifier.id());attribute.setBaseValue(.05*SpellPower.getSpellPower(SpellSchools.HEALING,args.owner).nonCriticalValue());}
 }
 public static void configure(ConfigFile.Effects config){
  WarriorTierTwo.entry(VITALITY,"Empowered Well",List.of(PriestRules.attr("minecraft:max_health",2,Operation.ADD_VALUE)),config);
  var entry=net.skill_tree_rpgs.effect.SkillEffects.entries.stream().filter(e->e.id.toString().equals(VITALITY)).findFirst().orElseThrow();
  OnRemoval.configure(entry.effect,c->{if(c.entity().getHealth()>c.entity().getMaxHealth())c.entity().setHealth(c.entity().getMaxHealth());});
  if(installed)return;installed=true;
  NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,(LivingIncomingDamageEvent event)->{
   var victim=event.getEntity();var source=event.getSource();
   if(victim.level().isClientSide()||source.is(DamageTypes.FELL_OUT_OF_WORLD)||source.is(DamageTypes.GENERIC_KILL))return;
   if(protectedNow(victim))event.setCanceled(true);
  });
  NeoForge.EVENT_BUS.addListener((EntityTickEvent.Pre event)->{
   if(event.getEntity() instanceof Projectile shot&&!shot.level().isClientSide()){sweep(shot);if(shot.isRemoved())event.setCanceled(true);}
  });
 }
 public static void wellTick(SummonedEntity entity){
  if(!well(entity)||entity.level().isClientSide())return;
  var owner=entity.getOwner();
  if(owner==null||!owner.isAlive()||owner.isRemoved()||owner.level()!=entity.level()){SUPPORT_TARGETS.remove(entity);entity.discard();return;}
  entity.setNoGravity(true);entity.setDeltaMovement(Vec3.ZERO);
  entity.setPos(owner.getX(),owner.getY()+owner.getBbHeight()+.45,owner.getZ());
  var target=select(entity,owner);if(target==null)SUPPORT_TARGETS.remove(entity);else SUPPORT_TARGETS.put(entity,target);entity.setTarget(target);
 }
 static boolean injured(LivingEntity e){return e.getHealth()<e.getMaxHealth()-.001f;}
 static boolean debuffed(LivingEntity e){return e.getActiveEffects().stream().anyMatch(PriestUltimates::cleansable);}
 static boolean cleansable(MobEffectInstance e){return e.getEffect().value().getCategory()==MobEffectCategory.HARMFUL&&!e.getEffect().is(Identifier.parse("minecraft:trial_omen"));}
 public static LivingEntity select(SummonedEntity entity,LivingEntity owner){
  boolean cleanse=PriestRules.node(owner,"tier_4_spell_1_modifier_1"),empowered=PriestRules.node(owner,"tier_4_spell_1_modifier_2");
  return PriestRules.nearby(owner,entity.position(),16).stream().filter(e->e!=entity&&e.isAlive()&&PartyRules.allies(owner,e)&&entity.hasLineOfSight(e))
   .filter(e->injured(e)||(cleanse&&debuffed(e))||(empowered&&levels(e)<2))
   .min(Comparator.<LivingEntity>comparingDouble(e->injured(e)?-(1-e.getHealth()/e.getMaxHealth()):1).thenComparingDouble(entity::distanceToSqr)).orElse(null);
 }
 public static int levels(LivingEntity target){var e=target.getEffect(PriestRules.effect(VITALITY));return e==null?0:e.getAmplifier()+1;}
 public static LivingEntity launchCaster(SpellExecution.ImpactContext context,Level world,LivingEntity caster,Holder<Spell> spell){
  // Retain the well as physical projectile owner so a mote can collide with its caster.
  // The existing effectiveCaster in the context still supplies party/impact attribution.
  return caster instanceof SummonedEntity&&well(caster)&&PriestRules.id(spell,"paladins:lightwell_orb")?caster:context.effectiveCaster(world);
 }
 public static void grantHeart(LivingEntity target,LivingEntity owner){
  float health=target.getHealth();int level=Math.min(2,levels(target)+1);
  target.addEffect(new MobEffectInstance(PriestRules.effect(VITALITY),120,level-1),owner);
  // The capacity is unfilled. Only the mote's subsequent normal heal adds HP.
  target.setHealth(Math.min(health,target.getMaxHealth()));
 }
 public static boolean moteProjectile(LivingEntity caster,Entity projectile,Entity hit,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(PriestRules.id(spell,"paladins:lightwell_orb")){
   Entity source=projectile instanceof Projectile p?p.getOwner():caster;
   if(source instanceof SummonedEntity summoned&&well(summoned)){
    var owner=summoned.getOwner();
    if(owner==null||!owner.isAlive()||!(hit instanceof LivingEntity target)||!PartyRules.allies(owner,target))return false;
    if(PriestRules.node(owner,"tier_4_spell_1_modifier_2"))grantHeart(target,owner);
    if(PriestRules.node(owner,"tier_4_spell_1_modifier_1")){
     for(var current:List.copyOf(target.getActiveEffects()))if(cleansable(current)&&target.removeEffect(current.getEffect()))break;
    }
   }
  }
  return SpellImpacts.projectileImpact(caster,projectile,hit,spell,context);
 }
 public static boolean contains(BarrierEntity barrier,LivingEntity target){
  if(!barrier.isAlive()||barrier.isRemoved()||target.level()!=barrier.level())return false;
  var spell=barrier.getSpellEntry();if(spell==null)return false;double r=spell.value().range;
  double dx=target.getX()-barrier.getX(),dz=target.getZ()-barrier.getZ();
  return dx*dx+dz*dz<=r*r&&target.getBoundingBox().maxY>=barrier.getY()&&target.getY()<=barrier.getY()+r;
 }
 public static boolean barrierAlly(BarrierEntity barrier,Entity target){var owner=barrier.getOwner();return owner!=null&&owner.isAlive()&&owner.level()==barrier.level()&&PartyRules.allies(owner,target);}
 public static boolean protectedNow(LivingEntity target){
  for(var barrier:target.level().getEntitiesOfClass(BarrierEntity.class,target.getBoundingBox().inflate(12)))
   if(contains(barrier,target)&&barrierAlly(barrier,target))return true;
  return false;
 }
 /** Native barrier used a five-tick immunity lease; replace it with exact live-volume checks. */
 public static void noImmunityLease(LivingEntity target,DamageType type,net.minecraft.tags.TagKey<DamageType> tag,Boolean pattern,boolean negate,int ticks){}
 public static List<Entity> barrierEntities(Level level,Entity barrier,AABB area){
  return level.getEntities(barrier,area).stream().filter(e->!(e instanceof LivingEntity living)||contains((BarrierEntity)barrier,living)).toList();
 }
 public static void barrierHit(BarrierEntity barrier,DamageSource source){if(source.getDirectEntity() instanceof Projectile shot)intercept(barrier,shot);}
 public static void sweep(Projectile shot){
  if(shot.isRemoved()||INTERCEPTED.contains(shot))return;
  Vec3 end=shot.position().add(shot.getDeltaMovement());AABB path=shot.getBoundingBox().expandTowards(shot.getDeltaMovement()).inflate(.1);
  BarrierEntity first=null;double distance=Double.POSITIVE_INFINITY;
  for(var barrier:shot.level().getEntitiesOfClass(BarrierEntity.class,path)){
   var owner=barrier.getOwner();if(owner==null||!owner.isAlive()||!barrier.isAlive())continue;
   if(shot.getOwner()!=null&&PartyRules.allies(owner,shot.getOwner()))continue;
   double d=entryTime(barrier,shot.position(),end);if(d>=0&&d<distance){distance=d;first=barrier;}
  }
  if(first!=null)intercept(first,shot);
 }
 /** Segment/cylinder intersection avoids blocking shots in square AABB corners. */
 public static double entryTime(BarrierEntity barrier,Vec3 start,Vec3 end){
  var spell=barrier.getSpellEntry();if(spell==null)return -1;
  double r=spell.value().range,lo=0,hi=1;Vec3 d=end.subtract(start);
  if(Math.abs(d.y)<1e-9){if(start.y<barrier.getY()||start.y>barrier.getY()+r)return -1;}
  else{double a=(barrier.getY()-start.y)/d.y,b=(barrier.getY()+r-start.y)/d.y;lo=Math.max(lo,Math.min(a,b));hi=Math.min(hi,Math.max(a,b));}
  double x=start.x-barrier.getX(),z=start.z-barrier.getZ(),a=d.x*d.x+d.z*d.z,c=x*x+z*z-r*r;
  if(a<1e-9){if(c>0)return -1;}
  else{double b=2*(x*d.x+z*d.z),disc=b*b-4*a*c;if(disc<0)return -1;double root=Math.sqrt(disc);lo=Math.max(lo,(-b-root)/(2*a));hi=Math.min(hi,(-b+root)/(2*a));}
  return lo<=hi?lo:-1;
 }
 public static void intercept(BarrierEntity barrier,Projectile shot){
  var owner=barrier.getOwner();var attacker=shot.getOwner();
  if(owner==null||!owner.isAlive()||owner.level()!=barrier.level()||!barrier.isAlive()||barrier.isRemoved()||shot.isRemoved())return;
  if(attacker!=null&&PartyRules.allies(owner,attacker))return;
  if(entryTime(barrier,shot.position(),shot.position().add(shot.getDeltaMovement()))<0||!INTERCEPTED.add(shot))return;
  if(owner!=null&&owner.isAlive()&&attacker instanceof LivingEntity living&&living.isAlive()&&PartyRules.harmfulAllowed(owner,living)
    &&PriestRules.node(owner,"tier_4_spell_2_modifier_2")){
   var spell=SpellRegistry.from(owner.level()).get(Identifier.parse("skill_tree_rpgs:barrier_retaliation")).orElseThrow();
   var context=new SpellExecution.ImpactContext(1,1,living.position(),SpellPower.getSpellPower(SpellSchools.HEALING,owner),SpellTarget.FocusMode.DIRECT,0);
   SpellImpacts.performImpacts(owner.level(),owner,living,barrier,spell,spell.value().impacts,context,false,null);
  }
  shot.discard();
 }
}
