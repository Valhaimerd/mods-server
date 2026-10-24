import java.nio.file.*;
import org.objectweb.asm.*;
public class PatchDual implements Opcodes {
 public static void main(String[] args)throws Exception{
  var reader=new ClassReader(Files.readAllBytes(Path.of(args[0])));var writer=new ClassWriter(reader,0);int[] edits={0};String mode=args[2];
  reader.accept(new ClassVisitor(ASM9,writer){public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){
   return new MethodVisitor(ASM9,super.visitMethod(access,name,desc,signature,exceptions)){
    public void visitFieldInsn(int op,String owner,String field,String descriptor){
     if(((mode.equals("fracture")&&name.equals("addEffect"))||(mode.equals("counter")&&name.equals("hit")))&&field.equals("PHYSICAL_MELEE")&&owner.equals("net/spell_engine/api/spell/ExternalSpellSchools")){field="PHYSICAL_MELEE_DUAL";edits[0]++;}
     super.visitFieldInsn(op,owner,field,descriptor);
    }
    public void visitLdcInsn(Object value){
     if(mode.equals("fracture")&&name.equals("addEffect")&&Double.valueOf(-.1).equals(value)){value=-.05;edits[0]++;}
     if(mode.equals("counter")&&name.equals("hit")&&Double.valueOf(.25).equals(value)){value=.15;edits[0]++;}
     if(mode.equals("crimson")&&name.equals("hit")&&Double.valueOf(.1).equals(value)){value=.15;edits[0]++;}
     if(mode.equals("bleed")&&name.equals("applyEffectTick")&&Float.valueOf(6).equals(value)){value=4f;edits[0]++;}
     super.visitLdcInsn(value);
    }
    public void visitInsn(int op){if(mode.equals("bleed")&&name.equals("applyEffectTick")&&op==FCONST_2){op=FCONST_1;edits[0]++;}super.visitInsn(op);}
   };
  }},0);
  int expected=mode.equals("crimson")?1:2;if(edits[0]!=expected)throw new AssertionError(mode+" edits="+edits[0]);Files.write(Path.of(args[1]),writer.toByteArray());
 }
}
