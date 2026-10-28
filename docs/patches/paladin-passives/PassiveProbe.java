package local.paladinprobe;
import java.nio.file.*;import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_power.api.*;
import net.skill_tree_rpgs.compat.PaladinPassives;
@Mod("paladin_doctrine_probe")
public class PassiveProbe{
 final List<String> checks=new ArrayList<>();int failures;ServerPlayer p;
 void check(boolean b,String s){checks.add((b?"PASS ":"FAIL ")+s);if(!b)failures++;}
 void near(double a,double b,String s){check(Math.abs(a-b)<.001,s+" actual="+a+" expected="+b);}
 Holder<Spell> spell(String id){return net.spell_engine.api.spell.registry.SpellRegistry.from(p.level()).get(Identifier.parse(id)).orElseThrow();}
 void equip(String...ids){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();var container=new net.spell_engine.api.spell.container.SpellContainer(net.spell_engine.api.spell.container.SpellContainer.ContentType.NONE,"","",0,List.of(ids));owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),List.of(),Arrays.stream(ids).map(this::spell).toList(),List.of(),List.of(new SpellContainerSource.SourcedContainer("passive_probe",ItemStack.EMPTY,container))));owner.spellModifierCache().clear();PaladinPassives.refresh(p);}
 double armor(){return p.getAttributeValue(Attributes.ARMOR);}double power(){return p.getAttributeValue(SpellSchools.HEALING.attributeEntry);}
 public PassiveProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void run(ServerStartedEvent event){var server=event.getServer();try{
  var world=server.overworld();world.getChunk(0,0);p=FakePlayerFactory.getMinecraft(world);p.setPos(0,100,0);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getInventory().clearContent();p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);p.getAttribute(Attributes.ARMOR).setBaseValue(20);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(40);p.getAttribute(SpellSchools.HEALING.attributeEntry).setBaseValue(20);p.removeAllEffects();
  equip(PaladinPassives.VANGUARD);near(armor(),20,"no shield gives no basic armor");p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));PaladinPassives.refresh(p);near(armor(),21,"vanilla shield gives5% base armor");
  p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.SHIELD));PaladinPassives.refresh(p);near(armor(),21,"main-hand shield also qualifies");
  int modded=0;for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();if(!id.startsWith("minecraft:")&&item.getDefaultInstance().has(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS)){check(PaladinPassives.shield(item.getDefaultInstance()),"native-capability modded shield "+id);modded++;}}check(modded>0,"installed modded shield coverage");
  var attacker=EntityTypes.HUSK.create(world,EntitySpawnReason.COMMAND);attacker.setPos(0,100,3);world.addFreshEntity(attacker);var source=world.damageSources().mobAttack(attacker);var v=spell(PaladinPassives.VANGUARD);var cd=((SpellCasterEntity)p).getCooldownManager();
  SpellTriggers.onShieldBlock(p,source,10);near(p.getEffect(PriestRules.effect("skill_tree_rpgs:redoubt")).getAmplifier(),0,"first block adds one stack");near(armor(),22,"basic plus first block armor");SpellTriggers.onShieldBlock(p,source,10);near(p.getEffect(PriestRules.effect("skill_tree_rpgs:redoubt")).getAmplifier(),0,"one-second cooldown prevents immediate stack");
  for(int i=1;i<5;i++){cd.setDurationLeft(v,0);SpellTriggers.onShieldBlock(p,source,10);}near(p.getEffect(PriestRules.effect("skill_tree_rpgs:redoubt")).getAmplifier(),3,"Vanguard caps at4 stacks");near(armor(),25,"Vanguard total25% armor");near(p.getEffect(PriestRules.effect("skill_tree_rpgs:redoubt")).getDuration(),100,"block refreshes duration5s");
  p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,ItemStack.EMPTY);PaladinPassives.refresh(p);near(armor(),24,"removing shield removes basic only");equip();near(armor(),20,"removing passive clears conditional and stacked armor");
  equip(PaladinPassives.GUARDIAN);near(power(),21,"empty offhand gives5% Healing Power");p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.STONE));PaladinPassives.refresh(p);near(power(),20,"occupied offhand removes basic healing power");
  SpellTriggers.onMeleeImpact(p,attacker);var g=PriestRules.effect("skill_tree_rpgs:vengeance");near(p.getEffect(g).getAmplifier(),0,"ordinary noncritical hit grants Guardian stack");near(power(),21,"first hit gives5% Healing Power even with occupied offhand");near(p.getAttributeValue(Attributes.ATTACK_DAMAGE),40,"Guardian no longer increases attack damage");
  for(int i=0;i<5;i++)SpellTriggers.onMeleeImpact(p,attacker);near(p.getEffect(g).getAmplifier(),3,"Guardian no cooldown and4-stack cap");near(power(),24,"four Guardian stacks20% Healing Power");p.removeEffect(g);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(g,1,3),p);SpellTriggers.onMeleeImpact(p,attacker);near(p.getEffect(g).getDuration(),100,"Guardian refreshes5s even at maximum stacks");p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,ItemStack.EMPTY);PaladinPassives.refresh(p);near(power(),25,"Guardian basic plus stacks25% Healing Power");
  p.removeEffect(g);var wave=spell("paladins:blessed_wave");SpellTriggers.onSpellImpactSpecific(p,attacker,wave,wave.value().impacts.getFirst(),false,Spell.Trigger.Stage.POST);check(p.hasEffect(g),"noncritical passive wave damage qualifies");p.removeEffect(g);var purge=spell("paladins:flash_heal");SpellTriggers.onSpellImpactSpecific(p,attacker,purge,purge.value().impacts.getFirst(),false,Spell.Trigger.Stage.POST);check(p.hasEffect(g),"noncritical Purge Heal damage qualifies");
  equip();near(power(),20,"respec clears Guardian condition and stacks");equip(PaladinPassives.VANGUARD);near(SpellParameters.getCooldownDuration(p,v),1,"Vanguard base cooldown1s");double speed=p.getAttribute(SpellPowerMechanics.HASTE.attributeEntry).getBaseValue();p.getAttribute(SpellPowerMechanics.HASTE.attributeEntry).setBaseValue(speed*2);near(SpellParameters.getCooldownDuration(p,v),.5,"Vanguard spell haste halves cooldown");p.getAttribute(SpellPowerMechanics.HASTE.attributeEntry).setBaseValue(speed);
  equip(PaladinPassives.GUARDIAN);near(SpellParameters.getCooldownDuration(p,spell(PaladinPassives.GUARDIAN)),0,"Guardian stays cooldown-free");NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(p,world.damageSources().generic()));near(power(),20,"death removes conditional bonus");
 }catch(Throwable e){e.printStackTrace();check(false,"fixture exception "+e);}finally{try{Files.writeString(Path.of("paladin-checks.txt"),String.join("\n",checks)+"\n");Files.writeString(Path.of("paladin-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures+"\n");}catch(Exception e){e.printStackTrace();}server.halt(false);}}
}
