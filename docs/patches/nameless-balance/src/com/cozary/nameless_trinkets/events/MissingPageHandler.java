package com.cozary.nameless_trinkets.events;
import com.cozary.nameless_trinkets.items.trinkets.*;
import com.cozary.nameless_trinkets.pack.AccessoryCombat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MissingPageHandler {
 public static boolean isValidTarget(LivingEntity target){return target.isAlive() && !target.isInvulnerable();}
 public static void triggerDamageReflection(Player player,DamageSource source,Entity ignored){
  if(!MissingPageBase.INSTANCE.getTrinketConfig().isEnable || !(player.level() instanceof ServerLevel level)
   || AccessoryCombat.proc(source) || !AccessoryCombat.enemy(player,source.getEntity()) || !AccessoryCombat.ready(player,"missing_page",80))return;
  for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5),t->AccessoryCombat.enemy(player,t) && player.distanceToSqr(t)<=25)){
   level.sendParticles(ParticleTypes.SOUL,target.getX(),target.getY(),target.getZ(),12,.5,.5,.5,.05);
   AccessoryCombat.damage(player,target,AccessoryCombat.scaled(player,1,.1,6));
  }
 }
}
