package net.spell_engine.internals;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.spell_engine.api.effect.Effects;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.delivery.SpellDelivery;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.rpg_series.config.ConfigFile;
import net.spell_power.api.*;
import net.spell_power.api.statuseffects.SpellVulnerabilityStatusEffect;

/** A consumed next-spell buff is snapshotted into its delivery context, including delayed projectiles. */
public final class PassiveRules {
    private record Cast(Identifier spell, long start, String marker) {}
    private static final Map<LivingEntity,Cast> channels = new WeakHashMap<>();
    private static final String ARCANE = "magic_pack:presence_spell", FROST = "magic_pack:reflex_spell";

    public static void configure(ConfigFile.Effects config) {
        ((SpellVulnerabilityStatusEffect)net.skill_tree_rpgs.effect.SkillEffects.FIRE_VULNERABILITY.effect)
            .setVulnerability(SpellSchools.FIRE,new SpellPower.Vulnerability(.04f,0,0));
        var entries=net.skill_tree_rpgs.effect.SkillEffects.entries;
        if(entries.stream().noneMatch(e->e.id.toString().equals("skill_tree_rpgs:arcane_vulnerability")))
            entries.add(new Effects.Entry(Identifier.parse("skill_tree_rpgs:arcane_vulnerability"),"Arcane Vulnerability",
                "Increased Arcane damage taken by 20%.",new SpellVulnerabilityStatusEffect(MobEffectCategory.HARMFUL,0xAA66FF)
                    .setVulnerability(SpellSchools.ARCANE,new SpellPower.Vulnerability(.2f,0,0))));
    }
    private static boolean has(LivingEntity caster,String effect) {
        return BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effect)).map(caster::hasEffect).orElse(false);
    }
    public static SpellExecution.ImpactContext snapshot(LivingEntity caster,Holder<Spell> spell,SpellExecution.ImpactContext context) {
        if(spell.value().type!=Spell.Type.ACTIVE) return context;
        var process=caster instanceof SpellCaster.Player owner?owner.getSpellCastProcess():null;
        Identifier id=spell.unwrapKey().orElseThrow().identifier();
        boolean channel=process!=null && process.id().equals(id) && SpellParameters.isChanneled(spell.value());
        Cast previous=channels.get(caster);
        String marker=null;
        if(channel && previous!=null && previous.spell.equals(id) && previous.start==process.startedAt()) marker=previous.marker;
        else {
            channels.remove(caster);
            String effect=null;
            if(spell.value().school==SpellSchools.ARCANE && has(caster,"skill_tree_rpgs:presence_of_mind")) {
                marker=ARCANE;effect="skill_tree_rpgs:presence_of_mind";
            } else if(spell.value().school==SpellSchools.FROST && has(caster,"skill_tree_rpgs:arctic_reflex")) {
                marker=FROST;effect="skill_tree_rpgs:arctic_reflex";
            }
            if(effect!=null) caster.removeEffect(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effect)).orElseThrow());
            // Remember even an unbuffed channel, so a roll halfway through cannot empower its later deliveries.
            if(channel) channels.put(caster,new Cast(id,process.startedAt(),marker));
        }
        if(marker==null) return context;
        Spell.Modifier copy=context.chargeModifier()==null?new Spell.Modifier():new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(context.chargeModifier()),Spell.Modifier.class);
        copy.spell_pattern=marker;
        return context.chargeModifier(copy);
    }
    public static boolean deliver(Level world,LivingEntity caster,Holder<Spell> spell,SpellTarget.SearchResult targets,
            SpellExecution.ImpactContext context,Consumer<SpellExecution.DeliveryCompletion> completion) {
        return SpellDelivery.resolveAndDeliver(world,caster,spell,targets,snapshot(caster,spell,context),completion);
    }
    private static boolean marked(SpellExecution.ImpactContext context,String marker) {
        return context.chargeModifier()!=null && marker.equals(context.chargeModifier().spell_pattern);
    }
    public static Spell.Impact impact(LivingEntity caster,Holder<Spell> spell,Spell.Impact original,SpellExecution.ImpactContext context) {
        Spell.Impact result=RevisionRules.impact(caster,spell,original,context);
        if(result.action.type==Spell.Impact.Action.Type.DAMAGE && marked(context,FROST)) {
            result=new com.google.gson.Gson().fromJson(new com.google.gson.Gson().toJson(result),Spell.Impact.class);
            result.action.damage.spell_power_coefficient*=1.2f;
        }
        return result;
    }
    public static SpellModifiers.ExtendedImpacts impacts(SpellModifiers.ExtendedImpacts original,SpellExecution.ImpactContext context) {
        if(!marked(context,ARCANE) || original.impacts().stream().noneMatch(i->i.action.type==Spell.Impact.Action.Type.DAMAGE)) return original;
        var list=new ArrayList<>(original.impacts());
        var impact=new Spell.Impact();impact.action=new Spell.Impact.Action();impact.action.type=Spell.Impact.Action.Type.STATUS_EFFECT;
        impact.action.status_effect=new Spell.Impact.Action.StatusEffect();
        impact.action.status_effect.effect_id="skill_tree_rpgs:arcane_vulnerability";
        impact.action.status_effect.duration=6;
        list.add(impact);
        return new SpellModifiers.ExtendedImpacts(list,original.areaImpact());
    }
}
