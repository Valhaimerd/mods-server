package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.GameType;
import net.spell_engine.entity.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import static local.audit.WarriorUltimatesProbe.*;
public class BalanceProbe {
 public static void run(ServerLevel w,ServerPlayer player,Consumer<String> sink)throws Exception {
  world=w;p=player;out=sink;
  var owner=(SpellContainerSource.Owner)p;var saved=owner.getSpellContainers();var mode=p.gameMode.getGameModeForPlayer();
  double health=p.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
  try {
   p.setGameMode(GameType.SURVIVAL);clear();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
   equip(List.of("skill_tree_rpgs:warrior_tier_1_passive_1"),List.of());
   p.addEffect(new MobEffectInstance(effect("skill_tree_rpgs:killing_spree"),160,4));
   p.setHealth(20);var target=mob(0,3);target.hurtServer(w,w.damageSources().playerAttack(p),5000);
   eq("Native kill heals four percent maximum HP",p.getHealth(),24);
   eq("Killing Spree has no damage reduction",attr("spell_engine:damage_taken"),p.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse("spell_engine:damage_taken")).orElseThrow()).getBaseValue());
   var canceled=new LivingDeathEvent(mob(0,4),w.damageSources().playerAttack(p));canceled.setCanceled(true);WarriorBalance.killed(canceled);eq("Canceled death does not heal",p.getHealth(),24);canceled.getEntity().discard();
   equip(List.of(),List.of());target=mob(0,3);target.hurtServer(w,w.damageSources().playerAttack(p),5000);eq("No passive no kill healing",p.getHealth(),24);
   clear();var rage=spell("rogues:mortal_strike");start(rage);
   for(int i=0;i<5;i++) {
    p.setHealth(1);pulse(rage);
    eq("Berserker pulse heal "+i,p.getHealth(),p.getMaxHealth());
    eq("Berserker Speed I-V "+i,p.getEffect(MobEffects.SPEED).getAmplifier(),i);
    eq("Berserker speed refreshed 2s "+i,p.getEffect(MobEffects.SPEED).getDuration(),40);
    eq("Berserker duration 14s "+i,p.getEffect(effect(WarriorUltimates.BERSERKER)).getDuration(),280);
   }
   for(String id:List.of("spell_engine:stun","wizards:frozen")){
    clear();removeCd("rogues:mortal_strike");var h=spell("rogues:mortal_strike");start(h);pulse(h);
    p.addEffect(new MobEffectInstance(effect(id),40));eq("Berserker interrupted by "+id,WarriorUltimates.interrupt(p,((SpellCaster.Player)p).getSpellCastProcess())?1:0,1);
    eq("Interrupted channel cleared "+id,((SpellCaster.Player)p).getSpellCastProcess()==null?1:0,1);
   }
   intervals();
  }finally {for(String id:List.of("skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2","skill_tree_rpgs:warrior_tier_4_spell_2_modifier_2"))removeCd(id);((SpellCaster.Player)p).getInteractor().requestClear();clear();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);p.setHealth(p.getMaxHealth());p.setGameMode(mode);owner.setSpellContainers(saved);owner.spellModifierCache().clear();}
 }
 static void intervals() {
  clear();var cooldown=((SpellCaster.Player)p).getCooldownManager();
  var lethality=spell("skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2");removeCd("skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2");
  eq("Lethality base interval",lethality.value().cost.cooldown.duration,.2);
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_4_spell_1_modifier_2"));start(spell("rogues:mortal_strike"));pulse(spell("rogues:mortal_strike"));p.setHealth(20);
  var target=mob(0,3);net.spell_engine.internals.SpellTriggers.onMeleeImpact(p,target);eq("First Lethality heal",p.getHealth(),22);
  int ticks=cooldown.getCooldownDuration(lethality);eq("Lethality native haste interval",ticks,Math.max(1,Math.round(net.spell_engine.internals.SpellParameters.getCooldownDuration(p,lethality)*20)));
  for(int i=0;i<10;i++)net.spell_engine.internals.SpellTriggers.onMeleeImpact(p,target);eq("Repeated same-tick Lethality heals blocked",p.getHealth(),22);
  for(int i=0;i<ticks-1;i++)cooldown.tickUpdate();net.spell_engine.internals.SpellTriggers.onMeleeImpact(p,target);eq("Lethality blocked before interval",p.getHealth(),22);
  cooldown.tickUpdate();net.spell_engine.internals.SpellTriggers.onMeleeImpact(p,target);eq("Lethality resumes at interval",p.getHealth(),24);target.discard();
  clear();var sentinel=spell("skill_tree_rpgs:warrior_tier_4_spell_2_modifier_2");removeCd("skill_tree_rpgs:warrior_tier_4_spell_2_modifier_2");eq("Sentinel base interval",sentinel.value().cost.cooldown.duration,.25);
  equip(List.of("skill_tree_rpgs:warrior_tier_4_spell_2_modifier_2"),List.of());start(spell("rogues:last_stand"));pulse(spell("rogues:last_stand"));target=mob(0,3);
  var source=world.damageSources().mobAttack(target);net.spell_engine.internals.SpellTriggers.onDamageTaken(p,source,1);eq("First Sentinel stack",p.getEffect(effect(WarriorUltimates.SENTINEL)).getAmplifier(),0);
  ticks=cooldown.getCooldownDuration(sentinel);
  for(int i=0;i<10;i++)net.spell_engine.internals.SpellTriggers.onDamageTaken(p,source,1);eq("Repeated same-tick Sentinel stacks blocked",p.getEffect(effect(WarriorUltimates.SENTINEL)).getAmplifier(),0);
  for(int i=0;i<ticks-1;i++)cooldown.tickUpdate();net.spell_engine.internals.SpellTriggers.onDamageTaken(p,source,1);eq("Sentinel blocked before interval",p.getEffect(effect(WarriorUltimates.SENTINEL)).getAmplifier(),0);
  cooldown.tickUpdate();net.spell_engine.internals.SpellTriggers.onDamageTaken(p,source,1);eq("Sentinel resumes at interval",p.getEffect(effect(WarriorUltimates.SENTINEL)).getAmplifier(),1);target.discard();
 }
}
