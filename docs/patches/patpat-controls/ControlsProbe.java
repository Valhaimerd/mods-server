package local.patcontrols;
import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.lopymine.patpat.client.keybinding.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
@Mod("pat_controls_probe")
public class ControlsProbe {
 int ticks;boolean done;List<String> checks=new ArrayList<>();
 public ControlsProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean b,String label){if(!b)throw new AssertionError(label);checks.add("PASS "+label);}
 void tick(ClientTickEvent.Post event){if(done||++ticks<100)return;done=true;var mc=Minecraft.getInstance();String result;
 try{
  org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());
  var key=PatPatClientKeybindingManager.getPatKeybinding();var mouse=InputConstants.Type.MOUSE.getOrCreate(1);var ctrl=InputConstants.Type.KEYSYM.getOrCreate(341);var g=InputConstants.Type.KEYSYM.getOrCreate(71);
  boolean restart=Files.exists(mc.gameDirectory.toPath().resolve("restart.marker"));
  if(restart){check(key.saveString().equals("key.keyboard.g"),"rebound G survives client restart");check(key.getKeyModifier()==KeyModifier.NONE,"rebound modifier survives restart");check(key.getFullTranslatedKeyMessage().getString().equals(g.getDisplayName().getString()),"restart display matches active key");}
  else{
   key.setKeyModifierAndCode(KeyModifier.NONE,g);check(key.getFullTranslatedKeyMessage().getString().equals(g.getDisplayName().getString()),"native key change updates active custom combination");
   mc.options.load();
   check(key.getKey().equals(mouse)&&key.getKeyModifier()==KeyModifier.CONTROL,"recommended options overrides legacy right-click-only PatPat config");
   check(PatPatKeybinding.DEFAULT_COMBINATION.getAttributeKey().equals(ctrl),"new/reset default is Left Ctrl + Right Click");
   check(!key.onKeyAction(mouse,true),"plain right-click does not activate pat combination");key.onKeyAction(mouse,false);
   check(!key.onKeyAction(ctrl,true),"modifier alone does not activate pat");check(key.onKeyAction(mouse,true),"Ctrl + right-click activates pat combination");key.onKeyAction(mouse,false);key.onKeyAction(ctrl,false);
   key.setKeyModifierAndCode(KeyModifier.NONE,g);check(key.saveString().equals("key.keyboard.g"),"native rebinding changes serialized key");
   check(!key.onKeyAction(mouse,true),"old right-click stops matching after rebind");key.onKeyAction(mouse,false);
   check(key.getFullTranslatedKeyMessage().getString().equals(g.getDisplayName().getString()),"display follows native rebind");
   key.setKeyModifierAndCode(KeyModifier.NONE,InputConstants.UNKNOWN);check(key.isUnbound(),"unbinding works");check(!key.onKeyAction(mouse,true),"unbound pat cannot capture right-click");
   key.setKeyModifierAndCode(key.getDefaultKeyModifier(),key.getDefaultKey());check(key.isDefault(),"Reset restores Ctrl + right-click");
   key.setKey(g);check(key.getFullTranslatedKeyMessage().getString().equals(g.getDisplayName().getString()),"direct key change is accepted");key.setKey(key.getDefaultKey());check(key.isDefault(),"Controls Reset button restores modifier and key");
   key.startBinding();check(!key.addBindingKey(ctrl),"Controls accepts modifier as first part");check(key.addBindingKey(mouse),"Controls accepts mouse as second part");key.sendBindingKeys();check(key.getKey().equals(mouse)&&key.getKeyModifier()==KeyModifier.CONTROL,"custom Controls capture synchronizes native binding");
   mc.options.save();mc.options.load();check(key.isDefault(),"Ctrl + right-click round-trips options save/load");
   key.setKeyModifierAndCode(KeyModifier.NONE,g);mc.options.save();Files.writeString(mc.gameDirectory.toPath().resolve("restart.marker"),"restart");
  }
  result="PASS "+checks.size()+" PatPat binding checks";
 }catch(Throwable t){t.printStackTrace();result="FAIL "+t;}
 try{Files.writeString(mc.gameDirectory.toPath().resolve("pat-controls-result.txt"),result);Files.write(mc.gameDirectory.toPath().resolve("pat-controls-checks.txt"),checks);}catch(Exception e){throw new RuntimeException(e);}mc.stop();
 }
}
