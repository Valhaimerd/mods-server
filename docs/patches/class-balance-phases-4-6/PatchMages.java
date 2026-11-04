import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchMages implements Opcodes {
 static String H="net/spell_engine/internals/MageFollowup",L="Lnet/minecraft/world/entity/LivingEntity;",O="Lnet/minecraft/core/Holder;",E="Lnet/minecraft/world/entity/Entity;",I="Lnet/spell_engine/api/spell/Spell$Impact;",CTX="Lnet/spell_engine/internals/SpellExecution$ImpactContext;",CFG="Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;";
 static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,owner,name,desc,false);}
 static InsnList load(int... slots){var x=new InsnList();for(int s:slots)x.add(new VarInsnNode(ALOAD,s));return x;}
 static void body(MethodNode m,InsnList x,int ret){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(x);m.instructions.add(new InsnNode(ret));}
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);String mode=args[2];int edits=0;
  for(var m:c.methods){
   if(mode.equals("third")&&m.name.equals("blink")){var x=load(0,1,2,3,4,5);x.add(call(H,"blink",m.desc));body(m,x,IRETURN);edits++;continue;}
   if(mode.equals("third")&&m.name.equals("burst")){var x=load(0,1,2,3);x.add(call(H,"burst",m.desc));body(m,x,RETURN);edits++;continue;}
   if(mode.equals("passive")&&m.name.equals("marked")){var x=load(0,1);x.add(call(H,"marker",m.desc));body(m,x,IRETURN);edits++;continue;}
   for(var i:m.instructions.toArray()){
    if(mode.equals("register")&&m.name.equals("register")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/api/effect/Effects")&&a.name.equals("register")){var x=load(0);x.add(call(H,"configure","("+CFG+")V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("third")&&m.name.equals("configureEffects")&&i.getOpcode()==RETURN){var x=load(0);x.add(call(H,"wizardConfig","("+CFG+")V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("rules")&&m.name.equals("impact")&&i.getOpcode()==ARETURN){var x=load(0,1,3,4);x.add(call(H,"impact","("+I+L+O+CTX+E+")"+I));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("rules")&&m.name.equals("after")&&i.getOpcode()==IRETURN){var x=load(1,2,3,4);x.add(call(H,"after","(Z"+L+E+O+I+")Z"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("passive")&&m.name.equals("snapshot")&&i.getOpcode()==ARETURN){var x=load(0,1);x.add(call(H,"snapshot","("+CTX+L+O+")"+CTX));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("timing")&&m.name.equals("pulseOffset")&&i.getOpcode()==FRETURN){var x=load(0);x.add(call(H,"pulseOffset","(F"+O+")F"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("ward")&&m.name.equals("allowed")&&i.getOpcode()==IRETURN){var x=load(0,1);x.add(call(H,"wardAllowed","(Z"+O+"Lnet/spell_engine/internals/SpellTriggers$Event;)Z"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("summon")&&m.name.equals("prepare")&&i.getOpcode()==ARETURN){m.instructions.insertBefore(i,call("net/spell_engine/internals/MageSummons","prepared","(Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;)Lnet/spell_engine/api/spell/summon/SpellSummoned$Args;"));edits++;}
    if(mode.equals("summon")&&m.name.equals("tick")&&i.getOpcode()==RETURN){var x=load(0);x.add(call("net/spell_engine/internals/MageSummons","tick","(Lnet/spell_engine/entity/SummonedEntity;)V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("projectile")&&m.name.equals("beforeTick")&&i.getOpcode()==RETURN){var x=load(0);x.add(call("net/spell_engine/internals/MageProjectiles","beforeTick","(Lnet/spell_engine/entity/SpellProjectile;)V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("controller")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/internals/PaladinRevamp")&&a.name.equals("impaired")){a.owner=H;edits++;}
    if(mode.equals("charges")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/internals/cost/EchoCharges")&&java.util.Set.of("spend","tick","progress","duration").contains(a.name)){a.owner="net/spell_engine/internals/cost/JointCharges";edits++;}
    if(mode.equals("hud")&&m.name.equals("render")&&i.getOpcode()==RETURN){var x=load(0,1);x.add(call("net/spell_engine/client/gui/BlinkHud","render",m.desc));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("parameters")&&m.name.equals("isInstantCast")&&i.getOpcode()==IRETURN){var x=load(0,1);x.add(call(H,"instant","(Z"+O+L+")Z"));m.instructions.insertBefore(i,x);edits++;}
   }
  }
  if(edits==0)throw new AssertionError(mode+" hook absent");var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());System.out.println(mode+" hooks="+edits);
 }
}
