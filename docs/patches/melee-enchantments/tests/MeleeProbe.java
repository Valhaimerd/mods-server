package local.melee;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
@Mod("melee_probe") public class MeleeProbe {
 int checks;
 final List<String> hidden=List.of("protection_grace_enchantment:protection_grace","nova_structures:antidote","nova_structures:traveler","enderzoology:soulbound","air_jump_enchantment:air_jump","tense_enchantment:tense","enderzoology:withering","crossarms:deflector","crossarms:bombard","crossarms:recycling","nova_structures:ghasted","minecraft:sweeping_edge","betterarcheology:penetrating_strike","nova_structures:spiteful","enderzoology:decay");
 void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("MELEE_PASS "+label);}

 double arrowHit(net.minecraft.server.level.ServerLevel level,net.minecraft.server.level.ServerPlayer player,String weaponId,boolean zero,boolean rpgCrit,boolean cooled)throws Exception{
  var chance=net.critical_strike.api.CriticalStrikeAttributes.CHANCE;var damage=net.critical_strike.api.CriticalStrikeAttributes.DAMAGE;
  player.getAttribute(chance.attributeEntry).setBaseValue(chance.baseValue*(rpgCrit?3:0.1));player.getAttribute(damage.attributeEntry).setBaseValue(damage.baseValue*2);player.tickCount++;
  var weapon=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(weaponId)));player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,weapon);
  if(zero)weapon.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("crossarms:zero_drop")).orElseThrow(),1);
  var arrow=new net.minecraft.world.entity.projectile.arrow.Arrow(level,player,new ItemStack(Items.ARROW),weapon);arrow.setCritArrow(zero);arrow.setDeltaMovement(0,0,3);arrow.setBaseDamage(2);
  EnchantmentHelper.onProjectileSpawned(level,weapon,arrow,item->{});if(cooled){arrow.removeTag("ZERO_DROP");arrow.setNoGravity(false);check(arrow.entityTags().contains("CROSSARMS_NO_CRITICAL"),"critical suppression persists after no-gravity ends "+weaponId);}check(arrow.getOwner()==player,"projectile owner retained through data functions "+weaponId+" zero="+zero);
  if(zero&&!cooled){check(arrow.isNoGravity(),"Zero Drop keeps no-gravity flight "+weaponId);check(!arrow.isCritArrow(),"Zero Drop disables vanilla crit "+weaponId);}
  var victim=(net.minecraft.world.entity.LivingEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:zombie")).create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);victim.setPos(0,80,5);level.addFreshEntity(victim);
  var hit=net.minecraft.world.entity.projectile.arrow.AbstractArrow.class.getDeclaredMethod("onHitEntity",net.minecraft.world.phys.EntityHitResult.class);hit.setAccessible(true);hit.invoke(arrow,new net.minecraft.world.phys.EntityHitResult(victim));double lost=1000-victim.getHealth();victim.discard();arrow.discard();return lost;
 }

 void lifeSteal(net.minecraft.server.level.ServerLevel level,net.minecraft.server.level.ServerPlayer player)throws Exception{
  var server=level.getServer();var h=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(Identifier.parse("life_steal_enchantment:life_steal")).orElseThrow();
  var objective=server.getScoreboard().getObjective("life_steal_enchantment.player.damage_dealt");check(objective!=null,"Life Steal damage objective loaded");
  var source=server.createCommandSourceStack().withEntity(player).withLevel(level).withPosition(player.position());
  var tickEffects=net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("tickEffects");tickEffects.setAccessible(true);
  for(int tier=1;tier<=3;tier++){
   var sword=new ItemStack(Items.DIAMOND_SWORD);var enchantments=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);enchantments.set(h,tier);sword.set(DataComponents.ENCHANTMENTS,enchantments.toImmutable());player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);check(EnchantmentHelper.getItemEnchantmentLevel(h,sword)==tier,"Life Steal exact test tier "+tier);
   int threshold=tier==1?200:tier==2?160:120;var score=server.getScoreboard().getOrCreatePlayerScore(player,objective);
   player.setHealth(10);score.set(threshold-1);server.getCommands().performPrefixedCommand(source,"function life_steal_enchantment:damage_dealt");tickEffects.invoke(player);check(player.getHealth()==10,"no heal below threshold tier "+tier);check(score.get()==threshold-1,"partial damage preserved tier "+tier);
   score.set(threshold);server.getCommands().performPrefixedCommand(source,"function life_steal_enchantment:damage_dealt");tickEffects.invoke(player);check(Math.abs(player.getHealth()-13)<0.001,"exact 3 HP heal tier "+tier+" actual="+player.getHealth());check(server.getScoreboard().getPlayerScoreInfo(player,objective)==null||server.getScoreboard().getPlayerScoreInfo(player,objective).value()==0,"damage counter reset tier "+tier);
   player.setHealth(19);server.getScoreboard().getOrCreatePlayerScore(player,objective).set(threshold);server.getCommands().performPrefixedCommand(source,"function life_steal_enchantment:damage_dealt");tickEffects.invoke(player);check(player.getHealth()==20,"healing caps at maximum tier "+tier);
  }
  player.setHealth(10);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_SWORD));server.getScoreboard().getOrCreatePlayerScore(player,objective).set(1000);server.getCommands().performPrefixedCommand(source,"function life_steal_enchantment:damage_dealt");tickEffects.invoke(player);check(player.getHealth()==10,"no Life Steal without enchantment");
 }
 int startupTicks;
 void allowedSword(net.minecraft.server.level.ServerLevel level,net.minecraft.server.level.ServerPlayer player,Holder<Enchantment> a,Holder<Enchantment> b){
  var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(a,1);var book=new ItemStack(Items.ENCHANTED_BOOK);var ench=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ench.set(b,1);book.set(DataComponents.STORED_ENCHANTMENTS,ench.toImmutable());var menu=new AnvilMenu(2,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));menu.getSlot(0).set(sword);menu.getSlot(1).set(book);menu.createResult();check(!menu.getSlot(2).getItem().isEmpty(),"actual compatible sword anvil "+a.getRegisteredName()+" / "+b.getRegisteredName());
 }
 public MeleeProbe(){NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.ServerTickEvent.Post event)->{if(++startupTicks==20)start(new ServerStartedEvent(event.getServer()));});}
 void start(ServerStartedEvent e){var server=e.getServer();server.execute(()->{try{
  var level=server.overworld();var registry=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
  var disabled=TagKey.create(Registries.ENCHANTMENT,Identifier.parse("penchant:disabled"));
  var viewer=TagKey.create(Registries.ENCHANTMENT,Identifier.parse("c:hidden_from_recipe_viewers"));
  var player=FakePlayerFactory.getMinecraft(level);level.addNewPlayer(player);player.setPos(0,80,0);player.giveExperienceLevels(1000);lifeSteal(level,player);
  for(String id:hidden){var h=registry.get(Identifier.parse(id)).orElseThrow();check(h.is(disabled),"disabled tag "+id);check(h.is(viewer),"recipe viewer tag "+id);
   var stack=new ItemStack(id.contains("air_jump")||id.contains("traveler")?Items.DIAMOND_BOOTS:Items.DIAMOND_CHESTPLATE);stack.enchant(h,1);check(EnchantmentHelper.getItemEnchantmentLevel(h,stack)==0,"normal application rejected "+id);
   var loot=server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("melee_probe:hidden/"+id.replace(':','/'))));
   var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
   for(int n=0;n<20;n++)for(var item:loot.getRandomItems(params,n))check(!item.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY).keySet().contains(h),"explicit random loot rejects "+id+" sample "+n);
  }
  CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),true,level.registryAccess());
  for(String id:hidden){var h=registry.get(Identifier.parse(id)).orElseThrow();boolean found=false;for(var tab:CreativeModeTabs.allTabs())for(var item:tab.getSearchTabDisplayItems())if(item.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY).keySet().contains(h))found=true;check(!found,"creative book absent "+id);}
  String[][] blocked={{"minecraft:sharpness","minecraft:bane_of_arthropods"},{"minecraft:sharpness","minecraft:smite"},{"minecraft:sharpness","nova_structures:aerials_bane"},{"minecraft:sharpness","nova_structures:illagers_bane"},{"dungeons_arise:discharge","ice_enchantments:frost_aspect"},{"dungeons_arise:discharge","minecraft:fire_aspect"},{"dungeons_arise:ensnaring","minecraft:sharpness"},{"minecraft:flame","ice_enchantments:frost"},{"minecraft:flame","spectral_enchantment:spectral"},{"ice_enchantments:frost","spectral_enchantment:spectral"},{"nova_structures:multishot","minecraft:power"},{"minecraft:multishot","nova_structures:power"},{"minecraft:multishot","crossarms:acceleration"},{"minecraft:multishot","crossarms:frenzy"},{"nova_structures:power","crossarms:acceleration"},{"nova_structures:power","crossarms:frenzy"},{"crossarms:acceleration","crossarms:frenzy"},{"nova_structures:gravity","crossarms:frenzy"},{"minecraft:thorns","minecraft:fire_protection"},{"minecraft:thorns","minecraft:blast_protection"},{"minecraft:thorns","fangs_enchantment:fangs"},{"minecraft:frost_walker","minecraft:soul_speed"}};
  for(var pair:blocked)for(int order=0;order<2;order++){
   var a=registry.get(Identifier.parse(pair[order])).orElseThrow();var b=registry.get(Identifier.parse(pair[1-order])).orElseThrow();
   check(!Enchantment.areCompatible(a,b),"native incompatibility "+a.getRegisteredName()+" / "+b.getRegisteredName());
   var armor=new ItemStack(pair[0].equals("minecraft:sharpness")||pair[0].startsWith("dungeons_arise:")?Items.DIAMOND_SWORD:pair[0].equals("minecraft:frost_walker")?Items.DIAMOND_BOOTS:pair[0].equals("minecraft:thorns")?(pair[1].contains("fangs")?Items.DIAMOND_LEGGINGS:Items.DIAMOND_CHESTPLATE):pair[0].contains("flame")||pair[0].equals("ice_enchantments:frost")||pair[0].equals("nova_structures:multishot")?Items.BOW:Items.CROSSBOW);armor.enchant(a,1);
   var book=new ItemStack(Items.ENCHANTED_BOOK);var ench=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ench.set(b,1);book.set(DataComponents.STORED_ENCHANTMENTS,ench.toImmutable());
   var menu=new AnvilMenu(0,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));menu.getSlot(0).set(armor);menu.getSlot(1).set(book);menu.createResult();
   check(menu.getSlot(2).getItem().isEmpty(),"actual anvil rejects both orders "+a.getRegisteredName()+" / "+b.getRegisteredName());
  }
  for(var pair:List.of(new String[]{"minecraft:thorns","minecraft:protection"},new String[]{"minecraft:thorns","minecraft:unbreaking"},new String[]{"minecraft:frost_walker","minecraft:thorns"},new String[]{"minecraft:soul_speed","minecraft:thorns"},new String[]{"minecraft:fire_protection","fangs_enchantment:fangs"}))check(Enchantment.areCompatible(registry.get(Identifier.parse(pair[0])).orElseThrow(),registry.get(Identifier.parse(pair[1])).orElseThrow()),"unrelated compatibility retained "+Arrays.toString(pair));
  var allowed=new ItemStack(Items.DIAMOND_CHESTPLATE);allowed.enchant(registry.get(Identifier.parse("minecraft:protection")).orElseThrow(),1);var allowedBook=new ItemStack(Items.ENCHANTED_BOOK);var ae=new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);ae.set(registry.get(Identifier.parse("minecraft:thorns")).orElseThrow(),1);allowedBook.set(DataComponents.STORED_ENCHANTMENTS,ae.toImmutable());var goodMenu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));goodMenu.getSlot(0).set(allowed);goodMenu.getSlot(1).set(allowedBook);goodMenu.createResult();check(!goodMenu.getSlot(2).getItem().isEmpty(),"control: actual anvil still combines Protection with Thorns");
  String[] banes={"minecraft:bane_of_arthropods","minecraft:smite","nova_structures:aerials_bane","nova_structures:illagers_bane"};
  for(int i=0;i<banes.length;i++)for(int j=i+1;j<banes.length;j++){var a=registry.get(Identifier.parse(banes[i])).orElseThrow();var b=registry.get(Identifier.parse(banes[j])).orElseThrow();check(Enchantment.areCompatible(a,b)&&Enchantment.areCompatible(b,a),"individual Banes compatible "+banes[i]+" / "+banes[j]);allowedSword(level,player,a,b);allowedSword(level,player,b,a);}
  for(String bane:banes)check(Enchantment.areCompatible(registry.get(Identifier.parse("dungeons_arise:ensnaring")).orElseThrow(),registry.get(Identifier.parse(bane)).orElseThrow()),"Ensnaring compatible "+bane);
  for(String bane:banes){var a=registry.get(Identifier.parse("dungeons_arise:ensnaring")).orElseThrow();var b=registry.get(Identifier.parse(bane)).orElseThrow();allowedSword(level,player,a,b);allowedSword(level,player,b,a);}
  var healNode=server.getCommands().getDispatcher().getRoot().getChild("modpack_lifesteal_heal");check(healNode!=null&&!healNode.canUse(server.createCommandSourceStack().withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS)),"exact-heal command unavailable without operator permissions");
  for(String universal:List.of("minecraft:piercing","crossarms:zero_drop")){var u=registry.get(Identifier.parse(universal)).orElseThrow();for(var other:registry.listElements().toList()){if(other.equals(u))continue;check(Enchantment.areCompatible(u,other),"unrestricted forward "+universal+" / "+other.getRegisteredName());check(Enchantment.areCompatible(other,u),"unrestricted reverse "+universal+" / "+other.getRegisteredName());}}
  for(String allowedGravity:List.of("minecraft:multishot","nova_structures:power","crossarms:acceleration"))check(Enchantment.areCompatible(registry.get(Identifier.parse("nova_structures:gravity")).orElseThrow(),registry.get(Identifier.parse(allowedGravity)).orElseThrow()),"Gravity compatible "+allowedGravity);
  if(net.neoforged.fml.ModList.get().isLoaded("critical_strike"))for(String weaponId:List.of("minecraft:crossbow","arsenal:unique_heavy_crossbow_1")){
   double ordinary=arrowHit(level,player,weaponId,false,false,false),ordinaryCrit=arrowHit(level,player,weaponId,false,true,false),zero=arrowHit(level,player,weaponId,true,false,false),zeroCrit=arrowHit(level,player,weaponId,true,true,false);
   check(ordinary>0&&ordinaryCrit>ordinary,"control: RPG ordinary arrows can crit "+weaponId);
   check(Math.abs(zeroCrit-zero)<0.001,"Zero Drop rejects RPG critical multiplier "+weaponId+" actual="+zeroCrit+" normal="+zero);
   double cooled=arrowHit(level,player,weaponId,true,true,true);check(Math.abs(cooled-zero)<0.001,"RPG critical blocked after ZERO_DROP flight tag removed "+weaponId);
   player.tickCount++;var melee=net.critical_strike.internal.CritLogic.modifyDamage((net.critical_strike.internal.CriticalStriker)player,level.damageSources().playerAttack(player),10);check(melee!=null&&melee.amount()>10,"ordinary melee RPG crit retained while holding Zero Drop "+weaponId);
   System.out.println("ZERO_DROP_MEASURE "+weaponId+" ordinary="+ordinary+" ordinaryCrit="+ordinaryCrit+" zero="+zero+" zeroCrit="+zeroCrit);
  }
  Files.writeString(Path.of("result.txt"),"PASS "+checks);System.out.println("MELEE_COMPLETE "+checks);server.halt(false);
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}server.halt(false);}});}
}
