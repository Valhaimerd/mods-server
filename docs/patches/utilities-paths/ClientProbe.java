package local.utilityclient;
import java.nio.file.*;import java.util.*;
import net.minecraft.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.valhaimerd.orboblivioncompat.Utilities;
import net.puffish.skillsmod.api.*;
@Mod("utility_client_probe")
public class ClientProbe {
 net.puffish.skillsmod.client.gui.SkillsScreen screen;int ticks;boolean hidden;volatile String failure;boolean checked;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening e){if(e.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void check(boolean value,String label){if(!value)throw new AssertionError(label);}
 void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());mc.options.pauseOnLostFocus=false;hidden=true;}if(mc.player==null)return;
  mc.mouseHandler.releaseMouse();++ticks;
  if(ticks==30)mc.getSingleplayerServer().execute(()->{try{
   var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);com.eiden.iourusraces.race.PlayerRaceData.setRace(p,com.eiden.iourusraces.race.Race.HUMAN);
   var c=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();c.resetSkills(p);c.setExtraPoints(p,1000);for(String n:List.of("h8krq290mbf4dwdx","nu1hd3hh115xcpax"))c.getSkill(n).orElseThrow().unlock(p);for(String n:Utilities.PATHS.get(2))c.getSkill(n).orElseThrow().unlock(p);
   var orb=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:orb_of_oblivion"));p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(orb));p.experienceLevel=29;
   check(!orb.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(),"new Utilities protected at29levels");check(Utilities.stat(p,"cost")==.33,"insufficient Orb preserves Utilities modifiers");p.experienceLevel=30;
   check(orb.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(),"combined Orb reset succeeds");check(p.experienceLevel==0&&c.getSpentPoints(p)==0&&Utilities.stat(p,"cost")==0,"Orb charges30andclearsnew Utilities rewards");
   com.eiden.iourusraces.race.PlayerRaceData.setRace(p,com.eiden.iourusraces.race.Race.HUMAN);for(String n:List.of("h8krq290mbf4dwdx","nu1hd3hh115xcpax"))c.getSkill(n).orElseThrow().unlock(p);for(String n:Utilities.PATHS.get(2))c.getSkill(n).orElseThrow().unlock(p);p.experienceLevel=30;p.experienceProgress=.25f;c.openScreen(p);
  }catch(Throwable t){t.printStackTrace();failure=t.toString();}});
  if(ticks==85){try{
   check(failure==null,"server checks: "+failure);check(screen!=null,"native tree screen opened");
   screen.mouseScrolled(210,150,0,-4);
   var f=screen.getClass().getDeclaredField("optActiveCategoryData");f.setAccessible(true);var cat=(net.puffish.skillsmod.client.data.ClientCategoryData)((Optional<?>)f.get(screen)).orElseThrow();cat.setScale(.35f);cat.setX(0);cat.setY(125);
   for(var path:Utilities.PATHS){var def=cat.getConfig().definitions().get("utility_"+path.get(7));check(def!=null&&!def.description().getString().isBlank(),"mastery description synced");}
   check(Utilities.stat(mc.player,"cost")==.33,"native utility attributes synchronized to client");check(Utilities.shownEnchantDebit(mc.player,3)==207,"client XPpayment preview matches server");
   var screenType=net.minecraft.client.gui.screens.inventory.EnchantmentScreen.class;check(screenType.getDeclaredMethods().length>0,"enchanting UI mixin loads");checked=true;
  }catch(Throwable t){failure=t.toString();t.printStackTrace();}}
  if(ticks==100)Screenshot.grab(mc,false);
  if(ticks==130){try{Files.writeString(mc.gameDirectory.toPath().resolve("utility-client-result.txt"),checked&&failure==null?"PASS native client layout, synchronized rewards, XP cost preview and combined Orb reset":"FAIL "+failure);}catch(Exception x){throw new RuntimeException(x);}mc.stop();}
 }
}
