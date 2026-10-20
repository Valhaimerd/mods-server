package net.spell_engine.internals;

import java.lang.reflect.Method;
import java.util.Collection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.utils.WorldScheduler;

public final class SecondTierRules {
    private static final ThreadLocal<Boolean> DELAYED = ThreadLocal.withInitial(() -> false);

    public static float cooldown(LivingEntity caster, Holder<Spell> holder, float progress) {
        var spell = holder.value();
        float full = SpellParameters.getCooldownDuration(caster, holder);
        String id = holder.unwrapKey().orElseThrow().identifier().toString();
        if (id.equals("wizards:arcane_beam")) return full * (2f / 3f + Math.clamp(progress, 0f, 1f) / 3f);
        if (id.equals("wizards:fire_storm")) return full * (.6f + .4f * Math.clamp(progress, 0f, 1f));
        return spell.cost.cooldown.proportional ? full * progress : full;
    }

    public static boolean delayFreeze(Level world, LivingEntity caster, Entity target,
            Holder<Spell> holder, Spell.Impact impact, SpellExecution.ImpactContext context,
            Collection<ServerPlayer> trackers) {
        if (DELAYED.get() || world.isClientSide() || impact.action.type != Spell.Impact.Action.Type.STATUS_EFFECT
                || !holder.unwrapKey().orElseThrow().identifier().toString().equals("wizards:frost_lance")
                || !impact.action.status_effect.effect_id.equals("wizards:frozen")) return false;
        ((WorldScheduler) world).schedule(10, () -> {
            if (!target.isAlive() || target.level() != world || !caster.isAlive() || caster.level() != world) return;
            try {
                DELAYED.set(true);
                Method method = SpellImpacts.class.getDeclaredMethod("performImpact", Level.class,
                    LivingEntity.class, Entity.class, Holder.class, Spell.Impact.class,
                    SpellExecution.ImpactContext.class, Collection.class);
                method.setAccessible(true);
                method.invoke(null, world, caster, target, holder, impact, context, trackers);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Delayed Ice Lance freeze failed", failure);
            } finally { DELAYED.remove(); }
        });
        return true;
    }
}
