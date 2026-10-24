package net.spell_engine.mixin.performance;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.entity.SpellCloud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Broken/removed spell data cannot bypass native cloud expiry forever. */
@Mixin(SpellCloud.class)
public abstract class CloudLifecycleMixin {
 @Shadow private int dataIndex;
 @Shadow private Identifier spellId;
 @Shadow public abstract Holder<Spell> getSpellEntry();
 @Inject(method="getCloudData",at=@At("HEAD"),cancellable=true)
 private void validateData(CallbackInfoReturnable<Spell.Delivery.Cloud> ci){
  var entry=spellId==null?null:getSpellEntry();
  Spell.Delivery.Cloud data=null;
  if(entry!=null&&entry.value().deliver!=null&&entry.value().deliver.clouds!=null&&dataIndex>=0&&dataIndex<entry.value().deliver.clouds.size())data=entry.value().deliver.clouds.get(dataIndex);
  if(data==null){var cloud=(SpellCloud)(Object)this;if(!cloud.level().isClientSide())cloud.discard();}
  // Resolve once, as the native method did. No extra registry lookup on healthy cloud ticks.
  ci.setReturnValue(data);
 }
}
