package net.spell_engine.mixin.roguerebalance;
import net.minecraft.core.Holder;import net.minecraft.world.entity.*;
import net.spell_engine.api.spell.Spell;import net.spell_engine.internals.*;import net.spell_engine.rpg_series.config.ConfigFile;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(RogueRules.class) public abstract class RogueAdjustmentMixin {
 @Inject(method="skill",at=@At("TAIL")) private static void stats(ConfigFile.Effects config,CallbackInfo ci){RogueAdjustments.configure(config);}
 @Inject(method="after",at=@At("HEAD")) private static void arrived(boolean success,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact,CallbackInfoReturnable<Boolean> ci){RogueAdjustments.arrived(success,caster,spell,impact);}
}
