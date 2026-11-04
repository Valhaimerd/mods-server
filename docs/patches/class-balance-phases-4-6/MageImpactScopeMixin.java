package net.spell_engine.mixin.mages;
import java.util.Collection;import net.minecraft.core.Holder;import net.minecraft.world.level.Level;import net.minecraft.world.entity.*;import net.minecraft.server.level.ServerPlayer;
import net.spell_engine.api.spell.Spell;import net.spell_engine.internals.*;import net.spell_engine.internals.impact.SpellImpacts;import net.spell_engine.internals.target.SpellTarget;
import org.spongepowered.asm.mixin.Mixin;import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(value=SpellImpacts.class,remap=false)
public class MageImpactScopeMixin {
 @WrapMethod(method="performImpact")
 private static boolean scope(Level level,LivingEntity caster,Entity target,Holder<Spell> spell,Spell.Impact impact,SpellExecution.ImpactContext context,Collection<ServerPlayer> trackers,Operation<Boolean> original){
  LivingEntity old=MagePenetration.CASTER.get();MagePenetration.CASTER.set(spell.is(MageFollowup.id("wizards:frost_lance"))&&context.focusMode()==SpellTarget.FocusMode.DIRECT&&impact.action.type==Spell.Impact.Action.Type.DAMAGE&&MageFollowup.node(caster,"skill_tree_rpgs:frost_tier_3_spell_2_modifier_1")?caster:null);
  try{return MageFollowup.afterContext(original.call(level,caster,target,spell,impact,context,trackers),caster,target,spell,impact,context);}finally{if(old==null)MagePenetration.CASTER.remove();else MagePenetration.CASTER.set(old);}
 }
}
