package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.puffish.skillsmod.server.data.*;
import net.minecraft.resources.Identifier;
@Mixin(value=PlayerData.class,remap=false)
public interface PlayerDataAccess {@Accessor("categories") Map<Identifier,CategoryData> utilities$categories();}
