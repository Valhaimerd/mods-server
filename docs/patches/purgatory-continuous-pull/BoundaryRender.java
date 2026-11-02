package net.paladins.client.entity;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.data.AtlasIds;
import net.paladins.entity.BarrierEntity;
import net.spell_engine.internals.PurgatoryBoundary;
/** Priest Barrier texture/layer, shaped as an orange cylinder extending five blocks above and below the caster. */
@net.neoforged.fml.common.EventBusSubscriber(modid="paladins",value=net.neoforged.api.distmarker.Dist.CLIENT) public final class BoundaryRender {
 public static int submissions,draws;
 @net.neoforged.bus.api.SubscribeEvent public static void submit(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event){
  var mc=Minecraft.getInstance();if(mc.level==null)return;var camera=mc.gameRenderer.mainCamera().position();var stack=event.getPoseStack();
  var layer=net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(net.minecraft.resources.Identifier.parse("paladins:textures/spell_effect/purgatory_boundary.png"));
  for(var b:PurgatoryBoundary.active(mc.level)){if(b.isRemoved())continue;stack.pushPose();stack.translate(b.getX()-camera.x,b.getY()+1-camera.y,b.getZ()-camera.z);event.getSubmitNodeCollector().submitCustomGeometry(stack,layer,(pose,vertices)->draw(b,pose,vertices,0xF000F0,mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)));stack.popPose();submissions++;}
 }
 public static boolean render(BarrierEntity entity,PoseStack.Pose pose,VertexConsumer vertices,int light,float partial){
  return PurgatoryBoundary.is(entity);
 }
 private static void draw(BarrierEntity entity,PoseStack.Pose pose,VertexConsumer vertices,int light,float partial){
  draws++;
  double r=PurgatoryBoundary.radius(entity);
  float alpha=.8f+.1f*(float)Math.sin((entity.tickCount+partial)*.15);
  for(int i=0;i<64;i++){
   double a=2*Math.PI*i/64,b=2*Math.PI*(i+1)/64;float x=(float)(Math.cos(a)*r),z=(float)(Math.sin(a)*r),xx=(float)(Math.cos(b)*r),zz=(float)(Math.sin(b)*r);
   vertex(vertices,pose,x,-6,z,0,1,light,alpha);vertex(vertices,pose,xx,-6,zz,1,1,light,alpha);vertex(vertices,pose,xx,4,zz,1,0,light,alpha);vertex(vertices,pose,x,4,z,0,0,light,alpha);
  }
 }
 private static void vertex(VertexConsumer v,PoseStack.Pose p,float x,float y,float z,float u,float w,int light,float alpha){v.addVertex(p.pose(),x,y,z).setColor(1,1,1,alpha).setUv(u,w).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p,0,1,0);}
}
