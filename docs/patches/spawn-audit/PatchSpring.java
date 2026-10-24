import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchSpring implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,0);int[] count={0};
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){var out=super.visitMethod(a,n,d,s,e);return new MethodVisitor(ASM9,out){public void visitFieldInsn(int op,String owner,String name,String desc){
   if(n.equals("<clinit>")&&op==GETSTATIC&&owner.equals("xyz/verarr/spreadspawnpoints/spawnpoints/generators/VanillaSpawnPointGenerator")&&name.equals("TYPE")){owner=owner.replace("Vanilla","Spring");count[0]++;}
   super.visitFieldInsn(op,owner,name,desc);
  }};}},0);
  if(count[0]!=1)throw new AssertionError("default replacements "+count[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
