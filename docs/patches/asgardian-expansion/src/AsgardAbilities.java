package com.emandev.bifrost;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.spell_engine.api.effect.EntityActionsAllowed;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_power.api.SpellPower;

/** Finite, interruptible weapon actions. No casts or damage are decided by the client. */
@EventBusSubscriber(modid="bifrost")
public final class AsgardAbilities {
    static final Identifier MOVE=BifrostMod.id("ability_movement"), SPEED=BifrostMod.id("realm_movement"), VORTEX_SLOW=BifrostMod.id("vortex_slow");
    static final Map<UUID,Action> ACTIVE=new HashMap<>();
    static final Map<UUID,Long> BLOCKED=new HashMap<>(), SPEED_END=new HashMap<>();
    static final Map<UUID,Recovery> RECOVERY=new HashMap<>();
    static final Map<UUID,Map<String,Set<UUID>>> CHARGE_HITS=new HashMap<>();
    static final Map<LivingEntity,Long> SLOWS=new HashMap<>();
    static final class Shard {Vec3 position;final UUID target;final int launch;boolean returning,done;Shard(Vec3 point,UUID target,int launch){position=point;this.target=target;this.launch=launch;}}
    static final class Action {
        final String name,weapon;final Identifier dimension;final long start;final int windup;final ItemStack stack;final double damage;final Vec3 origin,direction;
        final Map<UUID,Double> dealt=new HashMap<>();final Set<UUID> charged=new HashSet<>(),vortexTargets=new HashSet<>();
        UUID beamTarget;int beamStacks;long lastBeamHit;int lastPulse=-1;boolean released;final List<Shard> shards=new ArrayList<>();
        Action(ServerPlayer p,String name){this.name=name;weapon=AsgardWeapons.kind(p.getMainHandItem());dimension=p.level().dimension().identifier();start=AsgardState.now(p);int base=switch(name){case "realm"->7;case "wave","slam"->16;case "berserk"->8;case "shield"->14;case "vortex"->12;default->0;};windup=base==0?0:Math.max(1,(int)Math.ceil(base/haste(p)));stack=p.getMainHandItem();damage=AsgardCombat.normalDamage(p,stack);origin=p.position();direction=p.getLookAngle().multiply(1,0,1).normalize();}
    }
    record Recovery(ItemStack weapon,long end,long next,int remaining){}
    private AsgardAbilities(){}
    public static List<String> describe(String weapon){return switch(weapon){
        case "hofund"->List.of("Bifrost Travel\nNamed markers in this dimension\n3s charge; 5s travel cooldown\nPrimary: hold right-click for a Bifrost Slash\n8 blocks; 0.8s charge; 12s cooldown\n160% first target, 100% others; up to 6 targets",
            "Realm Shift\n6 blocks; 0.35s cast; 35s cooldown\nSlows enemies for 2.5s; shorter on bosses/players\n+10% movement for 2.5s",
            "Hilt Bash / Counter\n3 blocks; instant; 10s cooldown\n60% weapon damage; 110% after blocking\nBrief interruption; bosses resist interruption",
            "Berserk Charging Slashes\n7-block lunge; 0.4s windup; 30s cooldown\nRequires below 40% health\n65% + 65% + 90% weapon damage");
        case "mjolnir"->List.of("Bifrost Travel\nNamed markers; 3s charge; 5s cooldown\nThrow: 20 blocks, 135% weapon damage, 0.6s lockout\nPrimary: hold right-click for a Lightning Beam\n16 blocks; up to 3s; 10s cooldown after release\n85% first hit, then 25% every 0.4s\nSame-target bonus up to 30%; 250% total cap\nThunder's Respite: five lightning charges prepare a heal\nNext qualifying hit heals 1 heart; 12s cooldown\nCharges expire after 20s out of combat\nBoth weapon heals share a 10s lockout",
            "Shattered Shard Swarm\n12 blocks; 1.5s; 22s cooldown\nUp to 5 targets, twice each at 45% weapon damage",
            "Lightning-Charged Shield Strike\n10 blocks; 0.7s cast; 25s cooldown\nRequires offhand shield\n170% first target; 90% others; up to 4 targets",
            "Propulsion Tornado Vortex\n5 blocks; 0.6s windup + 3s; 30s cooldown\n3 pulses of 35% weapon damage; up to 8 targets");
        case "stormbreaker"->List.of("Bifrost Travel\nNamed markers; 3s charge; 5s cooldown\nThrow: 20 blocks, 135% weapon damage, 0.6s lockout\nStormforged Recovery: below 40% health on a hostile Slam hit\nHeals 2 hearts over 4s; 60s cooldown\nBoth weapon heals share a 10s lockout",
            "Wakanda Ground Slam\n6 blocks; 0.8s windup; 35s cooldown\n180% weapon damage at center, 108% at edge\nUp to 12 targets; no terrain damage",
            "Multi-Target Ricochet\n24 blocks; instant; 16s cooldown\n125% first hit; 100% up to 3 bounces\n180% single-target hit when no bounce is available");
        default->List.of();};}
    static double haste(ServerPlayer p){double h=SpellPower.getHaste(p,ExternalSpellSchools.PHYSICAL_MELEE);return Double.isFinite(h)?Math.max(.1,h):1;}
    public static int cooldownTicks(ServerPlayer p,String action){
        int base=switch(action){case "realm"->700;case "wave"->240;case "bash","beam"->200;case "berserk","vortex"->600;case "shards"->440;case "shield"->500;case "slam"->700;case "ricochet"->320;default->0;};
        double floor=action.equals("realm")||action.equals("berserk")?.75:0;
        return Math.max(1,(int)Math.ceil(base*Math.max(floor,1/haste(p))));
    }
    static String cooldownKey(ServerPlayer p,String action){return AsgardWeapons.kind(p.getMainHandItem())+"/"+action;}
    static boolean start(ServerPlayer p,String name){
        if(!AsgardWeapons.ready(p,p.getMainHandItem()))return deny(p,"This weapon is unavailable. Check its owner and your race.");
        if(ACTIVE.containsKey(p.getUUID())||EntityActionsAllowed.isImpaired(p,EntityActionsAllowed.Player.CAST_SPELL)||((SpellCaster.Player)p).getSpellCastProcess()!=null)return deny(p,"Finish your current action first.");
        long remaining=AsgardState.get(p,cooldownKey(p,name))-AsgardState.now(p);
        if(remaining>0)return deny(p,"Recharging: "+((remaining+19)/20)+"s.");
        ACTIVE.put(p.getUUID(),new Action(p,name));AsgardState.put(p,cooldownKey(p,name),AsgardState.now(p)+cooldownTicks(p,name));return true;
    }
    static boolean deny(ServerPlayer p,String text){p.sendSystemMessage(Component.literal(text));return false;}
    public static boolean active(ServerPlayer p){return ACTIVE.containsKey(p.getUUID());}
    public static boolean travelAllowed(ServerPlayer p){return !active(p)&&!EntityActionsAllowed.isImpaired(p,EntityActionsAllowed.Player.CAST_SPELL)&&((SpellCaster.Player)p).getSpellCastProcess()==null;}
    public static boolean activate(ServerPlayer p,int skill){
        String weapon=AsgardWeapons.kind(p.getMainHandItem());
        if(skill==0){if(!AsgardWeapons.ready(p,p.getMainHandItem())||active(p))return deny(p,"This weapon cannot open the Bifrost now.");BifrostMenuService.open(p,null,p.getMainHandItem().getHoverName().getString());return true;}
        String name=switch(weapon){case "hofund"->switch(skill){case 1->"realm";case 2->"bash";case 3->"berserk";default->"";};case "mjolnir"->switch(skill){case 1->"shards";case 2->"shield";case 3->"vortex";default->"";};case "stormbreaker"->switch(skill){case 1->"slam";case 2->"ricochet";default->"";};default->"";};
        if(name.isEmpty())return false;
        if(name.equals("berserk")&&p.getHealth()>=p.getMaxHealth()*.4)return deny(p,"Berserk requires less than 40% health.");
        if(name.equals("shield")&&!p.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS))return deny(p,"Equip a shield in your offhand.");
        if(!start(p,name))return false;
        if(name.equals("ricochet")){
            ACTIVE.remove(p.getUUID());if(WeaponOwnership.throwWeapon(p,p.getMainHandItem(),true))return true;
            AsgardState.put(p,cooldownKey(p,name),0);return false;
        }
        if(name.equals("vortex"))movement(p,-.3);if(name.equals("shield")){p.stopUsingItem();movement(p,-1);}
        return true;
    }
    public static boolean beginPrimary(ServerPlayer p){
        String kind=AsgardWeapons.kind(p.getMainHandItem());String name=kind.equals("hofund")?"wave":kind.equals("mjolnir")?"beam":"";
        if(name.isEmpty()||!start(p,name))return false;if(name.equals("wave"))movement(p,-.2);return true;
    }
    public static void endPrimary(ServerPlayer p){var a=ACTIVE.get(p.getUUID());if(a==null)return;
        if(a.name.equals("wave")&&valid(p,a))wave(p,a);
        if(a.name.equals("wave")||a.name.equals("beam"))finish(p,a,true);
    }
    static void movement(ServerPlayer p,double amount){var attr=p.getAttribute(Attributes.MOVEMENT_SPEED);attr.removeModifier(MOVE);if(amount!=0)attr.addTransientModifier(new AttributeModifier(MOVE,amount,amount==-1?AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL:AttributeModifier.Operation.ADD_MULTIPLIED_BASE));}
    static void finish(ServerPlayer p,Action a,boolean cooldownFromEnd){
        ACTIVE.remove(p.getUUID());movement(p,0);
        if(a.name.equals("slam")&&p.level().dimension().identifier().equals(a.dimension)&&p.position().distanceToSqr(a.origin)<64&&p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(a.origin))&&p.level().noCollision(p,p.getBoundingBox().move(a.origin.subtract(p.position())))){p.teleportTo(a.origin.x,a.origin.y,a.origin.z);p.resetFallDistance();}
        if(cooldownFromEnd)AsgardState.put(p,a.weapon+"/"+a.name,AsgardState.now(p)+cooldownTicks(p,a.name));
        if(a.name.equals("wave")||a.name.equals("beam"))p.stopUsingItem();
    }
    static boolean valid(ServerPlayer p,Action a){return p.level().dimension().identifier().equals(a.dimension)&&AsgardWeapons.ready(p,p.getMainHandItem())&&p.getMainHandItem()==a.stack&&!EntityActionsAllowed.isImpaired(p,EntityActionsAllowed.Player.CAST_SPELL)&&((SpellCaster.Player)p).getSpellCastProcess()==null;}
    static boolean hit(ServerPlayer p,Action a,LivingEntity target,double coefficient,double cap,boolean lightning){
        double used=a.dealt.getOrDefault(target.getUUID(),0d),allowed=Math.min(coefficient,Math.max(0,cap-used));if(allowed<=0)return false;
        // Count attempted damage against the cast cap, including blocks/resistance/invulnerability.
        a.dealt.put(target.getUUID(),used+allowed);
        boolean success=AsgardCombat.hitRaw(p,target,a.damage*allowed,lightning);
        if(success&&lightning&&a.charged.add(target.getUUID()))lightningHit(p,target,a.name+"/"+a.start);
        return success;
    }
    static void beamParticles(ServerPlayer p,Vec3 end){Vec3 start=p.getEyePosition();Vec3 d=end.subtract(start);int n=Math.min(40,(int)(d.length()*2)+1);for(int i=0;i<n;i++){Vec3 v=start.add(d.scale((double)i/n));p.level().sendParticles(ParticleTypes.ELECTRIC_SPARK,v.x,v.y,v.z,1,.02,.02,.02,0);}}
    static void burst(ServerPlayer p,Vec3 at){p.level().sendParticles(ParticleTypes.END_ROD,at.x,at.y+.5,at.z,32,1.5,.7,1.5,.03);}
    static void wave(ServerPlayer p,Action a){
        var targets=AsgardCombat.line(p,8,1,6);int i=0;for(var target:targets)hit(p,a,target,i++==0?1.6:1,1.6,false);
        Vec3 start=p.getEyePosition(),end=AsgardCombat.aimEnd(p,8),delta=end.subtract(start);for(int j=0;j<20;j++){Vec3 at=start.add(delta.scale(j/20d));p.level().sendParticles(ParticleTypes.WITCH,at.x,at.y,at.z,3,.6,.4,.6,.01);}
    }
    public static void tick(MinecraftServer server){
        for(UUID id:List.copyOf(ACTIVE.keySet())){
            var a=ACTIVE.get(id);var p=server.getPlayerList().getPlayer(id);
            if(p==null){ACTIVE.remove(id);continue;}
            if(!valid(p,a)){finish(p,a,true);continue;}
            int t=(int)(AsgardState.now(p)-a.start);boolean done=false;
            switch(a.name){
                case "realm"->{if(t>=a.windup){
                    for(var target:AsgardCombat.targets(p,p.position(),6,64)){boolean boss=AsgardCombat.boss(target);boolean other=target instanceof Player;int duration=boss||other?25:50;AsgardCombat.effect(p,target,MobEffects.SLOWNESS,duration,boss?0:other?1:3);AsgardCombat.effect(p,target,MobEffects.MINING_FATIGUE,duration,1);}
                    var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);speed.removeModifier(SPEED);speed.addTransientModifier(new AttributeModifier(SPEED,.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));SPEED_END.put(id,AsgardState.now(p)+50);burst(p,p.position());ACTIVE.remove(id);continue;
                }}
                case "wave"->{if(t>=a.windup){wave(p,a);done=true;}}
                case "bash"->{
                    boolean counter=AsgardState.now(p)-BLOCKED.getOrDefault(id,Long.MIN_VALUE/2)<=12;
                    var targets=AsgardCombat.line(p,3,.8,1);for(var target:targets){if(hit(p,a,target,counter?1.1:.6,1.1,false)&&!AsgardCombat.boss(target))AsgardCombat.stun(p,target,target instanceof Player?5:counter?15:10);}
                    burst(p,p.getEyePosition().add(p.getLookAngle()));done=true;
                }
                case "berserk"->{
                    if(t>=a.windup&&t<a.windup+16){Vec3 delta=a.direction.scale(7d/16);var dest=p.position().add(delta);if(!p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(dest))||!p.level().noCollision(p,p.getBoundingBox().move(delta))){done=true;break;}p.teleportTo(dest.x,dest.y,dest.z);p.resetFallDistance();}
                    if(t==a.windup||t==a.windup+8||t==a.windup+16){for(var target:AsgardCombat.line(p,3,1,6))hit(p,a,target,t==a.windup+16?.9:.65,2.2,false);burst(p,p.position());}if(t>=a.windup+24)done=true;
                }
                case "beam"->{
                    if(t<60&&(a.lastPulse<0||t-a.lastPulse>=8)){a.lastPulse=t;var list=AsgardCombat.line(p,16,.35,1);beamParticles(p,AsgardCombat.aimEnd(p,16));
                        if(!list.isEmpty()){var target=list.getFirst();boolean consecutive=target.getUUID().equals(a.beamTarget)&&AsgardState.now(p)-a.lastBeamHit<=20;
                            double coefficient=consecutive?.25*(1+.1*a.beamStacks):.85;
                            if(hit(p,a,target,coefficient,2.5,true)){a.beamStacks=consecutive?Math.min(3,a.beamStacks+1):1;a.beamTarget=target.getUUID();a.lastBeamHit=AsgardState.now(p);}
                        }}if(t>=60)done=true;
                }
                case "shards"->{
                    if(a.lastPulse<0){a.lastPulse=t;var choices=AsgardCombat.targets(p,p.position(),12,5);if(!choices.isEmpty())for(int i=0;i<Math.min(8,choices.size()*2);i++)a.shards.add(new Shard(p.getEyePosition(),choices.get(i%choices.size()).getUUID(),i<choices.size()?0:15));}
                    for(var shard:a.shards){if(shard.done||t<shard.launch)continue;var entity=p.level().getEntity(shard.target);Vec3 end=p.getEyePosition();
                        if(!shard.returning&&entity instanceof LivingEntity target&&AsgardCombat.eligible(p,target)&&p.distanceToSqr(target)<=144){end=target.getBoundingBox().getCenter();}else shard.returning=true;
                        Vec3 delta=end.subtract(shard.position);if(delta.length()<.7){if(!shard.returning&&entity instanceof LivingEntity target)hit(p,a,target,.45,.9,true);if(shard.returning)shard.done=true;shard.returning=true;}
                        else {Vec3 next=shard.position.add(delta.normalize().scale(Math.min(1.3,delta.length())));var wall=p.level().clip(new net.minecraft.world.level.ClipContext(shard.position,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));if(!shard.returning&&wall.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)shard.returning=true;else shard.position=next;}
                        p.level().sendParticles(ParticleTypes.ELECTRIC_SPARK,shard.position.x,shard.position.y,shard.position.z,2,.04,.04,.04,.01);
                    }if(t>=30)done=true;
                }
                case "shield"->{p.setDeltaMovement(Vec3.ZERO);if(p.level().noCollision(p,p.getBoundingBox().move(a.origin.subtract(p.position()))))p.teleportTo(a.origin.x,a.origin.y,a.origin.z);if(t>=a.windup){int index=0;for(var target:AsgardCombat.line(p,10,1,4))hit(p,a,target,index++==0?1.7:.9,1.7,true);beamParticles(p,AsgardCombat.aimEnd(p,10));done=true;}}
                case "vortex"->{
                    if(t>=a.windup&&t<a.windup+60){for(var target:AsgardCombat.targets(p,p.position(),5,64)){
                        if(!a.vortexTargets.contains(target.getUUID())){if(a.vortexTargets.size()>=8)continue;a.vortexTargets.add(target.getUUID());}
                        if(!AsgardCombat.boss(target)&&!(target instanceof Player)){Vec3 delta=p.position().subtract(target.position()).multiply(1,0,1).normalize().scale(.035);target.setDeltaMovement(target.getDeltaMovement().add(delta));}
                        if(target instanceof Player){var attr=target.getAttribute(Attributes.MOVEMENT_SPEED);attr.removeModifier(VORTEX_SLOW);attr.addTransientModifier(new AttributeModifier(VORTEX_SLOW,-.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));SLOWS.put(target,AsgardState.now(p)+10);}else AsgardCombat.effect(p,target,MobEffects.SLOWNESS,10,1);
                        if((t-a.windup)%20==0&&a.lastPulse!=t)hit(p,a,target,.35,1.05,true);
                    }if((t-a.windup)%20==0){a.lastPulse=t;burst(p,p.position());}}if(t>=a.windup+60)done=true;
                }
                case "slam"->{
                    if(a.lastPulse<0){a.lastPulse=t;Vec3 lift=new Vec3(0,4,0);if(!p.level().noCollision(p,p.getBoundingBox().expandTowards(lift))||!p.level().getBlockState(net.minecraft.core.BlockPos.containing(a.origin).below()).isSolid()){deny(p,"Ground Slam needs solid footing and four blocks of clear space above.");done=true;break;}p.teleportTo(a.origin.x,a.origin.y+4,a.origin.z);p.resetFallDistance();}
                    if(t>=a.windup){if(!p.level().noCollision(p,p.getBoundingBox().move(a.origin.subtract(p.position())))){done=true;break;}p.teleportTo(a.origin.x,a.origin.y,a.origin.z);p.resetFallDistance();boolean success=false;
                        for(var target:AsgardCombat.targets(p,a.origin,6,12)){double distance=target.position().distanceTo(a.origin);boolean confirmed=hit(p,a,target,1.8*(1-.4*Math.min(1,distance/6)),1.8,false);success|=confirmed&&hostileForHealing(target);}
                        burst(p,a.origin);if(success&&p.getHealth()<p.getMaxHealth()*.4)stormRecovery(p,a.stack);done=true;
                    }
                }
            }
            if(done)finish(p,a,a.name.equals("beam")||a.name.equals("wave"));
        }
        for(UUID id:List.copyOf(SPEED_END.keySet())){var p=server.getPlayerList().getPlayer(id);if(p==null||AsgardState.now(p)>=SPEED_END.get(id)||!AsgardianAccess.canUse(p)){if(p!=null)p.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED);SPEED_END.remove(id);}}
        for(var target:List.copyOf(SLOWS.keySet()))if(!target.isAlive()||target.isRemoved()||server.overworld().getGameTime()>=SLOWS.get(target)){target.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(VORTEX_SLOW);SLOWS.remove(target);}
        for(UUID id:List.copyOf(RECOVERY.keySet())){var r=RECOVERY.get(id);var p=server.getPlayerList().getPlayer(id);var record=p==null?null:WeaponOwnership.record(server,r.weapon);if(p==null||!AsgardianAccess.canUse(p)||record==null||!record.owner().equals(p.getUUID().toString())||record.state().equals(WeaponOwnership.WAITING)||AsgardState.now(p)>r.end){RECOVERY.remove(id);continue;}if(AsgardState.now(p)>=r.next){float hp=p.getHealth();p.heal(1);if(p.getHealth()>hp)AsgardState.put(p,"healing/shared",AsgardState.now(p)+200);if(r.remaining<=1)RECOVERY.remove(id);else RECOVERY.put(id,new Recovery(r.weapon,r.end,r.next+20,r.remaining-1));}}
    }
    static boolean hostileForHealing(LivingEntity target){return (target instanceof Enemy||target instanceof Player)&&!target.getType().toString().contains("dummy");}
    public static void lightningHit(ServerPlayer p,LivingEntity target,String castToken){
        if(!AsgardianAccess.canUse(p)||!hostileForHealing(target))return;
        long now=AsgardState.now(p);if(now-Math.max(AsgardState.get(p,"thunder/last"),AsgardState.get(p,"combat/last"))>400){AsgardState.put(p,"thunder/charges",0);CHARGE_HITS.remove(p.getUUID());}
        var casts=CHARGE_HITS.computeIfAbsent(p.getUUID(),id->new HashMap<>());if(!casts.computeIfAbsent(castToken,id->new HashSet<>()).add(target.getUUID()))return;
        if(casts.size()>64){casts.clear();casts.computeIfAbsent(castToken,id->new HashSet<>()).add(target.getUUID());}
        AsgardState.put(p,"thunder/last",now);long charges=AsgardState.get(p,"thunder/charges");
        if(charges>=5&&now>=AsgardState.get(p,"thunder/heal")&&now>=AsgardState.get(p,"healing/shared")){
            p.heal(2);AsgardState.put(p,"thunder/charges",0);AsgardState.put(p,"thunder/heal",now+240);AsgardState.put(p,"healing/shared",now+200);
        }else AsgardState.put(p,"thunder/charges",Math.min(5,charges+1));
    }
    static void stormRecovery(ServerPlayer p,ItemStack stack){long now=AsgardState.now(p);if(now<AsgardState.get(p,"storm/heal")||now<AsgardState.get(p,"healing/shared"))return;
        AsgardState.put(p,"storm/heal",now+1200);AsgardState.put(p,"healing/shared",now+200);RECOVERY.put(p.getUUID(),new Recovery(stack,now+80,now+20,4));}
    @SubscribeEvent public static void blocked(LivingShieldBlockEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getBlockedDamage()>0)BLOCKED.put(p.getUUID(),AsgardState.now(p));}
    @SubscribeEvent public static void combat(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){if(e.getInflictedDamage()>0){if(e.getEntity() instanceof ServerPlayer p)AsgardState.put(p,"combat/last",AsgardState.now(p));if(e.getSource().getEntity() instanceof ServerPlayer p)AsgardState.put(p,"combat/last",AsgardState.now(p));}}
    @SubscribeEvent public static void falling(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p){var a=ACTIVE.get(p.getUUID());if(a!=null&&a.name.equals("slam"))e.setDamageMultiplier(0);}}
    public static void reset(){ACTIVE.clear();BLOCKED.clear();SPEED_END.clear();RECOVERY.clear();CHARGE_HITS.clear();for(var target:SLOWS.keySet())target.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(VORTEX_SLOW);SLOWS.clear();}
}
