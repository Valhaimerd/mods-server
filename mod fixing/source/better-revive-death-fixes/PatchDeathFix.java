import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class PatchDeathFix implements Opcodes {
    private static final String BASE = "de/mrstilu2k6/better_revive/";
    private static final String FIX = BASE + "util/DeathFix";
    private static final String PLAYER = "Lnet/minecraft/server/level/ServerPlayer;";

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        for (String name : new String[]{"event/ModEvents", "util/CompassHelper", "util/DownedPlayerManager", "util/GraveRegistry", "block/GraveBlockEntity"}) {
            ClassNode node = new ClassNode();
            new ClassReader(Files.readAllBytes(input.resolve(name + ".class"))).accept(node, 0);
            int changes = name.endsWith("ModEvents") ? patchEvents(node) : name.endsWith("CompassHelper") ? patchCompass(node) : name.endsWith("GraveRegistry") ? patchRegistry(node) : name.endsWith("GraveBlockEntity") ? patchDisplay(node) : patchDowned(node);
            int expected = name.endsWith("ModEvents") ? 7 : name.endsWith("CompassHelper") ? 6 : 1;
            if (changes != expected) throw new IllegalStateException(name + ": " + changes + " changes; expected " + expected);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            Path target = output.resolve(BASE + name + ".class");
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
        }
    }

    private static int patchEvents(ClassNode node) {
        int changes = 0;
        for (MethodNode m : node.methods) {
            if (m.name.equals("onLivingDeath")) {
                for (AbstractInsnNode insn : m.instructions) {
                    if (insn instanceof MethodInsnNode call && call.owner.equals(BASE + "util/DownedPlayerManager") && call.name.equals("enterDowned")) {
                        InsnList extra = new InsnList();
                        extra.add(new VarInsnNode(ALOAD, 1));
                        extra.add(new MethodInsnNode(INVOKESTATIC, FIX, "shouldCompletePlayerReviveDeath", "(" + PLAYER + ")Z", false));
                        LabelNode continueDowned = new LabelNode();
                        extra.add(new JumpInsnNode(IFEQ, continueDowned));
                        extra.add(new InsnNode(RETURN));
                        extra.add(continueDowned);
                        extra.add(new FrameNode(F_SAME, 0, null, 0, null));
                        // PlayerRevive's give-up invokes die() at zero health. A nested
                        // hurtServer() cannot finish that death, so let the original event proceed.
                        m.instructions.insertBefore(insn.getPrevious(), extra);
                        // Remove the old post-enter hook from the previous repack.
                        for (AbstractInsnNode next = insn.getNext(); next != null; next = next.getNext()) {
                            if (next instanceof MethodInsnNode old && old.owner.equals(FIX) && old.name.equals("afterEnterDowned")) {
                                m.instructions.remove(next.getPrevious());
                                m.instructions.remove(next);
                                break;
                            }
                        }
                        changes++;
                        break;
                    }
                }
            } else if (m.name.equals("onPlayerRespawn")) {
                m.instructions.clear(); m.tryCatchBlocks.clear();
                m.instructions.add(new VarInsnNode(ALOAD, 0));
                m.instructions.add(new MethodInsnNode(INVOKESTATIC, FIX, "onRespawn", "(Lnet/neoforged/neoforge/event/entity/player/PlayerEvent$PlayerRespawnEvent;)V", false));
                m.instructions.add(new InsnNode(RETURN));
                changes++;
            } else if (m.name.equals("onServerTick")) {
                InsnList extra = new InsnList();
                extra.add(new VarInsnNode(ALOAD, 0));
                extra.add(new MethodInsnNode(INVOKEVIRTUAL, "net/neoforged/neoforge/event/tick/ServerTickEvent$Post", "getServer", "()Lnet/minecraft/server/MinecraftServer;", false));
                extra.add(new MethodInsnNode(INVOKESTATIC, FIX, "onServerTick", "(Lnet/minecraft/server/MinecraftServer;)V", false));
                m.instructions.insert(extra);
                changes++;
            } else if (m.name.equals("findGravePosition") && m.desc.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;")) {
                m.instructions.clear(); m.tryCatchBlocks.clear();
                m.instructions.add(new VarInsnNode(ALOAD, 0));
                m.instructions.add(new VarInsnNode(ALOAD, 1));
                m.instructions.add(new MethodInsnNode(INVOKESTATIC, FIX, "findGravePosition", m.desc, false));
                m.instructions.add(new InsnNode(ARETURN));
                changes++;
            } else if (m.name.equals("isConfiguredFinalDeath")) {
                int listContainsCalls = 0;
                for (AbstractInsnNode insn : m.instructions) {
                    if (insn instanceof MethodInsnNode call && call.owner.equals("java/util/List") && call.name.equals("contains")) {
                        if (++listContainsCalls == 2) {
                            m.instructions.set(call, new MethodInsnNode(INVOKESTATIC, FIX, "isFinalDamageType", "(Ljava/util/List;Ljava/lang/String;)Z", false));
                            changes++;
                            break;
                        }
                    }
                }
                if (listContainsCalls != 2) throw new IllegalStateException("Expected dimension and damage type checks");
            }
            // The upstream second-death path makes a new grave temporary whenever
            // another active grave exists. Normal respawn converts that temporary
            // grave to a collection crate, which expires immediately with this
            // pack's zero-second collection duration. Every death grave needs its
            // own ordinary registration and deadline instead.
            for (AbstractInsnNode insn : m.instructions.toArray()) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(BASE + "util/GraveRegistry")
                        && call.name.equals("hasActiveGraves") && call.desc.equals("(Ljava/util/UUID;)Z")) {
                    InsnList replacement = new InsnList();
                    replacement.add(new InsnNode(POP));
                    replacement.add(new InsnNode(ICONST_0));
                    m.instructions.insertBefore(call, replacement);
                    m.instructions.remove(call);
                    changes++;
                }
            }
        }
        return changes;
    }

    private static int patchCompass(ClassNode node) {
        int changes = 0;
        for (MethodNode m : node.methods) {
            if (m.name.equals("enforceCompassSlot")) {
                m.instructions.clear(); m.tryCatchBlocks.clear();
                m.instructions.add(new VarInsnNode(ALOAD, 0));
                m.instructions.add(new MethodInsnNode(INVOKESTATIC, FIX, "enforceCompassSlots", "(" + PLAYER + ")V", false));
                m.instructions.add(new InsnNode(RETURN));
                changes++;
            }
            if (m.name.equals("giveOrDropCompass")) {
                for (AbstractInsnNode insn : m.instructions.toArray()) {
                    if (!(insn instanceof MethodInsnNode call) || !call.owner.equals("net/minecraft/world/entity/player/Inventory")) continue;
                    if (call.name.equals("add")) {
                        m.instructions.set(call, new MethodInsnNode(INVOKESTATIC, FIX, "placeDisplacedHotbarItem",
                                "(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)Z", false));
                        changes++;
                        continue;
                    }
                    if (!(call.name.equals("getItem") || call.name.equals("setItem"))) continue;
                    AbstractInsnNode slot = call.getPrevious();
                    while (slot != null && slot.getOpcode() != ICONST_0) slot = slot.getPrevious();
                    if (slot == null || slot.getPrevious().getOpcode() != ALOAD) throw new IllegalStateException("Compass slot pattern changed");
                    InsnList replacement = new InsnList();
                    replacement.add(new InsnNode(DUP));
                    replacement.add(new MethodInsnNode(INVOKESTATIC, FIX, "compassSlot", "(Lnet/minecraft/world/entity/player/Inventory;)I", false));
                    m.instructions.insertBefore(slot, replacement);
                    m.instructions.remove(slot);
                    changes++;
                }
            }
            if (!m.name.equals("removeStaleCompasses") && !m.name.equals("lambda$removeStaleCompasses$0")) continue;
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(BASE + "util/GraveRegistry") && call.name.equals("isRegistered")) {
                    m.instructions.set(call, new MethodInsnNode(INVOKESTATIC, FIX, "isCompassValid", "(Ljava/util/UUID;)Z", false));
                    changes++;
                }
            }
        }
        return changes;
    }

    private static int patchDowned(ClassNode node) {
        MethodNode m = new MethodNode(ACC_PUBLIC | ACC_STATIC, "clearAfterRealDeath", "(" + PLAYER + ")V", null, null);
        m.instructions.add(new VarInsnNode(ALOAD, 0));
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", "getUUID", "()Ljava/util/UUID;", false));
        m.instructions.add(new VarInsnNode(ASTORE, 1));
        m.instructions.add(new FieldInsnNode(GETSTATIC, BASE + "util/DownedPlayerManager", "downed", "Ljava/util/Map;"));
        m.instructions.add(new VarInsnNode(ALOAD, 1));
        m.instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "remove", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        m.instructions.add(new InsnNode(POP));
        m.instructions.add(new FieldInsnNode(GETSTATIC, BASE + "util/DownedPlayerManager", "finalizingDeath", "Ljava/util/Set;"));
        m.instructions.add(new VarInsnNode(ALOAD, 1));
        m.instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "remove", "(Ljava/lang/Object;)Z", true));
        m.instructions.add(new InsnNode(POP));
        m.instructions.add(new FieldInsnNode(GETSTATIC, BASE + "util/DownedPlayerManager", "reviveOffers", "L" + BASE + "util/ReviveOfferTracker;"));
        m.instructions.add(new VarInsnNode(ALOAD, 1));
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, BASE + "util/ReviveOfferTracker", "removePlayer", "(Ljava/util/UUID;)V", false));
        m.instructions.add(new VarInsnNode(ALOAD, 0));
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/server/level/ServerPlayer", "level", "()Lnet/minecraft/server/level/ServerLevel;", false));
        m.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/server/level/ServerLevel", "getServer", "()Lnet/minecraft/server/MinecraftServer;", false));
        m.instructions.add(new VarInsnNode(ALOAD, 1));
        m.instructions.add(new MethodInsnNode(INVOKESTATIC, BASE + "util/ManualReviveManager", "clearDownedState", "(Lnet/minecraft/server/MinecraftServer;Ljava/util/UUID;)V", false));
        m.instructions.add(new MethodInsnNode(INVOKESTATIC, BASE + "util/DownedPlayerManager", "markPersistenceDirty", "()V", false));
        m.instructions.add(new InsnNode(RETURN));
        node.methods.add(m);
        return 1;
    }

    private static int patchRegistry(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("findPreferredGraveId")) continue;
            method.instructions.clear(); method.tryCatchBlocks.clear();
            method.instructions.add(new VarInsnNode(ALOAD, 0));
            method.instructions.add(new MethodInsnNode(INVOKESTATIC, FIX, "findNewestGraveId", "(Ljava/util/UUID;)Ljava/util/UUID;", false));
            method.instructions.add(new InsnNode(ARETURN));
            return 1;
        }
        return 0;
    }

    private static int patchDisplay(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (!method.name.equals("updateTextDisplay")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof VarInsnNode store && store.getOpcode() == ASTORE && store.var == 2) {
                    LabelNode present = new LabelNode();
                    InsnList guard = new InsnList();
                    guard.add(new VarInsnNode(ALOAD, 2));
                    guard.add(new JumpInsnNode(IFNONNULL, present));
                    guard.add(new InsnNode(RETURN));
                    guard.add(present);
                    guard.add(new FrameNode(F_APPEND, 1, new Object[]{"net/minecraft/world/entity/Display$TextDisplay"}, 0, null));
                    method.instructions.insert(instruction, guard);
                    return 1;
                }
            }
        }
        return 0;
    }
}
