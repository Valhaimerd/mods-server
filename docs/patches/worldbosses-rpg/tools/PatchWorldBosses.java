import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Narrow, asserted bytecode hooks; upstream methods remain intact unless replaced. */
public final class PatchWorldBosses implements Opcodes {
    static final String B="com/mki/worldbosses/entity/boss/WorldBossEntity";
    static final String R="local/worldbosses/RaidRuntime";
    static final String C="com/mki/worldbosses/config/WorldBossesConfig";
    static final Set<String> MAJOR=Set.of("sporeBurst","rootPrison","thornSlam","staticStorm","lightningDive","windBurst","thunderCage","sandstormVeil","burrowStrike","ruinSpikes","moltenCleave","lavaEruption","hellchainPull","flamePrison","soulDrain","eclipseZone","soulBarrage","fearPulse","voidRift","gravityCollapse","starfallBlades","primordialSlam","elementalBarrage","voidSingularity","worldbreakerBeam","primordialChains","cataclysmNova");
    static final Map<String,Integer> COUNTS=new TreeMap<>();
    static void count(String key){COUNTS.merge(key,1,Integer::sum);}
    static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,owner,name,desc,false);}
    static InsnList args(int... slots){InsnList x=new InsnList();for(int slot:slots)x.add(new VarInsnNode(ALOAD,slot));return x;}
    static void returns(MethodNode m,java.util.function.Supplier<InsnList> code){for(var n:m.instructions.toArray())if(n.getOpcode()==RETURN)m.instructions.insertBefore(n,code.get());}
    static void replace(MethodNode m,InsnList code,int ret){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();m.instructions.add(code);m.instructions.add(new InsnNode(ret));count("replace "+m.name);}
    static void bridge(MethodNode m,String name,String desc,int ret,int...slots){InsnList x=args(slots);x.add(call(R,name,desc));replace(m,x,ret);}
    static void constant(MethodNode m,Number from,Number to){
        for(var n:m.instructions.toArray()) {
            Object value=null;
            if(n instanceof LdcInsnNode l)value=l.cst;
            else if(n instanceof IntInsnNode i)value=i.operand;
            else if(n.getOpcode()>=ICONST_M1&&n.getOpcode()<=ICONST_5)value=n.getOpcode()-ICONST_0;
            else if(n.getOpcode()>=FCONST_0&&n.getOpcode()<=FCONST_2)value=(float)(n.getOpcode()-FCONST_0);
            if(value instanceof Number v&&v.getClass()==from.getClass()&&v.equals(from)){m.instructions.set(n,new LdcInsnNode(to));count("constant "+m.name);}
        }
    }
    static void gate(ClassNode c,MethodNode m,String predicate){
        String name=m.name;m.name="rpg$"+name;
        MethodNode wrapper=new MethodNode(m.access,name,m.desc,m.signature,m.exceptions.toArray(String[]::new));
        LabelNode ok=new LabelNode();wrapper.instructions.add(new VarInsnNode(ALOAD,0));wrapper.instructions.add(call(R,predicate,"(L"+B+";)Z"));wrapper.instructions.add(new JumpInsnNode(IFNE,ok));wrapper.instructions.add(new InsnNode(RETURN));wrapper.instructions.add(ok);wrapper.instructions.add(new FrameNode(F_SAME,0,null,0,null));
        wrapper.instructions.add(new VarInsnNode(ALOAD,0));int slot=1;
        for(Type t:Type.getArgumentTypes(m.desc)){wrapper.instructions.add(new VarInsnNode(t.getOpcode(ILOAD),slot));slot+=t.getSize();}
        wrapper.instructions.add(new MethodInsnNode(INVOKESPECIAL,c.name,m.name,m.desc,false));wrapper.instructions.add(new InsnNode(RETURN));c.methods.add(wrapper);count("gate "+name);
    }
    static byte[] patch(String path,byte[] bytes){
        boolean selected=path.equals(C+".class")||path.equals(C+"$BossConfig.class")||path.equals(C+"$RaidScalingConfig.class")
            ||path.startsWith("com/mki/worldbosses/entity/boss/")&&!path.contains("$")
            ||Set.of("com/mki/worldbosses/WorldBosses.class","com/mki/worldbosses/server/BossRewardManager.class","com/mki/worldbosses/server/RaidCacheOpeningManager.class","com/mki/worldbosses/item/ShadowbreakerSetBonus.class","com/mki/worldbosses/recipe/GearUpgradeRecipe.class","com/mki/worldbosses/item/GearUpgrades.class","com/mki/worldbosses/block/entity/BossAltarBlockEntity.class","com/mki/worldbosses/registry/ModCreativeTabs.class").contains(path);
        if(!selected)return bytes;
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);
        if(c.name.equals(C))c.fields.add(new FieldNode(ACC_PUBLIC,"rpgBalanceVersion","I",null,null));
        if(c.name.equals(C+"$BossConfig"))c.fields.add(new FieldNode(ACC_PUBLIC,"rpgRegenerationPerSecond","D",null,null));
        if(c.name.equals(C+"$RaidScalingConfig"))for(String key:List.of("regenerationPerExtraPlayerPercent","armorPerExtraPlayerPercent"))c.fields.add(new FieldNode(ACC_PUBLIC,key,"D",null,null));
        for(MethodNode m:new ArrayList<>(c.methods)) {
            if(c.name.equals("com/mki/worldbosses/registry/ModCreativeTabs"))for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode invoke&&invoke.name.equals("accept")&&invoke.desc.equals("(Lnet/minecraft/world/level/ItemLike;)V"))m.instructions.set(invoke,call(R,"creative","(Lnet/minecraft/world/item/CreativeModeTab$Output;Lnet/minecraft/world/level/ItemLike;)V"));
            if(c.name.equals(C)&&m.name.equals("save")) {
                InsnList x=new InsnList();x.add(new FieldInsnNode(GETSTATIC,C,"INSTANCE","L"+C+";"));x.add(call("local/worldbosses/RaidProfile","migrate","(L"+C+";)V"));m.instructions.insert(x);count("config migration");
            }
            if(c.name.equals("com/mki/worldbosses/WorldBosses")&&m.name.equals("<init>"))returns(m,()->{InsnList x=args(1);x.add(call(R,"register","(Lnet/neoforged/bus/api/IEventBus;)V"));return x;});
            if(c.name.equals(B))switch(m.name){
                case "updateRaidScaling" -> bridge(m,"scale","(L"+B+";Lnet/minecraft/server/level/ServerLevel;L"+C+"$BossConfig;)V",RETURN,0,1,2);
                case "tickSelfHeal" -> bridge(m,"heal","(L"+B+";Lnet/minecraft/server/level/ServerLevel;)V",RETURN,0,1);
                case "recordParticipant" -> {InsnList x=args(0,1);x.add(call(R,"record","(L"+B+";Lnet/minecraft/world/entity/player/Player;)V"));m.instructions.insert(x);}
                case "tick" -> returns(m,()->{InsnList x=args(0);x.add(call(R,"tick","(L"+B+";)V"));return x;});
                case "initializeBossArena" -> returns(m,()->{InsnList x=args(0,1);x.add(call(R,"home","(L"+B+";Lnet/minecraft/core/BlockPos;)V"));return x;});
                case "addAdditionalSaveData" -> returns(m,()->{InsnList x=args(0,1);x.add(call(R,"save","(L"+B+";Lnet/minecraft/world/level/storage/ValueOutput;)V"));return x;});
                case "readAdditionalSaveData" -> returns(m,()->{InsnList x=args(0,1);x.add(call(R,"load","(L"+B+";Lnet/minecraft/world/level/storage/ValueInput;)V"));return x;});
            }
            if(c.name.equals("com/mki/worldbosses/server/BossRewardManager")&&m.name.equals("rewardParticipants"))bridge(m,"reward","(Lnet/minecraft/server/level/ServerLevel;L"+B+";)V",RETURN,0,1);
            if(c.name.equals("com/mki/worldbosses/server/RaidCacheOpeningManager")&&m.name.equals("startOpening"))bridge(m,"openCache",m.desc,RETURN,0,1,2);
            if(c.name.equals("com/mki/worldbosses/item/ShadowbreakerSetBonus")&&m.name.equals("register"))replace(m,new InsnList(),RETURN);
            if(c.name.equals("com/mki/worldbosses/recipe/GearUpgradeRecipe")&&m.name.equals("matches")){InsnList x=new InsnList();x.add(new InsnNode(ICONST_0));replace(m,x,IRETURN);}
            if(c.name.equals("com/mki/worldbosses/item/GearUpgrades")&&m.name.equals("level")){InsnList x=new InsnList();x.add(new InsnNode(ICONST_0));replace(m,x,IRETURN);}
            if((c.superName.equals(B)||c.superName.equals("com/mki/worldbosses/entity/boss/RealmBossEntity"))&&!c.name.equals(B)) {
                boolean hazard=Arrays.stream(m.instructions.toArray()).anyMatch(n->n instanceof FieldInsnNode f&&f.name.equals("damagePerTick"));
                if(m.name.equals("cooldown")&&m.desc.equals("(I)I")){
                    InsnList x=args(0);x.add(new VarInsnNode(ILOAD,1));x.add(call(R,"realmCooldown","(L"+B+";I)I"));replace(m,x,IRETURN);
                }
                if(Set.of("ShadowKingEntity","DragonMotherEntity","StormGriffinEntity","ShadowWeaverEntity").stream().anyMatch(c.name::endsWith)&&(m.name.equals("startCast")||m.name.equals("start"))) {
                    InsnList x=args(0,1);x.add(new VarInsnNode(ILOAD,2));x.add(call(R,"windup","(L"+B+";Ljava/lang/Object;I)I"));x.add(new VarInsnNode(ISTORE,2));m.instructions.insert(x);
                }
                if(c.name.endsWith("ShadowKingEntity")) {
                    if(m.name.equals("castBolts"))constant(m,10f,6f);
                    if(m.name.equals("releaseNova"))constant(m,12f,8f);
                }
                if(c.name.endsWith("DragonMotherEntity")) {
                    if(m.name.equals("breatheFire"))constant(m,5f,2f);
                    if(m.name.equals("tailSwipe"))constant(m,12f,8f);
                    if(m.name.equals("spitFlame"))constant(m,8f,6f);
                    if(m.name.equals("diveImpact"))constant(m,14f,10f);
                    if(m.name.equals("tickSkill"))for(var n:m.instructions.toArray())if(n.getOpcode()==ICONST_5&&n.getNext()!=null&&n.getNext().getOpcode()==IREM)m.instructions.set(n,new IntInsnNode(BIPUSH,20));
                }
                if(c.name.endsWith("StormGriffinEntity")) {
                    if(m.name.equals("windGust"))constant(m,6f,5f);
                    if(m.name.equals("tickStrikes"))constant(m,10f,7f);
                    if(m.name.equals("diveImpact"))constant(m,12f,9f);
                    if(m.name.equals("markLightning")){constant(m,30,40);constant(m,36,44);}
                }
                if(c.name.endsWith("ShadowWeaverEntity")) {
                    if(m.name.equals("soulBolts"))constant(m,11f,6f);
                    if(m.name.equals("dragonFire"))constant(m,9f,6f);
                    if(m.name.equals("tickLightning"))constant(m,12f,8f);
                    if(m.name.equals("bindWithThread"))constant(m,6f,4f);
                    if(m.name.equals("hurtServer")){constant(m,.3f,.75f);constant(m,.1f,.6f);}
                    if(m.name.equals("raiseVeil"))constant(m,220,120);
                    if(m.name.equals("markLightning"))constant(m,30,40);
                }
                for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode call) {
                    if(c.name.endsWith("MorvathEntity")&&m.name.equals("triggerSoulDrain")&&call.name.equals("phaseDamageMultiplier")&&call.desc.equals("()D"))m.instructions.set(call,call(R,"drainMultiplier","(L"+B+";)D"));
                    if(call.name.equals("addFreshEntity")&&call.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.set(call,call(R,"spawnOwned","(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;L"+B+";)Z"));count("owned spawn");
                    }
                    if(hazard&&call.name.equals("hurt")&&call.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;F)V")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.insertBefore(call,new LdcInsnNode(c.name+"."+m.name));m.instructions.set(call,call(R,"hazardHurt","(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;FL"+B+";Ljava/lang/String;)V"));count("hazard budget");
                    }
                    if(call.name.equals("push")&&call.desc.equals("(DDD)V")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.set(call,call(R,"push","(Lnet/minecraft/world/entity/Entity;DDDL"+B+";)V"));
                    }
                    if(call.name.equals("setDeltaMovement")&&(call.desc.equals("(DDD)V")||call.desc.equals("(Lnet/minecraft/world/phys/Vec3;)V"))) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));String desc=call.desc.equals("(DDD)V")?"(Lnet/minecraft/world/entity/Entity;DDDL"+B+";)V":"(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;L"+B+";)V";
                        m.instructions.set(call,call(R,"move",desc));
                    }
                    if(c.name.endsWith("ShadowKingEntity")&&call.name.equals("teleportTo")&&call.desc.equals("(DDD)V"))m.instructions.set(call,call(R,"safeBossTeleport","(L"+B+";DDD)V"));
                    if(call.name.equals("lightningBolt")&&call.desc.equals("()Lnet/minecraft/world/damagesource/DamageSource;")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.set(call,call(R,"lightning","(Lnet/minecraft/world/damagesource/DamageSources;L"+B+";)Lnet/minecraft/world/damagesource/DamageSource;"));
                    }
                    if(c.name.endsWith("AureonEntity")&&m.name.equals("triggerLightningDive")&&call.owner.equals("java/lang/Math")&&call.name.equals("max")&&call.desc.equals("(DD)D"))m.instructions.set(call,call(R,"diveRadius","(DD)D"));
                    if(call.name.equals("setRemainingFireTicks")&&call.desc.equals("(I)V")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.set(call,call(R,"fire","(Lnet/minecraft/world/entity/LivingEntity;IL"+B+";)V"));
                    }
                    if(call.name.equals("igniteForSeconds")&&call.desc.equals("(F)V")) {
                        m.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));m.instructions.set(call,call(R,"ignite","(Lnet/minecraft/world/entity/LivingEntity;FL"+B+";)V"));
                    }
                }
                if(MAJOR.contains(m.name)&&m.desc.endsWith(")V"))gate(c,m,"major");
                if(m.name.equals("tickSkill"))gate(c,m,"realmTick");
            }
            if(c.name.endsWith("/DragonFlameEntity"))for(var n:m.instructions.toArray())if(n instanceof MethodInsnNode invoke&&invoke.name.equals("igniteForSeconds")&&invoke.desc.equals("(F)V")) {
                m.instructions.insertBefore(invoke,new VarInsnNode(ALOAD,0));m.instructions.set(invoke,call(R,"igniteProjectile","(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/entity/Entity;)V"));
            }
            if(c.name.equals("com/mki/worldbosses/block/entity/BossAltarBlockEntity")&&m.name.equals("tryOffer")) {
                InsnList x=args(1,0);x.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/level/block/entity/BlockEntity","getBlockPos","()Lnet/minecraft/core/BlockPos;",false));x.add(new VarInsnNode(ALOAD,2));x.add(call(R,"altarReady","(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)Z"));
                LabelNode ok=new LabelNode();x.add(new JumpInsnNode(IFNE,ok));x.add(new InsnNode(ICONST_0));x.add(new InsnNode(IRETURN));x.add(ok);x.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(x);count("altar cooldown");
            }
        }
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
    }
    public static void main(String[] args)throws Exception {
        Path output=Path.of(args[1]);Files.createDirectories(output);
        try(ZipFile jar=new ZipFile(args[0])) {
            for(var entries=jar.entries();entries.hasMoreElements();) {
                var e=entries.nextElement();String n=e.getName();
                if(!n.endsWith(".class")||!n.startsWith("com/mki/worldbosses/"))continue;
                byte[] old=jar.getInputStream(e).readAllBytes();byte[] patched=patch(n,old);
                Path file=output.resolve(n);Files.createDirectories(file.getParent());Files.write(file,patched);
            }
        }
        if(!COUNTS.containsKey("config migration")||!COUNTS.containsKey("replace rewardParticipants")||!COUNTS.containsKey("altar cooldown"))throw new AssertionError(COUNTS);
        System.out.println(COUNTS);
    }
}
