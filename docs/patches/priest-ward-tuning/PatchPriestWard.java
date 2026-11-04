import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchPriestWard {
 public static void main(String[] args)throws Exception {
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);
  int edits=0;String mode=args[2];
  for(var m:c.methods){
   int first=0;
   for(var n:m.instructions.toArray()){
    if(mode.equals("cooldown")&&m.name.equals("cooldown")&&n instanceof LdcInsnNode v&&v.cst.equals(.5f)){
     v.cst=first++==0?1f:0f;edits++;
    }
    if(mode.equals("penance")&&m.name.equals("adjust")&&n instanceof LdcInsnNode v){
     if(v.cst.equals(.2f)&&first==0){v.cst=.5f;first++;edits++;}
     else if(v.cst.equals(.15f)&&first==1){v.cst=.4f;first++;edits++;}
    }
    if(mode.equals("circle")&&m.name.equals("impact")&&n instanceof LdcInsnNode v){
     if(v.cst.equals(.75f)){v.cst=.9f;edits++;}
     else if(v.cst.equals(.6f)){v.cst=.65f;edits++;}
    }
    if(mode.equals("ward")&&m.name.equals("after")&&n instanceof MethodInsnNode v&&v.owner.equals("net/spell_engine/internals/PartyRules")&&v.name.equals("harmfulAllowed")&&first++==0){v.owner="net/spell_engine/internals/ArcaneWardRange";v.name="eligible";edits++;}
   }
  }
  int expected=mode.equals("ward")?1:2;if(edits!=expected)throw new AssertionError(mode+" edits="+edits+", expected="+expected);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());System.out.println(mode+" edits="+edits);
 }
}
