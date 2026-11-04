package net.spell_engine.mixin.mages;
import net.minecraft.world.entity.LivingEntity;import net.minecraft.world.damagesource.*;
import net.spell_engine.internals.MagePenetration;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.At;import com.llamalad7.mixinextras.injector.wrapoperation.*;
@Mixin(LivingEntity.class)
public class MageArmorMixin {
 @WrapOperation(method="getDamageAfterArmorAbsorb",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
 private float penetration(LivingEntity target,float damage,DamageSource source,float armor,float toughness,Operation<Float> original){
  if(MagePenetration.CASTER.get()!=null&&source.getEntity()==MagePenetration.CASTER.get()&&source.typeHolder().unwrapKey().orElseThrow().identifier().toString().equals("spell_power:frost"))armor*=.7f;
  return original.call(target,damage,source,armor,toughness);
 }
}
