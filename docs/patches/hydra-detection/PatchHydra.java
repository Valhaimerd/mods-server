import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchHydra implements Opcodes {
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);
  var original=c.methods.stream().filter(m->m.name.equals("channel")).findFirst().orElseThrow();original.name="channelInRange";
  var wrapper=new MethodNode(ACC_PUBLIC|ACC_STATIC,"channel",original.desc,null,null);var x=wrapper.instructions;var nativeCall=new LabelNode();
  x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,c.name,"ancient","(Lnet/minecraft/world/entity/LivingEntity;)Z",false));x.add(new JumpInsnNode(IFEQ,nativeCall));
  x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/MageSummons","canFire","(Lnet/spell_engine/entity/SummonedEntity;)Z",false));x.add(new JumpInsnNode(IFNE,nativeCall));
  x.add(new VarInsnNode(ALOAD,0));x.add(new InsnNode(ACONST_NULL));x.add(new MethodInsnNode(INVOKEVIRTUAL,"net/spell_engine/entity/SummonedEntity","declareCastProcess","(Lnet/spell_engine/internals/casting/SpellCast$Process;)V",false));x.add(new InsnNode(ICONST_1));x.add(new InsnNode(IRETURN));
  x.add(nativeCall);x.add(new FrameNode(F_SAME,0,null,0,null));x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,1));x.add(new MethodInsnNode(INVOKESTATIC,c.name,"channelInRange",original.desc,false));x.add(new InsnNode(IRETURN));c.methods.add(wrapper);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
