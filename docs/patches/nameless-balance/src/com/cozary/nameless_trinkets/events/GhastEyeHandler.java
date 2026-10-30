package com.cozary.nameless_trinkets.events;
import com.cozary.nameless_trinkets.items.trinkets.GhastEyeBase;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
public class GhastEyeHandler {
 public static void obtainRegenOnKill(Player player){
  if(!GhastEyeBase.INSTANCE.getTrinketConfig().isEnable || player.level().isClientSide() || player.isSpectator())return;
  // Native merging preserves a stronger or longer unrelated regeneration effect.
  player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));
 }
}
