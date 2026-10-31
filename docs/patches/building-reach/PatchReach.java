import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchReach implements Opcodes {
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  for(var m:c.methods){
   if(!m.name.equals("getPlacementReach")&&!m.name.equals("getBuildModeReach"))continue;
   if(!m.desc.endsWith(")I"))throw new AssertionError("Unexpected reach descriptor "+m.desc);
   m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
   m.instructions.add(new LdcInsnNode(m.name.equals("getPlacementReach")?1000:1006));m.instructions.add(new InsnNode(IRETURN));edits++;
  }
  if(edits!=2)throw new AssertionError(c.name+" expected two reach methods; got "+edits);
  var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
