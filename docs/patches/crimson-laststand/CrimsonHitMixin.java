package net.spell_engine.mixin.crimson;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.*;
import net.spell_engine.internals.delivery.melee.Melee;
@Mixin(RogueTierThree.class) public class CrimsonHitMixin {
 @WrapMethod(method="attack")
 private static void crimson$hit(Player player,Entity target,Melee.AttackContext context,Operation<Void> original){
  double before=target instanceof LivingEntity living?CrimsonHealing.health(living):0;
  original.call(player,target,context);
  if(context.spellId().toString().equals("rogues:mutilate")&&target instanceof LivingEntity living)CrimsonHealing.hit(player,living,before);
 }
}
