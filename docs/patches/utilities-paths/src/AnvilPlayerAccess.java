package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.entity.player.Player;
@Mixin(ItemCombinerMenu.class)
public interface AnvilPlayerAccess {@Accessor("player") Player utilities$player();}
