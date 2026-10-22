package local.crossarms.clientprobe;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
/** Isolated integrated client: early swing resets and repeated real server-shot recoil. */
@Mod("recoil_probe") public class ClientRecoilProbe {
 int ticks,resets,lastSwing;boolean hidden,failed;AtomicInteger shots=new AtomicInteger();
 void result(String s){if(s.startsWith("FAIL"))failed=true;if(failed&&s.startsWith("PASS"))return;try{Files.writeString(Minecraft.getInstance().gameDirectory.toPath().resolve("recoil-result.txt"),s);}catch(Exception e){throw new RuntimeException(e);}}
 public ClientRecoilProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();
  if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());hidden=true;}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;ticks++;
  try {
   if(ticks==40){mc.setScreenAndShow(null);var server=mc.getSingleplayerServer();server.execute(()->{try{
    var p=server.getPlayerList().getPlayer(mc.player.getUUID());p.getInventory().clearContent();p.getAbilities().instabuild=true;
    p.getInventory().setItem(1,new ItemStack(Items.ARROW,64));
    var frenzy=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy")).orElseThrow();
    for(var hand:InteractionHand.values()){var b=new ItemStack(Items.CROSSBOW);b.enchant(frenzy,1);p.setItemInHand(hand,b);}
    for(var key:new String[]{"frenzy_shots","frenzy_empty","frenzy_idle","frenzy_notice","HS_delay"})server.getCommands().performPrefixedCommand(p.createCommandSourceStack().withSuppressedOutput(),"scoreboard players set @s "+key+" 0");
    p.containerMenu.broadcastChanges();
   }catch(Throwable t){result("FAIL server setup "+t);}});}
   if(ticks==64){
    String rhythm=net.minecraft.client.resources.language.I18n.get("spell.skill_tree_rpgs.archer_tier_1_passive_2.description");
    String roots=net.minecraft.client.resources.language.I18n.get("spell.archers.entangling_roots.description");
    if(!rhythm.contains("3%")||rhythm.contains("Format error")||!roots.contains("60%")||roots.contains("Format error"))throw new AssertionError("localized descriptions "+rhythm+" / "+roots);
   }
   if(ticks==65)for(var hand:InteractionHand.values()){
    mc.player.swinging=true;mc.player.swingTime=1;mc.player.swing(hand,false);
    if(mc.player.swingTime!=-1||mc.player.swingingArm!=hand)throw new AssertionError("client first-half swing did not restart "+hand);
   }
   if(ticks>=80&&ticks<=108&&(ticks-80)%4==0){var server=mc.getSingleplayerServer();server.execute(()->{try{
    var p=server.getPlayerList().getPlayer(mc.player.getUUID());for(int i=0;i<8;i++)EnchantmentHelper.tickEffects(p.level(),p);
    var b=p.getMainHandItem();if(!CrossbowItem.isCharged(b))throw new AssertionError("shot not loaded");
    ((CrossbowItem)b.getItem()).use(p.level(),p,InteractionHand.MAIN_HAND);shots.incrementAndGet();p.containerMenu.broadcastChanges();
   }catch(Throwable t){result("FAIL actual shot "+t);}});}
   if(ticks>=80&&ticks<=120){int current=mc.player.swingTime;if(current<lastSwing&&current<=1)resets++;lastSwing=current;}
   if(ticks==112)Screenshot.grab(mc,false);
   if(ticks==125){if(shots.get()!=8||resets<7)throw new AssertionError("shots="+shots+" visible recoil resets="+resets);result("PASS client main/off-hand early swing restart; "+shots+" real native server shots; "+resets+" client recoil restarts");}
  }catch(Throwable t){result("FAIL "+t);}
  if(ticks==135)mc.stop();
 }
}
