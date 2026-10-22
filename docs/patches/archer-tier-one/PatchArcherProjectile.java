import java.nio.file.*;
import org.objectweb.asm.*;
/** Narrow hook in native steering, retaining targeting and party checks. */
public final class PatchArcherProjectile implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
   var out=super.visitMethod(a,n,d,s,e);if(!n.equals("followTarget"))return out;
   return new MethodVisitor(ASM9,out){public void visitFieldInsn(int op,String owner,String field,String desc){
    if(op==GETFIELD&&owner.equals("net/spell_engine/api/spell/Spell$ProjectileData")&&field.equals("homing_angle")){
     mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,"net/spell_engine/internals/ArcherProjectileRules","homing","(Lnet/spell_engine/api/spell/Spell$ProjectileData;Lnet/spell_engine/entity/SpellProjectile;)F",false);edits[0]++;
    }else super.visitFieldInsn(op,owner,field,desc);
   }};
  }},0);
  if(edits[0]!=1)throw new AssertionError("Expected one homing read, got "+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
