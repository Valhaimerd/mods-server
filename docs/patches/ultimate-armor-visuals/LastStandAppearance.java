package net.spell_engine.entity;
import net.spell_engine.api.effect.EntityTints;
public final class LastStandAppearance {
 public static final int GOLD_TINT=EntityTints.scaled(0xFFFFD700,.3f);
 public static void register(){EntityTints.register(net.rogues.effect.RogueEffects.LAST_STAND.effect,GOLD_TINT);}
}
