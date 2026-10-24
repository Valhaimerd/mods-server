package net.spell_engine.mixin.roguecounter;
import net.minecraft.core.Holder;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.rpg_series.config.ConfigFile;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(RogueRules.class) public abstract class CounterMixin {
 @Inject(method="skill",at=@At("TAIL")) private static void configure(ConfigFile.Effects config,CallbackInfo ci){PreparedCounter.configure(config);}
 @Inject(method="evaluate",at=@At("HEAD"),cancellable=true) private static void eligible(Holder<Spell> spell,Spell.Trigger trigger,SpellTriggers.Event event,CallbackInfoReturnable<Boolean> ci){if(!PreparedCounter.allowed(spell,trigger,event))ci.setReturnValue(false);}
}
