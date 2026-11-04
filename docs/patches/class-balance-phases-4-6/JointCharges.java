package net.spell_engine.internals.cost;
import net.minecraft.core.Holder;import net.minecraft.world.entity.Entity;import net.minecraft.world.entity.player.Player;import net.spell_engine.api.spell.Spell;
public final class JointCharges {
 public static boolean spend(Player p,Holder<Spell> h){return EchoCharges.spend(p,h)||BlinkCharges.spend(p,h);}
 public static void tick(SpellCooldownManager m,Entity owner){EchoCharges.tick(m,owner);BlinkCharges.tick(m,owner);}
 public static float progress(float value,SpellCooldownManager m,Entity owner,Holder<Spell> h,float delta){return BlinkCharges.progress(EchoCharges.progress(value,m,owner,h,delta),m,owner,h,delta);}
 public static int duration(int value,SpellCooldownManager m,Entity owner,Holder<Spell> h){return BlinkCharges.duration(EchoCharges.duration(value,m,owner,h),m,owner,h);}
}
