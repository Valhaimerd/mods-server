package net.spell_engine.internals;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.puffish.skillsmod.api.*;

/** Small ballistic near-miss correction; one target and a finite steering budget. */
@EventBusSubscriber(modid="spell_engine")
public final class ArrowAssist {
 static final String KEY="class_pack_arrow_assist";
 public static boolean archer(ServerPlayer p){return SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).flatMap(c->c.getSkill("418rsvnd17egwkse")).map(s->s.getState(p)==Skill.State.UNLOCKED).orElse(false);}
 public static double angle(Vec3 a,Vec3 b){return Math.acos(Math.clamp(a.normalize().dot(b.normalize()),-1,1));}
 public static Vec3 steer(Vec3 velocity,Vec3 desired,double budget){
  double angle=angle(velocity,desired),step=Math.min(Math.toRadians(.25),Math.min(budget,angle));
  if(angle<1e-8||step<=0)return velocity;
  var a=velocity.normalize();var b=desired.normalize();double fraction=step/angle;
  return a.scale(Math.sin((1-fraction)*angle)/Math.sin(angle)).add(b.scale(Math.sin(fraction*angle)/Math.sin(angle))).normalize().scale(velocity.length());
 }
 static boolean visible(AbstractArrow arrow,Vec3 point){return arrow.level().clip(new ClipContext(arrow.position(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,arrow)).getType()==HitResult.Type.MISS;}
 static boolean eligible(AbstractArrow a,ServerPlayer p,LivingEntity t){return t.isAlive()&&t.isAttackable()&&!t.isSpectator()&&PartyRules.harmfulAllowed(p,t)&&(!(t instanceof net.minecraft.world.entity.player.Player player)||(p.level().isPvpAllowed()&&p.canHarmPlayer(player)));}
 /** Return the closest point on the hitbox to the unassisted ballistic path. */
 static Vec3 nearMiss(AbstractArrow arrow,LivingEntity target){
  Vec3 origin=arrow.position(),point=origin,velocity=arrow.getDeltaMovement();double best=Double.MAX_VALUE;Vec3 aim=null;
  var box=target.getBoundingBox();
  for(int step=0;step<60&&point.distanceToSqr(origin)<=24*24;step++){
   // Sample each native step finely so high-speed arrows cannot skip the near-miss gate.
   Vec3 end=point.add(velocity);
   int samples=Math.max(1,(int)Math.ceil(velocity.length()/.2));
   for(int i=1;i<=samples;i++){
    Vec3 test=point.lerp(end,i/(double)samples);
    Vec3 closest=new Vec3(Math.clamp(test.x,box.minX,box.maxX),Math.clamp(test.y,box.minY,box.maxY),Math.clamp(test.z,box.minZ,box.maxZ));
    double d=test.distanceToSqr(closest);if(d<best){best=d;aim=closest;}
   }
   point=end;velocity=velocity.scale(.99);if(!arrow.isNoGravity())velocity=velocity.add(0,-.05,0);
  }
  return best<=.75*.75?aim:null;
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Pre event){
  if(!(event.getEntity() instanceof AbstractArrow a)||!(a.level() instanceof ServerLevel level)||!(a.getOwner() instanceof ServerPlayer p))return;
  var data=a.getPersistentData();
  if(!data.contains(KEY)){
   data.putInt(KEY,0);
   if(!archer(p)||a.getDeltaMovement().lengthSqr()<.01)return;
   LivingEntity selected=null;double nearest=Double.MAX_VALUE;
   for(var target:level.getEntitiesOfClass(LivingEntity.class,a.getBoundingBox().inflate(24),t->eligible(a,p,t))){
    Vec3 aim=nearMiss(a,target);if(aim==null||aim.distanceToSqr(a.position())>24*24||angle(a.getDeltaMovement(),aim.subtract(a.position()))>Math.toRadians(3)||!visible(a,aim))continue;
    double distance=aim.distanceToSqr(a.position());if(distance<nearest){nearest=distance;selected=target;}
   }
   if(selected==null)return;data.putInt(KEY,1);data.putString("assist_target",selected.getUUID().toString());data.putInt("assist_start",a.tickCount);data.putDouble("assist_budget",Math.toRadians(2));
  }
  if(data.getIntOr(KEY,0)!=1)return;
  Entity entity;try{entity=level.getEntity(UUID.fromString(data.getStringOr("assist_target","")));}catch(IllegalArgumentException invalid){data.putInt(KEY,0);return;}
  if(!(entity instanceof LivingEntity target)||!eligible(a,p,target)||!archer(p)||a.tickCount-data.getIntOr("assist_start",0)>=10||a.getDeltaMovement().lengthSqr()<.01){data.putInt(KEY,0);return;}
  Vec3 aim=target.getBoundingBox().getCenter();if(!visible(a,aim)){data.putInt(KEY,0);return;}
  Vec3 velocity=a.getDeltaMovement();if(velocity.dot(aim.subtract(a.position()))<=0){data.putInt(KEY,0);return;}
  double budget=data.getDoubleOr("assist_budget",0);Vec3 next=steer(velocity,aim.subtract(a.position()),budget);
  data.putDouble("assist_budget",Math.max(0,budget-angle(velocity,next)));a.setDeltaMovement(next);a.hurtMarked=true;
 }
}
