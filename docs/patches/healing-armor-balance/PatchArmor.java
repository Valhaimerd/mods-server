import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchArmor implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int edits=0;
  for(var method:node.methods)if(method.name.equals("register")&&method.desc.equals("(Ljava/util/Map;)V")){
   var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new FieldInsnNode(GETSTATIC,node.name,"entries","Ljava/util/ArrayList;"));add.add(new MethodInsnNode(INVOKESTATIC,args[2],"apply","(Ljava/util/Map;Ljava/util/List;)V",false));method.instructions.insert(add);edits++;
  }
  if(edits!=1)throw new AssertionError("register hooks="+edits);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
