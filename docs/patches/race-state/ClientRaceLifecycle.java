package com.eiden.iourusraces.client;
import com.eiden.iourusraces.race.ClientRaceData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
@EventBusSubscriber(modid="iourusraces",value=Dist.CLIENT)
public final class ClientRaceLifecycle {
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){ClientRaceData.reset();}
}
