package com.emandev.bifrost;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.spell_engine.internals.PartyRules;
import net.spell_engine.internals.target.*;

/** All expansion hits use native hurt, shields, protections and pack PvP scaling once. */
public final class AsgardCombat {
    private AsgardCombat(){}
    public static double normalDamage(ServerPlayer player,ItemStack stack){return Math.max(0,player.getAttributeValue(Attributes.ATTACK_DAMAGE));}
    public static boolean eligible(ServerPlayer player,LivingEntity target){
        if(target==player||!target.isAlive()||target.isInvulnerable()||target.isSpectator()||target.level()!=player.level())return false;
        // The pack deliberately protects all player-owned pets, including another party's dog.
        if(target instanceof OwnableEntity owned&&owned.getOwnerReference()!=null)return false;
        if(target instanceof Player other && (AsgardianAccess.downed(other)||!player.level().isPvpAllowed()||!player.canHarmPlayer(other)))return false;
        return EntityRelations.actionAllowed(SpellTarget.FocusMode.AREA,SpellTarget.Intent.HARMFUL,player,target);
    }
    public static boolean visible(ServerPlayer player,LivingEntity target){return player.hasLineOfSight(target);}
    public static List<LivingEntity> targets(ServerPlayer player,Vec3 center,double radius,int limit){
        return player.level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius),e->eligible(player,e)&&e.distanceToSqr(center)<=radius*radius&&visible(player,e)).stream().sorted(Comparator.comparingDouble(e->e.distanceToSqr(center))).limit(limit).toList();
    }
    public static boolean hitRaw(ServerPlayer player,LivingEntity target,double raw,boolean lightning){
        if(!AsgardianAccess.canUse(player)||!eligible(player,target)||!Double.isFinite(raw)||raw<=0)return false;
        DamageSource source=lightning?new DamageSource(player.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).get(BifrostMod.id("lightning")).orElseThrow(),player):player.damageSources().playerAttack(player);
        float before=target.getHealth()+target.getAbsorptionAmount();
        boolean accepted=target.hurtServer(player.level(),source,(float)raw);
        return accepted && target.getHealth()+target.getAbsorptionAmount()<before;
    }
    public static Vec3 aimEnd(ServerPlayer player,double range){
        var start=player.getEyePosition();var end=start.add(player.getLookAngle().scale(range));
        var hit=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));return hit.getLocation();
    }
    public static List<LivingEntity> line(ServerPlayer player,double range,double width,int limit){
        Vec3 start=player.getEyePosition(),end=aimEnd(player,range);if(start.distanceToSqr(end)<.0001)return List.of();
        return player.level().getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(width),e->eligible(player,e)&&e.getBoundingBox().inflate(width).clip(start,end).isPresent()).stream()
            .sorted(Comparator.comparingDouble(e->e.getBoundingBox().inflate(width).clip(start,end).orElse(e.position()).distanceToSqr(start))).limit(limit).toList();
    }
    public static void effect(ServerPlayer caster,LivingEntity target,net.minecraft.core.Holder<MobEffect> effect,int ticks,int amplifier){
        if(eligible(caster,target))PartyRules.addEffect(target,new MobEffectInstance(effect,ticks,amplifier),caster);
    }
    public static void stun(ServerPlayer caster,LivingEntity target,int ticks){BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("spell_engine:stun")).ifPresent(e->effect(caster,target,e,ticks,0));}
    public static boolean boss(LivingEntity entity){return entity.is(net.minecraft.tags.TagKey.create(Registries.ENTITY_TYPE,Identifier.parse("c:bosses")))||entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon||entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss;}
}
