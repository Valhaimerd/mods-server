package net.spell_engine.mixin.thirdtier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(LivingEntity.class)
public interface RogueAttackAccess {
 @Accessor("attackStrengthTicker") int rogueAttackStrength();
 @Accessor("attackStrengthTicker") void rogueAttackStrength(int ticks);
 @Accessor("itemSwapTicker") int rogueSwapStrength();
 @Accessor("itemSwapTicker") void rogueSwapStrength(int ticks);
}
