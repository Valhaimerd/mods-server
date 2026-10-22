package net.spell_engine.internals;
import com.google.gson.Gson;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.spell_engine.api.effect.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.rpg_series.config.*;
import net.spell_power.api.SpellPower;
/** Per-target Rogue rules; registry spell objects are never mutated during combat. */
public final class RogueRules {
 private static final Gson JSON=new Gson();
 private static final Identifier SCALING=Identifier.parse("rogue_pack:fracture_scaling");
 private static boolean id(Holder<Spell> spell,String id){return spell!=null&&spell.is(Identifier.parse(id));}
 public static void rogue(ConfigFile.Effects config){
  config.effects.put("rogues:slice_and_dice",new EffectConfig(List.of()));
 }
 public static void skill(ConfigFile.Effects config){
  config.effects.put("skill_tree_rpgs:fleet_footed",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:movement_speed",.05f,Operation.ADD_MULTIPLIED_BASE))));
  config.effects.put("skill_tree_rpgs:fracture",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("minecraft:armor",-2,Operation.ADD_VALUE))));
  config.effects.put("skill_tree_rpgs:smoke_screen",new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:evasion_chance",.5f,Operation.ADD_MULTIPLIED_BASE))));
  var key=Identifier.parse("skill_tree_rpgs:slice_evasion");
  if(net.skill_tree_rpgs.effect.SkillEffects.entries.stream().noneMatch(e->e.id.equals(key)))
   net.skill_tree_rpgs.effect.SkillEffects.entries.add(new Effects.Entry(key,"Slice Evasion","Increased evasion after successful Slice & Dice hits.",new CustomStatusEffect(MobEffectCategory.BENEFICIAL,0x99CCFF),new EffectConfig(List.of(new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:evasion_chance",.05f,Operation.ADD_MULTIPLIED_BASE)))));
  OnRemoval.configure(net.skill_tree_rpgs.effect.SkillEffects.FRACTURE.effect,c->{var armor=c.entity().getAttribute(Attributes.ARMOR);if(armor!=null)armor.removeModifier(SCALING);});
 }
 public static Spell.Impact impact(LivingEntity caster,Holder<Spell> spell,Spell.Impact original,SpellExecution.ImpactContext context,Entity target){
  var impact=PassiveRules.impact(caster,spell,original,context);
  if(id(spell,"rogues:shock_powder")&&impact.action.type==Spell.Impact.Action.Type.STATUS_EFFECT&&"rogues:shock".equals(impact.action.status_effect.effect_id)&&target instanceof LivingEntity living){
   impact=JSON.fromJson(JSON.toJson(impact),Spell.Impact.class);
   double power=context.power()!=null?context.power().nonCriticalValue():SpellPower.getSpellPower(spell.value().school,caster).nonCriticalValue();
   boolean longStun=living instanceof Player?living.getHealth()<24:living.getMaxHealth()<=50+2*power;
   impact.action.status_effect.duration=longStun?3:1;
   impact.action.status_effect.apply_limit=null;
  }
  return impact;
 }
 public static boolean addEffect(LivingEntity target,MobEffectInstance effect,Entity source){
  boolean applied=PartyRules.addEffect(target,effect,source);
  if(applied&&effect.getEffect().is(Identifier.parse("skill_tree_rpgs:fracture"))&&source instanceof LivingEntity caster){
   var armor=target.getAttribute(Attributes.ARMOR);var current=target.getEffect(effect.getEffect());
   if(armor!=null&&current!=null){
    double power=SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,caster).nonCriticalValue();
    armor.removeModifier(SCALING);
    // Persist the snapshot alongside the effect; OnRemoval clears it on expiration/dispel.
    armor.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(SCALING,-.1*power*(current.getAmplifier()+1),Operation.ADD_VALUE));
   }
  }
  return applied;
 }
 public static boolean evaluate(Holder<Spell> spell,Spell.Trigger trigger,SpellTriggers.Event event){
  if(id(spell,"rogues:slice_and_dice")){
   Entity target=event.target(trigger);
   if(!(target instanceof LivingEntity)||!PartyRules.harmfulAllowed(event.player,target))return false;
  }
  return SpellTriggers.evaluateTrigger(spell,trigger,event);
 }
 public static boolean after(boolean success,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact){
  if(success&&id(spell,"rogues:slice_and_dice")&&impact.action.type==Spell.Impact.Action.Type.DAMAGE&&target instanceof LivingEntity living&&!PartyRules.allies(caster,target)){
   var delta=caster.position().subtract(target.position()).multiply(1,0,1);double distance=delta.length();
   if(distance>.05){
    var push=delta.scale(Math.min(.2,distance)/distance*(1-living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
    target.push(push.x,0,push.z);target.needsSync=true;
    if(target instanceof net.minecraft.server.level.ServerPlayer player)player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));
   }
  }
  return success;
 }
}
