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
@Mixin(FoodProperties.class)
public class FoodMixin {
 @Redirect(method="onConsume",at=@At(value="INVOKE",target="Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
 private void eat(FoodData data,FoodProperties food,Level level,LivingEntity entity,ItemStack stack,Consumable consumable){
  if(!(entity instanceof ServerPlayer p)||Utilities.stat(p,"food")==0){data.eat(food);return;}
  double bonus=Utilities.stat(p,"food");int before=data.getFoodLevel();
  double carry=p.getPersistentData().getDoubleOr(Utilities.PREFIX+"food_fraction",0)+food.nutrition()*bonus;
  int extra=(int)Math.floor(carry+1e-9);
  data.eat(new FoodProperties(food.nutrition()+extra,(float)(food.saturation()*(1+bonus)),food.canAlwaysEat()));
  p.getPersistentData().putDouble(Utilities.PREFIX+"food_fraction",before+food.nutrition()+extra>=20?0:carry-extra);
 }
}
