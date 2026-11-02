package net.spell_engine.mixin.warrior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(Player.class)
public class AttackScopeMixin {
 @WrapMethod(method="attack") private void classPack$attack(Entity target,Operation<Void> original){net.spell_engine.entity.ClassRework.begin();try{original.call(target);}finally{net.spell_engine.entity.ClassRework.end();}}
}
