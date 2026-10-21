package local.audit;

import com.mojang.authlib.GameProfile;
import dev.betterparty.api.v1.server.BetterPartyServerApi;
import dev.betterparty.party.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.*;
import net.spell_engine.internals.impact.SpellImpacts;
import net.spell_engine.internals.target.*;
import net.spell_power.api.SpellPower;

/** Isolated-server helper. Call run(server, results) from the integrated probe. Never ship. */
public final class PartyProbe {
    private int checks;
    private final List<String> out;
    private final ServerLevel world;
    private final Method perform;
    private final Holder<Spell> holder;
    private PartyProbe(MinecraftServer server, List<String> out) throws Exception {
        this.out = out; world = server.overworld();
        holder = SpellRegistry.from(world).get(Identifier.parse("wizards:frost_nova")).orElseThrow();
        perform = SpellImpacts.class.getDeclaredMethod("performImpact", Level.class, LivingEntity.class,
            Entity.class, Holder.class, Spell.Impact.class, SpellExecution.ImpactContext.class, Collection.class);
        perform.setAccessible(true);
    }
    private void check(String name, boolean value) {
        if (!value) throw new AssertionError("Party: " + name);
        checks++; out.add("Party: " + name + ": PASS");
    }
    private ServerPlayer player(String name) {
        var result = new ProbePlayer(world, new GameProfile(UUID.nameUUIDFromBytes(("third-tier-" + name).getBytes()), name));
        result.setGameMode(GameType.SURVIVAL);
        result.setInvulnerable(false); result.getAbilities().invulnerable = false;
        fixture(result);
        try {
            for (String nameField : List.of("clientLoadedTimeoutTimer", "waitingForRespawn")) {
                Field field = net.minecraft.server.network.ServerGamePacketListenerImpl.class.getDeclaredField(nameField);
                field.setAccessible(true);
                if (field.getType() == int.class) field.setInt(result.connection, 0); else field.setBoolean(result.connection, false);
            }
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Player fixture client-load state", failure); }
        return result;
    }
    private SpellExecution.ImpactContext context(SpellTarget.FocusMode focus) {
        return new SpellExecution.ImpactContext(1, 1, null,
            new SpellPower.Result(holder.value().school, 40, 0, 1.5), focus, 0);
    }
    private void impact(LivingEntity caster, LivingEntity target, Spell.Impact impact, SpellTarget.FocusMode focus) throws Exception {
        perform.invoke(null, world, caster, target, holder, impact, context(focus), null);
    }
    private Spell.Impact action(Spell.Impact.Action.Type type) {
        Spell.Impact impact = new Spell.Impact(); impact.action = new Spell.Impact.Action(); impact.action.type = type;
        if (type == Spell.Impact.Action.Type.DAMAGE) {
            impact.action.damage = new Spell.Impact.Action.Damage(); impact.action.damage.knockback = 0;
        }
        if (type == Spell.Impact.Action.Type.HEAL) impact.action.heal = new Spell.Impact.Action.Heal();
        if (type == Spell.Impact.Action.Type.FIRE) impact.action.fire = new Spell.Impact.Action.Fire();
        return impact;
    }
    private Spell.Impact frost(String id) {
        var result = action(Spell.Impact.Action.Type.STATUS_EFFECT);
        result.action.status_effect = new Spell.Impact.Action.StatusEffect();
        result.action.status_effect.effect_id = id;
        result.action.status_effect.duration = 6;
        result.action.status_effect.apply_limit = new Spell.Impact.Action.StatusEffect.ApplyLimit();
        result.action.status_effect.apply_limit.health_base = 1;
        return result;
    }
    private <T extends LivingEntity> T fixture(T entity) {
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); entity.setHealth(entity.getMaxHealth());
        entity.setPos(500, 80, 500); entity.setNoGravity(true);
        if (entity instanceof Mob mob) mob.setNoAi(true);
        return entity;
    }
    @SuppressWarnings("unchecked")
    private static <K,V> Map<K,V> map(Object target, String field) throws Exception {
        Field declared = target.getClass().getDeclaredField(field); declared.setAccessible(true);
        return (Map<K,V>)declared.get(target);
    }
    public static int run(MinecraftServer server, List<String> out) throws Exception {
        return new PartyProbe(server, out).run();
    }
    private int run() throws Exception {
        var caster = player("PartyCaster"); var ally = player("PartyAlly"); var enemy = player("PartyEnemy");
        var manager = PartyManager.forServer(world.getServer());
        Map<UUID, Party> parties = map(manager, "parties");
        Map<UUID, UUID> memberships = map(manager, "playerParties");
        UUID partyId = UUID.nameUUIDFromBytes("third-tier-probe-party".getBytes());
        Party previousParty = parties.get(partyId);
        Map<UUID, UUID> previousMemberships = new HashMap<>();
        for (var player : List.of(caster, ally, enemy)) previousMemberships.put(player.getUUID(), memberships.get(player.getUUID()));
        Party party = new Party(partyId);
        party.addMember(new PartyMember(caster.getUUID(), "PartyCaster", PartyRole.LEADER));
        party.addMember(new PartyMember(ally.getUUID(), "PartyAlly", PartyRole.MEMBER));
        parties.put(partyId, party); memberships.put(caster.getUUID(), partyId); memberships.put(ally.getUUID(), partyId); memberships.remove(enemy.getUUID());
        List<LivingEntity> fixtures = new ArrayList<>();
        try {
            check("real Better Party API fixture", BetterPartyServerApi.arePartyMembers(world.getServer(), caster.getUUID(), ally.getUUID()));
            Wolf ownPet = fixture(EntityTypes.WOLF.create(world, EntitySpawnReason.COMMAND)); ownPet.setOwnerReference(EntityReference.of(caster.getUUID()));
            Wolf partyPet = fixture(EntityTypes.WOLF.create(world, EntitySpawnReason.COMMAND)); partyPet.setOwnerReference(EntityReference.of(ally.getUUID()));
            Wolf enemyPet = fixture(EntityTypes.WOLF.create(world, EntitySpawnReason.COMMAND)); enemyPet.setOwnerReference(EntityReference.of(enemy.getUUID()));
            var villager = fixture(EntityTypes.VILLAGER.create(world, EntitySpawnReason.COMMAND));
            var golem = fixture(EntityTypes.IRON_GOLEM.create(world, EntitySpawnReason.COMMAND));
            var cow = fixture(EntityTypes.COW.create(world, EntitySpawnReason.COMMAND));
            var hostile = fixture(EntityTypes.CREEPER.create(world, EntitySpawnReason.COMMAND));
            var immune = fixture(new ImmuneFixture(world));
            var dragon = fixture(EntityTypes.ENDER_DRAGON.create(world, EntitySpawnReason.COMMAND));
            fixtures.addAll(List.of(ownPet, partyPet, enemyPet, villager, golem, cow, hostile, immune, dragon));
            var damage = action(Spell.Impact.Action.Type.DAMAGE); var fire = action(Spell.Impact.Action.Type.FIRE); var heal = action(Spell.Impact.Action.Type.HEAL);
            paladinSpells(caster, ally, enemy, ownPet, partyPet, enemyPet, party);
            dragon.setPos(500, 200, 500); world.getChunkAt(dragon.blockPosition());
            var initialDragonPosition = dragon.position();
            dragon.move(MoverType.SELF, new net.minecraft.world.phys.Vec3(1, 0, 0));
            check("actual Ender Dragon move fixture moves before freeze", dragon.position().distanceTo(initialDragonPosition) > .5);
            impact(caster, dragon, frost("wizards:frozen"), SpellTarget.FocusMode.AREA);
            var frozenEffect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow();
            check("actual Ender Dragon receives Frozen", dragon.hasEffect(frozenEffect));
            var frozenDragonPosition = dragon.position();
            dragon.move(MoverType.SELF, new net.minecraft.world.phys.Vec3(2, 1, 3));
            check("actual Frozen Ender Dragon move clamped", dragon.position().distanceToSqr(frozenDragonPosition) < .000001);
            dragon.removeEffect(frozenEffect);
            dragon.move(MoverType.SELF, new net.minecraft.world.phys.Vec3(1, 0, 0));
            check("actual Ender Dragon moves again after Frozen removed", dragon.position().distanceTo(frozenDragonPosition) > .5);
            for (boolean friendlyFire : new boolean[]{false, true}) {
                party.setFriendlyFire(friendlyFire);
                for (var focus : SpellTarget.FocusMode.values()) {
                    for (var target : List.of(caster, ally, ownPet, partyPet)) {
                        String label = target.getType().toString() + " " + target.getUUID() + " " + focus + " FF=" + friendlyFire;
                        check("protect harmful acquisition " + label, !EntityRelations.actionAllowed(focus, SpellTarget.Intent.HARMFUL, caster, target));
                        check("allow helpful acquisition " + label, EntityRelations.actionAllowed(focus, SpellTarget.Intent.HELPFUL, caster, target));
                        float hp = target.getHealth();
                        impact(caster, target, damage, focus); impact(caster, target, fire, focus);
                        impact(caster, target, frost("wizards:frozen"), focus);
                        check("final damage/fire/freeze safety " + label, target.getHealth() == hp && target.getRemainingFireTicks() <= 0
                            && !target.hasEffect(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("wizards:frozen")).orElseThrow()));
                    }
                    for (var target : List.of(enemy, enemyPet, villager, golem, cow, hostile)) {
                        check("allow offensive acquisition " + target.getType() + " " + focus, EntityRelations.actionAllowed(focus, SpellTarget.Intent.HARMFUL, caster, target));
                        check("deny healing acquisition " + target.getType() + " " + focus, !EntityRelations.actionAllowed(focus, SpellTarget.Intent.HELPFUL, caster, target));
                        target.setHealth(target.getMaxHealth() / 2);
                        float hp = target.getHealth(); impact(caster, target, heal, focus);
                        check("final enemy healing safety " + target.getType() + " " + focus, target.getHealth() == hp);
                    }
                }
            }
            for (var target : List.of(villager, golem, cow, hostile, enemyPet)) {
                float before = target.getHealth(); target.invulnerableTime = 0;
                impact(caster, target, damage, SpellTarget.FocusMode.AREA);
                check("actual area damage " + target.getType(), target.getHealth() < before);
                impact(caster, target, fire, SpellTarget.FocusMode.AREA);
                check("actual burn eligibility " + target.getType(), target.getRemainingFireTicks() > 0);
            }
            check("owned spell caster protects party player", !PartyRules.harmfulAllowed(ownPet, ally));
            check("owned spell caster protects sibling pet", !PartyRules.harmfulAllowed(ownPet, partyPet));
            ownPet.setHealth(ownPet.getMaxHealth() / 2); float petHp = ownPet.getHealth();
            impact(caster, ownPet, heal, SpellTarget.FocusMode.AREA);
            check("actual party pet healing", ownPet.getHealth() > petHp);
            damage.action.apply_to_caster = true;
            check("self damage apply_to_caster blocked", !PartyRules.impactAllowed(world, caster, enemy, damage, context(SpellTarget.FocusMode.AREA)));
            var immunity = action(Spell.Impact.Action.Type.IMMUNITY); immunity.action.apply_to_caster = true;
            check("self immunity retained", PartyRules.impactAllowed(world, caster, enemy, immunity, context(SpellTarget.FocusMode.AREA)));
            for (String id : List.of("minecraft:slowness", "wizards:frozen", "wizards:frost_slowness", "skill_tree_rpgs:blizzard_slow")) {
                var effect = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(id)).orElseThrow();
                check("fixture rejects ordinary effect " + id, !immune.canBeAffected(new MobEffectInstance(effect, 120)));
                impact(caster, immune, frost(id), SpellTarget.FocusMode.AREA);
                check("actual frost bypass type/boss/" + immune.getMaxHealth() + " HP gates " + id, immune.hasEffect(effect) && immune.getEffect(effect).getDuration() == 120);
            }
            return checks;
        } finally {
            fixtures.forEach(Entity::discard);
            if (previousParty == null) parties.remove(partyId); else parties.put(partyId, previousParty);
            previousMemberships.forEach((id, previous) -> { if (previous == null) memberships.remove(id); else memberships.put(id, previous); });
        }
    }
    public static final class ImmuneFixture extends Creeper {
        public ImmuneFixture(Level world) { super(EntityTypes.CREEPER, world); }
        @Override public boolean canBeAffected(MobEffectInstance effect) { return false; }
        @Override public boolean is(TagKey<EntityType<?>> tag) { return true; }
    }
    /** NeoForge FakePlayer otherwise rejects all attributed player damage itself. */
    public static final class ProbePlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        public ProbePlayer(ServerLevel world, GameProfile profile) { super(world, profile); }
        @Override public boolean canHarmPlayer(Player other) {
            return level().isPvpAllowed() && (getTeam() == null || !getTeam().isAlliedTo(other.getTeam()) || getTeam().isAllowFriendlyFire());
        }
    }

    private void paladinSpells(ServerPlayer caster, ServerPlayer ally, ServerPlayer enemy,
            Wolf ownPet, Wolf partyPet, Wolf enemyPet, Party party) throws Exception {
        enemy.setHealth(enemy.getMaxHealth()); enemy.invulnerableTime = 0;
        float baselineHp = enemy.getHealth();
        boolean accepted = enemy.hurtServer(world, world.damageSources().playerAttack(caster), 2);
        out.add("Party: native player fixture damage before=" + baselineHp + " after=" + enemy.getHealth() + " accepted=" + accepted);
        check("player fixture accepts native attributed PvP damage", accepted && enemy.getHealth() < baselineHp);
        caster.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:healing")).orElseThrow()).setBaseValue(40);
        for (boolean friendlyFire : new boolean[]{false, true}) {
            party.setFriendlyFire(friendlyFire);
            for (var focus : SpellTarget.FocusMode.values()) {
                var judgement = SpellRegistry.from(world).get(Identifier.parse("paladins:judgement")).orElseThrow();
                var holy = SpellRegistry.from(world).get(Identifier.parse("paladins:holy_beam")).orElseThrow();
                for (var target : List.of(caster, ally, ownPet, partyPet, enemy, enemyPet)) {
                    boolean protectedTarget = target != enemy && target != enemyPet;
                    String label = target.getUUID() + " " + focus + " FF=" + friendlyFire;
                    target.setHealth(target.getMaxHealth() / 2); target.invulnerableTime = 0;
                    float judgementHp = target.getHealth();
                    var judgementContext = new SpellExecution.ImpactContext(1, 1, null,
                        new SpellPower.Result(judgement.value().school, 40, 0, 1.5), focus, 0);
                    SpellImpacts.performImpacts(world, caster, target, target, judgement, judgement.value().impacts, judgementContext, false, null);
                    out.add("Party: Judgement " + label + " relation=" + EntityRelations.getRelation(caster, target) + " before=" + judgementHp + " after=" + target.getHealth());
                    check("loaded Falling Judgement " + (protectedTarget ? "protects ally " : "damages enemy ") + label,
                        protectedTarget ? target.getHealth() == judgementHp : target.getHealth() < judgementHp);
                    target.setHealth(target.getMaxHealth() / 2); target.invulnerableTime = 0;
                    float holyHp = target.getHealth();
                    var holyContext = new SpellExecution.ImpactContext(1, 1, null,
                        new SpellPower.Result(holy.value().school, 40, 0, 1.5), focus, 0);
                    SpellImpacts.performImpacts(world, caster, target, target, holy, holy.value().impacts, holyContext, false, null);
                    out.add("Party: Holy Light " + label + " relation=" + EntityRelations.getRelation(caster, target) + " before=" + holyHp + " after=" + target.getHealth());
                    check("loaded Holy Light mixed pipeline " + (protectedTarget ? "heals ally " : "damages without healing enemy ") + label,
                        protectedTarget ? target.getHealth() > holyHp : target.getHealth() < holyHp);
                }
            }
        }
    }
}
