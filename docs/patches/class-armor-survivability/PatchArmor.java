import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchArmor implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int registrations=0,creations=0;
  for(var method:node.methods){
   if(method.name.equals("register")&&method.desc.equals("(Ljava/util/Map;)V")){
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new FieldInsnNode(GETSTATIC,node.name,"entries","Ljava/util/ArrayList;"));add.add(new MethodInsnNode(INVOKESTATIC,args[2],"apply","(Ljava/util/Map;Ljava/util/List;)V",false));method.instructions.insert(add);registrations++;
   }
   for(var insn:method.instructions)if(insn instanceof MethodInsnNode call&&call.owner.equals("net/spell_engine/rpg_series/item/Armor$Entry")&&call.name.equals("create")){call.owner=args[2];creations++;}
  }
  if(registrations!=1||creations<1)throw new AssertionError("hooks="+registrations+" creations="+creations);
  var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());System.out.println(node.name+" register="+registrations+" create="+creations);
 }
}
