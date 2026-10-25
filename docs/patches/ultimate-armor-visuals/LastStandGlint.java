package net.spell_engine.client.render.tint;
import java.util.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.spell_engine.api.effect.Synchronized;

/** State travels with queued model submissions, not a global player/item flag. */
public final class LastStandGlint {
 static {net.spell_engine.api.effect.CustomParticleStatusEffect.register(net.rogues.effect.RogueEffects.LAST_STAND.effect,null);}
 private static final Map<Object,Boolean> states=new WeakHashMap<>();
 private static final RenderType GOLD=RenderType.create("last_stand_gold_glint",RenderSetup.builder(RenderPipelines.GLINT)
  .withTexture("Sampler0",Identifier.parse("spell_engine:textures/misc/last_stand_glint.png"))
  .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
  .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
 public static RenderType renderType(){return GOLD;}
 public static void capture(LivingEntity entity,LivingEntityRenderState state){
  if(entity.isAlive()&&Synchronized.effectsOf(entity).stream().anyMatch(e->e.effect()==net.rogues.effect.RogueEffects.LAST_STAND.effect))states.put(state,true);
  else states.remove(state);
 }
 public static boolean active(Object state){return Boolean.TRUE.equals(states.get(state));}
 public static boolean nativeGlint(RenderType type){return type==RenderTypes.armorEntityGlint()||type==RenderTypes.entityGlint()||type==RenderTypes.glint();}
}
