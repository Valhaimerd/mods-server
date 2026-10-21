package local.audit;

import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;

@Mod("overhaul_probe")
public class RevisionProbe {
    int checks; final List<String> out = new ArrayList<>();
    net.minecraft.server.level.ServerLevel level;
    net.minecraft.server.MinecraftServer server;
    net.minecraft.server.level.ServerPlayer player;
    void check(String name, double actual, double expected) {
        if (Math.abs(actual - expected) > .004) throw new AssertionError(name + ": " + actual + " != " + expected);
        checks++; out.add(name + ": " + actual);
    }
    Holder<Spell> spell(String id) { return SpellRegistry.from(level).get(Identifier.parse(id)).orElseThrow(); }
    net.minecraft.world.entity.monster.Creeper target(double x) {
        var target = EntityTypes.CREEPER.create(level, EntitySpawnReason.COMMAND);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); target.setHealth(1000);
        target.setNoAi(true); target.setNoGravity(true); target.setPos(x, 80, 0);
        level.setChunkForced(target.blockPosition().getX() >> 4, 0, true); level.getChunkAt(target.blockPosition());
        level.addFreshEntity(target); return target;
    }
    SpellExecution.ImpactContext context(Holder<Spell> h, int index) {
        float multiplier = SpellParameters.channelValueMultiplier(h.value()); if (multiplier == 0) multiplier = 1;
        return new SpellExecution.ImpactContext(multiplier, 1, null, new SpellPower.Result(h.value().school, 40, 0, 1.5), SpellTarget.FocusMode.DIRECT, index);
    }
    void nodes(List<String> passives, List<String> modifiers) {
        var owner = (SpellContainerSource.Owner) player;
        var all = new ArrayList<String>(passives); all.addAll(modifiers); all.add("wizards:arcane_explosion");
        owner.serverSideSpellContainers().put("revision_probe", new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE, "", "", 0, all));
        owner.markServerSideSpellContainersDirty();
        var previous = owner.getSpellContainers();
        owner.setSpellContainers(new SpellContainerSource.Result(previous.activeContainer(), List.of(spell("wizards:arcane_explosion")), passives.stream().map(this::spell).toList(), modifiers.stream().map(this::spell).toList(), java.util.List.of(new SpellContainerSource.SourcedContainer("revision_probe", net.minecraft.world.item.ItemStack.EMPTY, owner.serverSideSpellContainers().get("revision_probe")))));
        owner.spellModifierCache().clear();
    }
    public RevisionProbe() { NeoForge.EVENT_BUS.addListener(this::start); }
    void start(ServerStartedEvent event) {
        server = event.getServer(); level = server.overworld();
        try {
            player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.fromString("36baa39f-f31a-41ae-bf89-682889d12589"), "RevisionFive"));
            player.setPos(0, 80, 0);
            for (String id : List.of("arcane", "fire", "frost")) player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:" + id)).orElseThrow()).setBaseValue(40);
            player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow()).setBaseValue(0);
            magistrate();
            nodes(List.of(), List.of());
            var cooldown = net.spell_engine.internals.cost.SpellCooldowns.class.getDeclaredMethod("cooldownToSet", LivingEntity.class, Holder.class, float.class); cooldown.setAccessible(true);
            for (String id : List.of("arcane_missile", "arcane_beam", "fire_storm", "fire_breath")) {
                var h = spell("wizards:" + id);
                for (float progress : new float[]{0, .5f, 1}) {
                    double min = id.equals("arcane_missile") ? 8 : id.equals("fire_breath") ? 4 : 15;
                    double max = id.equals("arcane_missile") ? 16 : id.equals("arcane_beam") ? 30 : id.equals("fire_breath") ? 8 : 25;
                    check(id + " cooldown " + progress, (float)cooldown.invoke(null, player, h, progress), min + (max - min) * progress);
                }
            }
            check("Frost Nova base cooldown", SpellParameters.getCooldownDuration(player, spell("wizards:frost_nova")), 12);
            check("Meteor base cooldown", SpellParameters.getCooldownDuration(player, spell("wizards:fire_meteor")), 20);
            nodes(List.of(), List.of("skill_tree_rpgs:fire_tier_3_spell_1_root"));
            check("Improved Meteor cooldown", SpellParameters.getCooldownDuration(player, spell("wizards:fire_meteor")), 18);
            nodes(List.of(), List.of());
            var emitter = (LivingEntity)BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse("wizards:arcane_emitter")).orElseThrow().value().create(level, EntitySpawnReason.COMMAND);
            var emitterBolt = spell("wizards:arcane_barrage_bolt"); var emitterVictim = target(1);
            SpellImpacts.performImpacts(level, emitter, emitterVictim, emitterVictim, emitterBolt, emitterBolt.value().impacts, context(emitterBolt, 0));
            check("Emitter bolt damage at 40", 1000 - emitterVictim.getHealth(), 16); emitterVictim.discard(); emitter.discard();
            var wandBolt = spell("wizards:arcane_bolt"); var wandVictim = target(1);
            SpellImpacts.performImpacts(level, player, wandVictim, wandVictim, wandBolt, wandBolt.value().impacts, context(wandBolt, 0));
            check("Wand Arcane Bolt remains 70 percent", 1000 - wandVictim.getHealth(), 28); wandVictim.discard();
            var beam = spell("wizards:arcane_beam");
            check("Normal beam cast duration", SpellParameters.getCastDuration(player, beam.value()), 5);
            nodes(List.of(), List.of("skill_tree_rpgs:arcane_tier_3_spell_1_modifier_2"));
            check("Charged Beam duration", SpellParameters.getCastDuration(player, beam.value()), 10);
            check("Charged Beam cast ticks", SpellParameters.getCastTimeDetails(player, beam.value()).length(), 200);
            check("Charged Beam deliveries", SpellParameters.channelTicks(player, beam), 50);
            double total = 0;
            for (int i = 0; i < 50; i++) {
                var victim = target(1); var ctx = context(beam, i);
                SpellImpacts.performImpacts(level, player, victim, victim, beam, beam.value().impacts, ctx);
                double expected = 40 * Math.min(.1 + .0015 * i, .15);
                check("Charged Beam actual delivery " + i, 1000 - victim.getHealth(), expected);
                total += expected; victim.discard();
            }
            check("Charged Beam full base channel at 40", total, 265.66);
            var process = new SpellCast.Process(player, beam, Items.STICK, 1, 200, level.getGameTime());
            ((SpellCaster.Player)player).getInteractor().setProcess(process); // exercise shared render appearance
            check("Charged beam initial width", ((SpellCaster.Entity)player).getBeam().width, .104);
            check("Charged beam width at delivery 25", RevisionRules.chargedWidth(24), .296);
            check("Charged beam width cap", RevisionRules.chargedWidth(49), .4);
            check("Charged beam just before cap", RevisionRules.chargedWidth(36), .392);
            check("Charged beam reaches cap on delivery 38", RevisionRules.chargedWidth(37), .4);
            ((SpellCaster.Player)player).getInteractor().requestClear();
            nodes(List.of(), List.of());
            var exposureTarget = target(1);
            exposureTarget.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:arcane_exposure")).orElseThrow(), 60, 24));
            check("Exposure max 25 levels", SpellPower.getVulnerability(exposureTarget, beam.value().school).powerBaseMultiplier(), 1);
            exposureTarget.discard();
            for (String id : List.of("arcane_missile", "arcane_explosion", "fire_breath", "fire_slash", "fire_storm", "fire_meteor", "frost_nova", "frost_spikes")) {
                var h = spell("wizards:" + id); var victim = target(1);
                SpellImpacts.performImpacts(level, player, victim, victim, h, h.value().impacts, context(h, 0));
                double expected = switch(id) {case "arcane_missile" -> 10.4; case "arcane_explosion" -> 104; case "fire_breath" -> 5.2; case "fire_slash" -> 40; case "fire_storm" -> 16; case "fire_meteor" -> 60; case "frost_nova" -> 72; default -> 32;};
                check(id + " base damage at 40", 1000 - victim.getHealth(), expected); victim.discard();
            }
            nodes(List.of(), List.of("skill_tree_rpgs:frost_tier_2_spell_2_root", "skill_tree_rpgs:frost_tier_2_spell_2_modifier_2"));
            var spikes = spell("wizards:frost_spikes"); var fanVictim = target(1);
            SpellImpacts.performImpacts(level, player, fanVictim, fanVictim, spikes, spikes.value().impacts, context(spikes, 0));
            check("Frost Fan actual damage with required root", 1000 - fanVictim.getHealth(), 10); fanVictim.discard();
            nodes(List.of("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1"), List.of());
            var explosion = spell("wizards:arcane_explosion"); var echoVictim = target(1);
            ((SpellCaster.Player)player).getCooldownManager().set(explosion, 200);
            SpellImpacts.performImpacts(level, player, echoVictim, echoVictim, explosion, explosion.value().impacts, context(explosion, 0));
            check("Echoing Blast actual damage", 1000 - echoVictim.getHealth(), 64);
            var echo = spell("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1");
            check("Echo source registered", SpellContainerSource.getFirstSourceOfSpell(Identifier.parse("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1"), player) != null ? 1 : 0, 1);
            check("Echo cast attempt", SpellCasting.attempt(player, player.getMainHandItem(), Identifier.parse("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1")).isSuccess() ? 1 : 0, 1);
            check("Caster attackable for passive effect", player.isAttackable() ? 1 : 0, 1);
            echoVictim.discard();
            ((WorldScheduler)level).schedule(2, () -> {
                try {
                    check("Guaranteed Echo reset", ((SpellCaster.Player)player).getCooldownManager().getCooldownDuration(explosion), 0);
                    geometry();
                } catch (Throwable failure) { fail(failure); }
            });
        } catch (Throwable failure) { fail(failure); }
    }

    void geometry() {
        nodes(List.of(), List.of("skill_tree_rpgs:frost_tier_3_spell_2_root", "skill_tree_rpgs:frost_tier_3_spell_2_modifier_2"));
        var primary = target(30); var nearby = target(35); var outside = target(40);
        var lance = spell("wizards:frost_lance");
        ((WorldScheduler)level).schedule(40, () -> {
            try {
                check("Geometry target indexed", level.getEntities(primary, primary.getBoundingBox().inflate(8)).contains(nearby) ? 1 : 0, 1);
                var ctx = context(lance, 0).position(primary.position());
                SpellImpacts.performImpacts(level, player, primary, primary, lance, lance.value().impacts, ctx);
                check("Shattering direct damage", 1000 - primary.getHealth(), 80);
                check("Shattering splash damage at five blocks", 1000 - nearby.getHealth(), 20);
                check("Outside Shattering radius", 1000 - outside.getHealth(), 0);
                ((WorldScheduler)level).schedule(11, () -> {
                    try {
                        var frozen = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
                        check("Shattering direct Frozen duration", primary.getEffect(frozen).getDuration(), 79);
                        check("Shattering splash Frozen duration", nearby.getEffect(frozen).getDuration(), 19);
                        primary.discard(); nearby.discard(); outside.discard();
                        chain();
                    } catch (Throwable failure) { fail(failure); }
                });
            } catch (Throwable failure) { fail(failure); }
        });
    }

    void chain() {
        nodes(List.of("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_2"), List.of());
        var primary = target(30); var nearby = target(32); var casterNeighbor = target(1);
        var explosion = spell("wizards:arcane_explosion");
        ((WorldScheduler)level).schedule(40, () -> {
            try {
                var center = primary.position().add(0, primary.getBbHeight() / 2f, 0).lerp(player.position().add(0, player.getBbHeight() / 2f, 0), .01f);
                double splashExpected = 12 * Math.max(0, 1 - nearby.distanceToSqr(center) / 16);
                SpellTriggers.onSpellImpactSpecific(player, primary, explosion, explosion.value().impacts.getFirst(), false, Spell.Trigger.Stage.POST);
                check("Chain not immediate", 1000 - primary.getHealth(), 0);
                ((WorldScheduler)level).schedule(11, () -> {
                    try {
                        check("Guaranteed delayed chain at struck mob", 1000 - primary.getHealth(), 12);
                        check("Chain surrounding enemy with retained falloff", 1000 - nearby.getHealth(), splashExpected);
                        check("Chain does not center on caster", 1000 - casterNeighbor.getHealth(), 0);
                        shield();
                        Files.write(Path.of("revision-results.txt"), out);
                        Files.writeString(Path.of("revision-result.txt"), "PASS " + checks + " runtime checks");
                        server.halt(false);
                    } catch (Throwable failure) { fail(failure); }
                });
            } catch (Throwable failure) { fail(failure); }
        });
    }
    void shield() throws Exception {
        nodes(List.of(), List.of()); player.removeAllEffects();
        var holder = spell("wizards:frost_shield");
        var effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frost_shield")).orElseThrow();
        var frozen = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
        var defender = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
        defender.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); defender.setHealth(1000);
        defender.setNoAi(true); defender.setNoGravity(true); defender.setPos(0, 80, 0); level.addFreshEntity(defender);
        defender.hurtServer(level, level.damageSources().magic(), 1);
        check("Unshielded fixture takes damage", defender.getHealth(), 999);
        defender.setHealth(1000); defender.invulnerableTime = 0;
        var near = target(3); var outside = target(6);
        var tick = LivingEntity.class.getDeclaredMethod("tickEffects"); tick.setAccessible(true);
        SpellImpacts.performImpacts(level, player, player, player, holder, holder.value().impacts, context(holder, 0));
        check("Shield base duration ticks", player.getEffect(effect).getDuration(), 60);
        defender.addEffect(new MobEffectInstance(player.getEffect(effect))); player.removeEffect(effect);
        for (var source : List.of(level.damageSources().generic(), level.damageSources().magic(), level.damageSources().onFire(), level.damageSources().fall(), level.damageSources().fellOutOfWorld(), level.damageSources().genericKill())) {
            defender.invulnerableTime = 0;
            defender.hurtServer(level, source, 1000);
            check("Shield immune to " + source.typeHolder().unwrapKey().orElseThrow().identifier(), defender.getHealth(), 1000);
        }
        for (int i = 0; i < 59; i++) {
            defender.invulnerableTime = 0;
            defender.hurtServer(level, i % 2 == 0 ? level.damageSources().magic() : level.damageSources().generic(), 1000);
            check("Shield repeated hit blocked at tick " + i, defender.getHealth(), 1000);
            check("Shield is not consumed at tick " + i, defender.hasEffect(effect) ? 1 : 0, 1);
            tick.invoke(defender);
        }
        defender.invulnerableTime = 0; defender.hurtServer(level, level.damageSources().magic(), 1000);
        check("Shield still blocks on final active tick", defender.getHealth(), 1000);
        check("No burst before shield expiry", near.hasEffect(frozen) ? 1 : 0, 0);
        tick.invoke(defender);
        check("Shield naturally expires", defender.hasEffect(effect) ? 1 : 0, 0);
        check("Expiry burst freezes nearby enemy", near.hasEffect(frozen) ? 1 : 0, 1);
        check("Expiry freeze duration", near.getEffect(frozen).getDuration(), 40);
        check("Expiry burst deals no damage", near.getHealth(), 1000);
        check("Enemy outside five blocks stays unfrozen", outside.hasEffect(frozen) ? 1 : 0, 0);
        defender.baseTick(); defender.invulnerableTime = 0;
        defender.hurtServer(level, level.damageSources().magic(), 1);
        check("Shield immunity ends on expiry", defender.getHealth(), 999);
        near.removeAllEffects();
        defender.addEffect(new MobEffectInstance(effect, 60, 1)); defender.removeEffect(effect);
        check("Dispel does not trigger burst", near.hasEffect(frozen) ? 1 : 0, 0);
        nodes(List.of(), List.of("skill_tree_rpgs:frost_tier_3_spell_1_modifier_2"));
        SpellImpacts.performImpacts(level, player, player, player, holder, holder.value().impacts, context(holder, 0));
        check("Durable Shield duration ticks", player.getEffect(effect).getDuration(), 80);
        defender.addEffect(new MobEffectInstance(player.getEffect(effect))); player.removeEffect(effect);
        for (int i = 0; i < 79; i++) tick.invoke(defender);
        check("Durable burst waits for extended expiry", near.hasEffect(frozen) ? 1 : 0, 0);
        tick.invoke(defender);
        check("Durable Shield also bursts on expiry", near.hasEffect(frozen) ? 1 : 0, 1);
        defender.discard(); near.discard(); outside.discard();
    }
    void magistrate() {
        var tree = net.puffish.skillsmod.api.SkillsAPI.getCategory(net.valhaimerd.orboblivioncompat.SkillOverhaul.CATEGORY).orElseThrow();
        tree.unlock(player); tree.resetSkills(player); tree.setExtraPoints(player, 1000);
        var crit = player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());
        net.valhaimerd.orboblivioncompat.SkillOverhaul.applyCritical(player);
        crit.setBaseValue(100); // Spell Power represents zero critical chance at its base-100 baseline.
        double baseline = crit.getValue();
        // Simulate the old live transient bonus: refreshing must remove it too.
        var oldId = Identifier.parse("orb_of_oblivion_puffish_compat:pet_critical_points");
        crit.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(oldId, 10, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        tree.getSkill("zdfpatqypd5nnqx8").orElseThrow().unlock(player);
        net.valhaimerd.orboblivioncompat.SkillOverhaul.applyCritical(player);
        check("Magistrate no critical bonus and old transient cleared", crit.getValue(), baseline);
        check("Magistrate school power retained", player.getAttributeValue(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:arcane")).orElseThrow()), 44);
        tree.getSkill("ro3sae58m8jzn4yz").orElseThrow().unlock(player);
        net.valhaimerd.orboblivioncompat.SkillOverhaul.applyCritical(player);
        check("Ordinary critical node retained", crit.getValue() - baseline, 2.5);
        tree.getSkill("gs6jmtskmyhpbcnu").orElseThrow().unlock(player);
        net.valhaimerd.orboblivioncompat.SkillOverhaul.applyCritical(player);
        check("Both ordinary critical nodes retained", crit.getValue() - baseline, 5);
        tree.resetSkills(player); net.valhaimerd.orboblivioncompat.SkillOverhaul.applyCritical(player); crit.setBaseValue(0);
    }
    void fail(Throwable failure) {
        failure.printStackTrace();
        try { Files.writeString(Path.of("revision-result.txt"), "FAIL " + failure); Files.write(Path.of("revision-results.txt"), out); } catch (Exception ignored) {}
        server.halt(false);
    }
}
