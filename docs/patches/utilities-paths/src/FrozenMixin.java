package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
@Mixin(Entity.class)
public class FrozenMixin {
 @Unique private double utilities$freezeFraction;
 @ModifyVariable(method="setTicksFrozen",at=@At("HEAD"),argsOnly=true)
 private int buildup(int next){
  if((Object)this instanceof ServerPlayer p&&p.isInPowderSnow&&next>p.getTicksFrozen()){
   if(Utilities.snow(p))return p.getTicksFrozen();
   double b=Utilities.stat(p,"freeze");if(b>0){utilities$freezeFraction+=(next-p.getTicksFrozen())*(1-b);int n=(int)Math.floor(utilities$freezeFraction+1e-9);utilities$freezeFraction-=n;return p.getTicksFrozen()+n;}
  }
  return next;
 }
}
