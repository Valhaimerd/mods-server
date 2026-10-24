import java.nio.file.*;
import org.objectweb.asm.*;
/** Append narrow hooks without rebuilding or replacing existing pack compatibility helpers. */
public class PatchWarrior implements Opcodes {
 static final String HELPER="net/spell_engine/entity/WarriorRules";
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};String mode=args[2];
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){
   var out=super.visitMethod(a,name,desc,sig,ex);return new MethodVisitor(ASM9,out){
    public void visitCode(){super.visitCode();
     if(mode.equals("skill")&&name.equals("register")&&desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,HELPER,"skill",desc,false);edits[0]++;}
     if(mode.equals("projectile")&&name.equals("tick")){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,HELPER,"beforeTick","(Lnet/spell_engine/entity/SpellProjectile;)V",false);edits[0]++;}
    }
    public void visitMethodInsn(int op,String owner,String n,String d,boolean itf){
     if(mode.equals("impacts")&&owner.equals("net/spell_engine/internals/RogueRules")&&java.util.Set.of("impact","addEffect","after").contains(n)){owner=HELPER;edits[0]++;}
     if(mode.equals("impacts")&&owner.equals("net/spell_engine/internals/PartyRules")&&n.equals("bossGate")){owner=HELPER;edits[0]++;}
     if(mode.equals("triggers")&&owner.equals("net/spell_engine/internals/SpellTriggers")&&n.equals("evaluateTrigger")&&!name.equals("evaluateTrigger")){owner=HELPER;n="evaluate";edits[0]++;}
     if(mode.equals("stash")&&owner.equals("net/spell_engine/internals/RogueRules")&&n.equals("evaluate")){owner=HELPER;edits[0]++;}
     if(mode.equals("launcher")&&name.equals("shootProjectile")&&n.equals("addFreshEntity")&&d.equals("(Lnet/minecraft/world/entity/Entity;)Z")){
      mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,3);mv.visitVarInsn(ALOAD,4);mv.visitMethodInsn(INVOKESTATIC,HELPER,"launch","(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Lnet/minecraft/world/entity/Entity;",false);edits[0]++;
     }
     super.visitMethodInsn(op,owner,n,d,itf);
    }
    public void visitFieldInsn(int op,String owner,String field,String d){
     super.visitFieldInsn(op,owner,field,d);
     if(mode.equals("parameters")&&op==GETFIELD&&owner.equals("net/spell_engine/api/spell/Spell")&&field.equals("school")&&java.util.Set.of("getCooldownDuration","getCastDuration","getCastTimeDetails").contains(name)){
      mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,name.equals("getCooldownDuration")?3:1);mv.visitMethodInsn(INVOKESTATIC,HELPER,"hasteSchool","(Lnet/spell_power/api/SpellSchool;Lnet/minecraft/world/entity/LivingEntity;Lnet/spell_engine/api/spell/Spell;)Lnet/spell_power/api/SpellSchool;",false);edits[0]++;
     }
    }
   };
  }},0);
  if(edits[0]==0)throw new AssertionError("No hooks: "+mode);System.out.println(mode+" hooks="+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
