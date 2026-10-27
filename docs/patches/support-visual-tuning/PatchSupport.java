import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchSupport implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int count=0;
  for(var m:node.methods)if(m.name.equals("<clinit>"))for(var i:m.instructions.toArray())if(i instanceof LdcInsnNode ldc&&Float.valueOf(.3f).equals(ldc.cst)){ldc.cst=.5f;count++;}
  if(count!=1)throw new AssertionError("Expected one 30% Last Stand tint, found "+count);
  var out=new ClassWriter(0);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
