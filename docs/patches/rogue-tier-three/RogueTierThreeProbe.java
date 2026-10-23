package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.server.level.*;
import net.spell_engine.api.event.CombatEvents;
import net.spell_engine.api.spell.event.SpellEvents;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.delivery.melee.Melee;
public final class RogueTierThreeProbe {
 static void eq(String key,double got,double want){RogueTierTwoProbe.eq("III "+key,got,want);}
 static void equip(String...mods){RogueTierTwoProbe.equip(List.of(),List.of(mods));}
 static void vanish(){RogueTierTwoProbe.impact(RogueTierTwoProbe.player,RogueTierTwoProbe.spell("rogues:vanish"));}
 public static void run(ServerLevel world,ServerPlayer player,Consumer<String> out)throws Exception{
  RogueTierTwoProbe.world=world;RogueTierTwoProbe.player=player;RogueTierTwoProbe.out=out;
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var oldMain=player.getMainHandItem();var oldOff=player.getOffhandItem();var damage=player.getAttribute(Attributes.ATTACK_DAMAGE);double savedDamage=damage.getBaseValue();
  var crit=player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("critical_strike:chance")).orElseThrow());double savedCrit=crit.getBaseValue();
  var spellCrit=player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());double savedSpellCrit=spellCrit.getBaseValue();
  var cm=((SpellCaster.Player)player).getCooldownManager();
  try{
   player.removeAllEffects();player.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);player.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);crit.setBaseValue(0);spellCrit.setBaseValue(0);damage.setBaseValue(20);
   var stealth=RogueTierTwoProbe.effect("rogues:stealth");double speed=player.getAttributeValue(Attributes.MOVEMENT_SPEED);
   for(int mode=0;mode<3;mode++){
    player.removeAllEffects();equip(mode==0?new String[]{}:mode==1?new String[]{"skill_tree_rpgs:rogue_tier_4_spell_1_root"}:new String[]{"skill_tree_rpgs:rogue_tier_4_spell_1_root","skill_tree_rpgs:rogue_tier_4_spell_1_modifier_1"});vanish();eq("Stealth seven seconds mode="+mode,player.getEffect(stealth).getDuration(),140);eq("Movement penalty mode="+mode,player.getAttributeValue(Attributes.MOVEMENT_SPEED)/speed,mode==0?.5:mode==1?.6:.8);
    player.removeEffect(stealth);eq("Movement restored on dispel mode="+mode,player.getAttributeValue(Attributes.MOVEMENT_SPEED),speed);
   }
   var enemy=RogueTierTwoProbe.mob("minecraft:villager",3,80,0);
   final var actionEnemy=enemy;
   for(boolean shadow:new boolean[]{false,true}){
    equip(shadow?new String[]{"skill_tree_rpgs:rogue_tier_4_spell_1_root","skill_tree_rpgs:rogue_tier_4_spell_1_modifier_2"}:new String[]{"skill_tree_rpgs:rogue_tier_4_spell_1_root"});
    for(int action=0;action<4;action++){
     player.removeAllEffects();vanish();
     if(action==0)CombatEvents.ENTITY_ANY_ATTACK.invoke(l->l.onEntityAttack(new CombatEvents.EntityAttack.Args(player,actionEnemy)));
     if(action==1)CombatEvents.ITEM_USE.invoke(l->l.onItemUseStart(new CombatEvents.ItemUse.Args(player,CombatEvents.ItemUse.Stage.START)));
     if(action==2)SpellEvents.SPELL_CAST.invoke(l->l.onSpellCast(new SpellEvents.SpellCastEvent.Args(player,RogueTierTwoProbe.spell("rogues:shock_powder"),List.of(actionEnemy),SpellCast.Action.RELEASE,1)));
     if(action==3)net.spell_engine.SpellEngineMod.onIncomingDamage(player,world.damageSources().mobAttack(enemy),1);
     eq("Stealth action="+action+" shadow="+shadow,player.hasEffect(stealth)?1:0,shadow?1:0);
    }
    player.removeAllEffects();vanish();var tickEffects=LivingEntity.class.getDeclaredMethod("tickEffects");tickEffects.setAccessible(true);for(int tick=0;tick<140;tick++)tickEffects.invoke(player);eq("Natural expiry shadow="+shadow,player.hasEffect(stealth)?1:0,0);eq("Expiry cleans movement",player.getAttributeValue(Attributes.MOVEMENT_SPEED),speed);
   }
   enemy.discard();player.removeAllEffects();equip();
   var h=RogueTierTwoProbe.spell("rogues:mutilate");
   for(boolean heal:new boolean[]{false,true}){
    equip(heal?new String[]{"skill_tree_rpgs:rogue_tier_4_spell_2_modifier_1"}:new String[]{});var target=RogueTierTwoProbe.mob("minecraft:villager",player.getX()+1,player.getY(),player.getZ());double d=net.spell_power.api.SpellPower.getSpellPower(h.value().school,player).nonCriticalValue();player.setHealth(10);float before=target.getHealth();Melee.performAttackAgainstTargets(player,new Melee.AttackContext(Identifier.parse("rogues:mutilate"),h.value().deliver.melee.attacks.getFirst().id,1),new int[]{target.getId()});eq("Mutilate 200% dual power",before-target.getHealth(),2*d);if(heal)eq("Crimson still 10% dual power",player.getHealth()-10,.1*d);target.discard();
   }
   equip("skill_tree_rpgs:rogue_tier_4_spell_2_modifier_2");var target=RogueTierTwoProbe.mob("minecraft:villager",player.getX()+1,player.getY(),player.getZ());Melee.performAttackAgainstTargets(player,new Melee.AttackContext(Identifier.parse("rogues:mutilate"),h.value().deliver.melee.attacks.getFirst().id,1),new int[]{target.getId()});eq("Bloody Strikes duration",target.getEffect(RogueTierTwoProbe.effect("spell_engine:bleed")).getDuration(),150);eq("Bloody Strikes no Poison",target.hasEffect(MobEffects.POISON)?1:0,0);target.discard();
   player.removeAllEffects();RogueTierTwoProbe.equip(List.of("skill_tree_rpgs:rogue_tier_3_passive_1"),List.of());var cheat=RogueTierTwoProbe.spell("skill_tree_rpgs:rogue_tier_3_passive_1");cm.remove(Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_1"));enemy=RogueTierTwoProbe.mob("minecraft:zombie",3,80,0);player.setHealth(10);SpellTriggers.onDamageIncoming(player,world.damageSources().mobAttack(enemy),11);
   eq("Cheat Death immunity duration",player.getEffect(RogueTierTwoProbe.effect("skill_tree_rpgs:cheat_death")).getDuration(),60);eq("Speed III",player.getEffect(MobEffects.SPEED).getAmplifier(),2);eq("Speed 3 seconds",player.getEffect(MobEffects.SPEED).getDuration(),60);eq("Jump Boost III",player.getEffect(MobEffects.JUMP_BOOST).getAmplifier(),2);eq("Jump 3 seconds",player.getEffect(MobEffects.JUMP_BOOST).getDuration(),60);eq("Evasion six seconds",player.getEffect(RogueTierTwoProbe.effect("skill_tree_rpgs:cheat_death_evasion")).getDuration(),120);eq("Evasion thirty points",net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.asChance(player.getAttributeValue(net.spell_engine.api.entity.SpellEngineAttributes.EVASION_CHANCE.entry)),.3);enemy.discard();
   player.removeAllEffects();RogueTierTwoProbe.equip(List.of("skill_tree_rpgs:rogue_tier_3_passive_2"),List.of());var counter=RogueTierTwoProbe.spell("skill_tree_rpgs:rogue_tier_3_passive_2");cm.remove(Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_2"));enemy=RogueTierTwoProbe.mob("minecraft:villager",3,80,0);double d=net.spell_power.api.SpellPower.getSpellPower(counter.value().school,player).nonCriticalValue();float hp=enemy.getHealth();SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));eq("Counter targets attacker at 25% dual power",hp-enemy.getHealth(),.25*d);eq("Counter sets cooldown",cm.isCoolingDown(counter)?1:0,1);SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));eq("Counter cooldown prevents repeat",hp-enemy.getHealth(),.25*d);enemy.discard();
   cm.remove(Identifier.parse("skill_tree_rpgs:rogue_tier_3_passive_2"));var pet=(net.minecraft.world.entity.animal.wolf.Wolf)RogueTierTwoProbe.mob("minecraft:wolf",3,80,0);pet.setOwnerReference(EntityReference.of(player.getUUID()));SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(pet));eq("Counter protects owned pet",pet.getHealth(),1000);pet.discard();
  }finally{player.removeAllEffects();player.setItemSlot(EquipmentSlot.MAINHAND,oldMain);player.setItemSlot(EquipmentSlot.OFFHAND,oldOff);damage.setBaseValue(savedDamage);crit.setBaseValue(savedCrit);spellCrit.setBaseValue(savedSpellCrit);owner.setSpellContainers(old);owner.spellModifierCache().clear();}
 }
}
