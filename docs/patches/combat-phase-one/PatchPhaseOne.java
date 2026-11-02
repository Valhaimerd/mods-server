import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Guarded edits to current class files retain all subsequent unrelated overlays. */
public class PatchPhaseOne implements Opcodes {
 static int edits;
 static void constants(ClassNode c,String method,Object from,Object to,int expected){int count=0;for(var m:c.methods)if(m.name.equals(method))for(var i:m.instructions)if(i instanceof LdcInsnNode l&&l.cst.equals(from)){l.cst=to;count++;}if(count!=expected)throw new AssertionError(c.name+"."+method+" "+from+": "+count+" != "+expected);edits+=count;}
 static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,owner,name,desc,false);}
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);String mode=args[2];
  if(mode.equals("revision")){
   constants(c,"chargedCoefficient",.1f,.08f,1);constants(c,"chargedCoefficient",.0015f,.0012f,1);constants(c,"chargedCoefficient",.15f,.12f,1);
   constants(c,"impact",1.6f,1.3f,1);constants(c,"impact",.5f,.4f,1);
   for(var m:c.methods)if(m.name.equals("cooldown")){
    var x=new InsnList();var skip=new LabelNode();x.add(new VarInsnNode(ALOAD,1));x.add(new LdcInsnNode("wizards:arcane_missile"));x.add(call("net/minecraft/resources/Identifier","parse","(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"));x.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/core/Holder","is","(Lnet/minecraft/resources/Identifier;)Z",true));x.add(new JumpInsnNode(IFEQ,skip));x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,1));x.add(call("net/spell_engine/internals/SpellParameters","getCooldownDuration","(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;)F"));x.add(new InsnNode(FRETURN));x.add(skip);m.instructions.insert(x);edits++;
   }
  }else if(mode.equals("summon")){
   int count=0;for(var m:c.methods)if(m.name.equals("summoned"))for(var i:m.instructions)if(i instanceof FieldInsnNode f&&f.name.equals("ATTACK_DAMAGE")){var p=i.getNext();while(p!=null&&!(p instanceof LdcInsnNode))p=p.getNext();if(!(p instanceof LdcInsnNode l)||!l.cst.equals(.5d))throw new AssertionError("attack coefficient");l.cst=.4d;count++;}if(count!=1)throw new AssertionError(count);edits++;
  }else if(mode.equals("cost")){
   for(var m:c.methods)if(m.name.equals("imposeCooldown")){var x=new InsnList();var skip=new LabelNode();x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,2));x.add(call("net/spell_engine/internals/cost/EchoCharges","spend","(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/Holder;)Z"));x.add(new JumpInsnNode(IFEQ,skip));x.add(new InsnNode(RETURN));x.add(skip);m.instructions.insert(x);edits++;}
  }else if(mode.equals("manager")){
   String owner=c.name,helper="net/spell_engine/internals/cost/EchoCharges";
   for(var m:c.methods){
    if(m.name.equals("<init>"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){m.instructions.insertBefore(i,call(helper,"init","()V"));edits++;}
    boolean progress=m.name.equals("getCooldownProgress")&&m.desc.startsWith("(Lnet/minecraft/core/Holder;");
    boolean duration=m.name.equals("getCooldownDuration")&&m.desc.startsWith("(Lnet/minecraft/core/Holder;");
    if(progress||duration){for(var i:m.instructions.toArray())if(i.getOpcode()==(progress?FRETURN:IRETURN)){
     var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,0));x.add(new FieldInsnNode(GETFIELD,owner,"owner","Lnet/minecraft/world/entity/Entity;"));x.add(new VarInsnNode(ALOAD,1));if(progress)x.add(new VarInsnNode(FLOAD,2));
     x.add(call(helper,progress?"progress":"duration",progress?"(FLnet/spell_engine/internals/cost/SpellCooldownManager;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;F)F":"(ILnet/spell_engine/internals/cost/SpellCooldownManager;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;)I"));m.instructions.insertBefore(i,x);edits++;
    }}
    if(m.name.equals("tickUpdate"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,0));x.add(new FieldInsnNode(GETFIELD,owner,"owner","Lnet/minecraft/world/entity/Entity;"));x.add(call(helper,"tick","(Lnet/spell_engine/internals/cost/SpellCooldownManager;Lnet/minecraft/world/entity/Entity;)V"));m.instructions.insertBefore(i,x);edits++;}
   }
  }else if(mode.equals("hud")){
   for(var m:c.methods)if(m.name.equals("render"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,3));x.add(call("net/spell_engine/client/gui/EchoHud","render","(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget$ViewModel;)V"));m.instructions.insertBefore(i,x);edits++;}
  }else if(mode.equals("sinful")){
   for(var m:c.methods){
    if(m.name.equals("incoming"))for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode a&&a.name.equals("igniteForSeconds")){m.instructions.insertBefore(i,new VarInsnNode(ALOAD,0));m.instructions.set(i,call("net/spell_engine/internals/SinfulDamage","start","(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/entity/LivingEntity;)V"));edits++;}
    if(m.name.equals("tick")){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(call("net/spell_engine/internals/SinfulDamage","tick","(Lnet/minecraft/world/entity/LivingEntity;)V"));m.instructions.insert(x);edits++;}
    if(m.name.equals("details"))for(var i:m.instructions.toArray())if(i.getOpcode()==ARETURN){var x=new InsnList();x.add(new VarInsnNode(ALOAD,0));x.add(new VarInsnNode(ALOAD,1));x.add(call("net/spell_engine/internals/SinfulDamage","details","(Ljava/util/List;Lnet/minecraft/world/entity/LivingEntity;Ljava/lang/String;)Ljava/util/List;"));m.instructions.insertBefore(i,x);edits++;}
    for(var i:m.instructions)if(i instanceof LdcInsnNode l&&l.cst.equals("The next hostile hit marks and burns its attacker.")){l.cst="The next hostile hit marks its attacker and drains 1 HP every 0.5s for 4s.";edits++;}
   }
  }else if(mode.equals("effects")){
   for(var m:c.methods)if(m.name.equals("<clinit>"))for(var i:m.instructions.toArray())if(i.getOpcode()==RETURN){var x=new InsnList();x.add(new FieldInsnNode(GETSTATIC,c.name,"entries","Ljava/util/List;"));x.add(call("net/spell_engine/internals/SinfulDamage","registerEffects","(Ljava/util/List;)V"));m.instructions.insertBefore(i,x);edits++;}
  }else throw new IllegalArgumentException(mode);
  if(edits==0)throw new AssertionError("No edits "+mode);
  var out=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS){@Override protected String getCommonSuperClass(String a,String b){try{return super.getCommonSuperClass(a,b);}catch(Exception e){return "java/lang/Object";}}};c.accept(out);Files.write(Path.of(args[1]),out.toByteArray());System.out.println(mode+": "+edits+" guarded edits");
 }
}
