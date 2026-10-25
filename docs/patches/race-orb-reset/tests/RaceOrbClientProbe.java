package local.raceorb;
import java.nio.file.*;
import java.util.*;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.client.gui.RaceSelectionScreen;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.puffish.skillsmod.api.SkillsAPI;
@Mod("race_orb_probe")
public class RaceOrbClientProbe {
 int ticks;boolean done,hidden;volatile String error;List<String> checks=Collections.synchronizedList(new ArrayList<>());
 public RaceOrbClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean b,String label){if(!b)throw new AssertionError(label);checks.add("PASS "+label);}
 interface ServerAction {void accept(net.minecraft.server.level.ServerPlayer p)throws Exception;}
 void server(ServerAction action){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){t.printStackTrace();error=t.toString();}});}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null)return;ticks++;
 try{
  if(error!=null)throw new AssertionError(error);
  if(ticks==40){mc.setScreenAndShow(null);server(p->{p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);for(var id:net.skill_tree_rpgs.utils.SkillHelper.RESET_CATEGORIES)SkillsAPI.getCategory(id).ifPresent(c->c.resetSkills(p));PlayerRaceData.setRace(p,Race.ELF);});}
  if(ticks==75){check(ClientRaceData.getRace()==Race.ELF,"saved race sync received");check(!(mc.gui.screen() instanceof RaceSelectionScreen),"selected race has no selection panel");ClientRaceData.reset();mc.setScreenAndShow(null);}
  if(ticks>75&&ticks<175)check(!(mc.gui.screen() instanceof RaceSelectionScreen),"no premature panel during delayed sync tick "+ticks);
  if(ticks==175){check(ClientRaceData.getRace()==Race.NONE,"unsynchronized client carries NONE without prompting");mc.setScreenAndShow(new RaceSelectionScreen());server(p->RaceNetworkHandler.syncRace(p,PlayerRaceData.getRace(p)));}
  if(ticks==205){check(ClientRaceData.getRace()==Race.ELF,"late saved-race packet restored identity");check(!(mc.gui.screen() instanceof RaceSelectionScreen),"late known race closes stale selection panel");
   server(p->{
    var other=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(p.level());PlayerRaceData.setRace(other,Race.DWARF);
    var item=BuiltInRegistries.ITEM.getValue(Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));check(item!=Items.AIR,"actual Orb item registered");
    p.getInventory().setSelectedSlot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));var stack=p.getMainHandItem();int before=stack.getDamageValue();
    var result=item.use(p.level(),p,InteractionHand.MAIN_HAND);check(result.consumesAction(),"race-only native Orb use succeeds with zero spent skills");check(PlayerRaceData.getRace(p)==Race.NONE,"Orb removes server race");check(stack.isEmpty()||stack.getDamageValue()==before+1,"successful race reset consumes normal Orb use");check(PlayerRaceData.getRace(other)==Race.DWARF,"other player's race unchanged");check(Math.abs(p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-.1)<.0001,"old Elf movement modifier removed immediately");
   });
  }
  if(ticks==240){check(ClientRaceData.getRace()==Race.NONE,"Orb sync confirms raceless identity");check(mc.gui.screen() instanceof RaceSelectionScreen,"Orb automatically opens race selection");Screenshot.grab(mc,false);server(p->{var result=p.level().getServer().getCommands().getDispatcher().execute("race goblin",p.createCommandSourceStack());check(result==1&&PlayerRaceData.getRace(p)==Race.GOBLIN,"native race selection command works after Orb reset");});}
  if(ticks==275){check(ClientRaceData.getRace()==Race.GOBLIN,"new race synchronized");check(!(mc.gui.screen() instanceof RaceSelectionScreen),"confirmed selection closes panel");
   server(p->{
    var category=SkillsAPI.getCategory(Identifier.parse("adventurer_skills:adventurer_skills")).orElseThrow();category.setExtraPoints(p,1000);category.getSkill("6xmqgm3uw97hrynh").orElseThrow().unlock(p);check(category.getSpentPoints(p)>0,"spent Adventurer skill fixture unlocked");
    var orb=BuiltInRegistries.ITEM.getValue(Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));check(orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"combined native skill/race reset succeeds");check(category.getSpentPoints(p)==0,"Orb still resets Adventurer skills");check(PlayerRaceData.getRace(p)==Race.NONE,"combined reset also clears race");
   });
  }
  if(ticks==315){check(mc.gui.screen() instanceof RaceSelectionScreen,"second Orb reset prompts again in same session");mc.setScreenAndShow(null);}
  if(ticks==335){check(!(mc.gui.screen() instanceof RaceSelectionScreen),"dismissing confirmed raceless panel does not reopen every tick");server(p->{var orb=BuiltInRegistries.ITEM.getValue(Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(orb));var stack=p.getMainHandItem();check(!orb.use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"no race and no spent skills preserve failed Orb behavior");check(!stack.isEmpty()&&stack.getDamageValue()==0,"failed reset does not spend Orb");});}
  if(ticks==365){server(p->{PlayerRaceData.setRace(p,Race.HUMAN);p.level().getServer().getDataStorage().saveAndJoin();});}
  if(ticks==390){check(ClientRaceData.getRace()==Race.HUMAN,"post-reset saved race synchronized");ClientRaceData.reset();check(!ClientRaceData.isSynced(),"logout clears readiness for next connection");finish("PASS "+checks.size()+" delayed-sync/native-Orb/UI checks");}
 }catch(Throwable t){t.printStackTrace();finish("FAIL "+t);}}
 void finish(String result){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("race-orb-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("race-orb-checks.txt"),checks);}catch(Exception ex){throw new RuntimeException(ex);}mc.stop();}
}
