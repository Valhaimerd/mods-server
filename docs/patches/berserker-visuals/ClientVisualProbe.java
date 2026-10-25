package local.berserkervisuals;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.spell_engine.api.effect.*;
import net.spell_engine.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="berserker_visual_probe",dist=Dist.CLIENT)
public class ClientVisualProbe {
 int ticks;boolean done,hidden,sparkSeen;List<String> results=Collections.synchronizedList(new ArrayList<>());
 public ClientVisualProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean value,String text){results.add((value?"PASS ":"FAIL ")+text);}
 net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect(){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(WarriorUltimates.BERSERKER)).orElseThrow();}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task){var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable t){results.add("FAIL server "+t);}});}
 void tick(ClientTickEvent.Post event){
  var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null||++ticks<40)return;
  try {
   if(ticks>45&&ticks<90&&!sparkSeen){
    var f=net.skill_tree_rpgs.client.effect.BerserkerSparkles.class.getDeclaredField("live");f.setAccessible(true);
    var tracked=(Map<?,?>)f.get(null);
    for(var list:tracked.values())for(var value:(List<?>)list){
     var particle=(net.minecraft.client.particle.Particle)value;
     check(particle.getLifetime()==4,"firework sparkle lifetime is four ticks");
     for(String channel:List.of("rCol","gCol","bCol")){var c=net.minecraft.client.particle.SingleQuadParticle.class.getDeclaredField(channel);c.setAccessible(true);check(c.getFloat(particle)==1,"white sparkle channel "+channel);}
     sparkSeen=true;Screenshot.grab(mc,false);break;
    }
   }
   if(ticks==40){
    mc.setScreenAndShow(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
    server(p->{p.removeAllEffects();p.getInventory().clearContent();p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"fill 195 -60 195 205 -54 205 air");p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"fill 195 -61 195 205 -61 205 grass_block");p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 200 -60 200 180 0");p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"time set noon");p.addEffect(new MobEffectInstance(effect(),280,4));});
   }
   if(ticks==90){
    check(sparkSeen,"actual firework sparks emitted on connected client");
    check(CustomParticleStatusEffect.spawnerOf(effect().value())!=null,"Berserker custom spark spawner registered");
    check(EntityTints.currentTint(mc.player)==BerserkerAppearance.RED_TINT,"connected client receives fixed 50% red tint");
    check(Synchronized.effectsOf(mc.player).stream().anyMatch(e->e.effect()==effect().value()),"Berserker visual state synchronized");
    for(int i=0;i<26;i++)check(BerserkerAppearance.sparkAt(i)==Set.of(0,4,8,13,17,21).contains(i),"three staggered sparks with five-tick pause age="+i);
    Screenshot.grab(mc,false);
   }
   if(ticks==110)server(WarriorUltimates::fatal);
   if(ticks==135){
    check(EntityTints.currentTint(mc.player)==EntityTints.NEUTRAL,"fatal rescue removes tint on connected client");
    check(Synchronized.effectsOf(mc.player).stream().noneMatch(e->e.effect()==effect().value()),"fatal rescue removes synchronized particle state");
    var f=net.skill_tree_rpgs.client.effect.BerserkerSparkles.class.getDeclaredField("live");f.setAccessible(true);check(((Map<?,?>)f.get(null)).isEmpty(),"fatal rescue removes all tracked sparks");
    Screenshot.grab(mc,false);server(p->{p.removeAllEffects();p.addEffect(new MobEffectInstance(effect(),12));});
   }
   if(ticks==175){check(EntityTints.currentTint(mc.player)==EntityTints.NEUTRAL,"natural expiry clears tint");check(Synchronized.effectsOf(mc.player).stream().noneMatch(e->e.effect()==effect().value()),"natural expiry clears spark state");}
   if(ticks==180){server(p->p.addEffect(new MobEffectInstance(effect(),100)));}
   if(ticks==200){check(EntityTints.currentTint(mc.player)==BerserkerAppearance.RED_TINT,"tint reapplies at one stack without stacking strength");server(WarriorUltimates::clearOnDeath);}
   if(ticks>=225){check(EntityTints.currentTint(mc.player)==EntityTints.NEUTRAL,"death cleanup clears tint");finish();}
  }catch(Throwable t){results.add("FAIL "+t);t.printStackTrace();finish();}
 }
 void finish(){done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("visual-results.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("visual-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
