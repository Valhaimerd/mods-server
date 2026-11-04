import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchFinal implements Opcodes {
 static String H="net/spell_engine/internals/FinalRules",L="Lnet/minecraft/world/entity/LivingEntity;",O="Lnet/minecraft/core/Holder;",CFG="Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;";
 static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,owner,name,desc,false);}
 static InsnList loads(int... slots){var x=new InsnList();for(int slot:slots)x.add(new VarInsnNode(ALOAD,slot));return x;}
 static void delegate(MethodNode m,String owner,String method){var x=new InsnList();int slot=0;for(var arg:Type.getArgumentTypes(m.desc)){x.add(new VarInsnNode(arg.getOpcode(ILOAD),slot));slot+=arg.getSize();}x.add(call(owner,method,m.desc));m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(x);m.instructions.add(new InsnNode(Type.getReturnType(m.desc).getOpcode(IRETURN)));}
 static boolean intValue(AbstractInsnNode n,int v){return n instanceof IntInsnNode a&&a.operand==v||v>=-1&&v<=5&&n.getOpcode()==ICONST_0+v;}
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);String mode=args[2];int edits=0;
  for(var m:c.methods){
   if(mode.equals("priest")&&m.name.equals("ascension")){delegate(m,"net/spell_engine/internals/FinalPriest","ascension");edits++;continue;}
   if(mode.equals("boundary")&&m.name.equals("clipCircle")){delegate(m,"net/spell_engine/internals/FinalDome","clip");edits++;continue;}
   if(mode.equals("blocks")&&m.name.equals("incoming")){delegate(m,"net/spell_engine/internals/FinalDefenses","incoming");edits++;continue;}
   if(mode.equals("ultimate")&&m.name.equals("reinforced")){delegate(m,"net/spell_engine/internals/FinalPurelight","refresh");edits++;continue;}
   if(mode.equals("revamp")&&m.name.startsWith("lambda$init$")&&m.desc.endsWith("Lnet/minecraft/world/phys/Vec3;)Z")&&java.util.Arrays.stream(m.instructions.toArray()).anyMatch(n->n instanceof MethodInsnNode a&&a.name.equals("cleanse"))){delegate(m,H,"breakRestriction");edits++;continue;}
   boolean baseChanged=false;
   for(var n:m.instructions.toArray()){
    if(mode.equals("register")&&m.name.equals("register")&&n instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/api/effect/Effects")&&a.name.equals("register")){var x=loads(0);x.add(call(H,"configure","("+CFG+")V"));m.instructions.insertBefore(n,x);edits++;}
    if(mode.equals("register")&&m.name.equals("register")&&n.getOpcode()==RETURN){m.instructions.insertBefore(n,call(H,"registered","()V"));edits++;}
    if(mode.equals("revamp")&&m.name.equals("impact")&&n.getOpcode()==ARETURN){var x=loads(0,1);x.add(call(H,"impact","(Lnet/spell_engine/api/spell/Spell$Impact;"+L+O+")Lnet/spell_engine/api/spell/Spell$Impact;"));m.instructions.insertBefore(n,x);edits++;}
    if(mode.equals("revamp")&&m.name.equals("details")&&n.getOpcode()==ARETURN){var x=loads(0,1);x.add(call(H,"details","(Ljava/util/List;"+L+"Ljava/lang/String;)Ljava/util/List;"));m.instructions.insertBefore(n,x);edits++;}
    if(mode.equals("vanguard")&&n instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/internals/PaladinRevamp")&&a.name.equals("addControl")){a.owner=H;a.name="vanguardControl";edits++;}
    if(mode.equals("guardian")&&m.name.equals("bonus")){
     if(n instanceof LdcInsnNode a&&a.cst.equals(.02d)&&!baseChanged){a.cst=.05d;baseChanged=true;edits++;}
     if(intValue(n,4)){m.instructions.set(n,new InsnNode(ICONST_5));edits++;}
    }
    if(mode.equals("guardian")&&m.name.equals("clean")&&intValue(n,3)){m.instructions.set(n,new InsnNode(ICONST_4));edits++;}
    if(mode.equals("favor")&&m.name.equals("retaliate")&&intValue(n,20)){m.instructions.set(n,new IntInsnNode(BIPUSH,60));edits++;}
    if(mode.equals("fade")&&m.name.startsWith("lambda$")&&java.util.Arrays.stream(m.instructions.toArray()).anyMatch(a->a instanceof LdcInsnNode b&&b.cst.equals("skill_tree_rpgs:priest_fade"))&&intValue(n,20)){m.instructions.set(n,new IntInsnNode(BIPUSH,40));edits++;}
    if(mode.equals("ultimate")&&java.util.Set.of("startChannel","end").contains(m.name)&&n.getOpcode()==RETURN){var x=loads(0);x.add(call(H,m.name.equals("startChannel")?"opening":"endOpening","("+L+")V"));m.instructions.insertBefore(n,x);edits++;}
    if(mode.equals("seeker")&&java.util.Set.of("homing","beforeTick").contains(m.name)&&intValue(n,10)&&n.getNext() instanceof JumpInsnNode){m.instructions.set(n,new IntInsnNode(BIPUSH,8));edits++;}
    if(mode.equals("seeker")&&m.name.equals("launch")&&n.getOpcode()==ARETURN){m.instructions.insertBefore(n,call("net/spell_engine/internals/FinalArcher","launched","(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;"));edits++;}
   }
  }
  if(edits==0)throw new AssertionError(mode+" no hooks");var out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());System.out.println(mode+" hooks="+edits);
 }
}
