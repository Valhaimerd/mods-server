package io.redspace.pvp_flagging;

/** Millisecond deadlines keep offline time and dimension changes consistent. */
public final class CombatState {
 public long until,breakUntil,placeUntil;
 public int broken,placed;
 public CombatState(){}
 public CombatState(long u,long b,long p,int bc,int pc){until=u;breakUntil=b;placeUntil=p;broken=bc;placed=pc;}
 public boolean active(long now){return until>now;}
 public boolean flag(long now){boolean fresh=!active(now);if(fresh){breakUntil=placeUntil=0;broken=placed=0;}until=now+40000;return fresh;}
 public long window(boolean place,long now){return Math.max(0,(place?placeUntil:breakUntil)-now);}
 public void start(boolean place,long now){if(window(place,now)>0)return;if(place){placeUntil=now+30000;placed=0;}else{breakUntil=now+30000;broken=0;}}
 public boolean allowed(boolean place,int count,long now){int used=window(place,now)==0?0:place?placed:broken;return count>=0&&used+count<=(place?20:30);}
 public void consume(boolean place,int count){if(place)placed+=count;else broken+=count;}
}
