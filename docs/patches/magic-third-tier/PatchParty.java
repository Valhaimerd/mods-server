import java.nio.file.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;

/** Usage: PatchParty input-engine.jar output-classes-directory */
public final class PatchParty implements Opcodes {
    private static final String RULES = "net/spell_engine/internals/PartyRules";
    public static void main(String[] args) throws Exception {
        try (JarFile jar = new JarFile(args[0])) {
            for (String name : new String[]{"net/spell_engine/internals/target/EntityRelations",
                    "net/spell_engine/internals/impact/SpellImpacts"}) {
                byte[] original = jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes();
                byte[] patched = patch(original, name.endsWith("EntityRelations"));
                Path out = Path.of(args[1], name + ".class");
                Files.createDirectories(out.getParent());
                Files.write(out, patched);
            }
        }
    }

    private static byte[] patch(byte[] bytes, boolean relations) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        int[] counts = new int[6];
        reader.accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor output = super.visitMethod(access, name, desc, signature, exceptions);
                if (relations && name.equals("getRelation")) {
                    counts[0]++;
                    output.visitCode();
                    output.visitVarInsn(ALOAD, 0); output.visitVarInsn(ALOAD, 1);
                    output.visitMethodInsn(INVOKESTATIC, RULES, "relation", desc, false);
                    output.visitInsn(ARETURN); output.visitMaxs(0, 0); output.visitEnd();
                    return null;
                }
                if (relations || !name.equals("performImpact")) return output;
                counts[1]++;
                return new MethodVisitor(ASM9, output) {
                    public void visitCode() {
                        super.visitCode();
                        mv.visitVarInsn(ALOAD, 0); mv.visitVarInsn(ALOAD, 1); mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 4); mv.visitVarInsn(ALOAD, 5);
                        mv.visitMethodInsn(INVOKESTATIC, RULES, "impactAllowed",
                            "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/spell_engine/api/spell/Spell$Impact;Lnet/spell_engine/internals/SpellExecution$ImpactContext;)Z", false);
                        Label allowed = new Label(); mv.visitJumpInsn(IFNE, allowed);
                        mv.visitInsn(ICONST_0); mv.visitInsn(IRETURN);
                        mv.visitLabel(allowed); mv.visitFrame(F_SAME, 0, null, 0, null);
                    }
                    public void visitMethodInsn(int opcode, String owner, String method, String descriptor, boolean itf) {
                        if (owner.equals("net/spell_engine/internals/impact/SpellImpacts") && method.equals("underApplyLimit")) {
                            counts[2]++;
                            mv.visitVarInsn(ALOAD, 4);
                            super.visitMethodInsn(INVOKESTATIC, RULES, method,
                                descriptor.replace(")Z", "Lnet/spell_engine/api/spell/Spell$Impact;)Z"), false);
                        } else if (owner.equals("net/minecraft/world/entity/Entity") && method.equals("is")
                                && descriptor.equals("(Lnet/minecraft/tags/TagKey;)Z")) {
                            counts[3]++;
                            mv.visitVarInsn(ALOAD, 4);
                            super.visitMethodInsn(INVOKESTATIC, RULES, "bossGate",
                                "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/tags/TagKey;Lnet/spell_engine/api/spell/Spell$Impact;)Z", false);
                        } else if (owner.equals("net/minecraft/world/entity/LivingEntity") && method.equals("addEffect")
                                && descriptor.equals("(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")) {
                            counts[4]++;
                            super.visitMethodInsn(INVOKESTATIC, RULES, "addEffect",
                                "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", false);
                        } else if (owner.equals("net/minecraft/world/entity/Entity") && method.equals("igniteForSeconds")) {
                            counts[5]++;
                            super.visitMethodInsn(INVOKESTATIC, RULES, "ignite", "(Lnet/minecraft/world/entity/Entity;F)V", false);
                        } else super.visitMethodInsn(opcode, owner, method, descriptor, itf);
                    }
                };
            }
        }, 0);
        String expected = relations ? "[1, 0, 0, 0, 0, 0]" : "[0, 1, 1, 1, 1, 1]";
        if (!java.util.Arrays.toString(counts).equals(expected))
            throw new AssertionError("Unexpected party patch sites: " + java.util.Arrays.toString(counts));
        return writer.toByteArray();
    }
}
