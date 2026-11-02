package net.spell_engine.internals;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;
/** Continuous inward acceleration; candidates are local and refreshed only five times a second. */
public final class PurgatoryPull {
 private static final Map<LivingEntity,State> STATES=new WeakHashMap<>();
 private static final class State {long lastTick=Long.MIN_VALUE,scan=Long.MIN_VALUE;List<LivingEntity> targets=List.of();}
 public static void clear(LivingEntity caster){STATES.remove(caster);}
 public static int phase(long elapsed){return elapsed>=400?2:elapsed>=200?1:0;}
 public static Vec3 accelerate(Vec3 velocity,Vec3 toward,int stage,double resistance){
  double distance=toward.length();if(distance<=.9)return velocity;double factor=Math.clamp(1-resistance,0,1);if(factor==0)return velocity;
  Vec3 axis=toward.scale(1/distance);double inward=velocity.dot(axis),limit=Math.min(new double[]{.35,.75,1.35}[stage]*factor,distance-.9);
  double added=Math.min(new double[]{.06,.18,.50}[stage]*factor,Math.max(0,limit-inward));
  return velocity.add(axis.scale(added));
 }
 public static void tick(LivingEntity caster){
  if(caster.level().isClientSide())return;var channel=PaladinUltimates.channelOf(caster);if(channel==null||channel.ended)return;
  long now=caster.level().getGameTime(),elapsed=now-channel.start;
  // Let the stronger opening yank settle before applying sustained pull.
  if(channel.pulses==0||elapsed<14)return;var state=STATES.computeIfAbsent(caster,k->new State());if(state.lastTick==now)return;state.lastTick=now;
  if(state.scan==Long.MIN_VALUE||now-state.scan>=4){state.scan=now;state.targets=PriestRules.nearby(caster,caster.position(),channel.outer);}
  int stage=phase(elapsed);
  for(var target:state.targets){if(!target.isAlive()||target.isRemoved()||target.level()!=caster.level()||target.distanceToSqr(caster)>channel.outer*channel.outer||!PartyRules.harmfulAllowed(caster,target))continue;
   // Remote player input lives in received movement, not the server's lingering impulse velocity.
   var old=target instanceof ServerPlayer player?player.getKnownMovement():target.getDeltaMovement();var next=accelerate(old,caster.position().subtract(target.position()),stage,target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
   if(next.equals(old))continue;target.setDeltaMovement(next);target.needsSync=true;
   if(target instanceof ServerPlayer player)player.connection.send(new ClientboundSetEntityMotionPacket(target));
  }
 }
}
