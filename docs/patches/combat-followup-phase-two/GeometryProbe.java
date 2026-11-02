import net.minecraft.world.phys.Vec3;
import net.spell_engine.internals.PurgatoryBoundary;
public class GeometryProbe {
 static int n;static void check(boolean ok,String s){if(!ok)throw new AssertionError(s);n++;}
 public static void main(String[]args){
  Vec3 center=new Vec3(0,95,0);
  for(double radius:new double[]{12,14})for(double width:new double[]{.6,1.5,2.4})for(double angle:new double[]{0,.4,1.5,3}){
   double x=Math.cos(angle),z=Math.sin(angle);Vec3 start=new Vec3(x*(radius-1.8),100,z*(radius-1.8));var delta=new Vec3(x*5,0,z*5);var clipped=PurgatoryBoundary.clipCircle(start,delta,center,radius,10,width/2,1.8);check(clipped.length()<delta.length(),"exit");
   start=new Vec3(x*(radius+2),100,z*(radius+2));delta=new Vec3(-x*5,0,-z*5);clipped=PurgatoryBoundary.clipCircle(start,delta,center,radius,10,width/2,1.8);check(clipped.length()<delta.length(),"entry");
  }
  for(double y:new double[]{90,106}){var d=new Vec3(5,0,0);check(PurgatoryBoundary.clipCircle(new Vec3(11,y,0),d,center,12,10,.3,1.8).equals(d),"open above/below");}
  var large=new Vec3(50,0,0);check(PurgatoryBoundary.clipCircle(new Vec3(-20,100,0),large,center,12,10,.3,1.8).length()<10,"swept outside to outside");
  System.out.println("PASS "+n+" enclosure geometry checks");
 }
}
