package archives.tater.penchant.mixin.disable;
import java.util.Set;
import com.mojang.serialization.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEnchantments.class)
public class RetiredEnchantmentsMixin {
 @Shadow @Final @Mutable public static Codec<ItemEnchantments> CODEC;
 @Inject(method="<clinit>",at=@At("RETURN"))
 private static void retirement$codec(CallbackInfo ci){
  final var original=CODEC;
  final Set<String> retired=Set.of("toughness_enchantment:toughness","air_jump_enchantment:air_jump","betterarcheology:penetrating_strike","crossarms:bombard","crossarms:deflector","crossarms:recycling","enderzoology:decay","enderzoology:soulbound","enderzoology:withering","life_steal_enchantment:life_steal","nova_structures:antidote","nova_structures:ghasted","nova_structures:spiteful","nova_structures:traveler","protection_grace_enchantment:protection_grace","tense_enchantment:tense");
  CODEC=new Codec<ItemEnchantments>(){
   public <T> DataResult<Pair<ItemEnchantments,T>> decode(DynamicOps<T> ops,T input){
    var map=ops.getMapValues(input).result();
    if(map.isPresent())input=ops.createMap(map.get().filter(p->!retired.contains(ops.getStringValue(p.getFirst()).result().orElse(""))));
    return original.decode(ops,input);
   }
   public <T> DataResult<T> encode(ItemEnchantments value,DynamicOps<T> ops,T prefix){return original.encode(value,ops,prefix);}
  };
 }
}
