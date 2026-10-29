package net.rogues.client;

import java.lang.reflect.*;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.rogues.effect.RogueEffects;
import net.rogues.mixin.client.*;

/** Client submission snapshots only. The entity's gameplay invisibility is never changed. */
public final class VanishVisuals {
    private static final Map<RenderType, RenderType> TRANSLUCENT = new IdentityHashMap<>();
    private static final RenderType GLINT = RenderType.create("rogues_vanish_glint",
        RenderSetup.builder(RenderPipelines.ENERGY_SWIRL)
            .withTexture("Sampler0", ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR)
            .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
    public static boolean isGlint(RenderType type){return type==GLINT;}
    public static int glintColor(float alpha){
        // ENERGY_SWIRL is additive ONE/ONE: scale RGB, not just its ignored blend alpha.
        int intensity=(int)(255*alpha), a=Math.round(255*alpha);
        return (a<<24)|(intensity<<16)|(intensity<<8)|intensity;
    }
    public static float alpha(LivingEntity entity, float partialTick) {
        var effect = entity.getEffect(RogueEffects.STEALTH.entry);
        // Later reveal effects remain authoritative; Vanish only cleans marks at cast time.
        return effect == null || entity.hasEffect(MobEffects.GLOWING) || entity.isCurrentlyGlowing()
            ? -1 : cycle(140 - effect.getDuration() + partialTick);
    }
    public static float cycle(float elapsed) {
        float phase = ((elapsed % 80) + 80) % 80;
        return .1f * Math.abs(phase - 40) / 40;
    }
    public static int color(int original, float alpha) {
        // Violet tint follows the same opacity as the material, including exactly zero.
        int a = Math.round((original >>> 24) * alpha);
        int r = ((original >> 16) & 255) * 195 / 255;
        int g = ((original >> 8) & 255) * 150 / 255;
        return (a << 24) | (r << 16) | (g << 8) | (original & 255);
    }
    public static RenderType translucent(RenderType original) {
        return TRANSLUCENT.computeIfAbsent(original, type -> {
            var setup = ((VanishRenderTypeAccess)(Object)type).vanish$setup();
            var textures = ((VanishRenderSetupAccess)(Object)setup).vanish$textures();
            Object texture = textures.get("Sampler0");
            if (texture == null) return type;
            return RenderTypes.entityTranslucent(((VanishTextureAccess)texture).vanish$location());
        });
    }
    public static SubmitNodeCollector wrap(SubmitNodeCollector delegate, float alpha) {
        if (alpha < 0) return delegate;
        return (SubmitNodeCollector)proxy(delegate, alpha, SubmitNodeCollector.class);
    }
    private static Object proxy(OrderedSubmitNodeCollector delegate, float alpha, Class<?> contract) {
        return Proxy.newProxyInstance(VanishVisuals.class.getClassLoader(), new Class<?>[]{contract}, (self, method, args) -> {
            String name = method.getName();
            Object[] glintArgs = null;
            // Run interface overloads on the wrapper so model-part/armor calls reach submitModel.
            if (method.isDefault()) return InvocationHandler.invokeDefault(self, method, args);
            if (name.equals("order")) return proxy(((SubmitNodeCollector)delegate).order((int)args[0]), alpha, OrderedSubmitNodeCollector.class);
            if (name.equals("submitModel")) {
                if (alpha <= 0) return null;
                args[3] = translucent((RenderType)args[3]);
                args[6] = color((int)args[6], alpha);
                args[8] = 0;
                glintArgs=args.clone();glintArgs[3]=GLINT;glintArgs[6]=glintColor(alpha);glintArgs[7]=null;
            } else if (name.equals("submitItem")) {
                if (alpha <= 0) return null;
                var pose = (PoseStack)args[0];
                int light = (int)args[2], overlay = (int)args[3];
                int[] tints = (int[])args[5];
                @SuppressWarnings("unchecked") var quads = (List<BakedQuad>)args[6];
                for (var quad : quads) {
                    var material = quad.materialInfo();
                    int tint = material.tintIndex() >= 0 && material.tintIndex() < tints.length ? tints[material.tintIndex()] : -1;
                    int shaded = color(tint, alpha);
                    delegate.submitCustomGeometry(pose, translucent(material.itemRenderType()), (matrix, vertices) -> {
                        var instance = new QuadInstance();
                        instance.setLightCoords(light); instance.setOverlayCoords(overlay); instance.setColor(shaded);
                        vertices.putBakedQuad(matrix, quad, instance);
                    });
                }
                delegate.submitCustomGeometry(pose, GLINT, (matrix, vertices) -> {
                    var instance=new QuadInstance();instance.setLightCoords(light);instance.setOverlayCoords(overlay);instance.setColor(glintColor(alpha));
                    for(var quad:quads)vertices.putBakedQuad(matrix,quad,instance);
                });
                return null;
            } else if (name.equals("submitCustomGeometry")) {
                if (alpha <= 0) return null;
                args[1] = translucent((RenderType)args[1]);
                var geometry = (SubmitNodeCollector.CustomGeometryRenderer)args[2];
                args[2] = (SubmitNodeCollector.CustomGeometryRenderer)(matrix, vertices) -> geometry.render(matrix, new FadedVertices(vertices, alpha));
            } else if (name.equals("submitShadow") || name.equals("submitNameTag") || name.equals("submitFlame")) {
                return null;
            }
            try {var result=method.invoke(delegate,args);if(glintArgs!=null)method.invoke(delegate,glintArgs);return result;}
            catch (InvocationTargetException error) { throw error.getCause(); }
        });
    }
    private record FadedVertices(VertexConsumer target, float alpha) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){target.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return setColor((a<<24)|(r<<16)|(g<<8)|b);}
        public VertexConsumer setColor(int rgba){target.setColor(color(rgba,alpha));return this;}
        public VertexConsumer setUv(float u,float v){target.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){target.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){target.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){target.setNormal(x,y,z);return this;}
        public VertexConsumer setLineWidth(float width){target.setLineWidth(width);return this;}
    }
}
