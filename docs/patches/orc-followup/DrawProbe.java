package local.asgardprobe;
import java.util.function.Consumer;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.internal.RangedHasteEntity;
public class DrawProbe {
 static void eq(Consumer<String> out,String label,double a,double b){if(Math.abs(a-b)>.006)throw new AssertionError(label+" "+a+" != "+b);out.accept("PASS "+label+" actual="+a);}
 static void race(ServerPlayer p,Race r){PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),r);RaceAttributes.applyRaceAttributes(p,r);}
 public static void run(ServerPlayer p,Consumer<String> out){
  race(p,Race.HUMAN);var a=p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:haste")).orElseThrow());double base=a.getValue();
  race(p,Race.ORC);p.setHealth(p.getMaxHealth());RaceBalanceRules.apply(p,Race.ORC);eq(out,"Orc draw attribute ninety percent",a.getValue(),base*.9);
  for(var item:new Item[]{Items.BOW,Items.CROSSBOW}){
   var stack=new ItemStack(item);p.setItemInHand(InteractionHand.MAIN_HAND,stack);p.startUsingItem(InteractionHand.MAIN_HAND);var haste=(RangedHasteEntity)p;haste.resetPartialHasteTicks();
   for(int i=0;i<20;i++)NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Tick(p,stack,72000-i));
   double time=net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties.pullTimeTicks(stack,20)/20.0;
   eq(out,"Native draw slowdown "+item,haste.getPartialHasteTick(),20*time*(EntityAttributes_RangedWeapon.HASTE.asMultiplier(base*.9)-1));p.stopUsingItem();
  }
  var gear=Identifier.parse("probe:draw_gear");a.addTransientModifier(new AttributeModifier(gear,.5,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));eq(out,"Draw penalty combines with gear",a.getValue(),base*1.5*.9);race(p,Race.HUMAN);eq(out,"Draw penalty removed with race",a.getValue(),base*1.5);a.removeModifier(gear);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
 }
}
