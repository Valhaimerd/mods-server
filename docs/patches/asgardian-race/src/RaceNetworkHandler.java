package com.eiden.iourusraces.network;

import com.eiden.iourusraces.network.RaceSyncPayload;
import com.eiden.iourusraces.race.ClientRaceData;
import com.eiden.iourusraces.race.Race;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid="iourusraces")
public final class RaceNetworkHandler {
    private RaceNetworkHandler() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("asgardian-v1");
        registrar.playToClient(RaceSyncPayload.TYPE, RaceSyncPayload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
            Race race;
            try {
                race = Race.valueOf(payload.raceName());
            }
            catch (IllegalArgumentException exception) {
                race = Race.NONE;
            }
            ClientRaceData.setRace(race);
        }));
    }

    public static void syncRace(ServerPlayer player, Race race) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new RaceSyncPayload(race.name()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}
