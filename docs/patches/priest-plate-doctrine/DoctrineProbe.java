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

    static final List<String> SETS=List.of("paladins:paladin_armor","paladins:crusader_armor","paladins:netherite_crusader_armor","armory_rpgs:justicar_armor","armory_rpgs:lightbringer_armor");
    static double armorValue(String id){return id.startsWith(SETS.get(0)+"_")?1:id.startsWith(SETS.get(1)+"_")?1.25:id.startsWith(SETS.get(2)+"_")?1.5:1.75;}
    void armorDefaults()throws Exception {
        var preserved=new TreeMap<String,List<String>>();var values=new TreeMap<String,Double>();
        for(var item:BuiltInRegistries.ITEM) {
            String id=BuiltInRegistries.ITEM.getKey(item).toString();if(!id.startsWith("paladins:")&&!id.startsWith("armory_rpgs:"))continue;
            var attr=item.getDefaultInstance().get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);if(attr==null)continue;
            boolean targeted=SETS.stream().anyMatch(set->id.startsWith(set+"_"));var kept=new ArrayList<String>();
            for(var entry:attr.modifiers()) {
                String key=entry.attribute().unwrapKey().orElseThrow().identifier().toString();var m=entry.modifier();
                if(targeted&&key.equals("spell_power:healing")&&m.operation()==net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE)values.merge(id,m.amount(),Double::sum);
                else kept.add(key+"|"+m.id()+"|"+m.amount()+"|"+m.operation()+"|"+entry.slot());
            }
            Collections.sort(kept);preserved.put(id,kept);
        }
        check(values.size()==20,"all twenty plate defaults registered");
        for(var entry:values.entrySet())near(entry.getValue(),armorValue(entry.getKey()),"new per-piece default "+entry.getKey());
        for(String set:SETS)near(values.entrySet().stream().filter(x->x.getKey().startsWith(set+"_")).mapToDouble(Map.Entry::getValue).sum(),4*armorValue(set+"_"),"new full-set flat item total "+set);
        var gson=new com.google.gson.Gson();Files.writeString(Path.of("armor-preserved.json"),gson.toJson(preserved));
        if(Files.exists(Path.of("expected-preserved.json"))) {
            var expected=gson.fromJson(Files.readString(Path.of("expected-preserved.json")),new com.google.gson.reflect.TypeToken<Map<String,List<String>>>(){}.getType());
            check(preserved.equals(expected),"all other native armor modifiers unchanged");
        }
    }
    public DoctrineProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
    void run(ServerStartedEvent e){var server=e.getServer();try{
        armorDefaults();
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
        // New Priest restriction: preserve all existing Paladin checks above.
        p.getInventory().clearContent();enforce.invoke(null,p);p.getInventory().clearContent();
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("ov6ag9tjdhsmldil").orElseThrow().unlock(p);
        var isPriest=doctrine.getMethod("isPriest",ServerPlayer.class);
        var bannedArmor=doctrine.getMethod("prohibitedArmor",ItemStack.class);
        check((Boolean)isPriest.invoke(null,p),"actual Priest root identifies Priest");
        var slots=List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET);
        var parts=List.of("head","chest","legs","feet");
        double priestBase=healing.getValue();
        for(String set:SETS)for(int i=0;i<4;i++) {
            p.getInventory().clearContent();for(var slot:slots)p.setItemSlot(slot,ItemStack.EMPTY);
            var armor=new ItemStack(item(set+"_"+parts.get(i)));
            check(!armor.isEmpty()&&(Boolean)bannedArmor.invoke(null,armor),"tag covers real plate piece "+set+"_"+parts.get(i));
            p.setItemSlot(slots.get(i),armor);enforce.invoke(null,p);
            check(p.getItemBySlot(slots.get(i)).isEmpty(),"Priest armor slot rejected "+set+"_"+parts.get(i));
            int count=0;for(int j=0;j<36;j++)if(p.getInventory().getItem(j).is(armor.getItem()))count+=p.getInventory().getItem(j).getCount();
            check(count==1,"rejected plate preserved once "+set+"_"+parts.get(i));
            near(healing.getValue(),priestBase,"rejected plate grants no Healing Power "+set+"_"+parts.get(i));
        }
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item("paladins:paladin_armor_chest")));
        var useArmor=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(p,InteractionHand.MAIN_HAND);
        NeoForge.EVENT_BUS.post(useArmor);
        check(useArmor.isCanceled()&&useArmor.getCancellationResult()==net.minecraft.world.InteractionResult.FAIL,"right-click equip rejected with failure response");
        check(p.getMainHandItem().is(item("paladins:paladin_armor_chest")),"denied equip preserves server held armor");
        p.getInventory().clearContent();p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));enforce.invoke(null,p);
        check(p.getItemBySlot(EquipmentSlot.HEAD).is(Items.DIAMOND_HELMET),"Priest retains ordinary diamond armor");
        var robe=new ItemStack(item("paladins:priest_robe_head"));
        check(!robe.isEmpty(),"Priest robe fixture exists");p.setItemSlot(EquipmentSlot.HEAD,robe);enforce.invoke(null,p);
        check(p.getItemBySlot(EquipmentSlot.HEAD).is(robe.getItem()),"Priest retains native robe");
        p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item(SETS.getFirst()+"_head")));equipment(p);
        check(p.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),"native equipment scan rejects plate insertion");
        p.getInventory().clearContent();for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        for(int i=0;i<4;i++)p.setItemSlot(slots.get(i),new ItemStack(item(SETS.get(i)+"_"+parts.get(i))));
        enforce.invoke(null,p);check(slots.stream().allMatch(slot->p.getItemBySlot(slot).isEmpty()),"full inventory still rejects all four plate slots");
        check(((List<?>)reserve.invoke(null,p)).size()==4,"four displaced armor pieces stored without loss");
        int stone=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).is(Items.STONE))stone+=p.getInventory().getItem(i).getCount();
        check(stone==2304,"full inventory preserves every stone stack");
        p.getInventory().setItem(5,ItemStack.EMPTY);enforce.invoke(null,p);
        check(((List<?>)reserve.invoke(null,p)).size()==3&&!p.getInventory().getItem(5).isEmpty(),"free slot recovers one displaced armor piece");
        p.getInventory().clearContent();enforce.invoke(null,p);check(((List<?>)reserve.invoke(null,p)).isEmpty(),"all displaced plate recovered without dropping");
        p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(item("armory_rpgs:lightbringer_armor_chest")));
        check((Boolean)deny.invoke(null,p,p.getMainHandItem()),"action guard rejects worn forbidden plate");
        check(p.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"action guard clears plate before cast or attack");
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("tdimeso8ajcwiygp").orElseThrow().unlock(p);
        p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(item("paladins:paladin_armor_head")));enforce.invoke(null,p);
        check(!p.getItemBySlot(EquipmentSlot.HEAD).isEmpty(),"Paladin still equips its plate");
        category.resetSkills(p);category.setExtraPoints(p,1000);category.getSkill("ov6ag9tjdhsmldil").orElseThrow().unlock(p);
        p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);enforce.invoke(null,p);near(healing.getValue(),10.3,"fast class and armor switch clears stale plate power");

    }catch(Throwable error){error.printStackTrace();check(false,"uncaught exception "+error);}
    finally{try{Files.write(Path.of("doctrine-checks.txt"),checks);Files.writeString(Path.of("doctrine-result.txt"),(failures==0?"PASS ":"FAIL ")+checks.size()+" checks; failures="+failures+"\n");}catch(Exception error){throw new RuntimeException(error);}server.halt(false);}}
}
