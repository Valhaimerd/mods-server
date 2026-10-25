import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchAppearance implements Opcodes {
 public static void main(String[] args)throws Exception {
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int edits=0;
  String method=args[2].equals("common")?"skill":"registerEffectRenderers";
  String helper=args[2].equals("common")?"net/spell_engine/entity/BerserkerAppearance":"net/skill_tree_rpgs/client/effect/BerserkerSparkles";
  for(var m:n.methods)if(m.name.equals(method))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){m.instructions.insertBefore(i,new MethodInsnNode(INVOKESTATIC,helper,"register","()V",false));edits++;}
  if(edits!=(args[2].equals("common")?2:1))throw new AssertionError("Unexpected registration sites: "+edits);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
