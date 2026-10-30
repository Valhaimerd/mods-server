package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionResult;
import java.util.function.Consumer;
@Mixin(ItemStack.class)
public class ToolMixin {
 @Inject(method={"mineBlock","postHurtEnemy"},at=@At("HEAD")) private void beginVoid(CallbackInfo ci){Utilities.TOOL_USE.set(Utilities.TOOL_USE.get()+1);}
 @Inject(method="hurtEnemy",at=@At("HEAD")) private void beginHit(CallbackInfoReturnable<Boolean> cir){Utilities.TOOL_USE.set(Utilities.TOOL_USE.get()+1);}
 @Inject(method={"mineBlock","postHurtEnemy"},at=@At("RETURN")) private void endVoid(CallbackInfo ci){Utilities.TOOL_USE.set(Math.max(0,Utilities.TOOL_USE.get()-1));}
 @Inject(method="hurtEnemy",at=@At("RETURN")) private void endHit(CallbackInfoReturnable<Boolean> cir){Utilities.TOOL_USE.set(Math.max(0,Utilities.TOOL_USE.get()-1));}
 @Inject(method={"useOn","interactLivingEntity"},at=@At("HEAD")) private void beginUse(CallbackInfoReturnable<InteractionResult> cir){Utilities.TOOL_USE.set(Utilities.TOOL_USE.get()+1);}
 @Inject(method={"useOn","interactLivingEntity"},at=@At("RETURN")) private void endUse(CallbackInfoReturnable<InteractionResult> cir){Utilities.TOOL_USE.set(Math.max(0,Utilities.TOOL_USE.get()-1));}
 @Inject(method="processDurabilityChange(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)I",at=@At("RETURN"),cancellable=true)
 private void preserve(int amount,ServerLevel level,LivingEntity entity,CallbackInfoReturnable<Integer> cir){
  ItemStack s=(ItemStack)(Object)this;
  if(cir.getReturnValue()>0&&Utilities.TOOL_USE.get()>0&&entity instanceof ServerPlayer p&&s.isDamageableItem()&&isTool(s)&&Utilities.stat(p,"durability")>0&&p.getRandom().nextFloat()<Utilities.stat(p,"durability"))cir.setReturnValue(0);
 }
 private static boolean isTool(ItemStack s){
  return s.is(Items.SHEARS)||java.util.stream.Stream.of("pickaxes","axes","shovels","hoes").anyMatch(k->s.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,net.minecraft.resources.Identifier.parse("minecraft:"+k))));
 }
}
