package net.spell_engine.mixin.entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.*;
import net.spell_engine.internals.SpatialState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class) public abstract class SpatialStateMixin implements SpatialState {
 @Unique private static final EntityDataAccessor<Boolean> SPATIAL_VANISH=SynchedEntityData.defineId(LivingEntity.class,EntityDataSerializers.BOOLEAN);
 @Inject(method="defineSynchedData",at=@At("TAIL")) private void spatial$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(SPATIAL_VANISH,false);}
 public boolean spatial$vanished(){return ((LivingEntity)(Object)this).getEntityData().get(SPATIAL_VANISH);}
 public void spatial$vanished(boolean value){((LivingEntity)(Object)this).getEntityData().set(SPATIAL_VANISH,value);}
}
