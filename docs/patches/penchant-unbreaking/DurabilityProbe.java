package local.unbreaking;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableFloat;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.util.PenchantmentHelper;
import archives.tater.penchant.component.EnchantmentProgress;
import archives.tater.penchant.enchantment.UnbreakableEffect;
import archives.tater.penchant.registry.PenchantEnchantments;
@Mod("unbreaking_probe")
public class DurabilityProbe {
 List<String> checks=new ArrayList<>();
 public DurabilityProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add("PASS "+text);}
 void run(ServerStartedEvent e){var server=e.getServer();try{
  PenchantmentDefinition.buildCache(server.registryAccess());
  boolean expectModule=Files.readString(Path.of("expect-module.txt")).trim().equals("true");boolean module=server.getPackRepository().getSelectedIds().stream().anyMatch(id->id.contains("durability_rework"));check(module==expectModule,"requested module state actually loaded: "+module);
  var level=server.overworld();level.getRandom().setSeed(123456789L);
  var enchantment=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
  check(PenchantmentHelper.getMaxLevel(enchantment)==10,"Penchant progression cap is X");check(enchantment.value().getMaxLevel()==10,"registered maximum is X");check(enchantment.value().getEffects(PenchantEnchantments.UNBREAKABLE).isEmpty(),"Unbreaking has no permanent-unbreakability effect");
  for(var item:List.of(Items.NETHERITE_PICKAXE,Items.NETHERITE_CHESTPLATE)){
   var stack=new ItemStack(item);stack.enchant(enchantment,10);
   check(!UnbreakableEffect.isUnbreakable(stack),"Unbreaking X is not Penchant-unbreakable: "+item);
   check(!stack.has(DataComponents.UNBREAKABLE),"no permanent item component added: "+item);
   var damage=new MutableFloat(11000f);enchantment.value().modifyDurabilityChange(level,10,stack,damage);check(damage.floatValue()>0&&damage.floatValue()<11000,"native X effect reduces damage without eliminating it: "+item+" -> "+damage.floatValue());
   stack.hurtAndBreak(256,level,(ServerPlayer)null,i->{});check(stack.getDamageValue()>0&&stack.getDamageValue()<256,"native X item actually loses reduced durability: "+item);
   stack.setDamageValue(stack.getMaxDamage()-1);for(int tries=0;tries<1024&&!stack.isEmpty();tries++)stack.hurtAndBreak(1,level,(ServerPlayer)null,i->{});check(stack.isEmpty(),"Unbreaking X item can break: "+item);
  }
  var growing=new ItemStack(Items.NETHERITE_PICKAXE);growing.enchant(enchantment,9);int target=EnchantmentProgress.getMaxProgress(enchantment,9,growing);check(target>0,"IX has a finite next-level progress target");EnchantmentProgress.addToProgress(growing,target,null);check(growing.getEnchantments().getLevel(enchantment)==10,"native progression advances IX to X");EnchantmentProgress.addToProgress(growing,10000000,null);check(growing.getEnchantments().getLevel(enchantment)==10,"native progression stops at X");
  var permanent=new ItemStack(Items.NETHERITE_PICKAXE);permanent.enchant(enchantment,10);permanent.set(DataComponents.UNBREAKABLE,Unit.INSTANCE);permanent.hurtAndBreak(256,level,(ServerPlayer)null,i->{});check(permanent.getDamageValue()==0,"explicitly unbreakable item component remains respected");
  Files.writeString(Path.of("unbreaking-result.txt"),"PASS "+checks.size()+" native Unbreaking checks");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("unbreaking-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{try{Files.write(Path.of("unbreaking-checks.txt"),checks);}catch(Exception ignored){}server.halt(false);}}
}
