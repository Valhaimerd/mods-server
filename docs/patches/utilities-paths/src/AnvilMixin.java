package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
@Mixin(AnvilMenu.class)
public class AnvilMixin {
 @Shadow @Final private DataSlot cost;
 @Inject(method="createResult",at=@At("RETURN"))
 private void price(CallbackInfo ci){cost.set(Utilities.anvil(((AnvilPlayerAccess)(Object)this).utilities$player(),cost.get()));}
}
