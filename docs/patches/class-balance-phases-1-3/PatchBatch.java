import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public class PatchBatch implements Opcodes {
 static String H="net/spell_engine/internals/BatchRules",L="Lnet/minecraft/world/entity/LivingEntity;",E="Lnet/minecraft/world/entity/Entity;",O="Lnet/minecraft/core/Holder;",I="Lnet/spell_engine/api/spell/Spell$Impact;";
 static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,owner,name,desc,false);}
 static InsnList loads(int... slots){var x=new InsnList();for(int s:slots)x.add(new VarInsnNode(ALOAD,s));return x;}
 static void constant(ClassNode c,String method,Object from,Object to,int wanted){int n=0;for(var m:c.methods)if(m.name.equals(method))for(var i:m.instructions.toArray()){
  Object v=i instanceof LdcInsnNode a?a.cst:i instanceof IntInsnNode a?a.operand:null;
  if(from.equals(v)){m.instructions.set(i,new LdcInsnNode(to));n++;}}
  if(n!=wanted)throw new AssertionError(method+" "+from+" matches="+n);}
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);String mode=args[2];int edits=0;
  if(mode.equals("revision")){constant(c,"chargedCoefficient",.08f,.1f,1);constant(c,"chargedCoefficient",.0012f,.0015f,1);constant(c,"chargedCoefficient",.12f,.15f,1);constant(c,"impact",1.3f,1.6f,1);constant(c,"impact",.4f,.5f,1);}
  else if(mode.equals("summon")){for(var m:c.methods)if(m.name.equals("summoned"))for(var i:m.instructions)if(i instanceof FieldInsnNode f&&f.name.equals("ATTACK_DAMAGE")){var p=i.getNext();while(p!=null&&!(p instanceof LdcInsnNode))p=p.getNext();if(!(p instanceof LdcInsnNode a)||!a.cst.equals(.4d))throw new AssertionError("summon");a.cst=.5d;edits++;}if(edits!=1)throw new AssertionError(edits);}
  else if(mode.equals("ultimate")){constant(c,"duration",60,40,1);constant(c,"duration",10,5,1);constant(c,"fatal",100,160,1);}
  else if(mode.equals("nullEffect")){for(var m:c.methods)if(m.name.equals("reduceMagicDamage")){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(new InsnNode(RETURN));edits++;}if(edits!=1)throw new AssertionError(edits);}
  else if(mode.equals("trap")){for(var m:c.methods)if(m.name.equals("area"))for(var i:m.instructions.toArray())if(i instanceof LdcInsnNode a&&a.cst.equals(2./3.)){if(i.getNext().getOpcode()!=DMUL)throw new AssertionError("activation");m.instructions.set(i.getNext(),call(H,"trapActivation","(DD)D"));edits++;}if(edits!=1)throw new AssertionError(edits);}
  else if(mode.equals("rules")){
   constant(c,"after",.2d,.35d,1);
   // Preserve the old per-stack modifier lifecycle; replace only its scaling term.
   for(var m:c.methods)if(m.name.equals("addEffect"))for(var i:m.instructions.toArray())if(i instanceof LdcInsnNode a&&a.cst.equals(-.05d)){
    var next=i.getNext();m.instructions.remove(i); // stack now contains the power operand after its DLOAD
    if(!(next instanceof VarInsnNode)||next.getOpcode()!=DLOAD)throw new AssertionError("fracture operand");
    var mul=next.getNext();if(mul.getOpcode()!=DMUL)throw new AssertionError("fracture multiply");m.instructions.set(mul,call(H,"fractureScaling","(D)D"));edits++;}
   for(var m:c.methods)if(m.name.equals("after"))for(var i:m.instructions.toArray())if(i.getOpcode()==IRETURN){var x=loads(1,2,3,4);x.add(call(H,"after","(Z"+L+E+O+I+")Z"));m.instructions.insertBefore(i,x);edits++;}
   if(edits!=2)throw new AssertionError(edits);
  }else if(mode.equals("lastStand")){
   // Existing branch is the fifth-pulse full heal. Make every earned pulse heal 20%.
   for(var m:c.methods)if(m.name.equals("after"))for(var i:m.instructions.toArray()){
    if(i instanceof InsnNode&&i.getOpcode()==ICONST_4){m.instructions.set(i,new InsnNode(ICONST_0));edits++;}
    if(i instanceof MethodInsnNode a&&a.name.equals("setHealth")){m.instructions.set(i,call(H,"lastStandPulse","("+L+"F)V"));edits++;}
   }if(edits!=2)throw new AssertionError(edits);
  }else{
   for(var m:c.methods)for(var i:m.instructions.toArray()){
    if(mode.equals("register")&&m.name.equals("register")&&i instanceof MethodInsnNode a&&a.owner.equals("net/spell_engine/api/effect/Effects")&&a.name.equals("register")){var x=loads(0);x.add(call(H,"skill","(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("food")&&m.name.equals("tick")&&i instanceof MethodInsnNode a&&a.owner.equals("net/minecraft/world/food/FoodData")&&a.name.equals("setFoodLevel")){m.instructions.set(i,call(H,"fullFood","(Lnet/minecraft/world/food/FoodData;I)V"));edits++;}
    if(mode.equals("retire")&&m.name.equals("removed")&&i.getOpcode()==IRETURN){var x=loads(0);x.add(call(H,"retired","(ZLjava/lang/String;)Z"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("retire")&&m.name.equals("apply")&&i.getOpcode()==RETURN){var x=loads(0);x.add(call(H,"retiredStats","(Ljava/lang/Object;)V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("gate")&&i instanceof MethodInsnNode a&&a.owner.equals("net/valhaimerd/orboblivioncompat/Utilities")&&a.name.equals("allowed")){a.owner="net/valhaimerd/orboblivioncompat/DefenseV2";edits++;}
    if(mode.equals("orbInit")&&m.name.equals("init")&&i.getOpcode()==RETURN){var x=new InsnList();x.add(call("net/valhaimerd/orboblivioncompat/DefenseV2","init","()V"));m.instructions.insertBefore(i,x);edits++;}
    if(mode.equals("timing")&&m.name.equals("pulseOffset")&&i.getOpcode()==FRETURN){var x=loads(0);x.add(call(H,"pulseOffset","(F"+O+")F"));m.instructions.insertBefore(i,x);edits++;}
   }
   if(edits!=((mode.equals("gate")||mode.equals("retire"))?2:1))throw new AssertionError(mode+" "+edits);
  }
  var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);Files.write(Path.of(args[1]),writer.toByteArray());System.out.println("Patched "+mode);
 }
}
