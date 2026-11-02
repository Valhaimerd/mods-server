package local.crossarms.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
public class FrenzyIframesMixin {
 @WrapOperation(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
 private boolean crossarms$frenzyImpact(Entity target,DamageSource source,float amount,Operation<Boolean> call){
  var arrow=(AbstractArrow)(Object)this;
  if(target.level().isClientSide()||!(arrow.getOwner() instanceof Player)||!arrow.entityTags().contains("FRENZY"))return call.call(target,source,amount);
  int prior=target.invulnerableTime;target.invulnerableTime=0;
  try{return call.call(target,source,amount);}finally{if(prior>0)target.invulnerableTime=prior;}
 }
}

