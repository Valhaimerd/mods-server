package local.remote;
import java.util.*;import java.nio.file.*;import com.google.gson.*;
import net.minecraft.client.Minecraft;import net.minecraft.world.InteractionHand;import net.minecraft.world.item.CrossbowItem;
import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.client.event.ClientTickEvent;
@Mod(value="remote_spatial_client",dist=Dist.CLIENT) public class FrenzyClientProbe {
 final Path dir=Path.of("../exchange");final List<String> lines=new ArrayList<>();final List<Runnable> tasks=new ArrayList<>();String pending;JsonObject response;int tick,at,index,waitUntil,spam,hold;boolean done;InteractionHand hand=InteractionHand.MAIN_HAND;
 void check(boolean ok,String s){lines.add((ok?"PASS ":"FAIL ")+s);}
 void request(String op,Object...pairs){try{var q=new JsonObject();q.addProperty("op",op);for(int i=0;i<pairs.length;i+=2)q.addProperty(pairs[i].toString(),pairs[i+1].toString());pending=UUID.randomUUID().toString();q.addProperty("token",pending);Files.writeString(dir.resolve("request-"+pending+".tmp"),q.toString());Files.move(dir.resolve("request-"+pending+".tmp"),dir.resolve("request-"+pending+".json"),StandardCopyOption.ATOMIC_MOVE);at=tick;}catch(Exception e){throw new RuntimeException(e);}}
 void delay(int n){waitUntil=tick+n;}
 void mode(String mode){tasks.add(()->request("setup","label",mode+"-rapid","mode",mode));tasks.add(()->delay(30));tasks.add(()->{spam=130;});tasks.add(()->delay(10));tasks.add(()->request("facts"));tasks.add(()->{lines.add("METRIC "+mode+" after spam "+response);check(response.get("empty").getAsBoolean(),mode+" exhausted after rapid firing");check(response.get("shots").getAsInt()==26,mode+" exactly 26 accepted shots");});tasks.add(()->{var mc=Minecraft.getInstance();mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,hand);hold=40;});tasks.add(()->request("facts"));tasks.add(()->{lines.add("METRIC "+mode+" after crank "+response);check(!response.get("empty").getAsBoolean()&&response.get("loaded").getAsBoolean(),mode+" empty magazine reloads after full crank");});}
 public FrenzyClientProbe(){NeoForge.EVENT_BUS.addListener(this::run);tasks.add(()->request("init"));mode("survival");
  tasks.add(()->request("spare"));tasks.add(()->delay(20));tasks.add(()->{var mc=Minecraft.getInstance();mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,hand);hold=5;});tasks.add(()->request("facts"));tasks.add(()->check(response.get("empty").getAsBoolean()&&(response.get("loaded").getAsBoolean()||response.get("parked").getAsBoolean()),"Interrupted loaded recovery stays locked and preserves ammo"));tasks.add(()->{var mc=Minecraft.getInstance();mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,hand);hold=40;});tasks.add(()->request("facts"));tasks.add(()->{lines.add("METRIC loaded recovery "+response);check(!response.get("empty").getAsBoolean()&&response.get("loaded").getAsBoolean(),"Loaded exhausted crossbow recovers after real full crank");});
  mode("creative");tasks.add(()->request("finish"));tasks.add(this::finish);}
 void finish(){if(done)return;done=true;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("remote-checks.txt"),lines);Files.writeString(mc.gameDirectory.toPath().resolve("remote-result.txt"),lines.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS");}catch(Exception ignored){}mc.stop();}
 void run(ClientTickEvent.Post e){if(done)return;tick++;var mc=Minecraft.getInstance();try{
  if(mc.player==null||mc.level==null){if(tick>5000){lines.add("FAIL connection timeout");finish();}return;}mc.setScreenAndShow(null);
  if(pending!=null){if(tick-at>600)throw new AssertionError("Response timeout");var file=dir.resolve("response-"+pending+".json");if(!Files.exists(file))return;var r=JsonParser.parseString(Files.readString(file)).getAsJsonObject();if(r.has("error"))throw new AssertionError(r.get("error"));response=r;pending=null;}
  if(tick<waitUntil)return;
  if(spam>0){spam--;for(int i=0;i<4;i++){mc.gameMode.useItem(mc.player,hand);mc.gameMode.releaseUsingItem(mc.player);}return;}
  if(hold>0){hold--;if(hold==0){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}return;}
  if(index<tasks.size())tasks.get(index++).run();else finish();
 }catch(Throwable ex){ex.printStackTrace();lines.add("FAIL "+ex);finish();}}
}
