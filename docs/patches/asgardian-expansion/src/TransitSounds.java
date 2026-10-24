package com.emandev.bifrost;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Each phase is sent once to each listener, including passengers outside hearing radius. */
public final class TransitSounds {
    private static final float RANGE=96f;
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,"bifrost");
    public static final DeferredHolder<SoundEvent,SoundEvent> CHARGE=sound("transit_charge"), DESCENT=sound("transit_descent"),
        IMPACT=sound("transit_impact"), CLOSURE=sound("transit_closure");
    private static DeferredHolder<SoundEvent,SoundEvent> sound(String id){return SOUNDS.register(id,()->SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath("bifrost",id),RANGE));}
    public static void register(IEventBus bus){SOUNDS.register(bus);}
    static void phase(ServerLevel level,Vec3 center,DeferredHolder<SoundEvent,SoundEvent> event,List<ServerPlayer> passengers) {
        var listeners=new LinkedHashSet<ServerPlayer>();
        for(var player:level.players())if(player.distanceToSqr(center)<=RANGE*RANGE)listeners.add(player);
        listeners.addAll(passengers);long seed=level.getRandom().nextLong();
        for(var player:listeners) {
            var position=player.distanceToSqr(center)>RANGE*RANGE?player.position():center;
            player.connection.send(new ClientboundSoundPacket(event,SoundSource.PLAYERS,position.x,position.y,position.z,1.8f,1f,seed));
        }
    }
}
