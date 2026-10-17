package local.balance;
import java.nio.file.*;
import java.util.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import com.cozary.nameless_trinkets.init.ModItems;
import com.cozary.nameless_trinkets.items.subTrinket.*;
import com.cozary.nameless_trinkets.events.*;

@Mod("balance_probe")
public class BalanceProbe {
 int checks;
 public BalanceProbe(){NeoForge.EVENT_BUS.addListener(this::started);}
 void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;System.out.println("BALANCE_PASS "+label);}
 void started(ServerStartedEvent event){
  var server=event.getServer();
  server.execute(()->{
   try {
    var level=server.overworld();
    var player=FakePlayerFactory.getMinecraft(level);
    player.setPos(0,65,0);
    var inventory=CuriosApi.getCuriosInventory(player).orElseThrow();
    inventory.loadDatapacks();
    var entry=inventory.getCurios().entrySet().stream().filter(e->e.getValue().getStacks().getSlots()>0).findFirst().orElseThrow();
    var slots=entry.getValue().getStacks();
    var context=new SlotContext(entry.getKey(),player,0,false,true);
    var zombie=EntityTypes.ZOMBIE.create(level,EntitySpawnReason.COMMAND);zombie.setPos(2,65,0);
    slots.setStackInSlot(0,new ItemStack(ModItems.LUNAR_CREST.get()));
    zombie.hurtServer(level,level.damageSources().playerAttack(player),1);
    check(true,"player damage while legacy Lunar Crest equipped does not load client classes");
    slots.setStackInSlot(0,ItemStack.EMPTY);
    if(Files.exists(Path.of("baseline.flag")))throw new AssertionError("Baseline unexpectedly survived");

    var spec=com.google.gson.JsonParser.parseString(Files.readString(Path.of("balance.json"))).getAsJsonObject();
    for(var group:spec.getAsJsonObject("overrides").entrySet()) {
     String className="com.cozary.nameless_trinkets.items.trinkets."+group.getKey()+"Base$Stats";
     var type=Class.forName(className);var stale=type.getConstructor().newInstance();
     for(var field:group.getValue().getAsJsonObject().entrySet()) {
      var f=type.getField(field.getKey());
      if(f.getType()==int.class)f.setInt(stale,999);else if(f.getType()==float.class)f.setFloat(stale,999);else f.setDouble(stale,999);
     }
     var base=Class.forName(className.replace("$Stats",""));
     var item=(TrinketItem<?>)base.getField("INSTANCE").get(null);
     item.setTrinketConfig((TrinketsStats)stale);
     for(var field:group.getValue().getAsJsonObject().entrySet())
      check(Math.abs(((Number)type.getField(field.getKey()).get(item.getTrinketConfig())).doubleValue()-field.getValue().getAsDouble())<0.00001,"stale config override "+group.getKey()+"."+field.getKey());
    }
    for(var value:spec.getAsJsonArray("removed")) {
     String name=value.getAsString();var item=BuiltInRegistries.ITEM.getValue(Identifier.parse("nameless_trinkets:"+name));
     var relic=(TrinketItem<?>)item;
     var config=relic.getTrinketConfig();config.getClass().getField("isEnable").setBoolean(config,true);
     relic.setTrinketConfig(config);
     check(!config.getClass().getField("isEnable").getBoolean(config),"removed disabled after old config "+name);
     check(!((ICurioItem)item).canEquip(context,new ItemStack(item)),"removed cannot equip "+name);
     check(!new ItemStack(item).is(com.cozary.nameless_trinkets.init.ModTags.NAMELESS_TRINKETS_TAG),"removed absent from random tag "+name);
     check(server.getRecipeManager().byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,Identifier.parse("nameless_trinkets:"+name))).isEmpty(),"removed recipe "+name);
    }
    var titan=(com.cozary.nameless_trinkets.items.trinkets.TitansMark)ModItems.TITANS_MARK.get();
    var stack=new ItemStack(titan);
    var attack=player.getAttribute(Attributes.ATTACK_DAMAGE);
    var legacy=Identifier.parse("nameless_trinkets:titans_mark_attack_damage");
    attack.addTransientModifier(new AttributeModifier(legacy,1.1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    var before=player.getAttributeValue(Attributes.ATTACK_DAMAGE)/2.1;
    var health=player.getMaxHealth();
    titan.onEquip(context,ItemStack.EMPTY,stack);titan.curioTick(context,stack);
    check(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE)-before)<0.0001,"Titan removes legacy attack bonus");
    check(player.getEffect(MobEffects.SLOWNESS).getAmplifier()==0 && player.getEffect(MobEffects.SLOWNESS).getDuration()==1200,"Titan Slowness I 60s");
    check(player.getEffect(MobEffects.HEALTH_BOOST).getAmplifier()==4 && player.getEffect(MobEffects.HEALTH_BOOST).getDuration()==100 && player.getMaxHealth()==health+20,"Titan +10 hearts 5s");
    check(player.getEffect(MobEffects.RESISTANCE).getAmplifier()==0 && player.getEffect(MobEffects.RESISTANCE).getDuration()==100,"Titan Resistance I 5s");
    player.removeEffect(MobEffects.HEALTH_BOOST);
    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.HEALTH_BOOST,20,4));titan.curioTick(context,stack);
    check(player.getEffect(MobEffects.HEALTH_BOOST).getDuration()==100,"Titan refreshes while worn");
    titan.onUnequip(context,ItemStack.EMPTY,stack);
    check(player.getEffect(MobEffects.HEALTH_BOOST).getDuration()==100,"Titan effects linger for stated duration after removal");
    check(LunarCrestHandler.getCurrentClientBonus()==0,"Lunar shared helper has no client dependency");
    var puffer=(com.cozary.nameless_trinkets.items.trinkets.PufferFishLiverBase)ModItems.PUFFER_FISH_LIVER.get();
    var cow=EntityTypes.COW.create(level,EntitySpawnReason.COMMAND);
    for(int i=0;i<20;i++){cow.removeEffect(MobEffects.POISON);PufferFishLiverHandler.applyPoisonEffect(cow);check(cow.getEffect(MobEffects.POISON).getAmplifier()==0&&cow.getEffect(MobEffects.POISON).getDuration()==60,"Poison I 3s guaranteed "+i);}
    final int[] bonus={0};ExperienceBatteryHandler.handleExperienceDrop(player,zombie,20,x->bonus[0]+=x);check(bonus[0]==10,"XP +50 percent actual calculation");
    check(Math.abs(FracturedNullstoneHandler.reduceMagicDamage(level.damageSources().magic(),10)-7)<0.001,"magic reduction 30 percent actual calculation");
    var sword=new ItemStack(ModItems.VAMPIRE_BLOOD.get());
    var vampire=(ICurioItem)ModItems.VAMPIRE_BLOOD.get();vampire.onEquip(context,ItemStack.EMPTY,sword);
    check(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE)/before-1.2)<0.0001,"Vampire 1.2x actual attribute");vampire.onUnequip(context,ItemStack.EMPTY,sword);
    player.removeAllEffects();
    var baseSpeed=player.getAttributeValue(Attributes.MOVEMENT_SPEED);
    var veil=(ICurioItem)ModItems.SHRINKING_VEIL.get();var veilStack=new ItemStack(ModItems.SHRINKING_VEIL.get());
    veil.onEquip(context,ItemStack.EMPTY,veilStack);
    check(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)/baseSpeed-1.25)<0.0001,"Shrinking Veil actual 1.25x movement");
    veil.onUnequip(context,ItemStack.EMPTY,veilStack);
    level.setBlock(new net.minecraft.core.BlockPos(0,64,0),net.minecraft.world.level.block.Blocks.SAND.defaultBlockState(),3);
    player.setPos(0.5,65,0.5);
    var scarab=(ICurioItem)ModItems.SCARAB_AMULET.get();var scarabStack=new ItemStack(ModItems.SCARAB_AMULET.get());
    scarab.curioTick(context,scarabStack);
    check(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED)/baseSpeed-1.5)<0.0001,"Scarab actual 1.5x movement on sand");
    scarab.onUnequip(context,ItemStack.EMPTY,scarabStack);
    var staleSpeed=Identifier.parse("nameless_trinkets:speed_force_movement_speed");
    player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(staleSpeed,2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    ((ICurioItem)ModItems.SPEED_FORCE.get()).onUnequip(context,ItemStack.EMPTY,new ItemStack(ModItems.SPEED_FORCE.get()));
    check(!player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(staleSpeed),"Removed Speed Force clears old attribute");
    var staleCrown=Identifier.parse("nameless_trinkets:gods_crown_max_health");
    player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(staleCrown,0.5,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    ((ICurioItem)ModItems.GODS_CROWN.get()).onUnequip(context,ItemStack.EMPTY,new ItemStack(ModItems.GODS_CROWN.get()));
    check(!player.getAttribute(Attributes.MAX_HEALTH).hasModifier(staleCrown),"Removed crown clears old attribute");
    var staleStar=Identifier.parse("nameless_trinkets:dying_star_minecraft_attack_damage");
    attack.addTransientModifier(new AttributeModifier(staleStar,5,AttributeModifier.Operation.ADD_VALUE));
    ((ICurioItem)ModItems.DYING_STAR.get()).onUnequip(context,ItemStack.EMPTY,new ItemStack(ModItems.DYING_STAR.get()));
    check(!attack.hasModifier(staleStar),"Removed Dying Star clears combat attributes including unsynced ones");
    Files.writeString(Path.of("result.txt"),"PASS "+checks+" checks\n");
    System.out.println("BALANCE_RESULT PASS "+checks);
   } catch(Throwable ex){ex.printStackTrace();try{Files.writeString(Path.of("result.txt"),"FAIL "+ex);}catch(Exception ignored){}}
   finally {server.halt(false);}
  });
 }
}

