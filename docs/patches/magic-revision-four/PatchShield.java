import java.nio.file.*;
import org.objectweb.asm.*;

public class PatchShield implements Opcodes {
    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        int[] patches = {0};
        reader.accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                var method = super.visitMethod(access, name, desc, signature, exceptions);
                if (!name.equals("register")) return method;
                return new MethodVisitor(ASM9, method) {
                    public void visitInsn(int opcode) {
                        if (opcode == RETURN) {
                            patches[0]++;
                            mv.visitMethodInsn(INVOKESTATIC, "net/wizards/effect/ShieldRework", "install", "()V", false);
                        }
                        super.visitInsn(opcode);
                    }
                };
            }
        }, 0);
        if (patches[0] != 1) throw new AssertionError("Expected one register return, got " + patches[0]);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
