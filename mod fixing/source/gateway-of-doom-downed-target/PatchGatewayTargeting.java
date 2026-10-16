import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PatchGatewayTargeting implements Opcodes {
    private static final String BASE = "de/mrstilu2k6/gateway_of_doom/entity/";
    private static final String TARGETING = BASE + "GatewayTargeting";
    private static final String PLAYER = "Lnet/minecraft/world/entity/player/Player;";

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        for (String name : new String[]{"GatewayForceTargetGoal", "GatewayAggressionGoal", "GatewayPortalEntity"}) {
            ClassNode node = new ClassNode();
            new ClassReader(Files.readAllBytes(input.resolve(name + ".class"))).accept(node, 0);
            int changes = 0;
            for (MethodNode method : node.methods) {
                if (name.equals("GatewayPortalEntity") && method.name.equals("findNearestPlayer")) {
                    replacePortalSearch(method); changes++;
                } else if (!name.equals("GatewayPortalEntity") && method.name.equals("nearestValidPlayer")) {
                    replaceMobSearch(method, name); changes++;
                } else if (name.equals("GatewayForceTargetGoal") && method.name.equals("isValidTarget")) {
                    replaceAliveCheck(method); changes++;
                } else if (name.equals("GatewayAggressionGoal") && method.name.equals("canContinueToUse")) {
                    replaceAliveCheck(method); changes++;
                } else if (!name.equals("GatewayPortalEntity") && method.name.equals("tick")) {
                    refreshMobTarget(method, name); changes++;
                }
            }
            int expected = name.equals("GatewayPortalEntity") ? 1 : 3;
            if (changes != expected) throw new IllegalStateException(name + ": " + changes + " changes");
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            Files.write(output.resolve(name + ".class"), writer.toByteArray());
        }
    }

    private static void replacePortalSearch(MethodNode method) {
        method.instructions.clear(); method.tryCatchBlocks.clear();
        method.instructions.add(new VarInsnNode(ALOAD, 1));
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new MethodInsnNode(INVOKESTATIC, TARGETING, "nearestActive", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/server/level/ServerPlayer;", false));
        method.instructions.add(new InsnNode(ARETURN));
    }

    private static void replaceMobSearch(MethodNode method, String owner) {
        method.instructions.clear(); method.tryCatchBlocks.clear();
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new FieldInsnNode(GETFIELD, BASE + owner, "mob", "Lnet/minecraft/world/entity/Mob;"));
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new FieldInsnNode(GETFIELD, BASE + owner, owner.equals("GatewayForceTargetGoal") ? "range" : "followRange", "D"));
        if (owner.equals("GatewayAggressionGoal")) {
            method.instructions.add(new VarInsnNode(ALOAD, 0));
            method.instructions.add(new FieldInsnNode(GETFIELD, BASE + owner, "daytimeLandOnly", "Z"));
            method.instructions.add(new MethodInsnNode(INVOKESTATIC, TARGETING, "nearestActive", "(Lnet/minecraft/world/entity/Mob;DZ)" + PLAYER, false));
        } else {
            method.instructions.add(new MethodInsnNode(INVOKESTATIC, TARGETING, "nearestActive", "(Lnet/minecraft/world/entity/Mob;D)" + PLAYER, false));
        }
        method.instructions.add(new InsnNode(ARETURN));
    }

    private static void refreshMobTarget(MethodNode method, String owner) {
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new FieldInsnNode(GETFIELD, BASE + owner, "mob", "Lnet/minecraft/world/entity/Mob;"));
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new FieldInsnNode(GETFIELD, BASE + owner, "target", PLAYER));
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new FieldInsnNode(GETFIELD, BASE + owner, owner.equals("GatewayForceTargetGoal") ? "range" : "followRange", "D"));
        if (owner.equals("GatewayAggressionGoal")) {
            prefix.add(new VarInsnNode(ALOAD, 0));
            prefix.add(new FieldInsnNode(GETFIELD, BASE + owner, "daytimeLandOnly", "Z"));
        } else {
            prefix.add(new InsnNode(ICONST_0));
        }
        prefix.add(new MethodInsnNode(INVOKESTATIC, TARGETING, "refreshTarget", "(Lnet/minecraft/world/entity/Mob;" + PLAYER + "DZ)" + PLAYER, false));
        prefix.add(new FieldInsnNode(PUTFIELD, BASE + owner, "target", PLAYER));
        method.instructions.insert(prefix);
    }

    private static void replaceAliveCheck(MethodNode method) {
        int changed = 0;
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode call && call.name.equals("isAlive") && call.desc.equals("()Z")) {
                method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, TARGETING, "isActive", "(" + PLAYER + ")Z", false));
                changed++;
            }
        }
        if (changed != 1) throw new IllegalStateException(method.name + ": " + changed + " alive checks");
    }
}
