package local.paladinprobe;
import java.nio.file.*;import java.util.*;import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.event.server.ServerStartedEvent;import net.neoforged.neoforge.common.util.FakePlayerFactory;import net.neoforged.neoforge.event.tick.PlayerTickEvent;import net.minecraft.world.entity.ai.attributes.Attributes;import com.eiden.iourusraces.race.*;
@Mod("paladin_doctrine_probe") public class NodeProbe {
 List<String> checks=new ArrayList<>();int fail;void near(double a,double b,String l){boolean ok=Math.abs(a-b)<.0001;checks.add((ok?"PASS ":"FAIL ")+l+" actual="+a+" expected="+b);if(!ok)fail++;}
 public NodeProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent e){var s=e.getServer();try{var p=FakePlayerFactory.getMinecraft(s.overworld());p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.connection.markClientLoaded();p.setInvulnerable(false);p.getAbilities().invulnerable=false;p.getAbilities().flying=false;
 PlayerRaceData.setRace(p,Race.HUMAN);double base=p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);PlayerRaceData.setRace(p,Race.ELF);
 near(p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),base+5,"Elf native safe fall +5");
 for(int i=0;i<10;i++)RaceBalanceRules.apply(p,Race.ELF);near(p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),base+5,"repeated application does not stack");
 p.setOnGround(false);p.setDeltaMovement(.3,-.8,.4);p.fallDistance=12;NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(p));near(p.getDeltaMovement().y,-.8,"normal falling speed");near(p.fallDistance,12,"fall distance not erased");
 p.setHealth(p.getMaxHealth());p.invulnerableTime=0;float hp=p.getHealth();p.causeFallDamage(8,1,p.damageSources().fall());near(p.getHealth(),hp,"8 block fall is safe");
 p.invulnerableTime=0;hp=p.getHealth();p.causeFallDamage(12,1,p.damageSources().fall());near(hp-p.getHealth(),4,"12 block fall still deals 4 HP");
 PlayerRaceData.setRace(p,Race.HUMAN);near(p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),base,"switch to Human removes bonus");
 PlayerRaceData.setRace(p,Race.ELF);RaceAttributes.removeAttributes(p);near(p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),base,"explicit race cleanup removes bonus");
 }catch(Throwable t){fail++;checks.add("ERROR "+t);t.printStackTrace();}finally{try{Files.write(Path.of("paladin-checks.txt"),checks);Files.writeString(Path.of("paladin-result.txt"),(fail==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+fail);}catch(Exception x){throw new RuntimeException(x);}s.halt(false);}}
}