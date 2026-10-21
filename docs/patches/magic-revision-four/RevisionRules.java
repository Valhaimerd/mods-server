package net.spell_engine.internals;

import java.lang.reflect.Field;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;

public final class RevisionRules {
    public static float cooldown(LivingEntity caster, Holder<Spell> holder, float progress) {
        String id = holder.unwrapKey().orElseThrow().identifier().toString();
        if (id.equals("wizards:arcane_missile") || id.equals("wizards:arcane_beam") || id.equals("wizards:fire_breath")) {
            return SpellParameters.getCooldownDuration(caster, holder) * (.5f + .5f * Math.clamp(progress, 0f, 1f));
        }
        return SecondTierRules.cooldown(caster, holder, progress);
    }

    public static boolean charged(LivingEntity caster, Holder<Spell> holder) {
        if (!(caster instanceof Player player)) return false;
        return SpellModifiers.of(player, holder).stream().anyMatch(m -> m.channel_ticks_add == 25 && "wizards:arcane_beam".equals(m.spell_pattern));
    }

    public static float duration(float base, LivingEntity caster, Spell spell) {
        var beam = SpellRegistry.from(caster.level()).get(Identifier.parse("wizards:arcane_beam")).orElse(null);
        return beam != null && beam.value() == spell && charged(caster, beam) ? 10f : base;
    }

    public static float chargedCoefficient(int deliveryIndex) {
        // Base channel normalization remains 5 / 25 = 0.2.
        return Math.min(.1f + .0015f * Math.max(deliveryIndex, 0), .15f) / .2f;
    }

    public static float chargedWidth(int deliveryIndex) {
        return .08f * Math.min(1.3f + .10f * Math.max(deliveryIndex, 0), 5f);
    }

    public static Spell.Target.Beam beam(SpellCaster.Entity caster) {
        Spell spell = caster.getCastedSpell();
        if (spell == null || spell.target == null || spell.target.type != Spell.Target.Type.BEAM) return null;
        var process = caster.getSpellCastProcess();
        Spell.Target.Beam appearance = spell.target.beam;
        if (process == null || !process.id().toString().equals("wizards:arcane_beam")
                || process.length() * process.speed() < 150 || !(caster instanceof LivingEntity entity)) return appearance;
        // Process length/speed is synchronized to viewers, so remote players see the growth too.
        float interval = process.length() / 50f;
        int completed = (int)Math.floor((process.spellCastTicksSoFar(entity.level().getGameTime()) + interval * .5f) / interval);
        Spell.Target.Beam result = copy(appearance);
        result.width = chargedWidth(Math.max(completed - 1, 0));
        return result;
    }

    public static Spell.Impact impact(LivingEntity caster, Holder<Spell> holder, Spell.Impact impact, SpellExecution.ImpactContext context) {
        String id = holder.unwrapKey().orElseThrow().identifier().toString();
        boolean damage = impact.action.type == Spell.Impact.Action.Type.DAMAGE;
        if (id.equals("wizards:arcane_beam") && damage && charged(caster, holder)) {
            Spell.Impact result = copyImpact(impact);
            result.action.damage.spell_power_coefficient = chargedCoefficient(context.channelTickIndex());
            return result;
        }
        if (id.equals("wizards:arcane_explosion") && damage && echo(caster)) {
            Spell.Impact result = copyImpact(impact);
            result.action.damage.spell_power_coefficient = 1.8f;
            return result;
        }
        if (id.equals("wizards:frost_lance") && context.focusMode() == SpellTarget.FocusMode.AREA) {
            if (damage) {
                Spell.Impact result = copyImpact(impact);
                result.action.damage.spell_power_coefficient = .5f;
                return result;
            }
            if (impact.action.type == Spell.Impact.Action.Type.STATUS_EFFECT && impact.action.status_effect.effect_id.equals("wizards:frozen")) {
                Spell.Impact result = copyImpact(impact);
                result.action.status_effect.duration = 1f;
                return result;
            }
        }
        return impact;
    }

    private static boolean echo(LivingEntity caster) {
        if (!(caster instanceof Player player)) return false;
        return SpellContainerSource.passiveSpellsOf(player).stream().anyMatch(h -> h.unwrapKey().orElseThrow().identifier().toString().equals("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1"));
    }

    private static Spell.Impact copyImpact(Spell.Impact original) {
        Spell.Impact result = copy(original);
        result.action = copy(original.action);
        if (original.action.damage != null) result.action.damage = copy(original.action.damage);
        if (original.action.status_effect != null) result.action.status_effect = copy(original.action.status_effect);
        return result;
    }

    private static <T> T copy(T original) {
        try {
            @SuppressWarnings("unchecked") T result = (T)original.getClass().getDeclaredConstructor().newInstance();
            for (Field field : original.getClass().getFields()) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) field.set(result, field.get(original));
            }
            return result;
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }
}
