import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchBalance implements Opcodes {
 public static void main(String[] args)throws Exception {
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);
  int edits=0;String helper="net/spell_engine/entity/WarriorBalance";
  if(args[2].equals("rules"))for(var m:node.methods)if(m.name.equals("skill"))
   for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode call&&call.owner.equals("net/spell_engine/entity/WarriorUltimates")&&call.name.equals("skill")){var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"configure","(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V",false));m.instructions.insert(i,add);edits++;}
  if(args[2].equals("tierTwo"))for(var m:node.methods)for(var i:m.instructions.toArray()) {
   if(m.name.equals("rogue")&&i instanceof LdcInsnNode c&&c.cst.equals(-.08f)){c.cst=-.12f;edits++;}
   if(m.name.equals("after")&&i instanceof IntInsnNode c&&c.operand==40){
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,1));add.add(new MethodInsnNode(INVOKESTATIC,helper,"freedomTicks","(Lnet/minecraft/world/entity/LivingEntity;)I",false));m.instructions.insertBefore(i,add);m.instructions.remove(i);edits++;
   }
  }
  if(args[2].equals("ultimate")) {
   for(var f:node.fields)if(f.name.equals("TENACITY_TICKS")){if(!f.value.equals(240))throw new AssertionError();f.value=280;}
   for(var m:node.methods) {
    if(m.name.startsWith("lambda$interrupt$")) {
     m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
     m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new MethodInsnNode(INVOKESTATIC,helper,"interrupting","(Lnet/minecraft/world/effect/MobEffectInstance;)Z",false));m.instructions.add(new InsnNode(IRETURN));edits++;
    }
    for(var i:m.instructions.toArray()) {
    if(m.name.equals("melee")&&i instanceof MethodInsnNode call&&call.name.equals("heal")&&call.desc.equals("(F)V")) {
     m.instructions.set(i,new MethodInsnNode(INVOKESTATIC,helper,"lethalityHeal","(Lnet/minecraft/world/entity/player/Player;F)V",false));edits++;
    }
    if(m.name.equals("tenacity")&&i instanceof IntInsnNode c&&c.operand==240){c.operand=280;edits++;}
    if(m.name.equals("pulse")&&i instanceof MethodInsnNode call&&call.name.equals("getAmplifier")){
     var n=i.getNext();if(n.getOpcode()!=ICONST_4||n.getNext().getOpcode()!=IF_ICMPNE)throw new AssertionError("pulse guard changed");
     m.instructions.set(n,new InsnNode(ICONST_M1));((JumpInsnNode)i.getNext().getNext()).setOpcode(IF_ICMPLE);edits++;
    }
    if(m.name.equals("pulse")&&i.getOpcode()==ICONST_3){
     var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"speedLevel","(Lnet/minecraft/world/entity/LivingEntity;)I",false));m.instructions.insertBefore(i,add);m.instructions.remove(i);edits++;
    }
   }}
  }
  int expected=args[2].equals("rules")?1:args[2].equals("tierTwo")?2:5;
  if(edits!=expected)throw new AssertionError(args[2]+" edits "+edits+" expected "+expected);
  var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
