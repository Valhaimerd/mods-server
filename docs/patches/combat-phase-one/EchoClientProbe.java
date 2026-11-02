package local.phaseclient;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.puffish.skillsmod.api.SkillsAPI;
import net.spell_engine.api.spell.*;
import net.spell_engine.api.spell.container.*;
import net.spell_engine.internals.*;
import net.spell_engine.internals.casting.*;
import net.spell_engine.internals.container.*;
import net.spell_engine.internals.cost.*;
@Mod(value="echo_client_probe",dist=Dist.CLIENT)
public class EchoClientProbe {
 int ticks,phase,at,action,actionAt;final List<String> lines=Collections.synchronizedList(new ArrayList<>());final AtomicReference<Throwable> error=new AtomicReference<>();boolean pending;
 public EchoClientProbe(){NeoForge.EVENT_BUS.addListener(this::tick);}
 void check(boolean yes,String label){lines.add((yes?"PASS ":"FAIL ")+label);if(!yes)throw new AssertionError(label);}
 void server(java.util.function.Consumer<net.minecraft.server.level.ServerPlayer> task){var mc=Minecraft.getInstance();pending=true;mc.getSingleplayerServer().execute(()->{try{task.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID()));}catch(Throwable e){error.set(e);}finally{pending=false;}});}
 void step(){phase++;at=ticks;}
 void shot(String label){Screenshot.grab(Minecraft.getInstance(),false);lines.add("SCREENSHOT "+label);}
 void finish(){if(phase==99)return;phase=99;var mc=Minecraft.getInstance();try{Files.write(mc.gameDirectory.toPath().resolve("echo-client-checks.txt"),lines);Files.writeString(mc.gameDirectory.toPath().resolve("echo-client-result.txt"),(lines.stream().anyMatch(s->s.startsWith("FAIL"))?"FAIL":"PASS")+"\n");}catch(Exception ignored){}mc.stop();}
 void tick(ClientTickEvent.Post event){var mc=Minecraft.getInstance();ticks++;if(phase==99)return;try{
  if(error.get()!=null)throw new RuntimeException(error.get());if(pending)return;if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;if(ticks-at>2400)throw new AssertionError("Client phase timeout "+phase);
  if(action>0&&ticks>=actionAt){int todo=action;action=0;if(todo==1)cast();else if(todo==2)server(p->p.hurtServer(p.level(),p.damageSources().genericKill(),Float.MAX_VALUE));else if(todo==3)finish();return;}var m=((SpellCaster.Player)mc.player).getCooldownManager();
  if(phase==0){mc.options.pauseOnLostFocus=false;net.spell_engine.client.SpellEngineClient.config.holdToCastCharged=false;server(p->{p.setGameMode(GameType.SURVIVAL);p.level().getGameRules().set(GameRules.KEEP_INVENTORY,true,p.level().getServer());p.getInventory().clearContent();var item=new ItemStack(Items.IRON_SWORD);item.set(SpellDataComponents.SPELL_CONTAINER,SpellContainers.forWeapon(SpellContainer.ContentType.ANY,List.of(EchoCharges.SPELL,Identifier.parse("skill_tree_rpgs:arcane_tier_2_spell_2_modifier_1"))));p.getInventory().setItem(0,item);p.getInventory().setSelectedSlot(0);p.getInventory().setItem(1,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse("runes:arcane_stone")),64));p.teleportTo(0,101,0);p.setYRot(0);p.setXRot(0);p.setDeltaMovement(0,0,0);p.removeAllEffects();p.setNoGravity(true);p.inventoryMenu.broadcastFullState();((SpellContainerSource.Owner)p).spellContainerCache().clear();SpellContainerSource.update(p);((SpellCaster.Player)p).getCooldownManager().reset(null);var target=EntityTypes.CREEPER.create(p.level(),EntitySpawnReason.COMMAND);target.setPos(0,101,6);target.setNoAi(true);target.setNoGravity(true);target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);p.level().addFreshEntity(target);});step();return;}
  if(ticks-at<30)return;
  if(phase==1){check(EchoCharges.selected(mc.player),"Selected passive synchronized to client");check(EchoCharges.available(m)==2,"Client bank2/2");mc.setScreenAndShow(null);mc.player.setYRot(0);mc.player.setXRot(0);shot("Echoing2of2");action=1;actionAt=ticks+10;step();return;}
  if(phase==2){if(ticks-at<65)return;check(EchoCharges.available(m)==1,"Native first cast consumes one synchronized charge");check(!m.isCoolingDown(PaladinRevamp.spell(mc.player,"wizards:arcane_explosion")),"Remaining charge usable during recharge");shot("Echoing1of2-recharging");action=1;actionAt=ticks+10;step();return;}
  if(phase==3){if(ticks-at<65)return;check(EchoCharges.available(m)==0,"Native second cast consumes final charge");var a=((SpellCaster.Client)mc.player).getCastController().startSpellCast(mc.player.getMainHandItem(),PaladinRevamp.spell(mc.player,"wizards:arcane_explosion"));check(!a.result().toString().equals("SUCCESS"),"Native third cast rejected while empty");shot("Echoing0of2");action=2;actionAt=ticks+10;step();return;}
  if(phase==4){if(mc.player.isAlive())return;mc.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));step();return;}
  if(phase==5){if(!mc.player.isAlive())return;check(EchoCharges.available(m)<2,"Actual death and respawn does not refill bank");mc.setScreenAndShow(null);step();return;}
  if(phase==6){if(EchoCharges.available(m)<2)return;check(EchoCharges.available(m)==2,"Serial recharge returns full bank after respawn");shot("Echoing-restored2of2");action=3;actionAt=ticks+10;step();}
 }catch(Throwable ex){ex.printStackTrace();lines.add("FAIL "+ex);finish();}}
 void cast(){var mc=Minecraft.getInstance();var attempt=((SpellCaster.Client)mc.player).getCastController().startSpellCast(mc.player.getMainHandItem(),PaladinRevamp.spell(mc.player,"wizards:arcane_explosion"));check(attempt.result().toString().equals("SUCCESS"),"Native client/server cast starts");}
}
