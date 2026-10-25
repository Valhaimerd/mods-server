package net.spell_engine.internals;

import java.util.*;
import com.google.gson.Gson;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.entity.*;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.*;

public final class PriestRules {
 public static final String SHIELD="skill_tree_rpgs:priest_penance_shield",SERENITY="skill_tree_rpgs:priest_serenity_shield",AURA="skill_tree_rpgs:ascension_power";
 private static final Gson JSON=new Gson();
 private static boolean installed;
 private record Shield(int level){}
 private static final Map<LivingEntity,Map<MobEffect,Shield>> SHIELDS=new WeakHashMap<>();
 public static boolean id(Holder<Spell> spell,String key){return spell.is(Identifier.parse(key));}
 public static boolean node(LivingEntity caster,String suffix){return caster instanceof SpellContainerSource.Owner owner && owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:priest_"+suffix)));}
 public static float pulseOffset(Holder<Spell> spell){return id(spell,"paladins:levitate")||id(spell,"paladins:penance")?0:.5f;}
 public static SpellExecution.ImpactContext normalize(Holder<Spell> spell,SpellExecution.ImpactContext context){
  return id(spell,"paladins:holy_beam")||id(spell,"paladins:penance")?context.channeled(1):context;
 }
 public static boolean deliver(Level world,LivingEntity caster,Holder<Spell> spell,SpellTarget.SearchResult targets,SpellExecution.ImpactContext context,java.util.function.Consumer<SpellExecution.DeliveryCompletion> completion){
  return PassiveRules.deliver(world,caster,spell,targets,normalize(spell,context),completion);
 }
 public static float cooldown(LivingEntity caster,Holder<Spell> spell,float progress){
  float q=Math.clamp(progress,0,1);
  if(id(spell,"paladins:holy_beam"))return net.spell_engine.internals.SpellParameters.getCooldownDuration(caster,spell)*(.5f+.5f*q);
  if(id(spell,"paladins:penance"))return net.spell_engine.internals.SpellParameters.getCooldownDuration(caster,spell)*(8f/14f+6f/14f*q);
  return ThirdTierRules.cooldown(caster,spell,progress);
 }
 public static net.spell_engine.rpg_series.config.AttributeModifier attr(String key,float amount,Operation operation){return new net.spell_engine.rpg_series.config.AttributeModifier(key,amount,operation);}
 public static void configure(ConfigFile.Effects config){
  WarriorTierTwo.entry(SHIELD,"Penance Shield",List.of(attr("minecraft:max_absorption",1,Operation.ADD_VALUE)),config);
  WarriorTierTwo.entry(SERENITY,"Serenity",List.of(attr("minecraft:max_absorption",1,Operation.ADD_VALUE)),config);
  var attrs=new ArrayList<net.spell_engine.rpg_series.config.AttributeModifier>();
  for(String key:List.of("minecraft:attack_damage","ranged_weapon:damage","spell_power:arcane","spell_power:fire","spell_power:frost","spell_power:lightning","spell_power:soul"))
   if(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(key)).isPresent())attrs.add(attr(key,.02f,Operation.ADD_MULTIPLIED_BASE));
  WarriorTierTwo.entry(AURA,"Ascension",attrs,config);
  config.effects.put("skill_tree_rpgs:hysteria",new EffectConfig(List.of(attr("minecraft:attack_speed",.01f,Operation.ADD_MULTIPLIED_BASE),attr("ranged_weapon:haste",.01f,Operation.ADD_MULTIPLIED_BASE),attr("spell_power:haste",.01f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:chastise",new EffectConfig(List.of(attr("spell_engine:damage_taken",.01f,Operation.ADD_MULTIPLIED_BASE))));
  if(!installed){
   installed=true;
   net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event)->{
    if(event.getEntity().level().isClientSide())return;
    var map=SHIELDS.get(event.getEntity());if(map!=null&&event.getEffectInstance()!=null)map.remove(event.getEffectInstance().getEffect().value());
   });
   net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired event)->{
    if(event.getEntity().level().isClientSide())return;
    var map=SHIELDS.get(event.getEntity());if(map!=null&&event.getEffectInstance()!=null)map.remove(event.getEffectInstance().getEffect().value());
   });
  }
  SpellHandlers.registerCustomDelivery(Identifier.parse("priest_pack:ascension"),PriestRules::ascension);
  SpellHandlers.registerCustomImpact(Identifier.parse("priest_pack:sanctuary"),(spell,power,caster,target,context)->{
   if(!(target instanceof LivingEntity living)||!PartyRules.allies(caster,target))return new SpellHandlers.ImpactResult(false,false);
   int amp=(int)Math.round(.06*power.nonCriticalValue());
   boolean ok=living.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,amp),caster);
   return new SpellHandlers.ImpactResult(ok,false);
  });
  SpellHandlers.registerCustomImpact(Identifier.parse("priest_pack:mass_dispel"),(spell,power,caster,target,context)->{
   if(!(target instanceof LivingEntity living)||!PartyRules.allies(caster,target))return new SpellHandlers.ImpactResult(false,false);
   int count=0;
   for(var current:List.copyOf(living.getActiveEffects()))
    if(current.getEffect().value().getCategory()==MobEffectCategory.HARMFUL&&!current.getEffect().is(Identifier.parse("minecraft:trial_omen"))){
     if(living.removeEffect(current.getEffect())&&++count>=2)break;
    }
   return new SpellHandlers.ImpactResult(count>0,false);
  });
 }
 public static Spell.Impact impact(LivingEntity caster,Holder<Spell> spell,Spell.Impact input,SpellExecution.ImpactContext context,Entity target){
  Spell.Impact out=WarriorRules.impact(caster,spell,input,context,target);
  if(id(spell,"paladins:circle_of_healing")&&out.action.type==Spell.Impact.Action.Type.HEAL){
   out=JSON.fromJson(JSON.toJson(out),Spell.Impact.class);out.action.heal.spell_power_coefficient=node(caster,"tier_3_spell_1_root")?.60f:.50f;return out;
  }
  if(id(spell,"paladins:penance")&&out.action.type==Spell.Impact.Action.Type.DAMAGE){
   out=JSON.fromJson(JSON.toJson(out),Spell.Impact.class);out.action.damage.spell_power_coefficient=node(caster,"tier_3_spell_2_root")?.30f:.25f;return out;
  }
  if(!id(spell,"paladins:holy_beam"))return out;
  out=JSON.fromJson(JSON.toJson(out),Spell.Impact.class);
  boolean improved=node(caster,"tier_2_spell_1_root"),grace=node(caster,"tier_2_spell_1_modifier_1"),sear=node(caster,"tier_2_spell_1_modifier_2");
  if(out.action.type==Spell.Impact.Action.Type.HEAL)out.action.heal.spell_power_coefficient=grace?.30f:improved?.20f:.15f;
  if(out.action.type==Spell.Impact.Action.Type.DAMAGE){out.action.damage.spell_power_coefficient=sear?.50f:improved?.35f:.30f;out.action.damage.knockback=grace?2:1;}
  return out;
 }
 public static List<LivingEntity> nearby(LivingEntity caster,Vec3 point,double radius){
  var found=new ArrayList<>(caster.level().getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius),e->e.isAlive()&&e.getBoundingBox().distanceToSqr(point)<=radius*radius));
  if(caster.isAlive()&&!found.contains(caster)&&caster.getBoundingBox().distanceToSqr(point)<=radius*radius)found.add(caster);
  return found;
 }
 public static Holder<MobEffect> effect(String key){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(key)).orElseThrow();}
 public static void stack(LivingEntity target,String key,int amount,int cap,int ticks,LivingEntity caster){
  Holder<MobEffect> effect=effect(key);var old=target.getEffect(effect);int before=old==null?0:old.getAmplifier()+1;
  int after=Math.min(cap,before+amount);float absorption=target.getAbsorptionAmount();
  target.addEffect(new MobEffectInstance(effect,ticks,after-1),caster);
  target.setAbsorptionAmount(Math.min((float)target.getAttributeValue(Attributes.MAX_ABSORPTION),absorption+Math.max(0,after-before)));
 }
 public static void statusStack(LivingEntity target,String key,int cap,int ticks,LivingEntity caster){
  var effect=effect(key);var old=target.getEffect(effect);int level=Math.min(cap,old==null?1:old.getAmplifier()+2);
  target.addEffect(new MobEffectInstance(effect,ticks,level-1),caster);
 }
 public static boolean projectile(LivingEntity caster,Entity projectile,Entity hit,Holder<Spell> spell,SpellExecution.ImpactContext context){
  if(!id(spell,"paladins:penance"))return SpellImpacts.projectileImpact(caster,projectile,hit,spell,context);
  if(caster.level().isClientSide())return false;
  Vec3 point=context.position()!=null?context.position():projectile.position();
  for(var target:nearby(caster,point,5))penanceTarget(caster,target,projectile,spell,context.position(point));
  if(caster.level() instanceof ServerLevel world)world.sendParticles(ParticleTypes.END_ROD,point.x,point.y,point.z,18,1.4,.5,1.4,.12);
  return true;
 }
 public static void penanceTarget(LivingEntity caster,LivingEntity target,Entity projectile,Holder<Spell> spell,SpellExecution.ImpactContext context){
   int shieldTicks=node(caster,"tier_3_spell_2_root")?80:60;
   if(PartyRules.allies(caster,target)){
    stack(target,SHIELD,1,10,shieldTicks,caster);
    if(node(caster,"tier_3_spell_2_modifier_1"))statusStack(target,"skill_tree_rpgs:hysteria",10,80,caster);
   }else{
    SpellImpacts.performImpacts(caster.level(),caster,target,projectile,spell,spell.value().impacts,context,false,Spell.Impact.Action.Type.DAMAGE);
    if(node(caster,"tier_3_spell_2_modifier_2"))statusStack(target,"skill_tree_rpgs:chastise",10,80,caster);
   }
 }
 public static boolean ascension(Level world,Holder<Spell> spell,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext context,Vec3 location){
  if(world.isClientSide())return false;
  SpellImpacts.performImpacts(world,caster,caster,caster,spell,spell.value().impacts,context,false,null);
  int ticks=node(caster,"tier_2_spell_2_root")?160:120;
  int pulse=context.channelTickIndex();
  for(var target:nearby(caster,caster.position(),8)){
   if(PartyRules.allies(caster,target)){
    statusStack(target,AURA,4,ticks,caster);
    if(node(caster,"tier_2_spell_2_modifier_2")){
     int amount=1+(int)Math.round(.1*context.power().nonCriticalValue());
     stack(target,SERENITY,amount,4*amount,ticks,caster);
    }
    if(target!=caster&&node(caster,"tier_2_spell_2_modifier_1")&&target.getBoundingBox().distanceToSqr(caster.position())<=9)
     SpellImpacts.performImpacts(world,caster,target,caster,spell,spell.value().impacts,context,false,null);
   }else{
    double x=caster.getX()-target.getX(),z=caster.getZ()-target.getZ();if(x*x+z*z<.0001)x=.01;
    target.knockback(pulse>=3?5:2,x,z,world.damageSources().magic(),0,true);
   }
  }
  return true;
 }
 /** Existing stack refreshes never reconstruct spent absorption from total capacity. */
 public static void absorptionStarted(LivingEntity entity,int amplifier,int perLevel,MobEffect effect){
  if(entity.level().isClientSide())return;
  var map=SHIELDS.computeIfAbsent(entity,k->new HashMap<>());
  var previous=map.get(effect);int old=previous!=null?previous.level:0;
  int level=amplifier+1;map.put(effect,new Shield(level));
  entity.setAbsorptionAmount(Math.min((float)entity.getAttributeValue(Attributes.MAX_ABSORPTION),entity.getAbsorptionAmount()+perLevel*Math.max(0,level-old)));
 }
}
