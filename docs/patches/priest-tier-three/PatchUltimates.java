import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchUltimates implements Opcodes {
 public static void main(String[] args)throws Exception{
  var node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);String helper="net/spell_engine/internals/PriestUltimates";int edits=0;
  for(var m:node.methods){
   if(args[2].equals("priest")&&m.name.equals("configure")){var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"configure",m.desc,false));m.instructions.insert(add);edits++;}
   if(args[2].equals("summon")&&m.name.equals("tick")){var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new MethodInsnNode(INVOKESTATIC,helper,"wellTick","(Lnet/spell_engine/entity/SummonedEntity;)V",false));m.instructions.insert(add);edits++;}
   if(args[2].equals("summon")&&m.name.equals("onSummonedBySpell"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new VarInsnNode(ALOAD,1));add.add(new MethodInsnNode(INVOKESTATIC,helper,"wellSummoned","(Lnet/spell_engine/entity/SummonedEntity;Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;)V",false));m.instructions.insertBefore(i,add);edits++;}
   if(args[2].equals("barrier")&&m.name.equals("hurtServer")){var add=new InsnList();add.add(new VarInsnNode(ALOAD,0));add.add(new VarInsnNode(ALOAD,2));add.add(new MethodInsnNode(INVOKESTATIC,helper,"barrierHit","(Lnet/paladins/entity/BarrierEntity;Lnet/minecraft/world/damagesource/DamageSource;)V",false));m.instructions.insert(add);edits++;}
   if(args[2].equals("barrier")&&m.name.equals("isProtected")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new MethodInsnNode(INVOKESTATIC,helper,"barrierAlly","(Lnet/paladins/entity/BarrierEntity;Lnet/minecraft/world/entity/Entity;)Z",false));m.instructions.add(new InsnNode(IRETURN));edits++;
   }
   if(args[2].equals("barrier")&&m.name.equals("canBeHitByProjectile")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new InsnNode(ICONST_0));m.instructions.add(new InsnNode(IRETURN));edits++;
   }
   for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode call){
    if(args[2].equals("launcher")&&m.name.equals("shootProjectile")&&call.name.equals("effectiveCaster")&&call.desc.equals("(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/LivingEntity;")){
     var add=new InsnList();add.add(new VarInsnNode(ALOAD,1));add.add(new VarInsnNode(ALOAD,3));m.instructions.insertBefore(i,add);call.setOpcode(INVOKESTATIC);call.owner=helper;call.name="launchCaster";call.desc="(Lnet/spell_engine/internals/SpellExecution$ImpactContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/LivingEntity;";edits++;
    }
    if(args[2].equals("priest")&&m.name.equals("projectile")&&call.owner.equals("net/spell_engine/internals/impact/SpellImpacts")&&call.name.equals("projectileImpact")){call.owner=helper;call.name="moteProjectile";edits++;}
    if(args[2].equals("barrier")&&m.name.equals("tick")&&call.owner.equals("net/spell_engine/api/entity/LivingEntityImmunity")&&call.name.equals("apply")){System.out.println("immunity descriptor "+call.desc);call.owner=helper;call.name="noImmunityLease";edits++;}
    if(args[2].equals("barrier")&&m.name.equals("tick")&&call.name.equals("getEntities")&&call.desc.equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;")){call.setOpcode(INVOKESTATIC);call.owner=helper;call.name="barrierEntities";call.desc="(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;";edits++;}
   }
  }
  if(args[2].equals("summon")){
   if(node.methods.stream().anyMatch(m->m.name.equals("getTarget")))throw new AssertionError("Target override already exists");
   var m=new MethodNode(ACC_PUBLIC,"getTarget","()Lnet/minecraft/world/entity/LivingEntity;",null,null);
   m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new MethodInsnNode(INVOKESPECIAL,node.superName,"getTarget",m.desc,false));m.instructions.add(new MethodInsnNode(INVOKESTATIC,helper,"supportTarget","(Lnet/spell_engine/entity/SummonedEntity;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/LivingEntity;",false));m.instructions.add(new InsnNode(ARETURN));node.methods.add(m);
  }
  int expected=args[2].equals("barrier")?5:args[2].equals("launcher")?1:2;if(edits!=expected)throw new AssertionError(args[2]+" edits "+edits);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
