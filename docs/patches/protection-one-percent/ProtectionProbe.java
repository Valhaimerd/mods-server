package local.paladinprobe;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.*;import net.minecraft.core.registries.*;import net.minecraft.resources.Identifier;import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.*;import net.minecraft.world.entity.*;import net.minecraft.world.damagesource.*;
@Mod("paladin_doctrine_probe")public class ProtectionProbe{
 List<String> checks=new ArrayList<>();int failures;
 void near(double a,double b,String label){boolean ok=Math.abs(a-b)<.0001;checks.add((ok?"PASS ":"FAIL ")+label+" actual="+a+" expected="+b);if(!ok)failures++;}
 public ProtectionProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent event){var server=event.getServer();try{var w=server.overworld();var p=FakePlayerFactory.getMinecraft(w);p.getInventory().clearContent();p.removeAllEffects();var registry=w.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
 String[] names={"minecraft:protection","minecraft:projectile_protection","minecraft:fire_protection","minecraft:blast_protection","spell_power:magic_protection"};
 DamageSource[] sources={w.damageSources().generic(),w.damageSources().arrow(EntityTypes.ARROW.create(w,EntitySpawnReason.COMMAND),p),w.damageSources().inFire(),w.damageSources().explosion(null,p),w.damageSources().fallingBlock(EntityTypes.FALLING_BLOCK.create(w,EntitySpawnReason.COMMAND))};
 for(int e=0;e<names.length;e++)for(int level=1;level<=4;level++){
  for(var slot:EquipmentSlot.VALUES)if(slot.isArmor())p.setItemSlot(slot,ItemStack.EMPTY);
  var h=registry.get(Identifier.parse(names[e])).orElseThrow();var chest=new ItemStack(Items.DIAMOND_CHESTPLATE);chest.enchant(h,level);p.setItemSlot(EquipmentSlot.CHEST,chest);
  float points=EnchantmentHelper.getDamageProtection(w,p,sources[e]);near(points,.25*level,names[e]+" protection points at "+level);near(CombatRules.getDamageAfterMagicAbsorb(100,points),100-level,names[e]+" actual layer damage at "+level);
 }
 for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){var item=new ItemStack(switch(slot){case HEAD->Items.DIAMOND_HELMET;case CHEST->Items.DIAMOND_CHESTPLATE;case LEGS->Items.DIAMOND_LEGGINGS;default->Items.DIAMOND_BOOTS;});for(String name:names)item.enchant(registry.get(Identifier.parse(name)).orElseThrow(),4);p.setItemSlot(slot,item);near(item.getEnchantments().size(),5,"all five retained on "+slot);}
 near(CombatRules.getDamageAfterMagicAbsorb(100,EnchantmentHelper.getDamageProtection(w,p,sources[0])),84,"full combined set generic damage16percent reduction");
 for(int e=1;e<sources.length;e++)near(CombatRules.getDamageAfterMagicAbsorb(100,EnchantmentHelper.getDamageProtection(w,p,sources[e])),68,"full combined set matching damage32percent "+names[e]);
 }catch(Throwable t){failures++;checks.add("ERROR "+t);t.printStackTrace();}finally{try{Files.write(Path.of("paladin-checks.txt"),checks);Files.writeString(Path.of("paladin-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures);}catch(Exception e){throw new RuntimeException(e);}server.halt(false);}}
}
