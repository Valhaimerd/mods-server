package local.paladinprobe;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.puffish.skillsmod.api.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_power.api.*;
import net.bettercombat.logic.PlayerAttackHelper;

@Mod("paladin_doctrine_probe")
public class PaladinTierOneProbe {
 final List<net.spell_engine.entity.SpellProjectile> joined=new ArrayList<>();final List<String> checks=new ArrayList<>();int failures;net.minecraft.server.level.ServerLevel world;ServerPlayer p;
 void check(boolean b,String s){checks.add((b?"PASS ":"FAIL ")+s);if(!b)failures++;}
 void near(double a,double b,String s){check(Math.abs(a-b)<.02,s+" actual="+a+" expected="+b);}
 Holder<Spell> spell(String id){return net.spell_engine.api.spell.registry.SpellRegistry.from(world).get(Identifier.parse(id)).orElseThrow();}
 void nodes(String...keys){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),old.actives(),old.passives(),Arrays.stream(keys).map(k->spell("skill_tree_rpgs:paladin_tier_2_"+k)).toList(),old.sources()));owner.spellModifierCache().clear();}
 net.minecraft.world.entity.monster.zombie.Zombie enemy(double x,double y,double z){var e=new net.minecraft.world.entity.monster.zombie.Zombie((EntityType<? extends net.minecraft.world.entity.monster.zombie.Zombie>)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")),world);e.setPos(x,y,z);e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);e.getAttribute(Attributes.ARMOR).setBaseValue(0);e.setHealth(1000);e.setNoAi(true);e.setNoGravity(true);world.addFreshEntity(e);return e;}
 double melee(){return SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_MELEE,p).nonCriticalValue();}
 double healing(){return SpellPower.getSpellPower(SpellSchools.HEALING,p).nonCriticalValue();}
 void purgeCase(String label,double damage,double heal,String...mods){nodes(mods);var e=enemy(0,100,6);p.setHealth(10);double before=e.getHealth();PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(e,new SpellExecution.ImpactContext())),PaladinTierOne.context(p,ExternalSpellSchools.PHYSICAL_MELEE),e.position());near(before-e.getHealth(),damage*melee()*.8,label+" damage receives80% PvE exactly once");near(p.getHealth()-10,heal*healing(),label+" healing uses P without damage multiplier");e.discard();}
 public PaladinTierOneProbe(){NeoForge.EVENT_BUS.addListener(this::run);NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e)->{if(e.getEntity() instanceof net.spell_engine.entity.SpellProjectile wave)joined.add(wave);});}
 void run(ServerStartedEvent event){var server=event.getServer();world=server.overworld();world.getChunk(0,0);try{
 p=FakePlayerFactory.getMinecraft(world);p.setPos(0,100,0);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(40);p.getAttribute(SpellSchools.HEALING.attributeEntry).setBaseValue(20);p.getAttribute(SpellPowerMechanics.CRITICAL_CHANCE.attributeEntry).setBaseValue(0);p.getAttribute(net.critical_strike.api.CriticalStrikeAttributes.CHANCE.attributeEntry).setBaseValue(0);p.setHealth(100);
 var cat=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();cat.unlock(p);cat.resetSkills(p);cat.setExtraPoints(p,1000);cat.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
 Class.forName("net.spell_engine.internals.PaladinTierOne");
 purgeCase("base",.5,.25);purgeCase("improved",.6,.3,"spell_1_root");purgeCase("strength",.6,.4,"spell_1_root","spell_1_modifier_1");purgeCase("absolution",.8,.3,"spell_1_root","spell_1_modifier_2");
 nodes();p.setHealth(10);check(!PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(),new SpellExecution.ImpactContext(),null),"missing enemy rejected");near(p.getHealth(),10,"no target gives no healing");
 var invulnerable=enemy(0,100,5);invulnerable.setInvulnerable(true);PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(invulnerable,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);near(p.getHealth(),10,"invulnerable enemy gives no healing");invulnerable.discard();
 nodes("spell_1_root","spell_1_modifier_2");var a=enemy(0,100,6);var b=enemy(1,100,6);p.setHealth(10);PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(a,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);near(1000-a.getHealth(),.8*melee()*.8,"explosion primary damaged once");near(1000-b.getHealth(),.8*melee()*.8,"explosion second victim damaged");near(p.getHealth()-10,.3*healing(),"multiple blast victims heal caster once");a.discard();b.discard();
 var pet=EntityTypes.WOLF.create(world,EntitySpawnReason.COMMAND);pet.tame(p);pet.setOwner(p);pet.setNoAi(true);pet.setNoGravity(true);pet.setPos(3,100,0);pet.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);pet.setHealth(10);world.addFreshEntity(pet);
 check(PartyRules.allies(p,pet),"owned pet uses shared ally protection");nodes("spell_1_root","spell_1_modifier_1");var target=enemy(0,100,6);p.setHealth(10);PaladinTierOne.purge(world,spell("paladins:flash_heal"),p,List.of(new SpellExecution.DeliveryTarget(target,new SpellExecution.ImpactContext())),new SpellExecution.ImpactContext(),null);near(pet.getHealth()-10,.4*healing(),"Divine Strength heals nearby eligible ally same amount");target.discard();
 nodes("spell_2_modifier_2");p.removeEffect(PaladinTierOne.chargeEffect());pet.setHealth(10);for(int i=0;i<4;i++)PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,null,SpellTarget.FocusMode.DIRECT,i),null);near(pet.getHealth(),10,"Light does not heal allies on first four pulses");PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,null,SpellTarget.FocusMode.DIRECT,4),null);near(pet.getHealth()-10,.1*healing(),"Light fifth pulse heals ally10% P");pet.discard();
 for(boolean light:new boolean[]{false,true}){nodes(light?new String[]{"spell_2_modifier_2"}:new String[]{});p.removeEffect(PaladinTierOne.chargeEffect());p.setHealth(10);for(int i=0;i<5;i++){PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,null,SpellPower.getSpellPower(SpellSchools.HEALING,p),SpellTarget.FocusMode.DIRECT,i),null);near(PaladinTierOne.charges(p),i+1,"one charge per pulse light="+light+" pulse="+i);}near(p.getHealth()-10,5*(light?.1:.05)*healing(),"five full healing pulses light="+light);near(p.getEffect(PaladinTierOne.chargeEffect()).getDuration(),300,"charge duration remains15s");PaladinTierOne.prepare(world,spell("paladins:blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(),null);near(PaladinTierOne.charges(p),5,"five-charge ceiling");}
 nodes();p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));p.getInventory().setSelectedSlot(0);var hand=PlayerAttackHelper.getCurrentAttack(p,0);check(hand!=null,"native Better Combat iron-sword swing resolved");
 p.setXRot(-40);p.setYRot(0);check(PaladinTierOne.swing(p,hand),"actual swing into empty air releases wave");near(PaladinTierOne.charges(p),4,"exactly one charge consumed");check(!PaladinTierOne.swing(p,hand),"duplicate same-tick swing rejected");near(PaladinTierOne.charges(p),4,"duplicate cannot burn charge");
 var projectiles=joined;check(projectiles.size()==1,"one native projectile launched");if(!projectiles.isEmpty()){var wave=projectiles.getFirst();near(wave.getDeltaMovement().length(),1.4,"wave speed1.4");check(wave.getDeltaMovement().y>.5,"wave aims upward for flying enemies");near(wave.range,20,"wave range20");near(wave.getBbWidth(),4.2,"native hitbox width1.2x original");wave.discard();}
 var hitMethod=net.spell_engine.entity.SpellProjectile.class.getDeclaredMethod("onHitEntity",net.minecraft.world.phys.EntityHitResult.class);hitMethod.setAccessible(true);
 for(boolean wrath:new boolean[]{false,true}){
  var waveSpell=spell(wrath?"paladins:blessed_wave_wrath":"paladins:blessed_wave");
  var wave=new net.spell_engine.entity.SpellProjectile(world,p,0,101,2,net.spell_engine.entity.SpellProjectile.Behaviour.FLY,waveSpell,PaladinTierOne.context(p,ExternalSpellSchools.PHYSICAL_MELEE),waveSpell.value().deliver.projectile.projectile.perks.copy());
  var first=enemy(0,100,4);var second=enemy(0,100,6);
  hitMethod.invoke(wave,new net.minecraft.world.phys.EntityHitResult(first));near(1000-first.getHealth(),(wrath?.4:.3)*melee()*.8,"native wave damage wrath="+wrath);
  if(wrath){check(!wave.isRemoved(),"Wrath survives first hit");float hp=first.getHealth();hitMethod.invoke(wave,new net.minecraft.world.phys.EntityHitResult(first));near(first.getHealth(),hp,"pierced target cannot be hit twice");hitMethod.invoke(wave,new net.minecraft.world.phys.EntityHitResult(second));near(1000-second.getHealth(),.4*melee()*.8,"Wrath damages second enemy");check(wave.isRemoved(),"Wrath stops after two total enemies");}
  else {check(wave.isRemoved(),"base wave stops at first enemy");near(second.getHealth(),1000,"base wave cannot pierce second enemy");}
  first.discard();second.discard();
 }

 double baseCd=SpellParameters.getCooldownDuration(p,spell("paladins:blessed_strikes"));near(baseCd,14,"base Blessed cooldown14");nodes("spell_2_root");near(SpellParameters.getCooldownDuration(p,spell("paladins:blessed_strikes")),12,"improved Blessed cooldown12");
 var haste=p.getAttribute(SpellPowerMechanics.HASTE.attributeEntry);double oldHaste=haste.getBaseValue();haste.setBaseValue(oldHaste*2);check(SpellParameters.getCooldownDuration(p,spell("paladins:blessed_strikes"))<12,"Healing-school haste shortens Blessed cooldown");check(SpellParameters.getCastDuration(p,spell("paladins:blessed_strikes").value())<2.5,"haste shortens channel");for(String key:List.of("paladins:battle_banner","skill_tree_rpgs:paladin_tier_4_spell_2_modifier_2")){var cloud=new net.spell_engine.entity.SpellCloud(world);cloud.setOwner(p);var cloudSpell=spell(key);cloud.onCreatedFromSpell(Identifier.parse(key),cloudSpell.value().deliver.clouds.getFirst(),PaladinTierOne.context(p,SpellSchools.HEALING),10,new Spell.Modifier.Cloud());near(PaladinTierOne.cloudInterval(10,cloud),5,"haste scales cloud pulse interval "+key);near(PaladinTierOne.cloudInterval(10000,cloud),10000,"decorative cloud stays inert");}haste.setBaseValue(oldHaste);
 for(String id:List.of("paladins:flash_heal","paladins:blessed_strikes","paladins:divine_protection","paladins:judgement","paladins:battle_banner","paladins:immolation","skill_tree_rpgs:paladin_tier_1_passive_1","skill_tree_rpgs:paladin_tier_2_passive_2","skill_tree_rpgs:paladin_tier_3_passive_1","skill_tree_rpgs:paladin_tier_3_passive_2"))check(spell(id).value().cost.cooldown.haste_affected,"native haste flag "+id);
 p.addEffect(new net.minecraft.world.effect.MobEffectInstance(PaladinTierOne.chargeEffect(),300,5));NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));near(p.getEffect(PaladinTierOne.chargeEffect()).getAmplifier(),4,"old six-charge saves normalize to five");p.addEffect(new net.minecraft.world.effect.MobEffectInstance(PaladinTierOne.chargeEffect(),300,4));NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,world.damageSources().generic()));near(PaladinTierOne.charges(p),0,"death clears prepared effect and glint");p.addEffect(new net.minecraft.world.effect.MobEffectInstance(PaladinTierOne.chargeEffect(),300,4));cat.resetSkills(p);NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));near(PaladinTierOne.charges(p),0,"respec clears prepared effect and glint");
 for(String set:List.of("justicar_armor","lightbringer_armor")){double total=0;for(String part:List.of("head","chest","legs","feet")){var stack=BuiltInRegistries.ITEM.getValue(Identifier.parse("armory_rpgs:"+set+"_"+part)).getDefaultInstance();for(var entry:stack.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers())if(entry.attribute().equals(SpellSchools.HEALING.attributeEntry)&&entry.modifier().operation()==net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)total+=entry.modifier().amount();}near(total,set.startsWith("justicar")?6:7,"item-only Healing Power "+set);}
 }catch(Throwable ex){ex.printStackTrace();check(false,"fixture exception "+ex);}finally{try{Files.writeString(Path.of("paladin-checks.txt"),String.join("\n",checks)+"\n");Files.writeString(Path.of("paladin-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures+"\n");}catch(Exception ex){ex.printStackTrace();}server.halt(false);}}
}
