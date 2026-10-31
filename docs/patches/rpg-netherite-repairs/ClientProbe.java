package local.netheriterepairclient;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.registry.PenchantComponents;
import archives.tater.penchant.util.PenchantmentHelper;

@Mod("netherite_client_probe")
public class ClientProbe {
 private int ticks;private boolean hidden;private String failure;private final List<String> checks=new ArrayList<>();
 public ClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 private void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks.add("PASS "+label);}
 private void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();
  if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());mc.options.pauseOnLostFocus=false;hidden=true;}
  if(mc.player==null||mc.getSingleplayerServer()==null)return;
  mc.mouseHandler.releaseMouse();ticks++;
  try{
   if(ticks==20)mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var enchant=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
    var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("paladins:netherite_prior_robe_legs")));stack.enchant(enchant,8);
    int required=EnchantmentProgress.getMaxProgress(enchant,8,stack);var progress=EnchantmentProgress.EMPTY.toMutable();progress.setProgress(enchant,required);stack.set(PenchantComponents.ENCHANTMENT_PROGRESS,progress.toImmutable());p.setItemSlot(EquipmentSlot.LEGS,stack);
   }catch(Throwable t){failure=t.toString();}});
   if(ticks==65){
    if(failure!=null)throw new AssertionError(failure);
    var enchant=mc.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);var stack=mc.player.getItemBySlot(EquipmentSlot.LEGS);
    check(PenchantmentHelper.getMaxLevel(enchant)==10,"connected client receives cap X");
    check(stack.getEnchantments().getLevel(enchant)==9,"real player worn full VIII progresses to IX and syncs");
    check(stack.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(Items.NETHERITE_SCRAP)),"client gear repair component accepts scraps");
   }
   if(ticks==75)mc.getSingleplayerServer().execute(()->{try{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();var enchant=p.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);var stack=p.getItemBySlot(EquipmentSlot.LEGS);
    var progress=EnchantmentProgress.getProgress(stack).toMutable();progress.setProgress(enchant,EnchantmentProgress.getMaxProgress(enchant,9,stack));stack.set(PenchantComponents.ENCHANTMENT_PROGRESS,progress.toImmutable());
   }catch(Throwable t){failure=t.toString();}});
   if(ticks==120){
    if(failure!=null)throw new AssertionError(failure);
    var enchant=mc.player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);var stack=mc.player.getItemBySlot(EquipmentSlot.LEGS);
    check(stack.getEnchantments().getLevel(enchant)==10,"real player worn full IX progresses to X and syncs");
    var line=archives.tater.penchant.PenchantClient.getProgressTooltip(EnchantmentProgress.getProgress(stack),enchant,10,stack).getString();
    check(line.contains("MAX"),"native Penchant tooltip reports MAX only at X");finish(mc);
   }
  }catch(Throwable t){failure=t.toString();t.printStackTrace();finish(mc);}
 }
 private void finish(Minecraft mc){try{Files.write(mc.gameDirectory.toPath().resolve("repair-client-checks.txt"),checks);Files.writeString(mc.gameDirectory.toPath().resolve("repair-client-result.txt"),failure==null?"PASS "+checks.size()+" connected client checks":"FAIL "+failure);}catch(Exception t){throw new RuntimeException(t);}mc.stop();}
}
