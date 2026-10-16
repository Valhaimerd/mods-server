import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Replace only target validity's alive predicate; preserve upstream target selection. */
public final class PatchDownedTargeting implements Opcodes {
    private static final String HELPER = "de/mrstilu2k6/gateway_of_doom/entity/DownedTargeting";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final Map<String, String> TARGETS = Map.ofEntries(
        Map.entry("entity/GatewayTargeting", "isValid"),
        Map.entry("entity/BossFocus", "valid"),
        Map.entry("entity/AzureBladeEntity", "valid"),
        Map.entry("bramblebacktest/RunenwurzEntity", "valid"),
        Map.entry("phoenixtest/VoidwingEntity", "valid"),
        Map.entry("entity/RogueEntity", "valid"),
        Map.entry("entity/KoboldEntity", "isEnemy"),
        Map.entry("entity/StoneGolemEntity", "valid"),
        Map.entry("entity/CinderWraithEntity", "valid")
    );

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        for (var entry : TARGETS.entrySet()) {
            String name = entry.getKey();
            ClassNode node = new ClassNode();
            new ClassReader(Files.readAllBytes(input.resolve(name + ".class"))).accept(node, 0);
            int count = 0;
            for (MethodNode method : node.methods) {
                if (!method.name.equals(entry.getValue()) || !method.desc.endsWith(")Z")) continue;
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode call && call.name.equals("isAlive") && call.desc.equals("()Z")) {
                        method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, HELPER, "isActive", "(" + LIVING + ")Z", false));
                        count++;
                    }
                }
            }
            if (count != 1) throw new IllegalStateException(name + ": expected one alive check, found " + count);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            Path target = output.resolve(name + ".class");
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
            System.out.println(name + ": guarded target validity");
        }
    }
}
