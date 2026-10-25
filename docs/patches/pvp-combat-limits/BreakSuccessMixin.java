package io.redspace.pvp_flagging.mixin;
import io.redspace.pvp_flagging.CombatRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ServerPlayerGameMode.class)
public class BreakSuccessMixin {
 @Shadow @Final protected ServerPlayer player;
 @Inject(method="removeBlock",at=@At("RETURN"))
 private void combat$completed(BlockPos pos,BlockState state,boolean canHarvest,ItemStack tool,CallbackInfoReturnable<Boolean> cir){if(cir.getReturnValueZ())CombatRules.completed(player,false,1);}
}
