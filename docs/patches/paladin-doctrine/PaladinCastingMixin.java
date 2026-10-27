package net.skill_tree_rpgs.mixin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.skill_tree_rpgs.compat.PaladinDoctrine;
import net.spell_engine.internals.casting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellCasting.class)
public abstract class PaladinCastingMixin {
    @Inject(method="attempt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/resources/Identifier;Z)Lnet/spell_engine/internals/casting/SpellCast$Attempt;",at=@At("HEAD"),cancellable=true)
    private static void paladin$cast(Player player,ItemStack stack,Identifier id,boolean ammo,CallbackInfoReturnable<SpellCast.Attempt> cir) {
        if(player instanceof ServerPlayer p&&PaladinDoctrine.rejectAction(p,stack))cir.setReturnValue(SpellCast.Attempt.none());
    }
}
