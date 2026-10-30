package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.skill.*;
@Mixin(value=CategoryData.class,remap=false)
public class CategoryGateMixin {
 @Inject(method="getSkillState",at=@At("HEAD"),cancellable=true)
 private void gate(CategoryConfig c,SkillConfig s,SkillDefinitionConfig d,CallbackInfoReturnable<Skill.State> cir){
  if(c.id().equals(Utilities.CATEGORY)&&!Utilities.allowed(((CategoryData)(Object)this).getUnlockedSkillIds(),s.id()))cir.setReturnValue(Skill.State.EXCLUDED);
 }
 @Inject(method="canUnlockSkill",at=@At("HEAD"),cancellable=true)
 private void unlock(CategoryConfig c,SkillConfig s,boolean force,CallbackInfoReturnable<Boolean> cir){
  if(c.id().equals(Utilities.CATEGORY)&&!Utilities.allowed(((CategoryData)(Object)this).getUnlockedSkillIds(),s.id()))cir.setReturnValue(false);
 }
}
