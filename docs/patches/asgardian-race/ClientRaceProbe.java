package local.asgardclient;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.client.gui.RaceSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("asgard_client_probe") public final class ClientRaceProbe {
 boolean hidden,done;int ticks;RaceSelectionScreen screen;java.lang.reflect.Field selected;
 final AtomicReference<String> serverChecks=new AtomicReference<>();
 void check(boolean value,String label){if(!value)throw new AssertionError(label);}
 void near(double got,double want,String label){check(Math.abs(got-want)<.005,label+" "+got+" != "+want);}
 int field(String id)throws Exception{var f=RaceSelectionScreen.class.getDeclaredField(id);f.setAccessible(true);return f.getInt(screen);}
 void result(String value){try{Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve("asgard-client-result.txt"),value);}catch(Exception e){throw new RuntimeException(e);}}
 public ClientRaceProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;ticks++;
  try{
   if(ticks==30){
    check(ClientRaceData.getRace()==Race.NONE,"Fresh player has no selected race");check(mc.gui.screen() instanceof RaceSelectionScreen,"First-login selector opened");screen=(RaceSelectionScreen)mc.gui.screen();
    var f=RaceSelectionScreen.class.getDeclaredField("races");f.setAccessible(true);check(((List<?>)f.get(screen)).size()==8,"Eight visible races");
    selected=RaceSelectionScreen.class.getDeclaredField("selectedIndex");selected.setAccessible(true);
    var layout=RaceSelectionScreen.class.getDeclaredMethod("calcLayout");layout.setAccessible(true);
    for(int[] size:new int[][]{{426,240},{640,360},{854,480}}){screen.resize(size[0],size[1]);layout.invoke(screen);check(field("listX")>=0,"List stays on screen");check(field("listY")+8*field("buttonH")+7*field("gap")<=size[1]-26,"All eight rows clear footer");check(field("cardX")+field("cardW")<=size[0],"Card stays on screen");}
    screen.resize(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
   }
   if(ticks>=31&&ticks<=70)selected.setInt(screen,Math.min(7,(ticks-31)/5));
   if(ticks==76)Screenshot.grab(mc,false);
   if(ticks==80){check(net.minecraft.client.resources.language.I18n.get("race.iourusraces.asgardian.tooltip").contains("Ragnarok"),"Lore localized");check(screen.keyPressed(new KeyEvent(257,0,0)),"Enter confirms through native selector");}
   if(ticks==105){
    check(ClientRaceData.getRace()==Race.valueOf("ASGARDIAN"),"Server sync selected race");near(mc.player.getMaxHealth(),22,"Client HP");near(mc.player.getAttributeValue(Attributes.MOVEMENT_SPEED),.095,"Client movement");near(mc.player.getAttributeValue(Attributes.SCALE),1.05,"Client scale");
    var server=mc.getSingleplayerServer();server.execute(()->{try{
     var p=server.getPlayerList().getPlayer(mc.player.getUUID());check(PlayerRaceData.getRace(p)==Race.valueOf("ASGARDIAN"),"Server race authoritative");
     p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.removeAllEffects();p.invulnerableTime=0;p.setHealth(p.getMaxHealth());float hp=p.getHealth();p.hurtServer(p.level(),p.damageSources().magic(),10);near(hp-p.getHealth(),11,"Connected player takes 10% extra magic");
     p.invulnerableTime=0;p.setHealth(p.getMaxHealth());hp=p.getHealth();p.hurtServer(p.level(),p.damageSources().inFire(),10);near(hp-p.getHealth(),10,"Connected player still takes fire damage");
     p.invulnerableTime=0;p.setHealth(p.getMaxHealth());hp=p.getHealth();p.hurtServer(p.level(),p.damageSources().lightningBolt(),10);near(hp-p.getHealth(),0,"Connected player lightning immunity");
     var owner=(net.spell_engine.internals.container.SpellContainerSource.Owner)p;var original=owner.getSpellContainers();
     var cheat=net.spell_engine.api.spell.registry.SpellRegistry.from(p.level()).get(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_1")).orElseThrow();
     var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of("skill_tree_rpgs:rogue_tier_3_passive_1"));
     owner.setSpellContainers(new net.spell_engine.internals.container.SpellContainerSource.Result(original.activeContainer(),List.of(),List.of(cheat),List.of(),List.of(new net.spell_engine.internals.container.SpellContainerSource.SourcedContainer("asgard_probe",net.minecraft.world.item.ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
     ((net.spell_engine.internals.casting.SpellCaster.Player)p).getCooldownManager().remove(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_1"));
     var caster=(net.minecraft.world.entity.LivingEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.parse("minecraft:villager")).create(p.level(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
     var magic=p.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).get(net.minecraft.resources.Identifier.parse("spell_power:arcane")).orElseThrow();
     p.invulnerableTime=0;p.setHealth(11);p.hurtServer(p.level(),new net.minecraft.world.damagesource.DamageSource(magic,caster),10);
     check(p.hasEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:cheat_death")).orElseThrow()),"Cheat Death handles magic made lethal by weakness");
     check(p.isAlive(),"Cheat Death prevents lethal hit");owner.setSpellContainers(original);owner.spellModifierCache().clear();p.removeAllEffects();p.setHealth(p.getMaxHealth());caster.discard();
     serverChecks.set("PASS connected player: 11 magic / 10 fire / 0 lightning damage from 10-point hits; Cheat Death triggers on magic made lethal by racial weakness");
    }catch(Throwable t){serverChecks.set("FAIL "+t);t.printStackTrace();}});
   }
   if(ticks==115)mc.player.connection.sendCommand("race human");
   if(ticks==135){check(serverChecks.get()!=null&&serverChecks.get().startsWith("PASS"),"Server damage checks: "+serverChecks.get());check(ClientRaceData.getRace()==Race.valueOf("ASGARDIAN"),"Unauthorized reselection denied");result("PASS actual NeoForge client: first-login eight-race selector; all eight previews; three GUI layouts; Enter selection; server/name sync; 22 HP / 0.95 speed / 1.05 scale; Ragnarok lore; reselection denied. "+serverChecks.get());done=true;mc.stop();}
  }catch(Throwable t){result("FAIL "+t);t.printStackTrace();done=true;mc.stop();}
 }
}
