import java.nio.file.*;
import org.objectweb.asm.*;

public class PatchThirdTier implements Opcodes {
    public static void main(String[] args) throws Exception {
        var reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        var writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        String mode = args[2]; int[] edits = {0};
        reader.accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                var output = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(ASM9, output) {
                    public void visitCode() {
                        super.visitCode();
                        if (mode.equals("effects") && name.equals("register")) {
                            edits[0]++;
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/ThirdTierRules", "configureEffects", "(Lnet/spell_engine/rpg_series/config/ConfigFile$Effects;)V", false);
                        }
                    }
                    public void visitInsn(int opcode) {
                        if (mode.equals("effects") && name.equals("register") && opcode == RETURN) {
                            edits[0]++;
                            mv.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/ThirdTierRules", "install", "()V", false);
                        }
                        super.visitInsn(opcode);
                    }
                    public void visitMethodInsn(int opcode, String owner, String method, String descriptor, boolean itf) {
                        if (mode.equals("cooldown") && owner.equals("net/spell_engine/internals/RevisionRules") && method.equals("cooldown")) {
                            owner = "net/spell_engine/internals/ThirdTierRules"; edits[0]++;
                        }
                        super.visitMethodInsn(opcode, owner, method, descriptor, itf);
                    }
                };
            }
        }, 0);
        int expected = mode.equals("effects") ? 2 : 1;
        if (edits[0] != expected) throw new AssertionError(mode + ": " + edits[0] + " edits; expected " + expected);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
