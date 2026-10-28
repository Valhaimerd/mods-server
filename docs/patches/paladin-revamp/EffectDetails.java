package net.spell_engine.internals;

import java.util.*;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

/** Native synchronized attachment: caster-resolved support data and supplemental effect details. */
public final class EffectDetails {
 private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,"spell_engine");
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<String>> DATA=TYPES.register("received_effect_details",()->AttachmentType.builder(()->"{}").serialize(Codec.STRING.fieldOf("value")).sync(ByteBufCodecs.STRING_UTF8).build());
 public static void register(IEventBus bus){TYPES.register(bus);}
 public static JsonObject data(net.minecraft.world.entity.Entity entity){String value=entity.getData(DATA);try{return JsonParser.parseString(value).getAsJsonObject();}catch(RuntimeException bad){return new JsonObject();}}
 public static void save(net.minecraft.world.entity.Entity entity,JsonObject root){String value=root.toString();if(!value.equals(entity.getData(DATA)))entity.setData(DATA,value);}
 public static JsonObject object(JsonObject parent,String key){if(!parent.has(key)||!parent.get(key).isJsonObject())parent.add(key,new JsonObject());return parent.getAsJsonObject(key);}
 public static double number(JsonObject record,String key){return record.has(key)?record.get(key).getAsDouble():0;}
 public static String fmt(double n){return java.math.BigDecimal.valueOf(n).setScale(2,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
 public static boolean rpg(String id){return id.startsWith("paladins:")||id.startsWith("skill_tree_rpgs:")||id.startsWith("archers:")||id.startsWith("rogues:")||id.startsWith("wizards:")||id.equals("spell_engine:stun")||id.equals("spell_engine:bleed");}
 public static List<String> received(LivingEntity entity,MobEffectInstance effect){
  String id=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();var result=new ArrayList<String>(PaladinRevamp.details(entity,id));int levels=effect.getAmplifier()+1;
  if(id.equals("skill_tree_rpgs:healing_focus"))result.add("Absorption received: +"+fmt(PriestPassives.absorptionBonus(entity)*100)+"%");
  if(id.equals("skill_tree_rpgs:vitality")){
   var armor=entity.getAttribute(Attributes.ARMOR);var scaling=armor==null?null:armor.getModifier(Identifier.parse("warrior_pack:vitality_scaling"));
   if(scaling!=null)result.add("Additional armor from melee power: +"+fmt(scaling.amount()));
  }
  if(id.equals("wizards:frozen"))result.add("Frozen: cannot move, attack or cast.");
  else if(id.equals("spell_engine:stun")||id.equals("paladins:judgement")||id.equals("rogues:shock"))result.add("Stunned: cannot move, attack or cast.");
  else if(Set.of("rogues:net_trap","rogues:bear_trap","skill_tree_rpgs:natures_grasp").contains(id))result.add("Rooted: cannot move or jump.");
  if(id.equals("skill_tree_rpgs:deflection"))result.add("Blocks remaining: "+levels);
  if(effect.getEffect().value() instanceof net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect vulnerability){
   var v=vulnerability.getVulnerability(vulnerability.school,effect.getAmplifier());String school=vulnerability.school.id.getPath();
   if(v.powerBaseMultiplier()!=0)result.add(school+" damage taken: +"+fmt(100*v.powerBaseMultiplier())+"%");
   if(v.criticalChanceBonus()!=0)result.add(school+" critical hit chance received: +"+fmt(100*v.criticalChanceBonus())+"%");
   if(v.criticalDamageBonus()!=0)result.add(school+" critical damage received: +"+fmt(100*v.criticalDamageBonus())+"%");
  }
  if(id.equals("skill_tree_rpgs:fracture")){var armor=entity.getAttribute(Attributes.ARMOR);var extra=armor==null?null:armor.getModifier(Identifier.parse("rogue_pack:fracture_scaling"));if(extra!=null)result.add("Additional armor lost: "+fmt(-extra.amount()));}
  if(id.equals("skill_tree_rpgs:berserker"))result.add("Basic melee attack damage: +"+fmt(10*levels)+"%");
  if(id.equals("paladins:blessed_strikes")){result.add("Holy waves remaining: "+Math.min(5,levels));result.add("Wave damage: "+(PaladinTierOne.node(entity,"spell_2_modifier_1")?"40":"30")+"% Melee Damage");}
  if(id.equals("skill_tree_rpgs:celestial_orb")){result.add("Retaliatory orbs remaining: "+levels);result.add("Orb damage: 30% Healing Power");}
  if(id.equals("skill_tree_rpgs:supercharge"))result.add("Next ordinary arrow: 125% normal damage.");
  if(id.equals("rogues:slice_and_dice")){result.add("Enhanced hits remaining: "+levels);result.add("Bonus damage: 8% dual-wield damage.");}
  if(id.equals("skill_tree_rpgs:prepared_counter_ready"))result.add("Next basic melee hit adds 15% dual-wield damage.");
  if(id.equals("skill_tree_rpgs:concussion_blow"))result.add("Next melee hit stuns for 1s.");
  if(id.equals("skill_tree_rpgs:divine_favor"))result.add("Next attacker and enemies within 2 blocks are stunned for 1s.");
  if(id.equals("skill_tree_rpgs:presence_of_mind"))result.add("Next Arcane spell makes enemies take 20% more Arcane damage for 6s.");
  if(id.equals("skill_tree_rpgs:arctic_reflex"))result.add("Next Frost spell deals 20% more damage.");
  if(id.equals("skill_tree_rpgs:warrior_freedom"))result.add("Immune to movement-impairing effects.");
  if(id.equals("skill_tree_rpgs:priest_fade"))result.add("Invisible, including armor and held equipment.");
  if(id.equals("spell_engine:bleed"))result.add("Every 1.25s: lose 1 HP while still, or 4 HP while moving horizontally.");
  if(Set.of("paladins:priest_absorption","skill_tree_rpgs:priest_penance_shield","skill_tree_rpgs:priest_serenity_shield","skill_tree_rpgs:tenacity_ward","skill_tree_rpgs:arcane_ward","skill_tree_rpgs:fire_ward","skill_tree_rpgs:frost_ward").contains(id))result.add("Total absorption remaining: "+fmt(entity.getAbsorptionAmount())+" HP");
  return result;
 }
 public static void update(LivingEntity entity){
  if(entity.level().isClientSide())return;
  var root=data(entity);var lines=new JsonObject();
  for(var effect:entity.getActiveEffects()){
   String id=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();if(!rpg(id))continue;
   var resolved=received(entity,effect);if(resolved.isEmpty())continue;var array=new JsonArray();for(String line:resolved)array.add(line);lines.add(id,array);
  }
  root.add("lines",lines);save(entity,root);
 }
 public static List<String> clientLines(LivingEntity entity,String id){
  if(entity==null)return List.of();var root=data(entity);if(!root.has("lines"))return List.of();var lines=root.getAsJsonObject("lines");if(!lines.has(id))return List.of();var result=new ArrayList<String>();for(var line:lines.getAsJsonArray(id))result.add(line.getAsString());return result;
 }
}
