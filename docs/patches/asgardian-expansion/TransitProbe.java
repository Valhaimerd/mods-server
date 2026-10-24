package com.emandev.bifrost;

import java.util.*;
import com.mojang.authlib.GameProfile;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.race.Race;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/** Native disposable-server tests; no production classes or test entities are packaged. */
public final class TransitProbe {
    static void check(List<String> out,String name,boolean condition){if(!condition)throw new AssertionError(name);out.add("PASS transit: "+name);}
    @SuppressWarnings("unchecked")
    public static void run(MinecraftServer server,List<String> out)throws Exception {
        check(out,"source descent 14 ticks",BifrostTeleportManager.SOURCE_DESCENT_TICKS==14);
        check(out,"target descent 25 ticks",BifrostTeleportManager.TARGET_DESCENT_TICKS==25);
        check(out,"charge/arrival/cooldown preserved",BifrostTeleportManager.CHARGE_TICKS==60&&BifrostTeleportManager.ARRIVAL_ANIMATION_TICKS==40&&BifrostTeleportManager.COOLDOWN_TICKS==100);
        check(out,"collision-box edge intersects",BifrostTeleportManager.intersects(new AABB(5.9,65,0,6.7,66,1),new Vec3(0,65,0)));
        check(out,"circle corner excluded",!BifrostTeleportManager.intersects(new AABB(5,65,5,6,66,6),new Vec3(0,65,0)));
        check(out,"above volume excluded",!BifrostTeleportManager.intersects(new AABB(0,140,0,1,141,1),new Vec3(0,65,0)));
        var level=server.overworld();
        for(int x=-12;x<=78;x++)for(int z=-10;z<=10;z++){
            level.getChunk(x>>4,z>>4);level.setBlockAndUpdate(new BlockPos(x,64,z),Blocks.STONE.defaultBlockState());
            for(int y=65;y<70;y++)level.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
        }
        var actor=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"TransitProbe"));actor.setGameMode(GameType.SURVIVAL);actor.setPos(0,65,0);
        actor.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ModItems.HEIMDALLS_SWORD.get()));
        PlayerRaceState.get(server).setRace(actor.getUUID(),Race.ASGARDIAN);
        var markerPos=new BlockPos(64,64,0);level.setBlockAndUpdate(markerPos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
        var marker=BifrostMarkerService.register(level,markerPos,"Transit probe");
        var cow=new Cow(EntityTypes.COW,level);cow.setPos(1,65,0);level.addFreshEntity(cow);
        var late=new Cow(EntityTypes.COW,level);late.setPos(10,65,0);level.addFreshEntity(late);
        var vehicle=new Pig(EntityTypes.PIG,level);vehicle.setPos(3,65,0);level.addFreshEntity(vehicle);
        var rider=new Cow(EntityTypes.COW,level);rider.setPos(3,66,0);level.addFreshEntity(rider);check(out,"initial riding relation",rider.startRiding(vehicle,true,true));
        var excluded=new ItemEntity(level,2,65,0,new ItemStack(Items.STONE));level.addFreshEntity(excluded);
        var pendingField=BifrostTeleportManager.class.getDeclaredField("PENDING");pendingField.setAccessible(true);
        var capturedField=BifrostTeleportManager.class.getDeclaredField("CAPTURED");capturedField.setAccessible(true);
        var arrivalsField=BifrostTeleportManager.class.getDeclaredField("ARRIVALS");arrivalsField.setAccessible(true);
        var captured=(Set<UUID>)capturedField.get(null);
        try {
            BifrostTeleportManager.stop(server);
            check(out,"native start accepts eligible actor",BifrostTeleportManager.begin(actor,marker,"Hofund"));
            for(int i=0;i<13;i++)BifrostTeleportManager.tick(server);
            check(out,"no snapshot before source impact",captured.isEmpty());
            actor.setPos(-8,65,0); // Beam remains at its original coordinates.
            BifrostTeleportManager.tick(server);
            var pending=((Map<?,?>)pendingField.get(null)).values().iterator().next();
            var sourceField=pending.getClass().getDeclaredField("sourceBeam");sourceField.setAccessible(true);
            var sourceBeam=(TransitVisuals)sourceField.get(pending);
            var bottomField=TransitVisuals.class.getDeclaredField("bottom");bottomField.setAccessible(true);
            check(out,"source upward phase starts at impact plane tick 14",bottomField.getDouble(sourceBeam)==0);
            check(out,"source impact captures ordinary mob",captured.contains(cow.getUUID()));
            check(out,"riding group captured together",captured.contains(vehicle.getUUID())&&captured.contains(rider.getUUID()));
            check(out,"item drops excluded",!captured.contains(excluded.getUUID()));
            check(out,"outside mob excluded",!captured.contains(late.getUUID()));
            late.setPos(0,65,0);
            BifrostTeleportManager.tick(server);
            check(out,"source column rises immediately after impact",bottomField.getDouble(sourceBeam)>0);
            for(int i=15;i<54;i++)BifrostTeleportManager.tick(server);
            check(out,"source visual closes at tick 54",sourceBeam.ids().isEmpty());
            check(out,"source closure does not teleport passengers early",cow.getX()<10);
            for(int i=54;i<59;i++)BifrostTeleportManager.tick(server);
            check(out,"riding relation before completion",rider.getVehicle()==vehicle);
            check(out,"passengers remain until completion boundary",cow.getX()<10&&vehicle.getX()<10);
            BifrostTeleportManager.tick(server);
            check(out,"captured mob moved once at tick 60",cow.getX()>58&&cow.getX()<71);
            check(out,"vehicle and rider moved",vehicle.getX()>58&&rider.getX()>58);
            check(out,"riding relation preserved",rider.getVehicle()==vehicle);
            check(out,"late entrant remains at source",late.getX()==0);
            check(out,"item remains at source",excluded.getX()==2);
            check(out,"capture claims released",captured.isEmpty());
            check(out,"no pending after completion",((Map<?,?>)pendingField.get(null)).isEmpty());
            check(out,"only target arrival retract tracked",((List<?>)arrivalsField.get(null)).size()==1);
            for(int i=0;i<39;i++)BifrostTeleportManager.tick(server);
            check(out,"target arrival remains until tick 99",((List<?>)arrivalsField.get(null)).size()==1);
            BifrostTeleportManager.tick(server);
            check(out,"all visual entities cleaned",((List<?>)arrivalsField.get(null)).isEmpty());
            for(String sound:List.of("charge","descent","impact","closure"))check(out,"registered "+sound+" sound",BuiltInRegistries.SOUND_EVENT.get(Identifier.parse("bifrost:transit_"+sound)).isPresent());
            BifrostTeleportManager.stop(server);actor.setPos(0,65,0);
            check(out,"restart native charge",BifrostTeleportManager.begin(actor,marker,"Hofund"));
            for(int i=0;i<14;i++)BifrostTeleportManager.tick(server);
            PlayerRaceState.get(server).setRace(actor.getUUID(),Race.HUMAN);BifrostTeleportManager.tick(server);
            check(out,"race change cancels pending",((Map<?,?>)pendingField.get(null)).isEmpty());
            check(out,"cancel releases claims",captured.isEmpty());
            check(out,"cancel discards beams",!java.util.stream.StreamSupport.stream(level.getAllEntities().spliterator(),false).anyMatch(e->e.entityTags().contains(TransitVisuals.TAG)));
            PlayerRaceState.get(server).setRace(actor.getUUID(),Race.ASGARDIAN);
            var blocked=new Cow(EntityTypes.COW,level);blocked.setPos(1,65,0);level.addFreshEntity(blocked);
            java.util.function.Consumer<EntityTeleportEvent> veto=event->{if(event.getEntity()==blocked)event.setCanceled(true);};
            NeoForge.EVENT_BUS.addListener(veto);
            try {
                check(out,"protection test starts",BifrostTeleportManager.begin(actor,marker,"Hofund"));
                for(int i=0;i<60;i++)BifrostTeleportManager.tick(server);
                check(out,"protection veto keeps entire group at source",blocked.getX()==1);
            }finally{NeoForge.EVENT_BUS.unregister(veto);blocked.discard();BifrostTeleportManager.stop(server);}
            actor.setPos(0,65,0);
            var giant=new Cow(EntityTypes.COW,level);giant.setPos(1,65,0);giant.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.SCALE).setBaseValue(4);giant.refreshDimensions();level.addFreshEntity(giant);
            var small=new Cow(EntityTypes.COW,level);small.setPos(-1,65,0);level.addFreshEntity(small);
            for(int x=56;x<=72;x++)for(int z=-8;z<=8;z++)level.setBlockAndUpdate(new BlockPos(x,68,z),Blocks.STONE.defaultBlockState());
            try {
                check(out,"mixed safe/unsafe groups start",BifrostTeleportManager.begin(actor,marker,"Hofund"));
                for(int i=0;i<60;i++)BifrostTeleportManager.tick(server);
                check(out,"no headroom leaves oversized group at source",giant.getX()==1);
                check(out,"unsafe group does not cancel successful group",small.getX()>58);
            }finally{giant.discard();small.discard();BifrostTeleportManager.stop(server);for(int x=56;x<=72;x++)for(int z=-8;z<=8;z++)level.setBlockAndUpdate(new BlockPos(x,68,z),Blocks.AIR.defaultBlockState());}
            actor.setPos(0,65,0);
            check(out,"marker removal test starts",BifrostTeleportManager.begin(actor,marker,"Hofund"));
            for(int i=0;i<14;i++)BifrostTeleportManager.tick(server);
            level.setBlockAndUpdate(markerPos,Blocks.AIR.defaultBlockState());BifrostTeleportManager.tick(server);
            check(out,"removed marker cancels and releases snapshot",((Map<?,?>)pendingField.get(null)).isEmpty()&&captured.isEmpty());
            level.setBlockAndUpdate(markerPos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
            actor.setPos(0,65,0);BifrostTeleportManager.stop(server);
            AsgardAbilities.ACTIVE.put(actor.getUUID(),new AsgardAbilities.Action(actor,"wave"));
            try{check(out,"active weapon ability denies travel start",!BifrostTeleportManager.begin(actor,marker,"Hofund"));}
            finally{AsgardAbilities.ACTIVE.remove(actor.getUUID());}
            check(out,"travel starts after weapon ability ends",BifrostTeleportManager.begin(actor,marker,"Hofund"));
            AsgardAbilities.ACTIVE.put(actor.getUUID(),new AsgardAbilities.Action(actor,"wave"));
            try{BifrostTeleportManager.tick(server);check(out,"weapon ability interrupts an existing charge",((Map<?,?>)pendingField.get(null)).isEmpty());}
            finally{AsgardAbilities.ACTIVE.remove(actor.getUUID());}
        }finally{BifrostTeleportManager.stop(server);cow.discard();late.discard();vehicle.discard();rider.discard();excluded.discard();}
    }
}
