package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.food.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FoodProperties.class)
public class FoodMixin {
 // Leave the original call available to Alex's Caves' raw-meat armor redirect.
 @Inject(method="onConsume",at=@At("RETURN"))
 private void eat(Level level,LivingEntity entity,ItemStack stack,Consumable consumable,CallbackInfo ci){
  if(!(entity instanceof ServerPlayer p)||Utilities.stat(p,"food")==0)return;
  FoodData data=p.getFoodData();FoodProperties food=(FoodProperties)(Object)this;
  double bonus=Utilities.stat(p,"food");
  double carry=p.getPersistentData().getDoubleOr(Utilities.PREFIX+"food_fraction",0)+food.nutrition()*bonus;
  int extra=(int)Math.floor(carry+1e-9);
  data.eat(new FoodProperties(extra,(float)(food.saturation()*bonus),food.canAlwaysEat()));
  p.getPersistentData().putDouble(Utilities.PREFIX+"food_fraction",data.getFoodLevel()>=20?0:carry-extra);
 }
}
