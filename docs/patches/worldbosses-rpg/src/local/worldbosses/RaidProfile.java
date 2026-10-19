package local.worldbosses;

import com.mki.worldbosses.config.WorldBossesConfig;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/** One-time migration of native config; later administrator edits remain editable. */
public final class RaidProfile {
    public record Boss(double health, double armor, double melee, double regen, int tier) {}
    public static final Map<String, Boss> BOSSES = new LinkedHashMap<>();
    static {
        add("sylvaron",7000,16,8,10,1); add("aureon",8000,14,8,10,1);
        add("nekhraz",9000,18,9,11,1); add("varkhul",10000,20,10,12,2);
        add("morvath",11000,16,9,13,2); add("elyndra",12000,18,10,14,2);
        add("shadowKing",14000,20,10,16,3); add("dragonMother",15000,20,10,17,3);
        add("stormGriffin",14500,18,10,16,3); add("astrael",18000,22,12,19,4);
        add("shadowWeaver",20000,22,12,20,4);
    }
    private static void add(String id,double h,double a,double m,double r,int tier) { BOSSES.put(id,new Boss(h,a,m,r,tier)); }
    private RaidProfile() {}
    public static Object field(Object target,String name) {
        try { return target.getClass().getField(name).get(target); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(name,e); }
    }
    public static double number(Object target,String name) { return ((Number)field(target,name)).doubleValue(); }
    public static void set(Object target,String name,Object value) {
        try {
            Field f=target.getClass().getField(name);
            if (value instanceof Number n) {
                if(f.getType()==double.class) f.setDouble(target,n.doubleValue());
                else if(f.getType()==float.class) f.setFloat(target,n.floatValue());
                else if(f.getType()==int.class) f.setInt(target,n.intValue());
                else f.set(target,value);
            } else f.set(target,value);
        } catch(ReflectiveOperationException e) { throw new IllegalStateException(name,e); }
    }
    public static WorldBossesConfig.BossConfig config(String key) { return (WorldBossesConfig.BossConfig)field(WorldBossesConfig.INSTANCE,key); }
    public static void migrate(WorldBossesConfig c) {
        if(number(c,"rpgBalanceVersion")>=1) return;
        for(var entry:BOSSES.entrySet()) {
            String key=entry.getKey(); Boss spec=entry.getValue(); var b=(WorldBossesConfig.BossConfig)field(c,key);
            b.maxHealth=spec.health; b.armor=spec.armor; b.attackDamage=spec.melee;
            b.followRange=64; b.shrineCooldownSeconds=1800;
            b.xpReward=new int[]{0,2000,4000,6000,10000}[spec.tier];
            set(b,"rpgRegenerationPerSecond",spec.regen);
            b.raidScaling.healthPerExtraPlayerPercent=15; b.raidScaling.damagePerExtraPlayerPercent=0;
            b.raidScaling.maxScaledPlayers=8; b.raidScaling.basePlayerCount=1;
            b.raidScaling.onlyScaleUp=true; b.raidScaling.activeParticipantTimeoutSeconds=60;
            set(b.raidScaling,"regenerationPerExtraPlayerPercent",10);
            set(b.raidScaling,"armorPerExtraPlayerPercent",10);
            b.phases.phase2DamageMultiplier=1.05; b.phases.phase3DamageMultiplier=1.10;
            b.phases.phase2CooldownMultiplier=.90; b.phases.phase3CooldownMultiplier=.80;
            b.phases.phase2HealthPercent=70; b.phases.phase3HealthPercent=35;
            for(Field f:b.getClass().getFields()) {
                try {
                    Object skill=f.get(b);
                    if(!(skill instanceof WorldBossesConfig.SkillConfig)) continue;
                    for(Field setting:skill.getClass().getFields()) {
                        String name=setting.getName();
                        if(name.equals("damage")) set(skill,name,damage(key+"."+f.getName()));
                        else if(name.equals("damagePerTick"))set(skill,name,2);
                        else if(name.equals("warningDelayTicks"))set(skill,name,Math.max(40,number(skill,name)));
                        else if(name.equals("blindnessDurationSeconds")||name.equals("darknessDurationSeconds"))set(skill,name,1);
                        else if(name.equals("slownessDurationSeconds")||name.equals("weaknessDurationSeconds")||name.equals("witherDurationSeconds")||name.equals("fireSeconds")||name.equals("poisonDurationSeconds"))set(skill,name,2);
                        else if(name.equals("slownessAmplifier"))set(skill,name,1);
                        else if(name.equals("markersPerPlayer")||name.equals("spikesPerPlayer"))set(skill,name,3);
                        else if(name.equals("durationSeconds"))set(skill,name,4);
                        else if(name.equals("knockbackStrength"))set(skill,name,1);
                        else if(name.equals("pullStrength"))set(skill,name,1.2);
                        else if(name.equals("pullDuringTelegraph")||name.equals("pullDuringDrain"))set(skill,name,false);
                    }
                } catch(IllegalAccessException e){throw new IllegalStateException(e);}
            }
        }
        c.sylvaron.rootPrison.radius=32;
        c.aureon.lightningDive.radius=40;
        c.nekhraz.burrowStrike.radius=40; c.nekhraz.ruinSpikes.radius=40;
        c.varkhul.hellchainPull.radius=32; c.varkhul.lavaEruption.radius=40;
        c.morvath.soulDrain.radius=24; c.morvath.soulBarrage.radius=40;
        c.elyndra.voidRift.radius=40; c.elyndra.starfallBlades.radius=40;
        c.astrael.elementalBarrage.radius=40; c.astrael.primordialChains.radius=40;
        c.astrael.voidSingularity.radius=40;
        c.morvath.soulDrain.healAmount=10;
        c.astrael.finalEnrageCooldownMultiplier=.90;
        c.bossScaling.globalHealthMultiplier=1; c.bossScaling.globalDamageMultiplier=1;
        c.bossScaling.arenaRadius=64; c.bossScaling.bossLeashExtraRadius=16;
        c.bossScaling.teleportBackAfterSeconds=8; c.bossScaling.outOfArenaHealPerSecond=50;
        c.bossScaling.bossOutsideArenaHealPerSecond=25; c.bossScaling.countCreativePlayersAsArenaParticipants=false;
        c.bossSelfHeal.baseHealPerSecond=10; c.bossSelfHeal.healPerActivePlayerPerSecond=0;
        c.bossSelfHeal.startDelaySeconds=10; c.bossSelfHeal.tickIntervalSeconds=1;
        c.bossSelfHeal.maxHealPercentPerSecond=.2;
        c.bossCombatPressure.tickIntervalSeconds=8; c.bossCombatPressure.radius=8;
        c.bossCombatPressure.damage=1; c.bossCombatPressure.damagePerActivePlayer=0;
        c.bossCombatPressure.effectDurationSeconds=2; c.bossCombatPressure.morvathLifeStealMultiplier=0;
        set(c,"rpgBalanceVersion",1);
    }
    private static float damage(String id) {
        return switch(id) {
            case "sylvaron.thornSlam" -> 6;
            case "aureon.staticStorm","aureon.windBurst","morvath.fearPulse" -> 4;
            case "aureon.lightningDive","nekhraz.burrowStrike","varkhul.lavaEruption","elyndra.voidRift" -> 7;
            case "nekhraz.ruinSpikes","varkhul.hellchainPull","morvath.soulBarrage" -> 5;
            case "varkhul.moltenCleave" -> 8;
            case "astrael.primordialSlam" -> 9;
            case "astrael.worldbreakerBeam","astrael.cataclysmNova" -> 10;
            default -> 6;
        };
    }
}
