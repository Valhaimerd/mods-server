package net.spell_engine.internals;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.fx.*;
import net.spell_engine.fx.*;
import net.spell_engine.entity.SpellCloud;
import net.spell_engine.utils.SoundHelper;

/** Short native particle batches, no independent visual timers or audio loops. */
public final class PaladinVisuals {
 static final com.google.gson.Gson JSON=new com.google.gson.Gson();
 public static void ring(LivingEntity e,double radius,long rgba){if(e.level().isClientSide())return;var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.FLOAT).color(rgba).scale(.35f).batch(b->b.shape(ParticleGroup.Shape.CIRCLE).anchor(ParticleGroup.Anchor.GROUND).count(24).extent((float)radius).speed(.01f,.025f));ParticleHelper.sendBatches(e,List.of(group));}
 public static void shield(LivingEntity e){var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_holy,ParticleGroup.Motion.ASCEND).color(0xFFEAA0FFL).scale(.65f).batch(b->b.shape(ParticleGroup.Shape.PIPE).count(16).speed(.08f,.18f));ParticleHelper.sendBatches(e,List.of(group));}
 public static void rescue(LivingEntity e){shield(e);ring(e,1.4,0xFFEAA0FFL);var sound=new Sound("paladins:divine_protection_release");SoundHelper.playSound(e.level(),e,sound);}
 public static void defender(LivingEntity e){rescue(e);if(e.level() instanceof ServerLevel w)w.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,e.getX(),e.getY()+1.3,e.getZ(),5,.3,.25,.3,.01);ring(e,2,0xF3A04AFFL);}
 public static void bannerModel(PaladinUltimates.Banner b){var root=EffectDetails.data(b.caster);root.addProperty("banner_carrier",b.start+b.duration);EffectDetails.save(b.caster,root);}
 public static void moveBanner(PaladinUltimates.Banner b){} // Body renderer owns presentation; no world entity to chase its owner.
 public static void channelPulse(LivingEntity e,PaladinUltimates.Channel c,int stage){groundPulse(e);long color=new long[]{0xFFD05AFFL,0xFF7B20FFL,0xD92510FFL}[stage];ring(e,c.radius,color);if(c.pulses%4==0)ring(e,c.outer,0xCD7E4066L);var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.ASCEND).color(color).scale(.6f).batch(b->b.shape(ParticleGroup.Shape.PIPE).count(8+stage*4).speed(.1f,.25f));ParticleHelper.sendBatches(e,List.of(group));if(e.level() instanceof ServerLevel world)world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,e.getX(),e.getY()+.8,e.getZ(),4+stage*2,.35,.5,.35,.015);}
 public static void groundPulse(LivingEntity e){
  if(e.level().isClientSide())return;
  var id=Identifier.parse("paladins:purgatory_ground");var h=net.spell_engine.api.spell.registry.SpellRegistry.from(e.level()).get(id).orElseThrow();
  var data=h.value().deliver.clouds.getFirst();var cloud=new SpellCloud(e.level());cloud.setOwner(e);
  var floor=net.spell_engine.utils.TargetHelper.findSolidBlockBelow(null,e.position(),e.level(),-2f);
  cloud.setPos(floor==null?e.position():new Vec3(e.getX(),floor.y()+.02,e.getZ()));
  cloud.onCreatedFromSpell(id,data,new SpellExecution.ImpactContext().power(new net.spell_power.api.SpellPower.Result(net.spell_power.api.SpellSchools.HEALING,0,0,1)),data.time_to_live_seconds,new net.spell_engine.api.spell.Spell.Modifier.Cloud());e.level().addFreshEntity(cloud);
 }
 public static void explosion(LivingEntity e,int radius,double power){
  var meteor=net.spell_engine.api.spell.registry.SpellRegistry.from(e.level()).get(Identifier.parse("wizards:fire_meteor")).orElseThrow().value();
  var groups=new ArrayList<ParticleGroup>();
  for(var original:meteor.area_impact.visuals.particles){
   var copy=JSON.fromJson(JSON.toJson(original),ParticleGroup.class);
   if("spell_engine:area_effect_473".equals(copy.id))copy.appearance.scale=radius;
   groups.add(copy);
  }
  ParticleHelper.sendBatches(e,groups);SoundHelper.playSound(e.level(),e,meteor.area_impact.sound);
 }
 public static void cleansed(boolean removed,LivingEntity e){if(removed)shield(e);}
 public static void retaliation(LivingEntity e){ring(e,.8,0xF79232FFL);}
 public static void oath(LivingEntity e,com.google.gson.JsonObject snapshot){tierTwo(e,snapshot.has("judgement")&&snapshot.get("judgement").getAsBoolean()?6:4);}
 public static void tierTwo(LivingEntity e,double radius){ring(e,radius,0xFFE58AAAL);}
 public static void statusSignals(net.minecraft.server.level.ServerPlayer e){
  if(!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(e))return;var root=EffectDetails.data(e);var previous=EffectDetails.object(root,"paladin_visual_levels");boolean changed=false;
  for(String id:List.of("skill_tree_rpgs:redoubt","skill_tree_rpgs:vengeance","skill_tree_rpgs:sinful_retaliation_ready","skill_tree_rpgs:reinforced","paladins:blessed_strikes")){
   var active=e.getEffect(PaladinRevamp.effect(id));int level=active==null?0:active.getAmplifier()+1;int old=(int)EffectDetails.number(previous,id);if(level!=old){previous.addProperty(id,level);changed=true;if(level>old){var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.ASCEND).color(0xFFE58AFFL).scale(.4f).batch(b->b.shape(ParticleGroup.Shape.PIPE).count(5).speed(.04f,.09f));ParticleHelper.sendBatches(e,List.of(group));}}
  }
  if(changed)EffectDetails.save(e,root);
 }
}
