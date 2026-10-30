import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Add two visual events to current compiled delivery code; do not recompile old gameplay sources. */
public class PatchGold implements Opcodes {
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int purge=0,prepare=0;
  for(var m:c.methods){
   if(m.name.equals("purge"))for(var ins:m.instructions.toArray())if(ins instanceof MethodInsnNode call&&call.owner.equals(c.name)&&call.name.equals("heal")){
    // The first heal is the successful-hit-only self heal. Ally-loop heals must not repeat the area.
    if(purge++==0){var list=new InsnList();list.add(new VarInsnNode(ALOAD,2));list.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/PaladinVisuals","divineStrength","(Lnet/minecraft/world/entity/LivingEntity;)V",false));m.instructions.insert(ins,list);}
   }
   if(m.name.equals("prepare"))for(var ins:m.instructions.toArray())if(ins.getOpcode()==IRETURN&&ins.getPrevious().getOpcode()==ICONST_1){
    var list=new InsnList();list.add(new VarInsnNode(ALOAD,2));list.add(new VarInsnNode(ALOAD,4));list.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/PaladinVisuals","sealOfLight","(Lnet/minecraft/world/entity/LivingEntity;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)V",false));m.instructions.insertBefore(ins.getPrevious(),list);prepare++;
   }
  }
  if(purge!=2||prepare!=1)throw new AssertionError("Unexpected tier-I structure: purgeHealCalls="+purge+" prepare="+prepare);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
