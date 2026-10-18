package net.bandit.many_bows.neoforge.curio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.SlotContext;
import net.minecraft.world.item.ItemStack;
/** Disabled legacy accessory: still registered for existing saves. */
public class SharpshotRingCurio implements ICurioItem {
 public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack){return CurioAttributeModifiers.builder().build();}
 public boolean canEquip(SlotContext context,ItemStack stack){return false;}
}
