import net.spell_engine.internals.*;import net.minecraft.world.phys.Vec3;
public class PullMathProbe {
 static int checks;
 static void check(boolean b,String name){if(!b)throw new AssertionError(name);checks++;}
 public static void main(String[] args){
  for(int phase=0;phase<3;phase++){
   Vec3 v=Vec3.ZERO;for(int i=0;i<40;i++){v=v.scale(.546).add(0,0,.13);v=PurgatoryPull.accelerate(v,new Vec3(0,0,-8),phase,0);}
   check(phase==0?v.z>0:v.z<0,"Sprint model phase "+phase);check(v.length()<1.6,"Sustained below opening yank "+phase);
   var untouched=new Vec3(.2,.1,.3);check(PurgatoryPull.accelerate(untouched,new Vec3(8,0,0),phase,1).equals(untouched),"Knockback immunity "+phase);
   var changed=PurgatoryPull.accelerate(untouched,new Vec3(8,0,0),phase,0);check(changed.z==untouched.z&&changed.y==untouched.y,"Tangent preserved "+phase);
  }
  check(PurgatoryPull.phase(199)==0&&PurgatoryPull.phase(200)==1&&PurgatoryPull.phase(400)==2,"Exact phase times");
  for(double y:new double[]{-4,0,4}){var move=PurgatoryBoundary.clipCircle(new Vec3(11,y,0),new Vec3(3,0,0),new Vec3(0,-5,0),12,10,.3,1.8);check(move.x<1,"Ten-high wall blocks at "+y);}
  for(double y:new double[]{-7,6}){var delta=new Vec3(3,0,0);check(PurgatoryBoundary.clipCircle(new Vec3(11.8,y,0),delta,new Vec3(0,-5,0),12,10,.3,1.8).equals(delta),"Open outside vertical extent "+y);}
  System.out.println("PASS "+checks+" focused movement/collision checks");
 }
}
