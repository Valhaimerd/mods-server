package local.crossarms.mixin;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Accuracy is spread, not a guaranteed percentage of hits. */
@Mixin(ProjectileWeaponItem.class)
public class FrenzyAccuracyMixin {
    @ModifyVariable(method="shoot",at=@At("HEAD"),argsOnly=true,ordinal=1)
    private float crossarms$tightSpread(float inaccuracy,ServerLevel level,LivingEntity shooter,InteractionHand hand,ItemStack stack,List<ItemStack> arrows,float speed,float ignored,boolean critical,LivingEntity target){
        var frenzy=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:frenzy"));
        return stack.getItem() instanceof CrossbowItem&&frenzy.isPresent()&&EnchantmentHelper.getItemEnchantmentLevel(frenzy.get(),stack)>0?inaccuracy*.05f:inaccuracy;
    }
}
