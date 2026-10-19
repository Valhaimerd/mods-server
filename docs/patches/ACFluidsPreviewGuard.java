import java.nio.file.*;
import org.objectweb.asm.*;

/** Alex's Caves Continued 1.0.11 / 1.1.2: preview levels have no chunk source. */
public final class ACFluidsPreviewGuard {
    public static void main(String[] args) throws Exception {
        var reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        var writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        int[] patched = {0};
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public MethodVisitor visitMethod(int access, String name, String desc,
                                                       String signature, String[] exceptions) {
                var method = super.visitMethod(access, name, desc, signature, exceptions);
                boolean height = name.equals("fluidHeight") && desc.equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/tags/TagKey;Lnet/minecraft/tags/TagKey;)D");
                boolean eye = name.equals("isEyeInFluid") && desc.equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/tags/TagKey;)Z");
                if (!height && !eye) return method;
                patched[0]++;
                return new MethodVisitor(Opcodes.ASM9, method) {
                    @Override public void visitCode() {
                        super.visitCode();
                        var level = new Label();
                        var chunks = new Label();
                        super.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", "level", "()Lnet/minecraft/world/level/Level;", false);
                        super.visitInsn(Opcodes.DUP);
                        super.visitJumpInsn(Opcodes.IFNONNULL, level);
                        super.visitInsn(Opcodes.POP);
                        empty();
                        super.visitLabel(level);
                        super.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"net/minecraft/world/level/Level"});
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/level/Level", "getChunkSource", "()Lnet/minecraft/world/level/chunk/ChunkSource;", false);
                        super.visitJumpInsn(Opcodes.IFNONNULL, chunks);
                        empty();
                        super.visitLabel(chunks);
                        super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    }
                    private void empty() {
                        super.visitInsn(height ? Opcodes.DCONST_0 : Opcodes.ICONST_0);
                        super.visitInsn(height ? Opcodes.DRETURN : Opcodes.IRETURN);
                    }
                };
            }
        }, 0);
        if (patched[0] != 2) throw new IllegalStateException("Expected two fluid helpers, got " + patched[0]);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
