package io.redspace.pvp_flagging.mixin;
import java.util.List;
import io.redspace.pvp_flagging.CombatRules;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Charge after every event listener has accepted the placement, including other mods. */
@Mixin(EventHooks.class)
public class PlaceSuccessMixin {
 @Inject(method="onBlockPlace",at=@At("RETURN"))
 private static void combat$placed(Entity entity,BlockSnapshot snapshot,Direction direction,CallbackInfoReturnable<Boolean> cir){if(!cir.getReturnValueZ()&&entity instanceof ServerPlayer p)CombatRules.completed(p,true,1);}
 @Inject(method="onMultiBlockPlace",at=@At("RETURN"))
 private static void combat$placedMany(Entity entity,List<BlockSnapshot> snapshots,Direction direction,CallbackInfoReturnable<Boolean> cir){if(!cir.getReturnValueZ()&&entity instanceof ServerPlayer p)CombatRules.completed(p,true,snapshots.size());}
}
