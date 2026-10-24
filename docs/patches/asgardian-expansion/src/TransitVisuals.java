package com.emandev.bifrost;

import com.mojang.math.Transformation;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Quaternionf;

/** Original prismatic, translucent display geometry. No collision or world edits. */
final class TransitVisuals {
    static final String TAG="bifrost_temporary_visual";
    private static final Map<String,Method> METHODS=new HashMap<>();
    private final ServerLevel level;private final Vec3 center;
    private final List<Panel> panels=new ArrayList<>();
    private double bottom=72,height=.01;
    private record Panel(Display.TextDisplay entity,double dx,double dz,float yaw,float width) {}
    TransitVisuals(ServerLevel level,Vec3 center) {
        this.level=level;this.center=center;
        int[] colors={0x24a2f5ff,0x249fa4ff,0x24c48bff,0x24ffbd90,0x24a5ffe6,0x24b1d7ff};
        for(int i=0;i<12;i++) {
            double angle=i*Math.PI/6;
            add(Math.cos(angle)*5.5,Math.sin(angle)*5.5,(float)(-angle+Math.PI/2),3f,colors[i%6]);
        }
        // Crossing inner planes create a bright core while leaving silhouettes readable.
        for(int i=0;i<4;i++)add(0,0,i*(float)Math.PI/4,2.1f,0x44d9eeff);
    }
    private static void call(Object target,String name,Class<?> type,Object value) {
        try {
            var owner=name.startsWith("setText")||name.equals("setFlags")||name.equals("setBackgroundColor")?Display.TextDisplay.class:Display.class;
            String key=owner.getName()+name;var method=METHODS.get(key);
            if(method==null){method=owner.getDeclaredMethod(name,type);method.setAccessible(true);METHODS.put(key,method);}
            method.invoke(target,value);
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Bifrost display API missing: "+name,e);}
    }
    private void add(double dx,double dz,float yaw,float width,int color) {
        var e=new Display.TextDisplay(EntityTypes.TEXT_DISPLAY,level);
        call(e,"setText",Component.class,Component.literal(" "));
        call(e,"setBackgroundColor",int.class,color);
        call(e,"setTextOpacity",byte.class,(byte)0);
        call(e,"setBrightnessOverride",net.minecraft.util.Brightness.class,net.minecraft.util.Brightness.FULL_BRIGHT);
        call(e,"setFlags",byte.class,(byte)0); // depth-tested, not visible through terrain
        call(e,"setViewRange",float.class,2f);call(e,"setShadowRadius",float.class,0f);call(e,"setShadowStrength",float.class,0f);
        e.setNoGravity(true);e.setInvulnerable(true);e.addTag(TAG);e.setPos(center.x+dx,center.y+72,center.z+dz);
        level.addFreshEntity(e);panels.add(new Panel(e,dx,dz,yaw,width));
        animate();
    }
    void descend(double progress) {
        progress=Math.max(0,Math.min(1,progress));height=Math.max(.01,72*progress);bottom=72-height;animate();
    }
    void retract(double progress) {
        progress=Math.max(0,Math.min(1,progress));bottom=72*progress;height=Math.max(.01,72-bottom);animate();
    }
    private void animate() {
        for(var panel:panels) {
            var e=panel.entity;e.setPos(center.x+panel.dx,center.y+bottom,center.z+panel.dz);
            // Native space background spans 5 x 10 pixels at 1/40 block; center its half-pixel offset.
            var transformation=new Transformation(new Vector3f(-panel.width*.1f,0,0).rotateY(panel.yaw),new Quaternionf().rotateY(panel.yaw),new Vector3f(panel.width*8,(float)(height*4),1),new Quaternionf());
            call(e,"setTransformation",Transformation.class,transformation);
            call(e,"setTransformationInterpolationDuration",int.class,2);call(e,"setPosRotInterpolationDuration",int.class,2);
        }
    }
    Set<UUID> ids(){var ids=new HashSet<UUID>();for(var p:panels)ids.add(p.entity.getUUID());return ids;}
    void close(){for(var p:panels)p.entity.discard();panels.clear();}
    void particles(int age,boolean retracting) {
        // 30 motes per four server ticks, per beam; ordinary client settings/distance apply.
        double y=center.y+bottom;
        level.sendParticles(ParticleTypes.END_ROD,center.x,y,center.z,8,4,.3,4,.02);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,center.x,y+height*.4,center.z,8,3,Math.min(height*.3,12),3,.03);
        level.sendParticles(ParticleTypes.GLOW,center.x,y+height*.6,center.z,8,2,Math.min(height*.3,12),2,.015);
        level.sendParticles(ParticleTypes.PORTAL,center.x,y,center.z,6,4,.5,4,retracting?.08:.03);
    }
    static void impact(ServerLevel level,Vec3 center) {
        for(int i=0;i<32;i++){double a=i*Math.PI/16;level.sendParticles(ParticleTypes.ELECTRIC_SPARK,center.x+Math.cos(a)*6,center.y+.15,center.z+Math.sin(a)*6,1,.02,.03,.02,.08);}
        level.sendParticles(ParticleTypes.GLOW,center.x,center.y+.4,center.z,20,2,.5,2,.1);
    }
    static void cleanup(MinecraftServer server,Set<UUID> active) {
        for(var level:server.getAllLevels())for(var entity:level.getAllEntities())if(entity.entityTags().contains(TAG)&&!active.contains(entity.getUUID()))entity.discard();
    }
}
