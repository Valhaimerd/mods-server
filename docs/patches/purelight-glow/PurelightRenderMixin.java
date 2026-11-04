package net.spell_engine.mixin.client.render.purelight;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.model.Model;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.rendertype.RenderType;import net.minecraft.client.renderer.texture.TextureAtlasSprite;import net.minecraft.client.renderer.feature.ModelFeatureRenderer;import net.spell_engine.client.render.tint.*;
import org.spongepowered.asm.mixin.Mixin;import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(SubmitNodeCollection.class) public class PurelightRenderMixin {
 @WrapMethod(method="submitModel") private <S> void purelight(Model<? super S> model,S state,PoseStack poses,RenderType type,int light,int overlay,int color,TextureAtlasSprite sprite,int outline,ModelFeatureRenderer.CrumblingOverlay crumble,Operation<Void> original){
  if(!PurelightVisual.active(state)){original.call(model,state,poses,type,light,overlay,color,sprite,outline,crumble);return;}
  if(LastStandGlint.nativeGlint(type))return;
  if(type.toString().toLowerCase(java.util.Locale.ROOT).contains("armor"))PurelightVisual.armorSubmissions++;else PurelightVisual.bodySubmissions++;
  original.call(model,state,poses,PurelightVisual.light(),net.minecraft.util.LightCoordsUtil.FULL_BRIGHT,overlay,0xFFFFEAB8,null,outline,crumble);
  original.call(model,state,poses,PurelightVisual.gold(),net.minecraft.util.LightCoordsUtil.FULL_BRIGHT,overlay,-1,null,outline,null);
 }
}
