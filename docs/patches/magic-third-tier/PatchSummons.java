import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchSummons implements Opcodes {
 static final String RULES="net/spell_engine/internals/SummonRules";
 public static void main(String[] args) throws Exception {
  var reader=new ClassReader(Files.readAllBytes(Path.of(args[1])));
  var writer=new ClassWriter(reader,ClassWriter.COMPUTE_MAXS);
  String mode=args[0]; int[] n={0};
  reader.accept(new ClassVisitor(ASM9,writer) {
   public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex) {
    var base=super.visitMethod(access,name,desc,sig,ex);
    if(mode.equals("entity") && name.equals("onSummonedBySpell")) return new MethodVisitor(ASM9,base) {
     public void visitCode(){super.visitCode(); mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,RULES,"prepare","(Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;)Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;",false);mv.visitVarInsn(ASTORE,1);n[0]++;}
     public void visitInsn(int op){if(op==RETURN){mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,RULES,"summoned","(Lnet/spell_engine/entity/SummonedEntity;Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;)V",false);n[0]++;}super.visitInsn(op);}
    };
    if(mode.equals("entity") && name.equals("tick")) return new MethodVisitor(ASM9,base) {
     public void visitCode(){super.visitCode();mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULES,"tick","(Lnet/spell_engine/entity/SummonedEntity;)V",false);n[0]++;}
    };
    if(mode.equals("entity") && name.equals("lockRotationTo")) return new MethodVisitor(ASM9,base) {
     public void visitInsn(int op){if(op==RETURN){mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,RULES,"orient","(Lnet/spell_engine/entity/SummonedEntity;Lnet/minecraft/world/entity/LivingEntity;)V",false);n[0]++;}super.visitInsn(op);}
    };
    if(mode.equals("impacts") && name.equals("summon")) return new MethodVisitor(ASM9,base) {
     public void visitMethodInsn(int op,String owner,String method,String d,boolean itf){super.visitMethodInsn(op,owner,method,d,itf);if(owner.equals("java/lang/Math")&&method.equals("max")&&d.equals("(II)I")){mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitInsn(n[0]++==0?ICONST_0:ICONST_1);mv.visitMethodInsn(INVOKESTATIC,RULES,"count","(ILnet/spell_engine/api/spell/Spell$Impact$Action$Summon;Ljava/util/List;Z)I",false);}}
    };
    if(mode.equals("launch")&& name.equals("launchPoint")&&desc.equals("(Lnet/minecraft/world/entity/LivingEntity;FF)Lnet/minecraft/world/phys/Vec3;")) return new MethodVisitor(ASM9,base){
     public void visitInsn(int op){if(op==ARETURN){mv.visitVarInsn(ALOAD,0);mv.visitMethodInsn(INVOKESTATIC,RULES,"launch","(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/phys/Vec3;",false);n[0]++;}super.visitInsn(op);}
    };
    if(mode.equals("controller")&&name.equals("tickChannel")) return new MethodVisitor(ASM9,base){
     public void visitCode(){super.visitCode();mv.visitVarInsn(ALOAD,0);mv.visitFieldInsn(GETFIELD,"net/spell_engine/internals/casting/MobCastController","entity","Lnet/spell_engine/entity/SummonedEntity;");mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESTATIC,RULES,"channel","(Lnet/spell_engine/entity/SummonedEntity;Lnet/minecraft/core/Holder;)Z",false);Label normal=new Label();mv.visitJumpInsn(IFEQ,normal);mv.visitInsn(RETURN);mv.visitLabel(normal);mv.visitFrame(F_SAME,0,null,0,null);n[0]++;}
    };
    if(mode.equals("model") && name.equals("setupAnim") && desc.equals("(Lnet/wizards/client/entity/SummonedEntityRenderState;)V")) return new MethodVisitor(ASM9,base){
     public void visitMethodInsn(int op,String owner,String method,String d,boolean itf){
      super.visitMethodInsn(op,owner,method,d,itf);
      if(owner.equals("net/minecraft/client/model/EntityModel")&&method.equals("setupAnim")) {
       Label normal=new Label();
       mv.visitVarInsn(ALOAD,1);mv.visitFieldInsn(GETFIELD,"net/minecraft/client/renderer/entity/state/LivingEntityRenderState","scale","F");mv.visitLdcInsn(2.99f);mv.visitInsn(FCMPL);mv.visitJumpInsn(IFLE,normal);
       mv.visitVarInsn(ALOAD,1);mv.visitFieldInsn(GETFIELD,"net/wizards/client/entity/SummonedEntityRenderState","spellCastAnimationState","Lnet/minecraft/world/entity/AnimationState;");mv.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/world/entity/AnimationState","isStarted","()Z",false);mv.visitJumpInsn(IFEQ,normal);
       mv.visitVarInsn(ALOAD,0);mv.visitFieldInsn(GETFIELD,"net/wizards/client/entity/FireHydraModel","firebreathAnimation","Lnet/minecraft/client/animation/KeyframeAnimation;");mv.visitLdcInsn(2000L);mv.visitInsn(FCONST_1);mv.visitMethodInsn(INVOKEVIRTUAL,"net/minecraft/client/animation/KeyframeAnimation","apply","(JF)V",false);mv.visitInsn(RETURN);mv.visitLabel(normal);mv.visitFrame(F_SAME,0,null,0,null);n[0]++;
      }
     }
    };
    return base;
   }
   public void visitEnd(){
    if(mode.equals("frost")) {
     var mv=super.visitMethod(ACC_PUBLIC,"getAttributeValue","(Lnet/minecraft/core/Holder;)D","(Lnet/minecraft/core/Holder<Lnet/minecraft/world/entity/ai/attributes/Attribute;>;)D",null);
     mv.visitCode();mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitVarInsn(ALOAD,0);mv.visitVarInsn(ALOAD,1);mv.visitMethodInsn(INVOKESPECIAL,"net/spell_engine/entity/SummonedEntity","getAttributeValue","(Lnet/minecraft/core/Holder;)D",false);mv.visitMethodInsn(INVOKESTATIC,RULES,"unclamped","(Lnet/spell_engine/entity/SummonedEntity;Lnet/minecraft/core/Holder;D)D",false);mv.visitInsn(DRETURN);mv.visitMaxs(0,0);mv.visitEnd();n[0]++;
    }
    super.visitEnd();
   }
  },0);
  int expected=mode.equals("entity")?4:mode.equals("impacts")?2:1;
  if(n[0]!=expected)throw new AssertionError(mode+": expected "+expected+", got "+n[0]);
  Files.write(Path.of(args[2]),writer.toByteArray());
 }
}
