package vazkii.patchouli.common.util;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.entity.Entity;
/** Detached book previews need renderer IDs, but must never enter the world. */
public final class PreviewEntityIds {
 private static final AtomicInteger NEXT_ID=new AtomicInteger(-1);
 private PreviewEntityIds(){}
 public static Entity prepare(Entity entity){
  if(entity!=null&&!entity.isAddedToLevel()){
   try{entity.getId();}catch(IllegalStateException unassigned){entity.setId(NEXT_ID.getAndDecrement());}
  }
  return entity;
 }
}
