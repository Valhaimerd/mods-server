import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchUltimates implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};String helper="net/spell_engine/entity/WarriorUltimates";
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){var out=super.visitMethod(a,n,d,s,e);return new MethodVisitor(ASM9,out){
   public void visitCode(){super.visitCode();
    if(args[2].equals("melee")&&n.equals("onMeleeImpact")){mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,helper,"melee",d,false);edits[0]++;}
    if(args[2].equals("rogue")&&n.equals("register")&&d.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,helper,"rogue",d,false);edits[0]++;}
   }
   public void visitFieldInsn(int op,String owner,String field,String desc){super.visitFieldInsn(op,owner,field,desc);
    if(args[2].equals("cooldown")&&n.equals("getCooldownDuration")&&op==GETFIELD&&owner.equals("net/spell_engine/api/spell/Spell$Cost$Cooldown")&&field.equals("duration")){
     mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,helper,"duration","(FLnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)F",false);edits[0]++;
    }
   }
  };}},0);
  if(edits[0]!=1)throw new AssertionError(args[2]+" edits="+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
