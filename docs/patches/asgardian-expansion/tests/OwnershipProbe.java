package com.emandev.bifrost;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.race.Race;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.*;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.nbt.NbtOps;

/** Invoke in a disposable initialized native server; not packaged in the production JAR. */
public final class OwnershipProbe {
 private static int checks;
 private static void ok(boolean value,String label){checks++;if(!value)throw new AssertionError("Ownership: "+label);}
 public static void run(MinecraftServer server,List<String> output){int count=run(server);output.add("PASS "+count+" ownership native/codec/state assertions");}
 public static int run(MinecraftServer server){
  WeaponOwnership.tick(server);
  ServerPlayer owner=FakePlayerFactory.get(server.overworld(),new GameProfile(UUID.fromString("793a6a02-7b57-42ac-9250-6a06a39030dd"),"OwnerProbe"));
  ServerPlayer stranger=FakePlayerFactory.get(server.overworld(),new GameProfile(UUID.fromString("973a6a02-7b57-42ac-9250-6a06a39030dd"),"StrangerProbe"));
  PlayerRaceState.get(server).setRace(owner.getUUID(),Race.valueOf("ASGARDIAN"));
  PlayerRaceState.get(server).setRace(stranger.getUUID(),Race.valueOf("ASGARDIAN"));
  try{
   java.nio.file.Path marker=java.nio.file.Path.of("ownership-restart-id.txt");
   if(java.nio.file.Files.exists(marker)){
    String prior=java.nio.file.Files.readString(marker).trim();
    ok(WeaponOwnership.records(server).stream().anyMatch(e->e.id().equals(prior)&&e.owner().equals(owner.getUUID().toString())&&(e.state().equals(WeaponOwnership.WAITING)||e.state().equals(WeaponOwnership.ORBIT))),"actual server restart retains authoritative waiting/orbit record");
   }
  }catch(java.io.IOException e){throw new RuntimeException(e);}
  owner.getInventory().clearContent();stranger.getInventory().clearContent();
  // Exercise the actual recipe -> result-slot -> cursor path, rather than the helper alone.
  var nativeMenu=new CraftingMenu(11,owner.getInventory(),ContainerLevelAccess.create(owner.level(),owner.blockPosition()));
  var previousMenu=owner.containerMenu;owner.containerMenu=nativeMenu;
  for(int slot:new int[]{1,4})nativeMenu.getSlot(slot).set(new ItemStack(net.minecraft.world.item.Items.OBSIDIAN));
  nativeMenu.getSlot(2).set(new ItemStack(net.minecraft.world.item.Items.CRYING_OBSIDIAN));
  for(int slot:new int[]{5,8})nativeMenu.getSlot(slot).set(new ItemStack(net.minecraft.world.item.Items.BLAZE_ROD));
  nativeMenu.slotsChanged(nativeMenu.getSlot(1).container);
  ItemStack nativeResult=nativeMenu.getSlot(0).getItem();
  ok(nativeResult.is(ModItems.STORMBREAKER.get())&&!WeaponOwnership.id(nativeResult).isEmpty(),"actual Stormbreaker recipe result is bound before extraction");
  String craftedId=WeaponOwnership.id(nativeResult);nativeMenu.clicked(0,0,ContainerInput.PICKUP,owner);
  ok(WeaponOwnership.id(nativeMenu.getCarried()).equals(craftedId)&&WeaponOwnership.record(server,nativeMenu.getCarried()).state().equals(WeaponOwnership.INVENTORY),"native result click commits the same bound UUID to cursor");
  ok(nativeMenu.getSlot(1).getItem().isEmpty()&&nativeMenu.getSlot(8).getItem().isEmpty(),"native crafting consumes recipe ingredients");
  nativeMenu.setCarried(ItemStack.EMPTY);owner.containerMenu=previousMenu;
  ItemStack hammer=new ItemStack(ModItems.THORS_HAMMER.get());
  ok(!WeaponOwnership.canUse(owner,hammer),"unclaimed use denied");
  owner.getInventory().setItem(0,hammer);owner.getInventory().setSelectedSlot(0);
  ok(WeaponOwnership.claim(owner),"explicit claim");
  ok(!WeaponOwnership.id(hammer).isEmpty(),"persistent weapon UUID");
  ok(WeaponOwnership.owner(hammer).equals(owner.getUUID().toString()),"owner UUID");
  ok(WeaponOwnership.canUse(owner,hammer),"owner activation allowed");
  ok(!WeaponOwnership.canUse(stranger,hammer),"nonowner activation denied");
  ok(!WeaponOwnership.claim(owner),"reclaim cannot replace UUID");
  stranger.getInventory().setItem(0,hammer.copy());
  ok(stranger.getInventory().getItem(0).isEmpty(),"native foreign inventory set blocked");
  ok(!stranger.getInventory().add(hammer.copy()),"native foreign inventory add blocked");
  Slot foreignSlot=new Slot(new SimpleContainer(1),0,0,0);
  ok(!foreignSlot.mayPlace(hammer),"native container insertion blocked");
  SimpleContainer legacy=new SimpleContainer(1);legacy.setItem(0,hammer.copy());
  Slot legacySlot=new Slot(legacy,0,0,0);
  ok(!legacySlot.mayPickup(stranger),"native nonowner grave/container extraction blocked");
  ok(legacySlot.mayPickup(owner),"legacy owner extraction allowed");
  ok(legacySlot.safeClone(owner).isEmpty(),"native creative clone blocked");
  ok(owner.getInventory().removeFromSelected(false).isEmpty(),"native Q drop blocked");
  ok(owner.getInventory().getItem(0)==hammer,"Q drop retains original");
  var chest=ChestMenu.threeRows(5,owner.getInventory(),new SimpleContainer(27));
  int chestHand=chest.findSlot(owner.getInventory(),0).orElseThrow();
  chest.clicked(chestHand,0,ContainerInput.QUICK_MOVE,owner);
  ok(owner.getInventory().getItem(0)==hammer,"native external menu quick-move cannot store bound weapon");
  ItemStack duplicate=hammer.copy();owner.getInventory().setItem(2,duplicate);
  ok(owner.getInventory().getItem(2).isEmpty(),"owner duplicate UUID insertion rejected before possession");
  net.minecraft.world.item.component.BundleContents.Mutable bundle=new net.minecraft.world.item.component.BundleContents.Mutable(net.minecraft.world.item.component.BundleContents.EMPTY);
  ok(bundle.tryInsert(hammer.copy())==0,"native bundle nesting rejected");
  try{
   Class<?> keep=Class.forName("de.mrstilu2k6.better_revive.util.KeepGear");
   net.minecraft.core.NonNullList<ItemStack> grave=net.minecraft.core.NonNullList.withSize(41,ItemStack.EMPTY);
   keep.getMethod("setGraveSlot",net.minecraft.core.NonNullList.class,int.class,ItemStack.class,ServerPlayer.class).invoke(null,grave,0,hammer.copy(),owner);
   ok(grave.get(0).isEmpty(),"actual BetterRevive KeepGear excludes bound hotbar weapon");
   keep.getMethod("setGraveSlot",net.minecraft.core.NonNullList.class,int.class,ItemStack.class,ServerPlayer.class).invoke(null,grave,12,hammer.copy(),owner);
   ok(grave.get(12).isEmpty(),"actual BetterRevive grave excludes bound backpack inventory weapon");
   ok(!owner.getPersistentData().getCompoundOrEmpty("better_revive_keep_gear").getListOrEmpty("Vanilla").stream().anyMatch(tag->tag.toString().contains(WeaponOwnership.id(hammer))),"no bound weapon stored in retained gear NBT");
  }catch(ClassNotFoundException absent){}catch(Exception e){throw new RuntimeException(e);}
  var entry=WeaponOwnership.record(server,hammer);
  var ops=server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
  var encoded=WeaponOwnership.Entry.CODEC.encodeStart(ops,entry).getOrThrow();
  var decoded=WeaponOwnership.Entry.CODEC.parse(ops,encoded).getOrThrow();
  ok(decoded.id().equals(entry.id()),"record codec UUID roundtrip");
  ok(decoded.owner().equals(entry.owner()),"record codec owner roundtrip");
  ok(ItemStack.matches(decoded.stack(),entry.stack()),"record codec full stack roundtrip");
  WeaponOwnership.cooldown(owner,hammer,"probe",100);
  ok(!WeaponOwnership.cooldownReady(owner,hammer,"probe"),"persistent cooldown active");
  ok(WeaponOwnership.record(server,hammer).clocks().get("probe")==WeaponOwnership.now(server)+100,"cooldown absolute game tick");
  ItemStack preview=new ItemStack(ModItems.STORMBREAKER.get());
  ok(WeaponOwnership.prepareCraft(owner,preview),"craft preview binds before extraction");
  ok(!WeaponOwnership.id(preview).isEmpty(),"craft result already UUID-bound");
  ok(WeaponOwnership.record(server,preview).state().equals(WeaponOwnership.CRAFTING),"unextracted preview provisional");
  ok(!WeaponOwnership.canUse(owner,preview),"unextracted preview unusable");
  owner.getInventory().setItem(1,preview);WeaponOwnership.finishCraft(owner,preview);
  ok(WeaponOwnership.record(server,preview).state().equals(WeaponOwnership.INVENTORY),"extracted craft commits");
  exerciseFlight(server,owner,hammer,preview);
  ItemStack discardedPreview=new ItemStack(ModItems.STORMBREAKER.get());WeaponOwnership.prepareCraft(owner,discardedPreview);
  for(int i=0;i<owner.getInventory().getContainerSize();i++)if(WeaponOwnership.id(owner.getInventory().getItem(i)).equals(WeaponOwnership.id(hammer)))owner.getInventory().setItem(i,ItemStack.EMPTY);
  ItemStack heldCursor=hammer.copy();heldCursor.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Cursor Snapshot Preserved"));owner.containerMenu.setCarried(heldCursor);
  nativeDeath(owner,hammer);
  ok(WeaponOwnership.record(server,hammer).stack().getHoverName().getString().equals("Cursor Snapshot Preserved"),"actual final death preserves live cursor custom components");
  ok(WeaponOwnership.record(server,hammer).state().equals(WeaponOwnership.WAITING),"final death persists waiting hammer");
  ok(WeaponOwnership.record(server,preview).state().equals(WeaponOwnership.WAITING),"final death persists waiting axe");
  ok(WeaponOwnership.record(server,discardedPreview).state().equals(WeaponOwnership.CRAFTING),"discarded preview does not spawn free weapon");
  ok(owner.getInventory().isEmpty(),"final death removes bound inventory before grave/drop");
  ok(!WeaponOwnership.canUse(owner,hammer),"waiting weapon cannot activate");
  ok(!WeaponOwnership.throwWeapon(owner,hammer,false),"waiting weapon cannot summon");
  ok(!WeaponOwnership.adminRecover(stranger,WeaponOwnership.id(hammer)),"admin API cannot recover another owner weapon");
  ok(WeaponOwnership.adminRecover(owner,WeaponOwnership.id(hammer)),"explicit recovery moves authority to orbit");
  ok(!WeaponOwnership.adminReassign(stranger,WeaponOwnership.id(preview),UUID.randomUUID().toString()),"operator reassignment rejects incorrect expected owner");
  ok(WeaponOwnership.adminReassign(stranger,WeaponOwnership.id(preview),owner.getUUID().toString()),"explicit operator reassignment succeeds");
  ok(WeaponOwnership.record(server,preview).id().equals(WeaponOwnership.id(preview)),"operator recovery preserves weapon UUID");
  ok(WeaponOwnership.record(server,preview).owner().equals(stranger.getUUID().toString()),"operator recovery changes authoritative owner");
  ok(!WeaponOwnership.insertionAllowed(owner,preview),"old owner stale copy cannot enter inventory");
  ok(!WeaponOwnership.adminReassign(stranger,WeaponOwnership.id(preview),owner.getUUID().toString()),"operator reassignment cannot replay old UUID claim");
  deathDuringFlight(server);
  try{java.nio.file.Files.writeString(java.nio.file.Path.of("ownership-restart-id.txt"),WeaponOwnership.id(hammer));}catch(Exception e){throw new RuntimeException(e);}
  return checks;
 }
 @SuppressWarnings("unchecked") private static void deathDuringFlight(MinecraftServer server){
  var p=FakePlayerFactory.get(server.overworld(),new GameProfile(UUID.randomUUID(),"FlightDeathProbe"));
  PlayerRaceState.get(server).setRace(p.getUUID(),Race.valueOf("ASGARDIAN"));p.getInventory().clearContent();p.setPos(0,100,20);p.level().getChunk(0,1);
  ItemStack weapon=new ItemStack(ModItems.THORS_HAMMER.get());p.getInventory().setItem(0,weapon);p.getInventory().setSelectedSlot(0);ok(WeaponOwnership.claim(p),"flight-death test binds weapon");
  ok(WeaponOwnership.throwWeapon(p,weapon,false),"flight-death test launches authoritative weapon");
  java.lang.reflect.Field finalTypes=null;Object original=null;
  try{
   try{finalTypes=Class.forName("de.mrstilu2k6.better_revive.config.ReviveConfig").getField("finalDamageTypes");original=finalTypes.get(null);var list=new ArrayList<>((List<String>)original);list.add("minecraft:generic_kill");finalTypes.set(null,list);}catch(ClassNotFoundException absent){}
   var lookup=java.lang.invoke.MethodHandles.privateLookupIn(net.neoforged.neoforge.common.util.FakePlayer.class,java.lang.invoke.MethodHandles.lookup());
   var death=lookup.findSpecial(ServerPlayer.class,"die",java.lang.invoke.MethodType.methodType(void.class,net.minecraft.world.damagesource.DamageSource.class),net.neoforged.neoforge.common.util.FakePlayer.class);
   death.invoke(p,p.damageSources().genericKill());
   if(!WeaponOwnership.record(server,weapon).state().equals(WeaponOwnership.WAITING)){
    ok(AsgardianAccess.downed(p),"in-flight first lethal event is downing rather than final death");
    death.invoke(p,p.damageSources().genericKill());
   }
   ok(WeaponOwnership.record(server,weapon).state().equals(WeaponOwnership.WAITING),"actual final death atomically replaces in-flight state with waiting");
   for(int i=0;i<30;i++)WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,weapon).state().equals(WeaponOwnership.WAITING)&&p.getInventory().isEmpty(),"late return cannot resurrect an in-flight final-death weapon");
  }catch(Throwable t){throw new RuntimeException(t);}finally{if(finalTypes!=null)try{finalTypes.set(null,original);}catch(Exception e){throw new RuntimeException(e);}}
 }
 @SuppressWarnings("unchecked") private static void nativeDeath(ServerPlayer p,ItemStack weapon){
  try{
   var lookup=java.lang.invoke.MethodHandles.privateLookupIn(net.neoforged.neoforge.common.util.FakePlayer.class,java.lang.invoke.MethodHandles.lookup());
   var death=lookup.findSpecial(ServerPlayer.class,"die",java.lang.invoke.MethodType.methodType(void.class,net.minecraft.world.damagesource.DamageSource.class),net.neoforged.neoforge.common.util.FakePlayer.class);
   try{
    Class<?> config=Class.forName("de.mrstilu2k6.better_revive.config.ReviveConfig");
    var finalTypes=config.getField("finalDamageTypes");List<String> original=(List<String>)finalTypes.get(null);List<String> forced=new ArrayList<>(original);forced.add("minecraft:generic_kill");finalTypes.set(null,forced);
    try{
     death.invoke(p,p.damageSources().generic());
     ok(AsgardianAccess.downed(p),"actual native first death becomes downing");
     ok(WeaponOwnership.record(p.level().getServer(),weapon).state().equals(WeaponOwnership.INVENTORY),"actual downing does not transition ownership to waiting");
     ok(p.getInventory().contains(s->WeaponOwnership.id(s).equals(WeaponOwnership.id(weapon)))||WeaponOwnership.id(p.containerMenu.getCarried()).equals(WeaponOwnership.id(weapon)),"actual grave capture retains bound weapon in downed inventory or cursor");
     death.invoke(p,p.damageSources().genericKill());
    }finally{finalTypes.set(null,original);}
   }catch(ClassNotFoundException absent){death.invoke(p,p.damageSources().genericKill());}
   ok(WeaponOwnership.record(p.level().getServer(),weapon).state().equals(WeaponOwnership.WAITING),"actual ServerPlayer.die hook transitions final death");
  }catch(Throwable t){throw new RuntimeException(t);}
 }
 @SuppressWarnings("unchecked") private static void exerciseFlight(MinecraftServer server,ServerPlayer p,ItemStack hammer,ItemStack axe){
  List<ServerPlayer> players=null;Map<UUID,ServerPlayer> map=null;
  try{
   var listField=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");listField.setAccessible(true);players=(List<ServerPlayer>)listField.get(server.getPlayerList());
   var mapField=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");mapField.setAccessible(true);map=(Map<UUID,ServerPlayer>)mapField.get(server.getPlayerList());
   players.add(p);map.put(p.getUUID(),p);
   p.level().getChunk(0,0);
   var unsafe=new net.minecraft.core.BlockPos(5,200,5);var previousBlock=p.level().getBlockState(unsafe);
   p.level().setBlockAndUpdate(unsafe,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
   var safe=net.minecraft.core.BlockPos.containing(WeaponOwnership.safeSite(p.level(),unsafe));
   ok(p.level().getBlockState(safe).isAir()&&p.level().getBlockState(safe.above()).isAir(),"occupied death block chooses clear safe location");
   p.level().setBlockAndUpdate(unsafe,net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
   safe=net.minecraft.core.BlockPos.containing(WeaponOwnership.safeSite(p.level(),unsafe));
   ok(p.level().getBlockState(safe).isAir()&&p.level().getBlockState(safe.above()).isAir()&&p.level().getFluidState(safe).isEmpty(),"lava/void fallback is air with headroom and no fluid");
   p.level().setBlockAndUpdate(unsafe,previousBlock);
   var unclaimed=new net.minecraft.world.entity.item.ItemEntity(p.level(),2,200,2,new ItemStack(ModItems.THORS_HAMMER.get()));
   ok(!unclaimed.hurtServer(p.level(),p.damageSources().onFire(),1000),"unclaimed legacy ground weapon cannot burn");
   ok(!unclaimed.hurtServer(p.level(),p.damageSources().explosion(null,null),1000),"unclaimed legacy ground weapon ignores explosion damage");
   ok(unclaimed.ignoreExplosion(null),"native explosion impulse ignored for unclaimed weapon");
   var anchored=unclaimed.position();unclaimed.setDeltaMovement(3,3,3);unclaimed.tick();
   ok(unclaimed.position().equals(anchored),"native protected ground tick cannot move legacy weapon");
   ok(!net.minecraft.world.level.block.entity.HopperBlockEntity.addItem(new SimpleContainer(5),unclaimed),"native hopper cannot lift unclaimed protected weapon");
   ItemStack unused=new ItemStack(ModItems.STORMBREAKER.get());WeaponOwnership.prepareCraft(p,unused);WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,unused)==null,"abandoned online crafting preview removed");
   ItemStack visible=new ItemStack(ModItems.STORMBREAKER.get());WeaponOwnership.prepareCraft(p,visible);p.inventoryMenu.getSlot(0).set(visible);WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,visible)!=null,"live crafting result preview retained");
   p.inventoryMenu.getSlot(0).set(ItemStack.EMPTY);WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,visible)==null,"cleared crafting preview record removed");
   p.level().getChunk(0,0);p.setPos(0,100,0);p.setXRot(0);p.setYRot(0);p.getInventory().setSelectedSlot(0);
   // Failed disposable runs may have saved their non-AI cow fixtures at this altitude.
   for(var old:p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class,new net.minecraft.world.phys.AABB(-4,95,-4,4,110,40)))old.discard();
   ok(WeaponOwnership.throwWeapon(p,hammer,false),"default throw enters flight");
   ok(p.getInventory().getItem(0).isEmpty(),"throw removes authoritative held stack");
   ok(WeaponOwnership.record(server,hammer).state().equals(WeaponOwnership.FLIGHT),"flight state durable");
   ok(!WeaponOwnership.throwWeapon(p,hammer,false),"duplicate concurrent throw rejected");
   List<Integer> filled=new ArrayList<>();for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).isEmpty()){p.getInventory().setItem(i,new ItemStack(net.minecraft.world.item.Items.STONE));filled.add(i);}
   for(int i=0;i<100;i++)WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,hammer).state().equals(WeaponOwnership.ORBIT),"full inventory retains protected orbit state");
   ok(p.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(3),item->WeaponOwnership.id(item.getItem()).equals(WeaponOwnership.id(hammer))).size()==1,"full inventory has exactly one protected orbit visual");
   var managedField=WeaponOwnership.class.getDeclaredField("visuals");managedField.setAccessible(true);
   var managed=(Map<UUID,net.minecraft.world.entity.item.ItemEntity>)managedField.get(null);var previousVisual=managed.get(UUID.fromString(WeaponOwnership.id(hammer)));
   var originalLevel=p.level();var nether=server.getLevel(net.minecraft.world.level.Level.NETHER);nether.getChunk(0,0);p.setServerLevel(nether);WeaponOwnership.tick(server);
   int visualCount=0;for(var dimension:server.getAllLevels())visualCount+=dimension.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(3),item->WeaponOwnership.id(item.getItem()).equals(WeaponOwnership.id(hammer))).size();
   var currentVisual=managed.get(UUID.fromString(WeaponOwnership.id(hammer)));
   // A direct FakePlayer level swap does not install native player chunk tickets. The new
   // entity may be in a hidden/non-ticking section until the normal level loop runs.
   ok(visualCount<=1&&currentVisual!=null&&!currentVisual.isRemoved()&&currentVisual.level()==nether,"full-inventory orbit has one managed visual in new dimension without a duplicate accessible visual actual="+visualCount);
   ok(previousVisual!=null&&previousVisual.isRemoved()&&previousVisual!=currentVisual,"dimension change discards the original visual even in non-ticking chunks");
   p.setServerLevel(originalLevel);WeaponOwnership.tick(server);
   for(int slot:filled)p.getInventory().setItem(slot,ItemStack.EMPTY);WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,hammer).state().equals(WeaponOwnership.INVENTORY),"finite flight returns to inventory");
   ok(p.getInventory().contains(stack->WeaponOwnership.id(stack).equals(WeaponOwnership.id(hammer))),"return restores same UUID to reserved or safe inventory slot");
   p.getInventory().setSelectedSlot(1);
   var a=new net.minecraft.world.entity.animal.cow.Cow(net.minecraft.world.entity.EntityTypes.COW,p.level());a.setPos(0,100,6);a.setNoAi(true);a.setHealth(100);a.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);a.setHealth(100);p.level().addFreshEntity(a);
   var b=new net.minecraft.world.entity.animal.cow.Cow(net.minecraft.world.entity.EntityTypes.COW,p.level());b.setPos(0,104,9);b.setNoAi(true);b.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);b.setHealth(100);p.level().addFreshEntity(b);
   ok(WeaponOwnership.throwWeapon(p,axe,true),"ricochet throw enters flight");
   for(int i=0;i<120;i++)WeaponOwnership.tick(server);
   ok(a.getHealth()<100,"native ricochet damages first target");
   ok(b.getHealth()<100,"native ricochet acquires target four blocks higher in 3D");
   ok(WeaponOwnership.record(server,axe).state().equals(WeaponOwnership.INVENTORY),"ricochet terminates and returns");
   a.discard();b.discard();p.level().getChunk(0,1);
   var farA=new net.minecraft.world.entity.animal.cow.Cow(net.minecraft.world.entity.EntityTypes.COW,p.level());farA.setPos(0,100,20);farA.setNoAi(true);farA.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);farA.setHealth(100);p.level().addFreshEntity(farA);
   var farB=new net.minecraft.world.entity.animal.cow.Cow(net.minecraft.world.entity.EntityTypes.COW,p.level());farB.setPos(0,100,28);farB.setNoAi(true);farB.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);farB.setHealth(100);p.level().addFreshEntity(farB);
   WeaponOwnership.cooldown(p,axe,"throw",0);
   ok(p.level().getEntitiesOfClass(net.minecraft.world.entity.animal.cow.Cow.class,new net.minecraft.world.phys.AABB(-2,99,18,2,104,30)).size()==2,"long-range target fixtures are visible to native entity queries");
   ItemStack liveAxe=ItemStack.EMPTY;for(int i=0;i<p.getInventory().getContainerSize();i++)if(WeaponOwnership.id(p.getInventory().getItem(i)).equals(WeaponOwnership.id(axe)))liveAxe=p.getInventory().getItem(i);
   ok(WeaponOwnership.throwWeapon(p,liveAxe,true),"long initial ricochet launches");for(int i=0;i<140;i++)WeaponOwnership.tick(server);
   ok(farA.getHealth()<100&&farB.getHealth()<100,"24-block initial range and separate 10-block bounce reach targets at20and28 hp="+farA.getHealth()+","+farB.getHealth());farA.discard();farB.discard();
   ItemStack liveHammer=ItemStack.EMPTY;for(int i=0;i<p.getInventory().getContainerSize();i++)if(WeaponOwnership.id(p.getInventory().getItem(i)).equals(WeaponOwnership.id(hammer)))liveHammer=p.getInventory().getItem(i);
   liveHammer.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Operator Snapshot Preserved"));
   ok(WeaponOwnership.adminRecover(p,WeaponOwnership.id(hammer)),"operator recover snapshots current inventory components");WeaponOwnership.tick(server);
   ok(WeaponOwnership.record(server,hammer).stack().getHoverName().getString().equals("Operator Snapshot Preserved"),"operator recovery retains live renamed weapon");
   a.discard();b.discard();
  }catch(Exception e){throw new RuntimeException(e);}finally{if(players!=null)players.remove(p);if(map!=null)map.remove(p.getUUID());}
 }
}
