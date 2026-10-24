package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.server.level.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
public class DualProbe {
 static void eq(Consumer<String> out,String label,double actual,double expected){if(Math.abs(actual-expected)>.02)throw new AssertionError(label+" actual="+actual+" expected="+expected);out.accept("Dual PASS "+label+" "+actual);}
 public static void run(ServerLevel world,ServerPlayer player,Consumer<String> out)throws Exception{
  RogueTierTwoProbe.world=world;RogueTierTwoProbe.player=player;player.removeAllEffects();RogueTierTwoProbe.equip(List.of(),List.of());var attack=player.getAttribute(Attributes.ATTACK_DAMAGE);double original=attack.getBaseValue();attack.setBaseValue(20);player.setPos(0,80,0);
  try{
   double dual=SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE_DUAL,player).nonCriticalValue();eq(out,"dual differs from main-hand power",dual,40);
   for(String id:List.of("rogues:slice_and_dice","skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1","skill_tree_rpgs:rogue_tier_3_spell_1_modifier_2")){
    var spell=RogueTierTwoProbe.spell(id);eq(out,"dual school "+id,spell.value().school==net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE_DUAL?1:0,1);
    var target=RogueTierTwoProbe.mob("minecraft:villager",0,80,3);
    var execution=spell;
    if(id.endsWith("modifier_2")){RogueTierTwoProbe.equip(List.of(),List.of(id));execution=RogueTierTwoProbe.spell("rogues:shadow_step");}
    var context=new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(execution.value().school,dual,0,1.5),SpellTarget.FocusMode.DIRECT,0);
    SpellImpacts.performImpacts(world,player,target,target,execution,execution.value().impacts,context,false,null);
    eq(out,"native dual damage "+id,1000-target.getHealth(),dual*(id.contains("slice")?.08:id.contains("tier_2")?.4:.3));target.discard();player.removeAllEffects();
   }
   RogueTierTwoProbe.equip(List.of(),List.of());var fracture=RogueTierTwoProbe.spell("skill_tree_rpgs:rogue_tier_1_passive_2");var target=RogueTierTwoProbe.mob("minecraft:villager",0,80,3);target.getAttribute(Attributes.ARMOR).setBaseValue(20);
   for(int i=1;i<=4;i++){
    var context=new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(fracture.value().school,dual,0,1.5),SpellTarget.FocusMode.DIRECT,0);
    SpellImpacts.performImpacts(world,player,target,target,fracture,fracture.value().impacts,context,false,null);eq(out,"Fracture stack "+i,target.getAttributeValue(Attributes.ARMOR),20-(2+.05*dual)*Math.min(i,3));
   }
   target.removeAllEffects();eq(out,"Fracture restores armor",target.getAttributeValue(Attributes.ARMOR),20);target.discard();
   player.removeAllEffects();var ready=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:prepared_counter_ready")).orElseThrow();player.addEffect(new MobEffectInstance(ready,200));
   target=RogueTierTwoProbe.mob("minecraft:villager",0,80,3);var access=(net.spell_engine.mixin.thirdtier.RogueAttackAccess)player;access.rogueAttackStrength(100);access.rogueSwapStrength(100);player.setOnGround(true);player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);player.attack(target);
   eq(out,"Prepared Counter native basic hit",1000-target.getHealth(),20+.15*dual);eq(out,"Counter consumes readiness",player.hasEffect(ready)?1:0,0);target.discard();
  }finally{player.removeAllEffects();attack.setBaseValue(original);player.setHealth(player.getMaxHealth());}
 }
}
