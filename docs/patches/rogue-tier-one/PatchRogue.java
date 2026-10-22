import java.nio.file.*;
import org.objectweb.asm.*;
public final class PatchRogue implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};String rules="net/spell_engine/internals/RogueRules";
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){
   var out=super.visitMethod(a,name,desc,sig,ex);
   return new MethodVisitor(ASM9,out){
    public void visitCode(){super.visitCode();if(name.equals("register")&&desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){
     mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,rules,args[2],desc,false);edits[0]++;
    }}
    public void visitMethodInsn(int op,String owner,String method,String d,boolean itf){
     if(name.equals("performImpact")&&owner.equals("net/spell_engine/internals/PassiveRules")&&method.equals("impact")){
      mv.visitVarInsn(ALOAD,2);owner=rules;d=d.replace(")Lnet/spell_engine/api/spell/Spell$Impact;","Lnet/minecraft/world/entity/Entity;)Lnet/spell_engine/api/spell/Spell$Impact;");edits[0]++;
     }
     if(name.equals("performImpact")&&owner.equals("net/spell_engine/internals/PartyRules")&&method.equals("addEffect")){owner=rules;edits[0]++;}
     if(r.getClassName().endsWith("/SpellStashHelper")&&owner.equals("net/spell_engine/internals/SpellTriggers")&&method.equals("evaluateTrigger")){owner=rules;method="evaluate";edits[0]++;}
     super.visitMethodInsn(op,owner,method,d,itf);
    }
    public void visitInsn(int op){
     if(name.equals("performImpact")&&op==IRETURN){
      mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,2);mv.visitVarInsn(ALOAD,3);mv.visitVarInsn(ALOAD,4);
      mv.visitMethodInsn(INVOKESTATIC,rules,"after","(ZLnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/spell_engine/api/spell/Spell$Impact;)Z",false);
     }
     super.visitInsn(op);
    }
   };
  }},0);
  int expected=r.getClassName().endsWith("/SpellImpacts")?2:1;
  if(edits[0]!=expected)throw new AssertionError(r.getClassName()+": "+edits[0]+" expected "+expected);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
