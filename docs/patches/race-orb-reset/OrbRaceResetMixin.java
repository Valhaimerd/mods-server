package com.eiden.iourusraces.mixin;
import com.eiden.iourusraces.race.*;
import net.minecraft.server.level.ServerPlayer;
import net.skill_tree_rpgs.utils.SkillHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/** Optional RPG integration: only the actual orb use, not arbitrary skill-reset callers. */
@Pseudo
@Mixin(targets="net.skill_tree_rpgs.items.RespecItem",remap=false)
public class OrbRaceResetMixin {
 @Redirect(method="use",at=@At(value="INVOKE",target="Lnet/skill_tree_rpgs/utils/SkillHelper;respec(Lnet/minecraft/server/level/ServerPlayer;)Z"),require=1)
 private boolean race$orbReset(ServerPlayer player){
  boolean skills=SkillHelper.respec(player);
  boolean race=PlayerRaceData.getRace(player)!=Race.NONE;
  if(race)PlayerRaceData.resetRace(player);
  return skills||race;
 }
}
