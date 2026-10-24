package net.spell_engine.mixin.warrior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BlocksAttacks.class)
public abstract class ShieldDisableMixin {
 @Inject(method="disable",at=@At("HEAD"),cancellable=true)
 private void warriorDisable(ServerLevel world,LivingEntity user,float seconds,ItemStack stack,CallbackInfo ci){ci.cancel();}
}
