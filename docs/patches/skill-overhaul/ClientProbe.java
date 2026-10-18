package local.preview;
import java.nio.file.*;
import net.minecraft.client.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("preview_probe")
public class ClientProbe {
 boolean isolated; int ticks; net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening event){if(event.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();
  if(!isolated){long w=mc.getWindow().handle();org.lwjgl.glfw.GLFW.glfwSetKeyCallback(w,null);org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback(w,null);org.lwjgl.glfw.GLFW.glfwHideWindow(w);mc.options.pauseOnLostFocus=false;isolated=true;}
  if(mc.player==null)return;mc.mouseHandler.releaseMouse();
  if(++ticks==40){var server=mc.getSingleplayerServer();if(server!=null)server.execute(()->{
   var player=server.getPlayerList().getPlayers().getFirst();
   try {
    var manager=Class.forName("io.redspace.pvp_flagging.core.PlayerFlagManager");
    manager.getMethod("flagPlayer",net.minecraft.server.level.ServerPlayer.class).invoke(manager.getField("INSTANCE").get(null),player);
   }catch(Exception ex){throw new RuntimeException(ex);}
   net.puffish.skillsmod.api.SkillsAPI.getCategory(net.minecraft.resources.Identifier.parse("adventurer_skills:adventurer_skills")).orElseThrow().openScreen(player);
  });}
  if(ticks==60 && screen!=null){
   screen.mouseScrolled(210,150,0,-10);
   try{var f=screen.getClass().getDeclaredField("optActiveCategoryData");f.setAccessible(true);var cat=(net.puffish.skillsmod.client.data.ClientCategoryData)((java.util.Optional<?>)f.get(screen)).orElseThrow();
    for(var key:java.util.List.of("martial_prowess","hunter","magistrate","aegis","colossus","bulwark","packleader","guardian_stead","root_versatile")){
     var title=cat.getConfig().definitions().get(key).title();if(title.getStyle().getColor()==null || title.getStyle().getColor().getValue()!=0x5555ff)throw new AssertionError("Not blue: "+key);
    }
    Files.writeString(mc.gameDirectory.toPath().resolve("title-check.txt"),"PASS 9 blue client title components");
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  if(ticks==70 || ticks==85){var server=mc.getSingleplayerServer();if(server!=null)server.execute(()->{
   try{var manager=Class.forName("io.redspace.pvp_flagging.core.PlayerFlagManager");
    manager.getMethod(ticks<85 ? "unflagPlayer":"cancelScheduledUnflag",net.minecraft.server.level.ServerPlayer.class).invoke(manager.getField("INSTANCE").get(null),server.getPlayerList().getPlayers().getFirst());
   }catch(Exception ex){throw new RuntimeException(ex);}
  });}
  if(ticks==80 || ticks==95){
   try{var helper=Class.forName("io.redspace.pvp_flagging.client.ClientHelper");var deadline=(Integer)helper.getMethod("getUnflagTimestamp").invoke(null);
    if(ticks==80 && deadline<=mc.player.tickCount)throw new AssertionError("Unflag scheduling packet missing");
    if(ticks==95 && deadline!=-1)throw new AssertionError("Unflag cancellation packet missing");
   }catch(Exception ex){throw new RuntimeException(ex);}
  }
  if(ticks==100){
   try{var helper=Class.forName("io.redspace.pvp_flagging.client.ClientHelper");
    if(!(Boolean)helper.getMethod("isFlagged",net.minecraft.world.entity.player.Player.class).invoke(null,mc.player))throw new AssertionError("PvP flag packet not received");
    mc.gui.hud.getTabList().setVisible(true);
    Files.writeString(mc.gameDirectory.toPath().resolve("pvp-client-result.txt"),"PASS PvP flag sync, HUD/tab classes, 9 blue specialization titles");
   }catch(Exception ex){throw new RuntimeException(ex);}
   Screenshot.grab(mc,false);try{Files.writeString(mc.gameDirectory.toPath().resolve("skill-screen.txt"),"captured Adventurer screen");}catch(Exception e){throw new RuntimeException(e);}}
  if(ticks==110){
   try{
    var helper=Class.forName("io.redspace.pvp_flagging.client.ClientHelper");
    var suffix=((net.minecraft.network.chat.Component)helper.getField("NAME_TAG_INDICATOR").get(null)).getString();
    var method=net.minecraft.client.gui.components.PlayerTabOverlay.class.getDeclaredMethod("decorateName",net.minecraft.client.multiplayer.PlayerInfo.class,net.minecraft.network.chat.MutableComponent.class);method.setAccessible(true);
    var info=mc.getConnection().getPlayerInfo(mc.player.getUUID());
    var name=(net.minecraft.network.chat.Component)method.invoke(mc.gui.hud.getTabList(),info,net.minecraft.network.chat.Component.literal("Probe"));
    if(!name.getString().endsWith(suffix))throw new AssertionError("Tab overlay flag suffix missing");
    Files.writeString(mc.gameDirectory.toPath().resolve("pvp-client-result.txt"),"PASS PvP flag sync, unflag/cancel packets and HUD messages, tab name mixin, HUD rendering, 9 blue specialization titles");
   }catch(Exception ex){throw new RuntimeException(ex);}
   mc.gui.setScreen(null);
  }
  if(ticks==125)Screenshot.grab(mc,false);
  if(ticks==160)mc.stop();
 }
}
