package local.wolfbalance;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.puffish.skillsmod.api.*;
import net.valhaimerd.orboblivioncompat.SkillOverhaul;
import archives.tater.penchant.PenchantmentDefinition;
import archives.tater.penchant.component.EnchantmentProgress;
import org.apache.commons.lang3.mutable.MutableFloat;
@Mod("wolf_balance_probe")
public class WolfProbe {
 List<String> results=new ArrayList<>();ServerLevel world;ServerPlayer owner;LivingEntity enemy;Category tree;
 public WolfProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);results.add("PASS "+text);}
 void near(double a,double b,String text){check(Math.abs(a-b)<.025,text+" actual="+a+" expected="+b);}
 Wolf wolf(){var w=EntityTypes.WOLF.create(world,EntitySpawnReason.COMMAND);w.setNoAi(true);w.tame(owner);w.setOwner(owner);w.setPos(4,80,4);world.addFreshEntity(w);w.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);w.setHealth(1000);return w;}
 ItemStack armor(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id)));}
 double hit(Wolf w,float damage,net.minecraft.world.damagesource.DamageSource source){w.invulnerableTime=0;w.setHealth(w.getMaxHealth());float before=w.getHealth();w.hurtServer(world,source,damage);return before-w.getHealth();}
 void unlock(String...ids){for(var id:ids)tree.getSkill(id).orElseThrow().unlock(owner);}
 void run(ServerStartedEvent event){var server=event.getServer();server.execute(()->{try{
  check(server.getPackRepository().getSelectedIds().stream().anyMatch(id->id.contains("durability_rework"))==Boolean.parseBoolean(Files.readString(Path.of("expect-module.txt")).trim()),"requested Durability Rework module state is loaded");
  world=server.overworld();enemy=EntityTypes.WARDEN.create(world,EntitySpawnReason.COMMAND);owner=FakePlayerFactory.getMinecraft(world);world.addNewPlayer(owner);owner.setPos(2,80,2);
  tree=SkillsAPI.getCategory(SkillOverhaul.CATEGORY).orElseThrow();tree.unlock(owner);tree.resetSkills(owner);tree.setExtraPoints(owner,1000);
  var w=wolf();var netherite=armor("ironwolfarmor:netherite_wolf_armor");w.setItemSlot(EquipmentSlot.BODY,netherite);
  near(hit(w,20,world.damageSources().mobAttack(EntityTypes.WARDEN.create(world,EntitySpawnReason.COMMAND))),11,"Netherite allows 55% of real Warden melee damage through");
  String[] names={"leather","copper","gold","chainmail","iron","diamond","netherite"};double[] reduction={.10,.15,.15,.20,.25,.35,.45};
  for(int i=0;i<names.length;i++){
   var stack=armor("ironwolfarmor:"+names[i]+"_wolf_armor");w.setItemSlot(EquipmentSlot.BODY,stack);
   check(stack.get(net.minecraft.core.component.DataComponents.LORE).lines().stream().anyMatch(line->line.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents&&contents.getKey().equals("tooltip.ironwolfarmor.damage_reduction")),"tier tooltip exposes replacement percentage "+names[i]);
   near(w.getArmorValue(),0,"no doubled armor points "+names[i]);near(w.getAttributeValue(Attributes.ARMOR_TOUGHNESS),0,"no doubled toughness "+names[i]);
   near(hit(w,20,world.damageSources().mobAttack(enemy)),20*(1-reduction[i]),"tier reduction "+names[i]);check(stack.getDamageValue()>0,"native equipped durability wear "+names[i]);
   near(hit(w,1,world.damageSources().mobAttack(enemy)),1-reduction[i],"small hits remain nonzero "+names[i]);
  }
  w.setItemSlot(EquipmentSlot.BODY,new ItemStack(Items.WOLF_ARMOR));near(w.getArmorValue(),0,"vanilla armor points removed");near(hit(w,20,world.damageSources().mobAttack(enemy)),16,"vanilla scute armor is 20% reduction");
  near(hit(w,20,world.damageSources().mobAttack(owner)),0,"existing owner/pet friendly-fire protection remains");
  w.setItemSlot(EquipmentSlot.BODY,ItemStack.EMPTY);near(hit(w,20,world.damageSources().mobAttack(enemy)),20,"unarmored wolf remains normal");
  w.setItemSlot(EquipmentSlot.BODY,armor("ironwolfarmor:netherite_wolf_armor"));near(hit(w,20,world.damageSources().fellOutOfWorld()),20,"void bypass preserved");
  var sonic=world.damageSources().sonicBoom(EntityTypes.WARDEN.create(world,EntitySpawnReason.COMMAND));results.add("INFO sonic wolf-armor bypass="+sonic.is(net.minecraft.tags.DamageTypeTags.BYPASSES_WOLF_ARMOR));near(hit(w,20,sonic),sonic.is(net.minecraft.tags.DamageTypeTags.BYPASSES_WOLF_ARMOR)?20:11,"sonic boom retains native wolf-armor eligibility");
  unlock("5frbav4zdc1hwquo");near(SkillOverhaul.reducedDamage(w,1),.97,"ordinary resistance is 3%, never flat subtraction");
  unlock("00ojkscfhac0ifpm","q0ul30ow7ifhtq6j","mxm9kmy816lzgkbt","iggdp0a33bljw4kl","8wjzxjnmc1gr7dvq");near(SkillOverhaul.effects(owner).get("res_pct"),.27,"all allocated node resistance totals 27% before cap");near(SkillOverhaul.reducedDamage(w,20),15,"Companion resistance caps at 25%");near(hit(w,20,world.damageSources().mobAttack(enemy)),8.25,"Netherite and capped pet skills multiply");
  near(SkillOverhaul.effects(owner).get("hp"),20,"Guardian retains ten hearts");near(SkillOverhaul.effects(owner).get("damage_pct"),.2,"Pack Leader retains damage");
  tree.resetSkills(owner);near(SkillOverhaul.reducedDamage(w,1),1,"reset removes pet resistance without stale bonus");
  PenchantmentDefinition.buildCache(server.registryAccess());var unbreaking=server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);check(unbreaking.value().getMaxLevel()==10,"Unbreaking cap remains X");
  for(var name:names)check(unbreaking.value().isSupportedItem(armor("ironwolfarmor:"+name+"_wolf_armor")),"all modded tiers accept Unbreaking "+name);
  world.getRandom().setSeed(1234567);
  for(var id:List.of("minecraft:wolf_armor","ironwolfarmor:leather_wolf_armor","ironwolfarmor:netherite_wolf_armor")){
   var stack=armor(id);check(unbreaking.value().isSupportedItem(stack),"native Unbreaking supports "+id);check(stack.is(net.minecraft.tags.ItemTags.create(Identifier.parse("penchant:unbreaking_armor"))),"armor durability curve selected "+id);
   check(!server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION).value().isSupportedItem(stack),"Unbreaking compatibility does not also enable Protection "+id);
   stack.enchant(unbreaking,10);var damage=new MutableFloat(11000);unbreaking.value().modifyDurabilityChange(world,10,stack,damage);check(damage.floatValue()>6500&&damage.floatValue()<7500,"armor X curve saves roughly 36% wear "+id+" remaining="+damage.floatValue());
  }
  var growing=armor("ironwolfarmor:netherite_wolf_armor");growing.enchant(unbreaking,1);w.setItemSlot(EquipmentSlot.BODY,growing);int before=EnchantmentProgress.getProgress(growing).getProgress(unbreaking);near(hit(w,20,world.damageSources().mobAttack(enemy)),11,"Unbreaking does not improve HP protection");check(EnchantmentProgress.getProgress(growing).getProgress(unbreaking)>before,"native equipped wolf armor wear advances Penchant progression");
  growing.setDamageValue(growing.getMaxDamage()-1);for(int i=0;i<300&&!growing.isEmpty();i++)hit(w,20,world.damageSources().mobAttack(enemy));check(growing.isEmpty(),"enchanted wolf armor can still break");near(hit(w,20,world.damageSources().mobAttack(enemy)),20,"broken armor leaves no protection");
  var breaking=armor("ironwolfarmor:netherite_wolf_armor");breaking.setDamageValue(breaking.getMaxDamage()-1);w.setItemSlot(EquipmentSlot.BODY,breaking);near(hit(w,20,world.damageSources().mobAttack(enemy)),11,"armor-breaking hit still receives tier reduction");check(breaking.isEmpty(),"native wear breaks armor on final hit");
  var playerTool=new ItemStack(Items.NETHERITE_PICKAXE);playerTool.enchant(unbreaking,1);int playerProgress=EnchantmentProgress.getProgress(playerTool).getProgress(unbreaking);playerTool.hurtAndBreak(20,owner,EquipmentSlot.MAINHAND);check(EnchantmentProgress.getProgress(playerTool).getProgress(unbreaking)>playerProgress,"existing player equipment progression still advances");
  w.discard();world.players().remove(owner);Files.writeString(Path.of("wolf-result.txt"),"PASS "+results.stream().filter(s->s.startsWith("PASS")).count()+" native checks\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("wolf-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{try{Files.write(Path.of("wolf-checks.txt"),results);}catch(Exception ignored){}server.halt(false);}});}
}
