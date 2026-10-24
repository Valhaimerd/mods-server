package net.spell_engine.mixin.warrior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Let the complete hit reach armor/Resistance; the independent shield layer runs in damage Pre. */
@Mixin(LivingEntity.class)
public abstract class WarriorShieldMixin {
 @Inject(method="applyItemBlocking",at=@At("HEAD"),cancellable=true)
 private void warriorBlocking(ServerLevel world,DamageSource source,float damage,CallbackInfoReturnable<Float> cir){
  if(((LivingEntity)(Object)this).getItemBlockingWith()!=null)cir.setReturnValue(0f);
 }
}
