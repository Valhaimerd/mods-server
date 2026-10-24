package com.emandev.bifrost;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Disposable connected client: actual registered input, selector, network and item use. */
@Mod(value="asgard_expansion_client_probe",dist=Dist.CLIENT)
public final class ClientExpansionProbe {
    int ticks; boolean hidden,done; final List<String> checks=Collections.synchronizedList(new ArrayList<>());
    final AtomicReference<String> status=new AtomicReference<>();
    final java.util.concurrent.ConcurrentHashMap<String,Integer> sounds=new java.util.concurrent.ConcurrentHashMap<>();
    volatile long transitStart=-1; volatile UUID passenger; volatile BifrostMarkerData.Marker visualMarker; int visualStage; boolean cameraReady,arrivalChecked;
    void sound(net.neoforged.neoforge.client.event.sound.PlaySoundEvent event){
        var value=event.getOriginalSound();if(value==null)return;var id=value.getIdentifier();
        if(id.getNamespace().equals("bifrost")&&id.getPath().startsWith("transit_"))sounds.merge(id.getPath(),1,Integer::sum);
    }
    void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add("PASS "+label);}
    void result(String message) {
        var mc=Minecraft.getInstance();
        try{Files.writeString(mc.gameDirectory.toPath().resolve("expansion-client-result.txt"),message);Files.write(mc.gameDirectory.toPath().resolve("expansion-client-measurements.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}
        done=true;mc.stop();
    }
    public ClientExpansionProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::sound);}
    int selected(AsgardAbilityScreen screen)throws Exception{var f=AsgardAbilityScreen.class.getDeclaredField("selected");f.setAccessible(true);return f.getInt(screen);}
    void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> work){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{work.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){t.printStackTrace();status.set("FAIL "+t);}});}
    void openByKey(){KeyMapping.click(AsgardClient.ABILITY.getKey());}
    void tick(ClientTickEvent.Post event) {
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        ticks++;
        try {
            if(status.get()!=null&&status.get().startsWith("FAIL"))throw new AssertionError(status.get());
            if(ticks==20)server(p->{
                PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.ASGARDIAN);RaceAttributes.applyRaceAttributes(p,Race.ASGARDIAN);RaceNetworkHandler.syncRace(p,Race.ASGARDIAN);
                p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.setHealth(p.getMaxHealth());
                p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.HEIMDALLS_SWORD.get()));status.set("ready");
            });
            if(ticks==40){
                check("ready".equals(status.get()),"Server fixture ready");mc.setScreenAndShow(null);
                check(ClientRaceData.getRace()==Race.ASGARDIAN,"Race synchronized");
                check(AsgardClient.ABILITY.isUnbound(),"Ability key unbound by default");
                check(Arrays.asList(mc.options.keyMappings).contains(AsgardClient.ABILITY),"Ability key registered in Controls");
                AsgardClient.ABILITY.setKey(InputConstants.Type.KEYSYM.getOrCreate(314));KeyMapping.resetMapping();
                for(String path:List.of("models/item/thors_hammer.json","models/item/stormbreaker.json","items/stormbreaker.json","textures/item/thors_hammer.png","textures/item/stormbreaker.png"))
                    check(mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("bifrost",path)).isPresent(),"Client resource loaded "+path);
                check(net.minecraft.client.resources.language.I18n.get("item.bifrost.stormbreaker").equals("Stormbreaker"),"Stormbreaker localized name");
            }
            if(ticks==45)openByKey();
            if(ticks==50){
                check(mc.gui.screen() instanceof AsgardAbilityScreen,"Registered key opens Hofund selector");
                var s=(AsgardAbilityScreen)mc.gui.screen();check(!s.isPauseScreen(),"Selector keeps live server simulation");
                check(AsgardAbilities.describe("hofund").size()==4,"Hofund has four ability details");
                s.keyPressed(new KeyEvent(264,0,0));check(selected(s)==1,"Down selects Realm Shift");
                s.mouseScrolled(0,0,0,-1);check(selected(s)==2,"Scroll selects Hilt Bash");
                s.keyPressed(new KeyEvent(265,0,0));check(selected(s)==1,"Up restores Realm Shift");
                s.resize(426,240);
            }
            if(ticks==55)Screenshot.grab(mc,false);
            if(ticks==60){var s=(AsgardAbilityScreen)mc.gui.screen();s.keyPressed(new KeyEvent(265,0,0));s.keyPressed(new KeyEvent(257,0,0));}
            if(ticks==75){check(mc.gui.screen() instanceof BifrostDestinationScreen,"Selector Travel reaches server and native destination packet");Screenshot.grab(mc,false);mc.setScreenAndShow(null);}
            if(ticks==80)server(p->{p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.THORS_HAMMER.get()));check(WeaponOwnership.claim(p),"Server explicitly claims Mjolnir");status.set("hammer");});
            if(ticks==100){check(AsgardWeapons.kind(mc.player.getMainHandItem()).equals("mjolnir"),"Bound Mjolnir reaches client inventory");Screenshot.grab(mc,false);}
            if(ticks==105){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
            if(ticks==112)check(mc.player.isUsingItem(),"Native Mjolnir right-use starts client channel");
            if(ticks==115){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}
            if(ticks==125)server(p->{check(!AsgardAbilities.active(p),"Native release ends authoritative primary channel");p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.STORMBREAKER.get()));check(WeaponOwnership.claim(p),"Server explicitly claims Stormbreaker");status.set("axe");});
            if(ticks==145){check(AsgardWeapons.kind(mc.player.getMainHandItem()).equals("stormbreaker"),"Bound Stormbreaker reaches client inventory");check(mc.player.getMainHandItem().getHoverName().getString().equals("Stormbreaker"),"Stormbreaker rendered item name");Screenshot.grab(mc,false);openByKey();}
            if(ticks==150){check(mc.gui.screen() instanceof AsgardAbilityScreen,"Registered key opens Stormbreaker selector");var s=(AsgardAbilityScreen)mc.gui.screen();check(AsgardAbilities.describe("stormbreaker").size()==3,"Stormbreaker has three ability details");s.mouseScrolled(0,0,0,-1);s.mouseScrolled(0,0,0,-1);check(selected(s)==2,"Scroll selects Ricochet");s.keyPressed(new KeyEvent(264,0,0));check(selected(s)==0,"Selection wraps to Travel");}
            if(ticks==155)Screenshot.grab(mc,false);
            if(ticks==160)((AsgardAbilityScreen)mc.gui.screen()).keyPressed(new KeyEvent(257,0,0));
            if(ticks==175){check(mc.gui.screen() instanceof BifrostDestinationScreen,"Owned Stormbreaker Travel validates through native network");mc.setScreenAndShow(null);}
            if(ticks==180){mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
            if(ticks==195){check(mc.gui.screen() instanceof BifrostDestinationScreen,"Native Stormbreaker right-use opens destinations");check(!mc.player.isUsingItem(),"Stormbreaker travel has no phantom held channel");AsgardClient.ABILITY.setKey(InputConstants.UNKNOWN);KeyMapping.resetMapping();check(AsgardClient.ABILITY.isUnbound(),"Temporary test key restored unbound");mc.setScreenAndShow(null);}
            if(ticks==205)server(p->{p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.THORS_HAMMER.get()));check(WeaponOwnership.claim(p),"Explicitly claims throw-test hammer");status.set("throw-ready");});
            if(ticks==225){
                var old=mc.hitResult;mc.hitResult=new net.minecraft.world.phys.EntityHitResult(mc.player);
                var melee=new net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,InteractionHand.MAIN_HAND);
                NeoForge.EVENT_BUS.post(melee);check(!melee.isCanceled(),"Ordinary entity left-click remains native melee");
                mc.hitResult=null;
                var air=new net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,InteractionHand.MAIN_HAND);
                NeoForge.EVENT_BUS.post(air);check(air.isCanceled()&&!air.shouldSwingHand(),"Air left-click intercepted for default throw");
                mc.hitResult=old;
            }
            if(ticks==230)server(p->{check(p.getMainHandItem().isEmpty(),"Air-attack intent removes authoritative thrown hammer");status.set("thrown");});
            if(ticks==300)server(p->{check(AsgardWeapons.kind(p.getMainHandItem()).equals("mjolnir"),"Thrown hammer returns to owner through native client input");status.set("returned");});
            if(ticks==315){check("returned".equals(status.get()),"Client input throw completed");}
            if(ticks==320)server(p->{
                p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();
                p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.HEIMDALLS_SWORD.get()));
                var level=p.level();
                for(int center:new int[]{256,320})for(int x=center-10;x<=center+10;x++)for(int z=246;z<=266;z++)
                    level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,63,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                var markerPos=new net.minecraft.core.BlockPos(320,63,256);level.setBlockAndUpdate(markerPos,ModBlocks.BIFROST_MARKER.get().defaultBlockState());
                visualMarker=BifrostMarkerService.register(level,markerPos,"Client beam destination");
                p.teleportTo(256.5,64,256.5);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                var cow=net.minecraft.world.entity.EntityTypes.COW.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                cow.setPos(258.5,64,256.5);cow.setNoAi(true);cow.setPersistenceRequired();level.addFreshEntity(cow);passenger=cow.getUUID();
                status.set("transit-stage-ready");
            });
            if(ticks>=350&&!cameraReady){
                    mc.setScreenAndShow(null);var camera=new net.minecraft.world.entity.decoration.ArmorStand(mc.level,288.5,98.3,340.5);
                    camera.setYRot(180);camera.yRotO=180;camera.setYHeadRot(180);camera.yHeadRotO=180;camera.setXRot(0);camera.xRotO=0;camera.setOldPosAndRot();camera.setNoGravity(true);mc.setCameraEntity(camera);cameraReady=true;
                    for(String sound:List.of("charge","descent","impact","closure"))check(mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("bifrost","sounds/transit_"+sound+".ogg")).isPresent(),"Packaged original sound resource "+sound);
            }
            if(ticks==380)server(p->{check(BifrostTeleportManager.begin(p,visualMarker,"Hofund"),"Connected cinematic transit begins");transitStart=p.level().getGameTime();status.set("transit-start");});
            if(transitStart>=0){
                long age=mc.getSingleplayerServer().overworld().getGameTime()-transitStart;
                if(visualStage==0&&age>=7){Screenshot.grab(mc,false);checks.add("OBSERVED screenshot source/target descent at server age "+age);visualStage++;}
                else if(visualStage==1&&age>=15){Screenshot.grab(mc,false);checks.add("OBSERVED screenshot full source impact at server age "+age);visualStage++;}
                else if(visualStage==2&&age>=32){Screenshot.grab(mc,false);checks.add("OBSERVED screenshot upward source retraction at server age "+age);visualStage++;}
                else if(visualStage==3&&age>=65){Screenshot.grab(mc,false);checks.add("OBSERVED screenshot target arrival/retraction at server age "+age);visualStage++;}
                if(age>=70&&!arrivalChecked){arrivalChecked=true;server(p->{
                    var target=new net.minecraft.world.phys.Vec3(320.5,64,256.5);var cow=p.level().getEntity(passenger);
                    check(p.position().distanceToSqr(target)<64,"Connected initiator arrives at target beam");
                    check(cow!=null&&cow.position().distanceToSqr(target)<64,"Impact-captured NPC passenger arrives at target beam");
                    status.set("arrived");
                });}
                if(age>=110){
                    check("arrived".equals(status.get()),"Server arrival checks completed");
                    check(visualStage==4,"All four cinematic screenshot phases captured");
                    check(sounds.getOrDefault("transit_charge",0)==1,"PlaySoundEvent charge exactly once: "+sounds);
                    check(sounds.getOrDefault("transit_descent",0)==2,"PlaySoundEvent source and target descent once each");
                    check(sounds.getOrDefault("transit_impact",0)==2,"PlaySoundEvent source and target impact once each");
                    check(sounds.getOrDefault("transit_closure",0)==2,"PlaySoundEvent source and target closure once each");
                    mc.setCameraEntity(mc.player);result("PASS "+checks.stream().filter(c->c.startsWith("PASS")).count()+" connected Asgardian client checks; four cinematic phase screenshots; sound play-path events "+sounds+" (no audible-hearing claim)");
                }
            }
            if(ticks>800)result("FAIL timeout");
        }catch(Throwable t){t.printStackTrace();result("FAIL "+t);}
    }
}
