package net.spell_engine.internals;
import net.spell_power.api.SpellSchool;
/** Null is Spell Power's valid all-schools vulnerability, not a failed cast. */
public final class ClassEffectLabels{public static String school(SpellSchool school){return school==null||school.id==null?"Spell":school.id.getPath();}}
