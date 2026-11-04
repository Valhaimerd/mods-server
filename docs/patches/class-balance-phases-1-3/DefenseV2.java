package net.valhaimerd.orboblivioncompat;
import java.util.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component;
import net.puffish.skillsmod.api.*;
import net.puffish.skillsmod.SkillsMod;
import net.valhaimerd.orboblivioncompat.mixin.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.spell_engine.internals.*;
public final class DefenseV2 {
 public static List<List<String>> PATHS;
 public static Set<String> ALL;
 private static boolean listening;
 public static void init(){
  if(listening)return;listening=true;
  try(var r=new InputStreamReader(DefenseV2.class.getResourceAsStream("/defense-v2.json"),StandardCharsets.UTF_8)){
   var data=JsonParser.parseReader(r).getAsJsonObject();PATHS=new ArrayList<>();ALL=new HashSet<>();
   for(var array:data.getAsJsonArray("paths")){List<String> path=new ArrayList<>();for(var id:array.getAsJsonArray()){path.add(id.getAsString());ALL.add(id.getAsString());}PATHS.add(List.copyOf(path));}
   for(var id:data.getAsJsonArray("removed"))ALL.add(id.getAsString());
  }catch(IOException ex){throw new UncheckedIOException(ex);}
  NeoForge.EVENT_BUS.addListener(DefenseV2::login);NeoForge.EVENT_BUS.addListener(DefenseV2::tick);NeoForge.EVENT_BUS.addListener(DefenseV2::damage);NeoForge.EVENT_BUS.addListener(DefenseV2::clonePlayer);
 }
 public static boolean allowed(Set<String> selected,String node){
  if(!Utilities.allowed(selected,node))return false;
  for(var path:PATHS)if(path.contains(node)){
   for(var other:PATHS)if(other!=path&&other.stream().anyMatch(selected::contains))return false;
   return !node.equals(path.get(7))||selected.containsAll(path.subList(0,7));
  }return true;
 }
 public static void migrate(ServerPlayer player){
  if(player.getPersistentData().getBooleanOr("defense_v2_refunded",false))return;
  SkillsAPI.getCategory(Utilities.CATEGORY).ifPresent(c->{
   var selected=c.streamUnlockedSkills(player).map(Skill::getId).filter(ALL::contains).toList();
   for(var id:selected)c.getSkill(id).ifPresent(s->s.lock(player));
   var data=((SkillsDataAccess)(Object)SkillsMod.getInstance()).utilities$playerData(player);
   var category=((PlayerDataAccess)(Object)data).utilities$categories().get(Utilities.CATEGORY);
   if(category!=null)category.getUnlockedSkillIds().removeAll(ALL);
   player.getPersistentData().putBoolean("defense_v2_refunded",true);
   if(!selected.isEmpty())player.sendSystemMessage(Component.literal("Defense updated: defense points refunded. Choose Aegis, Colossus or Bulwark."));
  });
 }
 private static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p)migrate(p);}
 private static void clonePlayer(PlayerEvent.Clone event){
  for(var key:List.of("defense_v2_refunded","defense_v2_aegis_cd","defense_v2_colossus_cd"))if(event.getOriginal().getPersistentData().contains(key))event.getEntity().getPersistentData().put(key,event.getOriginal().getPersistentData().get(key).copy());
 }
 public static boolean mastery(ServerPlayer p,int branch){return Utilities.unlocked(p).containsAll(PATHS.get(branch));}
 private static boolean direct(ServerPlayer p,LivingDamageEvent.Post event){var s=event.getSource();return s.getEntity() instanceof LivingEntity enemy&&enemy!=p&&s.getDirectEntity()!=null&&PartyRules.harmfulAllowed(enemy,p)&&!s.is(net.minecraft.tags.DamageTypeTags.BYPASSES_EFFECTS)&&!s.is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE)&&!s.is(net.minecraft.world.damagesource.DamageTypes.WITHER)&&!s.is(net.minecraft.world.damagesource.DamageTypes.FREEZE);}
 private static void damage(LivingDamageEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p)||!Utilities.active(p)||event.getInflictedDamage()<=0||!direct(p,event))return;
  long now=Utilities.now(p);var data=p.getPersistentData();
  if(mastery(p,0)&&now>=data.getLongOr("defense_v2_aegis_cd",0)){
   p.addEffect(new MobEffectInstance(PaladinRevamp.effect("skill_tree_rpgs:aegis_guard"),60));data.putLong("defense_v2_aegis_cd",now+400);
  }
  if(mastery(p,1)&&p.getHealth()<p.getMaxHealth()*.4&&now>=data.getLongOr("defense_v2_colossus_cd",0)){
   data.putLong("defense_v2_colossus_cd",now+600);data.putLong("defense_v2_heal_at",now+20);data.putInt("defense_v2_heals",5);data.putFloat("defense_v2_heal",p.getMaxHealth()*.02f);
  }
 }
 private static void tick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  var guard=PaladinRevamp.effect("skill_tree_rpgs:aegis_guard");if(p.hasEffect(guard)&&(!Utilities.active(p)||!mastery(p,0)))p.removeEffect(guard);
  var d=p.getPersistentData();int left=d.getIntOr("defense_v2_heals",0);if(left<=0)return;
  if(!Utilities.active(p)||!mastery(p,1)){d.remove("defense_v2_heals");return;}
  long now=Utilities.now(p);if(now>=d.getLongOr("defense_v2_heal_at",Long.MAX_VALUE)){p.heal(d.getFloatOr("defense_v2_heal",0));d.putInt("defense_v2_heals",left-1);d.putLong("defense_v2_heal_at",now+20);}
 }
}
