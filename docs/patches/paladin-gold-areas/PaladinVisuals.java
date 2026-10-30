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
 public static void ring(LivingEntity e,double radius,long rgba){if(e.level().isClientSide())return;if(rgba==0xFFE58AFFL&&radius>=3){goldArea(e,radius,true);return;}var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.FLOAT).color(rgba).scale(.35f).batch(b->b.shape(ParticleGroup.Shape.CIRCLE).anchor(ParticleGroup.Anchor.GROUND).count(24).extent((float)radius).speed(.01f,.025f));ParticleHelper.sendBatches(e,List.of(group));}
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
 public static void oath(LivingEntity e,com.google.gson.JsonObject snapshot){
  if(e.level().isClientSide())return;double radius=snapshot.has("judgement")&&snapshot.get("judgement").getAsBoolean()?6:4;
  goldArea(e,radius,false);
  var burst=ParticleGroupBuilder.magic(SpellEngineParticles.magic_holy,ParticleGroup.Motion.BURST).color(0xFFE58AFFL).glow(true).scale(.8f).batch(b->b.shape(ParticleGroup.Shape.SPHERE).count(36).speed(.15f,.55f));
  var pillar=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.ASCEND).color(0xFFF0B8FFL).glow(true).scale(.6f).batch(b->b.shape(ParticleGroup.Shape.PILLAR).count(20).widthFactor(1.5f).speed(.15f,.4f));
  ParticleHelper.sendBatches(e,List.of(burst,pillar));
 }
 public static void tierTwo(LivingEntity e,double radius){goldArea(e,radius,false);}
 public static List<ParticleGroup> goldAreaGroups(double radius,boolean attached){
  var edge=ParticleGroupBuilder.zone(SpellEngineParticles.area_circle_1).color(0xFFD56AFFL).glow(true).opacity(.8f).scale((float)radius).playbackSpeed(attached?1.25f:.65f);
  var light=ParticleGroupBuilder.zone(SpellEngineParticles.area_effect_293).color(0xFFD56AFFL).glow(true).opacity(.18f).scale((float)radius).playbackSpeed(attached?1.25f:1f);
  if(attached){edge.attachedToGround();light.attachedToGround();}
  var perimeter=edge.batch(b->b.shape(ParticleGroup.Shape.PILLAR).anchor(ParticleGroup.Anchor.GROUND).count(1).widthFactor(0).speed(0));
  var glow=light.batch(b->b.shape(ParticleGroup.Shape.PILLAR).anchor(ParticleGroup.Anchor.GROUND).count(1).widthFactor(0).speed(0));
  var sparks=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.ASCEND).color(0xFFE58AFFL).glow(true).scale(.3f).batch(b->b.shape(ParticleGroup.Shape.PILLAR).anchor(ParticleGroup.Anchor.GROUND).widthFactor(0).extent((float)radius).count((float)Math.min(48,radius*5)).speed(.01f,.035f));
  return List.of(perimeter,glow,sparks);
 }
 public static void goldArea(LivingEntity e,double radius,boolean attached){if(!e.level().isClientSide())ParticleHelper.sendBatches(e,goldAreaGroups(radius,attached));}
 public static void divineStrength(LivingEntity caster){if(PaladinTierOne.node(caster,"spell_1_modifier_1"))goldArea(caster,4,false);}
 public static void sealOfLight(LivingEntity caster,SpellExecution.ImpactContext context){if(context.channelTickIndex()==4&&PaladinTierOne.node(caster,"spell_2_modifier_2"))goldArea(caster,4,false);}

 public static void statusSignals(net.minecraft.server.level.ServerPlayer e){
  if(!net.skill_tree_rpgs.compat.PaladinDoctrine.isPaladin(e))return;var root=EffectDetails.data(e);var previous=EffectDetails.object(root,"paladin_visual_levels");boolean changed=false;
  for(String id:List.of("skill_tree_rpgs:redoubt","skill_tree_rpgs:vengeance","skill_tree_rpgs:sinful_retaliation_ready","skill_tree_rpgs:reinforced","paladins:blessed_strikes")){
   var active=e.getEffect(PaladinRevamp.effect(id));int level=active==null?0:active.getAmplifier()+1;int old=(int)EffectDetails.number(previous,id);if(level!=old){previous.addProperty(id,level);changed=true;if(level>old){var group=ParticleGroupBuilder.magic(SpellEngineParticles.magic_spark,ParticleGroup.Motion.ASCEND).color(0xFFE58AFFL).scale(.4f).batch(b->b.shape(ParticleGroup.Shape.PIPE).count(5).speed(.04f,.09f));ParticleHelper.sendBatches(e,List.of(group));}}
  }
  if(changed)EffectDetails.save(e,root);
 }
}
