package local.audit;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
@Mod("archer_probe") public class ArcherProbe {
 final List<String> results=new ArrayList<>();ServerLevel world;ServerPlayer player;
 void check(String label,boolean value){if(!value)throw new AssertionError(label);results.add("PASS "+label);}
 void near(String label,double actual,double expected){check(label+" actual="+actual+" expected="+expected,Math.abs(actual-expected)<.005);}
 Holder<Spell> spell(String id){return SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 JsonObject json(String id){return new Gson().toJsonTree(spell(id).value()).getAsJsonObject();}
 JsonElement field(JsonElement root,String path){for(String key:path.split("\\."))root=root.isJsonArray()?root.getAsJsonArray().get(Integer.parseInt(key)):root.getAsJsonObject().get(key);return root;}
 void value(String id,String path,double expected){near(id+" "+path,field(json(id),path).getAsDouble(),expected);}
 Holder<MobEffect> effect(String id){return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();}
 void equip(String... passives){var owner=(SpellContainerSource.Owner)player;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(passives));owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),Arrays.stream(passives).map(this::spell).toList(),List.of(),List.of(new SpellContainerSource.SourcedContainer("archer_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();}
 public ArcherProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void start(ServerStartedEvent event){var server=event.getServer();server.execute(()->{try{
  world=server.overworld();player=FakePlayerFactory.getMinecraft(world);player.setPos(0,80,0);world.getChunk(0,0);
  TierTwoProbe.run(world,player,results::add);RogueProbe.run(world,player,results::add);RogueTierTwoProbe.run(world,player,results::add);
  value("archers:entangling_roots","cost.cooldown.duration",12);
  value("archers:entangling_roots","deliver.clouds.0.volume.radius",6);
  value("archers:entangling_roots","deliver.clouds.0.time_to_live_seconds",6);
  value("skill_tree_rpgs:archer_tier_2_spell_2_root","modifiers.0.cooldown_duration_deduct",2);
  value("skill_tree_rpgs:archer_tier_2_spell_2_modifier_1","modifiers.0.impacts.0.chance",1);
  value("skill_tree_rpgs:archer_tier_2_spell_2_modifier_2","modifiers.0.impacts.0.chance",1);
  value("skill_tree_rpgs:archer_tier_2_spell_2_modifier_2","modifiers.0.impacts.0.action.status_effect.duration",1);
  value("skill_tree_rpgs:archer_tier_2_spell_1_modifier_2","modifiers.0.replacing_area_impact.radius",5);
  check("Charged Shot no falloff",field(json("skill_tree_rpgs:archer_tier_2_spell_1_modifier_2"),"modifiers.0.replacing_area_impact.area.distance_dropoff").getAsString().equals("NONE"));
  value("skill_tree_rpgs:archer_tier_1_passive_1","passive.triggers.0.chance",1);
  value("skill_tree_rpgs:archer_tier_1_passive_1","impacts.0.action.status_effect.duration",1);
  value("skill_tree_rpgs:archer_tier_1_passive_1","cost.cooldown.duration",8);
  value("skill_tree_rpgs:archer_tier_1_passive_2","impacts.0.action.status_effect.duration",4);
  var enemy=EntityTypes.CREEPER.create(world,EntitySpawnReason.COMMAND);enemy.setPos(0,80,4);enemy.setNoAi(true);enemy.setNoGravity(true);world.addFreshEntity(enemy);
  try {
   var speed=enemy.getAttribute(Attributes.MOVEMENT_SPEED);double base=speed.getValue();enemy.addEffect(new MobEffectInstance(effect("archers:entangling_roots"),20));near("roots -60% movement",speed.getValue(),base*.4);enemy.removeAllEffects();
   var jump=enemy.getAttribute(Attributes.JUMP_STRENGTH);if(jump!=null){double j=jump.getValue();enemy.addEffect(new MobEffectInstance(effect("archers:entangling_roots"),20));near("roots -60% jump",jump.getValue(),j*.4);enemy.removeAllEffects();}
   for(int stacks=1;stacks<=4;stacks++){
    enemy.removeAllEffects();enemy.setHealth(enemy.getMaxHealth());enemy.invulnerableTime=0;
    enemy.addEffect(new MobEffectInstance(effect("archers:hunters_mark"),240,stacks-1));
    float before=enemy.getHealth();enemy.hurtServer(world,world.damageSources().playerAttack(player),10);
    near("Hunter's Mark native incoming damage at "+stacks+" stacks",before-enemy.getHealth(),10*(1+.07*stacks));
   }
   enemy.removeAllEffects();
   equip("skill_tree_rpgs:archer_tier_1_passive_2");player.removeAllEffects();
   var haste=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:haste")).orElseThrow());double unbuffed=haste.getValue();
   for(int i=1;i<=6;i++){
    SpellTriggers.onArrowImpact(null,player,enemy,world.damageSources().playerAttack(player),1);
    var rhythm=player.getEffect(effect("skill_tree_rpgs:rhythm"));check("unmarked arrow hit triggers Rhythm "+i,rhythm!=null);near("Rhythm stacks capped five "+i,rhythm.getAmplifier()+1,Math.min(i,5));near("Rhythm refresh four seconds "+i,rhythm.getDuration(),80);near("Rhythm three percent per stack "+i,haste.getValue(),unbuffed*(1+.03*Math.min(i,5)));rhythm.tickServer(world,player,()->{});
   }
   player.removeAllEffects();equip("skill_tree_rpgs:archer_tier_1_passive_1");
   SpellTriggers.onArrowImpact(null,player,enemy,world.damageSources().playerAttack(player),1);
   check("actual guaranteed Concussive Shot trigger",enemy.hasEffect(effect("spell_engine:stun")));near("actual stun duration",enemy.getEffect(effect("spell_engine:stun")).getDuration(),20);
   enemy.removeAllEffects();SpellTriggers.onArrowImpact(null,player,enemy,world.damageSources().playerAttack(player),1);check("Concussive cooldown prevents immediate stun",!enemy.hasEffect(effect("spell_engine:stun")));
  } finally {enemy.discard();player.removeAllEffects();equip();}
  var ranged=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:damage")).orElseThrow());double oldRanged=ranged.getBaseValue();
  try {for(double r:new double[]{0,20,40})for(boolean improved:new boolean[]{false,true}){
   ranged.setBaseValue(r);var h=spell("archers:spirit_wolf");var def=h.value().impacts.getFirst().action.summon;
   var scaling=def.attribute_scaling;
   if(improved)scaling=net.spell_engine.api.spell.summon.AttributeScaling.merged(scaling,spell("skill_tree_rpgs:archer_tier_3_spell_2_root").value().modifiers.getFirst().summon_attribute_scaling);
   var wolf=(net.spell_engine.entity.SummonedEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("archers:spirit_wolf")).create(world,EntitySpawnReason.MOB_SUMMONED);
   wolf.onSummonedBySpell(new net.spell_engine.api.spell.summon.SpellSummoned.Args(player,h,def.behaviour,scaling,new SpellExecution.ImpactContext()));
   // Fake players are not world-resolvable by UUID; exercise the exact native scaling
   // method that setBehaviour calls when a real owner is present.
   var apply=net.spell_engine.entity.SummonedEntity.class.getDeclaredMethod("applyAttributeScaling",LivingEntity.class);apply.setAccessible(true);apply.invoke(wolf,player);
   near("Spirit Wolf native scaled HP R="+r+" improved="+improved,wolf.getMaxHealth(),20+(improved?.5:.4)*r);
   near("Spirit Wolf native scaled armor R="+r,wolf.getAttributeValue(Attributes.ARMOR),10+.05*r);
   near("Spirit Wolf native scaled melee R="+r,wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),3+.15*r);

   results.add("AUDIT Spirit Wolf R="+r+" improved="+improved+" HP="+wolf.getMaxHealth()+" armor="+wolf.getAttributeValue(Attributes.ARMOR)+" attack="+wolf.getAttributeValue(Attributes.ATTACK_DAMAGE)+" attackKnockback="+(wolf.getAttribute(Attributes.ATTACK_KNOCKBACK)==null?"absent":wolf.getAttributeValue(Attributes.ATTACK_KNOCKBACK))+" knockbackResistance="+wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)+" detection="+wolf.detectionRange());
  }}finally{ranged.setBaseValue(oldRanged);}
  PartyProbe.run(server,results);
  Files.write(Path.of("archer-results.txt"),results);Files.writeString(Path.of("archer-result.txt"),"PASS "+results.size()+" Archer/party measurements\n");
 }catch(Throwable failure){failure.printStackTrace();try{Files.write(Path.of("archer-results.txt"),results);Files.writeString(Path.of("archer-result.txt"),"FAIL "+failure);}catch(Exception ignored){}}server.halt(false);});}
}
