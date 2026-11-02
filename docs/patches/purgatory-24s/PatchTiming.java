import java.nio.file.*;import java.util.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
/** Method-scoped constants: unrelated banners, defenses and cooldowns keep their durations. */
public class PatchTiming implements Opcodes {
 static Integer integer(AbstractInsnNode i){if(i instanceof IntInsnNode a)return a.operand;if(i instanceof LdcInsnNode a&&a.cst instanceof Integer n)return n;return null;}
 public static void main(String[] args)throws Exception{
  var c=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(args[0]))).accept(c,0);int edits=0;String mode=args[2];Map<String,Integer> counts=new TreeMap<>();
  for(var m:c.methods)for(var i:m.instructions.toArray()){
   Object old=i instanceof LdcInsnNode a?a.cst:integer(i),next=null;
   if(mode.equals("ultimate")){
    if(m.name.equals("stage")){if(Integer.valueOf(20).equals(old))next=16;if(Integer.valueOf(40).equals(old))next=32;}
    if(m.name.equals("explosionCoefficient")){if(Double.valueOf(20).equals(old))next=16d;if(Double.valueOf(.5).equals(old))next=.625d;if(Double.valueOf(.7).equals(old))next=.875d;}
    if(m.name.equals("process")&&Long.valueOf(600).equals(old))next=480L;
    if(m.name.equals("startChannel")&&Integer.valueOf(602).equals(old))next=482;
    if(Set.of("advance","cost","end","resolute","low").contains(m.name)&&Integer.valueOf(40).equals(old))next=32;
    if(m.name.equals("advance")){if(Long.valueOf(60).equals(old))next=48L;if(Integer.valueOf(60).equals(old))next=48;}
    if(m.name.equals("end")&&Double.valueOf(30).equals(old))next=24d;
   }
   if(mode.equals("focus")&&m.name.equals("focused")){if(Long.valueOf(200).equals(old))next=160L;if(Integer.valueOf(200).equals(old))next=160;}
   if(mode.equals("pull")&&m.name.equals("phase")){if(Long.valueOf(200).equals(old))next=160L;if(Long.valueOf(400).equals(old))next=320L;}
   if(mode.equals("boundary")&&m.name.equals("newSpawn")&&Integer.valueOf(31).equals(old))next=25;
   if(next!=null){m.instructions.set(i,new LdcInsnNode(next));edits++;counts.merge(m.name,1,Integer::sum);}
  }
  int expected=switch(mode){case "ultimate"->15;case "focus","pull"->2;case "boundary"->1;default->-1;};
  if(edits!=expected)throw new AssertionError(mode+" sites "+edits+" expected "+expected+" "+counts);
  var w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Files.write(Path.of(args[1]),w.toByteArray());System.out.println(mode+" "+counts);
 }
}
