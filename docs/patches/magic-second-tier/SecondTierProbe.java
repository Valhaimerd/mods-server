package local.audit;

import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;

@Mod("overhaul_probe")
public class SecondTierProbe {
    int checks; List<String> out = new ArrayList<>();
    void check(String name, double actual, double expected) {
        if (Math.abs(actual - expected) > .003) throw new AssertionError(name + " " + actual + " != " + expected);
        checks++; out.add(name + ": " + actual);
    }
    public SecondTierProbe() { NeoForge.EVENT_BUS.addListener(this::start); }
    void start(ServerStartedEvent event) {
        var server = event.getServer(); var level = server.overworld();
        try {
            var p = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.fromString("36baa39f-f31a-41ae-bf89-682889d12589"), "TierTwo"));
            p.setPos(0, 80, 0);
            var cooldown = net.spell_engine.internals.cost.SpellCooldowns.class.getDeclaredMethod("cooldownToSet", LivingEntity.class, net.minecraft.core.Holder.class, float.class);
            cooldown.setAccessible(true);
            for (String id : List.of("arcane_beam", "fire_storm")) {
                var holder = SpellRegistry.from(level).get(Identifier.parse("wizards:" + id)).orElseThrow();
                for (float progress : new float[]{0, .5f, 1}) {
                    check(id + " cooldown progress " + progress, (float)cooldown.invoke(null, p, holder, progress), (id.equals("arcane_beam") ? 20 : 15) + 10 * progress);
                }
            }
            var missile = SpellRegistry.from(level).get(Identifier.parse("wizards:arcane_missile")).orElseThrow();
            check("First-tier missile proportional behavior retained", (float)cooldown.invoke(null, p, missile, .5f), 2);
            String[] ids = {"arcane_beam", "fire_meteor", "fire_storm", "frost_lance"};
            double[] expected = {8, 60, 28, 80};
            for (int i = 0; i < ids.length; i++) {
                var holder = SpellRegistry.from(level).get(Identifier.parse("wizards:" + ids[i])).orElseThrow();
                var spell = holder.value();
                var victim = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
                victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); victim.setHealth(1000); victim.setPos(1, 80, 0);
                float channel = SpellParameters.channelValueMultiplier(spell); if (channel == 0) channel = 1;
                SpellImpacts.performImpacts(level, p, victim, victim, holder, spell.impacts,
                    new SpellExecution.ImpactContext().power(new SpellPower.Result(spell.school, 40, 0, 1.5)).channeled(channel));
                check(ids[i] + " actual damage at 40", 1000 - victim.getHealth(), expected[i]);
                victim.discard();
            }
            var shield = SpellRegistry.from(level).get(Identifier.parse("wizards:frost_shield")).orElseThrow();
            p.removeAllEffects();
            SpellImpacts.performImpacts(level, p, p, p, shield, shield.value().impacts,
                new SpellExecution.ImpactContext().power(new SpellPower.Result(shield.value().school, 40, 0, 1.5)));
            var effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frost_shield")).orElseThrow();
            check("Shield duration", p.getEffect(effect).getDuration(), 60);
            check("Shield movement", p.getAttributeValue(Attributes.MOVEMENT_SPEED), 0);
            p.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:frost_shield_speed")).orElseThrow(), 60, 0));
            check("Nimble offsets half shield movement penalty", p.getAttributeValue(Attributes.MOVEMENT_SPEED), .05);
            p.removeAllEffects();
            var lance = SpellRegistry.from(level).get(Identifier.parse("wizards:frost_lance")).orElseThrow();
            var target = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); target.setHealth(1000); target.setPos(2, 80, 0);
            var frozen = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
            SpellImpacts.performImpacts(level, p, target, target, lance, lance.value().impacts,
                new SpellExecution.ImpactContext().power(new SpellPower.Result(lance.value().school, 40, 0, 1.5)));
            check("No immediate Lance freeze", target.hasEffect(frozen) ? 1 : 0, 0);
            ((WorldScheduler)level).schedule(9, () -> {
                try { check("No freeze at tick nine", target.hasEffect(frozen) ? 1 : 0, 0); }
                catch (Throwable e) { fail(e, server); }
            });
            ((WorldScheduler)level).schedule(11, () -> {
                try {
                    check("Freeze present after tick ten on 1000 HP target", target.hasEffect(frozen) ? 1 : 0, 1);
                    check("Delayed frozen full duration", target.getEffect(frozen).getDuration(), 80);
                    Files.write(Path.of("second-tier-results.txt"), out);
                    Files.writeString(Path.of("second-tier-result.txt"), "PASS " + checks + " runtime checks");
                } catch (Throwable e) { fail(e, server); }
                finally { server.halt(false); }
            });
        } catch (Throwable e) { fail(e, server); }
    }
    void fail(Throwable e, net.minecraft.server.MinecraftServer server) {
        e.printStackTrace();
        try { Files.writeString(Path.of("second-tier-result.txt"), "FAIL " + e); } catch (Exception ignored) {}
        server.halt(false);
    }
}
