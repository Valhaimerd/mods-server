import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchTuning implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);int edits=0;String helper="net/spell_engine/internals/PriestTuning",mode=args[2];
  for(var m:node.methods){
   if(mode.equals("rules")&&m.name.equals("impact"))for(var i:m.instructions.toArray())if(i.getOpcode()==ARETURN){
    var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new VarInsnNode(ALOAD,1));add.add(new MethodInsnNode(INVOKESTATIC,helper,"adjust","(Lnet/spell_engine/api/spell/Spell$Impact;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)Lnet/spell_engine/api/spell/Spell$Impact;",false));m.instructions.insertBefore(i,add);edits++;
   }
   if((mode.equals("rules")&&m.name.equals("projectile"))||(mode.equals("well")&&m.name.equals("select"))){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    for(int i=0;i<(mode.equals("rules")?5:2);i++)m.instructions.add(new VarInsnNode(ALOAD,i));
    m.instructions.add(new MethodInsnNode(INVOKESTATIC,helper,m.name,m.desc,false));m.instructions.add(new InsnNode(mode.equals("rules")?IRETURN:ARETURN));edits++;
   }
   for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode call){
    if(mode.equals("cooldown")&&call.owner.equals("net/spell_engine/internals/PriestRules")&&call.name.equals("cooldown")){call.owner=helper;edits++;}
    if(mode.equals("delivery")&&call.owner.equals("net/spell_engine/internals/delivery/ProjectileLauncher")&&call.name.equals("shootProjectile")){call.owner=helper;call.name="shoot";edits++;}
   }
  }
  int expected=mode.equals("rules")?5:mode.equals("delivery")?2:1;
  if(edits!=expected)throw new AssertionError(mode+" edits="+edits+" expected="+expected);
  var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);Files.write(Path.of(args[1]),out.toByteArray());
 }
}
