package local.audit;
import java.util.*;
import net.minecraft.world.entity.Entity;
import net.spell_engine.entity.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;
public class MoteProbe {
 public static void run(PriestProbe t){
  UltimatesProbe.a=t;TuningProbe.a=t;t.p.setPos(8,80,8);t.p.setHealth(t.p.getMaxHealth());t.equip();
  var well=UltimatesProbe.summon();var target=TuningProbe.pet(2,8,9);var scheduler=(WorldScheduler)t.world;
  var spell=t.spell("paladins:lightwell_orb");var context=t.context(spell,0).effectiveCaster(t.p);
  var prior=t.world.getEntitiesOfClass(SpellProjectile.class,well.getBoundingBox().inflate(30)).stream().map(Entity::getId).toList();
  java.util.function.Supplier<List<SpellProjectile>> shots=()->t.world.getEntitiesOfClass(SpellProjectile.class,well.getBoundingBox().inflate(30),e->!prior.contains(e.getId()));
  long due=scheduler.getSchedulerTime()+6;
  PriestTuning.shoot(t.world,well,target,spell,context);
  t.eq("Single target launches only first mote immediately",shots.get().size(),1);
  t.eq("Second mote scheduled exactly six ticks later",scheduler.getScheduledTasks().containsKey(due)?1:0,1);
  t.eq("No early second projectile before due time",shots.get().size(),1);
  List.copyOf(scheduler.getScheduledTasks().remove(due)).forEach(Runnable::run);
  t.eq("Six-tick task launches second mote",shots.get().size(),2);
  t.eq("Both spaced motes retain the same target",shots.get().stream().allMatch(e->e.getFollowedTarget()==target)?1:0,1);shots.get().forEach(Entity::discard);
  var other=TuningProbe.pet(3,8,10);PriestTuning.shoot(t.world,well,target,spell,context);
  t.eq("Two injured allies still launch simultaneously",shots.get().size(),2);
  t.eq("Two injured allies keep distinct projectile targets",shots.get().stream().map(SpellProjectile::getFollowedTarget).distinct().count(),2);shots.get().forEach(Entity::discard);other.discard();
  PriestTuning.shoot(t.world,well,target,spell,context);shots.get().forEach(Entity::discard);target.discard();
  List.copyOf(scheduler.getScheduledTasks().remove(due)).forEach(Runnable::run);
  t.eq("Removed target cancels pending second mote",shots.get().size(),0);
  var replacement=TuningProbe.pet(2,8,9);PriestTuning.shoot(t.world,well,replacement,spell,context);shots.get().forEach(Entity::discard);well.discard();
  List.copyOf(scheduler.getScheduledTasks().remove(due)).forEach(Runnable::run);
  t.eq("Removed well cancels pending second mote",shots.get().size(),0);replacement.discard();t.equip();
 }
}
