package net.spell_engine.client.render.priest;
import java.util.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.PriestPassives;
public final class FadeRender {
 private static final Map<LivingEntityRenderState,Boolean> hidden=new WeakHashMap<>();
 public static void capture(LivingEntity entity,LivingEntityRenderState state){hidden.put(state,PriestPassives.faded(entity));}
 public static boolean hidden(LivingEntityRenderState state){return hidden.getOrDefault(state,false);}
}
