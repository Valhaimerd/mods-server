package net.spell_engine.internals;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.paladins.entity.BarrierEntity;
import net.spell_engine.api.entity.SpellEntity;
import net.spell_engine.api.spell.Spell;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.level.LevelEvent;

/** No world blocks or broad entity scans: only registered active boundaries are tested on movement. */
@EventBusSubscriber(modid="spell_engine")
public final class PurgatoryBoundary {
 private static final Map<Level,CopyOnWriteArrayList<BarrierEntity>> ACTIVE=new ConcurrentHashMap<>();
 private static final Map<LivingEntity,BarrierEntity> OWNED=Collections.synchronizedMap(new WeakHashMap<>());
 public static List<BarrierEntity> active(Level level){var list=ACTIVE.get(level);return list==null?List.of():list.stream().filter(PurgatoryBoundary::is).toList();}
 public static boolean is(Entity e){return e instanceof BarrierEntity b&&b.getSpellEntry()!=null&&b.getSpellEntry().unwrapKey().orElseThrow().identifier().getPath().startsWith("purgatory_boundary");}
 @SubscribeEvent public static void joined(EntityJoinLevelEvent event){if(event.getEntity() instanceof BarrierEntity b)ACTIVE.computeIfAbsent(event.getLevel(),k->new CopyOnWriteArrayList<>()).addIfAbsent(b);}
 @SubscribeEvent public static void left(EntityLeaveLevelEvent event){var list=ACTIVE.get(event.getLevel());if(list!=null)list.remove(event.getEntity());}
 @SubscribeEvent public static void unloaded(LevelEvent.Unload event){ACTIVE.remove(event.getLevel());synchronized(OWNED){for(var owner:List.copyOf(OWNED.keySet()))if(owner.level()==event.getLevel())stop(owner);}}
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void teleported(EntityTeleportEvent event){if(!event.isCanceled()&&event.getEntity() instanceof LivingEntity e&&OWNED.containsKey(e)&&(event.getTargetLevel()!=e.level()||event.getTarget().distanceToSqr(e.position())>.0001))PaladinUltimates.end(e,PaladinUltimates.End.CLEANUP);}
 @SubscribeEvent public static void ticking(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event){tick(event.getEntity());}
 public static double radius(BarrierEntity b){return b.getSpellEntry().value().range;}
 public static void start(LivingEntity caster){
  stop(caster);if(caster.level().isClientSide())return;var c=PaladinUltimates.channelOf(caster);if(c==null)return;
  var id=c.outer==14?"paladins:purgatory_boundary_improved":"paladins:purgatory_boundary";
  var spell=PaladinRevamp.spell(caster,id);var type=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("paladins:barrier"));
  var entity=type.create(caster.level(),EntitySpawnReason.TRIGGERED);if(!(entity instanceof BarrierEntity b))throw new IllegalStateException("Priest Barrier entity missing");
  b.onSpawnedBySpell(new SpellEntity.Spawned.Args(caster,spell,newSpawn(),new SpellExecution.ImpactContext().channeled(1)));
  b.setPos(caster.position());caster.level().addFreshEntity(b);OWNED.put(caster,b);
 }
 private static Spell.Impact.Action.Spawn newSpawn(){var s=new Spell.Impact.Action.Spawn();s.time_to_live_seconds=31;return s;}
 public static void stop(LivingEntity caster){var b=OWNED.remove(caster);if(b!=null)b.discard();}
 public static void tick(Entity e){if(!(e instanceof BarrierEntity b)||!is(b))return;ACTIVE.computeIfAbsent(e.level(),k->new CopyOnWriteArrayList<>()).addIfAbsent(b);if(e.level().isClientSide())return;var owner=b.getOwner();if(owner==null||!owner.isAlive()||owner.level()!=b.level()||PaladinUltimates.channelOf(owner)==null)b.discard();}
 public static void firstPull(LivingEntity caster,PaladinUltimates.Channel channel,int pulse){
  if(pulse!=1)return;
  for(var e:PriestRules.nearby(caster,caster.position(),channel.outer))if(PartyRules.harmfulAllowed(caster,e)){
   var d=caster.position().subtract(e.position()).multiply(1,0,1);if(d.length()<1)continue;
   double speed=Math.min(1.6,d.length()-.9)*Math.max(0,1-e.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
   e.setDeltaMovement(d.normalize().scale(speed).add(0,e.getDeltaMovement().y,0));e.needsSync=true;if(e instanceof net.minecraft.server.level.ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(e));
  }
 }
 public static Vec3 clip(Entity e,Vec3 delta){
  if(!(e instanceof LivingEntity living)||e.isSpectator()||e.noPhysics)return delta;var list=ACTIVE.get(e.level());if(list==null||list.isEmpty())return delta;
  Vec3 result=delta;
  for(var b:list){var owner=b.getOwner();if(!is(b)||b.isRemoved()||!b.isAlive()||owner==null||!owner.isAlive()||PartyRules.allies(owner,e))continue;
   if(!e.level().isClientSide()&&PaladinUltimates.channelOf(owner)==null)continue;
   result=clipCircle(e.position(),result,b.position().add(0,-5,0),radius(b),10,e.getBbWidth()/2.0,e.getBbHeight());
  }return result;
 }
 public static Vec3 clipCircle(Vec3 start,Vec3 delta,Vec3 center,double radius,double height,double halfWidth,double bodyHeight){
  double x=start.x-center.x,z=start.z-center.z,a=delta.x*delta.x+delta.z*delta.z;if(a<1e-12)return delta;
  double initial=x*x+z*z,after=(x+delta.x)*(x+delta.x)+(z+delta.z)*(z+delta.z);
  boolean inside=initial<=radius*radius;double margin=halfWidth*Math.sqrt(2),edge=Math.max(.05,radius+(inside?-margin:margin));
  if((inside&&initial>edge*edge&&after>=initial)||(!inside&&initial<edge*edge&&after<=initial)){if(start.y>=center.y+height||start.y+bodyHeight<=center.y)return delta;return new Vec3(0,delta.y,0);}
  double b=2*(x*delta.x+z*delta.z),c=x*x+z*z-edge*edge,disc=b*b-4*a*c;if(disc<0)return delta;
  double t=(-b+(inside?1:-1)*Math.sqrt(disc))/(2*a);
  if(t<0||t>1)return delta;double y=start.y+delta.y*t;if(y>=center.y+height||y+bodyHeight<=center.y)return delta;
  double safe=Math.max(0,t-.0001);return new Vec3(delta.x*safe,delta.y,delta.z*safe);
 }
}
