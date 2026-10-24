package com.emandev.bifrost.mixin;
import com.emandev.bifrost.AsgardAbilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.spell_engine.internals.casting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A class cast cannot overlap a weapon channel, even if its own cast is instant. */
@Mixin(SpellCasting.class)
public abstract class AbilitySpellMixin {
    @Inject(method="attempt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/Identifier;Z)Lnet/spell_engine/internals/casting/SpellCast$Attempt;",at=@At("HEAD"),cancellable=true)
    private static void attempt(Player player,ItemStack stack,Identifier id,boolean checkAmmo,CallbackInfoReturnable<SpellCast.Attempt> cir){
        if(player instanceof ServerPlayer p&&AsgardAbilities.active(p))cir.setReturnValue(SpellCast.Attempt.none());
    }
}
