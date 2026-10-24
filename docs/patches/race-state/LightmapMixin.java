package com.eiden.iourusraces.mixin;
import com.eiden.iourusraces.race.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Lightmap.class)
public abstract class LightmapMixin {
 @ModifyArg(method="render",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/buffers/Std140Builder;putFloat(F)Lcom/mojang/blaze3d/buffers/Std140Builder;",ordinal=2),index=0)
 private float raceVision(float original){
  var p=Minecraft.getInstance().player;
  return p!=null&&RaceVisualRules.nightVision(ClientRaceData.getRace(),p.isInWater())?1:original;
 }
}
