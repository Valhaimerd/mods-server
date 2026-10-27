package local.paladinprobe;

import java.nio.file.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.puffish.skillsmod.api.*;
import net.skill_tree_rpgs.skills.NodeTypes;
import net.spell_power.api.SpellSchools;

@Mod("paladin_doctrine_probe")
public class DoctrineProbe {
    final List<String> checks=new ArrayList<>(); int failures;
    void check(boolean ok,String name){checks.add((ok?"PASS ":"FAIL ")+name);if(!ok)failures++;}
    void near(double a,double b,String name){check(Math.abs(a-b)<.00001,name+" actual="+a);}
    Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));}
    void equipment(ServerPlayer player)throws Exception {
        var m=net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");m.setAccessible(true);m.invoke(player);
    }
    public DoctrineProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
    void run(ServerStartedEvent e){var server=e.getServer();try{
        var p=FakePlayerFactory.getMinecraft(server.overworld());p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var category=SkillsAPI.getCategory(Identifier.parse("skill_tree_rpgs:class_skills")).orElseThrow();
        category.unlock(p);category.resetSkills(p);category.setExtraPoints(p,1000);
        var healing=p.getAttribute(SpellSchools.HEALING.attributeEntry);healing.setBaseValue(10);
        double before=healing.getValue();category.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
        near(healing.getValue()-before,.25,"actual Paladin root reward");
        for(String id:List.of("x9it8aavrrwsojhg","uornste0y3mpodex","j3m3zpdi0ryjmgxt")) {
            before=healing.getValue();category.getSkill(id).orElseThrow().unlock(p);
            near(healing.getValue()-before,.25,"actual ordinary Paladin reward "+id);
        }
        near(NodeTypes.PALADIN_ROOT.attributeReward().modifier().amount(),.25,"runtime root default");
        near(NodeTypes.PALADIN_BOOST.attributeReward().modifier().amount(),.25,"runtime ordinary default");
        Class<?> doctrine;
        try{doctrine=Class.forName("net.skill_tree_rpgs.compat.PaladinDoctrine");}
        catch(ClassNotFoundException missing){check(false,"doctrine exists");return;}
        var enforce=doctrine.getMethod("enforce",ServerPlayer.class);
        var member=doctrine.getMethod("isPaladin",ServerPlayer.class);
        var prohibited=doctrine.getMethod("prohibited",ItemStack.class);
        var reserve=doctrine.getMethod("reserved",ServerPlayer.class);
        check((Boolean)member.invoke(null,p),"actual class root identifies Paladin");
        var weapons=List.of("paladins:acolyte_wand","paladins:holy_wand","paladins:diamond_holy_wand","paladins:netherite_holy_wand","paladins:holy_staff","paladins:diamond_holy_staff","paladins:netherite_holy_staff","paladins:ruby_holy_staff","paladins:aether_holy_staff");
        for(String id:weapons)for(var hand:InteractionHand.values()) {
            if(item(id)==Items.AIR){check(id.contains("ruby")||id.contains("aether"),"only optional material weapons may be absent "+id);continue;}
            p.getInventory().clearContent();var weapon=new ItemStack(item(id));
            check(!weapon.isEmpty()&&(Boolean)prohibited.invoke(null,weapon),"native healing weapon tag "+id);
            p.setItemInHand(hand,weapon);enforce.invoke(null,p);
            check(p.getItemInHand(hand).isEmpty(),"forbidden hand cleared "+hand+" "+id);
            int count=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(weapon.getItem()))count+=p.getInventory().getItem(i).getCount();
            check(count==1,"exact item returned to inventory "+hand+" "+id);
        }
        // No empty slots: use a safe swap, with both forbidden hands populated.
        p.getInventory().clearContent();for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        var staff=new ItemStack(item("paladins:holy_staff"));var wand=new ItemStack(item("paladins:holy_wand"));
        p.setItemInHand(InteractionHand.MAIN_HAND,staff);p.setItemInHand(InteractionHand.OFF_HAND,wand);enforce.invoke(null,p);
        check(p.getMainHandItem().is(Items.STONE)&&p.getOffhandItem().is(Items.STONE),"full inventory safely swaps both hands");
        // Worst case: every inventory slot is another forbidden weapon.
        p.getInventory().clearContent();for(int i=0;i<36;i++)p.getInventory().setItem(i,staff.copy());p.setItemInHand(InteractionHand.OFF_HAND,wand.copy());
        enforce.invoke(null,p);check(p.getMainHandItem().isEmpty()&&p.getOffhandItem().isEmpty(),"all-forbidden full inventory still has empty hands");
        check(((List<?>)reserve.invoke(null,p)).size()==2,"two displaced items persist safely");
        var saved=p.getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG).copy();
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG,saved.copy());
        enforce.invoke(null,p);check(((List<?>)reserve.invoke(null,p)).size()==2,"serialized reserve is idempotent");
        p.getInventory().setItem(10,ItemStack.EMPTY);enforce.invoke(null,p);
        check(!p.getInventory().getItem(10).isEmpty()&&((List<?>)reserve.invoke(null,p)).size()==1,"free slot recovers one reserved item");
        category.resetSkills(p);enforce.invoke(null,p);
        check(!(Boolean)member.invoke(null,p),"respec removes restriction");
        check(((List<?>)reserve.invoke(null,p)).isEmpty(),"respec recovers remaining item into free hand slot");
        p.getInventory().clearContent();category.setExtraPoints(p,1000);category.getSkill("ov6ag9tjdhsmldil").orElseThrow().unlock(p);
        for(var hand:InteractionHand.values()){p.setItemInHand(hand,staff.copy());enforce.invoke(null,p);check(p.getItemInHand(hand).is(staff.getItem()),"Priest retains healing staff "+hand);}
        equipment(p);double priest=healing.getValue();check(priest>10,"Priest retains actual staff attribute");
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
        enforce.invoke(null,p);near(healing.getValue(),10.25,"Paladin staff attributes removed immediately");
        // Equipment scan itself must reject direct command/mod equipment insertion.
        p.setItemInHand(InteractionHand.MAIN_HAND,staff.copy());equipment(p);
        check(p.getMainHandItem().isEmpty(),"native equipment scan rejects direct insertion");near(healing.getValue(),10.25,"scan never applies forbidden staff bonus");
        p.setItemInHand(InteractionHand.MAIN_HAND,staff.copy());
        var attempt=net.spell_engine.internals.casting.SpellCasting.attempt(p,p.getMainHandItem(),Identifier.parse("paladins:holy_shock"),false);
        check(!attempt.isSuccess(),"direct healing-staff cast attempt rejected");check(p.getMainHandItem().isEmpty(),"cast guard clears forbidden hand");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));enforce.invoke(null,p);
        check(p.getMainHandItem().is(Items.IRON_SWORD),"ordinary melee weapon retained");
        p.setItemInHand(InteractionHand.OFF_HAND,staff.copy());
        var deny=doctrine.getMethod("rejectAction",ServerPlayer.class,ItemStack.class);
        check((Boolean)deny.invoke(null,p,p.getMainHandItem()),"offhand violation rejects initiating action");
        check(p.getOffhandItem().isEmpty(),"offhand action guard clears healing weapon");
        check((Boolean)deny.invoke(null,p,staff.copy()),"stale queued staff source is rejected after relocation");
        var victim=net.minecraft.world.entity.EntityTypes.PIG.create(server.overworld(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        p.setItemInHand(InteractionHand.MAIN_HAND,staff.copy());
        var attack=new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(p,victim);
        NeoForge.EVENT_BUS.post(attack);check(attack.isCanceled(),"native attack event rejects healing weapon");
        // A fast role-and-slot change must not retain a previously applied Priest staff bonus.
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("ov6ag9tjdhsmldil").orElseThrow().unlock(p);
        p.getInventory().clearContent();p.setItemInHand(InteractionHand.MAIN_HAND,staff.copy());equipment(p);
        check(healing.getValue()>10.25,"fast-switch fixture has applied Priest staff bonus");
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_SWORD));enforce.invoke(null,p);
        near(healing.getValue(),10.25,"fast class/slot switch removes stale weapon bonus");
        // Storage overflow follows normal death rules rather than becoming an item bank.
        p.getInventory().clearContent();for(int i=0;i<36;i++)p.getInventory().setItem(i,staff.copy());p.setItemInHand(InteractionHand.OFF_HAND,wand.copy());
        enforce.invoke(null,p);check(((List<?>)reserve.invoke(null,p)).size()==2,"death fixture has two displaced items");
        var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        var death=new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(p,p.damageSources().generic(),drops,true);
        doctrine.getMethod("deathDrops",net.neoforged.neoforge.event.entity.living.LivingDropsEvent.class).invoke(null,death);
        check(drops.size()==2,"normal death drops both displaced items");
        check(((List<?>)reserve.invoke(null,p)).isEmpty(),"normal death clears reserve before clone");
        p.getInventory().clearContent();for(int i=0;i<36;i++)p.getInventory().setItem(i,staff.copy());p.setItemInHand(InteractionHand.OFF_HAND,wand.copy());
        enforce.invoke(null,p);
        var output=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,server.registryAccess());
        p.saveWithoutId(output);
        var loaded=FakePlayerFactory.get(server.overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ReserveLoad"));
        loaded.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,server.registryAccess(),output.buildResult()));
        check(((List<?>)reserve.invoke(null,loaded)).size()==2,"native player save/load retains displaced equipment");
        p.level().getGameRules().set(GameRules.KEEP_INVENTORY,true,server);
        drops.clear();doctrine.getMethod("deathDrops",net.neoforged.neoforge.event.entity.living.LivingDropsEvent.class).invoke(null,new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(p,p.damageSources().generic(),drops,true));
        check(drops.isEmpty()&&((List<?>)reserve.invoke(null,p)).size()==2,"keepInventory retains displaced items without drops");
        var respawned=FakePlayerFactory.get(server.overworld(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ReserveRespawn"));
        respawned.restoreFrom(p,false);
        check(((List<?>)reserve.invoke(null,respawned)).size()==2,"native respawn clone retains displaced equipment");
        respawned.getInventory().clearContent();enforce.invoke(null,respawned);
        check(((List<?>)reserve.invoke(null,respawned)).isEmpty(),"respawned player recovers reserved equipment");
        int recovered=0;for(int i=0;i<36;i++){var stack=respawned.getInventory().getItem(i);if(stack.is(staff.getItem())||stack.is(wand.getItem()))recovered+=stack.getCount();}
        check(recovered==2,"respawn restores exactly two items without duplication");
        check(((List<?>)reserve.invoke(null,p)).size()==2,"clone writes do not mutate original shared persisted tag");
        p.level().getGameRules().set(GameRules.KEEP_INVENTORY,false,server);
    }catch(Throwable error){error.printStackTrace();check(false,"uncaught exception "+error);}
    finally{try{Files.write(Path.of("doctrine-checks.txt"),checks);Files.writeString(Path.of("doctrine-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures+"\n");}catch(Exception error){throw new RuntimeException(error);}server.halt(false);}}
}
