package com.emandev.bifrost.mixin;

import java.util.*;
import com.emandev.bifrost.AsgardWeapons;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.spell_engine.client.SpellEngineClient;
import net.spell_engine.client.input.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native use-key slot consumes numeric index 1 even though it displays right-click.
 * Compact only Bifrost weapon defaults: use, 1, 2, ...; preserve other loadouts. */
@Mixin(SpellHotbar.class)
public abstract class BifrostHotbarMixin {
    @Shadow public List<SpellHotbar.Slot> slots;
    @Shadow public SpellHotbar.StructuredSlots structuredSlots;
    @Inject(method="update",at=@At("RETURN"))
    private void bifrost$compactNumberedSkills(LocalPlayer player,Options options,CallbackInfoReturnable<Boolean> cir){
        if(AsgardWeapons.kind(player.getMainHandItem()).isEmpty()||!SpellEngineClient.config.spellHotbarUseKey||slots.isEmpty())return;
        var first=slots.getFirst();
        if(first.spell()==null||first.getKeyBinding(options)!=options.keyUse)return;
        var bindings=Keybindings.Wrapped.all();var compact=new ArrayList<SpellHotbar.Slot>();
        var numbered=new ArrayList<SpellHotbar.Slot>();compact.add(first);int index=0;
        for(int i=1;i<slots.size();i++){
            var slot=slots.get(i);
            if(slot.spell()!=null&&index<bindings.size()){
                slot=new SpellHotbar.Slot(slot.option(),slot.itemStack(),bindings.get(index++),slot.modifier());
                numbered.add(slot);
            }
            compact.add(slot);
        }
        slots=List.copyOf(compact);structuredSlots=new SpellHotbar.StructuredSlots(first,List.copyOf(numbered));
    }
}
