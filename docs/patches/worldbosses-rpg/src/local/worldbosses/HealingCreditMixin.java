package local.worldbosses.mixin;

import local.worldbosses.RaidRuntime;
import local.worldbosses.GearRetirement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellExecution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Capture the real Spell Engine caster rather than crediting nearby bystanders. */
@Pseudo
@Mixin(targets="net.spell_engine.internals.impact.SpellImpacts",remap=false)
public abstract class HealingCreditMixin {
    private static final String IMPACT="performImpacts(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Ljava/util/List;Lnet/spell_engine/internals/SpellExecution$ImpactContext;ZLnet/spell_engine/api/spell/Spell$Impact$Action$Type;)Z";
    @Inject(method=IMPACT,at=@At("HEAD"))
    private static void begin(Level level,LivingEntity caster,Entity target,Entity source,Holder<Spell> spell,List<Spell.Impact> impacts,SpellExecution.ImpactContext context,boolean secondary,Spell.Impact.Action.Type type,CallbackInfoReturnable<Boolean> ci) { RaidRuntime.beginHeal(caster); }
    @Inject(method=IMPACT,at=@At("RETURN"))
    private static void end(CallbackInfoReturnable<Boolean> ci) { RaidRuntime.endHeal(); }
}
