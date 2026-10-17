import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Preserve the original mixin and annotations; change only its appended-offer range. */
public class PatchTrades implements Opcodes {
    public static void main(String[] args) throws Exception {
        var node = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node, 0);
        int changes = 0;
        for (var method : node.methods) {
            if (!method.name.equals("trimTradesWithoutDuplicates")) continue;
            int beforeSize = method.maxLocals++;
            var capture = new InsnList();
            capture.add(new VarInsnNode(ALOAD, 2));
            capture.add(new MethodInsnNode(INVOKEVIRTUAL,
                "net/minecraft/world/item/trading/MerchantOffers", "size", "()I", false));
            capture.add(new VarInsnNode(ISTORE, beforeSize));
            method.instructions.insert(capture);
            for (var instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != ISUB) continue;
                var count = instruction.getPrevious();
                var size = count.getPrevious();
                var list = size.getPrevious();
                if (!(count instanceof VarInsnNode v && v.getOpcode() == ILOAD && v.var == 4) ||
                    !(size instanceof MethodInsnNode m && m.name.equals("size")) ||
                    !(list instanceof VarInsnNode l && l.getOpcode() == ALOAD && l.var == 2))
                    throw new IllegalStateException("Unexpected trade-range bytecode");
                method.instructions.insertBefore(list, new VarInsnNode(ILOAD, beforeSize));
                method.instructions.remove(list);
                method.instructions.remove(size);
                method.instructions.remove(count);
                method.instructions.remove(instruction);
                changes++;
            }
        }
        if (changes != 1) throw new IllegalStateException("Expected exactly one trade-range replacement: " + changes);
        // The added local is initialized before every branch and remains an integer throughout.
        // Recompute frames without resolving any Minecraft classes.
        var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override protected String getCommonSuperClass(String a, String b) { return "java/lang/Object"; }
        };
        node.accept(writer);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
