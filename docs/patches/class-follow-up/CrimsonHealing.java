package net.spell_engine.internals;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.internals.container.SpellContainerSource;
import net.minecraft.resources.Identifier;
public final class CrimsonHealing {
 private static final net.spell_engine.api.spell.Spell.Impact FX=readFx();
 private static net.spell_engine.api.spell.Spell.Impact readFx(){
  try(var reader=new java.io.InputStreamReader(CrimsonHealing.class.getResourceAsStream("/crimson_heal.json"),java.nio.charset.StandardCharsets.UTF_8)){
   return new com.google.gson.Gson().fromJson(reader,net.spell_engine.api.spell.Spell.Impact.class);
  }catch(java.io.IOException error){throw new IllegalStateException(error);}
 }
 public static final class Frame { public final double power;public boolean healed;Frame(double power){this.power=power;} }
 public static final ThreadLocal<Frame> FRAME=new ThreadLocal<>();
 public static Frame begin(Player player){var old=FRAME.get();FRAME.set(new Frame(net.spell_power.api.SpellPower.getSpellPower(net.spell_engine.api.spell.registry.SpellRegistry.from(player.level()).get(Identifier.parse("rogues:mutilate")).orElseThrow().value().school,player).nonCriticalValue()));return old;}
 public static void end(Frame previous){if(previous==null)FRAME.remove();else FRAME.set(previous);}
 public static double health(LivingEntity target){return target.getHealth()+target.getAbsorptionAmount();}
 public static void hit(Player player,LivingEntity target,double before){
  var frame=FRAME.get();if(frame==null||frame.healed||health(target)>=before||!PartyRules.harmfulAllowed(player,target))return;
  boolean crimson=player instanceof SpellContainerSource.Owner owner&&owner.getSpellContainers().modifiers().stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:rogue_tier_4_spell_2_modifier_1")));
  frame.healed=true;player.heal((float)((crimson?.2:.1)*frame.power));
  net.spell_engine.utils.SoundHelper.playSound(player.level(),player,FX.sound);
  for(var particles:FX.visuals.particles)net.spell_engine.fx.ParticleHelper.play(player.level(),player,player.getYRot(),player.getXRot(),particles);
 }
}
