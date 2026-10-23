package local.lifesteal;

import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Exact healing for the existing Life Steal datapack's threshold function. */
@Mod("mr_ly_lifestealenchantment")
public class LifeStealPack {
    public LifeStealPack() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            event.getDispatcher().register(Commands.literal("modpack_lifesteal_heal")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(context -> {
                    context.getSource().getPlayerOrException().heal(3.0F);
                    return 1;
                })));
    }
}
