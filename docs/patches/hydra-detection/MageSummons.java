package net.spell_engine.internals;
import com.google.gson.*;import net.spell_engine.api.spell.summon.*;import net.spell_engine.entity.SummonedEntity;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.phys.*;import net.minecraft.core.BlockPos;import net.spell_engine.api.effect.EntityActionsAllowed;
public final class MageSummons {
 public static SpellSummoned.Args prepared(SpellSummoned.Args args){
  if(!args.spell.is(MageFollowup.id("wizards:fire_hydra"))||!MageFollowup.JSON.toJson(args.attribute_scaling).contains("ancient_hydra"))return args;
  JsonObject tree=MageFollowup.JSON.toJsonTree(args.behaviour).getAsJsonObject();scaleRings(tree);var behaviour=MageFollowup.JSON.fromJson(tree,SummonBehaviour.class);behaviour.targeting.detection_range.value=20;return new SpellSummoned.Args(args.owner,args.spell,behaviour,args.attribute_scaling,args.context);
 }
 private static void scaleRings(JsonElement e){if(e.isJsonArray()){for(var child:e.getAsJsonArray())scaleRings(child);}else if(e.isJsonObject()){var o=e.getAsJsonObject();if(o.has("id")&&o.get("id").getAsString().contains("area_effect")&&o.has("appearance")){var a=o.getAsJsonObject("appearance");if(a.has("scale"))a.addProperty("scale",a.get("scale").getAsDouble()*3);}for(var row:o.entrySet())if(row.getValue().isJsonObject()||row.getValue().isJsonArray())scaleRings(row.getValue());}}
 public static void tick(SummonedEntity hydra){
  if(hydra.level().isClientSide()||!SummonRules.ancient(hydra)||!hydra.isAlive())return;
  // Detection is deliberately independent of the unchanged breath hit volume.
  LivingEntity chosen=null;boolean chosenInRange=false;double nearest=400;
  for(var enemy:hydra.level().getEntitiesOfClass(LivingEntity.class,hydra.getBoundingBox().inflate(20))){
   double distance=hydra.distanceToSqr(enemy);if(distance>400||!visibleEnemy(hydra,enemy))continue;
   boolean inRange=inFireRange(hydra,enemy);
   if(chosen==null||inRange&&!chosenInRange||inRange==chosenInRange&&distance<nearest){chosen=enemy;chosenInRange=inRange;nearest=distance;}
  }
  if(hydra.getTarget()!=chosen)hydra.setTarget(chosen);
  LivingEntity target=chosen;if(target==null||chosenInRange||!EntityActionsAllowed.fromEffects(hydra.getActiveEffects().stream().map(net.minecraft.world.effect.MobEffectInstance::getEffect).toList()).canMove())return;
  Vec3 to=target.position().subtract(hydra.position()).multiply(1,0,1);if(to.length()<Math.max(3,hydra.getBbWidth()*.5+target.getBbWidth()*.5))return;var speed=hydra.getAttribute(Attributes.MOVEMENT_SPEED);double ratio=speed==null||speed.getBaseValue()<=0?1:Math.clamp(speed.getValue()/speed.getBaseValue(),0,1);Vec3 step=to.normalize().scale(.05*ratio);
  if(!hydra.level().noCollision(hydra,hydra.getBoundingBox().move(step))||hydra.level().getBlockState(BlockPos.containing(hydra.position().add(step).add(0,-.15,0))).getCollisionShape(hydra.level(),BlockPos.containing(hydra.position().add(step).add(0,-.15,0))).isEmpty())return;hydra.move(MoverType.SELF,PurgatoryBoundary.clip(hydra,step));
 }
 private static boolean visibleEnemy(SummonedEntity hydra,LivingEntity target){return target!=hydra&&target.isAlive()&&!target.isSpectator()&&SpatialRules.enemyAllowed(hydra,target)&&hydra.level().clip(new net.minecraft.world.level.ClipContext(SummonRules.mouth(hydra),target.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,hydra)).getType()==HitResult.Type.MISS;}
 public static boolean inFireRange(SummonedEntity hydra,LivingEntity target){return target!=null&&hydra.distanceToSqr(target)<=144&&SummonRules.mouth(hydra).distanceToSqr(target.getBoundingBox().getCenter())<=144&&visibleEnemy(hydra,target);}
 public static boolean canFire(SummonedEntity hydra){return inFireRange(hydra,hydra.getTarget());}
}
