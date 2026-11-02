package net.spell_engine.entity;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.effect.Effects;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.*;
import net.spell_power.api.SpellPower;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Phase-one additions only; existing spell IDs and unrelated engine policies remain authoritative. */
@EventBusSubscriber(modid="spell_engine")
public final class ClassRework {
 public static final String LOCK="skill_tree_rpgs:challenged",STRIDE="skill_tree_rpgs:assassin_stride";
 private static final Map<Player,Set<UUID>> TACKLED=new WeakHashMap<>();
 private static final Map<LivingEntity,Integer> HEALED=new WeakHashMap<>();
 private static final ThreadLocal<Integer> ATTACK=ThreadLocal.withInitial(()->0);
 public static void begin(){ATTACK.set(ATTACK.get()+1);}
 public static void end(){int n=ATTACK.get()-1;if(n==0)ATTACK.remove();else ATTACK.set(n);}
 public static boolean suppressSprint(LivingEntity e){return ATTACK.get()>0&&mobile(e);}
 static boolean has(LivingEntity e,String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).map(e::hasEffect).orElse(false);}
 static void put(ConfigFile.Effects c,String id,List<net.spell_engine.rpg_series.config.AttributeModifier> attrs){c.effects.put(id,new EffectConfig(attrs));}
 public static void skill(ConfigFile.Effects c){
  put(c,"skill_tree_rpgs:vitality",List.of(WarriorTierTwo.attr("minecraft:armor",2,Operation.ADD_VALUE),WarriorTierTwo.attr("minecraft:max_health",2,Operation.ADD_VALUE)));
  put(c,"skill_tree_rpgs:battle_shout",WarriorTierTwo.offense(.12f));
  put(c,WarriorTierTwo.MOMENTUM,List.of(WarriorTierTwo.attr("minecraft:attack_speed",.03f,Operation.ADD_MULTIPLIED_BASE),WarriorTierTwo.attr("minecraft:movement_speed",.03f,Operation.ADD_MULTIPLIED_BASE)));
  put(c,WarriorTierTwo.ENDURANCE,List.of(WarriorTierTwo.attr("minecraft:knockback_resistance",1,Operation.ADD_VALUE),WarriorTierTwo.attr("minecraft:armor",2,Operation.ADD_VALUE)));
  put(c,WarriorUltimates.CRIT,List.of(WarriorTierTwo.attr("critical_strike:chance",6,Operation.ADD_VALUE),WarriorTierTwo.attr("critical_strike:damage",6,Operation.ADD_VALUE)));
  put(c,WarriorUltimates.SENTINEL,List.of(WarriorTierTwo.attr("minecraft:attack_damage",.02f,Operation.ADD_MULTIPLIED_BASE)));
  put(c,"skill_tree_rpgs:enrage",List.of(WarriorTierTwo.attr("minecraft:attack_damage",.03f,Operation.ADD_MULTIPLIED_BASE),WarriorTierTwo.attr("minecraft:scale",.04f,Operation.ADD_MULTIPLIED_BASE)));
  // Last Stand contributes 2%; Juggernaut adds the remaining 2%, for 4% total per pulse.
  put(c,"skill_tree_rpgs:juggernaut",List.of(WarriorTierTwo.attr("minecraft:scale",.1f,Operation.ADD_MULTIPLIED_BASE),WarriorTierTwo.attr("spell_engine:damage_taken",-.02f,Operation.ADD_MULTIPLIED_BASE)));
  var key=Identifier.parse(LOCK);var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  if(entries.stream().noneMatch(e->e.id.equals(key))){var effect=new net.spell_engine.api.effect.CustomStatusEffect(MobEffectCategory.HARMFUL,0xB32727);net.spell_engine.api.effect.Synchronized.configure(effect,true);entries.add(new Effects.Entry(key,"Challenged","Cannot combat roll.",effect,new EffectConfig(List.of())));}put(c,LOCK,List.of());
 }
 public static void rogue(ConfigFile.Effects c){
  put(c,"rogues:demoralize",WarriorTierTwo.offense(-.2f));
  put(c,"rogues:charge",List.of(WarriorTierTwo.attr("minecraft:movement_speed",.5f,Operation.ADD_MULTIPLIED_BASE),WarriorTierTwo.attr("minecraft:knockback_resistance",.25f,Operation.ADD_VALUE),WarriorTierTwo.attr("minecraft:armor",4,Operation.ADD_VALUE)));
  put(c,"rogues:last_stand",List.of(WarriorTierTwo.attr("minecraft:knockback_resistance",1,Operation.ADD_VALUE),WarriorTierTwo.attr("minecraft:max_health",.2f,Operation.ADD_MULTIPLIED_BASE),WarriorTierTwo.attr("spell_engine:damage_taken",-.02f,Operation.ADD_MULTIPLIED_BASE)));
 }
 public static boolean mobile(LivingEntity e){
  if(!e.isAlive())return false;
  return has(e,"rogues:charge")||has(e,WarriorUltimates.BERSERKER)||e instanceof SpellContainerSource.Owner o&&o.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse(STRIDE)));
 }
 public static boolean rollBlocked(Player p){return has(p,LOCK);}
 public static boolean never(LivingEntity p,String ignored){return false;}
 public static float lethalityAmount(LivingEntity p){return 1+(int)Math.floor(Math.max(0,WarriorTierTwo.power(p))/10);}
 public static void lethalityHeal(Player p,float ignored){
  var h=WarriorUltimates.spell(p,"skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2");var cd=((SpellCaster.Player)p).getCooldownManager();if(cd.isCoolingDown(h))return;
  cd.set(h,Math.max(1,Math.round(SpellParameters.getCooldownDuration(p,h)*20)));p.heal(lethalityAmount(p));
 }
 public static SpellExecution.ImpactContext warmup(LivingEntity p,Holder<Spell> h,SpellExecution.ImpactContext ctx){
  return h.is(Identifier.parse("skill_tree_rpgs:warrior_tier_2_passive_1"))?ctx.power(new SpellPower.Result(h.value().school,p.getMaxHealth()*.05,0,1)):ctx;
 }
 public static Spell.Impact impact(Spell.Impact result,LivingEntity caster,Holder<Spell> h){
  if(h.is(Identifier.parse("rogues:throw"))&&result.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"minecraft:slowness".equals(result.action.status_effect.effect_id)){
   result=new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(result),Spell.Impact.class);
   result.action.status_effect.amplifier=WarriorRules.node(caster,"spell_1_modifier_1")?4:3;
   result.action.status_effect.duration=WarriorRules.node(caster,"spell_1_modifier_2")?5:4;
  }return result;
 }
 public static void onEffect(LivingEntity target,MobEffectInstance instance,Entity source,boolean applied){
  if(!applied||target.level().isClientSide())return;
  if(instance.getEffect().is(Identifier.parse("skill_tree_rpgs:vitality")))target.getAttribute(Attributes.ARMOR).removeModifier(Identifier.parse("warrior_pack:vitality_scaling"));
 }
 private static void killingHeal(LivingEntity caster){
  if(!(caster instanceof Player p)||!p.isAlive()||p.level().isClientSide()||SpellContainerSource.passiveSpellsOf(p).stream().noneMatch(h->h.is(Identifier.parse("skill_tree_rpgs:warrior_tier_1_passive_1"))))return;
  int now=p.tickCount;var h=PaladinRevamp.spell(p,"skill_tree_rpgs:warrior_tier_1_passive_1");int gap=Math.max(1,Math.round(SpellParameters.getCooldownDuration(p,h)*20));
  if(now>=HEALED.getOrDefault(p,Integer.MIN_VALUE)){p.heal(1);HEALED.put(p,now+gap);}
 }
 @SubscribeEvent public static void attacked(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event){
  if(event.getInflictedDamage()<=0&&event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;
  var source=event.getSource();if(source.getEntity() instanceof Player p&&source.getDirectEntity()==p&&source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)&&!((DamageSourceExtension)source).isSpellIndirect()&&PartyRules.harmfulAllowed(p,event.getEntity()))killingHeal(p);
 }
 public static boolean after(boolean ok,LivingEntity caster,Entity target,Holder<Spell> h,Spell.Impact impact){
  if(!ok)return ok;
  if(impact.action.type==Spell.Impact.Action.Type.DAMAGE&&h.value().type==Spell.Type.ACTIVE&&(h.value().school==net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE||h.value().school==net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE_DUAL)&&target instanceof LivingEntity&&PartyRules.harmfulAllowed(caster,target))killingHeal(caster);
  if(h.is(Identifier.parse("rogues:shout"))&&target instanceof LivingEntity enemy&&PartyRules.harmfulAllowed(caster,enemy)&&WarriorTierTwo.node(caster,"spell_2_modifier_2")&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:demoralize".equals(impact.action.status_effect.effect_id)){
   var debuff=enemy.getEffect(WarriorRules.effect("rogues:demoralize"));int ticks=debuff==null?120:debuff.getDuration();
   PartyRules.addEffect(enemy,new MobEffectInstance(WarriorRules.effect(LOCK),ticks),caster);caster.addEffect(new MobEffectInstance(MobEffects.GLOWING,ticks),caster);
  }
  if(h.is(Identifier.parse("rogues:last_stand"))&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:last_stand".equals(impact.action.status_effect.effect_id)){
   var effect=caster.getEffect(WarriorRules.effect("rogues:last_stand"));if(effect!=null&&effect.getAmplifier()>=4)caster.setHealth(caster.getMaxHealth());
  }
  return ok;
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  var p=event.getEntity();if(p.level().isClientSide())return;
  if(p.isAlive()&&has(p,"rogues:charge")){
   if(WarriorTierTwo.node(p,"spell_1_modifier_2")){
    var hit=TACKLED.computeIfAbsent(p,k->new HashSet<>());
    for(var e:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(1.5),e->e.isAlive()&&e.distanceToSqr(p)<=2.25&&PartyRules.harmfulAllowed(p,e)))if(hit.add(e.getUUID()))PartyRules.addEffect(e,new MobEffectInstance(WarriorRules.effect("spell_engine:stun"),40),p);
   }
  }else TACKLED.remove(p);
 }
 public static void chargeStarted(LivingEntity caster){if(caster instanceof Player p)TACKLED.remove(p);}
 public static Vec3 mutilateDestination(Player p,LivingEntity target){return target.position().add(Vec3.directionFromRotation(0,target.getYHeadRot()).scale((p.getBbWidth()+target.getBbWidth())*.5+.25));}
 public static boolean prepareMutilate(net.minecraft.world.level.Level world,Player p,Holder<Spell> h,SpellTarget.SearchResult targets,SpellCast.Action action){
  if(!h.is(Identifier.parse("rogues:mutilate"))||action!=SpellCast.Action.RELEASE)return true;
  if(world.isClientSide())return true;
  if(!(p instanceof ServerPlayer player)||targets.entities().isEmpty()||!(targets.entities().getFirst() instanceof LivingEntity target)||!target.isAlive()||target.level()!=world||p.distanceToSqr(target)>64||!PartyRules.harmfulAllowed(p,target)||!p.hasLineOfSight(target))return false;
  var dest=mutilateDestination(p,target);
  var delta=dest.subtract(p.position());var box=p.getBoundingBox().move(delta);if(!world.noCollision(p,box)||!world.getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(dest))||!world.getBlockState(net.minecraft.core.BlockPos.containing(dest.add(0,-.1,0))).isSolid())return false;
  // Existing enclosure permission must also reject teleporting through a hostile wall.
  var clipped=PurgatoryBoundary.clip(p,delta);if(clipped.subtract(delta).lengthSqr()>.0001)return false;
  var aim=target.position().subtract(dest);float yaw=(float)(Math.atan2(aim.z,aim.x)*180/Math.PI)-90;
  if(!player.teleportTo(player.level(),dest.x,dest.y,dest.z,Set.of(),yaw,0,true))return false;player.setYHeadRot(yaw);player.resetFallDistance();return true;
 }
 public static float meleeRange(float original,Player p,Identifier id){return Identifier.parse("rogues:mutilate").equals(id)?(float)p.entityInteractionRange():original;}
 public static boolean deliver(net.minecraft.world.level.Level world,LivingEntity caster,Holder<Spell> spell,SpellTarget.SearchResult targets,SpellExecution.ImpactContext context,java.util.function.Consumer<SpellExecution.DeliveryCompletion> completion){
  if(caster instanceof Player p&&!prepareMutilate(world,p,spell,targets,SpellCast.Action.RELEASE))return false;
  return PriestRules.deliver(world,caster,spell,targets,context,completion);
 }
}
