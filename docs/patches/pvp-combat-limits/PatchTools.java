import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
public class PatchTools implements Opcodes {
 public static void main(String[] args)throws Exception{
  ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;
  for(var m:c.methods){
   if(c.name.endsWith("ServerBlockPlacer")&&m.name.equals("checkAndNotifyAllowedToUseMod")||c.name.endsWith("UndoRedo")&&(m.name.equals("undo")||m.name.equals("redo"))){
    var label=new LabelNode();InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,1));i.add(new MethodInsnNode(INVOKESTATIC,"sophisticated/building/pack/BuildingGuard","blocked","(Lnet/minecraft/world/entity/player/Player;)Z",false));i.add(new JumpInsnNode(IFEQ,label));i.add(new InsnNode(ICONST_0));i.add(new InsnNode(IRETURN));i.add(label);i.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(i);edits++;
   }
   if(c.name.endsWith("ServerBlockPlacer")&&m.name.equals("undoBlockSet")){
    var label=new LabelNode();InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,1));i.add(new MethodInsnNode(INVOKESTATIC,"sophisticated/building/pack/BuildingGuard","blocked","(Lnet/minecraft/world/entity/player/Player;)Z",false));i.add(new JumpInsnNode(IFEQ,label));i.add(new VarInsnNode(ALOAD,2));i.add(new InsnNode(ARETURN));i.add(label);i.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(i);edits++;
   }
   if(c.name.endsWith("ServerBlockPlacer")&&m.name.equals("tick"))for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode call&&call.name.equals("isRemoved")){
    call.setOpcode(INVOKESTATIC);call.owner="sophisticated/building/pack/BuildingGuard";call.name="discardQueued";call.desc="(Lnet/minecraft/world/entity/Entity;)Z";call.itf=false;edits++;
   }
   if(c.name.endsWith("ChainActionLogic")&&m.name.equals("execute")){
    var label=new LabelNode();InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,0));i.add(new MethodInsnNode(INVOKESTATIC,"org/xiyu/onekeyminer/pack/MiningGuard","blocked","(Lorg/xiyu/onekeyminer/chain/ChainActionContext;)Lorg/xiyu/onekeyminer/chain/ChainActionResult;",false));i.add(new InsnNode(DUP));i.add(new JumpInsnNode(IFNULL,label));i.add(new InsnNode(ARETURN));i.add(label);i.add(new FrameNode(F_SAME1,0,null,1,new Object[]{"org/xiyu/onekeyminer/chain/ChainActionResult"}));i.add(new InsnNode(POP));m.instructions.insert(i);edits++;
   }
  }
  int expected=c.name.endsWith("ServerBlockPlacer")?3:c.name.endsWith("UndoRedo")?2:1;if(edits!=expected)throw new AssertionError(c.name+" edits "+edits+" expected "+expected);
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Files.write(Path.of(args[1]),w.toByteArray());System.out.println(c.name+" guarded="+edits);
 }
}
