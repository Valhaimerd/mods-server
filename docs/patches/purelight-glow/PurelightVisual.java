package net.spell_engine.client.render.tint;
import java.util.*;import net.minecraft.world.entity.LivingEntity;import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;import net.minecraft.client.renderer.rendertype.*;import net.minecraft.client.renderer.RenderPipelines;import net.minecraft.resources.Identifier;import net.spell_engine.api.effect.Synchronized;
public final class PurelightVisual {
 private static final Map<Object,Boolean> ACTIVE=new WeakHashMap<>();public static int bodySubmissions,armorSubmissions;
 private static final RenderType LIGHT=RenderType.create("purelight_solid",RenderSetup.builder(RenderPipelines.EYES.toBuilder().withLocation(Identifier.parse("spell_engine:pipeline/purelight_solid")).withDepthStencilState(new com.mojang.blaze3d.pipeline.DepthStencilState(com.mojang.blaze3d.platform.CompareOp.GREATER_THAN_OR_EQUAL,true)).build()).withTexture("Sampler0",Identifier.parse("spell_engine:textures/misc/purelight_white.png")).createRenderSetup());
 private static final RenderType GOLD=RenderType.create("purelight_gold_glint",RenderSetup.builder(RenderPipelines.GLINT).withTexture("Sampler0",Identifier.parse("spell_engine:textures/misc/purelight_gold.png")).setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
 public static void capture(LivingEntity e,LivingEntityRenderState state){if(e.isAlive()&&Synchronized.effectsOf(e).stream().anyMatch(fx->net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(fx.effect()).toString().equals("skill_tree_rpgs:purelight_cost")))ACTIVE.put(state,true);else ACTIVE.remove(state);}
 public static boolean active(Object state){return Boolean.TRUE.equals(ACTIVE.get(state));}
 public static RenderType light(){return LIGHT;}public static RenderType gold(){return GOLD;}
}
