import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Build-only patcher: preserve native consumption, filters, and inventory transactions. */
public final class PatchConsumables implements Opcodes {
    static final String GUARD="net/p3pp3rf1y/sophisticatedcore/compat/DownedConsumables";
    static final String ALCHEMY="net/p3pp3rf1y/sophisticatedcore/upgrades/alchemy/AlchemyUpgradeWrapper";

    static InsnList guard(int local, boolean bool) {
        var code=new InsnList(); var active=new LabelNode();
        code.add(new VarInsnNode(ALOAD,local));
        code.add(new MethodInsnNode(INVOKESTATIC,GUARD,"isDowned","(Lnet/minecraft/world/entity/Entity;)Z",false));
        code.add(new JumpInsnNode(IFEQ,active));
        if(bool) code.add(new InsnNode(ICONST_0));
        code.add(new InsnNode(bool?IRETURN:RETURN)); code.add(active);
        return code;
    }

    public static void main(String[] args) throws Exception {
        var path=Path.of(args[0]); var reader=new ClassReader(Files.readAllBytes(path));
        var cls=new ClassNode(); reader.accept(cls,0); int count=0;
        for(var method:cls.methods) {
            if(method.name.equals("feedPlayerAndGetHungry") || method.name.equals("entityMatches")) {
                method.instructions.insert(guard(1,true)); count++;
            } else if(cls.name.equals(ALCHEMY) && method.name.equals("applyTo")) {
                method.instructions.insert(guard(1,false)); count++;
            } else if(cls.name.equals(ALCHEMY) && method.name.equals("tick")) {
                var code=guard(1,false); var active=new LabelNode();
                code.add(new VarInsnNode(ALOAD,0));
                code.add(new FieldInsnNode(GETFIELD,ALCHEMY,"applyingToEntity","Lnet/minecraft/world/entity/LivingEntity;"));
                code.add(new MethodInsnNode(INVOKESTATIC,GUARD,"isDowned","(Lnet/minecraft/world/entity/Entity;)Z",false));
                code.add(new JumpInsnNode(IFEQ,active)); code.add(new InsnNode(RETURN)); code.add(active);
                method.instructions.insert(code); count++;
            }
        }
        int expected=cls.name.equals(ALCHEMY)?3:1;
        if(count!=expected)throw new AssertionError(cls.name+" guards "+count+" expected "+expected);
        var out=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
        cls.accept(out); Files.write(path,out.toByteArray());
        System.out.println("Guarded "+count+" methods in "+cls.name);
    }
}
