package com.emandev.bifrost;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid="bifrost")
public final class AsgardCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("asgard")
            .then(Commands.literal("claim").executes(context->{var p=context.getSource().getPlayerOrException();boolean done=WeaponOwnership.claim(p);p.sendSystemMessage(Component.literal(done?"Weapon claimed. It is permanently bound to you.":"Hold an unclaimed hammer/axe, or look at an unclaimed one nearby. Only an Asgardian may claim it."));return done?1:0;}))
            .then(Commands.literal("recover").requires(s->s.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.argument("owner",EntityArgument.player()).then(Commands.argument("weapon_uuid",StringArgumentType.word()).executes(context->{var owner=EntityArgument.getPlayer(context,"owner");boolean done=WeaponOwnership.adminRecover(owner,StringArgumentType.getString(context,"weapon_uuid"));context.getSource().sendSuccess(()->Component.literal(done?"Bound weapon recovered to its existing owner; ownership unchanged.":"No matching bound weapon exists for that owner."),true);return done?1:0;}))))
            .then(Commands.literal("reassign").requires(s->s.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.argument("new_owner",EntityArgument.player()).then(Commands.argument("weapon_uuid",StringArgumentType.word()).then(Commands.argument("previous_owner_uuid",StringArgumentType.word()).executes(context->{
                    var next=EntityArgument.getPlayer(context,"new_owner");String id=StringArgumentType.getString(context,"weapon_uuid"),old=StringArgumentType.getString(context,"previous_owner_uuid");
                    boolean done=WeaponOwnership.adminReassign(next,id,old);context.getSource().sendSuccess(()->Component.literal(done?"Administrator reassigned bound weapon "+id+" from "+old+" to "+next.getUUID()+". Old copies are invalid.":"Reassignment failed: verify the exact previous owner UUID and weapon UUID."),true);return done?1:0;
                }))))));
    }
}
