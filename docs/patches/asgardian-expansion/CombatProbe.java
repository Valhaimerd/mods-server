package com.emandev.bifrost;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.eiden.iourusraces.data.PlayerRaceState;
import com.eiden.iourusraces.race.Race;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class CombatProbe {
    public static final class CombatPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        CombatPlayer(ServerLevel world,GameProfile profile){super(world,profile);setInvulnerable(false);getAbilities().invulnerable=false;try{for(String name:List.of("clientLoadedTimeoutTimer","waitingForRespawn")){var field=net.minecraft.server.network.ServerGamePacketListenerImpl.class.getDeclaredField(name);field.setAccessible(true);if(field.getType()==int.class)field.setInt(connection,0);else field.setBoolean(connection,false);}}catch(Exception e){throw new IllegalStateException(e);}}
        @Override public boolean canHarmPlayer(net.minecraft.world.entity.player.Player other){return level().isPvpAllowed()&&(getTeam()==null||!getTeam().isAlliedTo(other.getTeam())||getTeam().isAllowFriendlyFire());}
    }
    static List<String> out;static ServerLevel world;static ServerPlayer p;static LivingEntity target;static long time;
    static void ok(String name,boolean value){if(!value)throw new AssertionError(name);out.add("PASS combat: "+name);}
    static void near(String name,double got,double expected){ok(name+" "+got+" ~= "+expected,Math.abs(got-expected)<.025);}
    static void now(long value){((ServerLevelData)world.getLevelData()).setGameTime(value);}
    static void fresh(){AsgardAbilities.reset();time+=3000;now(time);p.removeAllEffects();p.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(AsgardAbilities.MOVE);p.setPos(0,65,0);p.setYRot(0);p.setXRot(0);p.setHealth(p.getMaxHealth());target.setPos(0,65,5);target.setHealth(target.getMaxHealth());target.invulnerableTime=0;}
    static void tick(int ticks){long start=world.getGameTime();for(int i=1;i<=ticks;i++){now(start+i);target.invulnerableTime=Math.max(0,target.invulnerableTime-1);AsgardAbilities.tick(world.getServer());}}
    static void held(ItemStack item){p.getInventory().setItem(0,item);p.getInventory().setSelectedSlot(0);}
    @SuppressWarnings("unchecked") public static void run(MinecraftServer server,List<String> output)throws Exception{
        out=output;world=server.overworld();time=world.getGameTime()+1000;
        for(int x=-10;x<=10;x++)for(int z=-10;z<=20;z++){world.getChunk(x>>4,z>>4);world.setBlockAndUpdate(new BlockPos(x,64,z),Blocks.STONE.defaultBlockState());for(int y=65;y<=72;y++)world.setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());}
        p=new CombatPlayer(world,new GameProfile(UUID.randomUUID(),"CombatProbe"));p.setGameMode(GameType.SURVIVAL);p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(40);PlayerRaceState.get(server).setRace(p.getUUID(),Race.ASGARDIAN);
        target=EntityTypes.VILLAGER.create(world,EntitySpawnReason.COMMAND);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000);target.setHealth(2000);world.addFreshEntity(target);
        var field=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");field.setAccessible(true);var players=(Map<UUID,ServerPlayer>)field.get(server.getPlayerList());players.put(p.getUUID(),p);
        try {
            for(var pair:List.of(new Object[]{ModItems.HEIMDALLS_SWORD.get(),8d,1.5d},new Object[]{ModItems.THORS_HAMMER.get(),9d,1.2d},new Object[]{ModItems.STORMBREAKER.get(),10d,1d})){
                var stack=new ItemStack((Item)pair[0]);var mods=stack.get(DataComponents.ATTRIBUTE_MODIFIERS);near("ordinary weapon damage "+stack.getItem(),mods.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND),(Double)pair[1]);near("ordinary attack speed "+stack.getItem(),mods.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND),(Double)pair[2]);
            }
            ok("Mjolnir unbreakable",new ItemStack(ModItems.THORS_HAMMER.get()).has(DataComponents.UNBREAKABLE));ok("Stormbreaker unbreakable",new ItemStack(ModItems.STORMBREAKER.get()).has(DataComponents.UNBREAKABLE));
            held(new ItemStack(ModItems.HEIMDALLS_SWORD.get()));fresh();
            ok("passive NPCs are eligible",AsgardCombat.eligible(p,target));
            var ally=new CombatPlayer(world,new GameProfile(UUID.randomUUID(),"PartyAlly"));ally.setGameMode(GameType.SURVIVAL);ally.setPos(3,65,3);
            var enemyPlayer=new CombatPlayer(world,new GameProfile(UUID.randomUUID(),"EnemyPlayer"));enemyPlayer.setGameMode(GameType.SURVIVAL);enemyPlayer.setPos(4,65,4);PlayerRaceState.get(server).setRace(enemyPlayer.getUUID(),Race.HUMAN);
            var manager=dev.betterparty.party.PartyManager.forServer(server);var partiesField=manager.getClass().getDeclaredField("parties");partiesField.setAccessible(true);var membershipsField=manager.getClass().getDeclaredField("playerParties");membershipsField.setAccessible(true);
            var parties=(Map<UUID,dev.betterparty.party.Party>)partiesField.get(manager);var memberships=(Map<UUID,UUID>)membershipsField.get(manager);UUID partyId=UUID.randomUUID();var party=new dev.betterparty.party.Party(partyId);
            party.addMember(new dev.betterparty.party.PartyMember(p.getUUID(),"CombatProbe",dev.betterparty.party.PartyRole.LEADER));party.addMember(new dev.betterparty.party.PartyMember(ally.getUUID(),"PartyAlly",dev.betterparty.party.PartyRole.MEMBER));parties.put(partyId,party);memberships.put(p.getUUID(),partyId);memberships.put(ally.getUUID(),partyId);
            try {
                for(boolean friendly:new boolean[]{false,true}){party.setFriendlyFire(friendly);ok("party member always protected FF="+friendly,!AsgardCombat.eligible(p,ally));double hp=ally.getHealth();ok("party hit denied FF="+friendly,!AsgardCombat.hitRaw(p,ally,10,true));near("party health unchanged",ally.getHealth(),hp);}
                out.add("PvP fixture flags: world="+world.isPvpAllowed()+" canHarm="+p.canHarmPlayer(enemyPlayer)+" invulnerable="+enemyPlayer.isInvulnerable()+" downed="+AsgardianAccess.downed(enemyPlayer));
                ok("non-party player eligible",AsgardCombat.eligible(p,enemyPlayer));enemyPlayer.invulnerableTime=0;double hp=enemyPlayer.getHealth();ok("native player physical hit accepted",AsgardCombat.hitRaw(p,enemyPlayer,10,false));near("native PvP physical scales exactly once",hp-enemyPlayer.getHealth(),5.1);
                enemyPlayer.invulnerableTime=0;hp=enemyPlayer.getHealth();ok("native player lightning hit accepted",AsgardCombat.hitRaw(p,enemyPlayer,10,true));near("native PvP lightning scales exactly once",hp-enemyPlayer.getHealth(),5.1);
            }finally{parties.remove(partyId);memberships.remove(p.getUUID());memberships.remove(ally.getUUID());}
            target.getAttribute(Attributes.SCALE).setBaseValue(4);target.refreshDimensions();p.setXRot(-45);ok("beam ray intersects tall target head hitbox",AsgardCombat.line(p,16,.05,1).contains(target));target.getAttribute(Attributes.SCALE).setBaseValue(1);target.refreshDimensions();p.setXRot(0);
            world.setBlockAndUpdate(new BlockPos(0,66,2),Blocks.STONE.defaultBlockState());ok("beam cannot pass through a solid wall",AsgardCombat.line(p,16,.35,1).isEmpty());world.setBlockAndUpdate(new BlockPos(0,66,2),Blocks.AIR.defaultBlockState());
            var wolf=EntityTypes.WOLF.create(world,EntitySpawnReason.COMMAND);wolf.setOwnerReference(EntityReference.of(UUID.randomUUID()));wolf.setPos(0,65,3);world.addFreshEntity(wolf);ok("another player's dog protected",!AsgardCombat.eligible(p,wolf));wolf.discard();
            near("Realm cooldown",AsgardAbilities.cooldownTicks(p,"realm"),700);near("Berserk cooldown",AsgardAbilities.cooldownTicks(p,"berserk"),600);
            var haste=p.getAttribute(net.spell_power.api.SpellPowerMechanics.HASTE.attributeEntry);double originalHaste=haste.getBaseValue();
            haste.setBaseValue(400);double fastHaste=AsgardAbilities.haste(p);ok("native haste increases combat cast speed",fastHaste>1);
            near("Realm haste floor75%",AsgardAbilities.cooldownTicks(p,"realm"),Math.ceil(700*Math.max(.75,1/fastHaste)));
            near("Berserk haste floor75%",AsgardAbilities.cooldownTicks(p,"berserk"),Math.ceil(600*Math.max(.75,1/fastHaste)));
            near("ordinary cooldown scales with native haste",AsgardAbilities.cooldownTicks(p,"wave"),Math.ceil(240/fastHaste));
            var hasteAction=new AsgardAbilities.Action(p,"wave");near("windup scales with native haste",hasteAction.windup,Math.max(1,Math.ceil(16/fastHaste)));haste.setBaseValue(originalHaste);
            ok("wave starts",AsgardAbilities.beginPrimary(p));ok("new actions cannot overlap",!AsgardAbilities.activate(p,1));
            var cast=net.spell_engine.internals.casting.SpellCasting.attempt(p,p.getMainHandItem(),Identifier.parse("wizards:arcane_missiles"),false);ok("class casts blocked during weapon channel",!cast.isSuccess());
            double before=target.getHealth();AsgardAbilities.endPrimary(p);near("wave first target 160% raw plus racial once",before-target.getHealth(),40*1.6*1.02);
            ok("wave cooldown prevents tap spam",!AsgardAbilities.beginPrimary(p));
            fresh();ok("release invalidation fixture starts",AsgardAbilities.beginPrimary(p));held(new ItemStack(Items.STICK));before=target.getHealth();AsgardAbilities.endPrimary(p);near("swap then release causes no damage",target.getHealth(),before);
            held(new ItemStack(ModItems.HEIMDALLS_SWORD.get()));fresh();ok("Realm starts",AsgardAbilities.activate(p,1));tick(7);ok("Realm slowness applied",target.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS));ok("Realm ordinary level IV",target.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS).getAmplifier()==3);target.removeAllEffects();
            fresh();target.setPos(0,65,2);ok("bash starts",AsgardAbilities.activate(p,2));before=target.getHealth();tick(1);near("bash 60%",before-target.getHealth(),40*.6*1.02);ok("bash applies finite stun",target.hasEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("spell_engine:stun")).orElseThrow()));target.removeAllEffects();
            fresh();ok("Berserk denied above threshold",!AsgardAbilities.activate(p,3));p.setHealth(20);ok("Berserk below threshold",AsgardAbilities.activate(p,3));tick(32);ok("Berserk finishes",!AsgardAbilities.active(p));
            fresh();held(new ItemStack(ModItems.THORS_HAMMER.get()));ok("explicit hammer claim",WeaponOwnership.claim(p));
            ok("beam starts",AsgardAbilities.beginPrimary(p));var a=AsgardAbilities.ACTIVE.get(p.getUUID());tick(60);ok("beam per-target raw cap",a.dealt.values().stream().allMatch(v->v<=2.500001));ok("beam naturally ends",!AsgardAbilities.active(p));
            fresh();ok("shards start",AsgardAbilities.activate(p,1));a=AsgardAbilities.ACTIVE.get(p.getUUID());tick(30);ok("shards per-target cap90%",a.dealt.values().stream().allMatch(v->v<=.900001));ok("shards end",!AsgardAbilities.active(p));
            fresh();ok("shield strike requires offhand shield",!AsgardAbilities.activate(p,2));p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD));ok("shield strike starts with shield",AsgardAbilities.activate(p,2));a=AsgardAbilities.ACTIVE.get(p.getUUID());tick(14);ok("shield strike per-target cap",a.dealt.values().stream().allMatch(v->v<=1.700001));
            fresh();ok("vortex starts",AsgardAbilities.activate(p,3));a=AsgardAbilities.ACTIVE.get(p.getUUID());target.setPos(0,65,3);tick(72);ok("vortex three pulse cap",a.dealt.values().stream().allMatch(v->v<=1.050001));
            fresh();held(new ItemStack(ModItems.STORMBREAKER.get()));ok("explicit axe claim",WeaponOwnership.claim(p));p.setHealth(20);ok("slam starts",AsgardAbilities.activate(p,1));a=AsgardAbilities.ACTIVE.get(p.getUUID());target.setPos(0,65,2);tick(16);ok("slam at most180%",!a.dealt.isEmpty()&&a.dealt.values().stream().allMatch(v->v<=1.800001));ok("neutral-target slam cannot farm healing",AsgardAbilities.RECOVERY.isEmpty());ok("slam no terrain destruction",world.getBlockState(new BlockPos(0,64,0)).is(Blocks.STONE));
            fresh();p.setHealth(20);var enemy=EntityTypes.ZOMBIE.create(world,EntitySpawnReason.COMMAND);enemy.setPos(2,65,2);world.addFreshEntity(enemy);
            for(int i=0;i<5;i++)AsgardAbilities.lightningHit(p,enemy,"testcast"+i);near("five charges do not heal until next qualifying hit",p.getHealth(),20);AsgardAbilities.lightningHit(p,enemy,"testcast5");near("Thunder heals2HP",p.getHealth(),22);ok("shared healing lockout persists",AsgardState.get(p,"healing/shared")==AsgardState.now(p)+200);
            AsgardAbilities.stormRecovery(p,p.getMainHandItem());ok("shared lockout prevents other passive",AsgardAbilities.RECOVERY.isEmpty());now(AsgardState.now(p)+201);AsgardAbilities.stormRecovery(p,p.getMainHandItem());tick(80);near("Storm recovery heals4HP",p.getHealth(),26);ok("shared recovery lock lasts10s after final healing tick",AsgardState.get(p,"healing/shared")==AsgardState.now(p)+200);enemy.discard();
            ok("invalid action rejected",!AsgardNetwork.handle(p,99,0));ok("invalid skill rejected",!AsgardNetwork.handle(p,0,99));ok("forged primary rejected while not using item",!AsgardNetwork.handle(p,1,0));
            PlayerRaceState.get(server).setRace(p.getUUID(),Race.HUMAN);ok("normal race action rejected",!AsgardNetwork.handle(p,0,0));ok("release allowed after race loss",AsgardNetwork.handle(p,2,0));
            var encoded=AsgardState.get(server).encode();var decoded=AsgardState.decode(encoded);ok("ability cooldown and healing save codec",decoded.value(p.getUUID(),"storm/heal")==AsgardState.get(p,"storm/heal"));
        }finally{players.remove(p.getUUID());target.discard();AsgardAbilities.reset();}
    }
}
