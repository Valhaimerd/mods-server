package com.cozary.nameless_trinkets.events;
import com.cozary.nameless_trinkets.items.trinkets.*;
import com.cozary.nameless_trinkets.pack.AccessoryCombat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VampireBloodHandler {
 public static void function(Player player,LivingEntity target){
  if(!VampireBloodBase.INSTANCE.getTrinketConfig().isEnable || !(player.level() instanceof ServerLevel level)
   || AccessoryCombat.friendly(player,target) || !player.isAlive() || player.getHealth()>=player.getMaxHealth()
   || !AccessoryCombat.ready(player,"vampire_heal",40))return;
  level.sendParticles(ParticleTypes.SNEEZE,target.getX(),target.getY(),target.getZ(),12,.5,.5,.5,.05);
  player.heal(Math.min(4,2+player.getMaxHealth()*.05f));
 }
}
