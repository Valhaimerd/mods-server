package local.audit;
import java.util.*;import java.nio.file.*;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.server.level.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.world.effect.MobEffectInstance;import net.spell_engine.internals.*;import net.spell_engine.internals.casting.*;
@Mod("phase_two_probe") public class TimingProbe {
 int count;List<String> lines=new ArrayList<>();ServerLevel w;
 public TimingProbe(){NeoForge.EVENT_BUS.addListener(this::start);}
 void eq(String name,double got,double expected){if(!Double.isFinite(got)||Math.abs(got-expected)>.0001)throw new AssertionError(name+" "+got+" != "+expected);count++;lines.add(name+" = "+got);}
 void ok(String name,boolean b){eq(name,b?1:0,1);}
 void start(ServerStartedEvent event){w=event.getServer().overworld();try{
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.get(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Timing"));p.setPos(0,80,0);w.addNewPlayer(p);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);p.setHealth(1000);p.getAbilities().instabuild=true;
  var spell=PaladinRevamp.spell(p,PaladinUltimates.PURGATORY);eq("Native cast duration",SpellParameters.getCastDuration(p,spell.value()),24);eq("Native channel deliveries",SpellParameters.channelTicks(p,spell),48);eq("Base cooldown preserved",SpellParameters.getCooldownDuration(p,spell),12);
  for(int pulse:new int[]{1,15,16,31,32,47,48}){int stage=pulse>=32?2:pulse>=16?1:0;eq("Damage stage "+pulse,PaladinUltimates.stage(pulse),stage);eq("Base pulse "+pulse,PaladinUltimates.pulseCoefficient(pulse,false),.1+.1*stage);eq("Damnation pulse "+pulse,PaladinUltimates.pulseCoefficient(pulse,true),.15+.1*stage);}
  for(double seconds:new double[]{16,18,20,22,24,30})for(boolean damnation:new boolean[]{false,true})eq("Explosion "+seconds+" damnation="+damnation,PaladinUltimates.explosionCoefficient(seconds,damnation),Math.clamp(2+(seconds-16)*(damnation?.875:.625),2,damnation?9:7));
  for(long time:new long[]{159,160,319,320,479,480})eq("Continuous pull stage "+time,PurgatoryPull.phase(time),time>=320?2:time>=160?1:0);
  for(int time:new int[]{159,160}){PaladinUltimates.startChannel(p,w.getGameTime()-time);var c=PaladinUltimates.channelOf(p);c.pulses=15;ok("Control protection at "+time,PaladinFollowup.focused(p)==(time>=160));p.addEffect(new MobEffectInstance(PaladinRevamp.effect("spell_engine:stun"),40));PaladinUltimates.advance(p,w.getGameTime());ok("Native stun boundary "+time,(PaladinUltimates.channelOf(p)!=null)==(time>=160));PaladinUltimates.end(p,PaladinUltimates.End.CLEANUP);p.removeAllEffects();}
  PaladinUltimates.startChannel(p,w.getGameTime()-480);var c=PaladinUltimates.channelOf(p);c.pulses=47;p.setHealth(1000);PaladinUltimates.advance(p,w.getGameTime());eq("Final delivery number",c.pulses,48);ok("Completes at24s",c.ended&&PaladinUltimates.channelOf(p)==null);eq("Final true HP cost unchanged",p.getHealth(),997);ok("Completion removes enclosure",w.getEntitiesOfClass(net.paladins.entity.BarrierEntity.class,p.getBoundingBox().inflate(20),PurgatoryBoundary::is).isEmpty());
  Files.writeString(Path.of("phase-two-result.txt"),"PASS "+count+" checks\n");
 }catch(Throwable e){try{Files.writeString(Path.of("phase-two-result.txt"),"FAIL "+e);}catch(Exception ignored){}e.printStackTrace();}finally{try{Files.write(Path.of("phase-two-checks.txt"),lines);}catch(Exception ignored){}w.getServer().halt(false);}}
}
