package local.overhaul;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.nbt.*;
import net.puffish.skillsmod.api.*;
import net.puffish.attributesmod.api.*;
import net.spell_power.api.*;
import net.valhaimerd.orboblivioncompat.SkillOverhaul;

@Mod("overhaul_probe")
public class OverhaulProbe {
 int checks; net.minecraft.server.level.ServerPlayer player; Category tree;
 void check(boolean b,String label) {if(!b)throw new AssertionError(label);checks++;System.out.println("OVERHAUL_PASS "+label);}
 void near(double actual,double expected,String label){check(Math.abs(actual-expected)<.0001,label+" actual="+actual+" expected="+expected);}
 void unlock(String...ids){for(var id:ids)tree.getSkill(id).orElseThrow().unlock(player);SkillOverhaul.applyCritical(player);}
 void reset(){tree.resetSkills(player);tree.setExtraPoints(player,1000);SkillOverhaul.applyCritical(player);}
 void stat(String node,String attribute,double bonus,boolean flat){
  reset();var holder=BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attribute)).orElseThrow();var instance=player.getAttribute(holder);boolean dynamic=attribute.startsWith("puffish_attributes:");if(!dynamic)instance.setBaseValue(10);double before=dynamic ? DynamicModification.create().withPositive(holder,player).applyTo(100):instance.getValue();unlock(node);double after=dynamic ? DynamicModification.create().withPositive(holder,player).applyTo(100):instance.getValue();near(after,flat ? before+bonus:before*(1+bonus),node+" "+attribute);
 }
 double value(String id){return player.getAttributeValue(BuiltInRegistries.ATTRIBUTE.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE,Identifier.parse(id))));}
 int useTicks(Item item)throws Exception{
  player.stopUsingItem();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(item));player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
  var tick=LivingEntity.class.getDeclaredMethod("updateUsingItem",ItemStack.class);tick.setAccessible(true);
  for(int i=0;i<20;i++)tick.invoke(player,player.getUseItem());
  int elapsed=player.getTicksUsingItem();player.stopUsingItem();return elapsed;
 }
 public OverhaulProbe(){NeoForge.EVENT_BUS.addListener(this::started);}
 void started(ServerStartedEvent event){var server=event.getServer();server.execute(()->{
  try {
   player=FakePlayerFactory.getMinecraft(server.overworld());tree=SkillsAPI.getCategory(SkillOverhaul.CATEGORY).orElseThrow();tree.unlock(player);tree.setExtraPoints(player,1000);
   if(Files.exists(Path.of("xp_only"))) {
    var categories=List.of(tree,SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow(),SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:weapon_skills")).orElseThrow());
    for(var cat:categories)cat.unlock(player);
    var zombie=EntityTypes.ZOMBIE.create(server.overworld(),EntitySpawnReason.COMMAND);zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(800);zombie.setHealth(800);
    var output=new ArrayList<String>();
    var all=net.puffish.skillsmod.SkillsMod.class.getDeclaredMethod("getAllCategories");all.setAccessible(true);
    var configs=(java.util.Collection<net.puffish.skillsmod.config.CategoryConfig>)all.invoke(net.puffish.skillsmod.SkillsMod.getInstance());
    for(var config:configs)if(config.experience().isPresent())for(var sourceConfig:config.experience().orElseThrow().experienceSources()) {
     var source=sourceConfig.instance();
     if(source instanceof net.puffish.skillsmod.experience.source.builtin.KillEntityExperienceSource kill)
      output.add("kill="+kill.calculation().evaluate(new net.puffish.skillsmod.experience.source.builtin.KillEntityExperienceSource.Data(player,zombie,ItemStack.EMPTY,server.overworld().damageSources().playerAttack(player),100)));
     if(source instanceof net.puffish.skillsmod.experience.source.builtin.SharedKillEntityExperienceSource kill)
      output.add("shared="+kill.calculation().evaluate(new net.puffish.skillsmod.experience.source.builtin.SharedKillEntityExperienceSource.Data(player,zombie,ItemStack.EMPTY,server.overworld().damageSources().playerAttack(player),100,800,1,1)));
    }
    Files.write(Path.of("xp-result.txt"),output);return;
   }
   var xp=tree.getExperience().orElseThrow();xp.setTotal(player,10000);unlock("50jkue9gpbpznlfd","3gpel3ejgsuvozax");
   var beforeXP=xp.getTotal(player);SkillOverhaul.migrate(player);check(tree.getSpentPoints(player)==0,"migration refunds allocations");near(xp.getTotal(player),beforeXP,"migration preserves category XP");
   unlock("50jkue9gpbpznlfd");SkillOverhaul.migrate(player);check(tree.getSkill("50jkue9gpbpznlfd").orElseThrow().getState(player)==Skill.State.UNLOCKED,"migration runs once");reset();
   unlock("50jkue9gpbpznlfd","3gpel3ejgsuvozax");check(tree.getSkill("ei4d3ri7bngns8lj").orElseThrow().getState(player)==Skill.State.EXCLUDED,"ranged entrance excluded after caster choice");
   reset();unlock("6xmqgm3uw97hrynh","15trz7ezr8z7kcp8","qp2x45h0nbr233e1");check(tree.getSkill("828iwet0erdpwynl").orElseThrow().getState(player)==Skill.State.EXCLUDED,"Companion right excluded after left choice");
   reset();double crit=value("spell_power:critical_chance"),haste=value("spell_power:haste");unlock("ro3sae58m8jzn4yz");near(value("spell_power:critical_chance")-crit,2.5,"ordinary critical chance adds 2.5 points");unlock("zdfpatqypd5nnqx8");near(value("spell_power:critical_chance")-crit,12.5,"Magistrate adds ten critical points");near(value("spell_power:haste")/haste,1.05,"Magistrate haste");
   reset();var schools=List.of(SpellSchools.ARCANE,SpellSchools.FIRE,SpellSchools.FROST);var powerBefore=new ArrayList<Double>();
   for(var school:schools){player.getAttribute(school.attributeEntry).setBaseValue(10);powerBefore.add(SpellPower.getSpellPower(school,player).baseValue());}
   double healingBefore=value("spell_power:healing"),healthBefore=player.getMaxHealth();unlock("zdfpatqypd5nnqx8");
   for(int i=0;i<schools.size();i++){
    var school=schools.get(i);double power=SpellPower.getSpellPower(school,player).baseValue();near(power/powerBefore.get(i),1.2,"Magistrate school power "+i);
    var victim=EntityTypes.PIG.create(server.overworld(),EntitySpawnReason.COMMAND);victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);victim.setHealth(100);
    victim.hurtServer(server.overworld(),SpellDamageSource.player(school,player),(float)power);near(100-victim.getHealth(),power,"school damage uses enhanced power "+i);
   }
   near(value("spell_power:healing"),healingBefore,"Magistrate excludes healing power");near(player.getMaxHealth(),healthBefore,"Magistrate excludes health");
   reset();var ca=player.getAttribute(BuiltInRegistries.ATTRIBUTE.get(Identifier.parse("spell_power:critical_chance")).orElseThrow());
   var gear=Identifier.parse("overhaul_probe:critical_gear");ca.addTransientModifier(new AttributeModifier(gear,20,AttributeModifier.Operation.ADD_VALUE));crit=value("spell_power:critical_chance");unlock("ro3sae58m8jzn4yz");near(value("spell_power:critical_chance")-crit,2.5,"critical points stay fixed with additive gear");ca.removeModifier(gear);
   reset();unlock("46o8u8oydffa04o6");near(DynamicModification.create().withPositive(PuffishAttributes.REPAIR_COST,player).applyTo(100),80,"one anvil node 20 percent discount");unlock("gh96zb6t67hnmswd","gk57oho18h66ghxe");near(DynamicModification.create().withPositive(PuffishAttributes.REPAIR_COST,player).applyTo(100),51.2,"three anvil discounts compose safely");
   reset();var wolf=EntityTypes.WOLF.create(server.overworld(),EntitySpawnReason.COMMAND);wolf.tame(player);wolf.setOwner(player);
   // Fake players are not in the level's entity lookup; ensure the reference resolves.
   var baseHP=wolf.getMaxHealth();var baseDamage=wolf.getAttributeValue(Attributes.ATTACK_DAMAGE);var baseSpeed=wolf.getAttributeValue(Attributes.MOVEMENT_SPEED);
   unlock("15trz7ezr8z7kcp8","qp2x45h0nbr233e1","e7fyzt06vz8mu5iv","5grntvcsxgm61gbq","vyt2xj8si9zbat4x","z6awauxhc53s0bhl","5frbav4zdc1hwquo","xz6a8myofnhkv0e0","o3fd9bzo883nqd57","629rr01c70gc90kj","kbheklivvbq2aaj8","87cn8ldpzmjljp0q","iggdp0a33bljw4kl");
   SkillOverhaul.apply(wolf);near(wolf.getMaxHealth(),baseHP+4,"left pet health");near(wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),(baseDamage+4)*1.2,"left flat and percentage pet damage");near(wolf.getAttributeValue(Attributes.MOVEMENT_SPEED),baseSpeed*1.24,"left pet speed");near(wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),.1,"left pet knockback resistance");near(wolf.getAttributeValue(Attributes.ATTACK_KNOCKBACK),.2,"left pet attack knockback");near(SkillOverhaul.reducedDamage(wolf,20),17.1,"left flat one plus ten percent pet resistance");
   var pig=EntityTypes.PIG.create(server.overworld(),EntitySpawnReason.COMMAND);pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);pig.setHealth(100);pig.setPos(1,65,0);wolf.setPos(0,65,0);
   var pigHealth=pig.getHealth();wolf.doHurtTarget(server.overworld(),pig);near(pigHealth-pig.getHealth(),(baseDamage+4)*1.2,"actual wolf attack damage");check(pig.getDeltaMovement().horizontalDistance()>0,"actual wolf attack knocks target back");
   SkillOverhaul.apply(wolf);near(wolf.getMaxHealth(),baseHP+4,"pet bonuses do not accumulate");
   reset();SkillOverhaul.apply(wolf);near(wolf.getMaxHealth(),baseHP,"pet max health restored after reset");near(wolf.getAttributeValue(Attributes.ATTACK_DAMAGE),baseDamage,"pet damage restored after reset");
   unlock("15trz7ezr8z7kcp8","828iwet0erdpwynl","00ojkscfhac0ifpm","ha05s7wy4h135376","iswxmklicwgn4xmp","q0ul30ow7ifhtq6j","mk08ei1wfvcssxs6","mxm9kmy816lzgkbt","rbfta22ujrmwzugp","fjtdo0ic4650e43d","6vlgfj2olene3csb","fr0yn9kxau3qimvv","8wjzxjnmc1gr7dvq");
   SkillOverhaul.apply(wolf);near(wolf.getMaxHealth(),baseHP+36,"Guardian Stead total plus eighteen hearts");near(wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),.5,"Guardian Stead total knockback resistance");near(SkillOverhaul.reducedDamage(wolf,20),12.75,"right three flat plus twenty-five percent resistance");
   wolf.setHealth(wolf.getMaxHealth());var wolfHP=wolf.getHealth();wolf.invulnerableTime=0;wolf.hurtServer(server.overworld(),server.overworld().damageSources().generic(),20);near(wolfHP-wolf.getHealth(),12.75,"actual incoming pet damage event");
   reset();unlock("ad8qet8e75ys5sek","5h6pmokx7ro45qtw");check(tree.getSkill("xzau8wf7cmkahump").orElseThrow().getState(player)==Skill.State.EXCLUDED,"Defensive exclusivity");
   reset();var ranged=net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon.HASTE.entry;
   var normalDraw=player.getAttributeValue(ranged);int bowTicks=useTicks(Items.BOW),crossbowTicks=useTicks(Items.CROSSBOW);unlock("8qdaale4k86t2ntn","02did3uvilfob8c2");near(player.getAttributeValue(ranged)/normalDraw,1.03*1.03,"two draw-speed nodes");
   check(useTicks(Items.BOW)>bowTicks,"bow drawing advances faster");check(useTicks(Items.CROSSBOW)>crossbowTicks,"crossbow charging advances faster");
   reset();double normalRanged=DynamicModification.create().withPositive(PuffishAttributes.BOW_PROJECTILE_SPEED,player).applyTo(1);
   unlock("ui0gusk2hniqwghe");near(DynamicModification.create().withPositive(PuffishAttributes.BOW_PROJECTILE_SPEED,player).applyTo(1)/normalRanged,1.25,"bow projectile speed");near(DynamicModification.create().withPositive(PuffishAttributes.CROSSBOW_PROJECTILE_SPEED,player).applyTo(1),1.25,"crossbow projectile speed");
   reset();var hp=player.getMaxHealth();unlock("ebhti3huj2v7zvny");near(player.getMaxHealth(),hp+12,"Colossus six hearts");
   stat("r7mv8vq583ntlk4y","puffish_attributes:melee_damage",.05,false);stat("r7mv8vq583ntlk4y","minecraft:attack_speed",.05,false);stat("r7mv8vq583ntlk4y","puffish_attributes:armor_shred",.1,false);
   stat("5es0kwe84yl9t3yb","puffish_attributes:ranged_damage",.05,false);stat("5es0kwe84yl9t3yb","minecraft:luck",1,true);stat("5es0kwe84yl9t3yb","minecraft:movement_speed",.05,false);stat("5es0kwe84yl9t3yb","puffish_attributes:jump",.05,false);
   stat("6tinz5atn533lzo9","puffish_attributes:magic_resistance",.15,false);stat("6tinz5atn533lzo9","puffish_attributes:ranged_resistance",.15,false);
   stat("3gpel3ejgsuvozax","spell_power:arcane",.05,false);stat("v3lnj0ubt40kezxm","spell_power:haste",.03,false);
   for(var catName:List.of("class_skills","weapon_skills")) {
    var cat=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:"+catName)).orElseThrow();check(cat.getExperience().isPresent(),catName+" loads");
   }
   Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
  }catch(Throwable ex){ex.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+ex+"\n");}catch(Exception ignored){}}
  finally{server.halt(false);}
 });}
}
