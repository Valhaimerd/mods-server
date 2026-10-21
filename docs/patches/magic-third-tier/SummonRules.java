package net.spell_engine.internals;

import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.summon.*;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.delivery.SpellDelivery;
import net.spell_engine.internals.target.*;
import net.spell_power.api.SpellPower;

/** Local summon rules; marker entries are ignored by ordinary attribute scaling. */
public final class SummonRules {
    public static boolean marker(AttributeScaling scaling, String id) {
        return scaling != null && scaling.entries.stream().anyMatch(e -> e.attribute_id.equals("magic_pack:" + id));
    }
    public static boolean marker(List<Spell.Modifier> mods, String id) {
        return mods.stream().anyMatch(m -> marker(m.summon_attribute_scaling, id));
    }
    public static int count(int normal, Spell.Impact.Action.Summon def, List<Spell.Modifier> mods, boolean group) {
        if (def.entity_type_id.equals("wizards:fire_hydra") && marker(mods, "ancient_hydra")) return 1;
        if (def.entity_type_id.equals("wizards:frost_elemental")) {
            if (group) return 1;
            if (marker(mods, "frost_colossus")) return 1;
            if (marker(mods, "frost_legion")) return 3;
        }
        return normal;
    }
    private static boolean type(SummonedEntity e, String path) {
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString().equals("wizards:" + path);
    }
    public static boolean ancient(LivingEntity e) {
        return e instanceof SummonedEntity s && type(s,"fire_hydra") && e.getScale() > 2.99f;
    }
    public static void summoned(SummonedEntity e, SpellSummoned.Args args) {
        if (type(e,"frost_elemental")) {
            double p = SpellPower.getSpellPower(net.spell_power.api.SpellSchools.FROST,args.owner).nonCriticalValue();
            boolean colossus = marker(args.attribute_scaling,"frost_colossus");
            double factor = colossus ? 2 : marker(args.attribute_scaling,"frost_legion") ? .8 : 1;
            set(e,Attributes.MAX_HEALTH,(30+.5*p)*factor);
            set(e,Attributes.ARMOR,(5+.08*p)*factor);
            set(e,Attributes.SCALE,colossus ? 1.7 : 1);
            set(e,Attributes.ATTACK_DAMAGE,.5*p*factor);
            net.spell_engine.rpg_series.config.ConfigUtil.attribute("spell_power:frost").ifPresent(a -> set(e,a,.2*p*factor));
            if (colossus) set(e,Attributes.KNOCKBACK_RESISTANCE,args.owner.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            e.refreshDimensions();
            e.setHealth(e.getMaxHealth());
        } else if (type(e,"fire_hydra") && marker(args.attribute_scaling,"ancient_hydra")) {
            set(e,Attributes.SCALE,3);
            e.refreshDimensions();
        }
    }
    public static SpellSummoned.Args prepare(SpellSummoned.Args args) {
        if (args.spell.is(Identifier.parse("wizards:frost_elemental"))) {
            var b = new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(args.behaviour),SummonBehaviour.class);
            boolean legion = marker(args.attribute_scaling,"frost_legion");
            for (var action : b.actions) {
                if (action.spell_cast != null && action.spell_cast.spell_id.equals("wizards:frost_nova"))
                    action.spell_cast.spell_id = legion ? "wizards:legion_frost_nova" : "wizards:elemental_frost_nova";
            }
            return new SpellSummoned.Args(args.owner,args.spell,b,args.attribute_scaling,args.context);
        }
        if (!marker(args.attribute_scaling,"ancient_hydra")) return args;
        var b = new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(args.behaviour),SummonBehaviour.class);
        var action = new SummonBehaviour.Action.SpellCast("wizards:ancient_hydra_breath",0);
        action.range.preferred = 1;
        action.cast_animation_variants = List.of(2);
        action.release_animation_variants = List.of(2);
        b.actions = List.of(SummonBehaviour.Action.spell(action));
        b.targeting.detection_range.mode = SummonBehaviour.Targeting.DetectionRange.Mode.STATIC;
        b.targeting.detection_range.value = 12;
        return new SpellSummoned.Args(args.owner,args.spell,b,args.attribute_scaling,args.context);
    }

    private static void set(SummonedEntity e, Holder<Attribute> attr, double value) {
        var instance = e.getAttribute(attr);
        if (instance == null) throw new IllegalStateException("Missing summon attribute " + attr);
        instance.removeModifier(Identifier.fromNamespaceAndPath("spell_engine","summon_scaling/" + attr.getRegisteredName().replace(':','/')));
        instance.setBaseValue(value);
    }
    // Used by the frost subclass on both sides so vanilla health/armor caps cannot clip the formula.
    public static double unclamped(SummonedEntity e, Holder<Attribute> attr, double normal) {
        String id = attr.getRegisteredName();
        if (!(id.equals("minecraft:max_health") || id.equals("minecraft:armor") || id.equals("minecraft:attack_damage") || id.equals("minecraft:scale"))) return normal;
        var a = e.getAttribute(attr);
        if (a == null) return normal;
        double base=a.getBaseValue();
        for(var m:a.getModifiers()) if(m.operation()==AttributeModifier.Operation.ADD_VALUE) base+=m.amount();
        double result=base;
        for(var m:a.getModifiers()) if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE) result+=base*m.amount();
        for(var m:a.getModifiers()) if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) result*=1+m.amount();
        return Math.max(0,result);
    }
    public static void orient(SummonedEntity e, LivingEntity target) {
        if(!ancient(e)) return;
        Vec3 horizontal=target.getBoundingBox().getCenter().subtract(e.position());
        e.setYBodyRot((float)Math.toDegrees(Math.atan2(-horizontal.x,horizontal.z)));
        Vec3 delta=target.getBoundingBox().getCenter().subtract(mouth(e)).normalize();
        float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
        e.setYRot(yaw);e.setYHeadRot(yaw);
        e.setXRot((float)-Math.toDegrees(Math.asin(Math.max(-1,Math.min(1,delta.y)))));
    }
    public static Vec3 mouth(LivingEntity e) {
        double yaw=Math.toRadians(e.yBodyRot);
        double side=.050096541426800936*e.getScale(), forward=1.7454600712720936*e.getScale();
        return e.position().add(Math.cos(yaw)*side-Math.sin(yaw)*forward,1.9109827381010058*e.getScale(),Math.sin(yaw)*side+Math.cos(yaw)*forward);
    }
    public static Vec3 launch(Vec3 normal, LivingEntity e) { return ancient(e) ? mouth(e) : normal; }
    public static SpellTarget.SearchResult targets(SummonedEntity e) {
        Vec3 origin=mouth(e), direction=e.getLookAngle();
        var targets=new ArrayList<Entity>();
        for(LivingEntity target:e.level().getEntitiesOfClass(LivingEntity.class,new AABB(origin,origin).inflate(12))) {
            if(target==e || !target.isAlive() || !EntityRelations.actionAllowed(SpellTarget.FocusMode.AREA,SpellTarget.Intent.HARMFUL,e,target)) continue;
            Vec3 point=target.getBoundingBox().getCenter(), delta=point.subtract(origin);
            double distance=delta.length();
            if(distance>12 || (distance>0 && delta.scale(1/distance).dot(direction)<Math.cos(Math.toRadians(10)))) continue;
            if(e.level().clip(new ClipContext(origin,point,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e)).getType()!=HitResult.Type.MISS) continue;
            targets.add(target);
        }
        return new SpellTarget.SearchResult(targets,null);
    }
    /** Keep the native 25 deliveries/100 ticks cadence through successive channel windows. */
    public static boolean channel(SummonedEntity e, Holder<Spell> entry) {
        if(!ancient(e)) return false;
        long time=e.level().getGameTime();
        SpellCast.Process process=e.getSpellCastProcess();
        if(process==null || process.progress(time).ratio()>=1) {
            long start=process==null?time:process.startedAt()+100;
            process=new SpellCast.Process(e,entry,null,1,100,start);
            e.declareCastProcess(process);
        }
        if(process.isDue(time)) {
            process.markDue();
            var context=new SpellExecution.ImpactContext().power(SpellPower.getSpellPower(entry.value().school,e)).target(SpellTarget.FocusMode.AREA).channeled(.2f);
            if(e.getOwner()!=null) context=context.effectiveCaster(e.getOwner());
            SpellDelivery.resolveAndDeliver(e.level(),e,entry,targets(e),context, completion -> {});
        }
        return true;
    }
}
