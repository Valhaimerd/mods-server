import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchTierTwo implements Opcodes {
 static String RULE="net/spell_engine/entity/WarriorTierTwo";
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){var out=super.visitMethod(a,name,desc,sig,ex);return new MethodVisitor(ASM9,out){
   public void visitCode(){super.visitCode();
    if(args[2].equals("rogue")&&name.equals("register")&&desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULE,"rogue",desc,false);edits[0]++;}
    if(args[2].equals("impacts")&&name.equals("performImpact")){mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,3);mv.visitVarInsn(ALOAD,5);mv.visitMethodInsn(INVOKESTATIC,RULE,"context","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Lnet/spell_engine/internals/SpellExecution$ImpactContext;",false);mv.visitVarInsn(ASTORE,5);edits[0]++;}
   }
   public void visitMethodInsn(int op,String owner,String n,String d,boolean itf){
    if(args[2].equals("client")&&name.equals("startSpellCast")&&owner.equals("net/spell_engine/api/effect/EntityActionsAllowed")&&n.equals("isImpaired")){
     mv.visitVarInsn(ALOAD,2);owner=RULE;n="impaired";d=d.replace(")Z","Lnet/minecraft/core/Holder;)Z");edits[0]++;
    }super.visitMethodInsn(op,owner,n,d,itf);
   }
  };}},0);
  if(edits[0]!=1)throw new AssertionError(args[2]+" hooks="+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
