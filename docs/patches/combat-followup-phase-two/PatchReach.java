import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchReach implements Opcodes{
 public static void main(String[] args)throws Exception{var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int count=0;
  for(var m:n.methods)if(m.name.startsWith("lambda$handleAttackRequest"))for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode a&&a.owner.equals("net/bettercombat/logic/PlayerAttackHelper")&&a.name.equals("getRangeForItem")){
   var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,"net/bettercombat/logic/ScaleReach","apply","(DLnet/minecraft/world/entity/player/Player;)D",false));m.instructions.insert(i,x);count++;
  }
  if(count!=1)throw new AssertionError("range validation hooks="+count);var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
