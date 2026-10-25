import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchPreview implements Opcodes {
 public static void main(String[] args)throws Exception{
  ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  for(var m:c.methods)if(m.name.startsWith("lambda$loadEntity$"))for(var i:m.instructions.toArray())if(i.getOpcode()==ARETURN){
   m.instructions.insertBefore(i,new MethodInsnNode(INVOKESTATIC,"vazkii/patchouli/common/util/PreviewEntityIds","prepare","(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;",false));edits++;
  }
  if(edits!=1)throw new AssertionError("EntityUtil preview creation changed: "+edits);
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
