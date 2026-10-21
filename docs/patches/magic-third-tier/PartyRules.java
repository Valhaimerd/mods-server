package net.spell_engine.internals;

import dev.betterparty.api.v1.server.BetterPartyServerApi;
import java.util.UUID;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.compat.MultipartEntityCompat;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.SpellIntents;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchool;

/** One policy for acquisition, projectile collision, summons and final impacts. */
public final class PartyRules {
    private PartyRules() {}

    private static UUID principal(Entity entity) {
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null)
            return ownable.getOwnerReference().getUUID();
        return entity.getUUID();
    }

    public static boolean allies(LivingEntity caster, Entity givenTarget) {
        Entity target = MultipartEntityCompat.coalesce(givenTarget);
        if (caster == target) return true;
        UUID first = principal(caster), second = principal(target);
        if (first.equals(second)) return true;
        if (caster.level() instanceof ServerLevel world)
            return BetterPartyServerApi.arePartyMembers(world.getServer(), first, second);
        return Client.allies(first, second);
    }

    // Keep the client-only API out of the dedicated server's class resolution path.
    private static final class Client {
        static boolean allies(UUID first, UUID second) {
            return dev.betterparty.api.v1.client.BetterPartyClientApi.arePartyMembers(first, second);
        }
    }

    public static EntityRelation relation(LivingEntity caster, Entity target) {
        return allies(caster, target) ? EntityRelation.ALLY : EntityRelation.HOSTILE;
    }

    public static boolean harmfulAllowed(LivingEntity caster, Entity target) {
        return !allies(caster, target);
    }

    public static boolean impactAllowed(Level world, LivingEntity givenCaster, Entity target,
            Spell.Impact impact, SpellExecution.ImpactContext context) {
        LivingEntity effective = context.effectiveCaster(world);
        LivingEntity caster = effective != null ? effective : givenCaster;
        if (impact.action.apply_to_caster) {
            // Explicit self statuses include intentional tradeoffs on otherwise helpful buffs.
            // Offensive damage/fire cannot exploit this bypass to injure the caster.
            return impact.action.type != Spell.Impact.Action.Type.DAMAGE
                && impact.action.type != Spell.Impact.Action.Type.FIRE;
        }
        boolean helpful = SpellIntents.impactIntent(impact.action) == SpellTarget.Intent.HELPFUL;
        return helpful == allies(caster, target);
    }

    private static boolean control(Spell.Impact impact) {
        return impact.action.type == Spell.Impact.Action.Type.STATUS_EFFECT
            && impact.action.status_effect.remove == null
            && control(impact.action.status_effect.effect_id);
    }

    private static boolean control(String id) {
        return "spell_engine:stun".equals(id) || "minecraft:slowness".equals(id) || "wizards:frozen".equals(id)
            || "wizards:frost_slowness".equals(id) || "skill_tree_rpgs:blizzard_slow".equals(id);
    }

    public static boolean underApplyLimit(SpellPower.Result power, LivingEntity target, SpellSchool school,
            Spell.Impact.Action.StatusEffect.ApplyLimit limit, Spell.Impact impact) {
        return control(impact) || SpellImpacts.underApplyLimit(power, target, school, limit);
    }

    public static boolean bossGate(Entity target, TagKey<EntityType<?>> tag, Spell.Impact impact) {
        return !control(impact) && target.is(tag);
    }

    public static boolean addEffect(LivingEntity target, MobEffectInstance effect, Entity source) {
        String id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
        boolean applied = target.addEffect(effect, source);
        if (applied || !control(id)) return applied;
        // forceAddEffect also calls canBeAffected in 26.2. Use native callbacks
        // when a type/status immunity rejected this explicitly universal frost effect.
        MobEffectInstance current = target.getEffect(effect.getEffect());
        try {
            if (current == null) {
                target.getActiveEffectsMap().put(effect.getEffect(), effect);
                EffectCallbacks.ADDED.invoke(target, effect, source);
                return true;
            }
            if (!current.update(effect)) return false;
            EffectCallbacks.UPDATED.invoke(target, current, true, source);
            return true;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Universal control effect callbacks failed", failure);
        }
    }

    private static final class EffectCallbacks {
        static final Method ADDED = method("onEffectAdded", MobEffectInstance.class, Entity.class);
        static final Method UPDATED = method("onEffectUpdated", MobEffectInstance.class, boolean.class, Entity.class);
        private static Method method(String name, Class<?>... parameters) {
            try {
                Method result = LivingEntity.class.getDeclaredMethod(name, parameters);
                result.setAccessible(true);
                return result;
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Missing native frost effect callback: " + name, failure);
            }
        }
    }

    public static void ignite(Entity target, float seconds) {
        // Eligibility is universal; subsequent fire damage still follows native immunity,
        // Fire Resistance, damage events and the pack's player damage scaling.
        int ticks = (int) Math.floor(seconds * 20f);
        if (target.getRemainingFireTicks() < ticks) target.setRemainingFireTicks(ticks);
    }
}
