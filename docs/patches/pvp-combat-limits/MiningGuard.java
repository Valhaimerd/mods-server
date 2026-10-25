package org.xiyu.onekeyminer.pack;
import org.xiyu.onekeyminer.chain.*;
import net.neoforged.fml.ModList;
import io.redspace.pvp_flagging.CombatRules;
public final class MiningGuard {
 public static ChainActionResult blocked(ChainActionContext c){
  return ModList.get().isLoaded("pvp_flagging")&&CombatRules.blockAdvanced(c.getPlayer())?ChainActionResult.cancelled(c.getActionType(),ChainActionResult.StopReason.PERMISSION_DENIED):null;
 }
}
