package net.spell_engine.internals;
import java.util.*;import net.minecraft.core.Holder;import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;import net.spell_engine.api.spell.Spell;import net.spell_engine.internals.impact.SpellImpacts;
public final class FinalPriest {
 public static boolean ascension(Level level,Holder<Spell> h,LivingEntity caster,List<SpellExecution.DeliveryTarget> targets,SpellExecution.ImpactContext ctx,Vec3 ignored){
  if(level.isClientSide()||!MageFollowup.active(caster))return false;
  int duration=PriestRules.node(caster,"tier_2_spell_2_root")?160:120;
  var recipients=new LinkedHashSet<>(PriestRules.nearby(caster,caster.position(),8));recipients.add(caster);
  for(var recipient:recipients){
   if(PartyRules.allies(caster,recipient)){
    PriestRules.statusStack(recipient,PriestRules.AURA,4,duration,caster);
    if(PriestRules.node(caster,"tier_2_spell_2_modifier_2")){int amount=1+(int)Math.round(.1*ctx.power().nonCriticalValue());PriestRules.stack(recipient,PriestRules.SERENITY,amount,4*amount,duration,caster);}
    boolean lift=recipient==caster||PriestRules.node(caster,"tier_2_spell_2_modifier_1")&&recipient.getBoundingBox().distanceToSqr(caster.position())<=9;
    if(lift){SpellImpacts.performImpacts(level,caster,recipient,caster,h,h.value().impacts,ctx,false,null);if(recipient.hasEffect(PriestRules.effect("paladins:levitate")))FinalRules.status(recipient,"ascension_guard",120,0,caster);}
   }else if(PartyRules.harmfulAllowed(caster,recipient)){
    double x=caster.getX()-recipient.getX(),z=caster.getZ()-recipient.getZ();if(x*x+z*z<.0001)x=.01;recipient.knockback(ctx.channelTickIndex()>=3?5:2,x,z,level.damageSources().magic(),0,true);
   }
  }return true;
 }
}
