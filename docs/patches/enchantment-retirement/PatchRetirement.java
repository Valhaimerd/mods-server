import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchRetirement implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int count=0;
  if(args[2].equals("soulbound")){
   for(var m:node.methods)if(m.name.equals("saveOnDeath")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    m.instructions.add(new FieldInsnNode(GETSTATIC,node.name,"EMPTY","Lfuzs/enderzoology/common/attachment/SoulboundItems;"));m.instructions.add(new InsnNode(ARETURN));m.maxStack=1;count++;
   }
  }else{
   for(var m:node.methods)for(var ins:m.instructions.toArray())if(ins instanceof FieldInsnNode f&&f.name.equals("PENETRATING_STRIKE_KEY")){
    var start=ins.getPrevious().getPrevious();var end=ins.getNext().getNext();if(start.getOpcode()!=ALOAD||ins.getPrevious().getOpcode()!=ALOAD||end.getOpcode()!=INVOKESTATIC)throw new AssertionError("Unexpected creative instruction sequence");
    for(var cur=start;cur!=null;){var next=cur.getNext();m.instructions.remove(cur);if(cur==end)break;cur=next;}count++;
   }
  }
  if(count!=1)throw new AssertionError("edits="+count);var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
