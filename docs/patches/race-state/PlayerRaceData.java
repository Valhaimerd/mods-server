package com.eiden.iourusraces.race;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
public final class PlayerRaceData {
 private PlayerRaceData(){}
 public static void setRace(ServerPlayer player,Race race){
  Race current=race==null?Race.NONE:race;
  PlayerRaceState.get(player.level().getServer()).setRace(player.getUUID(),current);
  RaceAttributes.applyRaceAttributes(player,current);
  RaceNetworkHandler.syncRace(player,current);
 }
 public static void resetRace(ServerPlayer player){setRace(player,Race.NONE);}
 public static Race getRace(Player player){return player instanceof ServerPlayer p?PlayerRaceState.get(p.level().getServer()).getRace(p.getUUID()):ClientRaceData.getRace();}
 public static void copyRace(Player old,Player next){if(next instanceof ServerPlayer p)setRace(p,getRace(old));}
}
