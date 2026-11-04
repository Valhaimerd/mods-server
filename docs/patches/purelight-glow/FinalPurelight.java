package net.spell_engine.internals;
import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.*;import net.minecraft.world.damagesource.DamageSource;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.effect.MobEffectInstance;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.entity.player.PlayerEvent;import net.neoforged.neoforge.event.entity.living.*;import net.neoforged.neoforge.event.tick.EntityTickEvent;import net.spell_power.api.SpellSchools;
public final class FinalPurelight {
 static final String ACTIVE="final_purelight_active",NEXT="final_purelight_next",UNTIL="final_purelight_cooldown_until";static boolean initialized;
 public static boolean active(ServerPlayer p){return p.getPersistentData().getBooleanOr(ACTIVE,false);}
 public static boolean downed(ServerPlayer p){return net.neoforged.fml.ModList.get().isLoaded("playerrevive")&&team.creative.playerrevive.server.PlayerReviveServer.isBleeding(p)||net.neoforged.fml.ModList.get().isLoaded("better_revive")&&de.mrstilu2k6.better_revive.util.DownedPlayerManager.isDowned(p.getUUID());}
 public static void init(){if(initialized)return;initialized=true;NeoForge.EVENT_BUS.addListener(FinalPurelight::tick);
  NeoForge.EVENT_BUS.addListener((PlayerEvent.Clone e)->{copyCooldown((ServerPlayer)e.getOriginal(),(ServerPlayer)e.getEntity());if(e.isWasDeath())return;if(active((ServerPlayer)e.getOriginal())){e.getEntity().getPersistentData().putBoolean(ACTIVE,true);e.getEntity().getPersistentData().putLong(NEXT,e.getEntity().level().getGameTime()+10);}});
  NeoForge.EVENT_BUS.addListener((LivingDeathEvent e)->{if(e.getEntity() instanceof ServerPlayer p)((net.spell_engine.utils.WorldScheduler)p.level()).schedule(1,()->{if(!p.isAlive()||downed(p))stop(p);});});
 }
 public static boolean rescue(boolean otherSucceeded,LivingEntity e,DamageSource source){
  if(otherSucceeded||!(e instanceof ServerPlayer p)||p.level().getGameTime()<p.getPersistentData().getLongOr(UNTIL,0)||active(p)||downed(p)||!FinalRules.owns(p,"paladin_tier_3_passive_2")||!PaladinUltimates.ordinary(source))return otherSucceeded;
  var skill=PaladinRevamp.spell(p,"skill_tree_rpgs:paladin_tier_3_passive_2");int cooldown=Math.max(1,Math.round(SpellParameters.getCooldownDuration(p,skill)*20));p.getPersistentData().putLong(UNTIL,p.level().getGameTime()+cooldown);((net.spell_engine.internals.casting.SpellCaster.Player)p).getCooldownManager().set(skill,cooldown,true);p.getPersistentData().putBoolean(ACTIVE,true);p.getPersistentData().putLong(NEXT,p.level().getGameTime()+10);p.setHealth(p.getMaxHealth());p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),50,.6,.8,.6,.08);FinalRules.status(p,"purelight_cost",-1,0,p);return true;
 }
 public static void copyCooldown(ServerPlayer old,ServerPlayer fresh){long until=old.getPersistentData().getLongOr(UNTIL,0);if(until>fresh.level().getGameTime()){fresh.getPersistentData().putLong(UNTIL,until);((net.spell_engine.internals.casting.SpellCaster.Player)fresh).getCooldownManager().set(PaladinRevamp.spell(fresh,"skill_tree_rpgs:paladin_tier_3_passive_2"),(int)Math.min(Integer.MAX_VALUE,until-fresh.level().getGameTime()),true);}}
 public static void stop(ServerPlayer p){p.getPersistentData().remove(ACTIVE);p.getPersistentData().remove(NEXT);p.removeEffect(FinalRules.effect("purelight_cost"));}
 public static void refresh(ServerPlayer p){
  PaladinUltimates.modifier(p,Attributes.ARMOR,"reinforced_armor",0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);PaladinUltimates.modifier(p,SpellSchools.HEALING.attributeEntry,"reinforced_power",0,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE);p.removeEffect(PaladinUltimates.effect(PaladinUltimates.REINFORCED));
 }
 static void tick(EntityTickEvent.Post event){if(!(event.getEntity() instanceof ServerPlayer p)||!active(p))return;if(!p.isAlive()||downed(p)){stop(p);return;}
  if(!p.hasEffect(FinalRules.effect("purelight_cost")))FinalRules.status(p,"purelight_cost",-1,0,p);
  long now=p.level().getGameTime();if(!p.getPersistentData().contains(NEXT))p.getPersistentData().putLong(NEXT,now+10);if(now<p.getPersistentData().getLongOr(NEXT,now+10))return;p.getPersistentData().putLong(NEXT,now+10);
  if(p.getHealth()>1)p.setHealth(p.getHealth()-1);else{p.setHealth(0);p.die(p.damageSources().generic());if(!p.isAlive()||downed(p))stop(p);}
 }
}
