package net.valhaimerd.orboblivioncompat.mixin;
import net.valhaimerd.orboblivioncompat.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.server.data.PlayerData;
import net.minecraft.server.level.ServerPlayer;
@Mixin(value=SkillsMod.class,remap=false)
public interface SkillsDataAccess {@Invoker("getPlayerData") PlayerData utilities$playerData(ServerPlayer p);}
