package net.spell_engine.client.gui;
import java.util.*;import java.util.stream.Stream;
import net.minecraft.client.Minecraft;import net.minecraft.network.chat.*;import net.minecraft.world.effect.MobEffectInstance;
import net.spell_engine.internals.EffectDetails;
/** Only loaded from Effect Insights' client tooltip extractor. Native attribute rows are untouched. */
public final class EffectClientDetails {
 public static Stream<Component> attributes(Stream<Component> original,Object extractor,MobEffectInstance effect){
  String id=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
  if(extractor.getClass().getName().endsWith("MobEffectTooltipLines$5")&&Set.of("spell_engine:stun","paladins:judgement","rogues:shock","rogues:net_trap","rogues:bear_trap","skill_tree_rpgs:natures_grasp","wizards:frozen").contains(id))return Stream.empty();
  return original;
 }
 public static Stream<Component> describe(Object value,int width,Stream<Component> fallback){
  if(!(value instanceof MobEffectInstance effect))return fallback;
  String id=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
  var lines=EffectDetails.clientLines(Minecraft.getInstance().player,id);if(lines.isEmpty())return fallback;
  return lines.stream().flatMap(line->Minecraft.getInstance().font.getSplitter().splitLines(Component.literal(line),width,Style.EMPTY).stream()).map(line->(Component)Component.literal(line.getString()));
 }
}
