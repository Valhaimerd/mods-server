package net.spell_engine.internals;
import java.util.*;
import net.minecraft.world.entity.*;import net.minecraft.world.effect.*;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.tick.EntityTickEvent;
public final class FrostExposure {
 private static final Map<LivingEntity,Cold> ACTIVE=new WeakHashMap<>();private static final Map<LivingEntity,Long> BLIZZARD=new WeakHashMap<>();private static boolean installed;
 private static final class Cold {long until;int external;LivingEntity owner;Cold(long until,int external,LivingEntity owner){this.until=until;this.external=external;this.owner=owner;}}
 public static void init(){if(installed)return;installed=true;NeoForge.EVENT_BUS.addListener(FrostExposure::before);NeoForge.EVENT_BUS.addListener(FrostExposure::after);}
 public static void apply(LivingEntity target,LivingEntity caster,int ticks){
  if(target.level().isClientSide()||!target.canFreeze()||!PartyRules.harmfulAllowed(caster,target))return;
  LivingEntity owner=caster instanceof net.spell_engine.entity.SummonedEntity summon&&summon.getOwner() instanceof LivingEntity living?living:caster;
  long until=target.level().getGameTime()+ticks;Cold old=ACTIVE.get(target);if(old==null){old=new Cold(until,target.getTicksFrozen(),owner);ACTIVE.put(target,old);}else{old.until=Math.max(old.until,until);old.owner=owner;}
  int left=(int)(old.until-target.level().getGameTime());target.addEffect(new MobEffectInstance(MageFollowup.effect(MageFollowup.COLD),left),caster);target.setTicksFrozen(Math.max(target.getTicksFrozen(),target.getTicksRequiredToFreeze()+2));
 }
 private static void release(LivingEntity target,Cold cold){ACTIVE.remove(target);target.setTicksFrozen(Math.min(target.getTicksFrozen(),cold.external));target.removeEffect(MageFollowup.effect(MageFollowup.COLD));}
 private static void before(EntityTickEvent.Pre event){if(!(event.getEntity() instanceof LivingEntity target)||target.level().isClientSide())return;Cold cold=ACTIVE.get(target);if(cold==null)return;
  if(!target.isAlive()||target.level().getGameTime()>=cold.until||!target.canFreeze()||!target.hasEffect(MageFollowup.effect(MageFollowup.COLD))||!PartyRules.harmfulAllowed(cold.owner,target)){release(target,cold);return;}
  cold.external=target.isInPowderSnow?Math.min(target.getTicksRequiredToFreeze(),cold.external+1):Math.max(0,cold.external-2);
  target.setTicksFrozen(Math.max(target.getTicksFrozen(),target.getTicksRequiredToFreeze()+2));
 }
 private static void after(EntityTickEvent.Post event){if(event.getEntity() instanceof LivingEntity target&&ACTIVE.containsKey(target)&&target.getTicksFrozen()>target.getTicksRequiredToFreeze()+2)ACTIVE.get(target).external=Math.max(ACTIVE.get(target).external,target.getTicksFrozen());}
 public static void blizzard(LivingEntity target,LivingEntity caster){long now=target.level().getGameTime();if(now<BLIZZARD.getOrDefault(target,Long.MIN_VALUE))return;BLIZZARD.put(target,now+25);if(PartyRules.addEffect(target,new MobEffectInstance(MageFollowup.effect("wizards:frozen"),5),caster))apply(target,caster,200);}
 public static long remaining(LivingEntity target){Cold cold=ACTIVE.get(target);return cold==null?0:Math.max(0,cold.until-target.level().getGameTime());}
}
