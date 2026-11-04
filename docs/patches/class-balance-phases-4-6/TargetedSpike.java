package net.spell_engine.internals;
import java.util.*;import java.io.*;import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import net.minecraft.core.*;import net.minecraft.core.particles.ParticleTypes;import net.minecraft.server.level.ServerLevel;import net.minecraft.world.entity.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.*;import net.spell_engine.api.spell.fx.ModelEffect;import net.spell_engine.internals.impact.SpellImpacts;import net.spell_engine.fx.ModelEffectHelper;
public final class TargetedSpike {
 private static List<ModelEffect> models;
 private static List<ModelEffect> models(){if(models==null)try(var reader=new InputStreamReader(TargetedSpike.class.getResourceAsStream("/mage-followup-models.json"),StandardCharsets.UTF_8)){models=new ArrayList<>();for(var row:JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("spike"))models.add(MageFollowup.JSON.fromJson(row,ModelEffect.class));}catch(IOException e){throw new UncheckedIOException(e);}return models;}
 public static boolean deliver(Level world,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext ctx,Vec3 ignored){
  if(!(world instanceof ServerLevel server)||!MageFollowup.active(caster)||targets.isEmpty()||!(targets.getFirst().entity() instanceof LivingEntity target)||!SpatialRules.enemyAllowed(caster,target)||caster.distanceToSqr(target)>Math.pow(24*caster.getScale(),2)||world.clip(new ClipContext(caster.getEyePosition(),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster)).getType()!=HitResult.Type.MISS)return false;
  Vec3 location=ground(world,target.position());if(location==null)return false;
  boolean glacial=MageFollowup.node(caster,"skill_tree_rpgs:frost_tier_2_spell_2_modifier_1"),fan=MageFollowup.node(caster,"skill_tree_rpgs:frost_tier_2_spell_2_modifier_2");
  Runnable hit=()->{if(!caster.isAlive()||caster.level()!=world)return;ModelEffectHelper.spawn(world,location,caster.getYRot(),models());server.sendParticles(ParticleTypes.SNOWFLAKE,location.x,location.y+.3,location.z,25,1,.5,1,.05);strike(caster,h,ctx,location,glacial?1:.8f,1,2);if(fan)((net.spell_engine.utils.WorldScheduler)world).schedule(20,()->{if(caster.isAlive()&&caster.level()==world){server.sendParticles(ParticleTypes.SNOWFLAKE,location.x,location.y+.7,location.z,80,2,1,2,.12);var fh=MageFollowup.spell(caster,"wizards:frost_fan_burst");strike(caster,fh,ctx,location,.2f,.5f,3);}});};
  if(glacial)hit.run();else{server.sendParticles(ParticleTypes.SNOWFLAKE,location.x,location.y+.1,location.z,18,1,.02,1,0);((net.spell_engine.utils.WorldScheduler)world).schedule(5,hit);}return true;
 }
 public static Vec3 ground(Level level,Vec3 target){BlockPos pos=BlockPos.containing(target.add(0,-.01,0));for(int i=0;i<5;i++){var shape=level.getBlockState(pos).getCollisionShape(level,pos);if(!shape.isEmpty())return new Vec3(target.x,pos.getY()+shape.bounds().maxY,target.z);pos=pos.below();}return null;}
 public static void strike(LivingEntity caster,Holder<Spell> h,SpellExecution.ImpactContext ctx,Vec3 at,float coefficient,float frozen,float radius){
  var impacts=new ArrayList<Spell.Impact>();for(var original:h.value().impacts){var x=MageFollowup.copy(original,Spell.Impact.class);if(x.action.type==Spell.Impact.Action.Type.DAMAGE)x.action.damage.spell_power_coefficient=coefficient;if(x.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"wizards:frozen".equals(x.action.status_effect.effect_id))x.action.status_effect.duration=frozen;impacts.add(x);}
  var area=new Spell.AreaImpact();area.radius=radius;area.area=new Spell.Target.Area();area.area.distance_dropoff=Spell.Target.Area.DropoffCurve.NONE;
  SpellImpacts.lookupAndPerformAreaImpact(area,h,caster,caster,null,impacts,ctx.position(at.add(0,.5,0)),false);
 }
}
