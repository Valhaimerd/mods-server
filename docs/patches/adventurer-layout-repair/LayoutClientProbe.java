package local.spatialclient;
import java.util.*;import java.nio.file.*;import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.*;import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.valhaimerd.orboblivioncompat.*;import net.puffish.skillsmod.api.*;
@Mod(value="spatial_client_probe",dist=Dist.CLIENT) public class LayoutClientProbe {
 int ticks,phase,at;boolean pending,hidden;final List<String> lines=new ArrayList<>();final AtomicReference<Throwable> error=new AtomicReference<>();
 net.puffish.skillsmod.client.gui.SkillsScreen screen;
 public LayoutClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::opening);}
 void opening(net.neoforged.neoforge.client.event.ScreenEvent.Opening e){if(e.getNewScreen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen s)screen=s;}
 void check(boolean ok,String label){lines.add((ok?"PASS ":"FAIL ")+label);if(!ok)throw new AssertionError(label);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> job){var mc=Minecraft.getInstance();pending=true;mc.getSingleplayerServer().execute(()->{try{job.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable e){error.set(e);}finally{pending=false;}});}
 void step(){phase++;at=ticks;}
 void finish(){if(phase==99)return;phase=99;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("spatial-client-checks.txt"),lines);Files.writeString(mc.gameDirectory.toPath().resolve("spatial-client-result.txt"),lines.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception ignored){}mc.stop();}
 void view(float scale,int x,int y)throws Exception{var field=screen.getClass().getDeclaredField("optActiveCategoryData");field.setAccessible(true);var category=(net.puffish.skillsmod.client.data.ClientCategoryData)((Optional<?>)field.get(screen)).orElseThrow();category.setScale(scale);category.setX(x);category.setY(y);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();ticks++;if(!hidden&&mc.getWindow()!=null){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;mc.options.pauseOnLostFocus=false;}if(phase==99)return;try{
  if(error.get()!=null)throw new RuntimeException(error.get());if(pending||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(ticks-at>2400)throw new AssertionError("Timeout phase"+phase);if(ticks-at<35)return;
  if(phase==0){server(p->{
   p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
   p.getPersistentData().putBoolean("skill_overhaul_v1",true);p.getPersistentData().putBoolean("utilities_v1_migrated",true);p.getPersistentData().putBoolean("defense_v2_refunded",true);
   var c=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();c.unlock(p);c.resetSkills(p);c.setExtraPoints(p,1000);
   int branch=0;
   for(var path:DefenseV2.PATHS){
    c.resetSkills(p);c.getSkill("ad8qet8e75ys5sek").orElseThrow().unlock(p);
    double hp=p.getMaxHealth(),armor=p.getAttributeValue(Attributes.ARMOR);
    for(int i=0;i<6;i++){check(DefenseV2.allowed(Utilities.unlocked(p),path.get(i)),"Defense allocation gate "+branch+":"+i);c.getSkill(path.get(i)).orElseThrow().unlock(p);}
    check(!DefenseV2.allowed(Utilities.unlocked(p),path.get(7)),"Mastery requires both penultimate nodes "+branch);
    c.getSkill(path.get(6)).orElseThrow().unlock(p);
    check(DefenseV2.allowed(Utilities.unlocked(p),path.get(7)),"Complete Defense path accepted "+branch);
    check(c.getSkill(path.get(7)).orElseThrow().getState(p)==Skill.State.AFFORDABLE,"Native final-node prerequisites satisfied "+branch);
    c.getSkill(path.get(7)).orElseThrow().unlock(p);
    check(DefenseV2.mastery(p,branch),"Complete mastery active "+branch);
    check(Math.abs(p.getMaxHealth()-hp-new double[]{4,6,2}[branch])<.001,"Exact permanent HP total "+branch);
    check(Math.abs(p.getAttributeValue(Attributes.ARMOR)-armor-new double[]{2,2,4}[branch])<.001,"Exact permanent armor total "+branch);
    branch++;
   }
   for(var path:Utilities.PATHS){
    c.resetSkills(p);for(var id:List.of("h8krq290mbf4dwdx","nu1hd3hh115xcpax"))c.getSkill(id).orElseThrow().unlock(p);
    for(int i=0;i<6;i++)c.getSkill(path.get(i)).orElseThrow().unlock(p);
    check(!Utilities.allowed(Utilities.unlocked(p),path.get(7)),"Utility mastery rejects missing ordinary node "+path.get(7));
    c.getSkill(path.get(6)).orElseThrow().unlock(p);
    check(Utilities.allowed(Utilities.unlocked(p),path.get(7)),"Original Utility layout supports full allocation "+path.get(7));
    check(c.getSkill(path.get(7)).orElseThrow().getState(p)==Skill.State.AFFORDABLE,"Native Utility two-link merge accepted "+path.get(7));
   }
   c.resetSkills(p);c.openScreen(p);
  });step();return;}
  if(phase==1){check(screen!=null,"Repaired tree opened on connected client");view(.52f,200,0);step();return;}
  if(phase==2){Screenshot.grab(mc,false);view(.32f,80,110);step();return;}
  if(phase==3){Screenshot.grab(mc,false);step();return;}
  if(phase==4)finish();
 }catch(Throwable e){e.printStackTrace();lines.add("FAIL "+e);finish();}}
}
