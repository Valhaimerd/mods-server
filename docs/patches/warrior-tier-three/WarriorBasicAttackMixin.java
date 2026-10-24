package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.entity.WarriorUltimates;
import net.spell_engine.internals.casting.SpellCaster;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(Player.class)
public abstract class WarriorBasicAttackMixin {
 @WrapMethod(method="attack")
 private void warriorBasicAttack(Entity target,Operation<Void> original){
  var player=(Player)(Object)this;
  if(((SpellCaster.Player)player).getActiveMeleeSkill()!=null){original.call(target);return;}
  WarriorUltimates.enter(player);try{original.call(target);}finally{WarriorUltimates.leave();}
 }
}
