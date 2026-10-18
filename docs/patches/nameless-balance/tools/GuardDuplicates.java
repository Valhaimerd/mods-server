import java.nio.file.*;
import org.objectweb.asm.*;

/** Build-time guards for native Curios callbacks; this tool is not shipped in the mod. */
public final class GuardDuplicates {
    public static void main(String[] args) throws Exception {
        Path root=Path.of(args[0]);int[] count={0};
        try(var paths=Files.walk(root)) {
            for(var path:paths.filter(p->p.toString().endsWith(".class")).toList()) {
                var reader=new ClassReader(Files.readAllBytes(path));
                if(!reader.getClassName().startsWith("com/cozary/nameless_trinkets/items/trinkets/"))continue;
                var writer=new ClassWriter(reader,ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
                reader.accept(new ClassVisitor(Opcodes.ASM9,writer) {
                    @Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions) {
                        var visitor=super.visitMethod(access,name,desc,signature,exceptions);
                        if(!desc.startsWith("(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;"))return visitor;
                        String guard=switch(name){case "curioTick","onEquip"->"active";case "onUnequip"->"unequip";case "canUnequip"->"inactive";default->null;};
                        if(guard==null || (name.equals("onUnequip") && reader.getClassName().endsWith("/VampireBlood")))return visitor;
                        int stackSlot=name.equals("onEquip")||name.equals("onUnequip") ? 3:2;count[0]++;
                        return new MethodVisitor(Opcodes.ASM9,visitor) {
                            @Override public void visitCode() {
                                super.visitCode();super.visitVarInsn(Opcodes.ALOAD,1);super.visitVarInsn(Opcodes.ALOAD,stackSlot);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,"com/cozary/nameless_trinkets/pack/PackUniqueness",guard,
                                    "(Ltop/theillusivec4/curios/api/SlotContext;Lnet/minecraft/world/item/ItemStack;)Z",false);
                                Label original=new Label();super.visitJumpInsn(name.equals("canUnequip") ? Opcodes.IFEQ:Opcodes.IFNE,original);
                                if(name.equals("canUnequip")){super.visitInsn(Opcodes.ICONST_1);super.visitInsn(Opcodes.IRETURN);}else super.visitInsn(Opcodes.RETURN);
                                super.visitLabel(original);
                            }
                        };
                    }
                },ClassReader.SKIP_FRAMES);
                Files.write(path,writer.toByteArray());
            }
        }
        if(count[0]<40)throw new IllegalStateException("Expected all wearable callback guards, got "+count[0]);
        System.out.println("Guarded "+count[0]+" native Curios callbacks");
    }
}
