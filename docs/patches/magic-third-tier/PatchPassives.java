import java.nio.file.*;
import org.objectweb.asm.*;
public final class PatchPassives implements Opcodes {
 public static void main(String[] args)throws Exception {
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);
  String rules="net/spell_engine/internals/PassiveRules";int[] edits={0};
  r.accept(new ClassVisitor(ASM9,w){
   public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex) {
    var out=super.visitMethod(access,name,desc,sig,ex);
    return new MethodVisitor(ASM9,out){
     public void visitCode(){super.visitCode();
      if(r.getClassName().equals("net/skill_tree_rpgs/effect/SkillEffects") && name.equals("register")) {
       mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,rules,"configure","(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V",false);edits[0]++;
      }
     }
     public void visitMethodInsn(int op,String owner,String method,String d,boolean itf) {
      if(r.getClassName().equals("net/skill_tree_rpgs/effect/SkillEffects") && name.equals("register") && owner.equals("net/spell_engine/api/effect/InstantCast") && method.equals("register")) {
       mv.visitInsn(POP2);edits[0]++;return;
      }
      if(r.getClassName().endsWith("/SpellExecution") && name.equals("performSpell") && owner.equals("net/spell_engine/internals/delivery/SpellDelivery") && method.equals("resolveAndDeliver")) {
       owner=rules;method="deliver";edits[0]++;
      }
      if(r.getClassName().endsWith("/SpellImpacts") && name.equals("performImpact") && owner.equals("net/spell_engine/internals/RevisionRules") && method.equals("impact")) {
       owner=rules;edits[0]++;
      }
      super.visitMethodInsn(op,owner,method,d,itf);
      if(r.getClassName().endsWith("/SpellImpacts") && name.equals("performImpacts") && owner.equals("net/spell_engine/internals/SpellModifiers") && method.equals("extendedImpactsOf")) {
       mv.visitVarInsn(ALOAD,6);mv.visitMethodInsn(INVOKESTATIC,rules,"impacts","(Lnet/spell_engine/internals/SpellModifiers$ExtendedImpacts;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Lnet/spell_engine/internals/SpellModifiers$ExtendedImpacts;",false);edits[0]++;
      }
     }
    };
   }
  },0);
  int expected=r.getClassName().endsWith("/SkillEffects")?3:r.getClassName().endsWith("/SpellImpacts")?2:1;
  if(edits[0]!=expected)throw new AssertionError(r.getClassName()+": "+edits[0]+" expected "+expected);
  Files.write(Path.of(args[1]),w.toByteArray());
 }
}
