package com.eiden.iourusraces.race;
/** NONE is a valid server response; it must not also mean 'packet still pending'. */
public final class ClientRaceData {
 private static Race clientRace=Race.NONE;
 private static boolean synced;
 private ClientRaceData(){}
 public static Race getRace(){return clientRace;}
 public static boolean isSynced(){return synced;}
 public static void setRace(Race race){clientRace=race!=null?race:Race.NONE;synced=true;}
 public static void reset(){clientRace=Race.NONE;synced=false;}
}
