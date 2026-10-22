import java.nio.file.*;
import org.objectweb.asm.*;
public final class PatchTierTwo implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};String rules="net/spell_engine/internals/TrapRules",state="net/rogues/client/entity/BearTrapEntityRenderer$State";
  r.accept(new ClassVisitor(ASM9,w){
   public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){var out=super.visitMethod(a,name,desc,sig,ex);
    return new MethodVisitor(ASM9,out){
     public void visitCode(){super.visitCode();if(name.equals("submit")&&desc.startsWith("(L"+state+";")){
      mv.visitVarInsn(ALOAD,1);mv.visitFieldInsn(GETFIELD,state,"packHidden","Z");Label visible=new Label();mv.visitJumpInsn(IFEQ,visible);mv.visitInsn(RETURN);mv.visitLabel(visible);mv.visitFrame(F_SAME,0,null,0,null);edits[0]++;
     }}
     public void visitInsn(int op){
      if(op==RETURN&&name.equals("extractRenderState")&&desc.startsWith("(Lnet/rogues/entity/BearTrapEntity;")){
       mv.visitVarInsn(ALOAD,2);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,rules,"hidden","(Lnet/spell_engine/entity/SpellCloud;)Z",false);mv.visitFieldInsn(PUTFIELD,state,"packHidden","Z");edits[0]++;
      }super.visitInsn(op);
     }
     public void visitMethodInsn(int op,String owner,String n,String d,boolean itf){
      if(r.getClassName().endsWith("/SpellCloud")&&name.equals("tick")&&owner.equals("net/spell_engine/internals/impact/SpellImpacts")&&n.equals("lookupAndPerformAreaImpact")){owner=rules;n="area";edits[0]++;}
      if(r.getClassName().endsWith("/SpellCloud")&&name.equals("getDimensions")&&owner.equals("net/spell_engine/internals/WallRules")&&n.equals("dimensions")){owner=rules;edits[0]++;}
      super.visitMethodInsn(op,owner,n,d,itf);
     }
    };
   }
   public void visitEnd(){if(r.getClassName().equals(state)){super.visitField(ACC_PUBLIC,"packHidden","Z",null,null).visitEnd();edits[0]++;}super.visitEnd();}
  },0);
  int expected=r.getClassName().equals(state)?1:2;if(edits[0]!=expected)throw new AssertionError(r.getClassName()+": "+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
