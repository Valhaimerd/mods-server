package net.spell_engine.internals;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_engine.rpg_series.config.EffectConfig;
import net.spell_engine.utils.WorldScheduler;

/** Third-tier rules retain native teleport collision checks and shared impact permissions. */
public final class ThirdTierRules {
    private static boolean installed;
    public static void install() {
        if (installed) return;
        installed = true;
        WallRules.init();
        SpellHandlers.registerCustomDelivery(Identifier.parse("wizards:burst_blink"), ThirdTierRules::blink);
    }

    public static void configureEffects(ConfigFile.Effects config) {
        var op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
        config.effects.put("wizards:arcane_evocation", new EffectConfig(List.of(
            new net.spell_engine.rpg_series.config.AttributeModifier("spell_power:critical_chance", .05f, op),
            new net.spell_engine.rpg_series.config.AttributeModifier("spell_power:haste", .03f, op),
            new net.spell_engine.rpg_series.config.AttributeModifier("spell_engine:damage_taken", .1f, op))));
    }

    public static float cooldown(LivingEntity caster, Holder<Spell> holder, float progress) {
        if (holder.is(Identifier.parse("wizards:frost_blizzard"))) {
            return SpellParameters.getCooldownDuration(caster, holder) * (.6f + .4f * Math.clamp(progress, 0f, 1f));
        }
        return RevisionRules.cooldown(caster, holder, progress);
    }

    public static boolean blink(Level world, Holder<Spell> holder, LivingEntity caster,
            List<SpellExecution.DeliveryTarget> targets, SpellExecution.ImpactContext context, Vec3 location) {
        if (world.isClientSide()) return false;
        Vec3 departure = caster.position();
        boolean performed = SpellImpacts.performImpacts(world, caster, caster, caster, holder,
            holder.value().impacts, context, false, Spell.Impact.Action.Type.TELEPORT);
        Vec3 arrival = caster.position();
        if (!performed || departure.distanceToSqr(arrival) < .001) return false;
        // A blocked teleport must not grant a free Purge movement buff.
        SpellImpacts.performImpacts(world, caster, caster, caster, holder,
            holder.value().impacts, context, false, Spell.Impact.Action.Type.STATUS_EFFECT);
        burst(world, caster, departure, context);
        ((WorldScheduler)world).schedule(5, () -> {
            if (caster.isAlive() && !caster.isRemoved() && caster.level() == world) burst(world, caster, arrival, context);
        });
        return true;
    }

    public static void burst(Level world, LivingEntity caster, Vec3 location, SpellExecution.ImpactContext context) {
        var holder = SpellRegistry.from(world).get(Identifier.parse("wizards:burst_blink_explosion")).orElseThrow();
        // Fixed world positions, including if the caster moves again during the five-tick delay.
        SpellImpacts.lookupAndPerformAreaImpact(holder.value().area_impact, holder, caster, caster, null,
            holder.value().impacts, context.position(location.add(0, .9, 0)), false);
    }
}
