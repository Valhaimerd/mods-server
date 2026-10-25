import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchAxis implements Opcodes {
 static final String HELPER="sophisticated/building/pack/AxisPolicy";
 public static void main(String[] args)throws Exception{
  ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  for(var m:c.methods){
   if(m.name.equals("getMaxBlocksPerAxis")&&(c.name.endsWith("/AttachmentHandler")||c.name.endsWith("/PowerLevel"))){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new LdcInsnNode(Integer.MAX_VALUE));m.instructions.add(new InsnNode(IRETURN));edits++;continue;
   }
   if(c.name.endsWith("/BuildLimits")&&m.name.equals("maxBlockDistance")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    m.instructions.add(new VarInsnNode(ILOAD,0));m.instructions.add(new VarInsnNode(DLOAD,1));m.instructions.add(new VarInsnNode(ILOAD,3));m.instructions.add(new VarInsnNode(ILOAD,4));m.instructions.add(new MethodInsnNode(INVOKESTATIC,HELPER,"maximumDistance","(IDII)I",false));m.instructions.add(new InsnNode(IRETURN));edits++;continue;
   }
   if(c.name.endsWith("/RadialMenu"))for(var ins:m.instructions.toArray()){
    if(ins instanceof MethodInsnNode call&&call.owner.equals("sophisticated/building/attachment/AttachmentHandler")&&call.name.equals("getMaxBlocksPerAxis")){
     call.owner=HELPER;call.name="label";call.desc="(Lnet/minecraft/world/entity/player/Player;Z)Ljava/lang/String;";
     AbstractInsnNode next=call.getNext();while(next!=null&&next.getOpcode()<0)next=next.getNext();
     if(!(next instanceof InvokeDynamicInsnNode concat)||!concat.desc.equals("(I)Ljava/lang/String;"))throw new AssertionError("axis tooltip concat changed");
     concat.desc="(Ljava/lang/String;)Ljava/lang/String;";edits++;
    }
   }
   if(c.name.endsWith("/ArrayEntry"))for(var ins:m.instructions.toArray())if(ins instanceof InvokeDynamicInsnNode concat&&concat.desc.endsWith("I)Ljava/lang/String;")&&java.util.Arrays.stream(concat.bsmArgs).anyMatch(a->a instanceof String s&&s.contains("/"))){
    m.instructions.insertBefore(ins,new MethodInsnNode(INVOKESTATIC,HELPER,"label","(I)Ljava/lang/String;",false));concat.desc=concat.desc.substring(0,concat.desc.length()-"I)Ljava/lang/String;".length())+"Ljava/lang/String;)Ljava/lang/String;";edits++;
   }
  }
  int expected=c.name.endsWith("/RadialMenu")?2:1;if(edits!=expected)throw new AssertionError(c.name+" edits="+edits+" expected="+expected);
  ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
