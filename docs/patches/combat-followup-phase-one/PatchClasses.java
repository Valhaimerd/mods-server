import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchClasses implements Opcodes {
 static String H="net/spell_engine/entity/ClassRework",L="Lnet/minecraft/world/entity/LivingEntity;",P="Lnet/minecraft/world/entity/player/Player;",E="Lnet/minecraft/world/entity/Entity;",S="Lnet/spell_engine/api/spell/Spell$Impact;",O="Lnet/minecraft/core/Holder;",C="Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;";
 static MethodInsnNode call(String name,String desc){return new MethodInsnNode(INVOKESTATIC,H,name,desc,false);}
 static InsnList loads(int... slots){var x=new InsnList();for(int s:slots)x.add(new VarInsnNode(ALOAD,s));return x;}
 static void body(MethodNode m,InsnList code){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(code);}
 public static void main(String[] args)throws Exception{
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);String mode=args[2];int edits=0;
  for(var m:n.methods){
   if(mode.equals("balance")&&m.name.equals("lethalityHeal")){var x=loads(0);x.add(new VarInsnNode(FLOAD,1));x.add(call("lethalityHeal","("+P+"F)V"));x.add(new InsnNode(RETURN));body(m,x);edits++;}
   if(mode.equals("follow")&&m.name.equals("warmup")){var x=loads(0,1,2);x.add(call("warmup",m.desc));x.add(new InsnNode(ARETURN));body(m,x);edits++;}
   if(mode.equals("movement")&&m.name.equals("tickMovement_ModifyInput")){
    var x=loads(0);x.add(new TypeInsnNode(CHECKCAST,"net/minecraft/world/entity/LivingEntity"));x.add(call("mobile","("+L+")Z"));var label=new LabelNode();x.add(new JumpInsnNode(IFEQ,label));x.add(new InsnNode(RETURN));x.add(label);x.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(x);edits++;
   }
   if((mode.equals("rollClient")&&m.name.equals("isRollAvailable"))||(mode.equals("rollServer")&&m.name.equals("handleRollPublish"))){
    var x=loads(mode.equals("rollClient")?1:2);x.add(call("rollBlocked","("+P+")Z"));var label=new LabelNode();x.add(new JumpInsnNode(IFEQ,label));if(mode.equals("rollClient"))x.add(new InsnNode(ICONST_0));x.add(new InsnNode(mode.equals("rollClient")?IRETURN:RETURN));x.add(label);x.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(x);edits++;
   }
   for(var i:m.instructions.toArray()){
    if(mode.equals("execution")&&m.name.equals("performSpell")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/internals/PriestRules")&&a.name.equals("deliver")){a.owner=H;edits++;}
    if((mode.equals("finalSkill")||mode.equals("finalRogue"))&&m.name.equals("register")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/api/effect/Effects")&&a.name.equals("register")){var x=loads(0);x.add(call(mode.equals("finalSkill")?"skill":"rogue","("+C+")V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("balance")&&m.name.equals("configure")&&i.getOpcode()==RETURN){var x=loads(0);x.add(call("skill","("+C+")V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("ultimate")&&m.name.equals("rogue")&&i.getOpcode()==RETURN){var x=loads(0);x.add(call("rogue","("+C+")V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("ultimate")&&m.name.equals("tick")&&i instanceof MethodInsnNode a&&a.name.equals("node")){a.owner=H;a.name="never";edits++;}
    if(mode.equals("rules")&&m.name.equals("after")&&i instanceof InsnNode&&i.getOpcode()==ICONST_5){m.instructions.set(i,new IntInsnNode(BIPUSH,10));edits++;}
    if(mode.equals("rules")&&m.name.equals("after")&&i.getOpcode()==IRETURN){var x=loads(1,2,3,4);x.add(call("after","(Z"+L+E+O+S+")Z"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("rules")&&m.name.equals("impact")&&i.getOpcode()==ARETURN){var x=loads(0,1);x.add(call("impact","("+S+L+O+")"+S));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("tierTwo")&&m.name.equals("onEffect")&&i instanceof LdcInsnNode a&&a.cst.equals(.06)){a.cst=0d;edits++;}
    if(mode.equals("tierTwo")&&m.name.equals("onEffect")&&i.getOpcode()==RETURN){var x=loads(0,1,2);x.add(new VarInsnNode(ILOAD,3));x.add(call("onEffect","("+L+"Lnet/minecraft/world/effect/MobEffectInstance;"+E+"Z)V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("tierTwo")&&m.name.equals("after")&&i instanceof MethodInsnNode a&&a.name.equals("cleanse")){var x=loads(1);x.add(call("chargeStarted","("+L+")V"));m.instructions.insert(i,x);edits++;}
    if(mode.equals("follow")&&m.name.equals("tick")&&i instanceof LdcInsnNode a&&a.cst.equals("minecraft:movement_speed")){a.cst="minecraft:luck";edits++;}
    if(mode.equals("melee")&&i instanceof MethodInsnNode a&&a.name.equals("getRangeCurved")){var x=loads(0,1);if(m.name.equals("performAttackAgainstTargets"))x.add(new MethodInsnNode(INVOKEVIRTUAL,"net/spell_engine/internals/delivery/melee/Melee$AttackContext","spellId","()Lnet/minecraft/resources/Identifier;",false));else if(!m.name.equals("convert"))throw new AssertionError("unknown range hook "+m.name);x.add(call("meleeRange","(F"+P+"Lnet/minecraft/resources/Identifier;)F"));m.instructions.insert(i,x);edits++;}
   }
  }
  int expected=switch(mode){case "balance"->3;case "rules","tierTwo"->4;case "ultimate","follow","melee"->2;default->1;};
  if(edits!=expected)throw new AssertionError(mode+" expected "+expected+" edits "+edits);System.out.println(mode+" edits="+edits);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
