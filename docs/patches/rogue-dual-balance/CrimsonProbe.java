package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.server.level.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.spell_engine.internals.delivery.melee.Melee;
import net.spell_engine.internals.container.SpellContainerSource;
public class CrimsonProbe {
 static void eq(Consumer<String> out,String key,double actual,double expected){if(Math.abs(actual-expected)>.02)throw new AssertionError(key+" actual="+actual+" expected="+expected);out.accept("Crimson PASS "+key);}
 public static void run(ServerLevel world,ServerPlayer player,Consumer<String> out){
  RogueTierTwoProbe.world=world;RogueTierTwoProbe.player=player;var owner=(SpellContainerSource.Owner)player;var saved=owner.getSpellContainers();var attack=player.getAttribute(Attributes.ATTACK_DAMAGE);double base=attack.getBaseValue();
  try{
   player.removeAllEffects();attack.setBaseValue(20);RogueTierTwoProbe.equip(List.of(),List.of("skill_tree_rpgs:rogue_tier_4_spell_2_modifier_1"));var spell=RogueTierTwoProbe.spell("rogues:mutilate");double dual=net.spell_power.api.SpellPower.getSpellPower(spell.value().school,player).nonCriticalValue();player.setPos(0,80,0);
   for(int count:new int[]{0,1,3}){
    var victims=new ArrayList<net.minecraft.world.entity.LivingEntity>();for(int i=0;i<count;i++)victims.add(RogueTierTwoProbe.mob("minecraft:villager",.5+i*.3,80,1));
    player.setHealth(1);Melee.performAttackAgainstTargets(player,new Melee.AttackContext(Identifier.parse("rogues:mutilate"),spell.value().deliver.melee.attacks.getFirst().id,1),victims.stream().mapToInt(e->e.getId()).toArray());eq(out,"heal count="+count,player.getHealth()-1,count==0?0:dual*.15);
    victims.forEach(net.minecraft.world.entity.Entity::discard);
   }
   var invulnerable=RogueTierTwoProbe.mob("minecraft:villager",0,80,1);invulnerable.setInvulnerable(true);player.setHealth(1);Melee.performAttackAgainstTargets(player,new Melee.AttackContext(Identifier.parse("rogues:mutilate"),spell.value().deliver.melee.attacks.getFirst().id,1),new int[]{invulnerable.getId()});eq(out,"rejected hit gives no heal",player.getHealth(),1);invulnerable.discard();
  }finally{player.removeAllEffects();attack.setBaseValue(base);player.setHealth(player.getMaxHealth());owner.setSpellContainers(saved);owner.spellModifierCache().clear();}
 }
}
