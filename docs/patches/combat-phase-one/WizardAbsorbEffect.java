package net.skill_tree_rpgs.effect;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.spell_engine.internals.PriestRules;
import java.util.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.spell_engine.internals.EffectDetails;

/** Amplifier is the number of complete eight-power groups, with no extra base heart. */
public class WizardAbsorbEffect extends MobEffect {
 private static boolean installed;
 public WizardAbsorbEffect(MobEffectCategory category,int color){super(category,color);install();}
 private static String key(MobEffect effect){return net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect).toString();}
 private static void clear(LivingEntity entity,MobEffectInstance effect){if(effect==null||!(effect.getEffect().value() instanceof WizardAbsorbEffect)||!entity.hasData(EffectDetails.DATA))return;var root=EffectDetails.data(entity);EffectDetails.object(root,"mage_ward_capacities").remove(key(effect.getEffect().value()));EffectDetails.save(entity,root);}
 private static void install(){if(installed)return;installed=true;
  NeoForge.EVENT_BUS.addListener((MobEffectEvent.Remove e)->{if(!e.getEntity().level().isClientSide())clear(e.getEntity(),e.getEffectInstance());});
  NeoForge.EVENT_BUS.addListener((MobEffectEvent.Expired e)->{if(!e.getEntity().level().isClientSide())clear(e.getEntity(),e.getEffectInstance());});
 }
 @Override public boolean applyEffectTick(ServerLevel world,LivingEntity e,int amplifier){return amplifier>0&&e.getAbsorptionAmount()>0;}
 @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
 @Override public void createModifiers(int amplifier,BiConsumer<Holder<Attribute>,AttributeModifier> consumer){super.createModifiers(amplifier-1,consumer);}
 @Override public void addAttributeModifiers(AttributeMap map,int amplifier){super.addAttributeModifiers(map,amplifier-1);}
 @Override public void onEffectStarted(LivingEntity e,int amplifier){
  if(e.level().isClientSide())return;
  if(amplifier==0){e.removeEffect(net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));return;}
  var root=EffectDetails.data(e);var capacities=EffectDetails.object(root,"mage_ward_capacities");
  int old=capacities.entrySet().stream().mapToInt(v->v.getValue().getAsInt()).max().orElse(0),capacity=amplifier*2;
  capacities.addProperty(key(this),Math.max(capacities.has(key(this))?capacities.get(key(this)).getAsInt():0,capacity));EffectDetails.save(e,root);
  e.setAbsorptionAmount(Math.min((float)e.getAttributeValue(Attributes.MAX_ABSORPTION),e.getAbsorptionAmount()+Math.max(0,capacity-old)));
 }
}
