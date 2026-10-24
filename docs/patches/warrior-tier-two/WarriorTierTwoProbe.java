package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.effect.EntityActionsAllowed;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
public class WarriorTierTwoProbe {
 static ServerLevel world;static ServerPlayer player;static Consumer<String> out;
 static void eq(String id,double actual,double expected){if(Math.abs(actual-expected)>.015)throw new AssertionError(id+" actual="+actual+" expected="+expected);out.accept("Warrior II PASS "+id+" "+actual);}
 static Holder<Spell> spell(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 static Holder<MobEffect> effect(String id){return WarriorRules.effect(id);}
 static void equip(List<String> passives,List<String> modifiers){
  var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var ids=new ArrayList<>(passives);ids.addAll(modifiers);var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
  owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),passives.stream().map(WarriorTierTwoProbe::spell).toList(),modifiers.stream().map(WarriorTierTwoProbe::spell).toList(),List.of(new SpellContainerSource.SourcedContainer("warrior_ii",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
 }
 static SpellExecution.ImpactContext context(Holder<Spell> h){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0);}
 static void impact(LivingEntity target,Holder<Spell> h){target.invulnerableTime=0;SpellImpacts.performImpacts(world,player,target,target,h,h.value().impacts,context(h),false,null);}
 static LivingEntity mob(String type,double x,float hp){var m=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(type)).create(world,EntitySpawnReason.COMMAND);m.setPos(x,80,8);m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);m.setHealth(hp);if(m instanceof Mob ai)ai.setNoAi(true);world.addFreshEntity(m);return m;}
 static void clear(String id){((SpellCaster.Player)player).getCooldownManager().remove(Identifier.parse(id));}
 public static void run(ServerLevel w,ServerPlayer p,Consumer<String> sink)throws Exception{
  world=w;player=p;out=sink;var owner=(SpellContainerSource.Owner)player;var saved=owner.getSpellContainers();var attack=player.getAttribute(Attributes.ATTACK_DAMAGE);double original=attack.getBaseValue();var gameMode=player.gameMode.getGameModeForPlayer();player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.getAbilities().invulnerable=false;player.setInvulnerable(false);
  try{rounding();vitality(attack);wild();shout();rolls(attack);}finally{player.removeAllEffects();attack.setBaseValue(original);player.setGameMode(gameMode);owner.setSpellContainers(saved);owner.spellModifierCache().clear();}
 }
 static void rounding(){for(double[] x:new double[][]{{.24,0},{.25,.5},{.44,.5},{.74,.5},{.75,1},{2.4,2.5}})eq("half-point rounding "+x[0],WarriorTierTwo.half(x[0]),x[1]);}
 static void vitality(net.minecraft.world.entity.ai.attributes.AttributeInstance attack){
  var enemy=mob("minecraft:zombie",4,100);player.removeAllEffects();attack.setBaseValue(40);equip(List.of("skill_tree_rpgs:warrior_tier_1_passive_2"),List.of());double armor=player.getAttributeValue(Attributes.ARMOR);float hp=player.getMaxHealth();player.setHealth(5);
  for(int i=1;i<=12;i++){clear("skill_tree_rpgs:warrior_tier_1_passive_2");float before=player.getHealth();SpellTriggers.onDamageTaken(player,world.damageSources().mobAttack(enemy),1);eq("Vitality scaled armor "+i,player.getAttributeValue(Attributes.ARMOR),Math.min(30,armor+3.5*Math.min(i,10)));eq("Vitality scaled HP "+i,player.getMaxHealth(),hp+2*Math.min(i,10));eq("Vitality no cap healing "+i,player.getHealth(),before+(i<=10?2:0));}
  player.removeAllEffects();eq("Vitality scaling removed",player.getAttributeValue(Attributes.ARMOR),armor);attack.setBaseValue(4);clear("skill_tree_rpgs:warrior_tier_1_passive_2");SpellTriggers.onDamageTaken(player,world.damageSources().mobAttack(enemy),1);eq("Vitality 0.24 rounds down",player.getAttributeValue(Attributes.ARMOR),armor+1);player.removeAllEffects();enemy.discard();
 }
 static void wild(){
  var h=spell("rogues:charge");eq("Wild cooldown",h.value().cost.cooldown.duration,16);eq("Wild default duration",h.value().impacts.getFirst().action.status_effect.duration,2);
  for(boolean improved:new boolean[]{false,true}){
   player.removeAllEffects();var mods=new ArrayList<String>();if(improved)mods.add("skill_tree_rpgs:warrior_tier_3_spell_1_root");mods.add("skill_tree_rpgs:warrior_tier_3_spell_1_modifier_1");equip(List.of(),mods);
   for(String id:List.of("wizards:frozen","spell_engine:stun","rogues:net_trap","archers:entangling_roots","minecraft:slowness"))WarriorRules.addEffect(player,new MobEffectInstance(effect(id),100),null);
   player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100));((EntityActionsAllowed.ControlledEntity)player).updateEntityActionsAllowed();
   eq("Native CC blocks ordinary casts",EntityActionsAllowed.isImpaired(player,EntityActionsAllowed.Player.CAST_SPELL)?1:0,1);
   eq("Wild allowed while controlled",WarriorTierTwo.impaired(player,EntityActionsAllowed.Player.CAST_SPELL,false,h)?1:0,0);eq("Other skill still blocked",WarriorTierTwo.impaired(player,EntityActionsAllowed.Player.CAST_SPELL,false,spell("rogues:throw"))?1:0,1);
   impact(player,h);eq("Wild actual duration improved="+improved,player.getEffect(effect("rogues:charge")).getDuration(),improved?60:40);eq("Wild freedom remains two seconds",player.getEffect(effect(WarriorTierTwo.FREEDOM)).getDuration(),40);
   eq("Endurance full resistance",player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),1);eq("Endurance matches Wild duration",player.getEffect(effect(WarriorTierTwo.ENDURANCE)).getDuration(),improved?60:40);
   for(String id:List.of("wizards:frozen","spell_engine:stun","rogues:net_trap","archers:entangling_roots","minecraft:slowness")){
    eq("Wild cleanses "+id,player.hasEffect(effect(id))?1:0,0);eq("Wild native immunity "+id,player.addEffect(new MobEffectInstance(effect(id),100))?1:0,0);eq("Wild pack force-path immunity "+id,WarriorRules.addEffect(player,new MobEffectInstance(effect(id),100),null)?1:0,0);
   }
   eq("Unrelated Weakness retained",player.hasEffect(MobEffects.WEAKNESS)?1:0,1);eq("Purify restores casting",EntityActionsAllowed.isImpaired(player,EntityActionsAllowed.Player.CAST_SPELL)?1:0,0);
   player.removeEffect(effect(WarriorTierTwo.FREEDOM));eq("Control works when protection expires",player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,20))?1:0,1);
  }
  player.removeAllEffects();equip(List.of(),List.of());double kb=player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);impact(player,h);eq("Wild gives flat quarter resistance",player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),kb+.25);player.removeAllEffects();
  equip(List.of("skill_tree_rpgs:warrior_tier_3_spell_1_modifier_2"),List.of());SpellTriggers.onSpellCast(player,h,List.of(player));eq("Concussion four-second readiness",player.getEffect(effect("skill_tree_rpgs:concussion_blow")).getDuration(),80);eq("Concussion no cooldown",spell("skill_tree_rpgs:warrior_tier_3_spell_1_modifier_2").value().cost.cooldown.duration,0);var boss=mob("minecraft:villager",4,10000);SpellTriggers.onMeleeImpact(player,boss);eq("Concussion universal two-second stun",boss.getEffect(effect("spell_engine:stun")).getDuration(),40);boss.discard();player.removeAllEffects();
 }
 static void shout(){
  var h=spell("rogues:shout");equip(List.of(),List.of());eq("Shout radius",SpellParameters.getRange(player,h),12);eq("Shout cooldown",h.value().cost.cooldown.duration,16);player.setPos(0,80,0);
  eq("Shout can cast while controlled",WarriorTierTwo.impaired(player,EntityActionsAllowed.Player.CAST_SPELL,false,h)?1:0,0);
  var targets=SpellTarget.findTargets(player,h,SpellTarget.SearchResult.empty(),false);eq("Shout includes caster without enemies",targets.entities().contains(player)?1:0,1);impact(player,h);eq("Shout freedom three seconds",player.getEffect(effect(WarriorTierTwo.FREEDOM)).getDuration(),60);
  var enemy=mob("minecraft:zombie",4,1000);enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);double base=enemy.getAttributeValue(Attributes.ATTACK_DAMAGE);impact(enemy,h);eq("Shout no base damage",enemy.getHealth(),1000);eq("Shout six seconds",enemy.getEffect(effect("rogues:demoralize")).getDuration(),120);eq("Shout high-HP mark",enemy.getEffect(MobEffects.GLOWING).getDuration(),120);eq("Shout nonstacking level",enemy.getEffect(effect("rogues:demoralize")).getAmplifier(),0);impact(enemy,h);eq("Repeated shout remains one level",enemy.getEffect(effect("rogues:demoralize")).getAmplifier(),0);eq("Shout melee reduction",enemy.getAttributeValue(Attributes.ATTACK_DAMAGE),base*.92);
  var dragon=mob("minecraft:ender_dragon",6,10000);impact(dragon,h);eq("Shout universal boss debuff",dragon.hasEffect(effect("rogues:demoralize"))?1:0,1);eq("Shout universal boss mark",dragon.hasEffect(MobEffects.GLOWING)?1:0,1);dragon.discard();
  equip(List.of("skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1"),List.of("skill_tree_rpgs:warrior_tier_3_spell_2_root"));eq("Improved shout radius",SpellParameters.getRange(player,h),16);eq("Battle Shout matches improved radius",SpellParameters.getRange(player,spell("skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1")),16);for(String key:List.of("minecraft:attack_damage","ranged_weapon:damage","spell_power:arcane","spell_power:fire","spell_power:frost")){player.removeEffect(effect("skill_tree_rpgs:battle_shout"));var a=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(key)).orElseThrow());double before=a.getValue();impact(player,spell("skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1"));eq("Battle Shout eight percent "+key,a.getValue(),before*1.08);}
  equip(List.of(),List.of("skill_tree_rpgs:warrior_tier_3_spell_2_modifier_2"));enemy.removeAllEffects();impact(enemy,h);eq("Challenging damage",1000-enemy.getHealth(),4);eq("Challenging 0.2 second stun",enemy.getEffect(effect("spell_engine:stun")).getDuration(),4);out.accept("Challenging target during stun="+((Mob)enemy).getTarget()+" player="+player);enemy.removeEffect(effect("spell_engine:stun"));((EntityActionsAllowed.ControlledEntity)enemy).updateEntityActionsAllowed();eq("Challenging acquires mob target",((Mob)enemy).getTarget()==player?1:0,1);enemy.discard();player.removeAllEffects();
 }
 static void rolls(net.minecraft.world.entity.ai.attributes.AttributeInstance attack){
  equip(List.of("skill_tree_rpgs:warrior_tier_2_passive_1"),List.of());
  for(double m:new double[]{4,40,7.3333333}){attack.setBaseValue(m);player.setHealth(5);SpellTriggers.onRoll(player);eq("Warm Up native roll heal M="+m,player.getHealth()-5,1+WarriorTierTwo.half(.06*m));eq("Warm Up no cooldown",((SpellCaster.Player)player).getCooldownManager().isCoolingDown(spell("skill_tree_rpgs:warrior_tier_2_passive_1"))?1:0,0);}
  player.removeAllEffects();equip(List.of("skill_tree_rpgs:warrior_tier_2_passive_2"),List.of());clear("skill_tree_rpgs:warrior_tier_2_passive_2");double speed=player.getAttributeValue(Attributes.MOVEMENT_SPEED),as=player.getAttributeValue(Attributes.ATTACK_SPEED),dmg=player.getAttributeValue(Attributes.ATTACK_DAMAGE);
  SpellTriggers.onRoll(player);eq("Momentum readiness five seconds",player.getEffect(effect("skill_tree_rpgs:warrior_momentum_ready")).getDuration(),100);eq("Momentum waits for hit",player.hasEffect(effect(WarriorTierTwo.MOMENTUM))?1:0,0);var enemy=mob("minecraft:villager",4,1000);SpellTriggers.onMeleeImpact(player,enemy);eq("Momentum actual hit grants buff",player.getEffect(effect(WarriorTierTwo.MOMENTUM)).getDuration(),60);eq("Momentum move",player.getAttributeValue(Attributes.MOVEMENT_SPEED),speed*1.05);eq("Momentum attack speed",player.getAttributeValue(Attributes.ATTACK_SPEED),as*1.05);eq("Momentum melee",player.getAttributeValue(Attributes.ATTACK_DAMAGE),dmg*1.02);eq("Momentum cooldown",spell("skill_tree_rpgs:warrior_tier_2_passive_2").value().cost.cooldown.duration,10);enemy.discard();player.removeAllEffects();
 }
}
