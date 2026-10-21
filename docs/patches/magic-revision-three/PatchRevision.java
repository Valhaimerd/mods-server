import java.nio.file.*;
import org.objectweb.asm.*;

public class PatchRevision implements Opcodes {
    public static void main(String[] args) throws Exception {
        ClassReader reader = new ClassReader(Files.readAllBytes(Path.of(args[0])));
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        String mode = args[2]; int[] edits = {0};
        reader.accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor output = super.visitMethod(access, name, desc, signature, exceptions);
                if (mode.equals("cooldown") && name.equals("cooldownToSet")) {
                    edits[0]++;
                    output.visitCode();
                    output.visitVarInsn(ALOAD, 0); output.visitVarInsn(ALOAD, 1); output.visitVarInsn(FLOAD, 2);
                    output.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/RevisionRules", "cooldown", desc, false);
                    output.visitInsn(FRETURN); output.visitMaxs(0, 0); output.visitEnd(); return null;
                }
                if (mode.equals("beam") && name.equals("getBeam")) {
                    edits[0]++;
                    output.visitCode(); output.visitVarInsn(ALOAD, 0);
                    output.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/RevisionRules", "beam", "(Lnet/spell_engine/internals/casting/SpellCaster$Entity;)Lnet/spell_engine/api/spell/Spell$Target$Beam;", false);
                    output.visitInsn(ARETURN); output.visitMaxs(0, 0); output.visitEnd(); return null;
                }
                return new MethodVisitor(ASM9, output) {
                    public void visitCode() {
                        super.visitCode();
                        if (mode.equals("impact") && name.equals("performImpact")) {
                            edits[0]++;
                            for (int i : new int[]{1, 3, 4, 5}) mv.visitVarInsn(ALOAD, i);
                            mv.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/RevisionRules", "impact", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/core/Holder;Lnet/spell_engine/api/spell/Spell$Impact;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Lnet/spell_engine/api/spell/Spell$Impact;", false);
                            mv.visitVarInsn(ASTORE, 4);
                        }
                    }
                    public void visitFieldInsn(int opcode, String owner, String field, String descriptor) {
                        super.visitFieldInsn(opcode, owner, field, descriptor);
                        if (mode.equals("duration") && (name.equals("getCastDuration") || name.equals("getCastTimeDetails"))
                                && opcode == GETFIELD && owner.equals("net/spell_engine/api/spell/Spell$Active$Cast") && field.equals("duration")) {
                            edits[0]++;
                            mv.visitVarInsn(ALOAD, 0); mv.visitVarInsn(ALOAD, 1);
                            mv.visitMethodInsn(INVOKESTATIC, "net/spell_engine/internals/RevisionRules", "duration", "(FLnet/minecraft/world/entity/LivingEntity;Lnet/spell_engine/api/spell/Spell;)F", false);
                        }
                    }
                    public void visitLdcInsn(Object value) {
                        if (mode.equals("exposure") && value instanceof Float f && f == .02f) {
                            edits[0]++; super.visitLdcInsn(.04f);
                        } else super.visitLdcInsn(value);
                    }
                };
            }
        }, 0);
        int expected = mode.equals("duration") ? 2 : 1;
        if (edits[0] != expected) throw new AssertionError(mode + ": expected " + expected + " edits, got " + edits[0]);
        Files.write(Path.of(args[1]), writer.toByteArray());
    }
}
