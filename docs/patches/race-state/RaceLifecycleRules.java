package com.eiden.iourusraces.race;
import java.util.*;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
/** One source of current identity; reconcile direct admin/SavedData race changes too. */
@EventBusSubscriber(modid="iourusraces")
public final class RaceLifecycleRules {
 private static final Map<ServerPlayer,Race> APPLIED=new WeakHashMap<>();
 static final Identifier WATER=Identifier.parse("iourusraces:melfork_water_strength"),LAND=Identifier.parse("iourusraces:melfork_land_weakness");
 public static void clearWater(Player p){var a=p.getAttribute(Attributes.ATTACK_DAMAGE);if(a!=null){a.removeModifier(WATER);a.removeModifier(LAND);}}
 public static void applied(Player p,Race race){
  clearWater(p);
  if(race==Race.MELFORK&&p instanceof ServerPlayer){
   var a=p.getAttribute(Attributes.ATTACK_DAMAGE);boolean water=p.isInWater();
   if(water)p.setAirSupply(p.getMaxAirSupply());
   if(a!=null)a.addTransientModifier(new AttributeModifier(water?WATER:LAND,water?3:-2,AttributeModifier.Operation.ADD_VALUE));
  }
  p.setHealth(Math.min(p.getHealth(),p.getMaxHealth()));
  if(p instanceof ServerPlayer server)APPLIED.put(server,race);
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Pre event){
  if(event.getEntity() instanceof ServerPlayer p){
   Race current=PlayerRaceData.getRace(p);
   if(APPLIED.get(p)!=current){RaceAttributes.applyRaceAttributes(p,current);RaceNetworkHandler.syncRace(p,current);}
  }
 }
}
