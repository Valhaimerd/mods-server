package net.spell_engine.mixin.crimson;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.spell_engine.internals.CrimsonHealing;
import net.spell_engine.internals.delivery.melee.Melee;
@Mixin(Melee.class) public class CrimsonCastMixin {
 @WrapMethod(method="performAttackAgainstTargets")
 private static void crimson$cast(ServerPlayer player,Melee.AttackContext context,int[] targets,Operation<Void> original){
  if(!context.spellId().toString().equals("rogues:mutilate")){original.call(player,context,targets);return;}
  var previous=CrimsonHealing.begin(player);try{original.call(player,context,targets);}finally{CrimsonHealing.end(previous);}
 }
}
