package net.spell_engine.mixin.client;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.client.gui.SpellTooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Presentation-only activation labels; never invent gameplay trigger events. */
@Mixin(SpellTooltip.class)
public class SkillTooltipMixin {
 @Inject(method="addSpellDetails",at=@At("TAIL"))
 private static void skillActivation(Holder<Spell> spell,Player player,ItemStack stack,int indent,ArrayList<Component> lines,CallbackInfo ci){
  String id=spell.unwrapKey().orElseThrow().identifier().toString();
  String key="spell.tooltip.activation."+id.replace(':','.');
  if(I18n.get(key).equals(key))return;
  String base=I18n.get("spell.tooltip.trigger.base");int token=base.indexOf("{trigger_list}");
  if(token<0)return;
  String prefix=base.substring(0,token);int insert=-1;
  for(int i=lines.size()-1;i>=0;i--){
   var line=lines.get(i);if(line.getStyle().getColor()==null||line.getStyle().getColor().getValue()!=0xFFAA00)continue;
   insert=i;
   if(line.getString().strip().startsWith(prefix.strip()))lines.remove(i);
  }
  if(insert<0)insert=lines.size();
  lines.add(Math.min(insert,lines.size()),Component.literal(" ".repeat(Math.max(0,indent))+I18n.get(key)).withStyle(ChatFormatting.GOLD));
 }
}
