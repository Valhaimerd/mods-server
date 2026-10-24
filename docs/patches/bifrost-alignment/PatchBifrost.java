import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Build-only insertion guards. Native saves, registry IDs and beam code are retained. */
public final class PatchBifrost implements Opcodes {
    static final String BASE="com/emandev/bifrost/", GUARD=BASE+"AsgardianAccess";
    static InsnList gate(int local, String kind) {
        var code=new InsnList();var allowed=new LabelNode();
        code.add(new VarInsnNode(ALOAD,local));
        code.add(new MethodInsnNode(INVOKESTATIC,GUARD,"allow","(Lnet/minecraft/world/entity/Entity;)Z",false));
        code.add(new JumpInsnNode(IFNE,allowed));
        switch(kind) {
            case "void" -> code.add(new InsnNode(RETURN));
            case "bool" -> { code.add(new InsnNode(ICONST_0));code.add(new InsnNode(IRETURN)); }
            case "null" -> { code.add(new InsnNode(ACONST_NULL));code.add(new InsnNode(ARETURN)); }
            case "list" -> { code.add(new MethodInsnNode(INVOKESTATIC,"java/util/List","of","()Ljava/util/List;",true));code.add(new InsnNode(ARETURN)); }
            case "interaction" -> { code.add(new FieldInsnNode(GETSTATIC,"net/minecraft/world/InteractionResult","FAIL","Lnet/minecraft/world/InteractionResult$Fail;"));code.add(new InsnNode(ARETURN)); }
            default -> throw new AssertionError(kind);
        }
        code.add(allowed); return code;
    }
    public static void main(String[] args)throws Exception {
        Path dir=Path.of(args[0]);int count=0;
        for(String name:new String[]{"BifrostMod","BifrostFocusItem","BifrostMarkerBlock","BifrostMenuService","BifrostMarkerService","BifrostNetworking","BifrostTeleportManager","ModItems"}) {
            Path path=dir.resolve(BASE+name+".class");var reader=new ClassReader(Files.readAllBytes(path));var cls=new ClassNode();reader.accept(cls,0);int changes=0;
            for(var m:cls.methods) {
                int local=-1;String kind=null;
                if(name.equals("BifrostFocusItem")) {
                    if(m.name.equals("use")){local=2;kind="interaction";}
                    if(m.name.equals("useOn")) {
                        var code=new InsnList();var allowed=new LabelNode();
                        code.add(new VarInsnNode(ALOAD,1));code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/item/context/UseOnContext","getPlayer","()Lnet/minecraft/world/entity/player/Player;",false));
                        code.add(new MethodInsnNode(INVOKESTATIC,GUARD,"allow","(Lnet/minecraft/world/entity/Entity;)Z",false));code.add(new JumpInsnNode(IFNE,allowed));
                        code.add(new FieldInsnNode(GETSTATIC,"net/minecraft/world/InteractionResult","FAIL","Lnet/minecraft/world/InteractionResult$Fail;"));code.add(new InsnNode(ARETURN));code.add(allowed);m.instructions.insert(code);changes++;
                    }
                }
                if(name.equals("BifrostMarkerBlock")) {
                    if(m.name.equals("useWithoutItem")){local=4;kind="interaction";}
                    if(m.name.equals("setPlacedBy")){local=4;kind="void";}
                }
                if(name.equals("BifrostMenuService")) {
                    if(java.util.Set.of("open","openCommandMenu","beginRename").contains(m.name)){local=0;kind="void";}
                    if(java.util.Set.of("select","renamePending").contains(m.name)){local=0;kind="bool";}
                }
                if(name.equals("BifrostMarkerService")) {
                    if(java.util.Set.of("rename","get").contains(m.name)){local=0;kind="null";}
                    if(m.name.equals("list")){local=0;kind="list";}
                }
                if(name.equals("BifrostNetworking") && m.name.equals("openScreen")){local=0;kind="void";}
                if(name.equals("BifrostTeleportManager") && m.name.equals("begin")){local=0;kind="bool";}
                if(local>=0){m.instructions.insert(gate(local,kind));changes++;}
                for(var i:m.instructions.toArray()) {
                    if(name.equals("BifrostTeleportManager") && i instanceof MethodInsnNode call && call.name.equals("isAlive") && call.owner.equals("net/minecraft/server/level/ServerPlayer")) {
                        m.instructions.set(call,new MethodInsnNode(INVOKESTATIC,GUARD,"canUse","(Lnet/minecraft/world/entity/Entity;)Z",false));changes++;
                    }
                    if(name.equals("ModItems") && i instanceof LdcInsnNode ldc && ldc.cst instanceof String text) {
                        if(text.equals("Heimdall's Sword")){ldc.cst="Hofund";changes++;}
                        if(text.equals("Thor's Hammer")){ldc.cst="Mjolnir";changes++;}
                    }
                    if(name.equals("BifrostNetworking") && i instanceof LdcInsnNode ldc && ldc.cst.equals("1")){ldc.cst="asgardian-v1";changes++;}
                }
                if(name.equals("BifrostMod") && m.name.equals("<init>")) {
                    // Insert after Object.<init>, so no access occurs on an uninitialized this.
                    for(var i:m.instructions.toArray())if(i instanceof MethodInsnNode call && call.name.equals("<init>") && call.owner.equals("java/lang/Object")) {
                        m.instructions.insert(i,new MethodInsnNode(INVOKESTATIC,GUARD,"verifyIntegration","()V",false));changes++;break;
                    }
                }
            }
            if(changes==0)throw new AssertionError("No guards inserted into "+name);
            var out=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);cls.accept(out);Files.write(path,out.toByteArray());count+=changes;
            System.out.println(name+": "+changes+" changes");
        }
        if(count!=20)throw new AssertionError("Unexpected patch count "+count);
    }
}
