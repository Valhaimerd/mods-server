package local.bifrostskillprobe;

import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.api.spell.SpellDataComponents;
import net.spell_engine.api.spell.container.*;
import net.bettercombat.logic.WeaponRegistry;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.client.input.SpellHotbar;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import java.util.concurrent.atomic.AtomicReference;

@Mod(value="bifrost_skill_probe",dist=Dist.CLIENT)
public class ClientSkillProbe {
    int ticks;boolean done,hidden,checked;List<String> results=Collections.synchronizedList(new ArrayList<>());
    AtomicReference<String> serverStatus=new AtomicReference<>();
    static final Identifier TRAVEL=Identifier.parse("bifrost:travel");
    public ClientSkillProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
    void check(boolean ok,String label){results.add((ok?"PASS ":"FAIL ")+label);}
    ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
    List<String> actives(){return SpellContainerSource.activeSpellsOf(Minecraft.getInstance().player).stream().map(h->h.unwrapKey().orElseThrow().identifier().toString()).toList();}
    void refresh(){var p=Minecraft.getInstance().player;((SpellContainerSource.Owner)p).spellContainerCache().clear();SpellContainerSource.update(p);}
    void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
        if(done||mc.player==null||mc.level==null||++ticks<40)return;
        try{
            if(checked){continuation();return;}
            checked=true;
            var travel=SpellRegistry.from(mc.level).get(TRAVEL).orElse(null);
            check(travel!=null,"Bifrost registered as native RPG active spell");
            if(travel!=null){
                check(travel.value().cost.cooldown.duration==0,"Menu spell has zero cooldown");
                for(String id:List.of("bifrost:heimdalls_sword","bifrost:thors_hammer","bifrost:stormbreaker")){
                    mc.player.setItemInHand(InteractionHand.MAIN_HAND,stack(id));refresh();
                    var ids=actives();check(!ids.isEmpty()&&ids.getLast().equals(TRAVEL.toString()),id+" appends Bifrost after native skill");
                    check(ids.stream().filter(TRAVEL.toString()::equals).count()==1,id+" no duplicate travel");
                    check(SpellCast.Option.allOf(mc.player).getLast().id().equals(TRAVEL),id+" native cast options end with travel");
                }
                // Inject a realistic mixed book/relic source through the native source API.
                var book=new ItemStack(Items.BOOK);
                var extra=SpellContainers.forWeapon(SpellContainer.ContentType.ANY,List.of(Identifier.parse("rogues:shock_powder"),Identifier.parse("rogues:slice_and_dice"),Identifier.parse("rogues:vanish"),Identifier.parse("wizards:arcane_blink")));
                book.set(SpellDataComponents.SPELL_CONTAINER,extra);
                SpellContainerSource.addSource(new SpellContainerSource.Entry("probe_book_and_relic",(p,n)->List.of(new SpellContainerSource.SourcedContainer(n,book,extra)),p->1));
                refresh();var ids=actives();results.add("ORDER "+ids);
                check(ids.size()==6&&ids.getLast().equals(TRAVEL.toString()),"native weapon + three class + relic + Bifrost is sixth overall");
                check(ids.subList(1,5).equals(extra.spell_ids()),"book and relic relative order preserved");
                for(int i=0;i<10;i++)SpellContainerSource.update(mc.player);
                check(actives().equals(ids),"repeated container updates do not duplicate or reorder skills");
                mc.player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));refresh();
                check(!actives().contains(TRAVEL.toString()),"travel disappears on ordinary weapon swap");
                mc.player.setItemInHand(InteractionHand.MAIN_HAND,stack("bifrost:heimdalls_sword"));refresh();
                check(actives().getLast().equals(TRAVEL.toString()),"travel restores at tail after re-equipping");
                var hofund=mc.player.getMainHandItem();var claymore=stack("paladins:diamond_claymore");
                check(hofund.get(DataComponents.ATTRIBUTE_MODIFIERS).equals(claymore.get(DataComponents.ATTRIBUTE_MODIFIERS)),"Hofund exact Diamond Claymore attribute components");
                var gson=new com.google.gson.Gson();var profile=WeaponRegistry.getAttributes(hofund);
                check(profile!=null&&profile.isTwoHanded()&&gson.toJson(profile).equals(gson.toJson(WeaponRegistry.getAttributes(claymore))),"Hofund native two-handed claymore profile");
                var tex=Identifier.parse("bifrost:textures/spell/travel.png");check(mc.getResourceManager().getResource(tex).isPresent(),"supplied Bifrost icon packaged and available");
                mc.getSingleplayerServer().execute(()->{
                    try{
                        var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                        p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                        p.setItemInHand(InteractionHand.MAIN_HAND,stack("bifrost:heimdalls_sword"));
                        var helper=Class.forName("com.emandev.bifrost.BifrostSkills");var open=helper.getMethod("open",net.minecraft.server.level.ServerPlayer.class);
                        PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.HUMAN);RaceAttributes.applyRaceAttributes(p,Race.HUMAN);
                        check(!(Boolean)open.invoke(null,p),"non-Asgardian travel cast denied server-side");
                        PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.ASGARDIAN);RaceAttributes.applyRaceAttributes(p,Race.ASGARDIAN);RaceNetworkHandler.syncRace(p,Race.ASGARDIAN);
                        for(int action=0;action<4;action++){
                            var handle=Class.forName("com.emandev.bifrost.AsgardNetwork").getDeclaredMethod("handle",net.minecraft.server.level.ServerPlayer.class,int.class,int.class);handle.setAccessible(true);
                            check(!(Boolean)handle.invoke(null,p,action,1),"legacy ability packet "+action+" denied");
                        }
                        var abilities=Class.forName("com.emandev.bifrost.AsgardAbilities");
                        check(!(Boolean)abilities.getMethod("beginPrimary",net.minecraft.server.level.ServerPlayer.class).invoke(null,p),"old held-use primary disabled");
                        check(!(Boolean)abilities.getMethod("activate",net.minecraft.server.level.ServerPlayer.class,int.class).invoke(null,p,1),"old selector combat disabled");
                        var ownership=Class.forName("com.emandev.bifrost.WeaponOwnership");
                        for(String id:List.of("bifrost:thors_hammer","bifrost:stormbreaker")){
                            p.setItemInHand(InteractionHand.MAIN_HAND,stack(id));
                            check((Boolean)ownership.getMethod("claim",net.minecraft.server.level.ServerPlayer.class).invoke(null,p),id+" ownership claim still works");
                            check((Boolean)ownership.getMethod("canUse",net.minecraft.server.level.ServerPlayer.class,ItemStack.class).invoke(null,p,p.getMainHandItem()),id+" owner remains eligible");
                            check(!(Boolean)ownership.getMethod("throwWeapon",net.minecraft.server.level.ServerPlayer.class,ItemStack.class,boolean.class).invoke(null,p,p.getMainHandItem(),false),id+" default throwing disabled even for owner");
                            check(p.getMainHandItem().getItem().use(p.level(),p,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.PASS,id+" direct use no longer opens travel or combat");
                        }
                        p.setItemInHand(InteractionHand.MAIN_HAND,stack("bifrost:heimdalls_sword"));
                        var use=p.getMainHandItem().getItem().use(p.level(),p,InteractionHand.MAIN_HAND);
                        check(use==net.minecraft.world.InteractionResult.PASS&&!p.isUsingItem(),"Hofund right-click leaves native weapon skill free");
                        ((SpellContainerSource.Owner)p).spellContainerCache().clear();SpellContainerSource.update(p);
                        check(SpellCast.Option.allOf(p).getLast().id().equals(TRAVEL),"server cast options also append Bifrost last");
                        var item=p.getMainHandItem();
                        ((SpellCaster.Player)p).getInteractor().requestCast(TRAVEL,SpellCast.TargetSnapshot.EMPTY);
                        serverStatus.set("cast");
                    }catch(Throwable t){results.add("FAIL server "+t);serverStatus.set("failed");}
                });
                return;
            }
        }catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}
        finish();
    }
    void continuation(){
        var mc=Minecraft.getInstance();
        if("visual".equals(serverStatus.get())){
            if(ticks==110){net.minecraft.client.Screenshot.grab(mc,false);}
            if(ticks>=120)finish();
            return;
        }
        if(ticks>240){check(false,"native skill menu timed out: "+serverStatus.get());finish();return;}
        if(!"cast".equals(serverStatus.get())&&!"recast".equals(serverStatus.get())){if("failed".equals(serverStatus.get()))finish();return;}
        if(ticks==75){
            check(mc.gui.screen()!=null&&mc.gui.screen().getClass().getSimpleName().equals("BifrostDestinationScreen"),"native zero-second spell opens Bifrost destination GUI");
            SpellHotbar.INSTANCE.update(mc.player,mc.options);
            var slots=SpellHotbar.INSTANCE.slots;results.add("SLOTS "+slots);
            results.add("CONFIG spellHotbarUseKey="+net.spell_engine.client.SpellEngineClient.config.spellHotbarUseKey+" use="+mc.options.keyUse.getTranslatedKeyMessage().getString());
            results.add("KEYS "+slots.stream().map(s->s.getKeyBinding(mc.options)==null?"none":s.getKeyBinding(mc.options).getTranslatedKeyMessage().getString()).toList());
            check(!slots.isEmpty()&&slots.getLast().spell()!=null&&slots.getLast().spell().is(TRAVEL),"Bifrost is the final displayed hotbar slot");
            var binding=slots.getLast().getKeyBinding(mc.options);
            results.add("BINDING "+(binding==null?"none":binding.getTranslatedKeyMessage().getString()));
            check(binding!=null&&!binding.equals(mc.options.keyUse),"Bifrost uses a numeric skill binding rather than weapon use");
            check(binding!=null&&binding.getTranslatedKeyMessage().getString().equals("5"),"native weapon uses right-click; four other skills put Bifrost on number 5");
            check(mc.options.keyMappings.length>0&&Arrays.stream(mc.options.keyMappings).noneMatch(k->k.getName().equals("key.bifrost.asgardian_ability")),"legacy selector key no longer registered");
            net.minecraft.client.Screenshot.grab(mc,false);
            mc.setScreenAndShow(null);
            // Exercise the actual numeric hotbar input path, not a custom ability packet.
            SpellHotbar.INSTANCE.prepare(0);binding.setDown(true);
            SpellHotbar.INSTANCE.handleOther(mc.player,mc.options,List.of());binding.setDown(false);
            serverStatus.set("recast");
        }
        if(ticks>=95&&"recast".equals(serverStatus.get())){
            check(mc.gui.screen()!=null&&mc.gui.screen().getClass().getSimpleName().equals("BifrostDestinationScreen"),"Bifrost can reopen without a menu-spell cooldown");
            mc.setScreenAndShow(null);serverStatus.set("visual");
        }
    }
    void finish(){
        var mc=Minecraft.getInstance();done=true;
        try{Files.write(mc.gameDirectory.toPath().resolve("skill-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("skill-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}
        mc.stop();
    }
}
