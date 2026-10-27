import java.nio.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Update the Paladin default once; its ordinary node inherits that reward. */
public final class PatchNodes {
    public static void main(String[] args) throws Exception {
        var node=new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(node,0);
        int edits=0;
        for(var method:node.methods) if(method.name.equals("<clinit>")) {
            boolean paladin=false;
            for(var insn:method.instructions) {
                if(insn instanceof LdcInsnNode ldc && "paladin_root".equals(ldc.cst)) paladin=true;
                if(paladin && insn instanceof LdcInsnNode ldc && ldc.cst instanceof Double value && value==.2) {
                    ldc.cst=.25;edits++;paladin=false;
                }
            }
        }
        if(edits!=1) throw new IllegalStateException("Expected one Paladin root default, got "+edits);
        var writer=new ClassWriter(0);node.accept(writer);
        Files.write(Path.of(args[1]),writer.toByteArray());
    }
}
