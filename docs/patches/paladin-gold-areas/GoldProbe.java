package local.paladinprobe;
import java.nio.file.*;import java.util.*;import com.google.gson.*;
import net.minecraft.core.Holder;import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.*;import net.minecraft.world.effect.*;import net.minecraft.world.item.*;import net.minecraft.server.level.*;
import net.neoforged.fml.common.Mod;import net.neoforged.neoforge.common.NeoForge;import net.neoforged.neoforge.common.util.FakePlayerFactory;import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_engine.api.spell.Spell;import net.spell_engine.internals.*;import net.spell_engine.internals.container.SpellContainerSource;import net.spell_engine.internals.casting.SpellCasterEntity;import net.spell_power.api.*;
@Mod("paladin_doctrine_probe") public class GoldProbe {
 public static final List<net.spell_engine.network.Packets.ParticleEffects.Spawn> visuals=new ArrayList<>();
 public static void capture(Entity e,List<net.spell_engine.api.spell.fx.ParticleGroup> groups){for(var g:groups)visuals.add(new net.spell_engine.network.Packets.ParticleEffects.Spawn(e.getId(),0,0,e.position(),g));}
 final List<String> checks=new ArrayList<>();final List<net.spell_engine.entity.SpellProjectile> joined=new ArrayList<>();int failures;ServerPlayer p;ServerLevel world;
 void check(boolean b,String s){checks.add((b?"PASS ":"FAIL ")+s);if(!b)failures++;}void near(double a,double b,String s){check(Math.abs(a-b)<(Math.abs(b)<2?.0001:.015),s+" actual="+a+" expected="+b);}
 Holder<Spell> spell(String id){return net.spell_engine.api.spell.registry.SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 void equip(String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(ids));owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),Arrays.stream(ids).map(this::spell).filter(h->h.value().type==Spell.Type.PASSIVE).toList(),Arrays.stream(ids).map(this::spell).filter(h->h.value().type==Spell.Type.MODIFIER).toList(),List.of(new SpellContainerSource.SourcedContainer("revamp_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();net.skill_tree_rpgs.compat.PaladinPassives.refresh(p);}
 void reset(){p.getAttribute(SpellPowerMechanics.CRITICAL_CHANCE.attributeEntry).setBaseValue(0);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);for(String id:List.of("skill_tree_rpgs:paladin_tier_1_passive_1","skill_tree_rpgs:paladin_tier_1_passive_2","skill_tree_rpgs:paladin_tier_2_passive_1","skill_tree_rpgs:paladin_tier_2_passive_2"))((SpellCasterEntity)p).getCooldownManager().setDurationLeft(spell(id),0);p.removeAllEffects();PaladinRevamp.clear(p);equip();p.getAttribute(Attributes.ARMOR).setBaseValue(20);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(40);p.getAttribute(SpellSchools.HEALING.attributeEntry).setBaseValue(20);p.setAbsorptionAmount(0);p.setHealth(p.getMaxHealth());p.invulnerableTime=0;p.getAbilities().invulnerable=false;p.setInvulnerable(false);}
 net.minecraft.world.entity.monster.zombie.Husk mob(){var mob=EntityTypes.HUSK.create(world,EntitySpawnReason.COMMAND);mob.setPos(0,100,3);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setHealth(1000);mob.setNoAi(true);mob.setNoGravity(true);world.addFreshEntity(mob);return mob;}
 void protection(){PaladinRevamp.protect(world,spell("paladins:divine_protection"),p,List.of(),new SpellExecution.ImpactContext(),null);}
 JsonObject state(String key){return EffectDetails.data(p).getAsJsonObject("support").getAsJsonObject(key);}
 double damage(){return p.getAttributeValue(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_engine:damage_taken")).orElseThrow());}

 public GoldProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 long gold(){return visuals.stream().filter(s->s.effect().id.equals("spell_engine:area_circle_1")).count();}
 void area(double radius,boolean attached){
  var g=visuals.stream().filter(s->s.effect().id.equals("spell_engine:area_circle_1")).findFirst().orElseThrow().effect();near(g.appearance.scale,radius,"ground radius"+radius);check(Boolean.TRUE.equals(g.appearance.glow),"ground is emissive");check(g.batch.count==1&&g.batch.width_factor==0&&g.batch.extent==0,"zone centered without random scatter");check(g.appearance.attachment==(attached?net.spell_engine.api.spell.fx.ParticleGroup.Attachment.POSITION_HORIZONTAL:net.spell_engine.api.spell.fx.ParticleGroup.Attachment.NONE),"correct follow behavior");
 }
 void run(ServerStartedEvent event){var server=event.getServer();try{
  world=server.overworld();world.getChunk(0,0);p=FakePlayerFactory.getMinecraft(world);p.setPos(0,100,0);p.connection.markClientLoaded();reset();
  visuals.clear();protection();check(gold()==1,"Protection cast emits one gold ground area");area(8,false);check(p.hasEffect(PaladinRevamp.effect("paladins:divine_protection")),"Protection still applies its buff");
  reset();visuals.clear();PaladinUltimates.startBanner(p);check(gold()==1,"Banner starts with gold footprint");area(6,true);
  var banner=PaladinUltimates.bannerOf(p);visuals.clear();PaladinUltimates.bannerPulse(banner);check(gold()==1,"Banner pulses keep gold area visible");area(6,true);PaladinUltimates.cleanup(p);
  reset();equip("skill_tree_rpgs:paladin_tier_4_spell_1_root");visuals.clear();PaladinUltimates.startBanner(p);area(8,true);PaladinUltimates.cleanup(p);
  var enemy=mob();reset();equip("skill_tree_rpgs:paladin_tier_2_spell_1_modifier_1");visuals.clear();
  PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(enemy,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);
  check(gold()==1,"successful Divine Strength heal emits area");area(4,false);
  visuals.clear();PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(),new SpellExecution.ImpactContext(),null);check(gold()==0,"failed Purge emits no healing area");
  reset();visuals.clear();PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(enemy,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);check(gold()==0,"Purge without Divine Strength emits no party-heal area");
  reset();equip("skill_tree_rpgs:paladin_tier_2_spell_2_modifier_2");
  for(int i=0;i<5;i++){visuals.clear();PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,null,net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,i),null);check(gold()==(i==4?1:0),"Seal of Light pulse"+i+" area timing");if(i==4)area(4,false);}
  reset();visuals.clear();PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,null,net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,4),null);check(gold()==0,"no Light area without its node");
  reset();for(var e:world.getAllEntities())if(e instanceof Mob)e.discard();
  var snapshot=new JsonObject();for(var key:List.of("p","m","armor","impact_m"))snapshot.addProperty(key,20);snapshot.addProperty("decree",false);snapshot.addProperty("judgement",false);
  visuals.clear();PaladinRevamp.landOath(p,p,snapshot);check(gold()==1,"Oath landing emits ground light without enemy targets");area(4,false);check(visuals.stream().anyMatch(s->s.effect().id.equals("spell_engine:magic_holy")&&s.effect().batch.count>=36),"Oath landing has central holy burst");
  snapshot.addProperty("judgement",true);visuals.clear();PaladinRevamp.landOath(p,p,snapshot);area(6,false);
  var original=spell("paladins:purgatory_ground").value();check(original.impacts.isEmpty()&&original.deliver.clouds.getFirst().volume.radius==6,"Purgatory burning-ground resource preserved");
 }catch(Throwable t){failures++;checks.add("ERROR "+t);t.printStackTrace();}finally{try{Files.write(Path.of("paladin-checks.txt"),checks);Files.writeString(Path.of("paladin-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures);}catch(Exception e){throw new RuntimeException(e);}server.halt(false);}}
}
