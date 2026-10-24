package punchy.compat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;

/** 26.2 shields can be ordinary Items with BLOCKS_ATTACKS, including Paladin kite shields. */
public final class ShieldKinds {
    private ShieldKinds(){}
    public static boolean isShield(Object value){
        return value instanceof ShieldItem || value instanceof Item item
            && item.getDefaultInstance().has(DataComponents.BLOCKS_ATTACKS);
    }
    /** Keep Minecraft's existing raised blocking-item pose in first person.
     * Punchy's shared use_shield clip also animates third person, but its custom
     * first-person item grip lowers these component shields out of the viewport.
     */
    public static boolean nativeBlockHand(net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand){
        var mc=net.minecraft.client.Minecraft.getInstance();
        return player!=null&&player==mc.player&&mc.options.getCameraType().isFirstPerson()
            &&player.isUsingItem()&&player.getUsedItemHand()==hand
            &&player.getUseItem().has(DataComponents.BLOCKS_ATTACKS);
    }
}
