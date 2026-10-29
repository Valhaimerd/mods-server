package net.spell_engine.internals;
import java.util.*;import net.spell_engine.entity.WarriorTierTwo;import net.minecraft.world.entity.*;import net.spell_engine.api.effect.*;import net.spell_engine.internals.casting.*;
public final class PaladinFollowup {
 public static boolean focused(LivingEntity e){if(!e.isAlive()||!e.hasEffect(PaladinRevamp.effect(PaladinUltimates.STANCE)))return false;var c=PaladinUltimates.channelOf(e);if(c!=null)return !c.ended&&e.level().getGameTime()-c.start>=200;if(e.level().isClientSide()&&e instanceof SpellCaster.Player p){var process=p.getSpellCastProcess();return process!=null&&process.id().toString().equals(PaladinUltimates.PURGATORY)&&process.spellCastTicksSoFar(e.level().getGameTime())>=200;}return false;}
 public static boolean deny(boolean old,LivingEntity e,net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect){return old||(focused(e)&&!effect.is(net.minecraft.resources.Identifier.parse(PaladinUltimates.STANCE))&&WarriorTierTwo.impairing(effect));}
 public static boolean impaired(LivingEntity e,EntityActionsAllowed.Player action,boolean show){return action==EntityActionsAllowed.Player.CAST_SPELL&&focused(e)?false:EntityActionsAllowed.isImpaired(e,action,show);}
 public static void cleanseFocused(LivingEntity e){if(!focused(e))return;for(var effect:List.copyOf(e.getActiveEffects()))if(!effect.getEffect().is(net.minecraft.resources.Identifier.parse(PaladinUltimates.STANCE))&&WarriorTierTwo.impairing(effect.getEffect()))e.removeEffect(effect.getEffect());((EntityActionsAllowed.ControlledEntity)e).updateEntityActionsAllowed();}
}

