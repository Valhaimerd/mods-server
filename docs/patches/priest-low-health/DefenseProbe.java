package local.audit;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_power.api.*;
public class DefenseProbe {
 static PriestProbe t;
 static int charges(){var e=t.p.getEffect(PriestRules.effect(PriestDefenses.ORBS));return e==null?0:e.getAmplifier()+1;}
 static void ready(){
  t.p.removeAllEffects();PassiveProbe.equip(t,PriestDefenses.ORB_SKILL);
  var holy=t.spell("paladins:holy_beam");
  SpellTriggers.onSpellImpactSpecific(t.p,t.p,holy,holy.value().impacts.getFirst(),true,Spell.Trigger.Stage.POST);
  t.eq("Critical active spell readies five orbs",charges(),5);
  t.eq("Orb readiness remains fifteen seconds",t.p.getEffect(PriestRules.effect(PriestDefenses.ORBS)).getDuration(),300);
 }
 static LivingEntity pet(){var e=EntityTypes.WOLF.create(t.world,EntitySpawnReason.COMMAND);e.setOwnerReference(EntityReference.of(t.p.getUUID()));e.setPos(9,80,8);e.setNoAi(true);t.world.addFreshEntity(e);return e;}
 public static void run(PriestProbe probe){
  t=probe;var p=t.p;var saved=((SpellContainerSource.Owner)p).getSpellContainers();var mode=p.gameMode.getGameModeForPlayer();
  var power=p.getAttribute(SpellSchools.HEALING.getAttributeEntry());double oldPower=power.getBaseValue();
  var crit=p.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());double oldCrit=crit.getBaseValue();
  var max=p.getAttribute(Attributes.MAX_HEALTH);double oldMax=max.getBaseValue();int oldTicks=p.tickCount;
  var entities=new ArrayList<LivingEntity>();
  try{
   p.removeAllEffects();p.setAbsorptionAmount(0);p.setPos(8,80,8);max.setBaseValue(100);power.setBaseValue(40);crit.setBaseValue(0);
   t.world.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);p.connection.markClientLoaded();p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setInvulnerable(false);
   PassiveProbe.equip(t,PriestDefenses.PAIN_SKILL);
   p.setHealth(49);p.tickCount=4;NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(p));t.eq("No protection update before fifth tick",p.hasEffect(PriestRules.effect(PriestDefenses.PAIN))?1:0,0);
   p.tickCount=5;NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(p));t.eq("Native five-tick event applies protection",p.getEffect(PriestRules.effect(PriestDefenses.PAIN)).getAmplifier(),0);
   var damage=p.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_engine:damage_taken")).orElseThrow());
   float[] hp={51,50,49,40,39,30,29,20,19,39,49,80};int[] levels={0,0,1,1,2,2,3,3,4,2,1,0};
   for(int i=0;i<hp.length;i++){
    p.setHealth(hp[i]);PriestDefenses.update(p);var e=p.getEffect(PriestRules.effect(PriestDefenses.PAIN));
    t.eq("Health threshold "+hp[i],e==null?0:e.getAmplifier()+1,levels[i]);t.eq("Native incoming-damage attribute "+hp[i],damage.getValue(),100*(1-.06*levels[i]));
   }
   max.setBaseValue(200);p.setHealth(79);PriestDefenses.update(p);t.eq("Current maximum HP controls thresholds",p.getEffect(PriestRules.effect(PriestDefenses.PAIN)).getAmplifier()+1,2);
   max.setBaseValue(100);p.setHealth(19);PriestDefenses.update(p);var enemy=t.mob(8,12);entities.add(enemy);p.invulnerableTime=0;p.hurtServer(t.world,t.world.damageSources().mobAttack(enemy),10);t.eq("Actual hit reduced by twenty-four percent",19-p.getHealth(),7.6);
   p.setHealth(49);t.equip();PriestDefenses.update(p);t.eq("Removing passive clears protection",p.hasEffect(PriestRules.effect(PriestDefenses.PAIN))?1:0,0);
   PassiveProbe.equip(t,PriestDefenses.PAIN_SKILL);var holy=t.spell("paladins:holy_beam");var ally=pet();entities.add(ally);ally.setHealth(2);
   SpellTriggers.onSpellImpactSpecific(p,ally,holy,holy.value().impacts.getFirst(),false,Spell.Trigger.Stage.PRE);t.eq("Healing no longer applies Pain to recipient",ally.hasEffect(PriestRules.effect(PriestDefenses.PAIN))?1:0,0);
   ready();
   float before=enemy.getHealth();
   for(int i=0;i<5;i++){enemy.invulnerableTime=0;PriestDefenses.protectAlly(ally,enemy);t.eq("Ally hit spends exactly one orb "+i,charges(),4-i);}
   t.eq("Five orbs deal thirty percent P each",before-enemy.getHealth(),60);enemy.invulnerableTime=0;PriestDefenses.protectAlly(ally,enemy);t.eq("Exhausted orbs deal no extra damage",before-enemy.getHealth(),60);
   ready();var outsider=t.mob(9,8);entities.add(outsider);PriestDefenses.protectAlly(outsider,enemy);t.eq("Unrelated nearby entity is not protected",charges(),5);
   ally.setPos(8,80,16.1);PriestDefenses.protectAlly(ally,enemy);t.eq("Ally beyond eight blocks cannot spend orb",charges(),5);
   ally.setPos(8,80,16);enemy.invulnerableTime=0;PriestDefenses.protectAlly(ally,enemy);t.eq("Ally at eight blocks is protected",charges(),4);
   ally.setPos(9,80,8);PriestDefenses.protectAlly(ally,ally);t.eq("Friendly attacker cannot spend orb",charges(),4);
   ready();p.setHealth(100);p.invulnerableTime=0;enemy.invulnerableTime=0;before=enemy.getHealth();p.hurtServer(t.world,t.world.damageSources().mobAttack(enemy),1);t.eq("Own hit uses one native charge without duplicate",charges(),4);t.eq("Native own-hit retaliation coefficient",before-enemy.getHealth(),12);
   ready();ally.invulnerableTime=0;ally.setHealth(ally.getMaxHealth());enemy.invulnerableTime=0;before=enemy.getHealth();ally.hurtServer(t.world,t.world.damageSources().mobAttack(enemy),1);t.eq("Actual ally damage event spends one charge",charges(),4);t.eq("Actual ally event deals orb damage",before-enemy.getHealth(),12);
   ready();PriestRules.stack(ally,PriestRules.SHIELD,4,10,100,p);ally.invulnerableTime=0;enemy.invulnerableTime=0;before=ally.getHealth();ally.hurtServer(t.world,t.world.damageSources().mobAttack(enemy),1);t.eq("Absorbed ally hit leaves real health intact",ally.getHealth(),before);t.eq("Absorbed ally hit still retaliates",charges(),4);
   ready();p.removeEffect(PriestRules.effect(PriestDefenses.ORBS));PriestDefenses.protectAlly(ally,enemy);t.eq("Expired readiness cannot retaliate",charges(),0);
  }finally{
   entities.forEach(Entity::discard);p.removeAllEffects();p.setAbsorptionAmount(0);power.setBaseValue(oldPower);crit.setBaseValue(oldCrit);max.setBaseValue(oldMax);p.setHealth(p.getMaxHealth());p.tickCount=oldTicks;p.setGameMode(mode);((SpellContainerSource.Owner)p).setSpellContainers(saved);((SpellContainerSource.Owner)p).spellModifierCache().clear();
  }
 }
}
