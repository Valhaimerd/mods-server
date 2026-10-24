import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchPulse implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int changed=0;
  for(var method:node.methods)if(method.name.equals("pulse")){
   MethodInsnNode heal=null,amplifier=null;
   for(var instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode call){
    if(call.name.equals("setHealth")){if(heal!=null)throw new AssertionError("Multiple pulse heals");heal=call;}
    if(call.name.equals("getAmplifier"))amplifier=call;
   }
   if(heal==null||amplifier==null)throw new AssertionError("Expected fifth-pulse heal not found");
   var start=heal.getPrevious().getPrevious().getPrevious();
   if(start.getOpcode()!=ALOAD||start.getNext().getOpcode()!=ALOAD||!(heal.getPrevious() instanceof MethodInsnNode maximum)||!maximum.name.equals("getMaxHealth"))throw new AssertionError("Unexpected heal sequence");
   var sequence=new InsnList();for(var instruction=start;;){var next=instruction.getNext();method.instructions.remove(instruction);sequence.add(instruction);if(instruction==heal)break;instruction=next;}
   method.instructions.insertBefore(amplifier.getPrevious(),sequence);changed++;
  }
  if(changed!=1)throw new AssertionError("Pulse edit count="+changed);var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
