import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchArmor implements Opcodes {
 public static void main(String[] args)throws Exception{
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int edits=0;
  for(var m:n.methods)if(m.name.equals("register"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){m.instructions.insertBefore(i,new MethodInsnNode(INVOKESTATIC,"net/spell_engine/entity/LastStandAppearance","register","()V",false));edits++;}
  if(edits!=1)throw new AssertionError("Registration edits "+edits);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
