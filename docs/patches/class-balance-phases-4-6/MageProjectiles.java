package net.spell_engine.internals;
import java.util.*;import net.minecraft.world.entity.*;import net.minecraft.world.phys.*;import net.minecraft.world.level.*;import net.spell_engine.entity.SpellProjectile;
public final class MageProjectiles {
 private static final Set<String> BASICS=Set.of("wizards:fireball","wizards:fire_blast","wizards:frostbolt","wizards:frost_shard","wizards:frost_lance");private static final Map<SpellProjectile,Vec3> AXES=new WeakHashMap<>();
 public static void beforeTick(SpellProjectile projectile){
  if(projectile.level().isClientSide()||projectile.getSpellEntry()==null||!BASICS.contains(projectile.getSpellEntry().unwrapKey().orElseThrow().identifier().toString())||!(projectile.getOwner() instanceof LivingEntity caster)||projectile.getDeltaMovement().lengthSqr()<.0001)return;
  Vec3 velocity=projectile.getDeltaMovement().normalize();Vec3 axis=AXES.computeIfAbsent(projectile,p->velocity);if(axis.dot(velocity)<.8){axis=velocity;AXES.put(projectile,axis);}final Vec3 forward=axis;
  var old=projectile.getFollowedTarget();if(old!=null&&!eligible(projectile,caster,old,forward))projectile.setFollowedTarget(null);
  if(projectile.getFollowedTarget()==null&&projectile.tickCount%2==0){var target=projectile.level().getEntitiesOfClass(LivingEntity.class,projectile.getBoundingBox().inflate(8),e->eligible(projectile,caster,e,forward)).stream().min(Comparator.comparingDouble(e->e.distanceToSqr(projectile))).orElse(null);projectile.setFollowedTarget(target);}
 }
 private static boolean eligible(SpellProjectile p,LivingEntity caster,Entity target,Vec3 axis){return SpatialRules.enemyAllowed(caster,target)&&p.distanceToSqr(target)<=64&&axis.dot(target.getBoundingBox().getCenter().subtract(p.position()).normalize())>=Math.cos(Math.toRadians(10))&&p.level().clip(new ClipContext(p.position(),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;}
}
