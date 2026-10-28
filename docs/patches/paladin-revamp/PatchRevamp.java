import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class PatchRevamp implements Opcodes {
 static final String H="net/spell_engine/internals/PaladinRevamp";
 public static void main(String[] args)throws Exception{
  var n=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(n,0);int edits=0;String mode=args[2];
  for(var m:n.methods){
   if(mode.equals("dump")){for(var i:m.instructions)if(i instanceof MethodInsnNode call&&(call.owner.contains("Warrior")||call.owner.contains("PriestRules")||call.name.contains("fallImpact")))System.out.println(n.name+" "+m.name+" -> "+call.owner+"."+call.name+call.desc);continue;}
   for(var i:m.instructions.toArray()){
    if(i instanceof MethodInsnNode c){
     boolean replace=switch(mode){
      case "impact"->c.owner.equals("net/spell_engine/internals/PriestRules")&&c.name.equals("impact");
      case "triggers"->c.owner.equals("net/spell_engine/entity/WarriorRules")&&c.name.equals("evaluate");
      case "impaired"->c.owner.equals("net/spell_engine/entity/WarriorTierTwo")&&c.name.equals("impaired");
      case "protection"->c.owner.equals("net/spell_engine/api/effect/Protection")&&c.name.equals("register");
      case "projectile"->(c.owner.equals("net/spell_engine/internals/PriestRules")&&c.name.equals("projectile"))||(c.owner.equals("net/spell_engine/internals/impact/SpellImpacts")&&c.name.equals("fallImpact"));
      default->false;};
     if(replace){c.owner=H;if(mode.equals("protection"))c.name="ignoreOldProtection";edits++;}
     if(mode.equals("rollground")&&m.name.equals("tryRolling")&&c.name.equals("onGround")){
      var a=new InsnList();a.add(new VarInsnNode(ALOAD,0));a.add(new FieldInsnNode(GETFIELD,n.name,"player","Lnet/minecraft/client/player/LocalPlayer;"));a.add(new MethodInsnNode(INVOKESTATIC,H,"rollGround","(ZLnet/minecraft/world/entity/player/Player;)Z",false));m.instructions.insert(c,a);edits++;
     }
     if(mode.equals("launch")&&m.name.equals("fallProjectile")&&c.name.equals("addFreshEntity")){
      var add=new InsnList();add.add(new VarInsnNode(ALOAD,1));add.add(new VarInsnNode(ALOAD,4));add.add(new VarInsnNode(ALOAD,5));add.add(new MethodInsnNode(INVOKESTATIC,H,"launched","(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Lnet/minecraft/world/entity/Entity;",false));m.instructions.insertBefore(c,add);edits++;
     }
     if(mode.equals("launch")&&c.owner.equals("net/spell_engine/entity/WarriorRules")&&c.name.equals("launch")){
      // Keep the original launch hook, then attach the Oath snapshot to its result.
      var add=new InsnList();add.add(new VarInsnNode(ALOAD,1));add.add(new VarInsnNode(ALOAD,5));add.add(new VarInsnNode(ALOAD,6));
      // Descriptors/local slots differ between shoot and fall; derive argument positions below.
      Type[] types=Type.getArgumentTypes(m.desc);int slot=0,cast=-1,spell=-1,context=-1;
      for(var t:types){String d=t.getDescriptor();if(d.equals("Lnet/minecraft/world/entity/LivingEntity;"))cast=slot;if(d.equals("Lnet/minecraft/core/Holder;"))spell=slot;if(d.equals("Lnet/spell_engine/internals/SpellExecution$ImpactContext;"))context=slot;slot+=t.getSize();}
      if(cast<0||spell<0||context<0)throw new AssertionError(m.name+m.desc);add.clear();add.add(new VarInsnNode(ALOAD,cast));add.add(new VarInsnNode(ALOAD,spell));add.add(new VarInsnNode(ALOAD,context));add.add(new MethodInsnNode(INVOKESTATIC,H,"launched",c.desc,false));m.instructions.insert(c,add);edits++;
     }
    }
    if(i.getOpcode()==RETURN&&mode.equals("attachment")&&m.name.equals("<init>")){var a=new InsnList();a.add(new VarInsnNode(ALOAD,1));a.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/internals/EffectDetails","register","(Lnet/neoforged/bus/api/IEventBus;)V",false));m.instructions.insertBefore(i,a);edits++;}
    if(i.getOpcode()==IRETURN&&mode.equals("freedom")&&m.name.equals("freedom")){var a=new InsnList();a.add(new VarInsnNode(ALOAD,0));a.add(new MethodInsnNode(INVOKESTATIC,H,"protectedControl","(ZLnet/minecraft/world/entity/LivingEntity;)Z",false));m.instructions.insertBefore(i,a);edits++;}
    if(i.getOpcode()==IRETURN&&mode.equals("roll")&&m.name.equals("isRollAvailable")){var a=new InsnList();a.add(new VarInsnNode(ALOAD,1));a.add(new VarInsnNode(ALOAD,0));a.add(new FieldInsnNode(GETFIELD,n.name,"isEnabled","Z"));a.add(new VarInsnNode(ALOAD,0));a.add(new MethodInsnNode(INVOKEVIRTUAL,n.name,"isRolling","()Z",false));a.add(new VarInsnNode(ALOAD,0));a.add(new FieldInsnNode(GETFIELD,n.name,"availableRolls","I"));a.add(new MethodInsnNode(INVOKESTATIC,H,"rollAvailable","(ZLnet/minecraft/world/entity/player/Player;ZZI)Z",false));m.instructions.insertBefore(i,a);edits++;}
    if(i.getOpcode()==ARETURN&&mode.equals("attributes")&&m.name.equals("getTooltipLines")&&m.desc.startsWith("(Lnet/minecraft/world/effect/MobEffectInstance;")){var a=new InsnList();a.add(new VarInsnNode(ALOAD,0));a.add(new VarInsnNode(ALOAD,1));a.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/client/gui/EffectClientDetails","attributes","(Ljava/util/stream/Stream;Ljava/lang/Object;Lnet/minecraft/world/effect/MobEffectInstance;)Ljava/util/stream/Stream;",false));m.instructions.insertBefore(i,a);edits++;}
   }
   if(mode.equals("rollserver")&&m.name.equals("handleRollPublish")){
    var a=new InsnList();var ok=new LabelNode();a.add(new VarInsnNode(ALOAD,2));a.add(new MethodInsnNode(INVOKESTATIC,H,"rollAllowed","(Lnet/minecraft/server/level/ServerPlayer;)Z",false));a.add(new JumpInsnNode(IFNE,ok));a.add(new InsnNode(RETURN));a.add(ok);a.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(a);edits++;
   }
  }
  if(mode.equals("description")){
   var m=new MethodNode(ACC_PUBLIC,"getTooltipLines","(Ljava/lang/Object;I)Ljava/util/stream/Stream;",null,null);
   m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ILOAD,2));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(new VarInsnNode(ILOAD,2));m.instructions.add(new MethodInsnNode(INVOKESPECIAL,n.superName,"getTooltipLines",m.desc,false));m.instructions.add(new MethodInsnNode(INVOKESTATIC,"net/spell_engine/client/gui/EffectClientDetails","describe","(Ljava/lang/Object;ILjava/util/stream/Stream;)Ljava/util/stream/Stream;",false));m.instructions.add(new InsnNode(ARETURN));n.methods.add(m);edits++;
  }
  if(mode.equals("dump"))return;
  int expected=Integer.parseInt(args[3]);if(edits!=expected)throw new AssertionError(mode+": expected "+expected+", found "+edits);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);Files.write(Path.of(args[1]),w.toByteArray());
 }
}
