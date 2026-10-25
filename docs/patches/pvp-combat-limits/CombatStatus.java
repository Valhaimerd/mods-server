package io.redspace.pvp_flagging;
import net.minecraft.resources.Identifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record CombatStatus(int combatMs,int breakMs,int placeMs) implements CustomPacketPayload {
 public static final Type<CombatStatus> TYPE=new Type<>(Identifier.parse("pvp_flagging:combat_status"));
 public static final StreamCodec<FriendlyByteBuf,CombatStatus> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.combatMs);b.writeVarInt(p.breakMs);b.writeVarInt(p.placeMs);},b->new CombatStatus(b.readVarInt(),b.readVarInt(),b.readVarInt()));
 public Type<CombatStatus> type(){return TYPE;}
}
