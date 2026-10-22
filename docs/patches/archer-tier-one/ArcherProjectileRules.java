package net.spell_engine.entity;
import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.PartyRules;
import net.spell_engine.internals.container.SpellContainerSource;
/** Archer-only projectile behavior; shared registry data is never mutated. */
public final class ArcherProjectileRules {
 public static boolean is(Holder<Spell> spell,String id){return spell!=null&&spell.is(Identifier.parse(id));}
 public static boolean magic(SpellProjectile p){return is(p.getSpellEntry(),"archers:magic_arrow");}
 public static boolean node(LivingEntity owner,String suffix){return owner instanceof SpellContainerSource.Owner source&&source.getSpellContainers().modifiers().stream().anyMatch(h->is(h,"skill_tree_rpgs:"+suffix));}
 public static float homing(Spell.ProjectileData data,SpellProjectile p){
  if(magic(p)&&p.getOwner() instanceof LivingEntity owner&&node(owner,"archer_tier_4_spell_2_modifier_1"))return 180;
  return data.homing_angle;
 }
 public static void beforeTick(SpellProjectile p){
  if(!magic(p))return;
  // Native range is distance traveled. The rework instead expires by time.
  p.range=p.tickCount>=200?-1:Float.MAX_VALUE;
  if(!p.level().isClientSide()&&p.getOwner() instanceof LivingEntity owner){
   double radius=32;
   var targets=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),e->e.isAlive()&&e.isAttackable()&&e.distanceToSqr(p)<=radius*radius&&!p.impactHistory.contains(e.getId())&&PartyRules.harmfulAllowed(owner,e));
   var nearest=targets.stream().min(Comparator.comparingDouble(e->e.distanceToSqr(p))).orElse(null);
   p.setFollowedTarget(nearest);
  }
 }
 public static HitResult collision(Entity entity,Predicate<Entity> predicate){
  if(!(entity instanceof SpellProjectile p)||!magic(p))return ProjectileUtil.getHitResultOnMoveVector(entity,predicate);
  Vec3 start=p.position(),end=start.add(p.getDeltaMovement());
  var hit=ProjectileUtil.getEntityHitResult(p.level(),p,start,end,p.getBoundingBox().expandTowards(p.getDeltaMovement()).inflate(1),predicate.and(e->!p.impactHistory.contains(e.getId())&&p.getOwner() instanceof LivingEntity owner&&PartyRules.harmfulAllowed(owner,e)));
  return hit==null?BlockHitResult.miss(end,p.getDirection(),net.minecraft.core.BlockPos.containing(end)):hit;
 }
 public static Vec3 scatter(Vec3 ringOffset,Holder<Spell> spell,Level world){
  return is(spell,"archers:rain_of_arrows")?ringOffset.scale(Math.sqrt(world.getRandom().nextDouble())):ringOffset;
 }
 public static Vec3 rainLocation(Holder<Spell> spell,Entity target,Vec3 location){return is(spell,"archers:rain_of_arrows")&&target!=null?target.position():location;}
 public static Entity rainTarget(Holder<Spell> spell,Entity target){return is(spell,"archers:rain_of_arrows")?null:target;}
 public static float stashDuration(Spell.Delivery.StashEffect stash,Holder<Spell> spell,LivingEntity caster){
  return is(spell,"archers:power_shot")&&node(caster,"archer_tier_2_spell_1_root")?10:stash.duration;
 }
}
