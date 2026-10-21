package net.wizards.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.delivery.SpellDelivery;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellPower;

public final class ShieldRework {
    private static boolean installed;
    public static void install() {
        if (installed) return;
        installed = true;
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingIncomingDamageEvent.class, ShieldRework::damage);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Expired.class, ShieldRework::expired);
    }
    private static boolean shielded(LivingEntity entity) {
        return entity.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frost_shield")).orElseThrow());
    }
    private static void damage(LivingIncomingDamageEvent event) {
        if (!event.getEntity().level().isClientSide() && shielded(event.getEntity())) event.setCanceled(true);
    }
    private static void expired(MobEffectEvent.Expired event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide() || !entity.isAlive() || event.getEffectInstance() == null
                || !event.getEffectInstance().getEffect().is(Identifier.parse("wizards:frost_shield"))) return;
        var burst = SpellRegistry.from(entity.level()).get(Identifier.parse("wizards:frost_shield_burst")).orElseThrow();
        var targets = SpellTarget.findTargets(entity, burst, SpellTarget.SearchResult.empty(), true);
        var context = new SpellExecution.ImpactContext().power(SpellPower.getSpellPower(burst.value().school, entity))
            .position(entity.position()).target(SpellTarget.FocusMode.AREA);
        SpellDelivery.resolveAndDeliver(entity.level(), entity, burst, targets, context, null);
        net.spell_engine.fx.ReleaseFx.send(entity.level(), entity, burst, 1f);
    }
}
