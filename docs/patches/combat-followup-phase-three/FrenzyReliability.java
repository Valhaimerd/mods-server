package local.crossarms.runtime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Player scores are authoritative; item hints only reconcile ordinary client use prediction. */
@EventBusSubscriber(modid="mr_crossarms")
public final class FrenzyReliability {
 static final String HINT="crossarms_pack:burst_state";
 static final String SAVED="crossarms_pack:reload_ammo";
 public static boolean saved(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(SAVED);}
 public static void parkLoaded(LivingEntity user,ItemStack stack){
  var loaded=stack.getOrDefault(DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.EMPTY);if(loaded.isEmpty())return;
  var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();tag.put(SAVED,net.minecraft.world.item.component.ChargedProjectiles.CODEC.encodeStart(user.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),loaded).getOrThrow());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));stack.set(DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.EMPTY);
 }
 public static boolean restore(LivingEntity user,ItemStack stack){
  var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(!tag.contains(SAVED))return false;
  var loaded=net.minecraft.world.item.component.ChargedProjectiles.CODEC.parse(user.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tag.get(SAVED)).result().orElse(null);
  if(loaded==null||loaded.isEmpty()||!loaded.itemCopies().stream().allMatch(s->s.is(Items.ARROW))){tag.remove(SAVED);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return false;}
  if(!CrossbowItem.isCharged(stack))stack.set(DataComponents.CHARGED_PROJECTILES,loaded);tag.remove(SAVED);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return true;
 }
 public static int state(LivingEntity user,ItemStack stack){
  if(user.level().isClientSide())return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr(HINT,0);
  if(FrenzyState.empty(user))return 2;return FrenzyState.score(user,"frenzy_shots")>0?1:0;
 }
 public static boolean awaitingAutoFeed(LivingEntity user,ItemStack stack){return state(user,stack)==1&&!CrossbowItem.isCharged(stack)&&stack.getMaxDamage()-stack.getDamageValue()>1&&!user.getProjectile(stack).isEmpty();}
 public static boolean normalLoaded(ItemStack stack){return stack.getOrDefault(DataComponents.CHARGED_PROJECTILES,net.minecraft.world.item.component.ChargedProjectiles.EMPTY).itemCopies().stream().allMatch(s->s.is(Items.ARROW));}
 public static void sync(LivingEntity user){
  if(!(user instanceof ServerPlayer player))return;
  boolean changed=false;
  for(var hand:InteractionHand.values()){
   var stack=player.getItemInHand(hand);if(!(stack.getItem() instanceof CrossbowItem))continue;
   if(saved(stack)&&(!FrenzyState.applies(player,stack)||!FrenzyState.empty(player)))changed|=restore(player,stack);
   if(!FrenzyState.applies(player,stack))continue;
   int state=state(player,stack);var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
   if(tag.getIntOr(HINT,0)!=state){changed=true;if(state==0)tag.remove(HINT);else tag.putInt(HINT,state);if(tag.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));}
  }
  if(changed)player.containerMenu.broadcastChanges();
 }
 public static boolean load(LivingEntity user,ItemStack stack,com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original){
  if(FrenzyState.applies(user,stack)&&FrenzyState.empty(user)&&saved(stack)){if(!restore(user,stack))return false;FrenzyState.cranked(user);return true;}
  if(FrenzyState.applies(user,stack)&&!user.getProjectile(stack).is(Items.ARROW))return false;
  boolean loaded=original.call(user,stack);if(loaded&&FrenzyState.applies(user,stack)&&FrenzyState.empty(user))FrenzyState.cranked(user);return loaded;
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
  var player=event.getEntity();if(!(player instanceof ServerPlayer))return;
  boolean held=false;for(var hand:InteractionHand.values()){var stack=player.getItemInHand(hand);if(stack.getItem() instanceof CrossbowItem&&FrenzyState.applies(player,stack)){held=true;if(player.isUsingItem()&&player.getUsedItemHand()==hand&&state(player,stack)==1)player.stopUsingItem();}}
  if(held)sync(player);
 }
}
