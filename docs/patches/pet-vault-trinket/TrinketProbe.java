package local.petvaulttrinket;
import java.nio.file.*;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import top.theillusivec4.curios.api.*;
import net.creativity0.petvault.compat.CuriosCompat;
@Mod("petvault_trinket_probe")
public class TrinketProbe {
 List<String> checks=new ArrayList<>();
 public TrinketProbe(){NeoForge.EVENT_BUS.addListener(this::run);}
 void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks.add("PASS "+text);}
 void run(ServerStartedEvent e){var server=e.getServer();try{
  var p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(server.overworld());var inv=CuriosApi.getCuriosInventory(p).orElseThrow();inv.loadDatapacks();
  var locket=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("pet_vault:pet_necklace")));
  check(locket.is(TagKey.create(Registries.ITEM,Identifier.parse("curios:necklace"))),"original necklace eligibility retained");
  check(locket.is(TagKey.create(Registries.ITEM,Identifier.parse("curios:trinket"))),"Keeper Locket included in Nameless trinket tag");
  var ordinary=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("nameless_trinkets:lucky_rock")));
  check(ordinary.is(TagKey.create(Registries.ITEM,Identifier.parse("curios:trinket"))),"additive tag preserves Nameless items");
  for(String slot:List.of("trinket","necklace")){
   var wearer=net.neoforged.neoforge.common.util.FakePlayerFactory.get(server.overworld(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"SlotProbe"));
   var equipment=CuriosApi.getCuriosInventory(wearer).orElseThrow();equipment.loadDatapacks();var handler=equipment.getCurios().get(slot);
   check(handler!=null&&handler.getStacks().getSlots()>0,"installed "+slot+" slot exists");
   var ctx=new SlotContext(slot,wearer,0,false,true);check(CuriosApi.isStackValid(ctx,locket),"native Curios accepts locket in "+slot);
   check(equipment.findFirstCurio(stack->stack.is(locket.getItem())).isEmpty(),"empty slots do not activate Pet Vault");
   handler.getStacks().setStackInSlot(0,locket.copy());
   check(CuriosCompat.hasLocketEquipped(wearer),"Pet Vault native equipped check works in "+slot);
   handler.getStacks().setStackInSlot(0,ItemStack.EMPTY);
   check(equipment.findFirstCurio(stack->stack.is(locket.getItem())).isEmpty(),"removed locket absent from live inventory in "+slot);
  }
  check(!CuriosApi.isStackValid(new SlotContext("trinket",p,0,false,true),new ItemStack(Items.DIRT)),"arbitrary items remain invalid in trinket slots");
  Files.writeString(Path.of("trinket-result.txt"),"PASS "+checks.size()+" native Curios/Pet Vault checks");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("trinket-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{try{Files.write(Path.of("trinket-checks.txt"),checks);}catch(Exception ignored){}server.halt(false);}}
}
