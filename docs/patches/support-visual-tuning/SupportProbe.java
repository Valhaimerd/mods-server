package local.audit;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.effect.EntityTints;
import net.spell_engine.entity.LastStandAppearance;
import net.spell_engine.entity.SpellCloud;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
public class SupportProbe {
 public static void run(PriestProbe t)throws Exception{
  t.eq("Last Stand gold tint is fifty percent",LastStandAppearance.GOLD_TINT,EntityTints.scaled(0xFFFFD700,.5f));
  var owner=(SpellContainerSource.Owner)t.p;var saved=owner.getSpellContainers();
  var refuge=t.spell("skill_tree_rpgs:priest_tier_4_spell_2_modifier_1");
  var root=t.spell("skill_tree_rpgs:priest_tier_4_spell_2_root");var barrier=t.spell("paladins:barrier");
  var ttl=SpellCloud.class.getDeclaredField("timeToLive");ttl.setAccessible(true);
  try{
   UltimatesProbe.a=t;TuningProbe.a=t;t.p.setPos(8,80,8);t.p.removeAllEffects();
   t.equip("skill_tree_rpgs:priest_tier_4_spell_1_modifier_1","skill_tree_rpgs:priest_tier_4_spell_1_modifier_2");
   t.p.setHealth(t.p.getMaxHealth());var well=UltimatesProbe.summon();
   var injured=TuningProbe.pet(3,8,9);var full=TuningProbe.pet(8,8,10);
   full.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,100));
   PriestUltimates.wellTick(well);var pair=PriestTuning.pair(well,t.p);
   t.eq("Sole injured ally gets two target slots despite full-health upgrade targets",pair.size(),2);
   t.eq("First mote prioritizes injured ally over full-health cleanse target",pair.getFirst()==injured?1:0,1);
   t.eq("Second mote also prioritizes sole injured ally",pair.get(1)==injured?1:0,1);
   var shots=TuningProbe.cycle(well);t.eq("Native well still launches both motes",shots.size(),2);
   t.eq("Both native motes follow sole injured ally with full-health allies nearby",shots.stream().allMatch(s->s.getFollowedTarget()==injured)?1:0,1);
   shots.forEach(Entity::discard);injured.discard();full.discard();well.discard();
   for(boolean improved:new boolean[]{false,true}){
    var ids=new ArrayList<String>();ids.add("skill_tree_rpgs:priest_tier_4_spell_2_modifier_1");if(improved)ids.add("skill_tree_rpgs:priest_tier_4_spell_2_root");
    var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,ids);
    owner.setSpellContainers(new SpellContainerSource.Result(saved.activeContainer(),List.of(),List.of(refuge),improved?List.of(root):List.of(),List.of(new SpellContainerSource.SourcedContainer("refuge",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();
    var b=UltimatesProbe.barrier();
    var prior=t.world.getEntitiesOfClass(SpellCloud.class,t.p.getBoundingBox().inflate(20)).stream().map(Entity::getId).toList();
    SpellTriggers.onSpellCast(t.p,barrier,List.of(t.p));TuningProbe.tasks();
    var clouds=t.world.getEntitiesOfClass(SpellCloud.class,t.p.getBoundingBox().inflate(20),c->!prior.contains(c.getId())&&c.getSpellEntry().is(net.minecraft.resources.Identifier.parse("skill_tree_rpgs:priest_tier_4_spell_2_modifier_1")));
    t.eq("Native Barrier cast creates one Refuge "+improved,clouds.size(),1);
    t.eq("Barrier stays ten-block radius "+improved,b.getSpellEntry().value().range,10);
    t.eq("Barrier native width is twenty blocks "+improved,b.getBbWidth(),20);
    t.eq("Barrier requested duration "+improved,b.getTimeToLive(),improved?240:200);
    t.eq("Refuge follows Barrier duration "+improved,ttl.getInt(clouds.getFirst()),b.getTimeToLive());
    t.eq("Refuge radius unchanged "+improved,clouds.getFirst().getCloudData().volume.radius,4);
    t.eq("Refuge pulse interval unchanged "+improved,clouds.getFirst().getCloudData().impact_tick_interval,40);
    clouds.forEach(Entity::discard);b.discard();
   }
  }finally{owner.setSpellContainers(saved);owner.spellModifierCache().clear();t.equip();}
 }
}
