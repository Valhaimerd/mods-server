package net.valhaimerd.orboblivioncompat;
import com.google.gson.*;
import net.valhaimerd.orboblivioncompat.mixin.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.*;
import net.puffish.skillsmod.api.*;
import net.puffish.skillsmod.SkillsMod;

/** Utilities only. Native tree rewards own synchronized stats; no global XP or combat multipliers. */
public final class Utilities {
 public static final String PREFIX="utilities_v1_";
 public static final Identifier CATEGORY=SkillOverhaul.CATEGORY;
 public static final DeferredRegister<Attribute> ATTRS=DeferredRegister.create(Registries.ATTRIBUTE,OrbOblivionPuffishCompat.MOD_ID);
 public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,OrbOblivionPuffishCompat.MOD_ID);
 public static final DeferredHolder<MobEffect,MobEffect> SNOW_GUARD=EFFECTS.register("sure_footing",()->new MobEffect(MobEffectCategory.BENEFICIAL,0x8bd5ed){});
 public static final Map<String,DeferredHolder<Attribute,Attribute>> STATS=new LinkedHashMap<>();
 public static final List<List<String>> PATHS=new ArrayList<>();
 public static final Set<String> ALL=new HashSet<>();
 public static final ThreadLocal<Integer> CONSUMING=ThreadLocal.withInitial(()->0);
 public static final ThreadLocal<Deque<LivingEntity>> CONSUMERS=ThreadLocal.withInitial(ArrayDeque::new);
 public static final ThreadLocal<Integer> TOOL_USE=ThreadLocal.withInitial(()->0);
 static {
  for(String key:List.of("food","breath","climb","freeze","cost","xp","durability","enchant_fraction"))
   STATS.put(key,ATTRS.register("utility_"+key,()->new RangedAttribute("attribute.modpack.utility_"+key,0,0,1).setSyncable(true)));
  try(var in=Utilities.class.getResourceAsStream("/utilities-nodes.json")) {
   var j=JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(in),StandardCharsets.UTF_8)).getAsJsonObject();
   for(var path:j.getAsJsonArray("paths")){var ids=new ArrayList<String>();for(var n:path.getAsJsonArray())ids.add(n.getAsString());PATHS.add(List.copyOf(ids));ALL.addAll(ids);}
   for(var n:j.getAsJsonArray("removed"))ALL.add(n.getAsString());
   ALL.add(j.get("shared").getAsString());ALL.add(j.get("root").getAsString());
  }catch(IOException ex){throw new UncheckedIOException(ex);}
 }
 public static void init(IEventBus bus){
  ATTRS.register(bus);EFFECTS.register(bus);bus.addListener(Utilities::attributes);
  NeoForge.EVENT_BUS.addListener(Utilities::login);
  NeoForge.EVENT_BUS.addListener(Utilities::clonePlayer);
  NeoForge.EVENT_BUS.addListener(Utilities::tick);
  NeoForge.EVENT_BUS.addListener(Utilities::incoming);
  NeoForge.EVENT_BUS.addListener(Utilities::damage);
  NeoForge.EVENT_BUS.addListener(Utilities::logout);
 }
 private static void attributes(EntityAttributeModificationEvent e){for(var a:STATS.values())e.add(EntityTypes.PLAYER,a);}
 public static double stat(LivingEntity e,String key){double v=e.getAttribute(STATS.get(key))==null?0:e.getAttributeValue(STATS.get(key));return key.equals("enchant_fraction")?v:Math.rint(v*1_000_000)/1_000_000;}
 public static Set<String> unlocked(ServerPlayer p){return SkillsAPI.getCategory(CATEGORY).map(c->c.streamUnlockedSkills(p).map(Skill::getId).collect(java.util.stream.Collectors.toSet())).orElse(Set.of());}
 public static boolean mastery(ServerPlayer p,int path){return unlocked(p).containsAll(PATHS.get(path)) && PATHS.stream().filter(ids->unlocked(p).stream().anyMatch(ids::contains)).count()==1;}
 public static boolean allowed(Set<String> allocated,String node){
  int branch=-1;for(int i=0;i<PATHS.size();i++)if(PATHS.get(i).contains(node))branch=i;
  if(branch<0)return true;
  for(int i=0;i<PATHS.size();i++)if(i!=branch&&PATHS.get(i).stream().anyMatch(allocated::contains))return false;
  return !node.equals(PATHS.get(branch).get(7))||allocated.containsAll(PATHS.get(branch).subList(0,7));
 }
 public static void migrate(ServerPlayer p){
  if(p.getPersistentData().getBooleanOr(PREFIX+"migrated",false))return;
  SkillsAPI.getCategory(CATEGORY).ifPresent(c->{
   // Native lock updates rewards, spending and client state without changing earned points/XP.
   var selected=c.streamUnlockedSkills(p).map(Skill::getId).filter(ALL::contains).toList();
   for(String id:selected)c.getSkill(id).ifPresent(s->s.lock(p));
   var data=((SkillsDataAccess)(Object)SkillsMod.getInstance()).utilities$playerData(p);
   var category=((PlayerDataAccess)(Object)data).utilities$categories().get(CATEGORY);
   if(category!=null)category.getUnlockedSkillIds().removeAll(ALL); // also retire IDs absent from new config
   p.getPersistentData().putBoolean(PREFIX+"migrated",true);
   if(!selected.isEmpty())p.sendSystemMessage(Component.literal("Utilities updated: utility points refunded. Choose Fitness, Adaptability or Ingenuity."));
  });
 }
 private static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){migrate(p);p.getAttribute(STATS.get("enchant_fraction")).setBaseValue(p.getPersistentData().getDoubleOr(PREFIX+"enchant_fraction",0));}}
 private static void clonePlayer(PlayerEvent.Clone e){
  for(String k:e.getOriginal().getPersistentData().keySet())if(k.startsWith(PREFIX)&&!k.equals(PREFIX+"snow_until"))
   e.getEntity().getPersistentData().put(k,e.getOriginal().getPersistentData().get(k).copy());
 }
 private static void logout(PlayerEvent.PlayerLoggedOutEvent e){e.getEntity().getPersistentData().remove(PREFIX+"snow_until");e.getEntity().removeEffect(SNOW_GUARD);}
 public static long now(ServerPlayer p){return p.level().getServer().overworld().getGameTime();}
 public static boolean active(ServerPlayer p){
  if(!p.isAlive()||p.isSpectator())return false;
  if(net.neoforged.fml.ModList.get().isLoaded("playerrevive")&&team.creative.playerrevive.server.PlayerReviveServer.isBleeding(p))return false;
  return !net.neoforged.fml.ModList.get().isLoaded("better_revive")||!de.mrstilu2k6.better_revive.util.DownedPlayerManager.isDowned(p.getUUID());
 }
 private static void tick(EntityTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%10!=0||!active(p))return;
  if(p.getFoodData().getFoodLevel()<=6&&now(p)>=p.getPersistentData().getLongOr(PREFIX+"rations_until",0)&&mastery(p,0)){
   p.getFoodData().setFoodLevel(Math.min(20,p.getFoodData().getFoodLevel()+8));
   p.getPersistentData().putLong(PREFIX+"rations_until",now(p)+3600);
  }
 }
 public static boolean snow(ServerPlayer p){return active(p)&&now(p)<p.getPersistentData().getLongOr(PREFIX+"snow_until",0);}
 private static void incoming(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getSource().is(DamageTypes.FREEZE)&&snow(p))e.setCanceled(true);}
 private static void damage(LivingDamageEvent.Pre e){
  if(e.getEntity() instanceof ServerPlayer p&&e.getSource().is(DamageTypes.FALL)&&e.getNewDamage()>0&&active(p)&&mastery(p,1)&&now(p)>=p.getPersistentData().getLongOr(PREFIX+"fall_until",0)){
   e.setNewDamage(e.getNewDamage()*.5f);p.getPersistentData().putLong(PREFIX+"fall_until",now(p)+400);p.getPersistentData().putLong(PREFIX+"snow_until",now(p)+100);p.addEffect(new MobEffectInstance(SNOW_GUARD,100,0,false,false,true));
  }
 }
 public static MobEffectInstance extend(LivingEntity entity,MobEffectInstance effect){
  if(CONSUMING.get()>0&&CONSUMERS.get().peek()==entity&&entity instanceof ServerPlayer p&&mastery(p,2)&&effect.getEffect().value().isBeneficial()&&!effect.getEffect().value().isInstantaneous()&&!effect.isInfiniteDuration())
   return new MobEffectInstance(effect.getEffect(),Math.round(effect.getDuration()*1.35f),effect.getAmplifier(),effect.isAmbient(),effect.isVisible(),effect.showIcon());
  return effect;
 }
 public static int xp(ServerPlayer p,int amount){
  if(amount<=0)return amount;double b=stat(p,"xp");if(b==0)return amount;
  double sum=amount*b+p.getPersistentData().getDoubleOr(PREFIX+"xp_fraction",0);int extra=(int)Math.floor(sum+1e-9);
  p.getPersistentData().putDouble(PREFIX+"xp_fraction",sum-extra);return amount+extra;
 }
 public static int anvil(Player p,int nativeCost){return nativeCost<=0?nativeCost:Math.max(1,(int)Math.ceil(nativeCost*(1-stat(p,"cost"))-1e-8));}
 public static int levelXp(int level){return level>=30?112+(level-30)*9:level>=15?37+(level-15)*5:7+level*2;}
 public static int totalAt(int level){if(level<=16)return level*level+6*level;if(level<=31)return (int)(2.5*level*level-40.5*level+360);return (int)(4.5*level*level-162.5*level+2220);}
 public static int enchantDebit(int level,float progress,int levels){int after=Math.max(0,level-levels);return Math.max(0,totalAt(level)+(int)(progress*levelXp(level))-totalAt(after)-(int)(progress*levelXp(after)));}
 public static void payEnchant(Player player,int negativeLevels){
  if(!(player instanceof ServerPlayer p)||negativeLevels>=0||stat(p,"cost")==0){player.giveExperienceLevels(negativeLevels);return;}
  int nativeDebit=enchantDebit(p.experienceLevel,p.experienceProgress,-negativeLevels);
  double raw=nativeDebit*(1-stat(p,"cost"))+p.getPersistentData().getDoubleOr(PREFIX+"enchant_fraction",0);
  int debit=Math.min(nativeDebit,Math.max(nativeDebit>0?1:0,(int)Math.floor(raw+1e-8)));
  p.getPersistentData().putDouble(PREFIX+"enchant_fraction",Math.max(0,raw-debit));
  p.getAttribute(STATS.get("enchant_fraction")).setBaseValue(Math.min(1,Math.max(0,raw-debit)));
  p.giveExperiencePoints(-debit);
 }
 public static int shownEnchantDebit(Player p,int levels){int nativeDebit=enchantDebit(p.experienceLevel,p.experienceProgress,levels);return Math.min(nativeDebit,Math.max(nativeDebit>0?1:0,(int)Math.floor(nativeDebit*(1-stat(p,"cost"))+stat(p,"enchant_fraction")+1e-8)));}
}
