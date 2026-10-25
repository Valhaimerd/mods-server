import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchPriest implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int edits=0;
  String mode=args[2],helper="net/spell_engine/internals/PriestRules";
  for(var m:node.methods){
   if(mode.equals("effects")&&m.name.equals("register")&&m.desc.equals("(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V")){
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"configure",m.desc,false));m.instructions.insert(add);edits++;
   }
   if(mode.equals("absorb")&&m.name.equals("onEffectStarted")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ILOAD,2));m.instructions.add(new InsnNode(node.name.contains("Tenacity")?ICONST_1:ICONST_2));m.instructions.add(new VarInsnNode(ALOAD,0));
    m.instructions.add(new MethodInsnNode(INVOKESTATIC,helper,"absorptionStarted","(Lnet/minecraft/world/entity/LivingEntity;IILnet/minecraft/world/effect/MobEffect;)V",false));m.instructions.add(new InsnNode(RETURN));edits++;
   }
   for(var i:m.instructions.toArray()){
    if(i instanceof MethodInsnNode call){
     if(mode.equals("delivery")&&call.owner.equals("net/spell_engine/internals/PassiveRules")&&call.name.equals("deliver")){call.owner=helper;edits++;}
     if(mode.equals("impact")&&call.owner.equals("net/spell_engine/entity/WarriorRules")&&call.name.equals("impact")){call.owner=helper;edits++;}
     if(mode.equals("projectile")&&call.owner.equals("net/spell_engine/internals/impact/SpellImpacts")&&call.name.equals("projectileImpact")){call.owner=helper;call.name="projectile";edits++;}
     if(mode.equals("cooldown")&&call.owner.equals("net/spell_engine/internals/ThirdTierRules")&&call.name.equals("cooldown")){call.owner=helper;edits++;}
    }
    if(mode.equals("timing")&&m.name.equals("<init>")&&m.desc.startsWith("(Lnet/minecraft/world/entity/LivingEntity;")&&i instanceof LdcInsnNode c&&c.cst.equals(.5f)){
     var add=new InsnList();add.add(new VarInsnNode(ALOAD,2));add.add(new MethodInsnNode(INVOKESTATIC,helper,"pulseOffset","(Lnet/minecraft/core/Holder;)F",false));m.instructions.insertBefore(i,add);m.instructions.remove(i);edits++;
    }
   }
  }
  int expected=mode.equals("projectile")?2:1;
  if(edits!=expected)throw new AssertionError(mode+" edits="+edits+" expected="+expected);
  var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
