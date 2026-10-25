package net.skill_tree_rpgs.client.effect;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.spell_engine.api.effect.CustomParticleStatusEffect;
import net.spell_engine.api.effect.Synchronized;
import net.spell_engine.entity.BerserkerAppearance;
import net.spell_engine.entity.WarriorUltimates;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Sparse white firework sparks. No delayed jobs can outlive the buff. */
public final class BerserkerSparkles {
 private static final Map<LivingEntity,List<Particle>> live=new WeakHashMap<>();
 private static boolean registered;
 public static void register() {
  if(registered)return;registered=true;
  var effect=net.skill_tree_rpgs.effect.SkillEffects.entries.stream()
   .filter(e->e.id.toString().equals(WarriorUltimates.BERSERKER)).findFirst().orElseThrow().effect;
  CustomParticleStatusEffect.register(effect,(entity,amplifier)->{
   var state=Synchronized.effectsOf(entity).stream().filter(e->e.effect()==effect).findFirst();
   if(state.isEmpty()||!entity.isAlive())return;
   long age=entity.level().getGameTime()-state.get().appliedAtWorldTime();
   if(!BerserkerAppearance.sparkAt(age))return;
   var random=entity.getRandom();double width=entity.getBbWidth()*.7;
   double x=entity.getX()+(random.nextDouble()*2-1)*width;
   double y=entity.getY()+.15+random.nextDouble()*entity.getBbHeight()*.9;
   double z=entity.getZ()+(random.nextDouble()*2-1)*width;
   var particle=Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.FIREWORK,x,y,z,
    (random.nextDouble()-.5)*.025,random.nextDouble()*.025,(random.nextDouble()-.5)*.025);
   if(particle!=null){if(particle instanceof net.minecraft.client.particle.SingleQuadParticle quad)quad.setColor(1,1,1);particle.setLifetime(4);live.computeIfAbsent(entity,k->new ArrayList<>()).add(particle);}
  });
  NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event)->{
   var level=Minecraft.getInstance().level;
   live.entrySet().removeIf(entry->{
    var entity=entry.getKey();boolean active=entity.isAlive()&&!entity.isRemoved()&&entity.level()==level
     &&Synchronized.effectsOf(entity).stream().anyMatch(e->e.effect()==effect);
    if(!active){entry.getValue().forEach(Particle::remove);return true;}
    entry.getValue().removeIf(p->!p.isAlive());return entry.getValue().isEmpty();
   });
  });
 }
}
