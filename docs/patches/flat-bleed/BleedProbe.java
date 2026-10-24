package local.audit;
import java.util.*;import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.resources.Identifier;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.effect.*;import net.minecraft.world.phys.Vec3;
public final class BleedProbe {
 static void check(Consumer<String> out,String label,boolean yes){if(!yes)throw new AssertionError(label);out.accept("Flat Bleed PASS "+label);}
 static LivingEntity mob(ServerLevel level){var target=net.minecraft.world.entity.EntityTypes.VILLAGER.create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setNoAi(true);target.setPos(8,80,8);level.addFreshEntity(target);return target;}
 public static void run(ServerLevel level,ServerPlayer player,Consumer<String> out)throws Exception{
  player.setPos(7,80,8);
  var h=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("spell_engine:bleed")).orElseThrow();var bleed=h.value();out.accept("OBSERVED Bleed class "+bleed.getClass().getName());var target=mob(level);
  try {
   for(int amp:new int[]{0,1,11,99})for(Vec3 movement:List.of(Vec3.ZERO,new Vec3(0,.3,0),new Vec3(.005,0,0),new Vec3(.3,0,0))){
    target.setDeltaMovement(movement);target.invulnerableTime=0;target.setHealth(1000);bleed.applyEffectTick(level,target,amp);double expected=movement.horizontalDistanceSqr()>1e-6?6:2;
    check(out,"fixed damage amplifier="+amp+" velocity="+movement,Math.abs((1000-target.getHealth())-expected)<.001);
   }
   for(int amp:new int[]{0,1,11,99}){var instance=new MobEffectInstance(h,100,amp);check(out,"all constructors normalize to level one "+amp,instance.getAmplifier()==0);target.addEffect(instance);check(out,"application stays level one "+amp,target.getEffect(h).getAmplifier()==0);
    check(out,"copy preserves level one "+amp,new MobEffectInstance(instance).getAmplifier()==0);
    var ops=level.registryAccess().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);var legacy=(net.minecraft.nbt.CompoundTag)MobEffectInstance.CODEC.encodeStart(ops,instance).getOrThrow();legacy.putInt("amplifier",amp);var loaded=MobEffectInstance.CODEC.parse(ops,legacy).getOrThrow();check(out,"legacy save decodes to level one "+amp,loaded.getAmplifier()==0);}
   for(String node:List.of("rogue_tier_3_spell_2_modifier_1","rogue_tier_4_spell_2_modifier_2"))for(double power:new double[]{1,40,400}){
    var entry=net.spell_engine.api.spell.registry.SpellRegistry.from(level).get(Identifier.parse("skill_tree_rpgs:"+node)).orElseThrow();
    var base=net.spell_engine.api.spell.registry.SpellRegistry.from(level).get(Identifier.parse(node.contains("tier_3")?"rogues:bear_trap":"rogues:mutilate")).orElseThrow();
    target.removeAllEffects();var context=new net.spell_engine.internals.SpellExecution.ImpactContext(1,1,null,new net.spell_power.api.SpellPower.Result(base.value().school,power,0,1),net.spell_engine.internals.target.SpellTarget.FocusMode.DIRECT,0);
    var apply=net.spell_engine.internals.impact.SpellImpacts.class.getDeclaredMethod("performImpact",net.minecraft.world.level.Level.class,LivingEntity.class,net.minecraft.world.entity.Entity.class,net.minecraft.core.Holder.class,net.spell_engine.api.spell.Spell.Impact.class,net.spell_engine.internals.SpellExecution.ImpactContext.class,java.util.Collection.class);apply.setAccessible(true);
    apply.invoke(null,level,player,target,base,entry.value().modifiers.getFirst().impacts.getFirst(),context,null);
    check(out,node+" uses level one at power="+power,target.getEffect(h).getAmplifier()==0);
    check(out,node+" retains duration at power="+power,target.getEffect(h).getDuration()==(node.contains("tier_3")?50:150));
   }
   check(out,"other effects retain levels",new MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,100,3).getAmplifier()==3);
   check(out,"tick at twenty-five",bleed.shouldApplyEffectTickThisTick(25,0));check(out,"no tick between pulses",!bleed.shouldApplyEffectTickThisTick(24,0));check(out,"tick at fifty",bleed.shouldApplyEffectTickThisTick(50,0));
  }finally{target.discard();}
 }
}
