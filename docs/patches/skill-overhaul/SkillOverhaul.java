package net.valhaimerd.orboblivioncompat;

import com.google.gson.JsonParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.puffish.skillsmod.api.*;

/** Owned-pet stat bridge; skill IDs and multipliers are generated alongside tree data. */
public final class SkillOverhaul {
    public static final Identifier CATEGORY=Identifier.parse("adventurer_skills:adventurer_skills");
    private static final String MIGRATION="skill_overhaul_v1";
    private static Map<String,Map<String,Double>> bonuses;
    public static void init() {
        try (var in=SkillOverhaul.class.getResourceAsStream("/skill-overhaul-pets.json")) {
            if(in==null)throw new IllegalStateException("Missing Companion node map");
            bonuses=new HashMap<>();
            JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().entrySet().forEach(node->{
                var values=new HashMap<String,Double>();
                node.getValue().getAsJsonObject().entrySet().forEach(v->values.put(v.getKey(),v.getValue().getAsDouble()));
                bonuses.put(node.getKey(),values);
            });
        } catch(IOException ex) { throw new UncheckedIOException(ex); }
        NeoForge.EVENT_BUS.addListener(SkillOverhaul::login);
        NeoForge.EVENT_BUS.addListener(SkillOverhaul::copyMigration);
        NeoForge.EVENT_BUS.addListener(SkillOverhaul::tick);
        NeoForge.EVENT_BUS.addListener(SkillOverhaul::damage);
    }
    public static void migrate(ServerPlayer player) {
        if(player.getPersistentData().getBooleanOr(MIGRATION,false))return;
        SkillsAPI.getCategory(CATEGORY).ifPresent(category->{
            boolean hadPoints=category.getSpentPoints(player)>0;
            category.resetSkills(player);
            if(hadPoints) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Skill tree updated: Adventurer points refunded; earned experience preserved. Choose your new paths."));
            }
            player.getPersistentData().putBoolean(MIGRATION,true);
        });
    }
    private static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)migrate(player);
    }
    private static void copyMigration(PlayerEvent.Clone event) {
        if(event.getOriginal().getPersistentData().getBooleanOr(MIGRATION,false))
            event.getEntity().getPersistentData().putBoolean(MIGRATION,true);
    }
    public static Map<String,Double> effects(ServerPlayer owner) {
        var result=new HashMap<String,Double>();
        SkillsAPI.getCategory(CATEGORY).ifPresent(category->category.streamUnlockedSkills(owner).forEach(skill->{
            var values=bonuses.get(skill.getId());
            if(values!=null)values.forEach((key,value)->result.merge(key,value,Double::sum));
        }));
        return result;
    }
    private static ServerPlayer owner(LivingEntity pet) {
        return pet instanceof OwnableEntity own && own.getOwner() instanceof ServerPlayer player ? player:null;
    }
    private static void tick(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && player.tickCount%20==0)applyCritical(player);
        if(event.getEntity() instanceof LivingEntity pet && !pet.level().isClientSide() && pet.tickCount%20==0)
            if(pet instanceof OwnableEntity)apply(pet);
    }
    /** Fixed percentage points after existing gear/innate multipliers, without changing them. */
    public static void applyCritical(ServerPlayer player) {
        double points=SkillsAPI.getCategory(CATEGORY).map(category->category.streamUnlockedSkills(player)
            .mapToDouble(skill->switch(skill.getId()) {
                case "ro3sae58m8jzn4yz","gs6jmtskmyhpbcnu"->2.5;
                default->0;
            }).sum()).orElse(0D);
        var attribute=net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElse(null);
        if(attribute==null)return;
        var instance=player.getAttribute(attribute);if(instance==null)return;
        var id=Identifier.parse("orb_of_oblivion_puffish_compat:pet_critical_points");
        double baseMultiplier=1,totalMultiplier=1;
        for(var m:instance.getModifiers()) {
            if(m.id().equals(id))continue;
            if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_BASE)baseMultiplier+=m.amount();
            if(m.operation()==AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)totalMultiplier*=1+m.amount();
        }
        double scale=baseMultiplier*totalMultiplier;
        modifier(player,attribute,"critical_points",scale>0 ? points/scale:0,AttributeModifier.Operation.ADD_VALUE);
    }
    public static void apply(LivingEntity pet) {
        var player=owner(pet);var e=player==null ? Map.<String,Double>of():effects(player);
        modifier(pet,Attributes.ATTACK_DAMAGE,"damage",e.getOrDefault("damage",0D),AttributeModifier.Operation.ADD_VALUE);
        modifier(pet,Attributes.ATTACK_DAMAGE,"damage_pct",e.getOrDefault("damage_pct",0D),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(pet,Attributes.MOVEMENT_SPEED,"speed",e.getOrDefault("speed",0D),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        modifier(pet,Attributes.ATTACK_KNOCKBACK,"knock",e.getOrDefault("knock",0D),AttributeModifier.Operation.ADD_VALUE);
        modifier(pet,Attributes.KNOCKBACK_RESISTANCE,"kr",e.getOrDefault("kr",0D),AttributeModifier.Operation.ADD_VALUE);
        modifier(pet,Attributes.MAX_HEALTH,"hp",e.getOrDefault("hp",0D),AttributeModifier.Operation.ADD_VALUE);
        if(pet.getHealth()>pet.getMaxHealth())pet.setHealth(pet.getMaxHealth());
    }
    private static void modifier(LivingEntity pet,Holder<Attribute> attribute,String key,double amount,AttributeModifier.Operation op) {
        var instance=pet.getAttribute(attribute);if(instance==null)return;
        var id=Identifier.fromNamespaceAndPath("orb_of_oblivion_puffish_compat","pet_"+key);
        var old=instance.getModifier(id);
        if(old!=null && old.amount()==amount && old.operation()==op)return;
        instance.removeModifier(id);
        if(amount!=0)instance.addTransientModifier(new AttributeModifier(id,amount,op));
    }
    public static float reducedDamage(LivingEntity pet,float amount) {
        var player=owner(pet);if(player==null)return amount;
        var e=effects(player);
        return (float)Math.max(0,(amount-e.getOrDefault("res",0D))*(1-Math.min(1,e.getOrDefault("res_pct",0D))));
    }
    private static void damage(LivingDamageEvent.Pre event) {
        if(event.getEntity() instanceof OwnableEntity && !event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS))
            event.setNewDamage(reducedDamage(event.getEntity(),event.getNewDamage()));
    }
}
