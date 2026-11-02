package net.spell_engine.internals;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Final server reconciliation: native player packet handling can overwrite Entity.move's clipped result. */
public final class MultiplayerBoundary {
 public static Vec3 clipFrom(ServerPlayer player,Vec3 start,Vec3 movement){
  if(player.isSpectator()||player.noPhysics||!player.isAlive())return movement;
  Vec3 result=movement;
  for(var wall:PurgatoryBoundary.active(player.level())){
   var owner=wall.getOwner();if(wall.isRemoved()||!wall.isAlive()||owner==null||!owner.isAlive()||owner.level()!=player.level()||PaladinUltimates.channelOf(owner)==null||PartyRules.allies(owner,player))continue;
   result=PurgatoryBoundary.clipCircle(start,result,wall.position().add(0,-5,0),PurgatoryBoundary.radius(wall),10,player.getBbWidth()/2d,player.getBbHeight());
  }
  return result;
 }
 public static void reconcile(ServerPlayer player,Vec3 start,net.minecraft.world.level.Level level){
  if(player.level()!=level)return;Vec3 accepted=player.position().subtract(start);if(!Double.isFinite(accepted.lengthSqr())||accepted.lengthSqr()<1e-12)return;
  Vec3 allowed=clipFrom(player,start,accepted);if(allowed.subtract(accepted).lengthSqr()<1e-10)return;
  Vec3 safe=start.add(allowed);player.connection.teleport(safe.x,safe.y,safe.z,player.getYRot(),player.getXRot());
 }
}
