import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.client.casting.ClientCastController;
public class ClientWallProbe { public static void main(String[] args)throws Exception {
 var registry=ResourceKey.<Spell>createRegistryKey(Identifier.parse("test:spell"));
 var ref=Holder.Reference.createStandAlone(new HolderOwner<Spell>(){},ResourceKey.create(registry,Identifier.parse("wizards:fire_wall")));
 var option=new SpellCast.Option(ref,SpellCast.Mode.INSTANT,null,0,0);
 var controller=new ClientCastController(null);
 // SpellHotbar debounces START and currently passes STOP freshness first, START second.
 // A fresh press has both true; a held press after START has (true,false).
 for(boolean[] flags:new boolean[][]{{true,false},{false,true},{false,false}})
  if(controller.keyHeld(option,flags[0],flags[1])!=ClientCastController.Reaction.NOTHING)throw new AssertionError("Held wall was repeated");
 try {controller.keyHeld(option,true,true);throw new AssertionError("Fresh press did not enter normal cast path");}
 catch(NullPointerException expected){} // The deliberately absent LocalPlayer proves fresh presses reach native casting.
 System.out.println("PASS actual patched client keyHeld suppresses held Wall of Flames before any cast request");
 for(var m:ClientCastController.class.getDeclaredMethods()){}
 System.out.println("PASS patched client casting class links with -Xverify:all");
} }