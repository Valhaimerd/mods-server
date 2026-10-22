import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Change downed-entry health only; preserve healing, expiry and final-death paths. */
public final class PatchDownedHealth implements Opcodes {
    static AbstractInsnNode previous(AbstractInsnNode node) {
        do {node=node.getPrevious();} while(node!=null && node.getOpcode()<0);
        return node;
    }
    public static void main(String[] args) throws Exception {
        var path=Path.of(args[0]);var reader=new ClassReader(Files.readAllBytes(path));
        var cls=new ClassNode();reader.accept(cls,0);int count=0;
        for(var method:cls.methods) {
            for(var instruction:method.instructions.toArray()) {
                if(cls.name.endsWith("/PlayerReviveServer") && method.name.equals("startBleeding") && instruction.getOpcode()==RETURN) {
                    var code=new InsnList();code.add(new VarInsnNode(ALOAD,0));code.add(new InsnNode(DUP));
                    code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/entity/player/Player","getMaxHealth","()F",false));
                    code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/entity/player/Player","setHealth","(F)V",false));
                    method.instructions.insertBefore(instruction,code);count++;
                } else if(cls.name.endsWith("/ReviveEventServer") && method.name.equals("playerDied")
                        && instruction instanceof FieldInsnNode field && field.name.equals("bleedingHealth")) {
                    var config=previous(instruction);var root=previous(config);var conversion=instruction.getNext();
                    if(root.getOpcode()!=GETSTATIC || config.getOpcode()!=GETFIELD || conversion.getOpcode()!=I2F)throw new AssertionError("Unexpected health load");
                    var code=new InsnList();code.add(new VarInsnNode(ALOAD,2));
                    code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/entity/player/Player","getMaxHealth","()F",false));
                    method.instructions.insertBefore(root,code);
                    for(var n:new AbstractInsnNode[]{root,config,instruction,conversion})method.instructions.remove(n);
                    count++;
                } else if(cls.name.endsWith("/DownedPlayerManager") && (method.name.equals("enterDowned") || method.name.equals("restoreAfterLogin"))
                        && instruction instanceof MethodInsnNode call && call.name.equals("setHealth") && call.desc.equals("(F)V")) {
                    var constant=previous(instruction);if(constant.getOpcode()!=FCONST_1)throw new AssertionError("Unexpected downed health constant");
                    var code=new InsnList();code.add(new VarInsnNode(ALOAD,0));
                    code.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/server/level/ServerPlayer","getMaxHealth","()F",false));
                    method.instructions.insertBefore(constant,code);method.instructions.remove(constant);count++;
                }
            }
        }
        int expected=cls.name.endsWith("/DownedPlayerManager")?2:1;
        if(count!=expected)throw new AssertionError(cls.name+" changes "+count+" expected "+expected);
        var out=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        cls.accept(out);Files.write(path,out.toByteArray());
        System.out.println("Changed "+count+" downed health paths in "+cls.name);
    }
}
