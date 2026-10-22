import java.nio.file.*;
import org.objectweb.asm.*;
/** Straight-line hooks preserve native frames and every previous pack patch. */
public final class PatchArcherProjectile implements Opcodes {
 static final String RULES="net/spell_engine/entity/ArcherProjectileRules";
 public static void main(String[] args)throws Exception{
  var r=new ClassReader(Files.readAllBytes(Path.of(args[0])));var w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);int[] edits={0};
  String mode=args.length>2?args[2]:"projectile";
  r.accept(new ClassVisitor(ASM9,w){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] e){
   var out=super.visitMethod(a,n,d,s,e);
   if(mode.equals("launcher")&&n.equals("fallProjectile")&&d.endsWith(";IZ"))return out;
   return new MethodVisitor(ASM9,out){
    public void visitCode(){super.visitCode();
     if(mode.equals("projectile")&&n.equals("tick")){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULES,"beforeTick","(Lnet/spell_engine/entity/SpellProjectile;)V",false);edits[0]++;}
     if(mode.equals("launcher")&&n.equals("fallProjectile")&&d.endsWith(";I)Z")){
      mv.visitVarInsn(ALOAD,4);mv.visitVarInsn(ALOAD,2);mv.visitVarInsn(ALOAD,3);mv.visitMethodInsn(INVOKESTATIC,RULES,"rainLocation","(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",false);mv.visitVarInsn(ASTORE,3);
      mv.visitVarInsn(ALOAD,4);mv.visitVarInsn(ALOAD,2);mv.visitMethodInsn(INVOKESTATIC,RULES,"rainTarget","(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;",false);mv.visitVarInsn(ASTORE,2);edits[0]++;
     }
    }
    public void visitFieldInsn(int op,String owner,String field,String desc){
     if(mode.equals("stash")&&op==GETFIELD&&owner.equals("net/spell_engine/api/spell/Spell$Delivery$StashEffect")&&field.equals("duration")){
      mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,2);mv.visitMethodInsn(INVOKESTATIC,RULES,"stashDuration","(Lnet/spell_engine/api/spell/Spell$Delivery$StashEffect;Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)F",false);edits[0]++;
     }else super.visitFieldInsn(op,owner,field,desc);
    }
    public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){
     if(mode.equals("projectile")&&n.equals("followTarget")&&owner.equals("net/spell_engine/internals/ArcherProjectileRules")&&method.equals("homing")){super.visitMethodInsn(op,RULES,method,desc,itf);edits[0]++;return;}
     if(mode.equals("projectile")&&n.equals("tick")&&owner.equals("net/minecraft/world/entity/projectile/ProjectileUtil")&&method.equals("getHitResultOnMoveVector")){super.visitMethodInsn(op,RULES,"collision",desc,itf);edits[0]++;return;}
     super.visitMethodInsn(op,owner,method,desc,itf);
     if(mode.equals("launcher")&&n.equals("fallProjectile")&&owner.equals("net/minecraft/world/phys/Vec3")&&method.equals("yRot")){
      mv.visitVarInsn(ALOAD,4);mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULES,"scatter","(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/phys/Vec3;",false);edits[0]++;
     }
    }
   };
  }},0);
  int expected=mode.equals("projectile")?3:2;
  if(edits[0]!=expected)throw new AssertionError(mode+" expected "+expected+" hooks, got "+edits[0]);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
