import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
public class PatchWolf implements Opcodes {
 public static void main(String[] args)throws Exception{
  ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  if(args[2].equals("pet")){
   for(var m:c.methods)if(m.name.equals("reducedDamage")){
    // Keep the current owner/effects lookup, remove flat subtraction and lower its existing cap.
    for(var i:m.instructions.toArray()){
     if(i instanceof LdcInsnNode l&&l.cst.equals("res")){
      // Leave amount on the stack; remove the retired flat-resistance subtraction.
      var start=i.getPrevious();var end=i;
      while(end!=null&&end.getOpcode()!=DSUB)end=end.getNext();
      if(start.getOpcode()!=ALOAD||end==null)throw new AssertionError("flat resistance shape");
      var after=end.getNext();for(var n=start;n!=after;){var next=n.getNext();m.instructions.remove(n);n=next;}edits++;
     }
     if(i.getOpcode()==DCONST_1&&i.getNext() instanceof VarInsnNode v&&v.getOpcode()==ALOAD){
      // Only the cap's constant is immediately before the effects map lookup; outer 1 remains intact.
      m.instructions.set(i,new LdcInsnNode(.25));edits++;
     }
    }
   }
   if(edits!=2)throw new AssertionError("pet edits="+edits);
  }else{
   for(var m:c.methods)if(m.name.equals("updateProgress"))for(var i:m.instructions.toArray()){
    if(i instanceof TypeInsnNode t&&t.getOpcode()==INSTANCEOF&&t.desc.equals("net/minecraft/server/level/ServerPlayer")){
     m.instructions.set(t,new MethodInsnNode(INVOKESTATIC,"archives/tater/penchant/util/WolfArmorProgress","eligible","(Lnet/minecraft/world/entity/LivingEntity;)Z",false));edits++;
    }
    if(i instanceof MethodInsnNode call&&call.owner.equals("archives/tater/penchant/component/EnchantmentProgress")&&call.name.equals("onDurabilityDamage")){
     call.owner="archives/tater/penchant/util/WolfArmorProgress";call.name="damage";edits++;
    }
   }
   if(edits!=4)throw new AssertionError("progress edits="+edits);
  }
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
