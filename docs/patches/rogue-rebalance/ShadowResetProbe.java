package local.audit;
import java.util.*;import java.util.function.Consumer;
import net.minecraft.core.Holder;import net.minecraft.resources.Identifier;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.ai.attributes.Attributes;
import net.spell_engine.api.spell.Spell;import net.spell_engine.api.spell.registry.SpellRegistry;import net.spell_engine.internals.*;import net.spell_engine.internals.casting.SpellCaster;import net.spell_engine.utils.WorldScheduler;
public final class ShadowResetProbe {
 static final Identifier ID=Identifier.parse("rogues:shadow_step");static final String KEY="rogue_pack_shadow_reset_until";
 static void check(Consumer<String> out,String label,boolean yes){if(!yes)throw new AssertionError(label);out.accept("Shadow reset PASS "+label);}
 static void arrive(ServerPlayer p,Holder<Spell> h,boolean success){RogueRules.after(success,p,p,h,h.value().impacts.getFirst());}
 static LivingEntity victim(ServerLevel l){var e=EntityTypes.ZOMBIE.create(l,EntitySpawnReason.COMMAND);e.setNoAi(true);e.setPos(8,80,8);e.setHealth(2);l.addFreshEntity(e);return e;}
 static void drain(ServerLevel l){var tasks=((WorldScheduler)l).getScheduledTasks().remove(((WorldScheduler)l).getSchedulerTime()+1);if(tasks!=null)for(var task:tasks)task.run();}
 public static void run(ServerLevel l,ServerPlayer p,Consumer<String> out){
  var h=SpellRegistry.from(l).get(ID).orElseThrow();var cd=((SpellCaster.Player)p).getCooldownManager();p.setHealth(p.getMaxHealth());p.getPersistentData().remove(KEY);
  cd.set(h,240);var e=victim(l);e.hurtServer(l,l.damageSources().playerAttack(p),100);drain(l);check(out,"kill without Shadowstep preserves cooldown",cd.isCoolingDown(h));e.discard();
  arrive(p,h,false);check(out,"failed teleport creates no reset window",p.getPersistentData().getLongOr(KEY,0)==0);
  arrive(p,h,true);check(out,"successful native callback opens eighty tick window",p.getPersistentData().getLongOr(KEY,0)==l.getServer().overworld().getGameTime()+80);
  e=victim(l);e.hurtServer(l,l.damageSources().playerAttack(p),100);cd.set(h,240);check(out,"reset waits until cast cooldown is imposed",cd.isCoolingDown(h));drain(l);check(out,"attributed enemy kill resets after cast cooldown",!cd.isCoolingDown(h));check(out,"reset consumes cast window",!p.getPersistentData().contains(KEY));e.discard();
  cd.set(h,240);e=victim(l);e.hurtServer(l,l.damageSources().playerAttack(p),100);drain(l);check(out,"second kill cannot reuse consumed window",cd.isCoolingDown(h));e.discard();
  arrive(p,h,true);p.getPersistentData().putLong(KEY,l.getServer().overworld().getGameTime());e=victim(l);e.hurtServer(l,l.damageSources().playerAttack(p),100);drain(l);check(out,"exact four second boundary is expired",cd.isCoolingDown(h));e.discard();
  arrive(p,h,true);e=victim(l);e.hurtServer(l,l.damageSources().generic(),100);drain(l);check(out,"unattributed kill cannot reset cooldown",cd.isCoolingDown(h));e.discard();
  arrive(p,h,true);var pet=EntityTypes.WOLF.create(l,EntitySpawnReason.COMMAND);pet.setOwnerReference(EntityReference.of(p.getUUID()));pet.setPos(8,80,8);pet.setHealth(2);l.addFreshEntity(pet);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(pet,l.damageSources().playerAttack(p)));pet.setHealth(0);drain(l);check(out,"owned pet death cannot reset cooldown",cd.isCoolingDown(h));pet.discard();
  arrive(p,h,true);e=victim(l);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(e,l.damageSources().playerAttack(p)));e.setHealth(2);drain(l);check(out,"surviving or revived target cannot reset cooldown",cd.isCoolingDown(h));e.discard();
  p.getPersistentData().remove(KEY);cd.remove(ID);
 }
}
