package com.cozary.nameless_trinkets.pack;
import java.util.Set;
public final class PackBalance {
 private PackBalance() {}
 public static boolean removed(String id) { return REMOVED.contains(id.replace("nameless_trinkets:", "")); }
 private static final Set<String> REMOVED=Set.of("broken_magnet","cracked_crown","dying_star","electric_paddle","ethereal_wings","gods_crown","lunar_crest","pocket_lightning_rod","reforger","speed_force","super_magnet","what_magnet");
 public static void apply(Object config) {
  String name=config.getClass().getName();name=name.substring(name.lastIndexOf('.')+1).replace("Base$Stats", "");
  try {
   switch(name) {
    case "AmphibiousHands" -> {
     number(config,"miningUnderwaterSpeedPercentage",125.0);
    }
    case "EclipseAshes" -> {
     number(config,"spawnIntervalTicks",20);
     number(config,"activeDurationSeconds",5.0);
    }
    case "ExperienceBattery" -> {
     number(config,"extraExperiencePercentage",50.0);
    }
    case "FracturedNullstone" -> {
     number(config,"magicDamageReductionPercentage",30.0);
    }
    case "GhastEye" -> {
     number(config,"extraHearts",6.0);
     number(config,"regenerationTime",100);
     number(config,"regenerationExtraTime",50);
    }
    case "IceCube" -> {
     number(config,"slownessLevel",0);
     number(config,"slownessTime",100);
     number(config,"frostWalkerLevel",1);
    }
    case "LightGloves" -> {
     number(config,"miningSpeedPercentage",150.0);
    }
    case "MissingPage" -> {
     number(config,"activationPercentage",100.0);
     number(config,"percentageOfDamage",5.0);
    }
    case "PufferFishLiver" -> {
     number(config,"chanceToApplyPoison",100.0);
     number(config,"poisonLevel",0);
     number(config,"poisonTime",60);
    }
    case "RageMind" -> {
     number(config,"damageMultiplierPercentage",125.0);
    }
    case "ReverseCard" -> {
     number(config,"chanceToActivate",25.0);
    }
    case "ScarabAmulet" -> {
     number(config,"speedMultiplierPercentage",50.0);
    }
    case "ShrinkingVeil" -> {
     number(config,"speedPercentage",25.0);
    }
    case "TitansMark" -> {
     number(config,"attackDamagePercentage",0.0);
    }
    case "TrueHeartOfTheSea" -> {
     number(config,"miningUnderwaterSpeedPercentage",125.0);
    }
    case "VampireBlood" -> {
     number(config,"damageMultiplierPercentage",20.0);
    }
    case "BrokenMagnet","CrackedCrown","DyingStar","ElectricPaddle","EtherealWings","GodsCrown","LunarCrest","PocketLightningRod","Reforger","SpeedForce","SuperMagnet","WhatMagnet" -> config.getClass().getField("isEnable").setBoolean(config,false);
   }
  } catch(ReflectiveOperationException e) { throw new IllegalStateException("Pack balance does not match installed trinket: "+name,e); }
 }
 private static void number(Object config,String field,double value) throws ReflectiveOperationException {
  var f=config.getClass().getField(field);
  if(f.getType()==int.class) f.setInt(config,(int)value);
  else if(f.getType()==float.class) f.setFloat(config,(float)value);
  else if(f.getType()==double.class) f.setDouble(config,value);
  else throw new IllegalStateException(field);
 }
}
