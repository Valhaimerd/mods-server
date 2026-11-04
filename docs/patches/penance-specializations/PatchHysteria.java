import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchHysteria {
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  for(var m:c.methods)if(m.name.equals("configure")){
   boolean hysteria=false;
   for(var n:m.instructions.toArray())if(n instanceof LdcInsnNode v){
    if(v.cst.equals("skill_tree_rpgs:hysteria"))hysteria=true;
    if(v.cst.equals("skill_tree_rpgs:chastise"))hysteria=true;
    if(hysteria&&v.cst.equals(.01f)){v.cst=.015f;edits++;}
   }
  }
  if(edits!=4)throw new AssertionError("Expected four Penance specialization modifiers, got "+edits);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());System.out.println("Hysteria modifiers updated: "+edits);
 }
}
