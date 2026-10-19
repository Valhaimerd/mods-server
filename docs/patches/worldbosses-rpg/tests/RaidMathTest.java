import local.worldbosses.RaidMath;
public class RaidMathTest {
 public static void main(String[] args){
  check(RaidMath.health(7000,1)==7000,"solo health");
  check(RaidMath.health(7000,4)==10150,"four-player health");
  check(RaidMath.health(20000,8)==41000,"eight-player health");
  check(RaidMath.health(20000,100)==41000,"participant cap");
  check(RaidMath.regeneration(20,4)==26,"regeneration scaling");
  check(RaidMath.armor(22,8)==30,"armor clamp");
  check(RaidMath.armor(16,4)==20.8,"four-player armor");
  RaidMath.Budget b=new RaidMath.Budget();
  check(b.take(100,8)==8,"initial hit");
  check(b.take(101,8)==4,"shared burst budget");
  check(b.take(119,8)==0,"rolling window");
  check(b.take(120,8)==8,"first hit expires");
  check(b.take(121,8)==4,"second hit expires");
  check(b.take(122,Float.NaN)==0,"invalid damage");
  System.out.println("PASS: scaling, player cap, armor limit, rolling damage budget");
 }
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);}
}
