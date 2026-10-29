import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class PatchProtection implements Opcodes {
 public static void main(String[] args)throws Exception {
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int pre=0,cast=0,details=0,cap=0;
  for(var m:n.methods) {
   if(m.name.startsWith("lambda$")&&m.desc.equals("(Lnet/neoforged/neoforge/event/entity/living/LivingDamageEvent$Pre;)V")) {
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    m.instructions.add(new InsnNode(RETURN));pre++;
   }
   if(m.name.equals("protect"))for(var i:m.instructions.toArray()) {
    if(i instanceof LdcInsnNode ldc&&Double.valueOf(.5).equals(ldc.cst)){ldc.cst=.4;cap++;}
    if(i.getOpcode()==IRETURN){var a=new InsnList();a.add(new VarInsnNode(ALOAD,2));a.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/ProtectionHits","cast","(ZLnet/minecraft/world/entity/LivingEntity;)Z",false));m.instructions.insertBefore(i,a);cast++;}
   }
   if(m.name.equals("details"))for(var i:m.instructions.toArray())if(i.getOpcode()==ARETURN){var a=new InsnList();a.add(new VarInsnNode(ALOAD,0));a.add(new VarInsnNode(ALOAD,1));a.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/ProtectionHits","details","(Ljava/util/List;Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;)Ljava/util/List;",false));m.instructions.insertBefore(i,a);details++;}
  }
  if(pre!=1||cast!=2||details!=1||cap!=1)throw new AssertionError(pre+":"+cast+":"+details+":"+cap);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
