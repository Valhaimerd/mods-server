package com.emandev.bifrost.mixin;
import com.emandev.bifrost.WeaponOwnership;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class OwnershipCreativeMixin {
 @Shadow public ServerPlayer player;
 @Inject(method="handleSetCreativeModeSlot",at=@At(value="INVOKE",target="Lnet/minecraft/network/protocol/game/ServerboundSetCreativeModeSlotPacket;itemStack()Lnet/minecraft/world/item/ItemStack;"),cancellable=true)
 private void copy(ServerboundSetCreativeModeSlotPacket packet,CallbackInfo ci){if(WeaponOwnership.boundType(packet.itemStack())){player.containerMenu.broadcastFullState();ci.cancel();}}
}
