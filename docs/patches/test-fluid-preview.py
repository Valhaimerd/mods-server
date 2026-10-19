"""Exercise the real ACFluids bytecode against minimal API fixtures (not a game test)."""
import argparse
from pathlib import Path
import subprocess
import tempfile
import zipfile

p = argparse.ArgumentParser()
p.add_argument('jar', type=Path)
p.add_argument('--java-bin', type=Path, required=True)
a = p.parse_args()
sources = {
 'net/minecraft/tags/TagKey.java': 'package net.minecraft.tags; public class TagKey {}',
 'net/minecraft/tags/FluidTags.java': 'package net.minecraft.tags; public class FluidTags { public static final TagKey WATER=new TagKey(); }',
 'com/github/alexmodguy/alexscaves/server/misc/ACTagRegistry.java': 'package com.github.alexmodguy.alexscaves.server.misc; import net.minecraft.tags.TagKey; public class ACTagRegistry { public static final TagKey ACID=new TagKey(), PURPLE_SODA=new TagKey(); }',
 'net/minecraft/world/level/BlockGetter.java': 'package net.minecraft.world.level; public interface BlockGetter {}',
 'net/minecraft/world/level/Level.java': 'package net.minecraft.world.level; import net.minecraft.world.level.chunk.ChunkSource; public class Level implements BlockGetter { public ChunkSource chunks; public ChunkSource getChunkSource(){return chunks;} }',
 'net/minecraft/world/phys/AABB.java': 'package net.minecraft.world.phys; public class AABB { public double minX=0,minY=0,minZ=0,maxX=1,maxY=1,maxZ=1; public AABB deflate(double d){return this;} }',
 'net/minecraft/world/entity/Entity.java': 'package net.minecraft.world.entity; import net.minecraft.world.level.Level; import net.minecraft.world.phys.AABB; public class Entity { public Level world; public Level level(){return world;} public AABB getBoundingBox(){return new AABB();} public double getEyeY(){return 0.5;} public double getX(){return 0.5;} public double getZ(){return 0.5;} }',
 'net/minecraft/core/BlockPos.java': '''package net.minecraft.core; public class BlockPos { int x,y,z; public int getX(){return x;} public int getY(){return y;} public int getZ(){return z;} public static BlockPos containing(double x,double y,double z){return new MutableBlockPos().set((int)Math.floor(x),(int)Math.floor(y),(int)Math.floor(z));} public static class MutableBlockPos extends BlockPos { public MutableBlockPos set(int x,int y,int z){this.x=x;this.y=y;this.z=z;return this;} } }''',
 'net/minecraft/world/level/chunk/ChunkSource.java': 'package net.minecraft.world.level.chunk; public class ChunkSource { public LevelChunk chunk; public LevelChunk getChunkNow(int x,int z){return chunk;} }',
 'net/minecraft/world/level/chunk/LevelChunk.java': 'package net.minecraft.world.level.chunk; import net.minecraft.core.BlockPos; import net.minecraft.world.level.material.FluidState; public class LevelChunk { public FluidState fluid=new FluidState(); public FluidState getFluidState(BlockPos pos){return fluid;} }',
 'net/minecraft/world/level/material/FluidState.java': 'package net.minecraft.world.level.material; import net.minecraft.tags.TagKey; import net.minecraft.world.level.BlockGetter; import net.minecraft.core.BlockPos; public class FluidState { public boolean empty; public boolean isEmpty(){return empty;} public boolean is(TagKey t){return true;} public float getHeight(BlockGetter l,BlockPos p){return 0.75f;} }',
 'FluidPreviewTest.java': '''import java.lang.reflect.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.*;
import net.minecraft.tags.TagKey;
public class FluidPreviewTest {
 public static void main(String[] args) throws Exception {
  Class<?> c=Class.forName("com.github.alexmodguy.alexscaves.server.misc.ACFluids");
  Method height=c.getDeclaredMethod("fluidHeight",Entity.class,TagKey.class,TagKey.class);
  Method eye=c.getDeclaredMethod("isEyeInFluid",Entity.class,TagKey.class);
  height.setAccessible(true); eye.setAccessible(true);
  Entity e=new Entity();
  check(height,eye,e,0,false); // no level
  e.world=new Level(); check(height,eye,e,0,false); // mannequin: no chunk source
  e.world.chunks=new ChunkSource(); check(height,eye,e,0,false); // unloaded chunk
  e.world.chunks.chunk=new LevelChunk(); check(height,eye,e,0.75,true); // live fluid path
  e.world.chunks.chunk.fluid.empty=true; check(height,eye,e,0,false); // dry path
  System.out.println("PASS: 10 ACFluids checks (null level/chunks, unloaded chunk, fluid and dry paths)");
 }
 static void check(Method h,Method eye,Entity e,double expected,boolean submerged)throws Exception {
  if(Math.abs((double)h.invoke(null,e,null,null)-expected)>0.000001)throw new AssertionError("height");
  if((boolean)eye.invoke(null,e,new TagKey())!=submerged)throw new AssertionError("eye");
 }
}''',
}
with tempfile.TemporaryDirectory(prefix='ac-fluid-test-') as tmp:
    work = Path(tmp)
    for name, content in sources.items():
        target = work / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding='utf-8')
    classes = work / 'classes'
    subprocess.run([str(a.java_bin / 'javac.exe'), '--release', '25', '-d', str(classes),
                    *[str(work / name) for name in sources]], check=True)
    entry = 'com/github/alexmodguy/alexscaves/server/misc/ACFluids.class'
    with zipfile.ZipFile(a.jar) as jar:
        assert jar.testzip() is None, 'Archive CRC failure'
        target = classes / entry
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(jar.read(entry))
    subprocess.run([str(a.java_bin / 'java.exe'), '-Xverify:all', '-cp', str(classes),
                    'FluidPreviewTest'], check=True)
