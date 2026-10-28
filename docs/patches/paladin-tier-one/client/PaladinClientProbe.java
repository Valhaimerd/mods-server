package local.paladinclient;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.puffish.skillsmod.api.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.entity.SpellProjectile;
import net.spell_power.api.*;

@Mod(value="paladin_client_probe",dist=Dist.CLIENT)
public class PaladinClientProbe {
 final List<String> results=Collections.synchronizedList(new ArrayList<>());final List<Long> launches=Collections.synchronizedList(new ArrayList<>());
 final AtomicReference<String> pending=new AtomicReference<>();final Set<java.util.UUID> seenWaves=new HashSet<>();int ticks,phase,phaseAt,shotAt=-1,clientWaves;boolean hidden,done;LivingEntity flying;
 interface Work{void run(ServerPlayer p)throws Exception;}
 void check(boolean b,String s){results.add((b?"PASS ":"FAIL ")+s);System.out.println("PALADIN_CLIENT "+results.getLast());}
 void near(double a,double b,String s){check(Math.abs(a-b)<.03,s+" actual="+a+" expected="+b);}
 void server(Work work){pending.set("waiting");var mc=Minecraft.getInstance();mc.getSingleplayerServer().execute(()->{try{work.run(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));pending.set("ready");}catch(Throwable e){e.printStackTrace();check(false,"server fixture "+e);pending.set("failed");}});}
 void next(){phase++;phaseAt=ticks;}
 void shot(String s){Screenshot.grab(Minecraft.getInstance(),false);results.add("SCREENSHOT "+s);}
 void nodes(ServerPlayer p,String...keys){var owner=(SpellContainerSource.Owner)p;var old=owner.getSpellContainers();owner.setSpellContainers(new SpellContainerSource.Result(old.activeContainer(),old.actives(),old.passives(),Arrays.stream(keys).map(k->PaladinTierOne.spell(p,"dummy")).toList(),old.sources()));}
 public PaladinClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent event)->{
  if(event.getEntity() instanceof SpellProjectile wave&&wave.getSpellEntry()!=null&&wave.getSpellEntry().is(Identifier.parse("paladins:blessed_wave"))){
   if(!event.getLevel().isClientSide()){launches.add(event.getLevel().getGameTime());check(wave.getDeltaMovement().y>.1,"native server swing wave inherits upward pitch");}
  }
 });}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();if(!hidden){org.lwjgl.glfw.GLFW.glfwHideWindow(mc.getWindow().handle());mc.options.pauseOnLostFocus=false;hidden=true;}if(done||mc.player==null||mc.level==null)return;ticks++;
 try{
  for(var entity:mc.level.entitiesForRendering())if(entity instanceof SpellProjectile wave&&wave.getSpellEntry()!=null&&wave.getSpellEntry().is(Identifier.parse("paladins:blessed_wave"))&&seenWaves.add(wave.getUUID())){clientWaves++;shotAt=ticks+2;}
  if(ticks==shotAt)shot("traveling golden wave "+clientWaves);
  if(ticks>1200)throw new AssertionError("timeout phase="+phase);
  if(phase==0&&ticks==30){server(p->{p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);for(var old:p.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,p.getBoundingBox().inflate(100)))old.discard();p.getInventory().clearContent();for(var slot:EquipmentSlot.VALUES)if(slot.isArmor())p.setItemSlot(slot,ItemStack.EMPTY);var c=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();c.unlock(p);c.resetSkills(p);c.setExtraPoints(p,1000);c.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(40);p.getAttribute(SpellSchools.HEALING.attributeEntry).setBaseValue(20);p.getAttribute(SpellPowerMechanics.CRITICAL_CHANCE.attributeEntry).setBaseValue(0);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(0,new ItemStack(Items.IRON_SWORD));p.inventoryMenu.broadcastFullState();mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),"tp @s 0.5 102 0.5 0 -23");p.setHealth(p.getMaxHealth());p.removeEffect(PaladinTierOne.chargeEffect());for(int i=0;i<5;i++)PaladinTierOne.prepare(p.level(),PaladinTierOne.spell(p,"blessed_strikes"),p,List.of(),new SpellExecution.ImpactContext(1,1,p.position(),SpellPower.getSpellPower(SpellSchools.HEALING,p),SpellTarget.FocusMode.DIRECT,i),null);
   flying=EntityTypes.HUSK.create(p.level(),EntitySpawnReason.COMMAND);flying.setPos(.5,108,14.5);flying.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);flying.getAttribute(Attributes.ARMOR).setBaseValue(0);flying.setHealth(1000);((net.minecraft.world.entity.Mob)flying).setNoAi(true);flying.setNoGravity(true);p.level().addFreshEntity(flying);
  });next();return;}
  if(phase==0)return;if("waiting".equals(pending.get())||ticks-phaseAt<25)return;if("failed".equals(pending.get())){finish();return;}
  switch(phase){
   case 1->{mc.player.getInventory().setSelectedSlot(0);mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(0));check(mc.player.getMainHandItem().is(Items.IRON_SWORD),"real client equipped melee weapon "+mc.player.getMainHandItem());check(mc.player.getEffect(PaladinTierOne.chargeEffect())!=null&&mc.player.getEffect(PaladinTierOne.chargeEffect()).getAmplifier()==4,"five prepared charges synchronized");check(mc.player.getXRot()<-20,"client aiming toward distant flying target");shot("five prepared charges and glint");mc.setScreenAndShow(null);mc.mouseHandler.grabMouse();var aim=flying.getBoundingBox().getCenter().subtract(mc.player.getEyePosition());mc.player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));mc.player.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,Math.sqrt(aim.x*aim.x+aim.z*aim.z))));mc.options.keyAttack.setDown(true);next();}
   case 2->{if(ticks-phaseAt<130)return;mc.options.keyAttack.setDown(false);mc.mouseHandler.releaseMouse();check(launches.size()==5,"held left-click released exactly five server waves "+launches);check(clientWaves==5,"all five waves synchronized to client "+clientWaves);for(int i=1;i<launches.size();i++)check(launches.get(i)-launches.get(i-1)>=10,"waves follow melee intervals, delta="+(launches.get(i)-launches.get(i-1)));server(p->{near(PaladinTierOne.charges(p),0,"holding cannot retain consumed charges");check(flying.getHealth()<1000,"actual distant flying enemy damaged by aimed waves, hp="+flying.getHealth());});next();}
   case 3->{check(mc.player.getEffect(PaladinTierOne.chargeEffect())==null,"spent charge effect/glint removed on client");var lang=net.minecraft.client.resources.language.I18n.get("spell.paladins.flash_heal.name");check(lang.equals("Purge Heal"),"native spell renamed "+lang);check(net.minecraft.client.resources.language.I18n.get("skill.skill_tree_rpgs.paladin_tier_2_spell_1_modifier_2.title").equals("Absolution"),"native node renamed Absolution");check(!net.minecraft.client.resources.language.I18n.get("spell.skill_tree_rpgs.paladin_tier_2_spell_2_modifier_1.description").contains("%%"),"description renders normal percent signs");server(p->SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow().openScreen(p));next();}
   case 4->{check(mc.gui.screen() instanceof net.puffish.skillsmod.client.gui.SkillsScreen,"native skill tree opens with new nodes");shot("Paladin revised skill tree");mc.setScreenAndShow(null);server(p->{for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){String piece=switch(slot){case HEAD->"head";case CHEST->"chest";case LEGS->"legs";default->"feet";};p.setItemSlot(slot,new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("armory_rpgs:justicar_armor_"+piece))));}});next();}
   case 5->{near(mc.player.getAttributeValue(SpellSchools.HEALING.attributeEntry),28.25,"Justicar grants exactly8 total including native set bonus");shot("Justicar eight Healing Power set");server(p->{var c=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();c.resetSkills(p);c.getSkill("ov6ag9tjdhsmldil").orElseThrow().unlock(p);});next();}
   case 6->{check(List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET).stream().allMatch(slot->mc.player.getItemBySlot(slot).isEmpty()),"Priest doctrine still rejects all equipped Justicar pieces");near(mc.player.getAttributeValue(SpellSchools.HEALING.attributeEntry),20.6,"Priest excludes displaced plate and set bonuses");shot("Priest doctrine retained");finish();}
  }
 }catch(Throwable e){e.printStackTrace();check(false,"client exception "+e);finish();}}
 void finish(){done=true;var mc=Minecraft.getInstance();mc.options.keyAttack.setDown(false);try{Files.write(mc.gameDirectory.toPath().resolve("client-checks.txt"),results);Files.writeString(mc.gameDirectory.toPath().resolve("client-result.txt"),(results.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+" "+results.stream().filter(s->s.startsWith("PASS")).count()+" checks\n");}catch(Exception e){throw new RuntimeException(e);}mc.stop();}
}
