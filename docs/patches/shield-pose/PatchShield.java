import java.nio.file.*;
import org.objectweb.asm.*;

public final class PatchShield {
    public static void main(String[] args)throws Exception{
        var reader=new ClassReader(Files.readAllBytes(Path.of(args[0])));
        var writer=new ClassWriter(reader,0);int[] count={0};
        reader.accept(new ClassVisitor(Opcodes.ASM9,writer){
            public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] exceptions){
                return new MethodVisitor(Opcodes.ASM9,super.visitMethod(access,name,desc,sig,exceptions)){
                    public void visitCode(){
                        super.visitCode();
                        if(name.equals("isHandBlacklisted")&&desc.equals("(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Z")){
                            var original=new Label();super.visitVarInsn(Opcodes.ALOAD,0);super.visitVarInsn(Opcodes.ALOAD,1);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"punchy/compat/ShieldKinds","nativeBlockHand",desc,false);
                            super.visitJumpInsn(Opcodes.IFEQ,original);super.visitInsn(Opcodes.ICONST_1);super.visitInsn(Opcodes.IRETURN);
                            super.visitLabel(original);super.visitFrame(Opcodes.F_SAME,0,null,0,null);count[0]++;
                        }
                    }
                    public void visitTypeInsn(int opcode,String type){
                        if(opcode==Opcodes.INSTANCEOF&&type.equals("net/minecraft/world/item/ShieldItem")){
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,"punchy/compat/ShieldKinds","isShield","(Ljava/lang/Object;)Z",false);count[0]++;
                        }else super.visitTypeInsn(opcode,type);
                    }
                };
            }
        },0);
        if(count[0]!=Integer.parseInt(args[2]))throw new IllegalStateException("Unexpected shield checks: "+count[0]);
        Files.write(Path.of(args[1]),writer.toByteArray());
    }
}
