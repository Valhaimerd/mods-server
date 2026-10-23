package local.beartrap;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.casting.*;
import net.spell_engine.client.animation.AnimatablePlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Calls the exact client animation method named by the crash report. Never shipped. */
@Mod("bear_cast_probe") public final class ClientCastProbe {
 boolean hidden,done;int wait;
 public ClientCastProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();
  if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(done||mc.player==null||mc.level==null)return;if(++wait<20)return;done=true;String result;
  try{
   mc.setScreenAndShow(null);var spell=SpellRegistry.from(mc.level).get(Identifier.parse("rogues:bear_trap")).orElseThrow();
   var caster=(SpellCaster.Client)mc.player;var controller=caster.getCastController();var predicted=controller.getClass().getDeclaredField("predictedProcess");predicted.setAccessible(true);int updates=0;
   for(float speed:new float[]{1,2})for(int cast=0;cast<20;cast++){
    predicted.set(controller,new SpellCast.Process(mc.player,spell,Items.AIR,speed,Math.round(4/speed),mc.level.getGameTime()));
    if(caster.getCastedSpell()!=spell.value())throw new AssertionError("Client did not enter Bear Trap casting state");
    for(int tick=0;tick<Math.round(4/speed);tick++){((AnimatablePlayer)mc.player).updateSpellCastAnimationsOnTick();updates++;}
    predicted.set(controller,null);((AnimatablePlayer)mc.player).updateSpellCastAnimationsOnTick();
   }
   if(spell.value().active.cast.animation==null)throw new AssertionError("Animation missing");
   if(Math.abs(spell.value().active.cast.duration-.2f)>.001||spell.value().cost.cooldown.duration!=4)throw new AssertionError("Gameplay values changed");
   result="PASS actual NeoForge client: 40 Bear Trap cast/stop cycles, "+updates+" animation updates at normal/double speed; 0.2s cast and 4s cooldown retained";
  }catch(Throwable error){result="FAIL "+error;error.printStackTrace();}
  try{Files.writeString(mc.gameDirectory.toPath().resolve("bear-cast-result.txt"),result);}catch(Exception error){throw new RuntimeException(error);}mc.stop();
 }
}
