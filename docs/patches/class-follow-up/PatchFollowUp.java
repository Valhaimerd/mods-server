import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Count-checked edits to the current classes preserve all previous pack hooks. */
public final class PatchFollowUp implements Opcodes {
 static final String RULE="net/spell_engine/internals/FollowUpRules";
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);String mode=args[2];int edits=0;
  if(mode.equals("animationpass"))c.fields.add(new FieldNode(ACC_PRIVATE,"classPackLastFirstPersonPass","Z",null,null));
  for(var m:c.methods){
   if(mode.equals("animationpass")&&m.name.equals("handleAnimations")){
    var list=new InsnList();var next=new LabelNode();
    list.add(new VarInsnNode(ALOAD,0));list.add(new FieldInsnNode(GETFIELD,c.name,"classPackLastFirstPersonPass","Z"));list.add(new VarInsnNode(ILOAD,3));list.add(new JumpInsnNode(IF_ICMPEQ,next));
    list.add(new VarInsnNode(ALOAD,0));list.add(new LdcInsnNode(Float.NaN));list.add(new MethodInsnNode(INVOKEVIRTUAL,c.name,"updatedAt","(F)V",false));
    list.add(next);list.add(new FrameNode(F_SAME,0,null,0,null));list.add(new VarInsnNode(ALOAD,0));list.add(new VarInsnNode(ILOAD,3));list.add(new FieldInsnNode(PUTFIELD,c.name,"classPackLastFirstPersonPass","Z"));m.instructions.insert(list);edits++;
   }
   if(mode.equals("config")&&(m.name.equals("skill")||m.name.equals("configure"))&&m.desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){
    for(var ins:m.instructions.toArray())if(ins.getOpcode()==RETURN){var list=new InsnList();list.add(new VarInsnNode(ALOAD,0));list.add(new MethodInsnNode(INVOKESTATIC,RULE,"configure",m.desc,false));m.instructions.insertBefore(ins,list);edits++;}
   }
   if(mode.equals("ardent")&&m.name.equals("tryArdent"))for(var ins:m.instructions.toArray())if(ins instanceof LdcInsnNode l&&Double.valueOf(.4).equals(l.cst)){l.cst=.2;edits++;}
   if(mode.equals("details"))for(var ins:m.instructions.toArray())if(ins instanceof LdcInsnNode l&&"Next ordinary arrow: 125% normal damage.".equals(l.cst)){l.cst="Next ordinary arrow: 135% normal damage.";edits++;}
   if(mode.equals("warmup")&&m.name.equals("context")){
    m.instructions.clear();m.tryCatchBlocks.clear();m.localVariables=null;
    m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ALOAD,2));m.instructions.add(new MethodInsnNode(INVOKESTATIC,RULE,"warmup",m.desc,false));m.instructions.add(new InsnNode(ARETURN));edits++;
   }
   if(mode.equals("cooldown")&&m.name.equals("imposeCooldown")){
    var list=new InsnList();var next=new LabelNode();list.add(new VarInsnNode(ALOAD,2));list.add(new MethodInsnNode(INVOKESTATIC,RULE,"deferred","(Lnet/minecraft/core/Holder;)Z",false));list.add(new JumpInsnNode(IFEQ,next));list.add(new InsnNode(RETURN));list.add(next);list.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(list);edits++;
   }
   if(mode.equals("crit")&&m.name.equals("modifyDamage"))for(var ins:m.instructions.toArray())if(ins instanceof MethodInsnNode call&&call.name.equals("rng_shouldDealCriticalHit")){
    m.instructions.insertBefore(call,new VarInsnNode(ALOAD,1));m.instructions.set(call,new MethodInsnNode(INVOKESTATIC,RULE,"guaranteed","(Lnet/critical_strike/internal/CriticalStriker;Lnet/minecraft/world/damagesource/DamageSource;)Z",false));edits++;
   }
  }
  int expected=Integer.parseInt(args[3]);if(edits!=expected)throw new AssertionError(mode+" edits="+edits+" expected="+expected);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
