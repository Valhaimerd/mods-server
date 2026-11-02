import java.nio.file.*;
import org.objectweb.asm.*;
public class CountQueries {
 public static void main(String[] args)throws Exception{
  var reader=new ClassReader(Files.readAllBytes(Path.of(args[0])));var writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);int[] count={0};
  reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
   public MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[] exceptions){
    return new MethodVisitor(Opcodes.ASM9,super.visitMethod(access,name,descriptor,signature,exceptions)){
     public void visitMethodInsn(int opcode,String owner,String method,String desc,boolean itf){
      if(name.equals("curioTick")&&method.equals("getEntitiesOfClass")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"local/accessoryperf/HotspotProbe","countScan","()V",false);count[0]++;}
      super.visitMethodInsn(opcode,owner,method,desc,itf);
     }
    };
   }
  },0);if(count[0]!=1)throw new AssertionError("query hooks="+count[0]);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
