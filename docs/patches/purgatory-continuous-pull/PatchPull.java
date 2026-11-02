import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchPull implements Opcodes {
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int heal=0,pull=0,caps=0,advance=0,end=0;
  for(var m:c.methods){
   if(m.name.equals("tryArdent"))for(var i:m.instructions)if(i instanceof LdcInsnNode x&&Double.valueOf(.2).equals(x.cst)){x.cst=.3;heal++;}
   if(m.name.equals("pulse"))for(var i:m.instructions)if(i instanceof LdcInsnNode x){if(Double.valueOf(.08).equals(x.cst)||Double.valueOf(.18).equals(x.cst)||Double.valueOf(.32).equals(x.cst)){x.cst=0d;pull++;}else if(Double.valueOf(.65).equals(x.cst)){x.cst=Double.MAX_VALUE;caps++;}}
   if(m.name.equals("advance"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/PurgatoryPull","tick","(Lnet/minecraft/world/entity/LivingEntity;)V",false));m.instructions.insertBefore(i,x);advance++;}
   if(m.name.equals("end")){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/PurgatoryPull","clear","(Lnet/minecraft/world/entity/LivingEntity;)V",false));m.instructions.insert(x);end++;}
  }
  if(heal!=1||pull!=3||caps!=2||advance<1||end!=1)throw new AssertionError("Patch sites "+heal+"/"+pull+"/"+caps+"/"+advance+"/"+end);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());System.out.println("Guarded Ardent heal and continuous pull sites patched");
 }
}
