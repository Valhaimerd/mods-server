package net.spell_engine.mixin.warrior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.spell_engine.entity.SpellProjectile;
import net.spell_engine.internals.SpellExecution;
@Mixin(SpellProjectile.class)
public interface ThrowContextAccess {
 @Accessor("context") void warrior$setContext(SpellExecution.ImpactContext context);
}
