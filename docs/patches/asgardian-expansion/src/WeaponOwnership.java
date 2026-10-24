package com.emandev.bifrost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** One world-owned record; rendered/projected stacks are never another authority. */
@EventBusSubscriber(modid="bifrost")
public final class WeaponOwnership {
    public static final String INVENTORY="inventory", FLIGHT="flight", WAITING="waiting", ORBIT="orbit", CRAFTING="crafting";
    private static final String ID="bifrost_weapon_id", OWNER="bifrost_owner", NAME="bifrost_owner_name";
    private static final Map<UUID, Flight> flights=new HashMap<>();
    private static final Set<UUID> spawning=new HashSet<>();
    private static final Map<UUID, ItemEntity> visuals=new HashMap<>();
    private static MinecraftServer activeServer;
    private static boolean internal;
    public record Entry(String id, String owner, String name, String kind, ItemStack stack,
                        String state, String dimension, double x, double y, double z, int slot,
                        Map<String,Long> clocks) {
        public Entry { stack=stack.copy();clocks=new HashMap<>(clocks); }
        static final Codec<Entry> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.STRING.fieldOf("id").forGetter(Entry::id),Codec.STRING.fieldOf("owner").forGetter(Entry::owner),
            Codec.STRING.fieldOf("name").forGetter(Entry::name),Codec.STRING.fieldOf("kind").forGetter(Entry::kind),
            ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack),Codec.STRING.fieldOf("state").forGetter(Entry::state),
            Codec.STRING.fieldOf("dimension").forGetter(Entry::dimension),Codec.DOUBLE.fieldOf("x").forGetter(Entry::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Entry::y),Codec.DOUBLE.fieldOf("z").forGetter(Entry::z),
            Codec.INT.fieldOf("slot").forGetter(Entry::slot),Codec.unboundedMap(Codec.STRING,Codec.LONG).fieldOf("clocks").forGetter(Entry::clocks)
        ).apply(i,Entry::new));
        Vec3 position(){return new Vec3(x,y,z);}
        Entry at(String next,ServerLevel level,Vec3 p,int reserved){return new Entry(id,owner,name,kind,stack,next,level.dimension().identifier().toString(),p.x,p.y,p.z,reserved,new HashMap<>(clocks));}
    }
    public static final class Store extends SavedData {
        final Map<String,Entry> entries;
        Store(){this(new HashMap<>());}
        Store(Map<String,Entry> entries){this.entries=new HashMap<>(entries);}
        static final Codec<Store> CODEC=Codec.unboundedMap(Codec.STRING,Entry.CODEC).xmap(Store::new,s->s.entries);
    }
    private static final SavedDataType<Store> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("bifrost","bound_weapons"),Store::new,Store.CODEC);
    private static Store data(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
    public static long now(MinecraftServer server){return server.overworld().getGameTime();}
    public static boolean boundType(ItemStack s){String k=AsgardWeapons.kind(s);return k.equals("mjolnir")||k.equals("stormbreaker");}
    public static String id(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(ID,"");}
    public static String owner(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(OWNER,"");}
    public static Entry record(MinecraftServer server,ItemStack s){return data(server).entries.get(id(s));}
    public static List<Entry> records(MinecraftServer server){return List.copyOf(data(server).entries.values());}
    private static void save(MinecraftServer s,Entry e){data(s).entries.put(e.id,e);data(s).setDirty();}
    public static boolean owns(Player p,ItemStack s){return !boundType(s)||owner(s).equals(p.getUUID().toString());}
    public static boolean canUse(ServerPlayer p,ItemStack s){
        if(!boundType(s))return true;
        Entry e=record(p.level().getServer(),s);
        if(!owns(p,s)||e==null||!e.owner.equals(p.getUUID().toString())||!e.state.equals(INVENTORY)||s.getCount()!=1)return false;
        int copies=0;
        for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack held=p.getInventory().getItem(i);if(!held.isEmpty()&&id(held).equals(e.id))copies++;}
        ItemStack cursor=p.containerMenu.getCarried();if(!cursor.isEmpty()&&id(cursor).equals(e.id))copies++;
        return copies==1;
    }
    public static boolean canTravel(ServerPlayer p){ItemStack held=p.getMainHandItem();return !AsgardAbilities.active(p)&&(!boundType(held)||canUse(p,held));}
    public static boolean isBusy(ServerPlayer p,ItemStack s){return boundType(s)&&!canUse(p,s);}
    public static boolean claim(ServerPlayer p){
        ItemStack s=p.getMainHandItem();
        if(!AsgardianAccess.allow(p))return false;
        if(!boundType(s)||!id(s).isEmpty()||!owner(s).isEmpty()||s.getCount()!=1){
            ItemEntity candidate=p.level().getEntitiesOfClass(ItemEntity.class,p.getBoundingBox().inflate(4),i->boundType(i.getItem())&&id(i.getItem()).isEmpty()&&owner(i.getItem()).isEmpty()&&i.getItem().getCount()==1&&p.hasLineOfSight(i))
                // A nearby item's floor origin is below its visible model and outside
                // the aim cone when the player stands close. Explicit claim is enough
                // intent within pickup reach; line of sight is still required above.
                .stream().filter(i->i.distanceToSqr(p)<=2.25||p.getLookAngle().dot(i.position().subtract(p.getEyePosition()).normalize())>.9).min(Comparator.comparingDouble(i->i.distanceToSqr(p))).orElse(null);
            if(candidate==null){
                if(boundType(s)&&(!id(s).isEmpty()||!owner(s).isEmpty()))return claimFailure(p,"That held weapon already has ownership data. Claiming cannot transfer it or reset its owner.");
                return claimFailure(p,"No claimable ground weapon found. Hold an unclaimed hammer/axe, stand within 1.5 blocks of one, or aim toward one within 4 blocks with a clear view. Displayed items must be taken down first.");
            }
            int free=p.getInventory().getFreeSlot();if(free<0)return claimFailure(p,"Free one main-inventory slot before claiming this ground weapon.");
            s=candidate.getItem().copy();bind(p,s,INVENTORY);candidate.discard();
            internal=true;try{p.getInventory().setItem(free,s);}finally{internal=false;}return true;
        }
        bind(p,s,INVENTORY);return true;
    }
    private static boolean claimFailure(ServerPlayer p,String message){p.sendSystemMessage(Component.literal(message));return false;}
    /** Called BEFORE the crafting result is transferred or shift-clicked. */
    public static boolean prepareCraft(ServerPlayer p,ItemStack s){
        if(!boundType(s))return true;
        if(!AsgardianAccess.canUse(p))return false;
        if(id(s).isEmpty())bind(p,s,CRAFTING);
        return owns(p,s)&&record(p.level().getServer(),s)!=null;
    }
    private static void bind(ServerPlayer p,ItemStack s,String state){
        String id=UUID.randomUUID().toString();
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putString(ID,id);t.putString(OWNER,p.getUUID().toString());t.putString(NAME,p.getGameProfile().name());});
        s.setCount(1);
        Map<String,Long> clocks=new HashMap<>();if(state.equals(CRAFTING))clocks.put("craftExpires",now(p.level().getServer())+1200);
        save(p.level().getServer(),new Entry(id,p.getUUID().toString(),p.getGameProfile().name(),AsgardWeapons.kind(s),s.copy(),state,p.level().dimension().identifier().toString(),p.getX(),p.getY(),p.getZ(),p.getInventory().getSelectedSlot(),clocks));
    }
    public static boolean insertionAllowed(Player p,ItemStack s){
        if(internal||!boundType(s))return true;
        if(p.level().isClientSide())return true;
        if(!(p instanceof ServerPlayer sp))return false;
        if(id(s).isEmpty()&&owner(s).isEmpty())return AsgardianAccess.canUse(p)&&s.getCount()==1; // explicit claim still required before any use
        if(!owns(p,s))return false;
        Entry e=record(sp.level().getServer(),s);
        if(e==null||!e.owner.equals(p.getUUID().toString())||!(e.state.equals(INVENTORY)||e.state.equals(CRAFTING)))return false;
        for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack old=p.getInventory().getItem(i);if(old!=s&&!old.isEmpty()&&id(old).equals(e.id))return false;}
        ItemStack cursor=p.containerMenu.getCarried();return cursor==s||cursor.isEmpty()||!id(cursor).equals(e.id);
    }
    public static void finishCraft(ServerPlayer p,ItemStack taken){
        commitCraft(p,taken);
        for(int i=0;i<p.getInventory().getContainerSize();i++)commitCraft(p,p.getInventory().getItem(i));
        commitCraft(p,p.containerMenu.getCarried());
    }
    private static void commitCraft(ServerPlayer p,ItemStack s){Entry e=record(p.level().getServer(),s);if(e!=null&&e.state.equals(CRAFTING)&&owns(p,s))save(p.level().getServer(),e.at(INVENTORY,p.level(),p.position(),p.getInventory().getSelectedSlot()));}
    public static boolean cooldownReady(ServerPlayer p,ItemStack s,String key){Entry e=record(p.level().getServer(),s);return e==null||e.clocks.getOrDefault(key,0L)<=now(p.level().getServer());}
    public static void cooldown(ServerPlayer p,ItemStack s,String key,long ticks){Entry e=record(p.level().getServer(),s);if(e!=null){e.clocks.put(key,now(p.level().getServer())+ticks);save(p.level().getServer(),e);}}
    static final class Flight {
        final UUID id,owner;final ItemStack stack;final double damage;final boolean ricochet;
        final Set<UUID> hit=new HashSet<>(); Vec3 position,direction; double traveled;int age,bounces; boolean returning;
        UUID target; final String dimension;
        Flight(ServerPlayer p,ItemStack s,boolean ricochet){id=UUID.fromString(WeaponOwnership.id(s));owner=p.getUUID();stack=s.copy();damage=AsgardCombat.normalDamage(p,s);this.ricochet=ricochet;position=p.getEyePosition();direction=p.getLookAngle();dimension=p.level().dimension().identifier().toString();}
    }
    public static boolean throwWeapon(ServerPlayer p,ItemStack s,boolean ricochet){
        if(!BifrostSkills.customAbilitiesEnabled())return false;
        if(!AsgardianAccess.canUse(p)||AsgardAbilities.active(p)||!boundType(s)||!canUse(p,s)||!cooldownReady(p,s,"throw"))return false;
        Entry e=record(p.level().getServer(),s); Flight f=new Flight(p,s,ricochet);
        int slot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i)==s){slot=i;break;}
        if(slot<0)return false;
        e=new Entry(e.id,e.owner,e.name,e.kind,s.copy(),e.state,e.dimension,e.x,e.y,e.z,e.slot,new HashMap<>(e.clocks));
        e.clocks.put("throw",now(p.level().getServer())+12);
        save(p.level().getServer(),e.at(FLIGHT,p.level(),p.position(),slot));
        internal=true;try{p.getInventory().setItem(slot,ItemStack.EMPTY);}finally{internal=false;}
        flights.put(f.id,f);return true;
    }
    private static ServerLevel level(MinecraftServer s,String dimension){return s.getLevel(ResourceKey.create(Registries.DIMENSION,Identifier.parse(dimension)));}
    public static void tick(MinecraftServer server){
        if(activeServer!=server){flights.clear();visuals.clear();activeServer=server;}
        for(ServerPlayer p:server.getPlayerList().getPlayers())reconcileInventory(p);
        for(Entry e:new ArrayList<>(data(server).entries.values())){
            if(e.state.equals(CRAFTING)){
                ServerPlayer owner=server.getPlayerList().getPlayer(UUID.fromString(e.owner));
                boolean preview=owner!=null&&owner.containerMenu.slots.stream().anyMatch(slot->slot.container instanceof net.minecraft.world.inventory.ResultContainer&&id(slot.getItem()).equals(e.id));
                if(!preview&&(owner!=null||e.clocks.getOrDefault("craftExpires",0L)<=now(server))){data(server).entries.remove(e.id);data(server).setDirty();}
            }else if(e.state.equals(FLIGHT)){
                Flight f=flights.get(UUID.fromString(e.id));
                if(f==null){ServerPlayer p=server.getPlayerList().getPlayer(UUID.fromString(e.owner));if(p!=null)returnTo(p,e);}
                else tickFlight(server,e,f);
            }else if(e.state.equals(WAITING)||e.state.equals(ORBIT)){
                ServerPlayer p=server.getPlayerList().getPlayer(UUID.fromString(e.owner));
                if(e.state.equals(ORBIT)&&p!=null&&p.isAlive()&&!AsgardianAccess.downed(p))returnTo(p,e);
                else ensureVisual(server,e,e.position());
            }
        }
    }
    private static void tickFlight(MinecraftServer s,Entry e,Flight f){
        ServerPlayer p=s.getPlayerList().getPlayer(f.owner);ServerLevel l=level(s,f.dimension);
        if(l==null||p==null||!p.level().dimension().identifier().toString().equals(f.dimension)||!AsgardianAccess.canUse(p)||++f.age>200){
            flights.remove(f.id);removeVisual(s,e);if(p!=null&&p.isAlive())returnTo(p,e);return;
        }
        if(f.returning){Vec3 delta=p.getEyePosition().subtract(f.position);if(delta.length()<1.6){flights.remove(f.id);removeVisual(s,e);returnTo(p,e);return;}f.direction=delta.normalize();}
        else if(f.target!=null){var target=l.getEntity(f.target);if(target instanceof LivingEntity t&&AsgardCombat.eligible(p,t))f.direction=t.getBoundingBox().getCenter().subtract(f.position).normalize();else f.returning=true;}
        double budget=f.ricochet?(f.bounces==0?24:10):20;
        double remaining=budget-f.traveled;
        if(!f.returning&&remaining<=.000001){f.returning=true;return;}
        Vec3 next=f.position.add(f.direction.scale(f.returning?1.35:Math.min(1.35,remaining)));
        if(!l.hasChunkAt(BlockPos.containing(next))){f.returning=true;return;}
        HitResult wall=l.clip(new ClipContext(f.position,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        Vec3 end=wall.getType()==HitResult.Type.MISS?next:wall.getLocation();
        if(!f.returning){
            LivingEntity closest=null;Vec3 hitAt=null;double dist=Double.MAX_VALUE;boolean bounced=false,entityHit=false;
            for(LivingEntity t:l.getEntitiesOfClass(LivingEntity.class,new AABB(f.position,end).inflate(.6),t->!f.hit.contains(t.getUUID())&&AsgardCombat.eligible(p,t))){
                Optional<Vec3> point=t.getBoundingBox().inflate(.35).clip(f.position,end);
                if(point.isPresent()&&point.get().distanceToSqr(f.position)<dist){closest=t;hitAt=point.get();dist=point.get().distanceToSqr(f.position);}
            }
            if(closest!=null){
                Vec3 hitPoint=hitAt;end=hitPoint;entityHit=true;
                LivingEntity struck=closest;
                List<LivingEntity> options=l.getEntitiesOfClass(LivingEntity.class,new AABB(hitPoint,hitPoint).inflate(10),
                    t->t!=struck&&!f.hit.contains(t.getUUID())&&AsgardCombat.eligible(p,t)&&t.getBoundingBox().getCenter().distanceToSqr(hitPoint)<=100
                    &&l.clip(new ClipContext(hitPoint,t.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS)
                    .stream().sorted(Comparator.comparingDouble(t->t.getBoundingBox().getCenter().distanceToSqr(hitPoint))).limit(64).toList();
                double multiplier=f.ricochet?(f.hit.isEmpty()&&options.isEmpty()?1.8:f.hit.isEmpty()?1.25:1.0):1.35;
                if(AsgardCombat.hitRaw(p,closest,f.damage*multiplier,false)){
                    f.hit.add(closest.getUUID());
                    if(f.ricochet&&f.bounces<3&&!options.isEmpty()){f.target=options.getFirst().getUUID();f.bounces++;f.traveled=0;bounced=true;}
                    else f.returning=true;
                }else f.returning=true;
            }
            if(!bounced)f.traveled+=f.position.distanceTo(end);
            if((!entityHit&&wall.getType()!=HitResult.Type.MISS)||(!bounced&&f.traveled>=budget-.000001))f.returning=true;
        }
        f.position=end;ensureVisual(s,e,end);
    }
    private static void removeVisual(MinecraftServer s,Entry e){ItemEntity visual=visuals.remove(UUID.fromString(e.id));if(visual!=null)visual.discard();}
    private static void ensureVisual(MinecraftServer s,Entry e,Vec3 position){
        ServerLevel l=level(s,e.dimension);if(l==null||!l.hasChunkAt(BlockPos.containing(position)))return;
        UUID key=UUID.fromString(e.id);ItemEntity old=visuals.get(key);
        // Loaded but non-ticking chunks can temporarily omit entities from UUID lookup.
        // Retain the actual transient object so changing dimension cannot orphan a visual.
        if(old!=null&&!old.isRemoved()&&old.level()==l){old.setPos(position);return;}
        if(old!=null)old.discard();
        ItemEntity item=new ItemEntity(l,position.x,position.y,position.z,e.stack.copy());
        item.setNeverPickUp();item.setUnlimitedLifetime();item.setInvulnerable(true);item.setNoGravity(true);item.setDeltaMovement(Vec3.ZERO);
        item.getPersistentData().putString("bifrost_visual",e.id);visuals.put(key,item);spawning.add(item.getUUID());
        try{l.addFreshEntity(item);}finally{spawning.remove(item.getUUID());}
    }
    public static boolean protectedVisual(ItemEntity e){return boundType(e.getItem());}
    /** Called by native ItemEntity.playerTouch before inventory code. Crouch to retrieve a death-site weapon. */
    public static void touch(ItemEntity item,Player actor){
        if(!(actor instanceof ServerPlayer p)||!owns(p,item.getItem()))return;
        Entry e=record(p.level().getServer(),item.getItem());if(e==null||!e.state.equals(WAITING)||!p.isShiftKeyDown()||!p.isAlive()||p.isSpectator()||AsgardianAccess.downed(p))return;
        if(e.dimension.equals(p.level().dimension().identifier().toString())&&e.position().distanceToSqr(p.position())<9)returnTo(p,e);
    }
    private static boolean returnTo(ServerPlayer p,Entry e){
        if(e.state.equals(WAITING)&&!p.isShiftKeyDown())return false;
        int free=e.slot>=0&&e.slot<p.getInventory().getContainerSize()&&p.getInventory().getItem(e.slot).isEmpty()?e.slot:p.getInventory().getFreeSlot();
        if(free<0){if(!e.state.equals(WAITING)){Entry orbit=e.at(ORBIT,p.level(),p.position().add(0,1,0),e.slot);save(p.level().getServer(),orbit);ensureVisual(p.level().getServer(),orbit,orbit.position());}return false;}
        removeVisual(p.level().getServer(),e);save(p.level().getServer(),e.at(INVENTORY,p.level(),p.position(),free));
        internal=true;try{p.getInventory().setItem(free,e.stack.copy());}finally{internal=false;}p.containerMenu.broadcastChanges();return true;
    }
    /** Injected into the final ServerPlayer.die path AFTER cancellation/revive checks. */
    public static void finalDeath(ServerPlayer p){
        MinecraftServer server=p.level().getServer();
        finishCraft(p,ItemStack.EMPTY);
        for(Entry e:new ArrayList<>(data(server).entries.values()))if(e.owner.equals(p.getUUID().toString())&&!e.state.equals(WAITING)&&!e.state.equals(CRAFTING)){
            e=snapshotLive(p,e);
            flights.remove(UUID.fromString(e.id));removeVisual(server,e);Vec3 site=safeSite(p.level(),p.blockPosition());
            save(server,e.at(WAITING,p.level(),site,e.slot));
            p.sendSystemMessage(Component.literal("Your "+e.kind+" waits at "+p.level().dimension().identifier()+" "+BlockPos.containing(site).toShortString()+". Crouch beside it to retrieve it."));
        }
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(boundType(p.getInventory().getItem(i)))p.getInventory().setItem(i,ItemStack.EMPTY);
        if(boundType(p.containerMenu.getCarried()))p.containerMenu.setCarried(ItemStack.EMPTY);
    }
    static Vec3 safeSite(ServerLevel l,BlockPos origin){
        int y=Math.clamp(origin.getY(),l.getMinY()+2,l.getMaxY()-3);BlockPos center=l.getWorldBorder().clampToBounds(new BlockPos(origin.getX(),y,origin.getZ()));
        List<BlockPos> candidates=new ArrayList<>();for(BlockPos pos:BlockPos.betweenClosed(center.offset(-16,-16,-16),center.offset(16,16,16)))candidates.add(pos.immutable());
        candidates.sort(Comparator.comparingDouble(pos->pos.distSqr(center)));
        for(BlockPos pos:candidates){
            if(!l.hasChunkAt(pos)||!l.getWorldBorder().isWithinBounds(pos))continue;
            var floor=l.getBlockState(pos.below());
            if(floor.isSolidRender()&&!floor.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)&&!floor.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)&&!floor.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)&&l.getBlockState(pos).isAir()&&l.getBlockState(pos.above()).isAir()&&l.getFluidState(pos.below()).isEmpty())return Vec3.atBottomCenterOf(pos);
        }
        // No loaded safe footing nearby: search the loaded source column for clear floating space.
        // This never carves blocks, loads distant terrain, or leaves the weapon embedded in lava/stone.
        if(l.hasChunkAt(center))for(int offset=0;offset<=l.getHeight();offset++)for(int sign:offset==0?new int[]{1}:new int[]{1,-1}){
            int candidateY=center.getY()+offset*sign;if(candidateY<l.getMinY()+1||candidateY>=l.getMaxY()-1)continue;
            BlockPos pos=new BlockPos(center.getX(),candidateY,center.getZ());
            if(l.getWorldBorder().isWithinBounds(pos)&&l.getBlockState(pos).isAir()&&l.getBlockState(pos.above()).isAir()&&l.getFluidState(pos).isEmpty()&&l.getFluidState(pos.below()).isEmpty())return Vec3.atBottomCenterOf(pos);
        }
        // Pathological all-solid column: a protected, reachable-by-flight point above build height.
        // The owner receives these exact coordinates; /asgard recover remains an explicit operator escape hatch.
        BlockPos top=new BlockPos(center.getX(),l.getMaxY()+1,center.getZ());
        System.getLogger("bifrost").log(System.Logger.Level.WARNING,"No clear loaded death-site footing/column; bound weapon waits above build height at "+top+". Operator recovery may be required.");
        return Vec3.atBottomCenterOf(top);
    }
    public static boolean adminRecover(ServerPlayer owner,String weaponId){
        Entry e=data(owner.level().getServer()).entries.get(weaponId);if(e==null||!e.owner.equals(owner.getUUID().toString()))return false;
        e=snapshotLive(owner,e);strip(owner,weaponId);
        flights.remove(UUID.fromString(e.id));removeVisual(owner.level().getServer(),e);
        save(owner.level().getServer(),e.at(ORBIT,owner.level(),owner.position(),e.slot));return true;
    }
    /** Operator-only recovery exception: caller must enforce permission and audit the exact old/new UUIDs. */
    public static boolean adminReassign(ServerPlayer nextOwner,String weaponId,String expectedOldOwnerUuid){
        MinecraftServer server=nextOwner.level().getServer();Entry e=data(server).entries.get(weaponId);
        if(e==null||!e.owner.equals(expectedOldOwnerUuid)||e.owner.equals(nextOwner.getUUID().toString())||e.state.equals(CRAFTING))return false;
        ServerPlayer previous=server.getPlayerList().getPlayer(UUID.fromString(e.owner));if(previous!=null)e=snapshotLive(previous,e);
        flights.remove(UUID.fromString(e.id));removeVisual(server,e);
        ItemStack canonical=e.stack.copy();
        CustomData.update(DataComponents.CUSTOM_DATA,canonical,t->{t.putString(OWNER,nextOwner.getUUID().toString());t.putString(NAME,nextOwner.getGameProfile().name());});
        Vec3 p=nextOwner.position();
        save(server,new Entry(e.id,nextOwner.getUUID().toString(),nextOwner.getGameProfile().name(),e.kind,canonical,ORBIT,nextOwner.level().dimension().identifier().toString(),p.x,p.y,p.z,nextOwner.getInventory().getSelectedSlot(),e.clocks));
        for(ServerPlayer player:server.getPlayerList().getPlayers()){
            strip(player,weaponId);
            player.containerMenu.broadcastChanges();
        }
        return true;
    }
    private static void strip(ServerPlayer p,String weaponId){
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(id(p.getInventory().getItem(i)).equals(weaponId))p.getInventory().setItem(i,ItemStack.EMPTY);
        if(id(p.containerMenu.getCarried()).equals(weaponId))p.containerMenu.setCarried(ItemStack.EMPTY);
    }
    private static Entry snapshotLive(ServerPlayer p,Entry e){
        if(p==null||!(e.state.equals(INVENTORY)||e.state.equals(CRAFTING)))return e;
        ItemStack found=ItemStack.EMPTY;
        for(int i=0;i<p.getInventory().getContainerSize();i++){ItemStack candidate=p.getInventory().getItem(i);if(!candidate.isEmpty()&&id(candidate).equals(e.id)&&owner(candidate).equals(e.owner)){found=candidate;break;}}
        ItemStack cursor=p.containerMenu.getCarried();if(found.isEmpty()&&!cursor.isEmpty()&&id(cursor).equals(e.id)&&owner(cursor).equals(e.owner))found=cursor;
        return found.isEmpty()?e:new Entry(e.id,e.owner,e.name,e.kind,found,e.state,e.dimension,e.x,e.y,e.z,e.slot,e.clocks);
    }
    private static void reconcileInventory(ServerPlayer p){
        Set<String> seen=new HashSet<>();
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            ItemStack s=p.getInventory().getItem(i);if(!boundType(s)||id(s).isEmpty())continue;
            commitCraft(p,s);
            if(!canUse(p,s)||!seen.add(id(s)))p.getInventory().setItem(i,ItemStack.EMPTY);
            else{Entry e=record(p.level().getServer(),s);if(e!=null&&!ItemStack.matches(e.stack,s))save(p.level().getServer(),snapshotLive(p,e));}
        }
        ItemStack carried=p.containerMenu.getCarried();if(boundType(carried)&&!id(carried).isEmpty()){
            if(!canUse(p,carried)||!seen.add(id(carried)))p.containerMenu.setCarried(ItemStack.EMPTY);
            else{Entry e=record(p.level().getServer(),carried);if(e!=null&&!ItemStack.matches(e.stack,carried))save(p.level().getServer(),snapshotLive(p,e));}
        }
    }
    @SubscribeEvent public static void join(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()||!(event.getEntity() instanceof ItemEntity item)||!boundType(item.getItem()))return;
        if(id(item.getItem()).isEmpty()){item.setNeverPickUp();item.setUnlimitedLifetime();item.setInvulnerable(true);item.setNoGravity(true);item.setDeltaMovement(Vec3.ZERO);return;}
        if(!spawning.contains(item.getUUID()))event.setCanceled(true); // old entity/grave/drop copies never become authoritative
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event){
        if(!boundType(event.getItemStack()))return;
        String name=event.getItemStack().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(NAME,"");
        String owner=owner(event.getItemStack()),id=id(event.getItemStack());
        event.getToolTip().add(Component.literal(owner.isEmpty()&&id.isEmpty()?"Unclaimed: use /asgard claim":owner.isEmpty()||id.isEmpty()?"Incomplete ownership data: ask an operator to recover it":"Bound to "+(name.isEmpty()?owner:name)));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){
        if(event.getEntity() instanceof ServerPlayer p)for(Entry e:new ArrayList<>(data(p.level().getServer()).entries.values()))if(e.owner.equals(p.getUUID().toString()))save(p.level().getServer(),snapshotLive(p,e));
    }
}
