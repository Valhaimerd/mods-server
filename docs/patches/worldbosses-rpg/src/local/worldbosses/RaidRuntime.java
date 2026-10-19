package local.worldbosses;

import com.google.gson.Gson;
import com.mki.worldbosses.config.WorldBossesConfig;
import com.mki.worldbosses.entity.boss.WorldBossEntity;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-owned encounter state. Persisted on bosses and in vanilla command storage. */
public final class RaidRuntime {
    private static final Gson GSON=new Gson();
    private static final Map<WorldBossEntity,State> STATES=new WeakHashMap<>();
    private static final Map<UUID,java.lang.ref.WeakReference<WorldBossEntity>> OWNERS=new HashMap<>();
    private static final Map<ServerPlayer,RaidMath.Budget> BUDGETS=new WeakHashMap<>();
    private static final Map<ServerPlayer,RaidMath.Budget> HAZARD_BUDGETS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Map<String,Long>> HAZARD_TICKS=new WeakHashMap<>();
    private static final ThreadLocal<String> HAZARD=new ThreadLocal<>();
    private static final Map<ServerPlayer,Map<String,Dot>> DOTS=new WeakHashMap<>();
    private static final ThreadLocal<ArrayDeque<DotTick>> DOT_TICKS=ThreadLocal.withInitial(ArrayDeque::new);
    private static final Map<ServerPlayer,Motion> MOTIONS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Long> SAFE_UNTIL=new WeakHashMap<>();
    private static final ThreadLocal<ArrayDeque<LivingEntity>> HEALERS=ThreadLocal.withInitial(ArrayDeque::new);
    private static final Identifier LEDGER_ID=Identifier.parse("worldbosses:rpg_rewards");
    private static boolean registered;
    private static MinecraftServer ledgerServer;
    private static Ledger ledger;
    public static final class State {
        public int players=1;
        public double x,y,z;
        public boolean homeSet,initialized;
        public long started=-1,lastActive,lastMajor=-10000,diveUntil;
        public Map<UUID,Long> participants=new HashMap<>();
        public Set<UUID> armored=new HashSet<>();
        public Map<UUID,Long> blockedSince=new HashMap<>(),eruptionAt=new HashMap<>(),relocateAt=new HashMap<>(),relocateCooldown=new HashMap<>();
        public Map<UUID,double[]> marks=new HashMap<>();
        public Map<UUID,double[]> movementPositions=new HashMap<>();
        public boolean rewarded;
    }
    private record Dot(WorldBossEntity owner,long until) {}
    private record DotTick(WorldBossEntity owner,UUID victim) {}
    private record Motion(WorldBossEntity owner,long until,Vec3 origin) {}
    public static final class Grant {
        public String boss,encounter,element,advancement;
        public int tier,xp;
        public boolean star;
        public List<String> challenges=new ArrayList<>();
    }
    public static final class Ledger {
        public Map<String,List<Grant>> pending=new HashMap<>();
        public Set<String> granted=new HashSet<>(),firstClear=new HashSet<>();
        public Map<String,Long> cooldowns=new HashMap<>();
    }
    private RaidRuntime() {}
    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        if(registered)return; registered=true;
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,LivingIncomingDamageEvent.class,RaidRuntime::damage);
        NeoForge.EVENT_BUS.addListener(RaidRuntime::healed);
        NeoForge.EVENT_BUS.addListener(RaidRuntime::joined);
        NeoForge.EVENT_BUS.addListener(RaidRuntime::playerTick);
    }
    private static boolean retiredCreative(ItemStack stack) { return GearRetirement.hidden(stack); }
    public static void creative(net.minecraft.world.item.CreativeModeTab.Output output,net.minecraft.world.level.ItemLike item) {
        if(!GearRetirement.hiddenId(BuiltInRegistries.ITEM.getKey(item.asItem())))output.accept(item);
    }
    public static String key(WorldBossEntity boss) {
        String path=BuiltInRegistries.ENTITY_TYPE.getKey(boss.getType()).getPath();
        return switch(path) {case "shadow_king"->"shadowKing";case "dragon_mother"->"dragonMother";case "storm_griffin"->"stormGriffin";case "shadow_weaver"->"shadowWeaver";default->path;};
    }
    public static WorldBossesConfig.BossConfig config(WorldBossEntity boss) {return RaidProfile.config(key(boss));}
    public static State state(WorldBossEntity boss) {return STATES.computeIfAbsent(boss,b->new State());}
    public static void home(WorldBossEntity boss,BlockPos center) {
        if(center==null)center=boss.blockPosition();
        State s=state(boss);s.x=center.getX()+.5;s.y=center.getY()+1;s.z=center.getZ()+.5;s.homeSet=true;
    }
    private static Vec3 home(WorldBossEntity boss) {
        State s=state(boss);
        if(!s.homeSet)home(boss,boss.blockPosition());
        return new Vec3(s.x,s.y,s.z);
    }
    public static void record(WorldBossEntity boss,Player player) {
        if(!(player instanceof ServerPlayer p)||p.isCreative()||p.isSpectator())return;
        State s=state(boss);long now=boss.level().getGameTime();
        if(p.level()!=boss.level()||p.distanceToSqr(boss)>96*96)return;
        s.participants.put(p.getUUID(),now);s.lastActive=now;
        if(wearsArmor(p))s.armored.add(p.getUUID());
        if(s.started<0)s.started=now;
    }
    public static void save(WorldBossEntity boss,ValueOutput output) { output.putString("rpg_raid",GSON.toJson(state(boss))); }
    public static void load(WorldBossEntity boss,ValueInput input) {
        String data=input.getStringOr("rpg_raid","");
        if(!data.isEmpty()) {
            try {State s=GSON.fromJson(data,State.class);if(s!=null){s.blockedSince.clear();s.eruptionAt.clear();s.relocateAt.clear();s.marks.clear();s.diveUntil=0;STATES.put(boss,s);}}
            catch(RuntimeException ex){throw new IllegalStateException("Invalid saved World Bosses RPG state",ex);}
        }
    }
    private static double factor(WorldBossesConfig.BossConfig c,int count,String percent) {
        return 1+Math.max(0,RaidProfile.number(c.raidScaling,percent))/100*(count-1);
    }
    public static void scale(WorldBossEntity boss,ServerLevel level,WorldBossesConfig.BossConfig c) {
        State s=state(boss);long now=level.getGameTime();
        int active=0;
        for(var e:s.participants.entrySet()) {
            ServerPlayer p=level.getServer().getPlayerList().getPlayer(e.getKey());
            if(p!=null&&p.level()==level&&!p.isCreative()&&!p.isSpectator()&&p.isAlive()&&now-e.getValue()<=1200&&p.distanceToSqr(boss)<=96*96)active++;
        }
        s.players=Math.max(s.players,RaidMath.players(active));
        double hp=c.maxHealth*WorldBossesConfig.INSTANCE.bossScaling.globalHealthMultiplier*factor(c,s.players,"healthPerExtraPlayerPercent");
        double oldMax=Math.max(1,boss.getMaxHealth());
        double fraction=Math.clamp(boss.getHealth()/oldMax,0,1);
        var health=boss.getAttribute(Attributes.MAX_HEALTH);
        if(health!=null&&Math.abs(health.getBaseValue()-hp)>.01) {
            health.setBaseValue(hp);boss.setHealth((float)(boss.getMaxHealth()*fraction));
        }
        boss.getAttribute(Attributes.ARMOR).setBaseValue(Math.min(30,c.armor*factor(c,s.players,"armorPerExtraPlayerPercent")));
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(c.attackDamage*WorldBossesConfig.INSTANCE.bossScaling.globalDamageMultiplier);
        boss.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(c.followRange);
        s.initialized=true;
    }
    private static boolean immune(WorldBossEntity boss) {
        if(boss.isInvulnerable())return true;
        try {var f=boss.getClass().getDeclaredField("awakenTicks");f.setAccessible(true);return f.getInt(boss)>0;}
        catch(ReflectiveOperationException e){return false;}
    }
    public static void heal(WorldBossEntity boss,ServerLevel level) {
        State s=state(boss);var settings=WorldBossesConfig.INSTANCE.bossSelfHeal;
        if(!settings.enabled||boss.tickCount%20!=0||s.started<0||level.getGameTime()-s.started<200||immune(boss)||boss.getTarget()==null||!boss.isAlive())return;
        if(level.players().stream().noneMatch(p->p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.distanceToSqr(boss)<96*96))return;
        var c=config(boss);
        double rate=RaidProfile.number(c,"rpgRegenerationPerSecond")*factor(c,s.players,"regenerationPerExtraPlayerPercent");
        double cap=boss.getMaxHealth()*settings.maxHealPercentPerSecond/100;
        boss.heal((float)(cap>0?Math.min(rate,cap):rate));
    }
    public static boolean major(WorldBossEntity boss) {
        State s=state(boss);long now=boss.level().getGameTime();
        if(now-s.lastMajor<60)return false;
        s.lastMajor=now;return true;
    }
    public static int realmCooldown(WorldBossEntity boss,int base) {
        try {
            if(key(boss).equals("shadowWeaver")) {
                var f=boss.getClass().getDeclaredField("phase");f.setAccessible(true);
                return (int)(base*(f.getInt(boss)>=3?.8:f.getInt(boss)==2?.9:1));
            }
            var f=boss.getClass().getDeclaredField("enraged");f.setAccessible(true);
            return (int)(base*(f.getBoolean(boss)?.8:1));
        }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    public static int windup(WorldBossEntity boss,Object skill,int ticks) { return Math.max(40,ticks); }
    public static double diveRadius(double minimum,double radius){return Math.min(3,Math.max(minimum,radius));}
    public static double drainMultiplier(WorldBossEntity ignored){return 1;}
    public static void safeBossTeleport(WorldBossEntity boss,double x,double y,double z) {
        if(!(boss.level() instanceof ServerLevel level))return;
        BlockPos feet=BlockPos.containing(x,y,z);Vec3 destination=new Vec3(x,y,z);
        if(destination.distanceToSqr(home(boss))>60*60||!level.hasChunkAt(feet)||!level.getWorldBorder().isWithinBounds(feet))return;
        if(!level.getBlockState(feet.below()).isCollisionShapeFullBlock(level,feet.below())||!level.getFluidState(feet).isEmpty()||!level.getFluidState(feet.above()).isEmpty())return;
        if(level.noCollision(boss,boss.getBoundingBox().move(destination.subtract(boss.position()))))boss.teleportTo(x,y,z);
    }
    public static boolean realmTick(WorldBossEntity boss) {
        State s=state(boss);
        try {
            var f=boss.getClass().getDeclaredField("skill");f.setAccessible(true);
            if(!String.valueOf(f.get(boss)).equals("DIVE")){s.diveUntil=0;return true;}
            long now=boss.level().getGameTime();
            if(s.diveUntil==0) {
                s.diveUntil=now+40;
                var target=boss.getTarget();
                if(target instanceof ServerPlayer p) {
                    p.sendOverlayMessage(Component.literal("Dive impact in 2 seconds - move!"));
                    ((ServerLevel)boss.level()).sendParticles(ParticleTypes.ENCHANT,p.getX(),p.getY()+.1,p.getZ(),80,2,.1,2,0);
                }
            }
            if(now<s.diveUntil){boss.setDeltaMovement(Vec3.ZERO);return false;}
            return true;
        }catch(NoSuchFieldException e){return true;}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    }
    public static WorldBossEntity owner(Entity entity) {
        if(entity instanceof WorldBossEntity boss)return boss;
        if(entity==null||!(entity.level() instanceof ServerLevel level))return null;
        if(entity instanceof net.minecraft.world.entity.projectile.Projectile projectile&&projectile.getOwner()!=entity) {
            WorldBossEntity boss=owner(projectile.getOwner());if(boss!=null)return boss;
        }
        String id=entity.getPersistentData().getStringOr("rpg_raid_owner","");
        if(!id.isEmpty()) {
            Entity found=level.getEntity(UUID.fromString(id));
            if(found instanceof WorldBossEntity boss)return boss;
            var ref=OWNERS.get(UUID.fromString(id));
            if(ref!=null&&ref.get()!=null&&ref.get().level()==level)return ref.get();
        }
        return null;
    }
    public static boolean spawnOwned(ServerLevel level,Entity entity,WorldBossEntity boss) {
        entity.getPersistentData().putString("rpg_raid_owner",boss.getUUID().toString());
        OWNERS.put(boss.getUUID(),new java.lang.ref.WeakReference<>(boss));
        return level.addFreshEntity(entity);
    }
    private static void joined(EntityJoinLevelEvent event) {
        if(!(event.getLevel() instanceof ServerLevel level)||event.loadedFromDisk())return;
        Entity entity=event.getEntity();
        if(!entity.getPersistentData().getStringOr("rpg_raid_owner","").isEmpty())return;
        String path=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if(!path.startsWith("worldbosses:"))return;
        if(!path.endsWith("skeleton_warrior")&&!path.endsWith("baby_dragon")&&!path.endsWith("shadow_clone"))return;
        // Only actual boss summons carry native summon tags, not wild inhabitants.
        if(entity.entityTags().stream().noneMatch(t->t.contains("minion")||t.contains("brood")||t.contains("clone")))return;
        var near=level.getEntitiesOfClass(WorldBossEntity.class,entity.getBoundingBox().inflate(12));
        near.stream().min(Comparator.comparingDouble(entity::distanceToSqr)).ifPresent(b->entity.getPersistentData().putString("rpg_raid_owner",b.getUUID().toString()));
    }
    public static MobEffectInstance effect(MobEffectInstance effect,Entity source,LivingEntity target) {
        WorldBossEntity boss=owner(source);
        if(boss==null)return effect;
        if(target instanceof ServerPlayer p&&effect.getEffect()==MobEffects.POISON)markDot(p,boss,"poison",Math.min(40,effect.getDuration()));
        if(target instanceof ServerPlayer p&&effect.getEffect()==MobEffects.WITHER)markDot(p,boss,"wither",Math.min(40,effect.getDuration()));
        if(target instanceof ServerPlayer p&&effect.getEffect()==MobEffects.LEVITATION)MOTIONS.put(p,new Motion(boss,target.level().getGameTime()+100,target.position()));
        int duration=effect.getEffect()==MobEffects.BLINDNESS||effect.getEffect()==MobEffects.DARKNESS?20:40;
        int amplifier=effect.getEffect()==MobEffects.SLOWNESS?Math.min(1,effect.getAmplifier()):effect.getAmplifier();
        if(effect.getEffect()==MobEffects.WITHER)amplifier=0;
        return new MobEffectInstance(effect.getEffect(),Math.min(duration,effect.getDuration()),amplifier,effect.isAmbient(),effect.isVisible(),effect.showIcon());
    }
    public static void fire(LivingEntity player,int ticks,WorldBossEntity boss) {
        player.setRemainingFireTicks(Math.min(40,ticks));
        if(player instanceof ServerPlayer p)markDot(p,boss,"fire",40);
    }
    public static boolean knockback(LivingEntity target,double strength,double x,double z,DamageSource source) {
        WorldBossEntity boss=source==null?null:owner(source.getEntity());
        if(boss==null)return true;
        double length=Math.max(.0001,Math.hypot(x,z));Vec3 old=target.getDeltaMovement();
        return safeMotion(target,new Vec3(old.x/2-x/length*strength,Math.min(.4,old.y/2+strength),old.z/2-z/length*strength),boss);
    }
    public static void push(Entity target,double x,double y,double z,WorldBossEntity boss) {
        if(safeMotion(target,target.getDeltaMovement().add(x,y,z),boss))target.push(x,y,z);
    }
    public static void move(Entity target,double x,double y,double z,WorldBossEntity boss){move(target,new Vec3(x,y,z),boss);}
    public static void move(Entity target,Vec3 motion,WorldBossEntity boss){if(safeMotion(target,motion,boss))target.setDeltaMovement(motion);}
    private static boolean safeMotion(Entity target,Vec3 velocity,WorldBossEntity boss) {
        if(!(target instanceof ServerPlayer p)||!(target.level() instanceof ServerLevel level))return true;
        Vec3 pos=p.position(),v=velocity;
        for(int tick=0;tick<10;tick++) {
            pos=pos.add(v);v=new Vec3(v.x*.91,(v.y-.08)*.98,v.z*.91);
            BlockPos feet=BlockPos.containing(pos);
            if(pos.y<level.getMinY()+4||pos.distanceToSqr(home(boss))>64*64||!level.hasChunkAt(feet)||!level.getWorldBorder().isWithinBounds(feet))return false;
            if(!level.getFluidState(feet).isEmpty()||!level.getFluidState(feet.above()).isEmpty())return false;
            if(!level.noCollision(p,p.getBoundingBox().move(pos.subtract(p.position())))) {
                Vec3 landing=new Vec3(pos.x,p.getY(),pos.z);
                if(v.y>0||!level.noCollision(p,p.getBoundingBox().move(landing.subtract(p.position()))))return false;
                pos=landing;v=new Vec3(v.x,0,v.z);feet=BlockPos.containing(pos);
            }
            boolean ground=false;
            for(int down=1;down<=6;down++) {
                var q=feet.below(down);var block=level.getBlockState(q);
                if(!block.getFluidState().isEmpty()||block.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||block.is(net.minecraft.world.level.block.Blocks.FIRE))return false;
                if(block.isCollisionShapeFullBlock(level,q)){ground=true;break;}
            }
            if(!ground)return false;
        }
        MOTIONS.put(p,new Motion(boss,level.getGameTime()+100,p.position()));return true;
    }
    public static void ignite(LivingEntity player,float seconds,WorldBossEntity boss) {fire(player,(int)(seconds*20),boss);}
    public static net.minecraft.world.damagesource.DamageSource lightning(net.minecraft.world.damagesource.DamageSources sources,WorldBossEntity boss) {
        return new net.minecraft.world.damagesource.DamageSource(sources.lightningBolt().typeHolder(),boss);
    }
    public static void igniteProjectile(Entity target,float seconds,Entity projectile) {
        WorldBossEntity boss=owner(projectile);
        target.igniteForSeconds(boss==null?seconds:Math.min(2,seconds));
        if(boss!=null&&target instanceof ServerPlayer p)markDot(p,boss,"fire",40);
    }
    private static void markDot(ServerPlayer player,WorldBossEntity owner,String kind,int duration){DOTS.computeIfAbsent(player,p->new HashMap<>()).put(kind,new Dot(owner,player.level().getGameTime()+duration));}
    public static void beginDot(LivingEntity target,String kind) {
        Dot dot=target instanceof ServerPlayer p?DOTS.getOrDefault(p,Map.of()).get(kind):null;
        DOT_TICKS.get().push(new DotTick(dot!=null&&dot.until>=target.level().getGameTime()?dot.owner:null,target.getUUID()));
    }
    public static void endDot(){if(!DOT_TICKS.get().isEmpty())DOT_TICKS.get().pop();}
    public static void hazardHurt(Entity target,DamageSource source,float amount,WorldBossEntity boss,String key) {
        String previous=HAZARD.get();HAZARD.set(boss.getUUID()+":"+key);
        try{target.hurt(source,amount);}finally{if(previous==null)HAZARD.remove();else HAZARD.set(previous);}
    }
    private static void damage(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel level))return;
        if(event.getEntity() instanceof WorldBossEntity target&&event.getSource().getEntity() instanceof ServerPlayer p)record(target,p);
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        WorldBossEntity boss=owner(event.getSource().getEntity());
        long now=level.getGameTime();
        boolean periodic=false;
        if(boss==null&&event.getSource().getEntity()==null) {
            if(event.getSource().is(DamageTypes.ON_FIRE)) {
                Dot dot=DOTS.getOrDefault(player,Map.of()).get("fire");
                if(dot!=null&&dot.until>=now){boss=dot.owner;periodic=true;}
            } else if(!DOT_TICKS.get().isEmpty()&&(event.getSource().is(DamageTypes.MAGIC)||event.getSource().is(DamageTypes.WITHER))) {
                DotTick context=DOT_TICKS.get().peek();
                if(context.victim.equals(player.getUUID())&&context.owner!=null){boss=context.owner;periodic=true;}
            }
        }
        if(boss==null) {
            Motion motion=MOTIONS.get(player);
            if(motion!=null&&motion.until>=now&&(event.getSource().is(DamageTypes.FALL)||event.getSource().is(DamageTypes.LAVA)||event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))) {
                event.setCanceled(true);player.resetFallDistance();
                if(!event.getSource().is(DamageTypes.FALL)) {
                    Vec3 safe=safeSpot(level,motion.owner,player);
                    if(safe!=null){player.teleportTo(safe.x,safe.y,safe.z);SAFE_UNTIL.put(player,now+40);}
                }
            }
            return;
        }
        if(SAFE_UNTIL.getOrDefault(player,0L)>now){event.setCanceled(true);return;}
        record(boss,player);
        float amount=event.getAmount();
        if(periodic||HAZARD.get()!=null) {
            String hazard=HAZARD.get()!=null?HAZARD.get():boss.getUUID()+":"+event.getSource().type().msgId();
            var times=HAZARD_TICKS.computeIfAbsent(player,p->new HashMap<>());
            long previous=times.getOrDefault(hazard,-100L);
            if(now-previous<20){event.setCanceled(true);return;}
            amount=HAZARD_BUDGETS.computeIfAbsent(player,p->new RaidMath.Budget(4)).take(now,Math.min(2,amount));
            if(amount<=0){event.setCanceled(true);return;}
            times.put(hazard,now);
        }
        float allowed=BUDGETS.computeIfAbsent(player,p->new RaidMath.Budget()).take(now,amount);
        if(allowed<=0)event.setCanceled(true);else event.setAmount(allowed);
    }
    public static void beginHeal(LivingEntity healer){HEALERS.get().push(healer);}
    public static void endHeal(){if(!HEALERS.get().isEmpty())HEALERS.get().pop();}
    private static void healed(LivingHealEvent event) {
        if(event.getAmount()<=0||!(event.getEntity() instanceof ServerPlayer ally)||ally.getHealth()>=ally.getMaxHealth()||HEALERS.get().isEmpty())return;
        LivingEntity caster=HEALERS.get().peek();
        if(!(caster instanceof ServerPlayer healer)||healer.level()!=ally.level()||healer==ally)return;
        long now=ally.level().getGameTime();
        for(var e:new ArrayList<>(STATES.entrySet())) {
            if(e.getKey().level()==ally.level()&&now-e.getValue().participants.getOrDefault(ally.getUUID(),-10000L)<=1200&&healer.distanceToSqr(e.getKey())<=96*96)record(e.getKey(),healer);
        }
    }
    private static boolean wearsArmor(Player player) {
        for(var slot:net.minecraft.world.entity.EquipmentSlot.values())if(slot.getType()==net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR&&!player.getItemBySlot(slot).isEmpty())return true;
        return false;
    }
    private static void cancelWarnings(State s,UUID id){s.blockedSince.remove(id);s.eruptionAt.remove(id);s.marks.remove(id);s.relocateAt.remove(id);}
    public static void tick(WorldBossEntity boss,ServerLevel level) {
        if(!boss.isAlive())return;
        State s=state(boss);Vec3 center=home(boss);
        scale(boss,level,config(boss));
        long now=level.getGameTime();
        if(boss.tickCount%20!=0)return;
        boolean active=false;
        for(var e:new ArrayList<>(s.participants.entrySet())) {
            ServerPlayer p=level.getServer().getPlayerList().getPlayer(e.getKey());
            if(p==null||p.level()!=level||!p.isAlive()||p.isCreative()||p.isSpectator()||p.position().distanceToSqr(center)>64*64){cancelWarnings(s,e.getKey());continue;}
            double[] previous=s.movementPositions.put(p.getUUID(),new double[]{p.getX(),p.getY(),p.getZ()});
            boolean evading=boss.getTarget()==p&&previous!=null&&p.position().distanceToSqr(new Vec3(previous[0],previous[1],previous[2]))>.0625;
            if(now-e.getValue()>1200&&!evading){cancelWarnings(s,e.getKey());continue;}
            if(wearsArmor(p))s.armored.add(p.getUUID());
            active=true; terrain(boss,p,s,level,now);
        }
        if(active)s.lastActive=now;
        if(s.started>=0&&!active&&now-s.lastActive>=600)reset(boss,level);
    }
    public static void tick(WorldBossEntity boss){if(boss.level() instanceof ServerLevel level)tick(boss,level);}
    private static void terrain(WorldBossEntity boss,ServerPlayer p,State s,ServerLevel level,long now) {
        double distance=boss.distanceToSqr(p);
        boolean blocked=!boss.hasLineOfSight(p)||Math.abs(p.getY()-boss.getY())>4||(distance>64&&boss.getNavigation().isDone());
        if(!blocked){cancelWarnings(s,p.getUUID());return;}
        long since=s.blockedSince.computeIfAbsent(p.getUUID(),id->now);
        if(now-since<120)return;
        UUID id=p.getUUID();
        if(s.relocateAt.containsKey(id)) {
            if(now>=s.relocateAt.get(id)) {
                Vec3 safe=safeSpot(level,boss,p);
                if(safe!=null){p.teleportTo(safe.x,safe.y,safe.z);p.resetFallDistance();SAFE_UNTIL.put(p,now+40);}
                else {Vec3 h=home(boss);if(level.noCollision(boss,boss.getBoundingBox().move(h.subtract(boss.position()))))boss.teleportTo(h.x,h.y,h.z);}
                s.relocateAt.remove(id);s.relocateCooldown.put(id,now+400);s.blockedSince.put(id,now);
            }
            return;
        }
        Long eruption=s.eruptionAt.get(id);
        if(eruption!=null&&now>=eruption) {
            double[] m=s.marks.remove(id);
            if(m!=null&&p.position().distanceToSqr(new Vec3(m[0],m[1],m[2]))<=2.25)p.hurtServer(level,level.damageSources().mobAttack(boss),6);
            s.eruptionAt.remove(id);
            if(now-since>=280&&now>=s.relocateCooldown.getOrDefault(id,0L)) {
                p.sendSystemMessage(Component.literal("The boss is forcing you out of cover! Relocating in 3 seconds."));
                s.relocateAt.put(id,now+60);
            }
        } else if(eruption==null&&(now-since-120)%160==0) {
            Vec3 mark=p.position();s.marks.put(id,new double[]{mark.x,mark.y,mark.z});s.eruptionAt.put(id,now+40);
            p.sendOverlayMessage(Component.literal("Ground eruption in 2 seconds - move!"));
            level.playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.WARDEN_SONIC_CHARGE,net.minecraft.sounds.SoundSource.HOSTILE,1,1);
        }
        double[] mark=s.marks.get(id);
        if(mark!=null)level.sendParticles(ParticleTypes.ENCHANT,mark[0],mark[1]+.1,mark[2],50,1,.15,1,0);
    }
    private static Vec3 safeSpot(ServerLevel level,WorldBossEntity boss,ServerPlayer p) {
        Vec3 center=home(boss);
        for(int i=0;i<24;i++) {
            double angle=i*Math.PI/12;double radius=8+(i%3)*2;
            for(int dy=3;dy>=-3;dy--) {
                BlockPos feet=BlockPos.containing(boss.getX()+Math.cos(angle)*radius,boss.getY()+dy,boss.getZ()+Math.sin(angle)*radius);
                Vec3 v=Vec3.atBottomCenterOf(feet);
                if(v.distanceToSqr(center)>60*60||!level.hasChunkAt(feet)||!level.getWorldBorder().isWithinBounds(feet))continue;
                if(!level.getBlockState(feet.below()).isCollisionShapeFullBlock(level,feet.below()))continue;
                if(!level.getFluidState(feet).isEmpty()||!level.getFluidState(feet.above()).isEmpty())continue;
                boolean hazard=false;
                for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++) {
                    BlockPos q=feet.offset(dx,-1,dz);
                    var state=level.getBlockState(q);
                    if(!state.getFluidState().isEmpty()||state.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)||state.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)||state.is(net.minecraft.world.level.block.Blocks.FIRE))hazard=true;
                }
                if(!hazard&&level.noCollision(p,p.getBoundingBox().move(v.subtract(p.position()))))return v;
            }
        }
        return null;
    }
    private static void reset(WorldBossEntity boss,ServerLevel level) {
        State old=state(boss);Vec3 h=home(boss);
        for(Entity e:level.getEntitiesOfClass(Entity.class,boss.getBoundingBox().inflate(96))) {
            if(e!=boss&&owner(e)==boss)e.discard();
        }
        boss.setTarget(null);
        if(level.noCollision(boss,boss.getBoundingBox().move(h.subtract(boss.position()))))boss.teleportTo(h.x,h.y,h.z);
        STATES.remove(boss);
        State fresh=state(boss);fresh.x=old.x;fresh.y=old.y;fresh.z=old.z;fresh.homeSet=old.homeSet;
        scale(boss,level,config(boss));boss.setHealth(boss.getMaxHealth());
        // Native timers, phase flags and active hazard lists are reset by the patched bridge.
        resetNative(boss);
    }
    private static void resetNative(WorldBossEntity boss) {
        try {
            for(var f:boss.getClass().getDeclaredFields()) {
                if(java.lang.reflect.Modifier.isStatic(f.getModifiers()))continue;
                f.setAccessible(true);
                if((f.getName().startsWith("active")||f.getName().equals("strikes")||f.getName().equals("lightningMarks"))&&Collection.class.isAssignableFrom(f.getType()))((Collection<?>)f.get(boss)).clear();
                if(f.getName().equals("currentPhase")||f.getName().equals("phase"))f.setInt(boss,1);
                if(f.getName().equals("enraged"))f.setBoolean(boss,false);
                if(Set.of("veilTicks","broodCalls","lightningTicks","castTicks","skillTicks","awakenTicks","maceHitCounter").contains(f.getName()))f.setInt(boss,0);
                if(f.getType().isEnum()&&(f.getName().equals("skill")||f.getName().equals("casting")))for(Object value:f.getType().getEnumConstants())if(value.toString().equals("NONE"))f.set(boss,value);
                if(f.getName().endsWith("Cooldown")&&f.getType()==int.class)f.setInt(boss,100);
            }
            var participants=WorldBossEntity.class.getDeclaredField("participants");participants.setAccessible(true);((Map<?,?>)participants.get(boss)).clear();
            try{var method=boss.getClass().getDeclaredMethod("resetSkillTimers");method.setAccessible(true);method.invoke(boss);}catch(NoSuchMethodException ignored){}
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Resetting raid",e);}
    }
    private static Ledger ledger(MinecraftServer server) {
        if(server!=ledgerServer) {
            ledgerServer=server;
            CompoundTag tag=server.getCommandStorage().get(LEDGER_ID);
            StringBuilder saved=new StringBuilder(tag.getStringOr("state",""));
            for(int i=1;i<tag.getIntOr("state_chunks",1);i++)saved.append(tag.getStringOr("state_"+i,""));
            String json=saved.toString();
            ledger=json.isEmpty()?new Ledger():GSON.fromJson(json,Ledger.class);
        }
        return ledger;
    }
    private static void persist(MinecraftServer server) {
        CompoundTag tag=new CompoundTag();String json=GSON.toJson(ledger);
        int chunks=Math.max(1,(json.length()+15999)/16000);tag.putInt("state_chunks",chunks);
        for(int i=0;i<chunks;i++)tag.putString(i==0?"state":"state_"+i,json.substring(i*16000,Math.min(json.length(),(i+1)*16000)));
        server.getCommandStorage().set(LEDGER_ID,tag);
    }
    public static void reward(ServerLevel level,WorldBossEntity boss) {
        State s=state(boss);if(s.rewarded)return;s.rewarded=true;
        Ledger data=ledger(level.getServer());String encounter=boss.getUUID().toString();long now=level.getGameTime();
        String id=key(boss);int tier=RaidProfile.BOSSES.get(id).tier();
        for(var e:s.participants.entrySet()) {
            if(now-e.getValue()>1200)continue;
            String uuid=e.getKey().toString(),grantId=encounter+":"+uuid;
            if(!data.granted.add(grantId))continue;
            Grant g=new Grant();g.boss=id;g.encounter=encounter;g.tier=tier;g.xp=config(boss).xpReward;
            g.element=BuiltInRegistries.ITEM.getKey(boss.elementRewardItem()).toString();
            g.advancement=boss.advancementId();
            if(!(boss instanceof com.mki.worldbosses.entity.boss.RealmBossEntity)) {
                if(boss.fightDurationTicks()<=6000)g.challenges.add(boss.advancementId()+"_speed");
                var player=level.getServer().getPlayerList().getPlayer(e.getKey());
                if(!s.armored.contains(e.getKey())&&(player==null||boss.foughtWithoutArmor(player)))g.challenges.add(boss.advancementId()+"_unarmored");
            }
            g.star=tier==4&&data.firstClear.add(uuid+":"+id);
            data.pending.computeIfAbsent(uuid,x->new ArrayList<>()).add(g);
        }
        Vec3 h=home(boss);data.cooldowns.put(altarKey(level,BlockPos.containing(h).below()),now+36000);
        persist(level.getServer());
        for(ServerPlayer p:level.getServer().getPlayerList().getPlayers())deliver(p);
    }
    private static String altarKey(ServerLevel level,BlockPos pos){return level.dimension().identifier()+":"+pos.asLong();}
    public static boolean altarReady(ServerLevel level,BlockPos pos,Player player) {
        long until=ledger(level.getServer()).cooldowns.getOrDefault(altarKey(level,pos),0L);
        if(level.getGameTime()>=until)return true;
        player.sendOverlayMessage(Component.literal("This altar is recovering: "+((until-level.getGameTime()+19)/20)+" seconds remaining."));return false;
    }
    private static void playerTick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer p&&p.tickCount%20==0)deliver(p);
    }
    private static String cache(String boss) {
        return switch(boss){case "sylvaron"->"verdant";case "aureon"->"storm";case "nekhraz"->"dune";case "varkhul"->"infernal";case "morvath"->"soul";case "elyndra"->"void";case "shadowKing"->"umbral";case "dragonMother"->"dragon";case "stormGriffin"->"griffin";case "shadowWeaver"->"weaver";default->"primordial";};
    }
    private static void deliver(ServerPlayer p) {
        if(!p.isAlive()||p.isCreative()||p.isSpectator())return;
        var data=ledger(p.level().getServer());List<Grant> pending=data.pending.remove(p.getUUID().toString());
        if(pending==null)return;
        for(Grant grant:pending) {
            give(p,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:"+cache(grant.boss)+"_raid_cache"))));
            if(grant.element!=null) {
                var element=BuiltInRegistries.ITEM.getValue(Identifier.parse(grant.element));
                if(element!=Items.AIR&&!GearRetirement.hidden(new ItemStack(element)))give(p,new ItemStack(element));
            }
            var portalItem=BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:"+(grant.boss.equals("astrael")?"primordial_spark":"worldbreaker_dust")));
            if(portalItem!=Items.AIR)give(p,new ItemStack(portalItem,grant.boss.equals("astrael")?1:3));
            p.giveExperiencePoints(grant.xp);
            if(grant.advancement!=null)com.mki.worldbosses.server.ModAdvancements.grant(p,grant.advancement);
            for(String challenge:grant.challenges)com.mki.worldbosses.server.ModAdvancements.grant(p,challenge);
            var item=BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:"+snake(grant.boss)+"_trophy"));
            if(item!=Items.AIR)give(p,new ItemStack(item));
            if(grant.star)give(p,new ItemStack(Items.NETHER_STAR));
            p.sendSystemMessage(Component.literal("Raid cleared! Your personal reward cache has arrived."));
        }
        persist(p.level().getServer());
    }
    private static String snake(String s){return s.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);}
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false);}
    public static void openCache(ServerPlayer p,ItemStack stack,String id) {
        int tier=switch(id){case "verdant_raid_cache","storm_raid_cache","dune_raid_cache"->1;case "infernal_raid_cache","soul_raid_cache","void_raid_cache"->2;case "umbral_raid_cache","dragon_raid_cache","griffin_raid_cache"->3;case "primordial_raid_cache","weaver_raid_cache"->4;default->0;};
        if(tier==0||stack.isEmpty()||BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(Identifier.parse("worldbosses:"+id))==false)return;
        stack.shrink(1);
        give(p,new ItemStack(Items.GOLDEN_APPLE,new int[]{0,4,6,8,10}[tier]));
        give(p,new ItemStack(Items.NETHERITE_INGOT,tier));
        give(p,new ItemStack(Items.TOTEM_OF_UNDYING,tier==4?2:1));
        give(p,new ItemStack(Items.DIAMOND,new int[]{0,8,12,16,24}[tier]));
        int bottles=new int[]{0,32,48,64,96}[tier];
        while(bottles>0){int count=Math.min(64,bottles);give(p,new ItemStack(Items.EXPERIENCE_BOTTLE,count));bottles-=count;}
        p.sendSystemMessage(Component.literal("Raid cache opened: supplies and materials received."));
    }
}
