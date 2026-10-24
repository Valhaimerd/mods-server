package com.emandev.bifrost.mixin;

import com.emandev.bifrost.BifrostSkills;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.internals.container.SpellContainerSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpellContainerSource.class)
public abstract class BifrostSkillSourceMixin {
    @Inject(method="update",at=@At("RETURN"))
    private static void bifrost$appendAfterEquipment(Player player,CallbackInfo ci){BifrostSkills.append(player);}
}
