package net.spell_engine.mixin.finale;
import java.util.Collection;import net.minecraft.core.Holder;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.server.level.ServerPlayer;import net.spell_engine.api.spell.Spell;import net.spell_engine.internals.*;import net.spell_engine.internals.impact.SpellImpacts;
import org.spongepowered.asm.mixin.Mixin;import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(value=SpellImpacts.class,remap=false) public class FinalImpactMixin {
 @WrapMethod(method="performImpact") private static boolean scoped(Level world,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact,SpellExecution.ImpactContext context,Collection<ServerPlayer> tracking,Operation<Boolean> original){
  boolean old=FinalDefenses.PERIODIC.get(),oldPassive=FinalDefenses.PASSIVE.get();FinalDefenses.PERIODIC.set(old||spell.value().deliver!=null&&spell.value().deliver.type==Spell.Delivery.Type.CLOUD);FinalDefenses.PASSIVE.set(oldPassive||spell.value().type==Spell.Type.PASSIVE||!(caster instanceof net.minecraft.world.entity.player.Player));
  try{return original.call(world,caster,target,spell,impact,context,tracking);}finally{FinalDefenses.PERIODIC.set(old);FinalDefenses.PASSIVE.set(oldPassive);}
 }
}
