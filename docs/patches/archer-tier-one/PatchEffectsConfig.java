import java.nio.file.*;
import org.objectweb.asm.*;
/** Add one configuration guard without replacing earlier effect registrations. */
public final class PatchEffectsConfig implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
   var out=super.visitMethod(a,n,d,s,e);
   if(!n.equals("register")||!d.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V"))return out;
   return new MethodVisitor(ASM9,out){public void visitCode(){super.visitCode();mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,"net/spell_engine/internals/ArcherRules",args[2],d,false);edits[0]++;}};
  }},0);
  if(edits[0]!=1)throw new AssertionError("Expected exactly one effect-register guard: "+edits[0]);
  Files.write(Path.of(args[1]),w.toByteArray());
 }
}
