package net.spell_engine.internals;
import net.minecraft.world.entity.LivingEntity;
import net.spell_power.api.SpellPower;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.puffish.attributesmod.api.DynamicModification;
import net.puffish.attributesmod.api.PuffishAttributes;
/** Support-only snapshots include real melee investment without changing skill impact schools. */
public final class SupportScaling {
 public static double melee(LivingEntity e){return Math.max(0,DynamicModification.create().withPositive(PuffishAttributes.MELEE_DAMAGE,e).applyTo(SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_MELEE,e).nonCriticalValue()));}
 public static double reduction(double p,boolean bulwark){return Math.clamp((bulwark?.08:.05)+(bulwark?.02:.015)*p,0,bulwark?.4:.3);}
 public static double knockback(double m){return Math.clamp(.1+.035*m,0,.8);}
 public static int blocks(double m){return Math.clamp(1+(int)Math.floor(Math.max(0,m)/10),1,3);}
 public static double oathDamage(double m,boolean decree){return Math.clamp((decree?.2:.15)*m,0,decree?4:3);}
 public static double oathArmor(double a,boolean decree){return Math.clamp(2+(decree?.3:.2)*a,0,decree?8:6);}
 public static double shield(double p,boolean decree){return Math.clamp(1+(decree?.6:.45)*p,0,decree?10:8);}
 public static double receivedHealing(LivingEntity e,double amount){var a=net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(net.minecraft.resources.Identifier.parse("spell_engine:healing_taken"));double multiplier=a.map(e::getAttributeValue).orElse(100d)/100;return DynamicModification.create().withPositive(PuffishAttributes.HEALING,e).applyTo(amount*multiplier);}
 public static double activeReduction(LivingEntity e){var root=EffectDetails.data(e);if(!e.hasEffect(PaladinRevamp.effect(PaladinRevamp.DP))||!root.has("support"))return 0;var support=root.getAsJsonObject("support");if(!support.has("protection"))return 0;var r=support.getAsJsonObject("protection");return r.get("expires").getAsDouble()>e.level().getGameTime()?Math.clamp(r.get("reduction").getAsDouble(),0,.4):0;}
}
