package com.cozary.nameless_trinkets.events;
import com.cozary.nameless_trinkets.items.trinkets.*;
import com.cozary.nameless_trinkets.pack.AccessoryCombat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ReverseCardHandler {
 public static void reverseDamage(Player player,DamageSource source,float originalDamage){
  if(!ReverseCardBase.INSTANCE.getTrinketConfig().isEnable || !(player.level() instanceof ServerLevel level)
   || !(source.getEntity() instanceof LivingEntity target) || AccessoryCombat.proc(source) || !AccessoryCombat.enemy(player,target)
   || !(originalDamage>0) || player.getRandom().nextFloat()>=.25 || !AccessoryCombat.ready(player,"reverse_card",80))return;
  level.sendParticles(ParticleTypes.WITCH,target.getX(),target.getY(),target.getZ(),15,.5,.5,.5,.05);
  AccessoryCombat.damage(player,target,Math.min(originalDamage*.25f,AccessoryCombat.scaled(player,2,.1,6)));
 }
}
