package local.audit;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.*;
import net.spell_engine.entity.ArcherProjectileRules;
import net.spell_engine.internals.*;
import net.spell_engine.internals.delivery.*;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
import static local.audit.TierTwoProbe.*;
public class ArcherRevisionProbe {
 static void check(String label,boolean ok){eq(label,ok?1:0,1);}
 static SpellExecution.ImpactContext context(Holder<Spell> h){return new SpellExecution.ImpactContext(1,1,null,new SpellPower.Result(h.value().school,40,0,1.5),SpellTarget.FocusMode.DIRECT,0);}
 static SpellProjectile arrow(Holder<Spell> h){return new SpellProjectile(level,player,0,81,0,SpellProjectile.Behaviour.FLY,h,context(h),h.value().deliver.projectile.projectile.perks.copy());}
 public static void run(ServerLevel world,ServerPlayer owner,Consumer<String> sink)throws Exception{
  var rain=spell("archers:rain_of_arrows");var magic=spell("archers:magic_arrow");var power=spell("archers:power_shot");
  eq("Power cooldown",power.value().cost.cooldown.duration,12);eq("Mark six seconds",power.value().impacts.getFirst().action.status_effect.duration,6);
  var stash=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("archers:hunters_mark_stash")).orElseThrow();
  for(boolean improved:new boolean[]{false,true}){
   player.removeAllEffects();equip(List.of(),improved?List.of("skill_tree_rpgs:archer_tier_2_spell_1_root"):List.of());
   SpellDelivery.deliver(level,power,player,List.of(new SpellExecution.DeliveryTarget(player,context(power))),context(power),null,null);
   eq("Native prepared arrow window improved="+improved,player.getEffect(stash).getDuration(),improved?200:160);
  }
  player.removeAllEffects();
  eq("Rain cooldown",rain.value().cost.cooldown.duration,25);eq("Rain damage",rain.value().impacts.getFirst().action.damage.spell_power_coefficient,.4);eq("Rain splash",rain.value().area_impact.radius,2.5);
  for(int branch=0;branch<3;branch++){
   equip(List.of(),branch==0?List.of():List.of("skill_tree_rpgs:archer_tier_4_spell_1_modifier_"+branch));
   var meteor=rain.value().deliver.meteor;var launch=meteor.launch_properties.copy();float radius=meteor.launch_radius;
   for(var m:SpellModifiers.of(player,rain)){if(m.projectile_launch!=null)launch.mutatingCombine(m.projectile_launch);radius+=m.meteor_launch_radius_add;}
   eq("Rain branch "+branch+" count",1+launch.extra_launch_count,branch==2?76:32);eq("Rain branch "+branch+" ticks",launch.extra_launch_delay,branch==1?2:5);eq("Rain branch "+branch+" scatter",radius,branch==2?11:10);
  }
  double sum=0;int center=0;for(int i=0;i<1000;i++){
   Vec3 offset=ArcherProjectileRules.scatter(new Vec3(10,0,0).yRot((float)(i*.2)),rain,level);double r2=offset.lengthSqr();check("Scatter inside disk "+i,r2<=100);sum+=r2;if(r2<25)center++;
  }
  check("Scatter fills center, not ring",center>170&&center<330);check("Scatter uniform area mean squared radius",sum/1000>44&&sum/1000<56);
  equip(List.of(),List.of());var target=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(level,EntitySpawnReason.COMMAND);target.setPos(0,80,4);
  var location=ArcherProjectileRules.rainLocation(rain,target,null);check("Rain fixes aim location",location.equals(target.position()));check("Rain removes following target",ArcherProjectileRules.rainTarget(rain,target)==null);
  eq("Magic cooldown",magic.value().cost.cooldown.duration,25);eq("Magic count",1+magic.value().deliver.projectile.launch_properties.extra_launch_count,4);eq("Magic spacing",magic.value().deliver.projectile.launch_properties.extra_launch_delay,5);check("Magic requires no aim target",magic.value().target.type==Spell.Target.Type.NONE);
  check("Magic has charge casting",magic.value().active.cast.type==Spell.Active.Cast.Type.CHARGE);
  eq("Magic full charge one second",magic.value().active.cast.duration,1);
  for(int variant=0;variant<3;variant++){
   equip(List.of(),variant==0?List.of():variant==1?List.of("skill_tree_rpgs:archer_tier_4_spell_2_root"):List.of("skill_tree_rpgs:archer_tier_4_spell_2_root","skill_tree_rpgs:archer_tier_4_spell_2_modifier_2"));
   double multiplier=1;for(var modifier:SpellModifiers.of(player,magic))if(modifier.power_modifier!=null)multiplier+=modifier.power_modifier.power_multiplier;
   double expected=variant==0?1:variant==1?1.1:1.25;
   eq("Magic full-charge damage variant="+variant,multiplier*SpellParameters.chargeOutputMultiplier(magic,1),expected);
   eq("Magic half-charge damage variant="+variant,multiplier*SpellParameters.chargeOutputMultiplier(magic,.5f),expected*.5);
  }
  var follow=SpellProjectile.class.getDeclaredMethod("followTarget");follow.setAccessible(true);
  for(boolean full:new boolean[]{false,true}){
   equip(List.of(),full?List.of("skill_tree_rpgs:archer_tier_4_spell_2_modifier_1"):List.of());var p=arrow(magic);p.setDeltaMovement(new Vec3(1.5,0,0));p.setFollowedTarget(target);follow.invoke(p);
   double angle=Math.toDegrees(Math.acos(p.getDeltaMovement().normalize().x));eq("Native turn full="+full,angle,full?90:5);eq("Native velocity preserved",p.getDeltaMovement().length(),1.5);p.discard();
  }
  target.discard();
  // Exercise real extra-launch scheduling and inspect final native projectile positions.
  var scheduled=(net.spell_engine.utils.WorldScheduler)level;
  var savedTasks=new HashMap<>(scheduled.getScheduledTasks());scheduled.getScheduledTasks().clear();
  List<SpellProjectile> falling=new ArrayList<>(),flying=new ArrayList<>();
  net.spell_engine.api.spell.event.SpellEvents.PROJECTILE_FALL.register(event->falling.add(event.projectile()));
  net.spell_engine.api.spell.event.SpellEvents.PROJECTILE_SHOOT.register(event->flying.add(event.projectile()));
  try{
   for(int branch=0;branch<3;branch++){
    falling.clear();equip(List.of(),branch==0?List.of():List.of("skill_tree_rpgs:archer_tier_4_spell_1_modifier_"+branch));
    var aim=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(level,EntitySpawnReason.COMMAND);aim.setPos(5,80,5);
    ProjectileLauncher.fallProjectile(level,player,aim,null,rain,context(rain));aim.setPos(80,80,80);
    var queue=new TreeMap<>(scheduled.getScheduledTasks());scheduled.getScheduledTasks().clear();
    eq("Native Rain scheduled arrows branch="+branch,queue.values().stream().mapToInt(List::size).sum(),branch==2?75:31);
    long previous=scheduled.getSchedulerTime();for(var entry:queue.entrySet()){eq("Native Rain scheduled gap branch="+branch,entry.getKey()-previous,branch==1?2:5);previous=entry.getKey();entry.getValue().forEach(Runnable::run);}
    eq("Native Rain spawned total branch="+branch,falling.size(),branch==2?76:32);
    for(var p:falling){double dx=p.getX()-5,dz=p.getZ()-5;check("Native Rain stays at fixed disk",dx*dx+dz*dz<=(branch==2?121:100)+.001);check("Native Rain does not follow mob",p.getFollowedTarget()==null);p.discard();}
    aim.discard();
   }
   equip(List.of(),List.of());ProjectileLauncher.shootProjectile(level,player,null,magic,context(magic));
   var queue=new TreeMap<>(scheduled.getScheduledTasks());scheduled.getScheduledTasks().clear();eq("Magic schedules three extra arrows",queue.size(),3);
   long previous=scheduled.getSchedulerTime();for(var entry:queue.entrySet()){eq("Magic scheduled interval",entry.getKey()-previous,5);previous=entry.getKey();entry.getValue().forEach(Runnable::run);}
   eq("Magic spawns four arrows without aim",flying.size(),4);for(var p:flying)p.discard();
  }finally{scheduled.getScheduledTasks().clear();scheduled.getScheduledTasks().putAll(savedTasks);}
  // Real native movement/impact through a solid wall, automatic reacquisition and timed expiry.
  var victim=(LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(level,EntitySpawnReason.COMMAND);victim.setPos(0,80,4);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);victim.getAttribute(Attributes.ARMOR).setBaseValue(0);victim.setHealth(1000);level.addFreshEntity(victim);
  var pet=(net.minecraft.world.entity.animal.wolf.Wolf)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:wolf")).create(level,EntitySpawnReason.COMMAND);pet.setOwnerReference(net.minecraft.world.entity.EntityReference.of(player.getUUID()));pet.setPos(0,80,1);level.addFreshEntity(pet);
  BlockPos wall=new BlockPos(0,81,2);var previous=level.getBlockState(wall);level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);
  try{
   for(boolean improved:new boolean[]{false,true}){
    equip(List.of(),improved?List.of("skill_tree_rpgs:archer_tier_4_spell_2_root","skill_tree_rpgs:archer_tier_4_spell_2_modifier_2"):List.of());victim.invulnerableTime=0;victim.setHealth(1000);var p=arrow(magic);p.setDeltaMovement(new Vec3(0,0,6));p.tick();
    eq("Native wall-passing damage improved="+improved,1000-victim.getHealth(),improved?50:40);check("Piercing arrow survives impact",!p.isRemoved());p.discard();
   }
   equip(List.of(),List.of());var p=arrow(magic);ArcherProjectileRules.beforeTick(p);check("Acquires enemy while ignoring closer owned pet",p.getFollowedTarget()==victim);
   victim.setPos(15,80,15);ArcherProjectileRules.beforeTick(p);check("Acquires enemy between 16 and 32 blocks",p.getFollowedTarget()==victim);
   victim.setPos(0,80,40);ArcherProjectileRules.beforeTick(p);check("Releases target outside vicinity",p.getFollowedTarget()==null);
   victim.setPos(0,80,4);p.setPos(0,81,8);ArcherProjectileRules.beforeTick(p);check("Acquires enemy behind arrow",p.getFollowedTarget()==victim);
   p.tickCount=199;p.setDeltaMovement(Vec3.ZERO);p.tick();check("Alive before ten-second expiry",!p.isRemoved());p.tickCount=200;p.tick();check("Expires after ten seconds",p.isRemoved());
  }finally{level.setBlock(wall,previous,3);victim.discard();pet.discard();equip(List.of(),List.of());player.removeAllEffects();}
 }
}
