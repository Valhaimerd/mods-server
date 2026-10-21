package local.audit;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.entity.SpellCloud;
import net.spell_engine.internals.WallRules;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.cost.SpellCooldowns;

/** Run in the isolated server's empty test arena; tick timeout assertions are separate stages. */
public final class WallProbe {
    private static int checks;
    static void check(boolean yes, String label) { if (!yes) throw new AssertionError(label); checks++; }
    static Holder<Spell> wall(ServerLevel l) { return SpellRegistry.from(l).get(Identifier.parse("wizards:fire_wall")).orElseThrow(); }
    public static int fullCast(ServerLevel level,ServerPlayer player) {
        checks=0;
        var h=wall(level);var owner=(net.spell_engine.internals.container.SpellContainerSource.Owner)player;
        var old=owner.getSpellContainers();
        var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,java.util.List.of("wizards:fire_wall"));
        var book=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK);
        var source=new net.spell_engine.internals.container.SpellContainerSource.SourcedContainer("wall_probe",book,container);
        owner.setSpellContainers(new net.spell_engine.internals.container.SpellContainerSource.Result(old.activeContainer(),java.util.List.of(h),java.util.List.of(),java.util.List.of(),java.util.List.of(source)));
        boolean creative=player.getAbilities().instabuild;player.getAbilities().instabuild=true;
        var manager=((SpellCaster.Player)player).getCooldownManager();manager.remove(Identifier.parse("wizards:fire_wall"));
        var option=net.spell_engine.internals.casting.SpellCast.Option.of(player,h);
        try {
            check(!WallRules.ignoreHeldRepeat(option,true),"wall fresh key press allowed");
            check(WallRules.ignoreHeldRepeat(option,false),"wall held key cannot spend extra charges");
            var other=net.spell_engine.internals.casting.SpellCast.Option.of(player,SpellRegistry.from(level).get(Identifier.parse("wizards:fire_hydra")).orElseThrow());
            check(!WallRules.ignoreHeldRepeat(other,false),"other instant spells retain held-repeat behavior");
            for(int i=1;i<=4;i++) {
                ((SpellCaster.Player)player).getInteractor().requestCast(Identifier.parse("wizards:fire_wall"),new net.spell_engine.internals.casting.SpellCast.TargetSnapshot(java.util.List.of(),null));
                check(manager.getCooldownDuration(h)==(i==4?480:0),"real wall cast "+i+" cooldown");
                check(player.getCooldowns().getCooldownPercent(book,0)==0 || i==4,"wall book stays usable for remaining casts");
            }
            check(!net.spell_engine.internals.casting.SpellCasting.attempt(player,player.getMainHandItem(),Identifier.parse("wizards:fire_wall")).isSuccess(),"fifth actual wall cast blocked");
        }finally{owner.setSpellContainers(old);player.getAbilities().instabuild=creative;player.getCooldowns().removeCooldown(Identifier.parse("minecraft:book"));}
        return checks;
    }
    public static int immediate(ServerLevel level, ServerPlayer player, Vec3 origin) {
        checks=0;
        Holder<Spell> h=wall(level);
        var manager=((SpellCaster.Player)player).getCooldownManager();
        manager.remove(Identifier.parse("wizards:fire_wall"));
        for(int i=0;i<3;i++) {
            SpellCooldowns.imposeCooldown(player,null,h,1);
            check(!manager.isCoolingDown(h),"wall recast "+i+" available");
        }
        SpellCooldowns.imposeCooldown(player,null,h,1);
        check(manager.getCooldownDuration(h)>0,"fourth wall immediately starts normal cooldown");
        SpellCloud cloud=new SpellCloud(level);
        Spell.Modifier.Cloud mod=new Spell.Modifier.Cloud();mod.radius_add=.3f;
        cloud.onCreatedFromSpell(Identifier.parse("wizards:fire_wall"),h.value().deliver.clouds.get(0),null,8,mod);
        cloud.setPos(origin);level.addFreshEntity(cloud);cloud.tick();
        check(Math.abs(cloud.getBbWidth()-2.4f)<.001,"Great Wall radius 1.2 synchronized width");
        check(Math.abs(cloud.getBbHeight()-3f)<.001,"Great Wall height 3");
        ArmorStand target=new ArmorStand(level,origin.x-2.5,origin.y,origin.z);
        level.addFreshEntity(target);
        try {
            check(cloud.canBeCollidedWith(target),"Great Wall is native living collision");
            double start=target.getX();
            AABB swept = target.getBoundingBox().expandTowards(new Vec3(5,0,0));
            var queried = level.getEntities(target, swept, e -> true);
            var colliders = level.getEntityCollisions(target, swept);
            System.out.println("WALL_DIAGNOSTIC before cloud="+cloud.getId()+" cloudBBox="+cloud.getBoundingBox()
                +" phase="+cloud.isActive()+" target="+target.position()+" targetBBox="+target.getBoundingBox()
                +" noPhysics="+target.noPhysics+" canCollide="+target.canCollideWith(cloud)
                +" worldEntity="+(level.getEntity(cloud.getId())==cloud)
                +" queryContains="+queried.contains(cloud)+" querySize="+queried.size()
                +" colliders="+colliders.size()+" cloudAdded="+cloud.isAddedToLevel());
            target.move(MoverType.SELF,new Vec3(5,0,0));
            System.out.println("WALL_DIAGNOSTIC after position="+target.position()+" horizontalCollision="+target.horizontalCollision);
            check(target.getX()<origin.x,"native LivingEntity.move cannot cross wall");
            check(target.getX()>start,"movement advanced up to barrier");
            Arrow arrow=new Arrow(EntityTypes.ARROW,level);
            check(!cloud.canBeCollidedWith(arrow),"projectiles are not physically blocked");
            check(!cloud.canBeHitByProjectile()&&!cloud.isPickable(),"cloud neither intercepts projectiles nor attacks");
            check(WallRules.intersects(new AABB(origin.x-.2,origin.y+1.8,origin.z-.2,origin.x+.2,origin.y+3.6,origin.z+.2),origin,.9f,2),"jump hitbox overlaps 2-high cylinder");
            check(!WallRules.intersects(new AABB(origin.x-.2,origin.y+2.01,origin.z-.2,origin.x+.2,origin.y+3.8,origin.z+.2),origin,.9f,2),"hitbox wholly above cylinder excluded");
            check(!WallRules.intersects(new AABB(origin.x+.8,origin.y,origin.z+.8,origin.x+1,origin.y+1.8,origin.z+1),origin,.9f,2),"cylinder excludes square corner");
        } finally {target.discard();cloud.discard();}
        return checks;
    }
    /** Call after immediate, then wait 99 actual server ticks without casting. */
    public static void startTimeout(ServerLevel level,ServerPlayer player) {
        ((SpellCaster.Player)player).getCooldownManager().remove(Identifier.parse("wizards:fire_wall"));
        SpellCooldowns.imposeCooldown(player,null,wall(level),1);
    }
    public static void beforeTimeout(ServerLevel level,ServerPlayer player) {
        check(!((SpellCaster.Player)player).getCooldownManager().isCoolingDown(wall(level)),"wall window remains open before tick100");
    }
    /** At tick100, manager.tickUpdate invokes WallRules.tick automatically even with no cast. */
    public static void afterTimeout(ServerLevel level,ServerPlayer player) {
        check(((SpellCaster.Player)player).getCooldownManager().isCoolingDown(wall(level)),"unused wall window automatically starts cooldown");
    }
}
