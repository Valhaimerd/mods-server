package net.spell_engine.mixin.client.render.tint;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.spell_engine.client.render.tint.LastStandGlint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SubmitNodeCollection.class)
public class UltimateGlintMixin {
 @Inject(method="submitModel",at=@At("HEAD"),cancellable=true)
 private <S> void lastStandGlint(Model<? super S> model,S state,PoseStack poses,RenderType type,int light,int overlay,int color,TextureAtlasSprite sprite,int outline,ModelFeatureRenderer.CrumblingOverlay crumble,CallbackInfo ci){
  if(LastStandGlint.active(state)&&LastStandGlint.nativeGlint(type))ci.cancel();
 }
 @Inject(method="submitModel",at=@At("RETURN"))
 private <S> void addLastStandGlint(Model<? super S> model,S state,PoseStack poses,RenderType type,int light,int overlay,int color,TextureAtlasSprite sprite,int outline,ModelFeatureRenderer.CrumblingOverlay crumble,CallbackInfo ci){
  if(!LastStandGlint.active(state)||type==LastStandGlint.renderType()||LastStandGlint.nativeGlint(type))return;
  ((OrderedSubmitNodeCollector)this).submitModel(model,state,poses,LastStandGlint.renderType(),light,overlay,-1,null,outline,null);
 }
}
