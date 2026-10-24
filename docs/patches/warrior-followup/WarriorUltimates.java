package net.spell_engine.entity;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;

/** Ultimates keep their saved IDs. Basic attack damage never changes spell power. */
public final class WarriorUltimates {
 public static final String BERSERKER="skill_tree_rpgs:berserker",CRIT="skill_tree_rpgs:berserker_crit",SENTINEL="skill_tree_rpgs:sentinel",WARD="skill_tree_rpgs:tenacity_ward";
 public static final int TENACITY_TICKS=240;
 private record Channel(String id,long started,int pulses){}
 private record Rage(int expires,int nextDrain){}
 private static final Map<Player,Channel> CHANNELS=new WeakHashMap<>();
 private static final Map<LivingEntity,Rage> RAGES=new WeakHashMap<>();
 private static final Set<LivingEntity> ENDING=Collections.newSetFromMap(new WeakHashMap<>());
 private static final ThreadLocal<ArrayDeque<Player>> BASICS=ThreadLocal.withInitial(ArrayDeque::new);
 private static boolean listening;
 static boolean id(Holder<Spell> h,String key){return WarriorRules.id(h,key);}
 static Holder<MobEffect> effect(String key){return WarriorRules.effect(key);}
 static boolean has(LivingEntity e,String key){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(key)).map(e::hasEffect).orElse(false);}
 static boolean node(LivingEntity p,String suffix){return ArcherProjectileRules.node(p,"warrior_tier_4_"+suffix);}
 static boolean passive(Player p,String suffix){return SpellContainerSource.passiveSpellsOf(p).stream().anyMatch(h->id(h,"skill_tree_rpgs:warrior_tier_3_passive_"+suffix));}
 static Holder<Spell> spell(Player p,String key){return SpellRegistry.from(p.level()).get(Identifier.parse(key)).orElseThrow();}
 static net.spell_engine.rpg_series.config.AttributeModifier attr(String key,float value,Operation op){return new net.spell_engine.rpg_series.config.AttributeModifier(key,value,op);}
 public static void skill(net.spell_engine.rpg_series.config.ConfigFile.Effects config){
  WarriorTierTwo.entry(BERSERKER,"Berserker",List.of(attr("minecraft:attack_speed",.15f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:knockback_resistance",.15f,Operation.ADD_VALUE),attr("spell_engine:damage_taken",.2f,Operation.ADD_MULTIPLIED_BASE)),config);
  WarriorTierTwo.entry(CRIT,"Brutality",List.of(attr("critical_strike:chance",.04f,Operation.ADD_MULTIPLIED_BASE)),config);
  WarriorTierTwo.entry(SENTINEL,"Sentinel",List.of(attr("minecraft:attack_damage",.01f,Operation.ADD_MULTIPLIED_BASE)),config);
  WarriorTierTwo.entry(WarriorTierTwo.MOMENTUM,"Momentum",List.of(attr("minecraft:attack_speed",.02f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:movement_speed",.02f,Operation.ADD_MULTIPLIED_BASE)),config);
  config.effects.put("skill_tree_rpgs:enrage",new net.spell_engine.rpg_series.config.EffectConfig(List.of(attr("minecraft:attack_damage",.04f,Operation.ADD_MULTIPLIED_BASE),attr("minecraft:scale",.08f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:juggernaut",new net.spell_engine.rpg_series.config.EffectConfig(List.of(attr("minecraft:scale",.1f,Operation.ADD_MULTIPLIED_BASE),attr("spell_engine:damage_taken",-.03f,Operation.ADD_MULTIPLIED_BASE))));
  var key=Identifier.parse(WARD);var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
  if(entries.stream().noneMatch(e->e.id.equals(key))){
   var ward=new TenacityAbsorbEffect();net.spell_engine.api.effect.Synchronized.configure(ward,true);
   entries.add(new Effects.Entry(key,"Tenacity","Absorbs incoming damage.",ward));
  }
  config.effects.put(WARD,new net.spell_engine.rpg_series.config.EffectConfig(List.of(attr("minecraft:max_absorption",1,Operation.ADD_VALUE))));
  OnRemoval.configure(entries.stream().filter(e->e.id.toString().equals(BERSERKER)).findFirst().orElseThrow().effect,c->removed(c.entity()));
  if(listening)return;listening=true;
  NeoForge.EVENT_BUS.addListener((LivingIncomingDamageEvent event)->{
   if(event.getSource().getEntity() instanceof Player p&&!BASICS.get().isEmpty()&&BASICS.get().peek()==p&&event.getSource().getDirectEntity()==p&&event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)&&has(p,BERSERKER)){
    int stacks=p.getEffect(effect(BERSERKER)).getAmplifier()+1;event.setAmount(event.getAmount()*(1+.1f*stacks));
   }
  });
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDamageEvent.Pre event)->{
   var target=event.getEntity();if(has(target,BERSERKER)&&event.getNewDamage()-target.getAbsorptionAmount()>=target.getHealth()&&event.getNewDamage()>0){event.setNewDamage(0);fatal(target);}
  });
  NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,(LivingDeathEvent event)->{if(has(event.getEntity(),BERSERKER)){event.setCanceled(true);fatal(event.getEntity());}});
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(LivingDeathEvent event)->clearOnDeath(event.getEntity()));
  NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event)->{if(event.isWasDeath()){clearOnDeath(event.getOriginal());clearOnDeath(event.getEntity());}});
  NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event)->{
   if(event.getEntity() instanceof Player p&&!p.level().isClientSide())tick(p);
  });
 }
 public static void rogue(net.spell_engine.rpg_series.config.ConfigFile.Effects config){
  config.effects.put("rogues:last_stand",new net.spell_engine.rpg_series.config.EffectConfig(List.of(attr("minecraft:knockback_resistance",1,Operation.ADD_VALUE),attr("minecraft:max_health",.2f,Operation.ADD_MULTIPLIED_BASE))));
 }
 public static void enter(Player player){BASICS.get().push(player);}
 public static void leave(){var stack=BASICS.get();stack.pop();if(stack.isEmpty())BASICS.remove();}
 static boolean ultimate(Holder<Spell> h){return id(h,"rogues:mortal_strike")||id(h,"rogues:last_stand");}
 public static void changeProcess(Player player,SpellCast.Process old,SpellCast.Process next){
  if(player.level().isClientSide())return;
  if(next!=null&&ultimate(next.spell())){
   String id=next.id().toString();var current=CHANNELS.get(player);
   if(old==null||current==null||!current.id.equals(id)||current.started!=next.startedAt())CHANNELS.put(player,new Channel(id,next.startedAt(),0));
  }
  if(old!=null&&next==null&&ultimate(old.spell())){
   var state=CHANNELS.get(player);if(state!=null&&state.pulses>0)cooldown(player,old.spell());
  }
 }
 public static float duration(float original,LivingEntity caster,Holder<Spell> h){
  if(!ultimate(h)||!(caster instanceof Player p))return original;
  var c=CHANNELS.get(p);int pulses=c!=null&&c.id.equals(h.unwrapKey().orElseThrow().identifier().toString())?c.pulses:5;
  return 60+10*Math.max(0,Math.min(5,pulses)-1);
 }
 public static void cooldown(Player p,Holder<Spell> h){((SpellCaster.Player)p).getCooldownManager().set(h,Math.round(SpellParameters.getCooldownDuration(p,h)*20));}
 public static boolean interrupt(Player p,SpellCast.Process process){
  if(process==null||!ultimate(process.spell())||p.level().isClientSide())return false;
  boolean hard=p.getActiveEffects().stream().anyMatch(e->{var a=((ActionImpairing)e.getEffect().value()).actionsAllowed();return a!=null&&!a.players().canCastSpell();});
  if(!hard)return false;
  var interactor=((SpellCaster.Player)p).getInteractor();interactor.requestClear();return true;
 }
 public static void pulse(LivingEntity caster,Holder<Spell> h,Spell.Impact impact,boolean success){
  if(!success||!ultimate(h)||impact.action.type!=Spell.Impact.Action.Type.STATUS_EFFECT)return;
  String effect=impact.action.status_effect.effect_id;
  if(!(BERSERKER.equals(effect)||"rogues:last_stand".equals(effect)))return;
  if(caster instanceof Player p){
   String key=h.unwrapKey().orElseThrow().identifier().toString();var c=CHANNELS.get(p);
   if(c==null||!c.id.equals(key))c=new Channel(key,p.tickCount,0);
   CHANNELS.put(p,new Channel(key,c.started,Math.min(5,c.pulses+1)));
  }
  if(BERSERKER.equals(effect)){
   var active=caster.getEffect(effect(BERSERKER));var old=RAGES.get(caster);
   RAGES.put(caster,new Rage(caster.tickCount+active.getDuration(),old==null?caster.tickCount+10:old.nextDrain));
   if(active.getAmplifier()==4){caster.setHealth(caster.getMaxHealth());caster.addEffect(new MobEffectInstance(MobEffects.SPEED,40,3));}
  }
 }
 static void removed(LivingEntity target){
  if(ENDING.contains(target))return;var state=RAGES.remove(target);
  target.removeEffect(effect(CRIT));if(state!=null&&target.tickCount>=state.expires-1&&target.isAlive()){target.setHealth(target.getMaxHealth());endSound(target);}
 }
 public static void endSound(LivingEntity target){
  if(!target.level().isClientSide())target.level().playSound(null,target.getX(),target.getY(),target.getZ(),net.minecraft.sounds.SoundEvent.createVariableRangeEvent(Identifier.parse("skill_tree_rpgs:rock_spike_impact")),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
 }
 public static void clearOnDeath(LivingEntity target){
  ENDING.add(target);
  try{
   boolean active=has(target,BERSERKER)||has(target,"rogues:last_stand");
   for(String key:List.of(BERSERKER,CRIT,"rogues:last_stand","skill_tree_rpgs:juggernaut",SENTINEL))target.removeEffect(effect(key));
   if(active){target.removeEffect(MobEffects.REGENERATION);target.removeEffect(MobEffects.SPEED);((net.spell_engine.api.entity.LivingEntityImmunity.Owner)target).getImmunities().clear();}
   RAGES.remove(target);if(target instanceof Player p)CHANNELS.remove(p);
  }finally{ENDING.remove(target);}
 }
 public static void fatal(LivingEntity target){
  if(ENDING.contains(target))return;ENDING.add(target);
  try{
   target.removeEffect(effect(BERSERKER));target.removeEffect(effect(CRIT));RAGES.remove(target);target.setHealth(target.getMaxHealth());endSound(target);
   if(target instanceof Player p){
    ((SpellCaster.Player)p).getInteractor().requestClear();
    for(String id:List.of("rogues:throw","rogues:throw_net","rogues:charge","rogues:shout"))cooldown(p,spell(p,id));
   }
   target.removeEffect(effect(WarriorTierTwo.FREEDOM));
   WarriorRules.addEffect(target,new MobEffectInstance(MobEffects.NAUSEA,200),null);
   WarriorRules.addEffect(target,new MobEffectInstance(effect("spell_engine:stun"),100),null);
  }finally{ENDING.remove(target);}
 }
 public static void melee(Player p,Entity target){
  if(target instanceof LivingEntity&&PartyRules.harmfulAllowed(p,target)&&has(p,BERSERKER)&&node(p,"spell_1_modifier_2"))p.heal(2);
 }
 public static int bands(float health,float max){
  double ratio=health/max;int n=0;for(double threshold:new double[]{.7,.6,.5,.4,.3,.2})if(ratio<=threshold+.000001)n++;return n;
 }
 static void enrage(Player p){
  int bands=passive(p,"1")?bands(p.getHealth(),p.getMaxHealth()):0;var key=effect("skill_tree_rpgs:enrage");var current=p.getEffect(key);
  if(bands==0){if(current!=null)p.removeEffect(key);return;}
  if(current!=null&&current.getAmplifier()!=bands-1)p.removeEffect(key);
  p.addEffect(new MobEffectInstance(key,60,bands-1));
 }
 public static void tenacity(Player p){
  if(!passive(p,"2"))return;double ratio=(double)p.getHealth()/p.getMaxHealth();var h=spell(p,"skill_tree_rpgs:warrior_tier_3_passive_2");
  if(ratio<.01-.000001||ratio>.3+.000001||((SpellCaster.Player)p).getCooldownManager().isCoolingDown(h))return;
  int amount=6+(int)Math.round(.3*WarriorTierTwo.power(p));p.addEffect(new MobEffectInstance(effect(WARD),TENACITY_TICKS,Math.max(0,amount-1)));cooldown(p,h);
  var targets=SpellTarget.findTargets(p,h,SpellTarget.SearchResult.empty(),false);
  var context=new SpellExecution.ImpactContext(1,1,p.position(),net.spell_power.api.SpellPower.getSpellPower(h.value().school,p),SpellTarget.FocusMode.AREA,0);
  for(var target:targets.entities())SpellImpacts.performImpacts(p.level(),p,target,p,h,h.value().impacts,context,false,null);
 }
 public static void tick(Player p){
  if(!p.isAlive()){clearOnDeath(p);return;}
  if(has(p,BERSERKER)){
   var e=p.getEffect(effect(BERSERKER));var state=RAGES.computeIfAbsent(p,k->new Rage(p.tickCount+e.getDuration(),p.tickCount+10));
   if(node(p,"spell_1_modifier_2")&&p.tickCount>=state.nextDrain){
    RAGES.put(p,new Rage(state.expires,p.tickCount+10));
    if(p.getHealth()<=1)fatal(p);else p.setHealth(p.getHealth()-1);
   }
  }
  if(p.tickCount%5==0){enrage(p);tenacity(p);}
 }
 public static boolean directSentinel(net.minecraft.world.damagesource.DamageSource source){return WarriorRules.direct(source)&&!((DamageSourceExtension)source).isSpellIndirect();}
}
