import java.nio.file.*;
import org.objectweb.asm.*;

public class PatchEngine implements Opcodes {
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        int[] edits = {0};
        reader.accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor output = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("cooldownToSet")) {
                    edits[0]++;
                    output.visitCode();
                    output.visitVarInsn(ALOAD, 0); output.visitVarInsn(ALOAD, 1); output.visitVarInsn(FLOAD, 2);
                    output.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/SecondTierRules", "cooldown", desc, false);
                    output.visitInsn(FRETURN); output.visitMaxs(0, 0); output.visitEnd();
                    return null;
                }
                if (!name.equals("performImpact")) return output;
                edits[0]++;
                return new MethodVisitor(ASM9, output) {
                    public void visitCode() {
                        super.visitCode();
                        for (int i = 0; i < 7; i++) mv.visitVarInsn(ALOAD, i);
                        mv.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/SecondTierRules", "delayFreeze", desc, false);
                        Label original = new Label(); mv.visitJumpInsn(IFEQ, original);
                        mv.visitInsn(ICONST_1); mv.visitInsn(IRETURN);
                        mv.visitLabel(original); mv.visitFrame(F_SAME, 0, null, 0, null);
                    }
                };
            }
        }, 0);
        if (edits[0] != 1) throw new AssertionError("Expected one method patch, got " + edits[0]);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
