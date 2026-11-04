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

/** Launch-only near-miss assistance, never an in-flight target tracker. */
@EventBusSubscriber(modid="spell_engine")
public final class ArrowAssist {
 static final String KEY="archer_assist_v2";
 static final String ARCHER="418rsvnd17egwkse";
 static final Set<String> ROOTS=Set.of(ARCHER,"qr4fdarqqqm5ifoa","m1pc5vz73nx4b28c","akyr8k2dv9xsy70c","tdimeso8ajcwiygp","ov6ag9tjdhsmldil","63gwys64onqg00ry","e5ljj7jx6pmdjatf");
 public static boolean archer(ServerPlayer player){
  return SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).map(category->{
   var roots=category.streamUnlockedSkills(player).map(Skill::getId).filter(ROOTS::contains).toList();
   return roots.size()==1&&roots.getFirst().equals(ARCHER);
  }).orElse(false);
 }
 public static double angle(Vec3 a,Vec3 b){return Math.acos(Math.clamp(a.normalize().dot(b.normalize()),-1,1));}
 public static Vec3 steer(Vec3 velocity,Vec3 desired,double budget){
  if(velocity.lengthSqr()<1e-12||desired.lengthSqr()<1e-12)return velocity;
  double angle=angle(velocity,desired),step=Math.min(Math.toRadians(2),Math.min(budget,angle));
  if(angle<1e-8||angle>Math.PI-1e-6||step<=0)return velocity;
  var a=velocity.normalize();var b=desired.normalize();double fraction=step/angle;
  return a.scale(Math.sin((1-fraction)*angle)/Math.sin(angle)).add(b.scale(Math.sin(fraction*angle)/Math.sin(angle))).normalize().scale(velocity.length());
 }
 static boolean visible(AbstractArrow arrow,Vec3 point){return arrow.level().clip(new ClipContext(arrow.position(),point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,arrow)).getType()==HitResult.Type.MISS;}
 static boolean eligible(AbstractArrow a,ServerPlayer p,LivingEntity t){return t.isAlive()&&t.isAttackable()&&!t.isSpectator()&&SpatialRules.enemyAllowed(p,t)&&(!(t instanceof net.minecraft.world.entity.player.Player player)||(p.level().isPvpAllowed()&&p.canHarmPlayer(player)));}
 /** Keep the correction at the near edge of the hitbox, rather than snapping to its center. */
 static Vec3 nearMiss(AbstractArrow arrow,LivingEntity target){
  Vec3 origin=arrow.position(),point=origin,velocity=arrow.getDeltaMovement();double best=Double.MAX_VALUE;Vec3 aim=null;
  var box=target.getBoundingBox().deflate(.02);
  for(int step=0;step<60&&point.distanceToSqr(origin)<=24*24;step++){
   Vec3 end=point.add(velocity);int samples=Math.max(1,(int)Math.ceil(velocity.length()/.2));
   for(int i=1;i<=samples;i++){
    Vec3 test=point.lerp(end,i/(double)samples);
    Vec3 closest=new Vec3(Math.clamp(test.x,box.minX,box.maxX),Math.clamp(test.y,box.minY,box.maxY),Math.clamp(test.z,box.minZ,box.maxZ));
    double d=test.distanceToSqr(closest);if(d<best){best=d;aim=closest;}
   }
   point=end;velocity=velocity.scale(.99);if(!arrow.isNoGravity())velocity=velocity.add(0,-.05,0);
  }
  return best<=.4*.4?aim:null;
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Pre event){
  if(!(event.getEntity() instanceof AbstractArrow arrow)||!(arrow.level() instanceof ServerLevel level)||!(arrow.getOwner() instanceof ServerPlayer player))return;
  var data=arrow.getPersistentData();if(data.contains(KEY))return;
  // Mark every observed shot, including ineligible shots. A respec cannot add assistance later.
  data.putBoolean(KEY,true);
  if(arrow.tickCount>1||!archer(player)||arrow.getDeltaMovement().lengthSqr()<.01)return;
  LivingEntity selected=null;Vec3 aim=null;double nearest=Double.MAX_VALUE;
  for(var target:level.getEntitiesOfClass(LivingEntity.class,arrow.getBoundingBox().inflate(24),t->eligible(arrow,player,t))){
   if(target.distanceToSqr(arrow)>24*24)continue;
   Vec3 point=nearMiss(arrow,target);
   if(point==null||point.distanceToSqr(arrow.position())>24*24||angle(arrow.getDeltaMovement(),point.subtract(arrow.position()))>Math.toRadians(3)||!visible(arrow,point))continue;
   double distance=target.distanceToSqr(arrow);if(distance<nearest){nearest=distance;selected=target;aim=point;}
  }
  if(selected==null)return;
  data.putString("assist_target",selected.getUUID().toString());
  arrow.setDeltaMovement(steer(arrow.getDeltaMovement(),aim.subtract(arrow.position()),Math.toRadians(2)));
  arrow.hurtMarked=true;
 }
}
