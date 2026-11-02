package net.spell_engine.internals;
/** Separate synchronized Vanish flag: ordinary invisibility remains targetable. */
public interface SpatialState { boolean spatial$vanished(); void spatial$vanished(boolean value); }
