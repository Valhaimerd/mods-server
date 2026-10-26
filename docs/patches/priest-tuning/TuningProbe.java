package local.audit;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.spell_engine.entity.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.delivery.SpellDelivery;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.*;
public final class TuningProbe {
 static PriestProbe a;
 static LivingEntity pet(float health,float max,double x){
  var p=EntityTypes.WOLF.create(a.world,EntitySpawnReason.COMMAND);p.setOwnerReference(EntityReference.of(a.p.getUUID()));p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);p.setHealth(health);p.setPos(x,80,8);p.setNoAi(true);a.world.addFreshEntity(p);return p;
 }
 static void tasks(){var s=(net.spell_engine.utils.WorldScheduler)a.world;s.updateScheduledTasks();for(var t:Map.copyOf(s.getScheduledTasks()).entrySet())if(t.getKey()<=a.world.getGameTime()+1){s.getScheduledTasks().remove(t.getKey());List.copyOf(t.getValue()).forEach(Runnable::run);}}
 static List<SpellProjectile> cycle(SummonedEntity well){
  var h=a.spell("paladins:lightwell_orb");var prior=a.world.getEntitiesOfClass(SpellProjectile.class,well.getBoundingBox().inflate(30)).stream().map(Entity::getId).toList();
  var context=new SpellExecution.ImpactContext(1,1,well.position(),SpellPower.getSpellPower(SpellSchools.HEALING,well),SpellTarget.FocusMode.DIRECT,0).effectiveCaster(a.p);
  SpellDelivery.resolveAndDeliver(a.world,well,h,SpellTarget.SearchResult.of(List.of(well.getTarget())),context,null);tasks();
  return a.world.getEntitiesOfClass(SpellProjectile.class,well.getBoundingBox().inflate(30),p->!prior.contains(p.getId()));
 }
 public static void run(PriestProbe probe)throws Exception{
  a=probe;boolean added=!a.world.players().contains(a.p);if(added)a.world.addNewPlayer(a.p);a.p.removeAllEffects();a.p.setPos(8,80,8);a.p.setHealth(a.p.getMaxHealth());a.equip();UltimatesProbe.a=a;
  var well=UltimatesProbe.summon();PriestUltimates.wellTick(well);
  var low=pet(3,4,9);var tank=pet(5,100,10);var middle=pet(4,20,11);
  var list=PriestTuning.pair(well,a.p);a.eq("Lowest raw HP outranks lower percentage",list.getFirst()==low?1:0,1);a.eq("Three injuries pick second lowest raw HP",list.get(1)==middle?1:0,1);
  PriestUltimates.wellTick(well);a.eq("Native selection uses raw HP",well.getTarget()==low?1:0,1);
  var shots=cycle(well);a.eq("Native well cycle emits exactly two motes",shots.size(),2);a.eq("First two injured receive distinct motes",shots.stream().map(SpellProjectile::getFollowedTarget).distinct().count(),2);a.eq("Raw HP priority applied to launched motes",shots.stream().allMatch(p->p.getFollowedTarget()==low||p.getFollowedTarget()==middle)?1:0,1);shots.forEach(Entity::discard);
  middle.setHealth(middle.getMaxHealth());list=PriestTuning.pair(well,a.p);a.eq("Exactly two injured split motes",list.getFirst()==low&&list.get(1)==tank?1:0,1);middle.setHealth(4);
  low.setHealth(low.getMaxHealth());middle.setHealth(middle.getMaxHealth());PriestUltimates.wellTick(well);list=PriestTuning.pair(well,a.p);a.eq("Only injured gets both slots",list.getFirst()==tank&&list.get(1)==tank?1:0,1);
  shots=cycle(well);a.eq("Single injured still gets two projectiles",shots.size(),2);a.eq("Both motes follow sole injured",shots.stream().allMatch(p->p.getFollowedTarget()==tank)?1:0,1);shots.forEach(Entity::discard);
  tank.setHealth(tank.getMaxHealth());a.eq("No injury or upgrades idles",PriestTuning.pair(well,a.p).size(),0);
  a.equip("skill_tree_rpgs:priest_tier_4_spell_1_modifier_1");low.addEffect(new MobEffectInstance(MobEffects.POISON,200));list=PriestTuning.pair(well,a.p);a.eq("Full-health cleanse fallback",list.size()==2&&list.getFirst()==low&&list.get(1)==low?1:0,1);
  tank.setHealth(8);a.eq("Injury outranks full-health cleanse",PriestTuning.pair(well,a.p).getFirst()==tank?1:0,1);
  low.discard();tank.discard();middle.discard();well.discard();
  a.p.removeAllEffects();a.p.setPos(1,80,1);a.p.setAbsorptionAmount(0);a.equip("skill_tree_rpgs:priest_tier_3_spell_2_modifier_1");
  var penance=a.spell("paladins:penance");var enemy=a.mob(1,13);var outside=a.mob(5.8,13);
  PriestRules.projectile(a.p,a.p,enemy,penance,a.context(penance,0).position(enemy.position()));a.eq("Remote enemy contact gives caster shield",a.p.getAbsorptionAmount(),1);a.eq("Remote enemy contact gives caster Hysteria",a.p.getEffect(PriestRules.effect("skill_tree_rpgs:hysteria")).getAmplifier()+1,1);a.eq("Penance four-block radius excludes 4.8 away",outside.getHealth(),1000);
  PriestRules.projectile(a.p,a.p,null,penance,a.context(penance,0).position(enemy.position()));a.eq("Remote terrain hit gives no caster buff",a.p.getAbsorptionAmount(),1);
  enemy.setPos(1,80,3);enemy.invulnerableTime=0;PriestRules.projectile(a.p,a.p,enemy,penance,a.context(penance,0).position(enemy.position()));a.eq("Caster inside blast gets only one stack",a.p.getAbsorptionAmount(),2);a.eq("Caster Hysteria no duplicate",a.p.getEffect(PriestRules.effect("skill_tree_rpgs:hysteria")).getAmplifier()+1,2);
  a.p.removeAllEffects();a.p.setAbsorptionAmount(0);a.equip();var ally=pet(2,8,8);ally.setPos(1,80,13);PriestRules.projectile(a.p,a.p,ally,penance,a.context(penance,0).position(ally.position()));a.eq("Remote ally contact grants caster shield",a.p.getAbsorptionAmount(),1);a.eq("Remote ally contact grants ally shield",ally.getAbsorptionAmount(),1);ally.discard();enemy.discard();outside.discard();
  a.p.removeAllEffects();a.p.setAbsorptionAmount(0);var doomed=a.mob(1,13);doomed.setHealth(1);PriestRules.projectile(a.p,a.p,doomed,penance,a.context(penance,0).position(doomed.position()));a.eq("Killing impact still grants remote caster support",a.p.getAbsorptionAmount(),1);doomed.discard();
  a.p.removeAllEffects();a.p.setAbsorptionAmount(0);var shock=a.spell("paladins:holy_shock");a.equip();a.p.setHealth(1);net.spell_engine.internals.impact.SpellImpacts.performImpacts(a.world,a.p,a.p,a.p,shock,shock.value().impacts,a.context(shock,0),false,null);float heal=a.p.getHealth()-1;float cd=SpellParameters.getCooldownDuration(a.p,shock);
  a.equip("skill_tree_rpgs:weapon_holy_root");a.p.setHealth(1);net.spell_engine.internals.impact.SpellImpacts.performImpacts(a.world,a.p,a.p,a.p,shock,shock.value().impacts,a.context(shock,0),false,null);a.eq("Holy Staff leaves healing unchanged",a.p.getHealth()-1,heal);a.eq("Holy Staff no cooldown reduction",SpellParameters.getCooldownDuration(a.p,shock),cd);
  var target=a.mob(8,10);net.spell_engine.internals.impact.SpellImpacts.performImpacts(a.world,a.p,target,a.p,shock,shock.value().impacts,a.context(shock,0),false,null);a.eq("Holy Staff five percent damage",1000-target.getHealth(),40*.8*1.05);target.discard();a.p.removeAllEffects();a.equip();
 }
}
