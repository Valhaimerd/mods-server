package local.combatclient;
import io.redspace.pvp_flagging.*;
import io.redspace.pvp_flagging.client.CombatHud;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod("combat_client_probe")
public class CombatClientProbe {
 int ticks,finishedAt;volatile long expirySent;boolean hidden,done,expired;List<String> checks=new ArrayList<>();
 public CombatClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add("PASS "+text);}
 long deadline(String n)throws Exception{var f=CombatHud.class.getDeclaredField(n);f.setAccessible(true);return f.getLong(null);}
 void capture(String name)throws Exception{Screenshot.grab(Minecraft.getInstance(),false);checks.add("CAPTURE "+name);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}if(done||mc.player==null||mc.level==null)return;ticks++;
 try{
  if(ticks==40){mc.setScreenAndShow(null);mc.options.guiScale().set(3);mc.resizeGui();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());CombatRules.flag(p);CombatRules.attempt(p,false,1);CombatRules.completed(p,false,30);CombatRules.attempt(p,true,1);CombatRules.completed(p,true,20);});}
  if(ticks==65){check(deadline("combat")>System.nanoTime()/1000000+30000,"real server packet starts client combat countdown");check(deadline("breaking")>System.nanoTime()/1000000,"breaking countdown delivered");check(deadline("placing")>System.nanoTime()/1000000,"placement countdown delivered");check(CombatHud.SIZE==22,"square size matches native 22-pixel skill panel height");capture("scale3-active");}
  if(ticks==75)mc.getSingleplayerServer().execute(()->CombatRules.flag(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));
  if(ticks==95){mc.options.guiScale().set(2);mc.resizeGui();}
  if(ticks==120){capture("scale2-active");mc.gui.hud.toggle();}
  if(ticks==150){check(mc.gui.hud.isHidden(),"hide-GUI state respected");capture("hidden-gui");mc.gui.hud.toggle();mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());var s=CombatRules.state(p);s.until=System.currentTimeMillis()+1000;CombatRules.save(p,s);CombatRules.sync(p);expirySent=System.nanoTime();});}
  if(!expired&&expirySent>0&&System.nanoTime()-expirySent>3_000_000_000L){check(deadline("combat")<=System.nanoTime()/1000000,"client countdown expires on deadline");capture("expired");expired=true;finishedAt=ticks+30;mc.getSingleplayerServer().execute(()->CombatRules.flag(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID())));}
  if(expired&&ticks>=finishedAt)finish("PASS "+checks.size()+" connected HUD checks/captures");
 if(ticks>600)throw new AssertionError("expiry fixture timeout");
 }catch(Throwable t){t.printStackTrace();finish("FAIL "+t);}}
 void finish(String text){done=true;var mc=Minecraft.getInstance();try{Files.writeString(mc.gameDirectory.toPath().resolve("combat-client-result.txt"),text);Files.write(mc.gameDirectory.toPath().resolve("combat-client-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
