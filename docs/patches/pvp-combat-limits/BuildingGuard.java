package sophisticated.building.pack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import io.redspace.pvp_flagging.CombatRules;
public final class BuildingGuard {
 public static boolean blocked(Player p){return ModList.get().isLoaded("pvp_flagging")&&CombatRules.blockAdvanced(p);}
 public static boolean discardQueued(Entity e){return e.isRemoved()||e instanceof Player p&&ModList.get().isLoaded("pvp_flagging")&&CombatRules.flagged(p);}
}
