package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.*;
@Mixin(EnchantmentScreen.class)
public class EnchantScreenMixin {
 private static boolean discounted(){var p=Minecraft.getInstance().player;return p!=null&&Utilities.stat(p,"cost")>0;}
 private static MutableComponent payment(int levels){return Component.literal(Utilities.shownEnchantDebit(Minecraft.getInstance().player,levels)+" XP points");}
 @Redirect(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
 private MutableComponent one(String key){return key.equals("container.enchant.level.one")&&discounted()?payment(1):Component.translatable(key);}
 @Redirect(method="extractRenderState",at=@At(value="INVOKE",target="Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
 private MutableComponent many(String key,Object[] args){return key.equals("container.enchant.level.many")&&discounted()?payment(((Number)args[0]).intValue()):Component.translatable(key,args);}
}
