package com.emandev.bifrost;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.spell_engine.api.effect.EntityActionsAllowed;
import net.spell_engine.internals.casting.SpellCaster;

/** Server clock owns impact snapshots; visual interpolation never owns a transition. */
final class BifrostTeleportManager {
    static final int CHARGE_TICKS=60, SOURCE_DESCENT_TICKS=14, TARGET_DESCENT_TICKS=25,
        ARRIVAL_ANIMATION_TICKS=40, COOLDOWN_TICKS=100;
    static final double BEAM_RADIUS=6, BEAM_HEIGHT=72;
    private static final Map<UUID,Pending> PENDING=new ConcurrentHashMap<>();
    private static final Map<UUID,Long> COOLDOWNS=new HashMap<>();
    private static final Set<UUID> CAPTURED=new HashSet<>();
    private static final List<Arrival> ARRIVALS=new ArrayList<>();
    private static int cleanup;
    private static final class Pending {
        final ServerPlayer actor; final ServerLevel level; final BifrostMarkerData.Marker target;
        final ItemStack focus;
        final Vec3 source,destination; final TransitVisuals sourceBeam,targetBeam;
        final List<Group> groups=new ArrayList<>(); final Set<UUID> claims=new HashSet<>();
        int age;
        Pending(ServerPlayer p,BifrostMarkerData.Marker t) {
            actor=p;level=p.level();target=t;focus=p.getMainHandItem().copy();source=p.position();destination=Vec3.atBottomCenterOf(t.position().above());
            sourceBeam=new TransitVisuals(level,source);targetBeam=new TransitVisuals(level,destination);
        }
    }
    private record Rider(UUID id,UUID vehicle,Vec3 offset) {}
    private record Group(UUID root,List<Rider> members) {}
    private record Arrival(ServerLevel level,Vec3 position,TransitVisuals beam,int age) {}
    private BifrostTeleportManager() {}
    static void start(MinecraftServer server) { stop(server); }
    static void stop(MinecraftServer server) {
        for(var p:PENDING.values()){p.sourceBeam.close();p.targetBeam.close();}
        for(var a:ARRIVALS)a.beam.close();
        PENDING.clear();ARRIVALS.clear();CAPTURED.clear();COOLDOWNS.clear();cleanup=0;
        TransitVisuals.cleanup(server,Set.of());
    }
    static boolean begin(ServerPlayer player,BifrostMarkerData.Marker target,String activatorName) {
        if(!AsgardianAccess.allow(player))return false;
        if(!WeaponOwnership.canTravel(player))return fail(player,"Only the bound owner can travel with this weapon; it must be available in inventory.");
        if(!travelAllowed(player))return fail(player,"Finish your current action before opening the Bifrost; you cannot travel while stunned.");
        if(!target.dimension().equals(player.level().dimension().identifier()))return fail(player,"Cross-dimension Bifrost travel is not enabled yet.");
        if(PENDING.containsKey(player.getUUID())||CAPTURED.contains(player.getUUID()))return fail(player,"You are already in a Bifrost transit.");
        long remaining=BifrostRules.remainingCooldownSeconds(player.level().getGameTime(),COOLDOWNS.getOrDefault(player.getUUID(),0L));
        if(remaining>0)return fail(player,"The Bifrost is recharging for "+remaining+" more second(s).");
        // Preserve native long-distance markers: load just their one target chunk, never a landing region.
        player.level().getChunk(target.position().getX()>>4,target.position().getZ()>>4);
        if(!isValidDestination(player.level(),target.position()))return fail(player,"The selected marker is missing, unloaded or obstructed.");
        if(BifrostRules.isStandingAt(player.getX(),player.getY(),player.getZ(),target.position().getX(),target.position().getY(),target.position().getZ()))return fail(player,"You are already at "+target.name()+".");
        var pending=new Pending(player,target);PENDING.put(player.getUUID(),pending);
        tell(player,activatorName+" is opening the Bifrost to "+target.name()+"...");
        TransitSounds.phase(pending.level,pending.source,TransitSounds.CHARGE,List.of(player));
        TransitSounds.phase(pending.level,pending.source,TransitSounds.DESCENT,List.of(player));
        TransitSounds.phase(pending.level,pending.destination,TransitSounds.DESCENT,List.of());
        return true;
    }
    static void tick(MinecraftServer server) {
        for(var level:server.getAllLevels())BifrostMenuService.tick(level);
        // Process old arrivals first, so a new arrival owns exactly the following 40 ticks.
        tickArrivals();
        for(var entry:List.copyOf(PENDING.entrySet())) {
            var p=entry.getValue();var actor=p.actor;
            if(actor.isRemoved()||!AsgardianAccess.canUse(actor)||!WeaponOwnership.canTravel(actor)||!WeaponOwnership.canUse(actor,p.focus)||!travelAllowed(actor)||actor.level()!=p.level||!isValidDestination(p.level,p.target.position())) {
                cancel(p,"Bifrost transit cancelled because its caster or destination became unavailable.");continue;
            }
            ++p.age;
            if(p.age<=SOURCE_DESCENT_TICKS)p.sourceBeam.descend(p.age/(double)SOURCE_DESCENT_TICKS);
            else if(p.age<SOURCE_DESCENT_TICKS+ARRIVAL_ANIMATION_TICKS)p.sourceBeam.retract((p.age-SOURCE_DESCENT_TICKS)/(double)ARRIVAL_ANIMATION_TICKS);
            else if(p.age==SOURCE_DESCENT_TICKS+ARRIVAL_ANIMATION_TICKS)p.sourceBeam.close();
            p.targetBeam.descend(Math.min(1,p.age/(double)TARGET_DESCENT_TICKS));
            if(p.age==SOURCE_DESCENT_TICKS) {
                capture(p);
                TransitVisuals.impact(p.level,p.source);
                TransitSounds.phase(p.level,p.source,TransitSounds.IMPACT,players(p));
                TransitSounds.phase(p.level,p.source,TransitSounds.CLOSURE,players(p));
            }
            if(p.age==TARGET_DESCENT_TICKS) {
                TransitVisuals.impact(p.level,p.destination);
                TransitSounds.phase(p.level,p.destination,TransitSounds.IMPACT,List.of());
            }
            if(p.age%4==0){if(p.age<SOURCE_DESCENT_TICKS+ARRIVAL_ANIMATION_TICKS)p.sourceBeam.particles(p.age,p.age>SOURCE_DESCENT_TICKS);p.targetBeam.particles(p.age,false);}
            if(p.age==CHARGE_TICKS)complete(p);
        }
        if(++cleanup>=200) {
            cleanup=0;COOLDOWNS.entrySet().removeIf(e->e.getValue()<=server.overworld().getGameTime());
            var live=new HashSet<UUID>();for(var p:PENDING.values()){live.addAll(p.sourceBeam.ids());live.addAll(p.targetBeam.ids());}for(var a:ARRIVALS)live.addAll(a.beam.ids());
            TransitVisuals.cleanup(server,live);
        }
    }
    private static boolean travelAllowed(ServerPlayer player) {
        return !AsgardAbilities.active(player)&&!EntityActionsAllowed.isImpaired(player,EntityActionsAllowed.Player.CAST_SPELL)
            &&((SpellCaster.Player)player).getSpellCastProcess()==null;
    }
    private static void tickArrivals() {
        for(int i=ARRIVALS.size()-1;i>=0;i--) {
            var a=ARRIVALS.get(i);int age=a.age+1;
            if(age>=ARRIVAL_ANIMATION_TICKS){a.beam.close();ARRIVALS.remove(i);}
            else {a.beam.retract(age/(double)ARRIVAL_ANIMATION_TICKS);if(age%4==0)a.beam.particles(age,true);ARRIVALS.set(i,new Arrival(a.level,a.position,a.beam,age));}
        }
    }
    /** Circle intersects the horizontal AABB, not merely the entity center. */
    static boolean intersects(AABB b,Vec3 c) {
        double dx=Math.max(b.minX,Math.min(c.x,b.maxX))-c.x,dz=Math.max(b.minZ,Math.min(c.z,b.maxZ))-c.z;
        return dx*dx+dz*dz<=BEAM_RADIUS*BEAM_RADIUS&&b.maxY>=c.y&&b.minY<=c.y+BEAM_HEIGHT;
    }
    static boolean eligible(Entity e) {
        return e.isAlive()&&!e.isRemoved()&&!e.isSpectator()&&!e.entityTags().contains(TransitVisuals.TAG)
            &&(!(e instanceof Player p)||!AsgardianAccess.downed(p))
            &&(e instanceof LivingEntity||e instanceof AbstractBoat||e instanceof AbstractMinecart);
    }
    private static void capture(Pending p) {
        var roots=new LinkedHashSet<Entity>();roots.add(p.actor.getRootVehicle());
        var volume=new AABB(p.source.x-6,p.source.y,p.source.z-6,p.source.x+6,p.source.y+72,p.source.z+6);
        for(var e:p.level.getEntities((Entity)null,volume,e->eligible(e)&&intersects(e.getBoundingBox(),p.source)))roots.add(e.getRootVehicle());
        for(var root:roots) {
            var members=root.getSelfAndPassengers().toList();
            if(members.stream().anyMatch(e->!eligible(e)||CAPTURED.contains(e.getUUID())||PENDING.containsKey(e.getUUID())&&e!=p.actor))continue;
            var riders=new ArrayList<Rider>();
            for(var e:members) {
                riders.add(new Rider(e.getUUID(),e.getVehicle()==null?null:e.getVehicle().getUUID(),e.position().subtract(root.position())));
                CAPTURED.add(e.getUUID());p.claims.add(e.getUUID());
            }
            p.groups.add(new Group(root.getUUID(),List.copyOf(riders)));
        }
    }
    private static void complete(Pending p) {
        var occupied=new ArrayList<AABB>();
        for(var group:p.groups) {
            var members=new ArrayList<Entity>();boolean valid=true;
            for(var rider:group.members) {var e=p.level.getEntity(rider.id);if(e==null||!eligible(e)){valid=false;break;}members.add(e);}
            if(!valid){notifyGroup(p,group,"A passenger became unavailable; your group remains at the source.");continue;}
            var root=p.level.getEntity(group.root);if(root==null)continue;
            // If a captured member boarded an unrelated vehicle after impact, do not move that group.
            if(members.stream().anyMatch(e->e.getVehicle()!=null&&!group.members.stream().anyMatch(r->r.id.equals(e.getVehicle().getUUID()))))continue;
            var landing=landing(p,root,group,members,occupied);
            if(landing==null){notifyGroup(p,group,"No protected, loaded safe landing is available; your group remains at the source.");continue;}
            // Preflight every protection event before changing any member or its riding relation.
            var moves=new ArrayList<Vec3>();
            for(int i=0;i<members.size();i++) {
                var dest=landing.add(group.members.get(i).offset);var e=members.get(i);
                var event=new EntityTeleportEvent(e,p.level,dest.x,dest.y,dest.z);NeoForge.EVENT_BUS.post(event);
                if(event.isCanceled()||event.getTarget().distanceToSqr(dest)>0.000001){valid=false;break;}moves.add(dest);
            }
            if(!valid){notifyGroup(p,group,"A protection rule prevented this group's Bifrost travel.");continue;}
            for(var e:members)e.stopRiding();
            for(int i=0;i<members.size();i++){var e=members.get(i);var dest=moves.get(i);e.teleportTo(dest.x,dest.y,dest.z);e.setDeltaMovement(Vec3.ZERO);e.resetFallDistance();}
            var moved=new HashMap<UUID,Entity>();for(var e:members)moved.put(e.getUUID(),e);
            for(var rider:group.members)if(rider.vehicle!=null){var e=moved.get(rider.id);var vehicle=moved.get(rider.vehicle);if(e!=null&&vehicle!=null)e.startRiding(vehicle,true,true);}
            for(var e:members)occupied.add(e.getBoundingBox());
            for(var e:members)if(e instanceof ServerPlayer player){COOLDOWNS.put(player.getUUID(),p.level.getGameTime()+COOLDOWN_TICKS);tell(player,"Bifrost transit complete. Arrived at "+p.target.name()+".");}
        }
        TransitSounds.phase(p.level,p.destination,TransitSounds.CLOSURE,players(p));
        p.sourceBeam.close(); // Already closed at tick 54; idempotent on cancellation or completion.
        ARRIVALS.add(new Arrival(p.level,p.destination,p.targetBeam,0));
        COOLDOWNS.put(p.actor.getUUID(),p.level.getGameTime()+COOLDOWN_TICKS);
        release(p);
    }
    private static Vec3 landing(Pending p,Entity root,Group g,List<Entity> members,List<AABB> occupied) {
        for(int ring=0;ring<=6;ring++)for(int n=0;n<(ring==0?1:ring*12);n++) {
            double angle=ring==0?0:n*Math.PI*2/(ring*12);
            for(int dy:new int[]{0,1,-1,2,-2,3,-3}) {
                var candidate=p.destination.add(ring*Math.cos(angle),dy,ring*Math.sin(angle));boolean safe=true;
                for(int i=0;i<members.size();i++) {
                    var e=members.get(i);var dest=candidate.add(g.members.get(i).offset);var delta=dest.subtract(e.position());var box=e.getBoundingBox().move(delta);
                    var floor=BlockPos.containing(dest).below();
                    if(!p.level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))
                        ||!p.level.getWorldBorder().isWithinBounds(box)||!e.mayInteract(p.level,BlockPos.containing(dest))||!p.level.mayInteract(e,BlockPos.containing(dest))
                        ||!p.level.noCollision(e,box)||p.level.containsAnyLiquid(box)||hazards(p.level,box)||occupied.stream().anyMatch(box::intersects)) {safe=false;break;}
                    if(e==root&&(!p.level.getBlockState(floor).isSolid()||p.level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||p.level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.CAMPFIRE)||p.level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)||p.level.getBlockState(floor).is(net.minecraft.world.level.block.Blocks.CACTUS))) {safe=false;break;}
                }
                if(safe)return candidate;
            }
        }
        return null;
    }
    private static boolean hazards(ServerLevel level,AABB box) {
        for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))) {
            var state=level.getBlockState(pos);
            if(state.is(net.minecraft.tags.BlockTags.FIRE)||state.is(net.minecraft.world.level.block.Blocks.WITHER_ROSE)
                ||state.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH)||state.is(net.minecraft.world.level.block.Blocks.POWDER_SNOW))return true;
        }
        return false;
    }
    private static List<ServerPlayer> players(Pending p) {
        var result=new ArrayList<ServerPlayer>();for(var id:p.claims)if(p.level.getEntity(id) instanceof ServerPlayer player)result.add(player);return result;
    }
    private static void notifyGroup(Pending p,Group g,String text){for(var r:g.members)if(p.level.getEntity(r.id) instanceof ServerPlayer player)tell(player,text);}
    private static void release(Pending p){CAPTURED.removeAll(p.claims);PENDING.remove(p.actor.getUUID());}
    private static void cancel(Pending p,String message){tell(p.actor,message);p.sourceBeam.close();p.targetBeam.close();release(p);}
    private static boolean isValidDestination(ServerLevel level,BlockPos pos) {
        return level.hasChunkAt(pos)&&level.getBlockState(pos).is(ModBlocks.BIFROST_MARKER.get())&&level.getBlockState(pos.above()).isAir()&&level.getBlockState(pos.above(2)).isAir();
    }
    private static boolean fail(ServerPlayer p,String text){tell(p,text);return false;}
    private static void tell(ServerPlayer p,String text){p.sendSystemMessage(Component.literal(text));}
}
