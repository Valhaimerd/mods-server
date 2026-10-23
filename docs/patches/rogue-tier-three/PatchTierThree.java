import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchTierThree implements Opcodes {
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};String helper="net/spell_engine/internals/RogueTierThree";
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){
   var out=super.visitMethod(a,name,desc,sig,ex);
   return new MethodVisitor(ASM9,out){
    public void visitCode(){super.visitCode();if(name.equals("register")&&desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){
     mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,helper,args[2],desc,false);edits[0]++;
    }}
    public void visitMethodInsn(int op,String owner,String method,String d,boolean itf){
     if(r.getClassName().equals("net/rogues/effect/RogueEffects")&&method.equals("removeEffect")&&d.equals("(Lnet/minecraft/core/Holder;)Z")){
      op=INVOKESTATIC;owner=helper;method="removeForAction";d="(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)Z";itf=false;edits[0]++;
     }
     if(r.getClassName().equals("net/rogues/effect/RogueEffects")&&owner.equals("net/rogues/effect/StealthEffect")&&method.equals("onRemove")){
      mv.visitInsn(DUP);mv.visitMethodInsn(INVOKESTATIC,helper,"cleanup","(Lnet/minecraft/world/entity/LivingEntity;)V",false);edits[0]++;
     }
     if(r.getClassName().equals("net/spell_engine/SpellEngineMod")&&owner.equals("net/spell_engine/api/effect/RemoveOnHit")&&method.equals("removeCount")){
      mv.visitVarInsn(ALOAD,0);owner=helper;method="removeOnHit";itf=false;d=d.replace(")I","Lnet/minecraft/world/entity/LivingEntity;)I");edits[0]++;
     }
     if(r.getClassName().equals("net/spell_engine/internals/delivery/melee/Melee")&&name.equals("performAttackAgainstTargets")&&method.equals("attack")&&d.equals("(Lnet/minecraft/world/entity/Entity;)V")){
      mv.visitVarInsn(ALOAD,1);op=INVOKESTATIC;owner=helper;method="attack";d="(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/spell_engine/internals/delivery/melee/Melee$AttackContext;)V";itf=false;edits[0]++;
     }
     if(r.getClassName().equals("net/spell_engine/internals/delivery/melee/Melee")&&owner.equals("net/spell_engine/internals/impact/SpellImpacts")&&method.equals("meleeImpact")){
      owner=helper;method="meleeImpacts";edits[0]++;
     }
     super.visitMethodInsn(op,owner,method,d,itf);
    }
   };
  }},0);
  int expected=r.getClassName().equals("net/rogues/effect/RogueEffects")?6:r.getClassName().endsWith("/Melee")?2:1;
  if(edits[0]!=expected)throw new AssertionError(r.getClassName()+" edits="+edits[0]+" expected="+expected);
  Files.write(Path.of(args[1]),w.toByteArray());
 }
}
