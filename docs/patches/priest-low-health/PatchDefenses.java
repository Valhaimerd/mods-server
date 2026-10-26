import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchDefenses implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int count=0;
  for(var m:node.methods)if(m.name.equals("configure"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){
   var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/PriestDefenses","configure",m.desc,false));m.instructions.insertBefore(i,add);count++;
  }
  if(count!=2)throw new AssertionError("configure returns "+count);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
