package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
@Mixin(Consumable.class)
public class ConsumptionMixin {
 @Inject(method="onConsume",at=@At("HEAD")) private void begin(Level level,LivingEntity entity,ItemStack stack,CallbackInfoReturnable<ItemStack> cir){Utilities.CONSUMING.set(Utilities.CONSUMING.get()+1);Utilities.CONSUMERS.get().push(entity);}
 @Inject(method="onConsume",at=@At("RETURN")) private void end(CallbackInfoReturnable<ItemStack> cir){Utilities.CONSUMING.set(Math.max(0,Utilities.CONSUMING.get()-1));Utilities.CONSUMERS.get().pop();}
}
