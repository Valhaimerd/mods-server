package local.performance;
import java.nio.file.*;import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.*;import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;import net.minecraft.world.level.storage.*;
import net.spell_engine.entity.*;import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.registry.SpellRegistry;
@Mod("rpg_performance_probe") public final class PerformanceProbe {
 final List<String> out=new ArrayList<>();int failures;
 void check(String name,boolean ok){out.add((ok?"PASS ":"FAIL ")+name);if(!ok)failures++;}
 public PerformanceProbe(){NeoForge.EVENT_BUS.addListener((ServerStartedEvent e)->e.getServer().execute(()->run(e.getServer())));}
 void run(net.minecraft.server.MinecraftServer server){try {
  var level=server.overworld();level.getChunk(0,0);
  var wall=SpellRegistry.from(level).get(Identifier.parse("wizards:fire_wall")).orElseThrow();
  var cloud=new SpellCloud(level);cloud.onCreatedFromSpell(Identifier.parse("wizards:fire_wall"),wall.value().deliver.clouds.getFirst(),new net.spell_engine.internals.SpellExecution.ImpactContext().power(new net.spell_power.api.SpellPower.Result(wall.value().school,10,0,1)),1,new Spell.Modifier.Cloud());
  var field=SpellCloud.class.getDeclaredField("spellId");field.setAccessible(true);field.set(cloud,Identifier.parse("rpg_test:removed_spell"));
  cloud.tickCount=9999;cloud.tick();check("invalid expired cloud is discarded",cloud.isRemoved());
  // A valid cloud keeps its original expiry and persists its existing native age.
  var valid=new SpellCloud(level);valid.onCreatedFromSpell(Identifier.parse("wizards:fire_wall"),wall.value().deliver.clouds.getFirst(),new net.spell_engine.internals.SpellExecution.ImpactContext().power(new net.spell_power.api.SpellPower.Result(wall.value().school,10,0,1)),1,new Spell.Modifier.Cloud());
  valid.tick();check("valid unexpired cloud remains",!valid.isRemoved());valid.tickCount=9999;valid.tick();check("valid expired cloud removed",valid.isRemoved());
  var brokenIndex=new SpellCloud(level);brokenIndex.onCreatedFromSpell(Identifier.parse("wizards:fire_wall"),wall.value().deliver.clouds.getFirst(),new net.spell_engine.internals.SpellExecution.ImpactContext().power(new net.spell_power.api.SpellPower.Result(wall.value().school,10,0,1)),1,new Spell.Modifier.Cloud());
  var index=SpellCloud.class.getDeclaredField("dataIndex");index.setAccessible(true);index.setInt(brokenIndex,999);brokenIndex.tick();check("removed cloud variant discarded without tick exception",brokenIndex.isRemoved());
  for(String type:List.of("wizards:fire_hydra","wizards:frost_elemental","archers:spirit_wolf")) {
   var entityType=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(type));
   if(entityType==null){out.add("SKIP unregistered "+type);continue;}
   var summon=(SummonedEntity)entityType.create(level,EntitySpawnReason.COMMAND);summon.tickCount=321;
   var ttl=SummonedEntity.class.getDeclaredField("timeToLive");ttl.setAccessible(true);ttl.setInt(summon,322);
   var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());summon.addAdditionalSaveData(saved);
   var restored=(SummonedEntity)entityType.create(level,EntitySpawnReason.COMMAND);
   restored.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
   check(type+" age survives native save/load",restored.tickCount==321);
   restored.tickCount++;restored.tick();check(type+" expires after reload without refreshed lifetime",restored.isRemoved());
   saved.buildResult().remove("rpg_pack_age");
   var legacy=(SummonedEntity)entityType.create(level,EntitySpawnReason.COMMAND);var old=saved.buildResult().copy();old.remove("rpg_pack_age");legacy.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),old));check(type+" legacy save accepted",legacy.tickCount==0);

  }
  var projectile=new SpellProjectile(SpellProjectile.ENTITY_TYPE,level);projectile.setSpell(wall);projectile.tickCount=1199;projectile.range=64;projectile.moveDist=63;
  var savedProjectile=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());projectile.addAdditionalSaveData(savedProjectile);
  var reloadedProjectile=new SpellProjectile(SpellProjectile.ENTITY_TYPE,level);reloadedProjectile.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),savedProjectile.buildResult()));
  check("projectile age survives reload",reloadedProjectile.tickCount==1199);check("projectile range survives reload",reloadedProjectile.range==64);check("projectile distance survives reload",reloadedProjectile.moveDist==63);
  reloadedProjectile.moveDist=64;reloadedProjectile.tick();check("projectile expires at restored range",reloadedProjectile.isRemoved());
 }catch(Throwable t){t.printStackTrace();failures++;out.add("FAIL "+t);}
 try{Files.write(Path.of("performance-measurements.txt"),out);Files.writeString(Path.of("performance-result.txt"),(failures==0?"PASS ":"FAIL ")+out.size()+" RPG lifecycle checks; failures="+failures);}catch(Exception ex){throw new RuntimeException(ex);}server.halt(false);}
}
