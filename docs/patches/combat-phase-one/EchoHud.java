package net.spell_engine.client.gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.cost.EchoCharges;
import net.minecraft.client.renderer.RenderPipelines;

/** Count stays visible while a remaining charge is usable; thin bar shows serial recharge. */
public final class EchoHud {
 public static void render(GuiGraphicsExtractor g,HudRenderHelper.SpellHotBarWidget.ViewModel vm){
  var mc=Minecraft.getInstance();if(mc.player==null||!EchoCharges.selected(mc.player))return;
  var rect=HudRenderHelper.SpellHotBarWidget.lastRendered;if(rect==null)return;
  var m=((SpellCaster.Player)mc.player).getCooldownManager();
  for(int i=0;i<vm.spells().size();i++){
   var icon=vm.spells().get(i).iconId();if(icon==null||!icon.toString().equals("wizards:textures/spell/arcane_explosion.png"))continue;
   int x=(int)rect.topLeft().x+3+20*i,y=(int)rect.topLeft().y+3;
   g.centeredText(mc.font,EchoCharges.available(m)+"/2",x+8,y+9,0xFFFFFFFF);
   float left=EchoCharges.recharge(m);if(left>0){g.fill(RenderPipelines.GUI,x,y+16,x+16,y+18,0xFF251236);g.fill(RenderPipelines.GUI,x,y+16,x+Math.round(16*(1-left)),y+18,0xFFDA96FF);}
  }
 }
}
