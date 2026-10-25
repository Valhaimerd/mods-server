package com.eiden.iourusraces.client;
import com.eiden.iourusraces.client.gui.RaceSelectionScreen;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@EventBusSubscriber(modid="iourusraces",value=Dist.CLIENT)
public final class ClientInputHandler {
 private static boolean openedThisSession;
 private ClientInputHandler(){}
 @SubscribeEvent public static void onClientTick(ClientTickEvent.Post e){
  var client=Minecraft.getInstance();
  if(client.player==null){openedThisSession=false;return;}
  if(!ClientRaceData.isSynced()){
   openedThisSession=false;
   while(RaceKeybinds.OPEN_RACE_MENU.consumeClick()){} // A keypress cannot turn pending sync into a new character.
   return;
  }
  Race race=ClientRaceData.getRace();
  if(race!=Race.NONE){
   openedThisSession=false;
   if(client.gui.screen() instanceof RaceSelectionScreen)client.setScreenAndShow(null);
  }
  while(RaceKeybinds.OPEN_RACE_MENU.consumeClick()){
   if(race!=Race.NONE){client.player.sendOverlayMessage(Component.translatable("message.iourusraces.already_has_race"));continue;}
   if(!(client.gui.screen() instanceof RaceSelectionScreen))client.setScreenAndShow(new RaceSelectionScreen());
   openedThisSession=true;
  }
  if(race==Race.NONE&&!openedThisSession&&client.gui.screen()==null){
   openedThisSession=true;client.setScreenAndShow(new RaceSelectionScreen());
  }
 }
}
