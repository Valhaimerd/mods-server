package net.spell_engine.mixin.classfollowup;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Let the RPG carrying animation own idle arms, as it does when unloaded. */
@Mixin(AvatarRenderer.class)
public abstract class CrossbowCarryMixin {
 @Inject(method="getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;",at=@At("RETURN"),cancellable=true)
 private static void carry(Avatar player,ItemStack stack,InteractionHand hand,CallbackInfoReturnable<HumanoidModel.ArmPose> ci){
  if(ci.getReturnValue()==HumanoidModel.ArmPose.CROSSBOW_HOLD&&!player.isUsingItem()&&BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("archers"))ci.setReturnValue(HumanoidModel.ArmPose.ITEM);
 }
}
