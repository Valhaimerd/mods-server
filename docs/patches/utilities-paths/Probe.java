package local.utilitiesprobe;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.food.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.puffish.skillsmod.api.*;
import net.puffish.skillsmod.SkillsMod;
import net.valhaimerd.orboblivioncompat.*;
import net.valhaimerd.orboblivioncompat.mixin.*;
@Mod("utility_probe")
public class Probe {
 List<String> checks=new ArrayList<>();ServerPlayer p;Category c;
 public Probe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void check(boolean b,String s){if(!b)throw new AssertionError(s);checks.add("PASS "+s);}
 void near(double a,double b,String s){check(Math.abs(a-b)<.002,s+" actual="+a+" expected="+b);}
 void unlock(String... ids){for(String id:ids)c.getSkill(id).orElseThrow().unlock(p);}
 void reset(){c.resetSkills(p);}
 void path(int index){unlock("h8krq290mbf4dwdx","nu1hd3hh115xcpax");for(String id:Utilities.PATHS.get(index))unlock(id);}
 void run(ServerStartedEvent e){var server=e.getServer();String result;
  try{
   p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(server.overworld());p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setPos(0,65,0);
   p.getPersistentData().putBoolean("skill_overhaul_v1",true);
   c=SkillsAPI.getCategory(Utilities.CATEGORY).orElseThrow();c.unlock(p);c.setExtraPoints(p,1000);
   var xp=c.getExperience().orElseThrow();xp.setTotal(p,10000);int categoryXp=xp.getTotal(p);
   reset();unlock("50jkue9gpbpznlfd","3gpel3ejgsuvozax","h8krq290mbf4dwdx","nu1hd3hh115xcpax",Utilities.PATHS.get(0).get(0));
   var raw=((PlayerDataAccess)(Object)((SkillsDataAccess)(Object)SkillsMod.getInstance()).utilities$playerData(p)).utilities$categories().get(Utilities.CATEGORY);
   raw.getUnlockedSkillIds().addAll(Utilities.PATHS.get(1));raw.getUnlockedSkillIds().add("yu7a75bw5anm9z2u");
   int total=c.getPointsTotal(p);Utilities.migrate(p);
   check(c.getSpentPoints(p)==2,"migration refunds Utilities only");check(c.getPointsTotal(p)==total&&xp.getTotal(p)==categoryXp,"migration preserves points and earned XP");check(!raw.getUnlockedSkillIds().contains("yu7a75bw5anm9z2u"),"removed historical leaf cleared");
   unlock("h8krq290mbf4dwdx");Utilities.migrate(p);check(c.getSkill("h8krq290mbf4dwdx").orElseThrow().getState(p)==Skill.State.UNLOCKED,"second migration does not refund again");
   reset();unlock("h8krq290mbf4dwdx","nu1hd3hh115xcpax",Utilities.PATHS.get(0).get(0));unlock(Utilities.PATHS.get(0).get(7));check(!Utilities.mastery(p,0),"mastery cannot skip ordinary leaves");
   unlock(Utilities.PATHS.get(1).get(0));check(!Utilities.unlocked(p).contains(Utilities.PATHS.get(1).get(0)),"second path blocked on server");
   unlock(Utilities.PATHS.get(0).get(1));c.getSkill(Utilities.PATHS.get(0).get(0)).orElseThrow().lock(p);unlock(Utilities.PATHS.get(1).get(0));check(!Utilities.unlocked(p).contains(Utilities.PATHS.get(1).get(0)),"partial entrance refund cannot bypass exclusion");
   reset();path(0);check(Utilities.mastery(p,0),"Fitness mastery complete");near(Utilities.stat(p,"food"),.14,"food/saturation exact14%");near(Utilities.stat(p,"breath"),.24,"breathing exact24%");
   p.getFoodData().setFoodLevel(0);p.getFoodData().setSaturation(0);
   new ItemStack(Items.BREAD).finishUsingItem(p.level(),p);
   near(p.getFoodData().getFoodLevel(),5,"fractional food does not round every meal up");near(p.getFoodData().getSaturationLevel(),5,"native saturation clamp preserved");
   new ItemStack(Items.BREAD).finishUsingItem(p.level(),p);near(p.getFoodData().getFoodLevel(),11,"food remainder carried to next meal");
   p.getFoodData().setFoodLevel(10);p.getFoodData().setSaturation(0);p.getPersistentData().remove(Utilities.PREFIX+"food_fraction");new ItemStack(Items.BREAD).finishUsingItem(p.level(),p);near(p.getFoodData().getSaturationLevel(),6*1.14,"saturation bonus applied once to original payload");
   p.getFoodData().setFoodLevel(6);p.getFoodData().setSaturation(0);p.tickCount=20;NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(p));near(p.getFoodData().getFoodLevel(),14,"rations restore exactly4drumsticks");near(p.getFoodData().getSaturationLevel(),0,"rations grant no saturation");
   long cooldown=p.getPersistentData().getLongOr(Utilities.PREFIX+"rations_until",0);p.getFoodData().setFoodLevel(6);NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(p));near(p.getFoodData().getFoodLevel(),6,"rations cooldown blocks reactivation");reset();path(0);check(p.getPersistentData().getLongOr(Utilities.PREFIX+"rations_until",0)==cooldown,"rations cooldown survives reset");
   var air=LivingEntity.class.getDeclaredMethod("decreaseAirSupply",int.class);air.setAccessible(true);int supply=10000;for(int i=0;i<1240;i++)supply=(int)air.invoke(p,supply);near(10000-supply,1000,"24% longer air uses fractional consumption");
   reset();double jump=p.getAttributeValue(Attributes.JUMP_STRENGTH),sneak=p.getAttributeValue(Attributes.SNEAKING_SPEED),safe=p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);path(1);
   near(p.getAttributeValue(Attributes.JUMP_STRENGTH),jump*1.1,"jump+10%");near(p.getAttributeValue(Attributes.SNEAKING_SPEED),sneak*1.24,"sneak+24%, no walk modifier");near(p.getAttributeValue(Attributes.SAFE_FALL_DISTANCE),safe+4,"safe fall+4blocks");near(Utilities.stat(p,"climb"),.24,"climb+24%");
   p.isInPowderSnow=true;p.setTicksFrozen(0);for(int i=0;i<100;i++)p.setTicksFrozen(p.getTicksFrozen()+1);near(p.getTicksFrozen(),75,"freezing buildup25%slower, not immunity");p.isInPowderSnow=false;
   var dc=new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().fall(),8);
   var pre=new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre(p,dc);NeoForge.EVENT_BUS.post(pre);near(pre.getNewDamage(),4,"SureFooting halves positive final fall damage");check(Utilities.snow(p)&&p.hasEffect(Utilities.SNOW_GUARD),"snow guard has synchronized icon effect");
   check(net.minecraft.world.level.block.PowderSnowBlock.canEntityWalkOnPowderSnow(p),"snow guard prevents sinking");
   var freeze=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(p,new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().freeze(),1));NeoForge.EVENT_BUS.post(freeze);check(freeze.isCanceled(),"snow guard prevents environmental freezing damage");
   var second=new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre(p,new net.neoforged.neoforge.common.damagesource.DamageContainer(p.damageSources().fall(),8));NeoForge.EVENT_BUS.post(second);near(second.getNewDamage(),8,"SureFooting20s cooldown");
   p.removeEffect(Utilities.SNOW_GUARD);p.getPersistentData().remove(Utilities.PREFIX+"snow_until");reset();path(2);
   near(Utilities.stat(p,"cost"),.33,"cost discounts sum33%");near(Utilities.stat(p,"xp"),.16,"XPbonus sums16%");near(Utilities.stat(p,"durability"),.16,"durability preservation sums16%");near(p.getAttributeValue(Attributes.LUCK),1,"Ingenuity+1Luck");
   p.experienceLevel=0;p.experienceProgress=0;p.totalExperience=0;p.getPersistentData().remove(Utilities.PREFIX+"xp_fraction");
   for(int i=0;i<7;i++){p.takeXpDelay=0;new ExperienceOrb(p.level(),0,65,0,1).playerTouch(p);}near(p.totalExperience,8,"native XP orbs award16%once with carry");
   var durability=ItemStack.class.getDeclaredMethod("processDurabilityChange",int.class,net.minecraft.server.level.ServerLevel.class,LivingEntity.class);durability.setAccessible(true);
   var pick=new ItemStack(Items.DIAMOND_PICKAXE);p.getRandom().setSeed(1927);int preserved=0;Utilities.TOOL_USE.set(1);for(int i=0;i<10000;i++)if((int)durability.invoke(pick,1,p.level(),p)==0)preserved++;Utilities.TOOL_USE.set(0);check(preserved>1450&&preserved<1750,"single16%durability roll distribution");
   near((int)durability.invoke(pick,1,p.level(),p),1,"scripted durability outside tool use unchanged");Utilities.TOOL_USE.set(1);near((int)durability.invoke(new ItemStack(Items.DIAMOND_CHESTPLATE),1,p.level(),p),1,"armor durability excluded");near((int)durability.invoke(new ItemStack(Items.SHIELD),1,p.level(),p),1,"shield durability excluded");near((int)durability.invoke(new ItemStack(Items.ELYTRA),1,p.level(),p),1,"elytra durability excluded");Utilities.TOOL_USE.set(0);
   for(var item:List.of(Items.DIAMOND_PICKAXE,Items.DIAMOND_AXE,Items.DIAMOND_SHOVEL,Items.DIAMOND_HOE,Items.SHEARS)){
    var tool=new ItemStack(item);p.getRandom().setSeed(1927);int saved=0;Utilities.TOOL_USE.set(1);for(int i=0;i<100;i++)if((int)durability.invoke(tool,1,p.level(),p)==0)saved++;Utilities.TOOL_USE.set(0);check(saved>0,"tool category preserved "+item);
   }
   var nativePick=new ItemStack(Items.DIAMOND_PICKAXE);int beforeWear=0;
   p.getRandom().setSeed(1927);for(int i=0;i<100;i++)nativePick.mineBlock(p.level(),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),new net.minecraft.core.BlockPos(0,64,0),p);check(nativePick.getDamageValue()>0&&nativePick.getDamageValue()<100,"native mining enters scoped preservation");check(Utilities.TOOL_USE.get()==0,"native tool scope released after use");
   p.getPersistentData().remove(Utilities.PREFIX+"xp_fraction");int awards=0;for(int i=0;i<7;i++)awards+=Utilities.xp(p,1);near(awards,8,"seven1XP awards yield8XP");near(p.getPersistentData().getDoubleOr(Utilities.PREFIX+"xp_fraction",0),.12,"XP fraction carries independently");near(Utilities.xp(p,-100),-100,"negativeXP not amplified");
   for(int nodes=1;nodes<=3;nodes++){
    reset();unlock("h8krq290mbf4dwdx","nu1hd3hh115xcpax");for(int i=0;i<nodes;i++)unlock(Utilities.PATHS.get(2).get(new int[]{1,3,6}[i]));
    near(Utilities.anvil(p,100),100-11*nodes,"additive anvil tier"+nodes);near(Utilities.anvil(p,1),1,"positive anvil minimum tier"+nodes);near(Utilities.anvil(p,0),0,"zero anvil stayszero tier"+nodes);
   }
   reset();path(2);
   var anvil=new AnvilMenu(1,p.getInventory());anvil.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));anvil.setItemName("Utilities test");check(anvil.getCost()>0,"native rename displays payable discounted cost");
   for(float fraction:new float[]{0,.25f,.99f}){
    p.experienceLevel=30;p.experienceProgress=fraction;p.totalExperience=Utilities.totalAt(30)+(int)(fraction*Utilities.levelXp(30));p.getPersistentData().remove(Utilities.PREFIX+"enchant_fraction");
    int before=Utilities.totalAt(p.experienceLevel)+(int)(p.experienceProgress*Utilities.levelXp(p.experienceLevel));int debit=Utilities.enchantDebit(30,fraction,3);p.onEnchantmentPerformed(new ItemStack(Items.DIAMOND_SWORD),3);
    int after=Utilities.totalAt(p.experienceLevel)+(int)(p.experienceProgress*Utilities.levelXp(p.experienceLevel)+.0001);near(before-after,Math.floor(debit*.67+1e-8),"native enchanting XPdebit progress"+fraction);
   }
   p.removeAllEffects();Utilities.CONSUMING.set(1);p.addEffect(new MobEffectInstance(MobEffects.SPEED,100));Utilities.CONSUMING.set(0);check(p.getEffect(MobEffects.SPEED).getDuration()==100,"class/equipment callbacks during consumption are excluded");
   p.removeAllEffects();var apple=new ItemStack(Items.GOLDEN_APPLE);apple.finishUsingItem(p.level(),p);check(p.getEffect(MobEffects.REGENERATION).getDuration()==135,"positive food effect lasts35%longer");
   p.removeAllEffects();var potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.SWIFTNESS));potion.finishUsingItem(p.level(),p);check(p.getEffect(MobEffects.SPEED).getDuration()==4860,"drinking potion extends original duration once");
   potion=new ItemStack(Items.POTION);potion.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.SWIFTNESS));potion.finishUsingItem(p.level(),p);check(p.getEffect(MobEffects.SPEED).getDuration()==4860,"repeated potion does not compound active duration");
   p.addEffect(new MobEffectInstance(MobEffects.SPEED,6000));check(p.getEffect(MobEffects.SPEED).getDuration()==6000,"nonconsumption effects not extended");p.removeAllEffects();
   var poison=new ItemStack(Items.POTION);poison.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.POISON));poison.finishUsingItem(p.level(),p);check(p.getEffect(MobEffects.POISON).getDuration()==900,"harmful drink duration unchanged");
   result="PASS "+checks.size()+" Utilities runtime checks";
  }catch(Throwable t){t.printStackTrace();result="FAIL "+t;}
  try{Files.write(Path.of("utilities-checks.txt"),checks);Files.writeString(Path.of("utilities-result.txt"),result);}catch(Exception x){throw new RuntimeException(x);}server.halt(false);
 }
}
