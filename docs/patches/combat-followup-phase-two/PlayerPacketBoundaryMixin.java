package net.spell_engine.mixin.entity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.spell_engine.internals.MultiplayerBoundary;
import org.spongepowered.asm.mixin.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(ServerGamePacketListenerImpl.class)
public class PlayerPacketBoundaryMixin {
 @Shadow public ServerPlayer player;
 @WrapMethod(method="handleMovePlayer")
 private void pack$enforceWall(ServerboundMovePlayerPacket packet,Operation<Void> original){
  // PacketUtils schedules off-thread calls before any state is read or checked here.
  if(!player.level().getServer().isSameThread()){original.call(packet);return;}
  var start=player.position();var level=player.level();original.call(packet);MultiplayerBoundary.reconcile(player,start,level);
 }
}
