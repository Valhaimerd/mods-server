import java.nio.file.*;
import org.objectweb.asm.*;
public final class PatchWalls implements Opcodes {
 static final String RULES="net/spell_engine/internals/WallRules", CLOUD="net/spell_engine/entity/SpellCloud";
 public static void main(String[] args) throws Exception {
  ClassReader r=new ClassReader(Files.readAllBytes(Path.of(args[0])));
  ClassWriter w=new ClassWriter(r,ClassWriter.COMPUTE_MAXS);
  int[] edits={0};
  r.accept(new ClassVisitor(ASM9,w) {
   public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex) {
    MethodVisitor out=super.visitMethod(access,name,desc,sig,ex);
    if(r.getClassName().endsWith("/ClientCastController") && name.equals("keyHeld")) {
     edits[0]++;return new MethodVisitor(ASM9,out) {
      public void visitCode(){super.visitCode();mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ILOAD,2);mv.visitVarInsn(ILOAD,3);mv.visitInsn(IAND);
       mv.visitMethodInsn(INVOKESTATIC,RULES,"ignoreHeldRepeat","(Lnet/spell_engine/internals/casting/SpellCast$Option;Z)Z",false);
       Label normal=new Label();mv.visitJumpInsn(IFEQ,normal);
       mv.visitFieldInsn(GETSTATIC,"net/spell_engine/client/casting/ClientCastController$Reaction","NOTHING","Lnet/spell_engine/client/casting/ClientCastController$Reaction;");mv.visitInsn(ARETURN);
       mv.visitLabel(normal);mv.visitFrame(F_SAME,0,null,0,null);}
     };
    }
    if(r.getClassName().endsWith("/SpellCooldowns") && name.equals("imposeCooldown")) {
     edits[0]++; return new MethodVisitor(ASM9,out) {
      public void visitCode(){super.visitCode();mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,2);mv.visitVarInsn(FLOAD,3);
       mv.visitMethodInsn(INVOKESTATIC,RULES,"imposeCooldown",desc.replace(")V",")Z"),false);
       Label normal=new Label();mv.visitJumpInsn(IFEQ,normal);mv.visitInsn(RETURN);mv.visitLabel(normal);mv.visitFrame(F_SAME,0,null,0,null);}
     };
    }
    if(r.getClassName().endsWith("/SpellCooldownManager") && name.equals("tickUpdate")) {
     edits[0]++;return new MethodVisitor(ASM9,out){public void visitInsn(int op){
      // Run after the manager increments its tick, preserving exactly 480 subsequent cooldown ticks.
      if(op==RETURN){mv.visitVarInsn(ALOAD,0);mv.visitFieldInsn(GETFIELD,r.getClassName(),"owner","Lnet/minecraft/world/entity/Entity;");mv.visitMethodInsn(INVOKESTATIC,RULES,"tick","(Lnet/minecraft/world/entity/Entity;)V",false);}super.visitInsn(op);}};
    }
    if(r.getClassName().endsWith("/SpellImpacts") && name.equals("lookupAndPerformAreaImpact")) {
     return new MethodVisitor(ASM9,out){ public void visitMethodInsn(int op,String owner,String n,String d,boolean itf){
      if(owner.equals("net/spell_engine/utils/TargetHelper") && n.equals("targetsFromArea")){owner=RULES;edits[0]++;}super.visitMethodInsn(op,owner,n,d,itf);}};
    }
    if(r.getClassName().equals(CLOUD)&&name.equals("onCreatedFromSpell")) {
     edits[0]++;return new MethodVisitor(ASM9,out){public void visitInsn(int op){if(op==RETURN){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKEVIRTUAL,CLOUD,"refreshDimensions","()V",false);}super.visitInsn(op);}};
    }
    if(r.getClassName().equals(CLOUD)&&name.equals("getDimensions")){
     // Replace the existing dimension factory call: retain radius-derived width; substitute fixed height only for selected clouds.
     return new MethodVisitor(ASM9,out){public void visitMethodInsn(int op,String owner,String n,String d,boolean itf){
      if(owner.equals("net/minecraft/world/entity/EntityDimensions")&&n.equals("scalable")){
       mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULES,"dimensions","(FFLnet/spell_engine/entity/SpellCloud;)Lnet/minecraft/world/entity/EntityDimensions;",false);edits[0]++;
      }else super.visitMethodInsn(op,owner,n,d,itf);}};
    }
    return out;
   }
   public void visitEnd(){if(r.getClassName().equals(CLOUD)){
    MethodVisitor m=super.visitMethod(ACC_PUBLIC,"canBeCollidedWith","(Lnet/minecraft/world/entity/Entity;)Z",null,null);m.visitCode();m.visitVarInsn(ALOAD,0);m.visitVarInsn(ALOAD,1);m.visitMethodInsn(INVOKESTATIC,RULES,"solid","(Lnet/spell_engine/entity/SpellCloud;Lnet/minecraft/world/entity/Entity;)Z",false);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();
    for(String n:new String[]{"isPickable","canBeHitByProjectile"}){m=super.visitMethod(ACC_PUBLIC,n,"()Z",null,null);m.visitCode();m.visitInsn(ICONST_0);m.visitInsn(IRETURN);m.visitMaxs(0,0);m.visitEnd();}edits[0]+=3;
   }super.visitEnd();}
  },0);
  int expected=r.getClassName().equals(CLOUD)?5:1;
  if(edits[0]!=expected)throw new AssertionError(r.getClassName()+": expected "+expected+" edits, got "+edits[0]);
  Files.write(Path.of(args[1]),w.toByteArray());
 }
}
