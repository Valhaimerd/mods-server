package net.spell_engine.internals;

import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.spell_engine.utils.VectorHelper;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellCloud;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.utils.TargetHelper;

/** Per-player wall burst and native cloud geometry. No block edits or projectile blockers. */
public final class WallRules {
    private static final String WALL = "wizards:fire_wall";
    private static final Map<UUID, Burst> bursts = new HashMap<>();
    private static final class Burst {
        final long expires;
        final Holder<Spell> spell;
        final int cooldown;
        final net.minecraft.world.item.ItemStack hostingItem;
        int casts = 1;
        Burst(long now, Holder<Spell> spell, int cooldown, SpellContainerSource.SourcedContainer source) {
            expires = now + 100; this.spell = spell; this.cooldown = cooldown;
            this.hostingItem = source == null ? null : source.itemStack();
        }
    }
    public static void init() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(WallRules::logout);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(WallRules::stopped);
    }
    private static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        Burst burst = bursts.get(player.getUUID());
        if (burst != null) finish(player, burst);
    }
    private static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent event) { bursts.clear(); }
    private static String id(Holder<Spell> h) {
        return h == null ? "" : h.unwrapKey().map(k -> k.identifier().toString()).orElse("");
    }
    public static boolean ignoreHeldRepeat(net.spell_engine.internals.casting.SpellCast.Option option, boolean fresh) {
        return !fresh && WALL.equals(option.id().toString());
    }
    public static boolean imposeCooldown(Player player, SpellContainerSource.SourcedContainer source, Holder<Spell> spell, float progress) {
        if (!WALL.equals(id(spell))) return false;
        if (player.level().isClientSide()) return true;
        tick(player);
        Burst burst = bursts.get(player.getUUID());
        if (burst == null) {
            burst = new Burst(player.level().getGameTime(), spell,
                    Math.round(SpellParameters.getCooldownDuration(player, spell) * 20), source);
            bursts.put(player.getUUID(), burst);
        } else {
            burst.casts++;
        }
        if (burst.casts >= 4) finish(player, burst);
        else ((SpellCaster.Player)player).getCooldownManager().remove(net.minecraft.resources.Identifier.parse(WALL));
        // Send an explicit zero cooldown to clear any client prediction during the remaining charges.
        return true;
    }
    private static void finish(Player player, Burst burst) {
        bursts.remove(player.getUUID());
        ((SpellCaster.Player)player).getCooldownManager().set(burst.spell, burst.cooldown);
        if (net.spell_engine.SpellEngineMod.config.spell_item_cooldown_lock
                && burst.spell.value().cost.cooldown.hosting_item && burst.hostingItem != null) {
            int ticks = burst.cooldown;
            if (burst.hostingItem.is(net.spell_engine.api.tags.SpellEngineItemTags.SPELL_BOOK))
                ticks += (int)(net.spell_engine.SpellEngineMod.config.spell_book_additional_cooldown * 20f);
            var itemCooldowns = player.getCooldowns();
            float remaining = ((net.spell_engine.utils.ItemCooldownManagerExtension)itemCooldowns)
                .SE_getLastCooldownDuration(burst.hostingItem) * itemCooldowns.getCooldownPercent(burst.hostingItem, 0f);
            if (ticks > remaining) itemCooldowns.addCooldown(burst.hostingItem, ticks);
        }
    }
    public static void tick(Entity owner) {
        if (!(owner instanceof Player player) || owner.level().isClientSide()) return;
        Burst b = bursts.get(player.getUUID());
        if (b != null && owner.level().getGameTime() >= b.expires) finish(player, b);
    }
    public static boolean cylindrical(SpellCloud cloud) {
        String id = id(cloud.getSpellEntry());
        return WALL.equals(id) || "skill_tree_rpgs:fire_tier_3_spell_1_modifier_2".equals(id);
    }
    public static boolean great(SpellCloud cloud) {
        // Radius is synchronized by vanilla entity data, unlike owner/modifier state.
        return WALL.equals(id(cloud.getSpellEntry())) && cloud.getBbWidth() >= 2.39f;
    }
    public static EntityDimensions dimensions(float width, float originalHeight, SpellCloud cloud) {
        return EntityDimensions.scalable(width, cylindrical(cloud) ? height(cloud, width / 2f) : originalHeight);
    }
    public static boolean solid(SpellCloud cloud, Entity other) {
        return other instanceof LivingEntity && cloud.isAlive() && cloud.isActive() && great(cloud);
    }
    public static float height(SpellCloud cloud, float radius) {
        return WALL.equals(id(cloud.getSpellEntry())) && radius >= 1.19f ? 3f : 2f;
    }
    public static boolean intersects(AABB box, Vec3 ground, float radius, float height) {
        if (box.maxY <= ground.y || box.minY >= ground.y + height) return false;
        double dx = Math.max(box.minX - ground.x, Math.max(0, ground.x - box.maxX));
        double dz = Math.max(box.minZ - ground.z, Math.max(0, ground.z - box.maxZ));
        return dx * dx + dz * dz <= radius * radius;
    }
    private static boolean visible(Level world, Entity source, Vec3 origin, Entity target) {
        Vec3 center = target.position().add(0, target.getBbHeight() / 2d, 0);
        Vec3 closest = origin.add(VectorHelper.distanceVector(origin, target.getBoundingBox()));
        return world.clip(new ClipContext(origin, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).getType() != HitResult.Type.BLOCK
            || world.clip(new ClipContext(origin, closest, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).getType() != HitResult.Type.BLOCK;
    }
    public static List<Entity> targetsFromArea(Level world, Entity source, Vec3 origin, Vec3 look,
            float radius, Spell.Target.Area area, Predicate<Entity> predicate) {
        if (!(source instanceof SpellCloud cloud) || !cylindrical(cloud)) {
            return TargetHelper.targetsFromArea(world, source, origin, look, radius, area, predicate);
        }
        float height = height(cloud, radius);
        AABB bounds = new AABB(origin.x-radius, origin.y, origin.z-radius,
                origin.x+radius, origin.y+height, origin.z+radius);
        return new ArrayList<>(world.getEntities(source, bounds, e -> e instanceof LivingEntity
                && e.isAlive() && !e.isSpectator() && e.isPickable() && intersects(e.getBoundingBox(), origin, radius, height)
                && (radius < 1f || visible(world, cloud, origin, e))
                && (predicate == null || predicate.test(e))));
    }
}
