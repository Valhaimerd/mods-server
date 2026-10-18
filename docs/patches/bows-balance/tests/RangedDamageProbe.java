package local.bows;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.rpg_foundation.ranged_weapon.api.*;

public final class RangedDamageProbe {
 public static void run(net.minecraft.server.level.ServerPlayer player,net.minecraft.server.level.ServerLevel level)throws Exception{
  for(var sample:java.util.Map.of("wind_bow",Double.parseDouble(System.getProperty("bows.probe.wind","3")),"sentinels_wrath",5d,"ancient_sage_bow",7d,"dark_bow",20d,"gravewire_bow",6.5d,"soulhoard",9.5d,"vaultpiercer",7.5d).entrySet()){
   var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("too_many_bows:"+sample.getKey())));
   double damage=stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream().filter(e->e.attribute().equals(EntityAttributes_RangedWeapon.DAMAGE.entry)).mapToDouble(e->e.modifier().amount()).sum();
   if(Math.abs(damage-sample.getValue())>.00001)throw new AssertionError(sample.getKey()+" ranged damage "+damage+" expected "+sample.getValue());
   var props=RangedWeaponProperties.get(stack);
   if(props==null||Math.abs(props.damageBaseline(stack.getItem())-damage)>.00001)throw new AssertionError("wrong scaling baseline "+sample.getKey());
   System.out.println("RANGED_PASS "+sample.getKey()+" original damage "+damage);
  }
  int covered=0;
  for(var item:BuiltInRegistries.ITEM)if(item instanceof net.bandit.many_bows.item.ModBowItem){
   var stack=new ItemStack(item);var props=RangedWeaponProperties.get(stack);
   var damage=stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream().filter(e->e.attribute().equals(EntityAttributes_RangedWeapon.DAMAGE.entry)).mapToDouble(e->e.modifier().amount()).sum();
   if(props==null||props.damage().isEmpty()||Math.abs(props.damageBaseline(item)-damage)>.00001)throw new AssertionError("Unmapped native bow "+BuiltInRegistries.ITEM.getKey(item));covered++;
  }
  if(covered!=42)throw new AssertionError("Expected 42 native bows, got "+covered);
  System.out.println("RANGED_PASS all 42 native bows have explicit matching damage components");
  var shoot=ProjectileWeaponItem.class.getDeclaredMethod("shoot",net.minecraft.server.level.ServerLevel.class,net.minecraft.world.entity.LivingEntity.class,net.minecraft.world.InteractionHand.class,ItemStack.class,java.util.List.class,float.class,float.class,boolean.class,net.minecraft.world.entity.LivingEntity.class);shoot.setAccessible(true);
  var spawned=new java.util.ArrayList<net.minecraft.world.entity.projectile.arrow.AbstractArrow>();
  java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=event->{if(event.getEntity() instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow)spawned.add(arrow);};
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
  level.getChunk(0,0);player.setPos(0,80,0);
  for(var sample:java.util.List.of("flame_bow","wind_bow","sentinels_wrath","sentinels_wrath:tipped","solar_bow:tipped","shulker_blast:tipped")){
   String name=sample.split(":")[0];boolean tipped=sample.contains(":");
   double[] values=new double[2];
   for(int boost=0;boost<2;boost++){
    var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("too_many_bows:"+name)));
    var damage=stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers().stream().filter(e->e.attribute().equals(EntityAttributes_RangedWeapon.DAMAGE.entry)).mapToDouble(e->e.modifier().amount()).sum();
    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
    var attribute=player.getAttribute(EntityAttributes_RangedWeapon.DAMAGE.entry);attribute.removeModifiers();attribute.setBaseValue(damage);
    if(boost==1)attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(Identifier.parse("probe:bonus"),.2,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    spawned.clear();
    shoot.invoke(stack.getItem(),level,player,net.minecraft.world.InteractionHand.MAIN_HAND,stack,java.util.List.of(new ItemStack(tipped?Items.TIPPED_ARROW:Items.ARROW)),3f,0f,false,null);
    net.minecraft.world.entity.projectile.arrow.AbstractArrow fired=spawned.isEmpty()?null:spawned.getLast();
    if(fired==null)throw new AssertionError("No fired projectile "+name);
    values[boost]=((net.rpg_foundation.ranged_weapon.internal.ArrowExtension)fired).rwa_getDamage();fired.discard();
   }
   double ratio=values[1]/values[0];
   if(Math.abs(ratio-1.2)>.0001)throw new AssertionError(sample+" shot scales "+ratio+" instead of once by 1.2");
   System.out.println("RANGED_PASS fired "+sample+" base="+values[0]+" boosted="+values[1]+" ratio="+ratio);
  }
 }
}
