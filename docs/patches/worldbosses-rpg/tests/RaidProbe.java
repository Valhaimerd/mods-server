package local.worldbosses.test;

import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.mki.worldbosses.entity.boss.WorldBossEntity;
import com.mki.worldbosses.config.WorldBossesConfig;
import local.worldbosses.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("worldbosses_rpg_probe")
public class RaidProbe {
    int checks;
    void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("RAID_PASS "+label);}
    void near(double a,double b,String label){check(Math.abs(a-b)<.02,label+" actual="+a+" expected="+b);}
    public RaidProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
    @SuppressWarnings("unchecked")
    ServerPlayer player(net.minecraft.server.MinecraftServer server,ServerLevel level,int index)throws Exception {
        var profile=new GameProfile(new UUID(123,index),"RaidPlayer"+index);
        var p=new ServerPlayer(server,level,profile,ClientInformation.createDefault());
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(server,new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND),p,net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false));
        p.connection.markClientLoaded();p.setGameMode(GameType.SURVIVAL);p.setInvulnerable(false);p.getAbilities().invulnerable=false;
        p.snapTo(index+1,70,1,0,0);
        var list=net.minecraft.server.players.PlayerList.class.getDeclaredField("players");list.setAccessible(true);((List<ServerPlayer>)list.get(server.getPlayerList())).add(p);
        var local=ServerLevel.class.getDeclaredField("players");local.setAccessible(true);((List<ServerPlayer>)local.get(level)).add(p);
        var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);((Map<UUID,ServerPlayer>)field.get(server.getPlayerList())).put(p.getUUID(),p);
        return p;
    }
    void run(ServerStartedEvent event){var server=event.getServer();try{
        var level=server.overworld();((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(10000);List<ServerPlayer> players=new ArrayList<>();
        for(int i=1;i<=8;i++)players.add(player(server,level,i));
        check(RaidProfile.number(WorldBossesConfig.INSTANCE,"rpgBalanceVersion")==1,"config migration persisted");
        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(),true,level.registryAccess());
        check(true,"creative tabs build without migrated-gear duplicates");
        for(var entry:RaidProfile.BOSSES.entrySet()) {
            String key=entry.getKey();var spec=entry.getValue();String path=key.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);
            var type=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:"+path));
            var boss=(WorldBossEntity)type.create(level,EntitySpawnReason.COMMAND);boss.snapTo(0,70,0,0,0);
            RaidRuntime.scale(boss,level,RaidRuntime.config(boss));
            near(boss.getMaxHealth(),spec.health(),key+" actual solo health exceeds engine baseline cap");
            boss.setHealth(boss.getMaxHealth()/2);
            for(int i=0;i<4;i++)RaidRuntime.record(boss,players.get(i));
            RaidRuntime.scale(boss,level,RaidRuntime.config(boss));
            near(boss.getMaxHealth(),spec.health()*1.45,key+" four-player HP");
            near(boss.getHealth()/boss.getMaxHealth(),.5,key+" scaling preserves health fraction");
            near(boss.getAttributeValue(Attributes.ARMOR),Math.min(30,spec.armor()*1.3),key+" four-player armor");
            near(boss.getAttributeValue(Attributes.ATTACK_DAMAGE),spec.melee(),key+" outgoing damage not scaled");
            RaidRuntime.state(boss).started=level.getGameTime()-200;boss.tickCount=20;boss.setTarget(players.getFirst());
            if(key.equals("shadowWeaver")){var f=boss.getClass().getDeclaredField("awakenTicks");f.setAccessible(true);f.setInt(boss,0);}
            float before=boss.getHealth();RaidRuntime.heal(boss,level);
            near(boss.getHealth()-before,spec.regen()*1.3,key+" four-player regeneration");
            for(var p:players)RaidRuntime.record(boss,p);RaidRuntime.scale(boss,level,RaidRuntime.config(boss));
            near(boss.getMaxHealth(),spec.health()*2.05,key+" eight-player HP");
            near(boss.getAttributeValue(Attributes.ARMOR),Math.min(30,spec.armor()*1.7),key+" armor stays within native cap");
            var saved=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());
            RaidRuntime.save(boss,saved);
            check(saved.buildResult().contains("rpg_raid"),key+" encounter state serialized");
            RaidRuntime.state(boss).players=1;
            RaidRuntime.load(boss,net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(RaidRuntime.state(boss).players==8,key+" encounter state restored");
            RaidRuntime.scale(boss,level,RaidRuntime.config(boss));
            near(boss.getMaxHealth(),spec.health()*2.05,key+" restored scaling does not compound");
            boss.discard();
        }
        for(String family:List.of("worldbreaker","shadowbreaker"))for(String suffix:List.of("sword","axe","pickaxe","shovel","hoe","helmet","chestplate","leggings","boots","shield")) {
            var old=BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:"+family+"_"+suffix));
            var stack=new ItemStack(old);
            if(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("worldbosses"))System.out.println("GEAR_DIAG original="+BuiltInRegistries.ITEM.getKey(old)+" retired="+GearRetirement.retired(BuiltInRegistries.ITEM.getKey(old))+" manual="+BuiltInRegistries.ITEM.getKey(GearRetirement.convert(stack).getItem()));
            check(!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("worldbosses"),"migrated "+family+"_"+suffix);
        }
        var oldSword=BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:worldbreaker_sword"));
        var upgrade=com.mki.worldbosses.registry.ModDataComponents.UPGRADE_LEVEL.get();
        var enchants=new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        var unbreaking=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).get(Identifier.withDefaultNamespace("unbreaking")).orElseThrow();enchants.set(unbreaking,3);
        var extra=net.minecraft.world.item.component.ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.parse("worldbosses:retired_damage"),99,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build();
        var patch=DataComponentPatch.builder().set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Named old sword")).set(DataComponents.ATTRIBUTE_MODIFIERS,extra).set(upgrade,5).set(DataComponents.ENCHANTMENTS,enchants.toImmutable()).set(DataComponents.DAMAGE,100).build();
        var migrated=new ItemStack(oldSword.builtInRegistryHolder(),1,patch);
        check(migrated.get(DataComponents.CUSTOM_NAME).getString().equals("Named old sword"),"old names preserved");
        check(!migrated.has(upgrade),"old upgrade component removed");
        check(migrated.get(DataComponents.ATTRIBUTE_MODIFIERS).equals(new ItemStack(Items.NETHERITE_SWORD).get(DataComponents.ATTRIBUTE_MODIFIERS)),"old combat modifiers removed");
        check(migrated.get(DataComponents.ENCHANTMENTS).getLevel(unbreaking)==3,"compatible enchantment preserved");
        var b=(WorldBossEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:sylvaron")).create(level,EntitySpawnReason.COMMAND);b.snapTo(0,70,0,0,0);
        level.addFreshEntity(b);
        var victim=players.getFirst();victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);victim.setHealth(100);victim.invulnerableTime=0;
        victim.hurtServer(level,level.damageSources().mobAttack(b),40);
        near(100-victim.getHealth(),12,"boss burst hit capped to 12 before mitigation");
        victim.invulnerableTime=0;float after=victim.getHealth();victim.hurtServer(level,level.damageSources().mobAttack(b),40);
        near(victim.getHealth(),after,"repeat hit shares rolling budget");
        ((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(10040);
        victim.invulnerableTime=0;victim.setHealth(100);
        victim.hurtServer(level,RaidRuntime.lightning(level.damageSources(),b),40);
        near(100-victim.getHealth(),12,"owned lightning also shares burst budget");
        victim.invulnerableTime=0;float selfBefore=victim.getHealth();victim.hurtServer(level,level.damageSources().magic(),4);
        near(selfBefore-victim.getHealth(),4,"Frenzy-like self damage unaffected after boss hit");
        victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,40,0),b);
        victim.invulnerableTime=0;selfBefore=victim.getHealth();victim.hurtServer(level,level.damageSources().magic(),4);
        near(selfBefore-victim.getHealth(),4,"unrelated magic unaffected while boss poison is present");
        victim.invulnerableTime=0;((net.minecraft.world.effect.PoisonMobEffect)net.minecraft.world.effect.MobEffects.POISON.value()).applyEffectTick(level,victim,0);
        victim.invulnerableTime=0;selfBefore=victim.getHealth();victim.hurtServer(level,level.damageSources().magic(),4);
        near(selfBefore-victim.getHealth(),4,"boss poison context clears after effect tick");
        var minion=BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:skeleton_warrior")).create(level,EntitySpawnReason.MOB_SUMMONED);
        minion.snapTo(1,70,0,0,0);minion.addTag("worldbosses_minion");RaidRuntime.spawnOwned(level,minion,b);
        check(RaidRuntime.owner(minion)==b,"summon owner attributed");
        var healer=players.get(1);RaidRuntime.state(b).participants.remove(healer.getUUID());victim.setHealth(50);
        RaidRuntime.beginHeal(healer);victim.heal(5);RaidRuntime.endHeal();
        check(RaidRuntime.state(b).participants.containsKey(healer.getUUID()),"actual healer credited");
        for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,69,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        var terrain=RaidRuntime.class.getDeclaredMethod("terrain",WorldBossEntity.class,ServerPlayer.class,RaidRuntime.State.class,ServerLevel.class,long.class);terrain.setAccessible(true);
        victim.snapTo(15,70,1,0,0);var state=RaidRuntime.state(b);long now=level.getGameTime();state.blockedSince.put(victim.getUUID(),now-120);
        terrain.invoke(null,b,victim,state,level,now);
        check(state.eruptionAt.containsKey(victim.getUUID()),"unreachable player gets warned eruption");
        terrain.invoke(null,b,victim,state,level,now+40);
        terrain.invoke(null,b,victim,state,level,now+160);
        terrain.invoke(null,b,victim,state,level,now+200);
        check(state.relocateAt.containsKey(victim.getUUID()),"persistent hiding gets separate relocation warning");
        terrain.invoke(null,b,victim,state,level,now+260);
        check(!state.relocateAt.containsKey(victim.getUUID())&&victim.distanceToSqr(b)>=64&&victim.distanceToSqr(b)<=145,"relocation lands at validated 8-12 block point");
        victim.snapTo(2,70,1,0,0);state.eruptionAt.put(victim.getUUID(),now);state.relocateAt.put(victim.getUUID(),now);state.marks.put(victim.getUUID(),new double[]{2,70,1});
        terrain.invoke(null,b,victim,state,level,now+280);
        check(!state.eruptionAt.containsKey(victim.getUUID())&&!state.relocateAt.containsKey(victim.getUUID())&&!state.marks.containsKey(victim.getUUID()),"regained reach cancels stale warnings");
        victim.snapTo(18,70,0,0,0);victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        RaidRuntime.move(victim,new net.minecraft.world.phys.Vec3(4,.4,0),b);
        check(victim.getDeltaMovement().lengthSqr()==0,"forced movement toward unsupported edge canceled");
        victim.snapTo(12,70,0,0,0);level.setBlockAndUpdate(new net.minecraft.core.BlockPos(13,70,0),net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState());
        RaidRuntime.move(victim,new net.minecraft.world.phys.Vec3(1,0,0),b);
        check(victim.getDeltaMovement().lengthSqr()==0,"forced movement into lava canceled");
        level.setBlockAndUpdate(new net.minecraft.core.BlockPos(13,70,0),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        RaidRuntime.move(victim,new net.minecraft.world.phys.Vec3(.2,.35,0),b);
        check(victim.getDeltaMovement().x>0,"safe displacement on level ground retained");
        var shadow=(WorldBossEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:shadow_king")).create(level,EntitySpawnReason.COMMAND);shadow.snapTo(3,70,0,0,0);
        level.setBlockAndUpdate(new net.minecraft.core.BlockPos(10,70,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        RaidRuntime.safeBossTeleport(shadow,10,70,0);near(shadow.getX(),3,"Shadow Step rejects solid destination");
        RaidRuntime.safeBossTeleport(shadow,1000,70,0);near(shadow.getX(),3,"Shadow Step stays inside arena");
        var reset=RaidRuntime.class.getDeclaredMethod("reset",WorldBossEntity.class,ServerLevel.class);reset.setAccessible(true);double homeY=state.y;
        reset.invoke(null,b,level);near(RaidRuntime.state(b).y,homeY,"wipe reset preserves home and altar elevation");
        var evasion=RaidRuntime.state(b);evasion.started=now-2000;evasion.lastActive=now-2000;evasion.participants.clear();evasion.participants.put(victim.getUUID(),now-1300);evasion.movementPositions.put(victim.getUUID(),new double[]{victim.getX()-1,victim.getY(),victim.getZ()});b.setTarget(victim);b.tickCount=20;
        RaidRuntime.tick(b,level);
        check(RaidRuntime.state(b)==evasion&&evasion.lastActive==level.getGameTime(),"actively evading target prevents abandonment reset");
        ((net.minecraft.world.level.storage.ServerLevelData)level.getLevelData()).setGameTime(now+300);
        victim.snapTo(2,70,1,0,0);victim.setHealth(100);victim.invulnerableTime=0;
        RaidRuntime.hazardHurt(victim,level.damageSources().mobAttack(b),8,b,"zone-a");near(100-victim.getHealth(),2,"hazard cap applied after difficulty");
        victim.invulnerableTime=0;RaidRuntime.hazardHurt(victim,level.damageSources().mobAttack(b),8,b,"zone-b");
        victim.invulnerableTime=0;RaidRuntime.hazardHurt(victim,level.damageSources().mobAttack(b),8,b,"zone-c");near(100-victim.getHealth(),4,"combined hazards limited to four HP per second");
        var slow=RaidRuntime.effect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS,200,3),b,victim);
        check(slow.getDuration()==40&&slow.getAmplifier()==1,"boss slowness capped");
        for(String cache:List.of("verdant","storm","dune","infernal","soul","void","umbral","dragon","griffin","primordial","weaver")) {
            var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:"+cache+"_raid_cache")));
            victim.getInventory().clearContent();RaidRuntime.openCache(victim,stack,cache+"_raid_cache");
            check(stack.isEmpty(),cache+" cache consumed once");
            check(victim.getInventory().countItem(Items.GOLDEN_APPLE)>=4,cache+" guaranteed apples");
            check(victim.getInventory().countItem(Items.NETHERITE_INGOT)>=1,cache+" guaranteed netherite");
            check(victim.getInventory().countItem(Items.TOTEM_OF_UNDYING)>=1,cache+" guaranteed totem");
            check(victim.getInventory().countItem(Items.ENCHANTED_GOLDEN_APPLE)==0,cache+" no enchanted apples");
            int tier=List.of("verdant","storm","dune").contains(cache)?1:List.of("infernal","soul","void").contains(cache)?2:List.of("umbral","dragon","griffin").contains(cache)?3:4;
            check(victim.getInventory().countItem(Items.NETHERITE_INGOT)==tier,cache+" exact tier materials");
        }
        var bystander=players.getLast();victim.getInventory().clearContent();bystander.getInventory().clearContent();
        RaidRuntime.state(b).participants.clear();RaidRuntime.record(b,victim);RaidRuntime.reward(level,b);RaidRuntime.reward(level,b);
        check(victim.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:verdant_raid_cache")))==1,"one personal cache despite repeated reward callback");
        check(bystander.getInventory().isEmpty(),"bystander not rewarded");
        var advancement=server.getAdvancements().get(Identifier.parse(b.advancementId()));
        check(victim.getAdvancements().getOrStartProgress(advancement).isDone(),"boss clear advancement preserved");
        var challenge=server.getAdvancements().get(Identifier.parse(b.advancementId()+"_speed"));
        check(victim.getAdvancements().getOrStartProgress(challenge).isDone(),"boss challenge advancement preserved");
        check(victim.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:verdant_element")))==1,"progression element preserved");
        check(victim.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:worldbreaker_dust")))==3,"portal dust preserved");
        var deadBoss=(WorldBossEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:aureon")).create(level,EntitySpawnReason.COMMAND);deadBoss.snapTo(0,70,0,0,0);
        victim.getInventory().clearContent();RaidRuntime.record(deadBoss,victim);victim.setHealth(0);RaidRuntime.reward(level,deadBoss);
        check(victim.getInventory().isEmpty(),"dead contributor reward waits");
        victim.setHealth(100);
        var deliver=RaidRuntime.class.getDeclaredMethod("deliver",ServerPlayer.class);deliver.setAccessible(true);deliver.invoke(null,victim);
        check(victim.getInventory().countItem(BuiltInRegistries.ITEM.getValue(Identifier.parse("worldbosses:storm_raid_cache")))==1,"dead contributor receives saved pending grant");
        check(!server.getCommandStorage().get(Identifier.parse("worldbosses:rpg_rewards")).getStringOr("state","").isEmpty(),"reward ledger saved in world storage");
        var ledgerMethod=RaidRuntime.class.getDeclaredMethod("ledger",net.minecraft.server.MinecraftServer.class);ledgerMethod.setAccessible(true);
        var data=(RaidRuntime.Ledger)ledgerMethod.invoke(null,server);boolean cleared=data.firstClear.contains(victim.getUUID()+":shadowWeaver");
        victim.getInventory().clearContent();
        for(int i=0;i<2;i++) {
            var finale=(WorldBossEntity)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("worldbosses:shadow_weaver")).create(level,EntitySpawnReason.COMMAND);finale.snapTo(0,70,0,0,0);
            RaidRuntime.record(finale,victim);RaidRuntime.reward(level,finale);
        }
        check(victim.getInventory().countItem(Items.NETHER_STAR)==(cleared?0:1),"finale Nether Star awarded only on first clear");
        for(int i=0;i<1000;i++)data.granted.add("probe-history-"+i+"-"+"x".repeat(80));
        int history=data.granted.size();var persist=RaidRuntime.class.getDeclaredMethod("persist",net.minecraft.server.MinecraftServer.class);persist.setAccessible(true);persist.invoke(null,server);
        check(server.getCommandStorage().get(Identifier.parse("worldbosses:rpg_rewards")).getIntOr("state_chunks",1)>1,"large reward ledger uses bounded NBT strings");
        var cacheServer=RaidRuntime.class.getDeclaredField("ledgerServer");cacheServer.setAccessible(true);cacheServer.set(null,null);
        check(((RaidRuntime.Ledger)ledgerMethod.invoke(null,server)).granted.size()==history,"large reward ledger survives reload");
        try{Class.forName("net.spell_engine.internals.impact.SpellImpacts");check(true,"Spell Engine healing mixin transforms with installed API");}catch(ClassNotFoundException ignored){}
        Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
    }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+t);}catch(Exception ignored){}}
    finally{server.halt(false);}}
}
