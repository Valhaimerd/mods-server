package local.audit;

import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellParameters;
import net.spell_engine.internals.SpellExecution;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_power.api.SpellPower;

@Mod("overhaul_probe")
public class MagicReworkProbe {
    int checks;
    boolean awaitingGeometry;
    final List<String> results = new ArrayList<>();

    void check(String label, double actual, double expected) {
        if (Math.abs(actual - expected) > .003) throw new AssertionError(label + ": " + actual + " != " + expected);
        checks++;
        results.add(label + ": " + actual);
    }

    public MagicReworkProbe() { NeoForge.EVENT_BUS.addListener(this::start); }

    void start(ServerStartedEvent event) {
        var server = event.getServer();
        server.execute(() -> {
            try {
                var level = server.overworld();
                var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.fromString("36baa39f-f31a-41ae-bf89-682889d12589"), "MagicRework"));
                player.setPos(0, 80, 0);
                String[] ids = {"arcane_missile", "arcane_explosion", "fire_breath", "fire_slash", "frost_nova", "frost_spikes"};
                double[] damage = {40 * 3.2 / 12, 160, 8.8, 52, 120, 56};
                double[] cooldown = {4, 10, 4, 5, 10, 8};
                for (int power : List.of(10, 40, 80)) {
                    for (int i = 0; i < ids.length; i++) {
                        var holder = SpellRegistry.from(level).get(Identifier.parse("wizards:" + ids[i])).orElseThrow();
                        var spell = holder.value();
                        player.removeAllEffects();
                        player.getAttribute(spell.school.attributeEntry).setBaseValue(power);
                        var victim = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
                        victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
                        victim.setHealth(1000);
                        victim.setPos(1, 80, 0);
                        float multiplier = SpellParameters.channelValueMultiplier(spell);
                        if (multiplier == 0) multiplier = 1;
                        var context = new SpellExecution.ImpactContext().power(new SpellPower.Result(spell.school, power, 0, 1.5)).channeled(multiplier).position(victim.position());
                        SpellImpacts.performImpacts(level, player, victim, victim, holder,
                            spell.impacts.stream().filter(x -> x.action.type == Spell.Impact.Action.Type.DAMAGE).toList(), context);
                        check(ids[i] + " damage at " + power, 1000 - victim.getHealth(), damage[i] * power / 40.0);
                        check(ids[i] + " cooldown at " + power, SpellParameters.getCooldownDuration(player, holder), cooldown[i]);
                        if (ids[i].startsWith("frost")) {
                            SpellImpacts.performImpacts(level, player, victim, victim, holder,
                                spell.impacts.stream().filter(x -> x.action.type == Spell.Impact.Action.Type.STATUS_EFFECT).toList(), context);
                            var frozen = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
                            if (!victim.hasEffect(frozen)) throw new AssertionError("Freeze rejected 1,000 HP target");
                            checks++;
                            check(ids[i] + " frozen duration at " + power, victim.getEffect(frozen).getDuration(), 40);
                        }
                        victim.discard();
                    }
                }
                var spikesHolder = SpellRegistry.from(level).get(Identifier.parse("wizards:frost_spikes")).orElseThrow();
                var modifierOwner = (net.spell_engine.internals.container.SpellContainerSource.Owner) player;
                var fanModifiers = new ArrayList<Spell.Modifier>();
                for (String node : List.of("frost_tier_2_spell_2_root", "frost_tier_2_spell_2_modifier_2")) {
                    fanModifiers.addAll(SpellRegistry.from(level).get(Identifier.parse("skill_tree_rpgs:" + node)).orElseThrow().value().modifiers);
                }
                modifierOwner.spellModifierCache().put(Identifier.parse("wizards:frost_spikes"), fanModifiers);
                var fanVictim = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
                fanVictim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
                fanVictim.setHealth(1000);
                SpellImpacts.performImpacts(level, player, fanVictim, fanVictim, spikesHolder,
                    spikesHolder.value().impacts.stream().filter(x -> x.action.type == Spell.Impact.Action.Type.DAMAGE).toList(),
                    new SpellExecution.ImpactContext().power(new SpellPower.Result(spikesHolder.value().school, 40, 0, 1.5)));
                check("Frost Fan with mandatory root damage at 40", 1000 - fanVictim.getHealth(), 20);
                modifierOwner.spellModifierCache().clear();
                fanVictim.discard();
                var explosion = SpellRegistry.from(level).get(Identifier.parse("wizards:arcane_explosion")).orElseThrow();
                player.getAttribute(explosion.value().school.attributeEntry).setBaseValue(40);
                var primary = EntityTypes.CREEPER.create(level, EntitySpawnReason.COMMAND);
                var nearby = EntityTypes.CREEPER.create(level, EntitySpawnReason.COMMAND);
                var casterNeighbor = EntityTypes.CREEPER.create(level, EntitySpawnReason.COMMAND);
                var outside = EntityTypes.CREEPER.create(level, EntitySpawnReason.COMMAND);
                var targets = List.of(primary, nearby, casterNeighbor, outside);
                double[] positions = {30, 32, 1, 40};
                for (int i = 0; i < targets.size(); i++) {
                    var victim = targets.get(i);
                    victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
                    victim.setHealth(1000);
                    victim.setPos(positions[i], 80, 0);
                    level.setChunkForced(victim.blockPosition().getX() >> 4, 0, true);
                    level.getChunkAt(victim.blockPosition());
                    if (!level.addFreshEntity(victim)) throw new AssertionError("Could not add geometry target");
                }
                awaitingGeometry = true;
                ((net.spell_engine.utils.WorldScheduler) level).schedule(20, () -> {
                    try {
                results.add("Loaded geometry entity count: " + level.getEntities(primary, primary.getBoundingBox().inflate(8)).size());
                results.add("Area helper count: " + net.spell_engine.utils.TargetHelper.targetsFromArea(level, primary, primary.position(), primary.getLookAngle(), 7, explosion.value().area_impact.area, null).size());
                var blastContext = new SpellExecution.ImpactContext().power(new SpellPower.Result(explosion.value().school, 40, 0, 1.5));
                net.spell_engine.internals.delivery.SpellDelivery.resolveAndDeliver(level, player, explosion,
                    new net.spell_engine.internals.target.SpellTarget.SearchResult(List.of(primary), null), blastContext, null);
                check("Target-centered primary damage", 1000 - primary.getHealth(), 160);
                check("Target-centered neighboring damage", 1000 - nearby.getHealth(), 160);
                check("Caster neighbor takes no blast damage", 1000 - casterNeighbor.getHealth(), 0);
                check("Outside target blast radius takes no damage", 1000 - outside.getHealth(), 0);
                for (var victim : targets) victim.discard();
                // Loading these entries also proves the new placement data parsed successfully.
                for (String id : List.of("frost_tier_2_spell_2_modifier_1", "frost_tier_2_spell_2_modifier_2", "fire_tier_2_spell_1_modifier_1")) {
                    SpellRegistry.from(level).get(Identifier.parse("skill_tree_rpgs:" + id)).orElseThrow();
                    checks++;
                }
                Files.write(Path.of("magic-rework-results.txt"), results);
                Files.writeString(Path.of("magic-rework-result.txt"), "PASS " + checks + " checks: loaded registry, actual controlled impacts, cooldowns and 1,000 HP target freezing. Not a live cast or multiplayer playtest.");
                    } catch (Throwable failure) {
                        failure.printStackTrace();
                        try {
                            Files.write(Path.of("magic-rework-debug.txt"), results);
                            Files.writeString(Path.of("magic-rework-result.txt"), "FAIL " + failure);
                        } catch (Exception ignored) {}
                    } finally { server.halt(false); }
                });
            } catch (Throwable failure) {
                failure.printStackTrace();
                try { Files.write(Path.of("magic-rework-debug.txt"), results); } catch (Exception ignored) {}
                try { Files.writeString(Path.of("magic-rework-result.txt"), "FAIL " + failure); } catch (Exception ignored) {}
            } finally {
                if (!awaitingGeometry) server.halt(false);
            }
        });
    }
}
