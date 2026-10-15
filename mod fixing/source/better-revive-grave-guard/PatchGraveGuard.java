import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PatchGraveGuard implements Opcodes {
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);
        for (MethodNode method : node.methods) if (method.name.equals("setRemoved")) throw new IllegalStateException("Existing override");
        MethodNode method = new MethodNode(ACC_PUBLIC, "setRemoved", "()V", null, null);
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/world/level/block/entity/BlockEntity", "setRemoved", "()V", false));
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new MethodInsnNode(INVOKESTATIC, "de/mrstilu2k6/better_revive/util/GraveGuard", "onRemoved", "(Lde/mrstilu2k6/better_revive/block/GraveBlockEntity;)V", false));
        method.instructions.add(new InsnNode(RETURN));
        node.methods.add(method);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
