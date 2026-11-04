package net.spell_engine.internals;
import net.minecraft.world.entity.*;import net.minecraft.world.phys.Vec3;import net.minecraft.world.item.Items;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.entity.EntityTeleportEvent;import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
public final class FinalDome {
 static boolean initialized;
 public static void init(){if(initialized)return;initialized=true;NeoForge.EVENT_BUS.addListener(FinalDome::item);NeoForge.EVENT_BUS.addListener(FinalDome::teleport);}
 static boolean trapped(LivingEntity e){for(var b:PurgatoryBoundary.active(e.level())){var owner=b.getOwner();if(owner!=null&&owner!=e&&!PartyRules.allies(owner,e)&&PaladinUltimates.channelOf(owner)!=null){var p=e.position().subtract(b.position());double radius=PurgatoryBoundary.radius(b);if((p.x*p.x+p.z*p.z)/(radius*radius)+Math.pow((p.y+e.getBbHeight()/2)/5,2)<1)return true;}}return false;}
 static void item(PlayerInteractEvent.RightClickItem e){if((e.getItemStack().is(Items.ENDER_PEARL)||e.getItemStack().is(Items.CHORUS_FRUIT))&&trapped(e.getEntity())){e.setCanceled(true);e.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);}}
 static void teleport(EntityTeleportEvent e){if(e.getEntity() instanceof LivingEntity living&&trapped(living)&&(e.getTargetLevel()!=living.level()||PurgatoryBoundary.clip(living,e.getTarget().subtract(living.position())).distanceToSqr(e.getTarget().subtract(living.position()))>.000001))e.setCanceled(true);}
 public static Vec3 clip(Vec3 feet,Vec3 delta,Vec3 bottom,double radius,double height,double halfWidth,double entityHeight){
  // A closed ellipsoid retains the existing radius and +/-5 vertical bounds.
  Vec3 point=feet.add(0,entityHeight/2,0).subtract(bottom.add(0,height/2,0));double normalized=(point.x*point.x+point.z*point.z)/(radius*radius)+point.y*point.y/(height*height/4);boolean inside=normalized<=1;
  double r=Math.max(.05,radius+(inside?-1:1)*halfWidth*Math.sqrt(2)),v=Math.max(.05,height/2+(inside?-1:1)*entityHeight/2);
  Vec3 a=new Vec3(point.x/r,point.y/v,point.z/r),b=new Vec3(delta.x/r,delta.y/v,delta.z/r);double A=b.lengthSqr();if(A<1e-14)return delta;double B=2*a.dot(b),C=a.lengthSqr()-1,D=B*B-4*A*C;
  if(inside&&C>=0&&a.dot(b)>=0)return Vec3.ZERO;
  if(D<0)return delta;double t=(-B+(inside?1:-1)*Math.sqrt(D))/(2*A);if(t<0||t>1)return delta;return delta.scale(Math.max(0,t-.0001));
 }
}
