package local.bifrostcombatprobe;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.network.RaceNetworkHandler;
import net.bettercombat.network.*;
import net.bettercombat.mixin.player.LivingEntityAccessor;

@Mod(value="bifrost_combat_probe",dist=Dist.CLIENT)
public class ClientCombatProbe {
 int ticks;boolean hidden,done;int visualStart;AtomicInteger stage=new AtomicInteger();
 List<String> results=Collections.synchronizedList(new ArrayList<>());
 void check(boolean b,String s){results.add((b?"PASS ":"FAIL ")+s);}
 ItemStack stack(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
 public ClientCombatProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 boolean canUse(ServerPlayer p)throws Exception{return (Boolean)Class.forName("com.emandev.bifrost.WeaponOwnership").getMethod("canUse",ServerPlayer.class,ItemStack.class).invoke(null,p,p.getMainHandItem());}
 void runCase(ServerPlayer p,List<String> ids,int index){
  if(index>=ids.size()){
   p.level().getServer().getCommands().performPrefixedCommand(p.level().getServer().createCommandSourceStack(),"time set noon");p.level().getServer().getCommands().performPrefixedCommand(p.level().getServer().createCommandSourceStack(),"weather clear");
   p.getInventory().setItem(1,stack("bifrost:thors_hammer"));
   p.getInventory().setItem(2,stack("bifrost:heimdalls_sword"));
   p.getInventory().setItem(3,stack("paladins:netherite_great_hammer"));
   stage.set(1);return;
  }
  try{
   String id=ids.get(index);p.setItemInHand(InteractionHand.MAIN_HAND,stack(id));
   check(!canUse(p),id+" unclaimed ordinary-use diagnostic");
   var first=new IronGolem(EntityTypes.IRON_GOLEM,p.level());first.setPos(p.getX(),p.getY(),p.getZ()-2);first.setNoAi(true);p.level().addFreshEntity(first);
   ((LivingEntityAccessor)p).betterCombat_setTicksSinceLastAttack(200);
   p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(20);
   p.attack(first);results.add("FIRST "+id+" health="+first.getHealth()+" canUse="+canUse(p));
   check(first.getHealth()==100,id+" unclaimed weapon remains blocked as requested");
   check(!canUse(p),id+" attack does not silently bind an unclaimed weapon");first.discard();
   if(!canUse(p))Class.forName("com.emandev.bifrost.WeaponOwnership").getMethod("claim",ServerPlayer.class).invoke(null,p);
   check(canUse(p),id+" claimed owner eligible");
   var golem=new IronGolem(EntityTypes.IRON_GOLEM,p.level());golem.setPos(p.getX(),p.getY(),p.getZ()-2);golem.setNoAi(true);p.level().addFreshEntity(golem);
   ((LivingEntityAccessor)p).betterCombat_setTicksSinceLastAttack(200);
   p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(20);
   p.attack(golem);results.add("DIRECT "+id+" health="+golem.getHealth()+" canUse="+canUse(p));
   check(golem.getHealth()<100,id+" claimed native melee damages golem");golem.discard();
   var target=new IronGolem(EntityTypes.IRON_GOLEM,p.level());target.setPos(p.getX(),p.getY(),p.getZ()-2);target.setNoAi(true);p.level().addFreshEntity(target);
   ((LivingEntityAccessor)p).betterCombat_setTicksSinceLastAttack(200);
   ServerNetwork.handleAttackRequest(new Packets.C2S_AttackRequest(0,false,p.getInventory().getSelectedSlot(),target,List.of(target)),p.level().getServer(),p,p.connection);
   p.level().getServer().execute(()->{
    try{results.add("BETTERCOMBAT "+id+" health="+target.getHealth()+" canUse="+canUse(p));check(target.getHealth()<100,id+" claimed Better Combat melee damages golem");target.discard();runCase(p,ids,index+1);}
    catch(Throwable t){results.add("FAIL error "+t);stage.set(1);}
   });
  }catch(Throwable t){t.printStackTrace();results.add("FAIL error "+t);stage.set(1);}
 }
 void tick(ClientTickEvent.Post e){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;
  if(ticks==40)mc.getSingleplayerServer().execute(()->{
   try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),Race.ASGARDIAN);RaceAttributes.applyRaceAttributes(p,Race.ASGARDIAN);RaceNetworkHandler.syncRace(p,Race.ASGARDIAN);
    p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();
    p.setPos(30,-60,100);p.setYRot(180);p.setXRot(0);
    runCase(p,List.of("bifrost:thors_hammer","bifrost:stormbreaker"),0);
   }catch(Throwable t){t.printStackTrace();results.add("FAIL "+t);}
   if(results.stream().anyMatch(r->r.startsWith("FAIL error")))stage.set(1);
  });
  if(stage.get()==1){visualStart=ticks;stage.set(2);}
  if(stage.get()==2){
   int t=ticks-visualStart;
   try{
    if(t==5){
     var gson=new com.google.gson.Gson();
     for(String[] pair:new String[][]{{"bifrost:stormbreaker","paladins:netherite_great_hammer"},{"bifrost:thors_hammer","paladins:netherite_mace"},{"bifrost:heimdalls_sword","paladins:diamond_claymore"}}){
      var own=punchy.client.render.ToolTuning.getBetterCombat(stack(pair[0]));var ref=punchy.client.render.ToolTuning.getBetterCombat(stack(pair[1]));
      check(own!=null&&!own.isEmpty()&&gson.toJson(own).equals(gson.toJson(ref)),pair[0]+" resolved Punchy combo equals RPG reference");
      results.add("PUNCHY "+pair[0]+" "+gson.toJson(own));
     }
     mc.setScreenAndShow(null);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
     mc.player.setYRot(180);mc.player.setXRot(0);
    }
    if(t==20)net.minecraft.client.Screenshot.grab(mc,false);
    if(t==25){
     var attack=net.bettercombat.logic.PlayerAttackHelper.getCurrentAttack(mc.player,0);
     check(attack!=null&&attack.attack().animation().equals("bettercombat:two_handed_slam"),"Stormbreaker native third-person attack is two-handed slam");
     punchy.client.state.AttackActionTracker.recordAttack();
    }
    if(t==29){
     var clip=punchy.client.animation.PunchyAnimationManager.POSE_HANDLER.getCurrentClip();
     String name="none";
     if(clip!=null){var f=clip.getClass().getDeclaredField("name");f.setAccessible(true);name=String.valueOf(f.get(clip));}
     results.add("OBSERVED attack clip "+name);check(name.equals("attack_mace"),"Punchy first-person plays the reference downward-smash clip");net.minecraft.client.Screenshot.grab(mc,false);
    }
    if(t==50)mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
    if(t==65)net.minecraft.client.Screenshot.grab(mc,false);
    if(t==75){mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);mc.setScreenAndShow(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
    if(t==90)net.minecraft.client.Screenshot.grab(mc,false);
    if(t>=100)finish();
   }catch(Throwable ex){ex.printStackTrace();results.add("FAIL visual "+ex);finish();}
  }
 }
 void finish(){var mc=Minecraft.getInstance();done=true;try{Files.write(mc.gameDirectory.toPath().resolve("combat-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("combat-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.size()+" checks\n");}catch(Exception ex){throw new RuntimeException(ex);}mc.stop();}
}
