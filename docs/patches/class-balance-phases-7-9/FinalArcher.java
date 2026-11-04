package net.spell_engine.internals;
import java.util.*;import net.minecraft.world.entity.*;import net.minecraft.resources.Identifier;import net.spell_engine.entity.*;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;import net.neoforged.neoforge.event.level.LevelEvent;
public final class FinalArcher {
 static final Map<net.minecraft.world.level.Level,Map<UUID,Deque<SpellCloud>>> CLOUDS=new WeakHashMap<>();static boolean initialized;
 public static void init(){if(initialized)return;initialized=true;NeoForge.EVENT_BUS.addListener(FinalArcher::joined);NeoForge.EVENT_BUS.addListener((LevelEvent.Unload e)->CLOUDS.remove(e.getLevel()));}
 static void joined(EntityJoinLevelEvent event){if(event.getLevel().isClientSide()||!(event.getEntity() instanceof SpellCloud cloud)||cloud.getSpellEntry()==null||!cloud.getSpellEntry().is(Identifier.parse("archers:entangling_roots"))||cloud.getOwner()==null)return;
  var queue=CLOUDS.computeIfAbsent(event.getLevel(),l->new HashMap<>()).computeIfAbsent(cloud.getOwner().getUUID(),u->new ArrayDeque<>());queue.removeIf(Entity::isRemoved);if(queue.contains(cloud))return;while(queue.size()>=2)queue.removeFirst().discard();queue.addLast(cloud);
 }
 public static Entity launched(Entity e){if(e instanceof SpellProjectile projectile&&net.spell_engine.entity.ArcherProjectileRules.conjured(projectile))projectile.setDeltaMovement(projectile.getDeltaMovement().scale(1.3));return e;}
}
