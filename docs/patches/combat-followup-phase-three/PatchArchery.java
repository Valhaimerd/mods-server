import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchArchery implements Opcodes {
 public static void main(String[]args)throws Exception{var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int count=0;
  for(var m:n.methods){
   if(args[2].equals("reload")&&m.name.equals("crossarms$completeCrank")){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ALOAD,2));m.instructions.add(new VarInsnNode(ALOAD,3));m.instructions.add(new MethodInsnNode(INVOKESTATIC,"local/crossarms/runtime/FrenzyReliability","load","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Z",false));m.instructions.add(new InsnNode(IRETURN));count++;}
   if(args[2].equals("frenzy")&&(m.name.equals("fired")||m.name.equals("cranked")))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKESTATIC,"local/crossarms/runtime/FrenzyReliability","sync","(Lnet/minecraft/world/entity/LivingEntity;)V",false));m.instructions.insertBefore(i,x);count++;}
   if(args[2].equals("assist")&&m.name.equals("steer"))for(var i:m.instructions.toArray())if(i instanceof LdcInsnNode a&&a.cst.equals(.25d)){
    var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/phys/Vec3","length","()D",false));x.add(new LdcInsnNode(3d));x.add(new InsnNode(DDIV));x.add(new LdcInsnNode(1d));x.add(new LdcInsnNode(2d));x.add(new MethodInsnNode(INVOKESTATIC,"java/lang/Math","clamp","(DDD)D",false));x.add(new InsnNode(DMUL));m.instructions.insert(i,x);count++;
   }
  }
  if(count!=(args[2].equals("frenzy")?3:1))throw new AssertionError(args[2]+" edits="+count);var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());System.out.println(args[2]+" edits="+count);
 }
}
