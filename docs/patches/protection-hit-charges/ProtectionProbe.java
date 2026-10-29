package local.protectionprobe;
import java.nio.file.*;import java.util.*;
import net.minecraft.resources.Identifier;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;import net.minecraft.server.level.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.common.util.FakePlayerFactory;import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_engine.internals.*;import net.spell_power.api.SpellSchools;
@Mod("protection_probe") public class ProtectionProbe {
 final List<String> checks=new ArrayList<>();int failed;ServerLevel world;ServerPlayer p;LivingEntity enemy;
 void check(boolean ok,String text){checks.add((ok?"PASS ":"FAIL ")+text);if(!ok)failed++;}
 void near(double a,double b,String text){check(Math.abs(a-b)<.02,text+" actual="+a+" expected="+b);}
 void reset(double m,double power){p.removeAllEffects();PaladinRevamp.clear(p);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);p.setHealth(200);p.getAttribute(Attributes.ARMOR).setBaseValue(0);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(m);p.getAttribute(SpellSchools.HEALING.attributeEntry).setBaseValue(power);p.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);p.setAbsorptionAmount(0);p.invulnerableTime=0;}
 void cast(){PaladinRevamp.protect(world,net.spell_engine.api.spell.registry.SpellRegistry.from(world).get(Identifier.parse("paladins:divine_protection")).orElseThrow(),p,List.of(),new SpellExecution.ImpactContext(),null);}
 void hit(boolean magic){p.invulnerableTime=0;p.hurtServer(world,magic?world.damageSources().indirectMagic(enemy,enemy):world.damageSources().mobAttack(enemy),10);}
 int blocks(){var r=EffectDetails.data(p).getAsJsonObject("support").getAsJsonObject("protection");return r.has("blocks")?r.get("blocks").getAsInt():-1;}
 public ProtectionProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent event){var server=event.getServer();try{
  world=server.overworld();server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);world.getChunk(0,0);p=FakePlayerFactory.getMinecraft(world);p.setPos(0,100,0);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.connection.markClientLoaded();p.getAbilities().invulnerable=false;p.setInvulnerable(false);enemy=EntityTypes.HUSK.create(world,EntitySpawnReason.COMMAND);enemy.setPos(0,100,2);world.addFreshEntity(enemy);
  reset(17,10);cast();near(blocks(),2,"M17 grants two attack blocks");p.setAbsorptionAmount(5);hit(false);near(p.getHealth(),200,"first melee hit blocked");near(p.getAbsorptionAmount(),5,"blocked melee preserves absorption");near(blocks(),1,"one charge consumed");hit(true);near(p.getHealth(),200,"second magic hit blocked");near(p.getAbsorptionAmount(),5,"blocked magic preserves absorption");near(blocks(),0,"magic consumes final charge");p.setAbsorptionAmount(0);hit(false);near(p.getHealth(),192.5,"percentage mitigation persists after charges spent");near(blocks(),0,"spent charges remain zero");
  reset(0,0);cast();near(blocks(),1,"zero M grants one block");hit(false);hit(false);near(p.getHealth(),190.5,"single block expires after one attack");
  reset(40,20);cast();near(blocks(),5,"M40 grants five attack blocks");near(EffectDetails.data(p).getAsJsonObject("support").getAsJsonObject("protection").get("reduction").getAsDouble(),.4,"reduction cap is 40 percent");
  reset(17,10);cast();p.invulnerableTime=0;p.hurtServer(world,world.damageSources().fall(),10);near(blocks(),2,"environmental damage consumes no attack block");near(p.getHealth(),192.5,"environmental hit uses percentage reduction only");
  reset(17,10);cast();hit(false);cast();near(blocks(),2,"new cast replenishes attack budget");p.removeEffect(PaladinRevamp.effect(PaladinRevamp.DP));hit(false);check(p.getHealth()<200,"dispelled protection cannot block attacks");
  reset(17,10);cast();var arrow=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);p.hurtServer(world,world.damageSources().arrow(arrow,enemy),10);near(p.getHealth(),200,"attributed projectile blocked");near(blocks(),1,"projectile spends one charge");
  var details=PaladinRevamp.details(p,PaladinRevamp.DP);check(details.stream().anyMatch(s->s.equals("Attacks blocked: 1 remaining")),"status shows remaining attack count");check(details.stream().noneMatch(s->s.contains("additional damage per hit")),"flat HP wording removed from details");
  reset(17,10);cast();p.hurtServer(world,world.damageSources().mobAttack(p),10);near(blocks(),2,"self damage does not consume charge");
  reset(17,10);cast();var root=EffectDetails.data(p);var protection=root.getAsJsonObject("support").getAsJsonObject("protection");protection.remove("blocks");protection.addProperty("flat",2);EffectDetails.save(p,root);hit(false);near(blocks(),1,"existing saved flat value migrates to finite charges");near(p.getHealth(),200,"migrated first charge blocks attack");
  reset(17,10);cast();root=EffectDetails.data(p);root.getAsJsonObject("support").getAsJsonObject("protection").addProperty("expires",world.getGameTime()-1);EffectDetails.save(p,root);hit(false);near(blocks(),2,"expired state spends no charges before cleanup tick");check(p.getHealth()<200,"expired state grants no attack immunity");
  reset(17,10);cast();PaladinRevamp.clear(p);hit(false);check(p.getHealth()<200,"cleanup removes attack immunity");
 }catch(Throwable t){failed++;checks.add("ERROR "+t);t.printStackTrace();}finally{try{Files.write(Path.of("checks.txt"),checks);Files.writeString(Path.of("result.txt"),(failed==0?"PASS ":"FAIL ")+failed+" failures / "+checks.size()+" checks");}catch(Exception ex){throw new RuntimeException(ex);}server.halt(false);}}
}
