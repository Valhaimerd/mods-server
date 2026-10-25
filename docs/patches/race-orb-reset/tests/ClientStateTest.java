package com.eiden.iourusraces.race;
public class ClientStateTest {
 public static void main(String[] args){
  ClientRaceData.reset();check(!ClientRaceData.isSynced(),"logout starts unknown, not confirmed raceless");
  ClientRaceData.setRace(Race.ELF);check(ClientRaceData.isSynced()&&ClientRaceData.getRace()==Race.ELF,"selected race acknowledged");
  ClientRaceData.reset();check(!ClientRaceData.isSynced(),"reconnect waits for fresh race packet");
  ClientRaceData.setRace(Race.NONE);check(ClientRaceData.isSynced()&&ClientRaceData.getRace()==Race.NONE,"server confirms new player or reset");
  ClientRaceData.setRace(Race.GOBLIN);check(ClientRaceData.getRace()==Race.GOBLIN,"selection replaces raceless state");
  System.out.println("PASS 5 synchronized race-state checks");
 }
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
}
