package net.spell_engine.internals.cost;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.internals.SpellParameters;
import net.spell_engine.internals.casting.SpellCaster;
import net.spell_engine.internals.container.SpellContainerSource;

/** Two serial charges, using native saved/synchronized cooldown entries. */
public final class EchoCharges {
 private static boolean installed;
 public static final Identifier SPELL=Identifier.parse("wizards:arcane_explosion");
 public static final Identifier HEAD=Identifier.parse("spell_engine:echo_recharge"),TAIL=Identifier.parse("spell_engine:echo_queued");
 public static void init(){if(installed)return;installed=true;net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event)->{
  if(event.getEntity().level().isClientSide())return;
  copy(((SpellCaster.Player)event.getOriginal()).getCooldownManager(),((SpellCaster.Player)event.getEntity()).getCooldownManager(),event.getEntity());
 });
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event)->syncAfterRecreation(event.getEntity()));
  net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event)->syncAfterRecreation(event.getEntity()));
 }
 private static void syncAfterRecreation(Player p){if(!p.level().isClientSide())((net.spell_engine.utils.WorldScheduler)p.level()).schedule(1,()->((SpellCaster.Player)p).getCooldownManager().pushSync());}
 public static void copy(SpellCooldownManager old,SpellCooldownManager fresh,Player p){
  var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,p.registryAccess());var list=output.childrenList("spell_engine_cooldowns");
  for(var id:java.util.List.of(HEAD,TAIL,SPELL)){int left=old.getCooldownDuration(id);if(left<=0)continue;float progress=old.getCooldownProgress(id,0);int total=Math.max(left,Math.round(left/Math.max(.000001f,progress)));var row=list.addChild();row.putString("spell",id.toString());row.putInt("start",left-total);row.putInt("end",left);}
  fresh.readCustomData(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,p.registryAccess(),output.buildResult()));fresh.pushSync();
 }
 public static boolean selected(Player p){return SpellContainerSource.passiveSpellsOf(p).stream().anyMatch(h->h.is(Identifier.parse("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1")));}
 private static int interval(Player p,Holder<Spell> h){return Math.max(1,Math.round(SpellParameters.getCooldownDuration(p,h)*20));}
 public static int available(SpellCooldownManager m){return m.getCooldownDuration(TAIL)>0?0:m.getCooldownDuration(HEAD)>0?1:2;}
 public static float recharge(SpellCooldownManager m){return m.getCooldownProgress(HEAD,0);}
 public static boolean spend(Player p,Holder<Spell> h){
  if(!h.is(SPELL)||!selected(p))return false;
  var m=((SpellCaster.Player)p).getCooldownManager();
  if(m.getCooldownDuration(SPELL)>0||available(m)==0)return true;
  int t=interval(p,h);
  if(m.getCooldownDuration(HEAD)==0)m.set(HEAD,t,true);
  else m.set(TAIL,m.getCooldownDuration(HEAD)+t,true);
  // Incremental native packets resolve a spell registry holder and ignore internal queue IDs.
  // Full sync already supports arbitrary cooldown keys and carries the authoritative clock.
  m.pushSync();
  return true;
 }
 public static void tick(SpellCooldownManager m,Entity owner){
  if(m.getCooldownDuration(HEAD)>0||m.getCooldownDuration(TAIL)<=0)return;
  int next=m.getCooldownDuration(TAIL);
  if(owner instanceof Player p&&!p.level().isClientSide()){
   var h=SpellRegistry.from(p.level()).get(SPELL).orElse(null);if(h!=null)next=interval(p,h);
  }
  m.remove(TAIL);m.set(HEAD,next,true);if(!owner.level().isClientSide())m.pushSync();
 }
 public static float progress(float nativeValue,SpellCooldownManager m,Entity owner,Holder<Spell> h,float delta){
  if(!h.is(SPELL)||!(owner instanceof Player p))return nativeValue;
  float queued=(!selected(p)||available(m)==0)?m.getCooldownProgress(HEAD,delta):0;
  return Math.max(nativeValue,queued);
 }
 public static int duration(int nativeValue,SpellCooldownManager m,Entity owner,Holder<Spell> h){
  if(!h.is(SPELL)||!(owner instanceof Player p))return nativeValue;
  return Math.max(nativeValue,(!selected(p)||available(m)==0)?m.getCooldownDuration(HEAD):0);
 }
}
