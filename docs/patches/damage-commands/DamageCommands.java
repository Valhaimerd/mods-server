package io.redspace.pvp_flagging;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Native world data, cached by Minecraft; no static world/player references. */
public final class DamageCommands extends SavedData {
 private double pvp=35,pve=80;
 private static final Codec<DamageCommands> CODEC=RecordCodecBuilder.create(i->i.group(
  Codec.doubleRange(0,100).optionalFieldOf("pvp",35.0).forGetter((DamageCommands d)->d.pvp),
  Codec.doubleRange(0,100).optionalFieldOf("pve",80.0).forGetter((DamageCommands d)->d.pve)
 ).apply(i,DamageCommands::new));
 private static final SavedDataType<DamageCommands> TYPE=new SavedDataType<>(Identifier.parse("pvp_flagging:damage_percentages"),DamageCommands::new,CODEC);
 public DamageCommands(){}
 private DamageCommands(double pvp,double pve){this.pvp=pvp;this.pve=pve;}
 public static DamageCommands get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(TYPE);}
 public double percent(boolean players){return players?pvp:pve;}
 public static void register(RegisterCommandsEvent event){
  event.getDispatcher().register(command("pvpdamage","PvP damage",true));
  event.getDispatcher().register(command("pvedamage","Player-to-entity damage",false));
 }
 private static LiteralArgumentBuilder<CommandSourceStack> command(String name,String label,boolean players){
  return Commands.literal(name).requires(s->s.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
   .executes(c->{double value=get(c.getSource().getServer()).percent(players);c.getSource().sendSuccess(()->Component.literal(label+": "+format(value)+"% (includes player-owned RPG summons)."),false);return 1;})
   .then(Commands.argument("percent",DoubleArgumentType.doubleArg(0,100)).executes(c->{
    double value=DoubleArgumentType.getDouble(c,"percent");
    if(!Double.isFinite(value)){c.getSource().sendFailure(Component.literal("Use a percentage from 0 to 100."));return 0;}
    var server=c.getSource().getServer();var data=get(server);
    if(players)data.pvp=value;else data.pve=value;
    data.setDirty();server.overworld().getDataStorage().saveAndJoin();
    c.getSource().sendSuccess(()->Component.literal(label+" set to "+format(value)+"% (includes player-owned RPG summons)."),true);return 1;
   }));
 }
 private static String format(double value){return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();}
}
