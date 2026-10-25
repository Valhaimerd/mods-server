package io.redspace.pvp_flagging.client;
import io.redspace.pvp_flagging.CombatStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
@EventBusSubscriber(modid="pvp_flagging",value=Dist.CLIENT)
public final class CombatHud {
 public static final int SIZE=22;
 private static long combat,breaking,placing;
 private static Object connection;
 private static final Identifier[] ICONS={icon("combat"),icon("breaking"),icon("placing")};
 private static Identifier icon(String n){return Identifier.parse("pvp_flagging:textures/gui/"+n+".png");}
 @SubscribeEvent public static void register(RegisterClientPayloadHandlersEvent e){e.register(CombatStatus.TYPE,(p,c)->{long now=System.nanoTime()/1000000;combat=now+Math.clamp(p.combatMs(),0,40000);breaking=now+Math.clamp(p.breakMs(),0,30000);placing=now+Math.clamp(p.placeMs(),0,30000);connection=Minecraft.getInstance().getConnection();});}
 @SubscribeEvent public static void layers(RegisterGuiLayersEvent e){e.registerAboveAll(Identifier.parse("pvp_flagging:combat"),(g,d)->render(g));}
 public static void render(GuiGraphicsExtractor g){
  var mc=Minecraft.getInstance();long now=System.nanoTime()/1000000;
  if(mc.player==null||mc.getConnection()!=connection||mc.gui.hud.isHidden()||combat<=now)return;
  int y=g.guiHeight()-SIZE;long[] ends={combat,breaking,placing};
  for(int i=0;i<3;i++){
   int x=i*SIZE;g.fill(x,y,x+SIZE,y+SIZE,0xFF242424);
   g.blit(RenderPipelines.GUI_TEXTURED,ICONS[i],x,y,0,0,SIZE,SIZE,SIZE,SIZE,SIZE,SIZE);
   g.fill(x,y,x+SIZE,y+1,0xFF808080);g.fill(x,y,x+1,y+SIZE,0xFF808080);g.fill(x,y+SIZE-1,x+SIZE,y+SIZE,0xFF000000);g.fill(x+SIZE-1,y,x+SIZE,y+SIZE,0xFF000000);
   String text=Long.toString(Math.max(0,(ends[i]-now+999)/1000));int tx=x+(SIZE-mc.font.width(text))/2,ty=y+(SIZE-8)/2;
   g.text(mc.font,text,tx-1,ty,0xFF000000,false);g.text(mc.font,text,tx+1,ty,0xFF000000,false);g.text(mc.font,text,tx,ty-1,0xFF000000,false);g.text(mc.font,text,tx,ty+1,0xFF000000,false);g.text(mc.font,text,tx,ty,0xFFFFFFFF,false);
  }
 }
}
