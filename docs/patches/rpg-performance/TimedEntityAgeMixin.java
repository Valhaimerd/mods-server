package net.spell_engine.mixin.performance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.spell_engine.entity.SummonedEntity;
import net.spell_engine.entity.SpellProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Tick-based lifetimes must not restart whenever a chunk is loaded. */
@Mixin({SummonedEntity.class,SpellProjectile.class})
public abstract class TimedEntityAgeMixin {
 @Inject(method="addAdditionalSaveData",at=@At("TAIL"))
 private void saveAge(ValueOutput out,CallbackInfo ci){
  Entity e=(Entity)(Object)this;out.putInt("rpg_pack_age",Math.max(0,e.tickCount));
  if(e instanceof SpellProjectile p){out.putFloat("rpg_pack_range",p.range);out.putFloat("rpg_pack_distance",p.moveDist);}
 }
 @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
 private void loadAge(ValueInput in,CallbackInfo ci){
  Entity e=(Entity)(Object)this;e.tickCount=Math.max(0,in.getIntOr("rpg_pack_age",e.tickCount));
  if(e instanceof SpellProjectile p){p.range=in.getFloatOr("rpg_pack_range",p.range);p.moveDist=in.getFloatOr("rpg_pack_distance",p.moveDist);}
 }
}
