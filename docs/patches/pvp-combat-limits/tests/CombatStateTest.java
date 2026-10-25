package io.redspace.pvp_flagging;
public class CombatStateTest {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 public static void main(String[] a){
  CombatState s=new CombatState();long t=100000;
  check(s.flag(t));check(!s.flag(t+1000));check(s.active(t+40999));
  check(s.window(false,t+1000)==0);s.start(false,t+1000);check(s.window(false,t+1000)==30000);
  check(s.window(true,t+2000)==0);s.start(true,t+2000);check(s.window(true,t+2000)==30000);
  for(int i=0;i<20;i++){check(s.allowed(true,1,t+2000));s.consume(true,1);}
  check(!s.allowed(true,1,t+2001));check(s.window(true,t+2001)==29999);
  check(s.allowed(false,30,t+2001));s.consume(false,30);check(!s.allowed(false,1,t+2001));
  check(!s.flag(t+3000));check(!s.allowed(true,1,t+3001));
  check(s.allowed(false,1,t+31000));s.start(false,t+31000);check(s.window(false,t+31000)==30000);
  check(s.allowed(true,20,t+32000));check(!s.allowed(true,21,t+32000));s.start(true,t+32000);s.consume(true,19);check(!s.allowed(true,2,t+32000));
  CombatState restored=new CombatState(s.until,s.breakUntil,s.placeUntil,s.broken,s.placed);
  check(restored.active(t+42000));check(!restored.allowed(true,2,t+42000));
  check(!restored.active(t+43000));check(!restored.active(t+100000));
  check(restored.flag(t+100000));check(restored.window(true,t+100000)==0);check(restored.allowed(true,20,t+100000));
  System.out.println("PASS "+checks+" combat deadline/budget checks");
 }
}
