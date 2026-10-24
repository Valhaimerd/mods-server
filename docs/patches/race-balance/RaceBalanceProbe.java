package local.asgardprobe;
import java.util.*;
import java.util.function.Consumer;
import com.mojang.authlib.GameProfile;
import com.eiden.iourusraces.race.*;
import com.eiden.iourusraces.data.PlayerRaceState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.spell_power.api.*;
public class RaceBalanceProbe {
 static Consumer<String> out;
 static void eq(String label,double value,double expected){if(Math.abs(value-expected)>.006)throw new AssertionError(label+": "+value+" != "+expected);out.accept("PASS "+label+" actual="+value);}
 static void race(ServerPlayer p,Race r){PlayerRaceState.get(p.level().getServer()).setRace(p.getUUID(),r);RaceAttributes.applyRaceAttributes(p,r);}
 static double damage(ServerPlayer p,LivingEntity target,net.minecraft.world.damagesource.DamageSource source){var e=new LivingIncomingDamageEvent(target,new net.neoforged.neoforge.common.damagesource.DamageContainer(source,10));NeoForge.EVENT_BUS.post(e);return e.getAmount();}
 public static void run(ServerLevel world,ServerPlayer p,Consumer<String> sink){
  out=sink;race(p,Race.HUMAN);p.removeAllEffects();var attack=p.getAttribute(Attributes.ATTACK_DAMAGE);attack.setBaseValue(1);
  var ranged=p.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("ranged_weapon:damage")).orElseThrow());
  double rbase=ranged.getValue(),speed=p.getAttributeValue(Attributes.ATTACK_SPEED),delay=p.getCurrentItemAttackStrengthDelay();
  double bc=net.bettercombat.logic.PlayerAttackHelper.getAttackCooldownTicksCapped(p),haste=SpellPower.getHaste(p,net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE);
  race(p,Race.ORC);p.setHealth(p.getMaxHealth());RaceBalanceRules.apply(p,Race.ORC);
  eq("Orc attack speed 90%",p.getAttributeValue(Attributes.ATTACK_SPEED),speed*.9);eq("Native attack delay slower",p.getCurrentItemAttackStrengthDelay(),delay/.9);
  eq("Better Combat cooldown slower",net.bettercombat.logic.PlayerAttackHelper.getAttackCooldownTicksCapped(p),bc/.9);
  eq("RPG melee haste follows racial speed",SpellPower.getHaste(p,net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE),haste*.9);
  double[] hp={1,.51,.5,.4,.3,.2,.1,.01};double[] melee={0,0,2,3,4,5,5,5},range={0,0,1,1.5,2,2.5,2.5,2.5};
  for(int i=0;i<hp.length;i++){
   p.setHealth((float)(p.getMaxHealth()*hp[i]));RaceBalanceRules.apply(p,Race.ORC);
   eq("Melee bonus at HP "+hp[i],attack.getValue(),1+melee[i]);eq("Ranged bonus at HP "+hp[i],ranged.getValue(),rbase+range[i]);
   eq("RPG melee power at HP "+hp[i],SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_MELEE,p).baseValue(),1+melee[i]);
   eq("RPG ranged power at HP "+hp[i],SpellPower.getSpellPower(net.spell_engine.api.spell.ExternalSpellSchools.PHYSICAL_RANGED,p).baseValue(),rbase+range[i]);
   eq("Orc never rewrites base",attack.getBaseValue(),1);
  }
  p.setHealth(p.getMaxHealth());RaceBalanceRules.apply(p,Race.ORC);eq("Healing removes melee bonus",attack.getValue(),1);eq("Healing removes ranged bonus",ranged.getValue(),rbase);
  var gear=Identifier.parse("probe:attack_speed");p.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(new AttributeModifier(gear,.5,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
  eq("Orc penalty multiplies gear speed",p.getAttributeValue(Attributes.ATTACK_SPEED),speed*1.5*.9);
  race(p,Race.HUMAN);eq("Race change restores speed with gear",p.getAttributeValue(Attributes.ATTACK_SPEED),speed*1.5);p.getAttribute(Attributes.ATTACK_SPEED).removeModifier(gear);
  eq("Race change removes melee",attack.getValue(),1);eq("Race change removes ranged",ranged.getValue(),rbase);
  attack.setBaseValue(7);race(p,Race.ORC);p.setHealth(p.getMaxHealth());RaceBalanceRules.apply(p,Race.ORC);eq("Migrated custom base preserved",attack.getBaseValue(),7);race(p,Race.HUMAN);attack.setBaseValue(1);
  var legacy=FakePlayerFactory.get(world,new GameProfile(UUID.randomUUID(),"LegacyOrc"));legacy.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(8.8);race(legacy,Race.ORC);eq("Legacy Orc raw-base repair",legacy.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue(),1);eq("Legacy base retained as backup",legacy.getPersistentData().getDoubleOr("iourusraces:legacy_orc_attack_base",0),8.8);
  var clone=FakePlayerFactory.get(world,new GameProfile(UUID.randomUUID(),"ClonedOrc"));clone.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);RaceBalanceRules.clone(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(clone,legacy,false));race(clone,Race.ORC);eq("Clone retains completed migration",clone.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue(),7);eq("Clone retains legacy backup",clone.getPersistentData().getDoubleOr("iourusraces:legacy_orc_attack_base",0),8.8);
  race(p,Race.DWARF);double armor=p.getAttributeValue(Attributes.ARMOR);eq("Dwarf two armor",armor,2);for(int i=0;i<3;i++)race(p,Race.DWARF);eq("Dwarf repeat does not stack",p.getAttributeValue(Attributes.ARMOR),2);race(p,Race.HUMAN);eq("Dwarf armor removed",p.getAttributeValue(Attributes.ARMOR),0);
  race(p,Race.GOBLIN);eq("Goblin 75% size",p.getAttributeValue(Attributes.SCALE),.75);
  race(p,Race.ASGARDIAN);eq("Asgardian 110% size",p.getAttributeValue(Attributes.SCALE),1.1);eq("Asgardian no mining bonus",p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED),1);
  race(p,Race.ELF);eq("Elf movement +8%",p.getAttributeValue(Attributes.MOVEMENT_SPEED),.108);
  var target=EntityTypes.VILLAGER.create(world,EntitySpawnReason.COMMAND);var arrow=EntityTypes.ARROW.create(world,EntitySpawnReason.COMMAND);arrow.setOwner(p);
  eq("Elf no projectile amplification",damage(p,target,world.damageSources().arrow(arrow,p)),10);
  p.setOnGround(false);p.getAbilities().flying=false;p.setDeltaMovement(.3,-.8,.4);p.fallDistance=20;p.hurtMarked=false;RaceBalanceRules.slowFall(p);
  eq("Elf terminal speed",p.getDeltaMovement().y,-.2);eq("Elf horizontal X retained",p.getDeltaMovement().x,.3);eq("Elf horizontal Z retained",p.getDeltaMovement().z,.4);eq("Elf fall distance cleared",p.fallDistance,0);eq("Elf correction synchronized",p.hurtMarked?1:0,1);
  p.setDeltaMovement(0,.4,0);RaceBalanceRules.slowFall(p);eq("Elf upward jump retained",p.getDeltaMovement().y,.4);
  p.getAbilities().flying=true;p.setDeltaMovement(0,-.8,0);RaceBalanceRules.slowFall(p);eq("Elf creative flight retained",p.getDeltaMovement().y,-.8);p.getAbilities().flying=false;
  race(p,Race.HUMAN);arrow.discard();target.discard();
 }
}
