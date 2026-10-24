package com.emandev.bifrost;

import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Dedicated-server regression: native marker mutations must honor stored race. */
@Mod("bifrost_probe") public final class BifrostProbe {
    final List<String> checks = new ArrayList<>();
    void check(String text, boolean ok) { if (!ok) throw new AssertionError(text); checks.add("PASS " + text); }
    public BifrostProbe() { NeoForge.EVENT_BUS.addListener(this::start); }
    void start(ServerStartedEvent event) {
        var server=event.getServer();
        server.execute(() -> {
            String result;
            try {
                var level=server.overworld();
                var player=FakePlayerFactory.get(level,new GameProfile(UUID.randomUUID(),"BifrostProbe"));
                player.setGameMode(GameType.SURVIVAL); player.setPos(0,65,0);
                var state=PlayerRaceState.get(server);
                var pos=new BlockPos(12,64,12);
                BifrostMarkerService.remove(level,pos);
                level.setBlockAndUpdate(pos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
                var marker=BifrostMarkerService.register(level,pos,"Legacy destination");
                state.setRace(player.getUUID(),Race.HUMAN);
                check("Non-Asgardian cannot rename legacy markers",BifrostMarkerService.rename(player,marker.id(),"Unauthorized")==null);
                check("Denied rename preserves original marker",BifrostMarkerData.get(server).markers().get(marker.id()).name().equals("Legacy destination"));
                state.setRace(player.getUUID(),Race.ASGARDIAN);
                var renamed=BifrostMarkerService.rename(player,marker.id(),"Asgard destination");
                check("Asgardian can rename",renamed!=null && renamed.name().equals("Asgard destination"));
                check("Rename retains legacy UUID",renamed.id().equals(marker.id()));
                check("Rename retains position and dimension",renamed.position().equals(pos)&&renamed.dimension().equals(marker.dimension()));
                runMore(player,level,renamed);
                result="PASS "+checks.size()+" Bifrost native server checks";
            } catch(Throwable t) { t.printStackTrace(); result="FAIL "+t; }
            try {Files.write(Path.of("bifrost-measurements.txt"),checks);Files.writeString(Path.of("bifrost-result.txt"),result);} catch(Exception e){throw new RuntimeException(e);}
            server.halt(false);
        });
    }
    @SuppressWarnings("unchecked")
    void runMore(ServerPlayer player,ServerLevel level,BifrostMarkerData.Marker marker) throws Exception {
        var state=PlayerRaceState.get(level.getServer());
        var gate=Class.forName("com.emandev.bifrost.AsgardianAccess");
        var canUse=gate.getMethod("canUse",net.minecraft.world.entity.Entity.class);
        for(var race:Race.values()) {
            state.setRace(player.getUUID(),race);
            check("Stored race gate "+race,((Boolean)canUse.invoke(null,player))==(race==Race.ASGARDIAN));
            if(race!=Race.ASGARDIAN) {
                check("Direct travel denied "+race,!BifrostTeleportManager.begin(player,marker,"probe"));
                check("Command menu selection denied "+race,BifrostMenuService.select(player,1)==0);
                check("Rename command denied "+race,BifrostMenuService.renamePending(player,"forbidden")==0);
                check("Marker list denied "+race,BifrostMarkerService.list(player).isEmpty());
                check("Marker lookup denied "+race,BifrostMarkerService.get(player,marker.id())==null);
                for(var item:List.of(ModItems.HEIMDALLS_SWORD.get(),ModItems.THORS_HAMMER.get())) {
                    check("Focus use denied "+race+" "+item,item.use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));
                    var attack=new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(player,player);
                    NeoForge.EVENT_BUS.post(attack);check("Focus attack denied "+race,attack.isCanceled());
                    var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(marker.position()),net.minecraft.core.Direction.UP,marker.position(),false);
                    check("Focus marker interaction denied "+race,item.useOn(new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,hit))==net.minecraft.world.InteractionResult.FAIL);
                }
                var placement=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,marker.position()),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),player);
                NeoForge.EVENT_BUS.post(placement);check("Marker placement denied "+race,placement.isCanceled());
            }
        }
        state.setRace(player.getUUID(),Race.ASGARDIAN);
        check("Asgardian list retains old marker",BifrostMarkerService.list(player).stream().anyMatch(m->m.id().equals(marker.id())));
        check("Asgardian lookup retains old marker",BifrostMarkerService.get(player,marker.id())!=null);
        var placement=new net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,marker.position()),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),player);
        NeoForge.EVENT_BUS.post(placement);check("Asgardian marker placement permitted",!placement.isCanceled());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(ModItems.HEIMDALLS_SWORD.get()));
        var attack=new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(player,player);NeoForge.EVENT_BUS.post(attack);check("Asgardian focus attack permitted",!attack.isCanceled());
        player.setGameMode(GameType.SPECTATOR);check("Spectator Asgardian denied",!(Boolean)canUse.invoke(null,player));player.setGameMode(GameType.SURVIVAL);
        var context=(net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.neoforged.neoforge.network.handling.IPayloadContext.class},(proxy,method,args)->{
            if(method.getName().equals("player"))return player;
            throw new AssertionError("Unexpected packet-context call "+method);
        });
        state.setRace(player.getUUID(),Race.HUMAN);
        for(var entry:List.of(
            new Object[]{"handleTravel",new BifrostNetworking.TravelPayload(marker.id())},
            new Object[]{"handleRename",new BifrostNetworking.RenamePayload(marker.id(),"Packet bypass")},
            new Object[]{"handleRefresh",new BifrostNetworking.RefreshPayload(marker.id())})) {
            Object payload=entry[1];var handler=BifrostNetworking.class.getDeclaredMethod((String)entry[0],payload.getClass(),net.neoforged.neoforge.network.handling.IPayloadContext.class);handler.setAccessible(true);handler.invoke(null,payload,context);
            check("Forged packet denied "+entry[0],BifrostMarkerData.get(level.getServer()).markers().get(marker.id()).name().equals("Asgard destination"));
        }
        state.setRace(player.getUUID(),Race.ASGARDIAN);
        check("Cross-dimension travel remains rejected",!BifrostTeleportManager.begin(player,new BifrostMarkerData.Marker(UUID.randomUUID(),"Nether",net.minecraft.resources.Identifier.parse("minecraft:the_nether"),marker.position()),"probe"));
        var playersField=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");playersField.setAccessible(true);
        var players=(Map<UUID,ServerPlayer>)playersField.get(level.getServer().getPlayerList());players.put(player.getUUID(),player);
        try {
            check("Asgardian starts native charge",BifrostTeleportManager.begin(player,marker,"Hofund"));
            check("Duplicate charge rejected",!BifrostTeleportManager.begin(player,marker,"Hofund"));
            var pending=BifrostTeleportManager.class.getDeclaredField("PENDING");pending.setAccessible(true);
            var arrivals=BifrostTeleportManager.class.getDeclaredField("ARRIVALS");arrivals.setAccessible(true);
            state.setRace(player.getUUID(),Race.HUMAN);BifrostTeleportManager.tick(level.getServer());
            check("Race reset cancels charge",((Map<?,?>)pending.get(null)).isEmpty());
            check("Canceled charge does not teleport",player.getX()==0);
            state.setRace(player.getUUID(),Race.ASGARDIAN);
            check("Can restart after cancellation",BifrostTeleportManager.begin(player,marker,"Mjolnir"));
            for(int i=0;i<60;i++)BifrostTeleportManager.tick(level.getServer());
            check("Native charge completes once",((Map<?,?>)pending.get(null)).isEmpty());
            check("Native cooldown remains five seconds",BifrostRules.remainingCooldownSeconds(level.getGameTime(),level.getGameTime()+BifrostTeleportManager.COOLDOWN_TICKS)==5);
            check("Native arrival animation remains active",((Map<?,?>)arrivals.get(null)).containsKey(player.getUUID()));
            var arrival=((Map<?,?>)arrivals.get(null)).get(player.getUUID());
            var x=arrival.getClass().getDeclaredMethod("x");x.setAccessible(true);
            check("Native completion records existing marker coordinates",Math.abs((Double)x.invoke(arrival)-12.5)<.01);
            for(int i=0;i<40;i++)BifrostTeleportManager.tick(level.getServer());
            check("Arrival visuals clean up",((Map<?,?>)arrivals.get(null)).isEmpty());
        } finally {players.remove(player.getUUID());BifrostTeleportManager.stop(level.getServer());}
        if(net.neoforged.fml.ModList.get().isLoaded("playerrevive")) {
            team.creative.playerrevive.server.PlayerReviveServer.getBleeding(player).knockOut(player,level.damageSources().generic());
            check("PlayerRevive downed Asgardian denied",!(Boolean)canUse.invoke(null,player));
            team.creative.playerrevive.server.PlayerReviveServer.getBleeding(player).revive(player);
            check("Rescued Asgardian allowed",(Boolean)canUse.invoke(null,player));
        }
        if(net.neoforged.fml.ModList.get().isLoaded("better_revive")) {
            var f=de.mrstilu2k6.better_revive.util.DownedPlayerManager.class.getDeclaredField("downed");f.setAccessible(true);
            var downed=(Map<UUID,Object>)f.get(null);
            downed.put(player.getUUID(),new de.mrstilu2k6.better_revive.util.DownedPlayerManager.DownedState(System.currentTimeMillis(),Long.MAX_VALUE,player.position(),level.dimension(),GameType.SURVIVAL));
            check("Better Revive downed Asgardian denied",!(Boolean)canUse.invoke(null,player));downed.remove(player.getUUID());
        }
        var persistenceFile=Path.of("bifrost-persist-marker.txt");
        if(Files.exists(persistenceFile)) {
            var saved=BifrostMarkerData.get(level.getServer()).markers().get(UUID.fromString(Files.readString(persistenceFile).trim()));
            check("Actual server restart retains saved marker UUID and name",saved!=null&&saved.name().equals("Restart destination"));
        } else {
            var pos=new BlockPos(30,64,30);level.setBlockAndUpdate(pos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
            var saved=BifrostMarkerService.register(level,pos,"Restart destination");Files.writeString(persistenceFile,saved.id().toString());
        }
    }
}
