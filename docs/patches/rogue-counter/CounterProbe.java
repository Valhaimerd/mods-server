package local.audit;
import java.util.*;import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.resources.Identifier;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.item.ItemStack;
import net.spell_engine.internals.*;import net.spell_engine.internals.casting.SpellCaster;import net.spell_engine.internals.delivery.melee.Melee;
public final class CounterProbe {
 static final Identifier READY=Identifier.parse("skill_tree_rpgs:prepared_counter_ready");
 static void eq(Consumer<String> out,String key,double a,double b){if(Math.abs(a-b)>.01)throw new AssertionError(key+" got="+a+" expected="+b);out.accept("Prepared Counter PASS "+key+" "+a);}
 public static void run(ServerLevel world,ServerPlayer player,Consumer<String> out)throws Exception{
  player.removeAllEffects();RogueTierTwoProbe.equip(List.of("skill_tree_rpgs:rogue_tier_3_passive_2"),List.of());var counter=RogueTierTwoProbe.spell("skill_tree_rpgs:rogue_tier_3_passive_2");var cd=((SpellCaster.Player)player).getCooldownManager();cd.remove(counter.unwrapKey().orElseThrow().identifier());
  var enemy=RogueTierTwoProbe.mob("minecraft:villager",3,80,0);float hp=enemy.getHealth();SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));
  eq(out,"no retaliation damage",hp-enemy.getHealth(),0);eq(out,"two second base cooldown",counter.value().cost.cooldown.duration,2);
  var ready=BuiltInRegistries.MOB_EFFECT.get(READY).orElseThrow();eq(out,"evasion readies one hit",player.hasEffect(ready)?1:0,1);eq(out,"ready charge remains until used",player.getEffect(ready).isInfiniteDuration()?1:0,1);eq(out,"readiness does not alter spell/melee attribute",player.getAttributeValue(Attributes.ATTACK_DAMAGE),20);
  var spell=RogueTierTwoProbe.spell("rogues:mutilate");var target=RogueTierTwoProbe.mob("minecraft:villager",1,80,0);double d=net.spell_power.api.SpellPower.getSpellPower(spell.value().school,player).nonCriticalValue();
  Melee.performAttackAgainstTargets(player,new Melee.AttackContext(spell.unwrapKey().orElseThrow().identifier(),spell.value().deliver.melee.attacks.getFirst().id,1),new int[]{target.getId()});eq(out,"Mutilate stays at 100% while ready",1000-target.getHealth(),d);eq(out,"Mutilate preserves readiness",player.hasEffect(ready)?1:0,1);target.discard();
  // Exercise full-strength native Player.attack, without a running spell skill.
  ((SpellCaster.Player)player).setActiveMeleeSkill(null);
  var access=(net.spell_engine.mixin.thirdtier.RogueAttackAccess)player;access.rogueAttackStrength(100);access.rogueSwapStrength(100);player.setOnGround(true);player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
  target=RogueTierTwoProbe.mob("minecraft:villager",1,80,0);player.attack(target);eq(out,"twenty damage basic hit becomes twenty five",1000-target.getHealth(),25);eq(out,"basic hit consumes readiness",player.hasEffect(ready)?1:0,0);
  target.invulnerableTime=0;access.rogueAttackStrength(100);access.rogueSwapStrength(100);float after=target.getHealth();player.attack(target);eq(out,"second basic attack has no bonus",after-target.getHealth(),20);target.discard();
  SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));eq(out,"cooldown blocks immediate rearming",player.hasEffect(ready)?1:0,0);
  cd.remove(counter.unwrapKey().orElseThrow().identifier());SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));eq(out,"can rearm after cooldown",player.hasEffect(ready)?1:0,1);
  // A ready charge cannot stack even when another evasion succeeds after cooldown.
  cd.remove(counter.unwrapKey().orElseThrow().identifier());SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(enemy));eq(out,"readiness never stacks",player.getEffect(ready).getAmplifier(),0);
  var haste=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:haste")).orElseThrow());double beforeHaste=haste.getBaseValue();haste.setBaseValue(100);double base=net.spell_engine.internals.SpellParameters.getCooldownDuration(player,counter);haste.setBaseValue(200);eq(out,"counter cooldown follows haste",net.spell_engine.internals.SpellParameters.getCooldownDuration(player,counter),base/2);haste.setBaseValue(beforeHaste);
  // Party-protected basic hits preserve readiness; no damage can leak through the buff.
  var protectedPet=(net.minecraft.world.entity.animal.wolf.Wolf)RogueTierTwoProbe.mob("minecraft:wolf",1,80,0);protectedPet.setOwnerReference(EntityReference.of(player.getUUID()));access.rogueAttackStrength(100);access.rogueSwapStrength(100);player.attack(protectedPet);eq(out,"protected hit preserves charge",player.hasEffect(ready)?1:0,1);eq(out,"protected hit has no damage",protectedPet.getHealth(),1000);protectedPet.discard();
  player.removeEffect(ready);cd.remove(counter.unwrapKey().orElseThrow().identifier());var pet=(net.minecraft.world.entity.animal.wolf.Wolf)RogueTierTwoProbe.mob("minecraft:wolf",3,80,0);pet.setOwnerReference(EntityReference.of(player.getUUID()));SpellTriggers.onEvasion(player,5,world.damageSources().mobAttack(pet));eq(out,"owned pet cannot ready counter",player.hasEffect(ready)?1:0,0);pet.discard();enemy.discard();player.removeAllEffects();
 }
}
