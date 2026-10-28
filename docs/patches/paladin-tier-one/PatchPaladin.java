import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchPaladin implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int edits=0;String mode=args[2],helper="net/spell_engine/internals/PaladinTierOne";
  for(var m:node.methods){
   if(mode.equals("init")&&m.name.equals("configure")){
    m.instructions.insert(new MethodInsnNode(INVOKESTATIC,helper,"init","()V",false));edits++;
   }
   if(mode.equals("swing")&&m.name.equals("lambda$handleAttackRequest$1")){
    if(!m.desc.startsWith("(Lnet/minecraft/server/level/ServerPlayer;Lnet/bettercombat/network/Packets$C2S_AttackRequest;Lnet/bettercombat/api/AttackHand;"))throw new AssertionError(m.desc);
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new VarInsnNode(ALOAD,2));add.add(new MethodInsnNode(INVOKESTATIC,helper,"swing","(Lnet/minecraft/server/level/ServerPlayer;Lnet/bettercombat/api/AttackHand;)Z",false));add.add(new InsnNode(POP));m.instructions.insert(add);edits++;
   }
   if(mode.equals("cloud")&&m.name.equals("tick"))for(var i:m.instructions.toArray())if(i instanceof FieldInsnNode field&&field.getOpcode()==GETFIELD&&field.name.equals("impact_tick_interval")){
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"cloudInterval","(ILnet/spell_engine/entity/SpellCloud;)I",false));m.instructions.insert(i,add);edits++;
   }
  }
  if(edits!=1)throw new AssertionError(mode+" edits="+edits);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
