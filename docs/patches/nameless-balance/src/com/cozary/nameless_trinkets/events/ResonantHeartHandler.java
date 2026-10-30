package com.cozary.nameless_trinkets.events;
import com.cozary.nameless_trinkets.items.trinkets.*;
import com.cozary.nameless_trinkets.pack.AccessoryCombat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
public class ResonantHeartHandler {
 public static void heartSonicBoom(Player player,DamageSource source){
  if(!ResonantHeartBase.INSTANCE.getTrinketConfig().isEnable || player.level().isClientSide() || AccessoryCombat.proc(source)
   || !AccessoryCombat.enemy(player,source.getEntity()) || player.getRandom().nextFloat()>=.1
   || !AccessoryCombat.ready(player,"resonant_heart",120))return;
  launchSonicBoom(player,source.getEntity(),player.level());
 }
 public static void launchSonicBoom(Player player,Entity src,Level world){
  if(!(world instanceof ServerLevel level) || !(src instanceof LivingEntity target) || !AccessoryCombat.enemy(player,target))return;
  var origin=player.getEyePosition();var delta=target.getBoundingBox().getCenter().subtract(origin);var direction=delta.normalize();
  for(int i=1;i<=Math.min(32,Math.ceil(delta.length()));i++){
   var pos=origin.add(direction.scale(i));level.sendParticles(ParticleTypes.SONIC_BOOM,pos.x,pos.y,pos.z,1,0,0,0,0);
  }
  level.playSound(null,player.blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,player.getSoundSource(),1,1);
  if(AccessoryCombat.damage(player,target,AccessoryCombat.scaled(player,2,.15,8)))target.push(direction.x*.4*(1-target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)),.1,direction.z*.4*(1-target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)));
 }
}
