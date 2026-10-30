package local.accessory;

import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import com.cozary.nameless_trinkets.items.trinkets.MoonStoneBase;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import com.cozary.nameless_trinkets.pack.PackBalance;
import com.cozary.nameless_trinkets.events.*;

@Mod("accessory_probe")
public class Probe {
    int checks; int ticks;
    public Probe() { NeoForge.EVENT_BUS.addListener(this::tick); }
    void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) { if(++ticks==1)event.getServer().overworld().setChunkForced(0,0,true); if(ticks==60)run(new ServerStartedEvent(event.getServer())); }
    void check(boolean value,String label) {
        if (!value) throw new AssertionError(label);
        checks++; System.out.println("ACCESSORY_PASS " + label);
    }
    void near(double actual,double expected,String label) { check(Math.abs(actual-expected)<.001,label+" = "+actual); }
    void run(ServerStartedEvent event) {
        try {
            near(MoonStoneBase.INSTANCE.getTrinketConfig().gravityValue,-.02625,"Moon Stone 3/8 gravity reduction");
            near(FracturedNullstoneHandler.reduceMagicDamage(event.getServer().overworld().damageSources().magic(),10),8.5,"Nullstone vanilla magic reduction");
            policy();
            var level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);
            var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(level);
            player.setPos(0,65,0);player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            characterScaling(level,player);
            var support=Class.forName("com.cozary.nameless_trinkets.pack.AccessoryCombat");
            var scaled=support.getMethod("scaled",LivingEntity.class,double.class,double.class,double.class);
            near(((Number)scaled.invoke(null,player,1.,.1,6.)).doubleValue(),3,"20 power gives 3 Missing Page damage");
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(1000);
            near(((Number)scaled.invoke(null,player,1.,.1,6.)).doubleValue(),6,"endgame Missing Page cap");
            player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
            var ready=support.getMethod("ready",LivingEntity.class,String.class,int.class);
            check((boolean)ready.invoke(null,player,"swap_test",80),"first proc ready");
            check(!(boolean)ready.invoke(null,player,"swap_test",80),"re-equipping cannot reset wearer cooldown");
            var copied=player.getPersistentData().copy();check(copied.getLongOr("nameless_trinkets:proc_swap_test",0)>level.getGameTime(),"cooldown stored in serializable entity data");
            player.getPersistentData().putLong("nameless_trinkets:proc_swap_test",level.getGameTime());
            check((boolean)ready.invoke(null,player,"swap_test",80),"exact cooldown boundary");
            var boss=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);boss.setPos(2,65,0);
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);boss.setHealth(10000);level.addFreshEntity(boss);
            var ordinary=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);ordinary.setPos(3,65,0);level.addFreshEntity(ordinary);

            float hp=boss.getHealth(),other=ordinary.getHealth();
            MissingPageHandler.triggerDamageReflection(player,boss.damageSources().mobAttack(boss),player);
            near(hp-boss.getHealth(),other-ordinary.getHealth(),"boss and ordinary targets take identical bounded Page damage");
            check(hp-boss.getHealth()>0 && hp-boss.getHealth()<=3,"Page deals bounded damage after armor");
            float after=boss.getHealth();MissingPageHandler.triggerDamageReflection(player,boss.damageSources().mobAttack(boss),player);
            near(boss.getHealth(),after,"Page cooldown suppresses repeated sourced hits");
            var data=boss.getLastDamageSource();check(data!=null && data.getEntity()==player,"proc damage retains player attribution");
            check((boolean)support.getMethod("proc",net.minecraft.world.damagesource.DamageSource.class).invoke(null,data),"proc source has shared recursion tag");
            player.getPersistentData().remove("nameless_trinkets:proc_missing_page");
            MissingPageHandler.triggerDamageReflection(player,data,player);near(boss.getHealth(),after,"tagged retaliation cannot trigger Page");
            var wolf=EntityTypes.WOLF.create(level,EntitySpawnReason.COMMAND);
            wolf.setOwnerReference(net.minecraft.world.entity.EntityReference.of(player));
            check((boolean)support.getMethod("friendly",LivingEntity.class,net.minecraft.world.entity.Entity.class).invoke(null,player,wolf),"own pet protected");
            var team=event.getServer().getScoreboard().addPlayerTeam("accessory_test");
            event.getServer().getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
            event.getServer().getScoreboard().addPlayerToTeam(ordinary.getScoreboardName(),team);
            check((boolean)support.getMethod("friendly",LivingEntity.class,net.minecraft.world.entity.Entity.class).invoke(null,player,ordinary),"scoreboard ally protected");
            player.setHealth(1);VampireBloodHandler.function(player,boss);near(player.getHealth(),4,"Vampire heal depends on wearer HP, not boss HP");
            VampireBloodHandler.function(player,boss);near(player.getHealth(),4,"Vampire heal interval");
            GhastEyeHandler.obtainRegenOnKill(player);GhastEyeHandler.obtainRegenOnKill(player);
            check(player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getDuration()==100,"Ghast regeneration refreshes without adding duration");
            var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).orElseThrow();inventory.loadDatapacks();
            var moon=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.MOON_STONE.get());
            var context=new top.theillusivec4.curios.api.SlotContext("hands",player,0,false,true);
            var gravity=player.getAttribute(Attributes.GRAVITY);var gravityId=Identifier.parse("nameless_trinkets:moon_stone_gravity");
            gravity.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(gravityId,-.07,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
            player.tickCount=60;((top.theillusivec4.curios.api.type.capability.ICurioItem)moon.getItem()).curioTick(context,moon);
            near(gravity.getValue(),.05375,"already-equipped Moon Stone migrates old gravity modifier");
            ((top.theillusivec4.curios.api.type.capability.ICurioItem)moon.getItem()).onUnequip(context,ItemStack.EMPTY,moon);
            near(gravity.getValue(),.08,"Moon Stone removal restores gravity");
            check(MoonStoneHandler.moonStoneFallDamage(player,level.damageSources().fall()),"Moon Stone fall protection retained");
            var feet=new top.theillusivec4.curios.api.SlotContext("feet",player,0,false,true);
            check(top.theillusivec4.curios.api.CuriosApi.isStackValid(feet,new ItemStack(artifacts.registry.ModItems.STEADFAST_SPIKES.get())),"Spikes still fit Feet");
            check(inventory.getStacksHandler("feet").isPresent(),"Feet slot retained");
            var hands=inventory.getStacksHandler("hands").orElseThrow().getStacks();
            hands.setStackInSlot(0,new ItemStack(artifacts.registry.ModItems.VAMPIRIC_GLOVE.get()));
            player.setHealth(1);
            artifacts.event.ArtifactHooks.absorbDamage(boss,boss.damageSources().playerAttack(player),40);
            near(player.getHealth(),3,"Glove heals at most 2 HP");
            artifacts.event.ArtifactHooks.absorbDamage(boss,boss.damageSources().playerAttack(player),40);
            near(player.getHealth(),3,"Glove 1s gate shared across hits");
            hands.setStackInSlot(0,new ItemStack(artifacts.registry.ModItems.VAMPIRIC_GLOVE.get()));
            artifacts.event.ArtifactHooks.absorbDamage(boss,boss.damageSources().playerAttack(player),40);
            near(player.getHealth(),3,"fresh glove stack cannot bypass cooldown");
            player.getPersistentData().remove("artifacts:proc_vampiric_glove");boss.setHealth(1);
            artifacts.event.ArtifactHooks.absorbDamage(boss,boss.damageSources().playerAttack(player),1000);
            near(player.getHealth(),3.05,"overkill limited to remaining victim HP");
            player.getPersistentData().remove("artifacts:proc_vampiric_glove");
            artifacts.event.ArtifactHooks.absorbDamage(boss,new net.minecraft.world.damagesource.DamageSource(data.typeHolder(),player),1000);
            near(player.getHealth(),3.05,"accessory procs cannot life steal");
            player.getPersistentData().remove("artifacts:proc_vampiric_glove");
            artifacts.event.ArtifactHooks.absorbDamage(ordinary,ordinary.damageSources().playerAttack(player),20);
            near(player.getHealth(),3.05,"ally hits cannot life steal");
            hands.setStackInSlot(0,ItemStack.EMPTY);
            var thorn=new artifacts.component.ability.retaliation.ThornsEffect(
                new artifacts.component.ability.retaliation.RetaliationEffect.ActivationParams(artifacts.config.value.Value.of(1D),artifacts.config.value.Value.of(4),artifacts.config.value.Value.of(0)),
                artifacts.config.value.Value.of(1),artifacts.config.value.Value.of(3));
            var pendant=new ItemStack(artifacts.registry.ModItems.THORN_PENDANT.get());
            var slot=new artifacts.equipment.EquipmentSlotAccess(){
                public ItemStack get(){return pendant;} public LivingEntity entity(){return player;}
                public void broadcastBreakEvent(LivingEntity ignored){}
            };
            boss.setHealth(100);boss.invulnerableTime=0;float thornBefore=boss.getHealth();
            thorn.onLivingHurt(player,slot,boss.damageSources().mobAttack(boss));
            check(boss.getHealth()<thornBefore && thornBefore-boss.getHealth()<=3,"Thorn bounded retaliation");
            boss.invulnerableTime=0;float thornAfter=boss.getHealth();
            thorn.onLivingHurt(player,slot,boss.damageSources().mobAttack(boss));near(boss.getHealth(),thornAfter,"Thorn wearer interval");
            player.getPersistentData().remove("artifacts:proc_retaliation_thorns");
            thorn.onLivingHurt(player,slot,new net.minecraft.world.damagesource.DamageSource(data.typeHolder(),boss));
            near(boss.getHealth(),thornAfter,"cross-mod retaliation cannot recurse");
            var wound=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.WOUNDBEARER.get());
            wound.set(com.cozary.nameless_trinkets.init.ModDataComponents.WOUNDBEARER_DAMAGE.get(),10F);
            ((top.theillusivec4.curios.api.type.capability.ICurioItem)wound.getItem()).curioTick(new top.theillusivec4.curios.api.SlotContext("hands",player,0,false,true),wound);
            check(wound.getOrDefault(com.cozary.nameless_trinkets.init.ModDataComponents.WOUNDBEARER_DAMAGE.get(),0F)<=2,"legacy Woundbearer storage immediately clamped");
            var sigil=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.SIGIL_OF_BAPHOMET.get());
            for(int i=0;i<10;i++)SigilOfBaphometHandler.handleSigilKillCount(sigil);
            check(sigil.getOrDefault(com.cozary.nameless_trinkets.init.ModDataComponents.SIGIL_COUNT.get(),0)==40,"Sigil actual bank capped at 2s");
            sigil.set(com.cozary.nameless_trinkets.init.ModDataComponents.SIGIL_COUNT.get(),200);
            ((top.theillusivec4.curios.api.type.capability.ICurioItem)sigil.getItem()).curioTick(context,sigil);
            check(sigil.getOrDefault(com.cozary.nameless_trinkets.init.ModDataComponents.SIGIL_COUNT.get(),0)<=40,"Sigil legacy immunity immediately clamped");
            optionalIntegrations(level,player,support);
            eclipseRegressions(level,player,context);
            var creeper=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.CREEPER_SENSE.get());
            player.getCooldowns().removeCooldown(BuiltInRegistries.ITEM.getKey(creeper.getItem()));player.setShiftKeyDown(true);player.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            level.setBlock(player.blockPosition().above().above(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),3);
            player.getPersistentData().remove("nameless_trinkets:proc_creeper_sense");
            ((top.theillusivec4.curios.api.type.capability.ICurioItem)creeper.getItem()).curioTick(context,creeper);
            check(player.getPersistentData().getLongOr("nameless_trinkets:proc_creeper_sense",0)>level.getGameTime(),"Creeper cooldown survives relog in entity data");
            Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
        } catch(Throwable failure) {
            failure.printStackTrace();
            try { Files.writeString(Path.of("result.txt"),"FAIL "+failure); } catch(Exception ignored) {}
        } finally { event.getServer().halt(false); }
    }
    void characterScaling(ServerLevel level,net.minecraft.server.level.ServerPlayer player) throws Exception {
        var context=new top.theillusivec4.curios.api.SlotContext("hands",player,0,false,true);
        var titan=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.TITANS_MARK.get());
        var item=(top.theillusivec4.curios.api.type.capability.ICurioItem)titan.getItem();
        player.setHealth(1);item.curioTick(context,titan);
        near(player.getMaxHealth(),23,"Titan scales from 20 HP to add 3 HP");
        com.cozary.nameless_trinkets.items.trinkets.TitansMarkBase.INSTANCE.getTrinketConfig().isEnable=false;
        item.curioTick(context,titan);near(player.getMaxHealth(),20,"disabled Titan clears owned health bonus");
        com.cozary.nameless_trinkets.items.trinkets.TitansMarkBase.INSTANCE.getTrinketConfig().isEnable=true;item.curioTick(context,titan);
        var ghast=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.GHAST_EYE.get());
        var ghastItem=(top.theillusivec4.curios.api.type.capability.ICurioItem)ghast.getItem();
        ghastItem.curioTick(context,ghast);
        var heart=new ItemStack(artifacts.registry.ModItems.CRYSTAL_HEART.get());
        var heartAbility=heart.get(artifacts.registry.ModDataComponents.ATTRIBUTE_MODIFIERS.get()).entries().getFirst();
        var ticker=new artifacts.component.ability.EquipmentAttributeModifier.Ticker();
        ticker.wornTick(heartAbility,null,player,false,false);
        near(player.getMaxHealth(),26.5,"three HP accessories share one unmodified baseline");
        com.cozary.nameless_trinkets.items.trinkets.GhastEyeBase.INSTANCE.getTrinketConfig().isEnable=false;
        ghastItem.curioTick(context,ghast);near(player.getMaxHealth(),25,"disabled Ghast Eye clears owned health bonus");
        com.cozary.nameless_trinkets.items.trinkets.GhastEyeBase.INSTANCE.getTrinketConfig().isEnable=true;ghastItem.curioTick(context,ghast);
        for(int i=0;i<50;i++){item.curioTick(context,titan);ghastItem.curioTick(context,ghast);ticker.wornTick(heartAbility,null,player,false,false);}
        near(player.getMaxHealth(),26.5,"repeated HP refresh has no feedback");near(player.getHealth(),1,"HP accessories never heal on refresh");
        var health=player.getAttribute(Attributes.MAX_HEALTH);
        var outside=Identifier.parse("accessory_probe:outside_multiplier");
        health.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(outside,1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        health.setBaseValue(100);item.curioTick(context,titan);ghastItem.curioTick(context,ghast);ticker.wornTick(heartAbility,null,player,false,false);
        near(player.getMaxHealth(),224,"effective HP bonuses stay capped despite external 2x modifier");
        near(player.getHealth(),1,"raising external health does not heal");
        var baseMultiplier=Identifier.parse("accessory_probe:base_multiplier");
        health.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(baseMultiplier,.5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        item.curioTick(context,titan);ghastItem.curioTick(context,ghast);ticker.wornTick(heartAbility,null,player,false,false);
        near(player.getMaxHealth(),324,"mixed external base and total multipliers preserve effective caps");
        health.removeModifier(baseMultiplier);
        ticker.onUnequip(heartAbility,player);ghastItem.onUnequip(context,ItemStack.EMPTY,ghast);
        item.onUnequip(context,ItemStack.EMPTY,titan);
        near(player.getMaxHealth(),200,"removal cleans only accessory HP bonuses");health.removeModifier(outside);health.setBaseValue(20);
        player.removeAllEffects();
        var glove=new ItemStack(artifacts.registry.ModItems.POWER_GLOVE.get());
        var gloveAbility=glove.get(artifacts.registry.ModDataComponents.ATTRIBUTE_MODIFIERS.get()).entries().getFirst();
        ticker.wornTick(gloveAbility,null,player,false,false);near(player.getAttributeValue(Attributes.ATTACK_DAMAGE),21,"Glove adds 5% at 20 melee");
        for(int i=0;i<30;i++)ticker.wornTick(gloveAbility,null,player,false,false);
        near(player.getAttributeValue(Attributes.ATTACK_DAMAGE),21,"Glove cannot scale its own bonus");
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);ticker.wornTick(gloveAbility,null,player,false,false);
        near(player.getAttributeValue(Attributes.ATTACK_DAMAGE),102,"Glove capped at 2 attack");
        ticker.onUnequip(gloveAbility,player);player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
        var heal=new artifacts.item.consumeeffects.HealConsumeEffect(artifacts.config.value.Value.of(9));
        player.setHealth(1);heal.apply(level,new ItemStack(artifacts.registry.ModItems.CHORUS_TOTEM.get()),player);
        near(player.getHealth(),6,"Chorus heals 25% of 20 HP");
        health.setBaseValue(100);player.setHealth(1);heal.apply(level,new ItemStack(artifacts.registry.ModItems.CHORUS_TOTEM.get()),player);
        near(player.getHealth(),17,"Chorus healing capped at 16 HP");health.setBaseValue(20);player.setHealth(20);
        var wound=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.WOUNDBEARER.get());
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        WoundbearerHandler.savePlayerDamageIncrement(player,1000,wound);
        near(wound.getOrDefault(com.cozary.nameless_trinkets.init.ModDataComponents.WOUNDBEARER_DAMAGE.get(),0F),4,"Woundbearer reaches scaled 4-damage storage cap");
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)wound.getItem()).curioTick(context,wound);
        near(player.getAttributeValue(Attributes.ATTACK_DAMAGE),104,"Woundbearer applies scaled stored damage");
        com.cozary.nameless_trinkets.items.trinkets.WoundbearerBase.INSTANCE.getTrinketConfig().isEnable=false;
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)wound.getItem()).curioTick(context,wound);
        near(player.getAttributeValue(Attributes.ATTACK_DAMAGE),100,"disabled Woundbearer clears stored damage modifier");
        com.cozary.nameless_trinkets.items.trinkets.WoundbearerBase.INSTANCE.getTrinketConfig().isEnable=true;
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)wound.getItem()).onUnequip(context,ItemStack.EMPTY,wound);
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(20);
        var target=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);target.setPos(2,65,0);target.setNoAi(true);target.getAttribute(Attributes.ARMOR).setBaseValue(0);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);level.addFreshEntity(target);
        var thorn=new artifacts.component.ability.retaliation.ThornsEffect(
            new artifacts.component.ability.retaliation.RetaliationEffect.ActivationParams(artifacts.config.value.Value.of(1D),artifacts.config.value.Value.of(4),artifacts.config.value.Value.of(0)),
            artifacts.config.value.Value.of(1),artifacts.config.value.Value.of(3));
        var apply=thorn.getClass().getDeclaredMethod("applyEffect",LivingEntity.class,LivingEntity.class);apply.setAccessible(true);
        player.getAttribute(Attributes.ARMOR).setBaseValue(20);apply.invoke(thorn,player,target);
        near(target.getHealth(),997,"Thorn scales from wearer armor");
        target.invulnerableTime=0;player.getAttribute(Attributes.ARMOR).setBaseValue(100);apply.invoke(thorn,player,target);
        near(target.getHealth(),993,"Thorn damage remains capped at 4");
        target.invulnerableTime=0;target.setHealth(1000);player.getAttribute(Attributes.ARMOR).setBaseValue(0);
        var tick=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.TICK.get());
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);player.setInvulnerable(false);player.getAbilities().invulnerable=false;
        for(String field:List.of("clientLoadedTimeoutTimer","waitingForRespawn")){
            var f=net.minecraft.server.network.ServerGamePacketListenerImpl.class.getDeclaredField(field);f.setAccessible(true);
            if(f.getType()==int.class)f.setInt(player.connection,0);else f.setBoolean(player.connection,false);
        }
        player.getPersistentData().remove("nameless_trinkets:proc_tick");player.invulnerableTime=0;player.setHealth(20);
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)tick.getItem()).curioTick(context,tick);
        near(target.getHealth(),997,"Tick damage depends on wearer power, not 1000 boss HP");
        near(player.getHealth(),19,"Tick wearer pays cost once per pulse");
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)tick.getItem()).curioTick(context,tick);
        near(target.getHealth(),997,"Tick has fixed 1s interval");
        check(target.getLastDamageSource().getEntity()==player,"Tick damage retains player attribution");
        var pet=EntityTypes.WOLF.create(level,EntitySpawnReason.COMMAND);pet.setPos(3,65,0);pet.setOwnerReference(net.minecraft.world.entity.EntityReference.of(player));pet.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);pet.setHealth(100);level.addFreshEntity(pet);
        player.getPersistentData().remove("nameless_trinkets:proc_tick");target.invulnerableTime=0;player.invulnerableTime=0;
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)tick.getItem()).curioTick(context,tick);
        near(pet.getHealth(),100,"Tick protects owned pets");
        target.discard();pet.discard();player.setHealth(20);
    }
    @SuppressWarnings("unchecked")
    void eclipseRegressions(ServerLevel level,net.minecraft.server.level.ServerPlayer player,top.theillusivec4.curios.api.SlotContext context) throws Exception {
        var target=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);target.setPos(2,65,1);target.setNoAi(true);level.addFreshEntity(target);
        var stack=new ItemStack(com.cozary.nameless_trinkets.init.ModItems.ECLIPSE_ASHES.get());
        player.getPersistentData().remove("nameless_trinkets:proc_eclipse_ashes");
        EclipseAshesHandler.onPlayerAttacked(player,target,stack);EclipseAshesHandler.tick(player,stack);
        var field=EclipseAshesHandler.class.getDeclaredField("SESSIONS");field.setAccessible(true);
        Map<UUID,EclipseAshesHandler.EclipseSession> sessions=(Map<UUID,EclipseAshesHandler.EclipseSession>)field.get(null);
        var session=sessions.get(player.getUUID());check(session!=null && session.currentWolfUUID!=null,"Eclipse phantom spawned");
        var wolf=(net.minecraft.world.entity.animal.wolf.Wolf)level.getEntity(session.currentWolfUUID);
        wolf.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
        var ownerField=EclipseAshesHandler.class.getDeclaredField("WOLF_OWNERS");ownerField.setAccessible(true);
        var owner=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);owner.setPos(1,65,1);level.addFreshEntity(owner);
        ((Map<UUID,UUID>)ownerField.get(null)).put(wolf.getUUID(),owner.getUUID());
        target.invulnerableTime=0;float hp=target.getHealth();
        var event=new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(target,new net.neoforged.neoforge.common.damagesource.DamageContainer(target.damageSources().mobAttack(wolf),100));
        EclipseAshesEvents.onLivingIncomingDamage(event);
        check(hp-target.getHealth()>0 && hp-target.getHealth()<=3,"buffed phantom wolf still obeys 3-damage cap");
        ((top.theillusivec4.curios.api.type.capability.ICurioItem)stack.getItem()).onUnequip(context,ItemStack.EMPTY,stack);
        check(!sessions.containsKey(player.getUUID()),"unequipping ends Eclipse session immediately");
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    void optionalIntegrations(ServerLevel level,Player player,Class<?> support) throws Exception {
        var power=support.getMethod("power",LivingEntity.class);
        for(String id:List.of("spell_power:arcane","spell_power:fire","spell_power:frost","spell_power:lightning","ranged_weapon:damage")) {
            var holder=BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(id));
            if(holder.isEmpty() || player.getAttribute(holder.get())==null)continue;
            var attribute=player.getAttribute(holder.get());double previous=attribute.getBaseValue();attribute.setBaseValue(80);
            near(((Number)power.invoke(null,player)).doubleValue(),80,"strongest optional school selected "+id);
            attribute.setBaseValue(previous);
        }
        var magic=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,Identifier.parse("spell_power:arcane")));
        if(magic.isPresent())near(FracturedNullstoneHandler.reduceMagicDamage(new net.minecraft.world.damagesource.DamageSource(magic.get(),player),10),8.5,"Nullstone also reduces RPG school damage");
        Class<?> managerClass;
        try {managerClass=Class.forName("dev.betterparty.party.PartyManager");}catch(ClassNotFoundException absent){return;}
        var server=level.getServer();var manager=managerClass.getMethod("forServer",net.minecraft.server.MinecraftServer.class).invoke(null,server);
        var partiesField=managerClass.getDeclaredField("parties");partiesField.setAccessible(true);
        var memberField=managerClass.getDeclaredField("playerParties");memberField.setAccessible(true);
        Map parties=(Map)partiesField.get(manager),members=(Map)memberField.get(manager);
        UUID id=UUID.randomUUID(),allyId=UUID.randomUUID();
        var ally=net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,new com.mojang.authlib.GameProfile(allyId,"AccessoryAlly"));
        var partyClass=Class.forName("dev.betterparty.party.Party");var party=partyClass.getConstructor(UUID.class).newInstance(id);
        var role=Class.forName("dev.betterparty.party.PartyRole");var memberClass=Class.forName("dev.betterparty.party.PartyMember");
        var constructor=memberClass.getConstructor(UUID.class,String.class,role);
        partyClass.getMethod("addMember",memberClass).invoke(party,constructor.newInstance(player.getUUID(),"Wearer",Enum.valueOf((Class)role,"LEADER")));
        partyClass.getMethod("addMember",memberClass).invoke(party,constructor.newInstance(allyId,"AccessoryAlly",Enum.valueOf((Class)role,"MEMBER")));
        parties.put(id,party);members.put(player.getUUID(),id);members.put(allyId,id);
        try {
            var pet=EntityTypes.WOLF.create(level,EntitySpawnReason.COMMAND);pet.setOwnerReference(net.minecraft.world.entity.EntityReference.of(allyId));
            for(String helper:List.of("com.cozary.nameless_trinkets.pack.AccessoryCombat","artifacts.pack.AccessoryCombat")) {
                var friendly=Class.forName(helper).getMethod("friendly",LivingEntity.class,net.minecraft.world.entity.Entity.class);
                check((boolean)friendly.invoke(null,player,ally),helper+" protects real Better Party ally");
                check((boolean)friendly.invoke(null,player,pet),helper+" protects party-owned pet");
            }
        } finally {parties.remove(id);members.remove(player.getUUID());members.remove(allyId);}
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    void policy() throws Exception {
        var gson=new com.google.gson.Gson();
        var n=gson.fromJson(Files.readString(Path.of("nameless-policy.json")),com.google.gson.JsonObject.class);
        for(var entry:n.getAsJsonObject("overrides").entrySet()) {
            var type=Class.forName("com.cozary.nameless_trinkets.items.trinkets."+entry.getKey()+"Base$Stats");
            var config=type.getDeclaredConstructor().newInstance();
            for(var field:entry.getValue().getAsJsonObject().entrySet()) {
                var f=type.getField(field.getKey());
                if(f.getType()==int.class)f.setInt(config,999);else if(f.getType()==float.class)f.setFloat(config,999);else f.setDouble(config,999);
            }
            PackBalance.apply(config);
            for(var field:entry.getValue().getAsJsonObject().entrySet())near(((Number)type.getField(field.getKey()).get(config)).doubleValue(),field.getValue().getAsDouble(),entry.getKey()+" stale "+field.getKey());
        }
        var a=gson.fromJson(Files.readString(Path.of("artifacts-policy.json")),com.google.gson.JsonObject.class);
        for(var entry:a.entrySet()) {
            String[] key=entry.getKey().split("\\.");String[] words=key[0].split("_");String name=words[0];
            for(int i=1;i<words.length;i++)name+=Character.toUpperCase(words[i].charAt(0))+words[i].substring(1);
            Object section=artifacts.Artifacts.CONFIG.items.getClass().getField(name).get(artifacts.Artifacts.CONFIG.items);
            var value=(artifacts.config.value.ConfigValue)section.getClass().getField(key[1]).get(section);
            near(((Number)value.get()).doubleValue(),entry.getValue().getAsDouble(),entry.getKey());
            Object stale=value.get() instanceof Integer?Integer.valueOf(999):(Object)Double.valueOf(999);
            value.set(stale);near(((Number)value.get()).doubleValue(),entry.getValue().getAsDouble(),entry.getKey()+" rejects stale config");
        }
        for(var id:n.getAsJsonArray("removed")) {
            var item=BuiltInRegistries.ITEM.getValue(Identifier.parse("nameless_trinkets:"+id.getAsString()));
            var config=((com.cozary.nameless_trinkets.items.subTrinket.TrinketItem)item).getTrinketConfig();
            check(item!=null && !config.getClass().getField("isEnable").getBoolean(config),"removed relic remains inert "+id.getAsString());
        }
    }
}
