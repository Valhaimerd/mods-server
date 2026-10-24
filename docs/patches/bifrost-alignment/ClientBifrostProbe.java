package com.emandev.bifrost;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Real connected client exercises existing menu packets and native player teleport. */
@Mod(value="bifrost_client_probe",dist=Dist.CLIENT)
public final class ClientBifrostProbe {
    int ticks; boolean hidden,done; volatile BifrostMarkerData.Marker marker;
    final AtomicReference<String> status=new AtomicReference<>();
    void check(boolean ok,String label){if(!ok)throw new AssertionError(label);}
    void result(String message) {
        var mc=Minecraft.getInstance();
        try{Files.writeString(mc.gameDirectory.toPath().resolve("bifrost-client-result.txt"),message);}catch(Exception e){throw new RuntimeException(e);}
        done=true;mc.stop();
    }
    public ClientBifrostProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        ticks++;
        try {
            if(ticks==20)mc.getSingleplayerServer().execute(()->{
                try {
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.ASGARDIAN);RaceAttributes.applyRaceAttributes(p,Race.ASGARDIAN);RaceNetworkHandler.syncRace(p,Race.ASGARDIAN);
                    p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setHealth(p.getMaxHealth());
                    var pos=p.blockPosition().offset(32,0,0);
                    p.level().setBlockAndUpdate(pos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
                    for(int y=1;y<=3;y++)p.level().setBlockAndUpdate(pos.above(y),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                    marker=BifrostMarkerService.register(p.level(),pos,"Client destination");
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.HEIMDALLS_SWORD.get()));
                    status.set("ready");
                }catch(Throwable t){t.printStackTrace();status.set("FAIL "+t);}
            });
            if(ticks==45){check("ready".equals(status.get()),"Server fixture ready: "+status.get());mc.setScreenAndShow(null);}
            if(ticks==50)mc.getSingleplayerServer().execute(()->{
                try {
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    ModItems.HEIMDALLS_SWORD.get().use(p.level(),p,InteractionHand.MAIN_HAND);
                    status.set("menu");
                }catch(Throwable t){t.printStackTrace();status.set("FAIL "+t);}
            });
            if(ticks==75){
                check(mc.gui.screen() instanceof BifrostDestinationScreen,"Hofund opens native destination screen: "+mc.gui.screen());
                check(net.minecraft.client.resources.language.I18n.get("item.bifrost.heimdalls_sword").equals("Hofund"),"Hofund localized name");
                var lines=new ArrayList<net.minecraft.network.chat.Component>();
                var stack=new ItemStack(ModItems.HEIMDALLS_SWORD.get());
                AsgardianAccess.tooltip(new net.neoforged.neoforge.event.entity.player.ItemTooltipEvent(stack,mc.player,lines,net.minecraft.world.item.TooltipFlag.NORMAL,net.minecraft.world.item.Item.TooltipContext.of(mc.level),net.minecraft.world.item.component.TooltipDisplay.DEFAULT));
                check(lines.stream().anyMatch(c->c.getString().equals("Requires Asgardian race")),"Race tooltip available");
                Screenshot.grab(mc,false);
            }
            if(ticks==80){
                mc.setScreenAndShow(null);
                mc.getSingleplayerServer().execute(()->{
                    try{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                        check(BifrostTeleportManager.begin(p,marker,"Hofund"),"Native travel begins");status.set("charging");
                    }catch(Throwable t){t.printStackTrace();status.set("FAIL "+t);}
                });
            }
            if(ticks==160)mc.getSingleplayerServer().execute(()->{
                try {
                    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    check(Math.abs(p.getX()-marker.position().getX()-.5)<.02&&Math.abs(p.getY()-marker.position().getY()-1)<.02&&Math.abs(p.getZ()-marker.position().getZ()-.5)<.02,"Connected player arrived at exact native destination: "+p.position());
                    check(!BifrostTeleportManager.begin(p,marker,"Hofund"),"Native cooldown rejects immediate second travel");
                    PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.HUMAN);RaceAttributes.applyRaceAttributes(p,Race.HUMAN);RaceNetworkHandler.syncRace(p,Race.HUMAN);
                    check(ModItems.HEIMDALLS_SWORD.get().use(p.level(),p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL,"Race reset disables already-held Hofund");
                    status.set("PASS connected client: Hofund name and race tooltip; existing destination menu packet; native 60-tick teleport reaches marker; five-second cooldown; reset disables held focus");
                }catch(Throwable t){t.printStackTrace();status.set("FAIL "+t);}
            });
            if(ticks==180){check(status.get()!=null&&status.get().startsWith("PASS"),"Server checks: "+status.get());check(ClientRaceData.getRace()==Race.HUMAN,"Race reset syncs to client");result(status.get());}
        } catch(Throwable t){t.printStackTrace();result("FAIL "+t);}
    }
}
