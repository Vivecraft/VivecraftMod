package org.vivecraft.menuworlds;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FakeBlockAccess implements LevelReader
{
    private int version;
    private long seed;
    private DimensionType dimensionType;
    private boolean isFlat;
    private BlockState[] blocks;
    private byte[] skylightmap;
    private byte[] blocklightmap;
    private Biome[] biomemap;
    private int xSize;
    private int ySize;
    private int zSize;
    private int ground;
    private BiomeManager biomeManager;
    private DimensionSpecialEffects dimensionInfo;

    public FakeBlockAccess(int version, long seed, BlockState[] blocks, byte[] skylightmap, byte[] blocklightmap, Biome[] biomemap, int xSize, int ySize, int zSize, int ground, DimensionType dimensionType, boolean isFlat)
    {
        this.version = version;
        this.seed = seed;
        this.blocks = blocks;
        this.skylightmap = skylightmap;
        this.blocklightmap = blocklightmap;
        this.biomemap = biomemap;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.ground = ground;
        this.dimensionType = dimensionType;
        this.isFlat = isFlat;
        this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(seed), dimensionType.getBiomeZoomer());
        this.dimensionInfo = DimensionSpecialEffects.forType(dimensionType);
    }

    private int encodeCoords(int x, int z)
    {
        return z * this.xSize + x;
    }

    private int encodeCoords(int x, int y, int z)
    {
        return (y * this.zSize + z) * this.xSize + x;
    }

    private int encodeCoords(BlockPos pos)
    {
        return this.encodeCoords(pos.getX(), pos.getY(), pos.getZ());
    }

    private boolean checkCoords(int x, int y, int z)
    {
        return x >= 0 && y >= 0 && z >= 0 && x < this.xSize && y < this.ySize && z < this.xSize;
    }

    private boolean checkCoords(BlockPos pos)
    {
        return this.checkCoords(pos.getX(), pos.getY(), pos.getZ());
    }

    public int getGround()
    {
        return this.ground;
    }

    public int getXSize()
    {
        return this.xSize;
    }

    public int getYSize()
    {
        return this.ySize;
    }

    public int getZSize()
    {
        return this.zSize;
    }

    public long getSeed()
    {
        return this.seed;
    }

    public DimensionType dimensionType()
    {
        return this.dimensionType;
    }

    public DimensionSpecialEffects getDimensionReaderInfo()
    {
        return this.dimensionInfo;
    }

    public double getVoidFogYFactor()
    {
        return this.isFlat ? 1.0D : 0.03125D;
    }

    public double getHorizon()
    {
        return this.isFlat ? 0.0D : 63.0D;
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        if (!this.checkCoords(pPos))
        {
            return Blocks.BEDROCK.defaultBlockState();
        }
        else
        {
            BlockState blockstate = this.blocks[this.encodeCoords(pPos)];
            return blockstate != null ? blockstate : Blocks.AIR.defaultBlockState();
        }
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        return this.getBlockState(pPos).getFluidState();
    }

    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        return null;
    }

    public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver)
    {
        int i = Minecraft.getInstance().options.biomeBlendRadius;

        if (i == 0)
        {
            return pColorResolver.getColor(this.getBiome(pBlockPos), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
        }
        else
        {
            int j = (i * 2 + 1) * (i * 2 + 1);
            int k = 0;
            int l = 0;
            int i1 = 0;
            Cursor3D cursor3d = new Cursor3D(pBlockPos.getX() - i, pBlockPos.getY(), pBlockPos.getZ() - i, pBlockPos.getX() + i, pBlockPos.getY(), pBlockPos.getZ() + i);
            int j1;

            for (BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(); cursor3d.advance(); i1 += j1 & 255)
            {
                blockpos$mutableblockpos.set(cursor3d.nextX(), cursor3d.nextY(), cursor3d.nextZ());
                j1 = pColorResolver.getColor(this.getBiome(blockpos$mutableblockpos), (double)blockpos$mutableblockpos.getX(), (double)blockpos$mutableblockpos.getZ());
                k += (j1 & 16711680) >> 16;
                l += (j1 & 65280) >> 8;
            }

            return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
        }
    }

    public int getBrightness(LightLayer pLightType, BlockPos pBlockPos)
    {
        if (this.checkCoords(pBlockPos))
        {
            if (pLightType == LightLayer.SKY)
            {
                return this.dimensionType.hasSkyLight() ? this.skylightmap[this.encodeCoords(pBlockPos)] : 0;
            }
            else
            {
                return pLightType == LightLayer.BLOCK ? this.blocklightmap[this.encodeCoords(pBlockPos)] : pLightType.surrounding;
            }
        }
        else
        {
            return (pLightType != LightLayer.SKY || !this.dimensionType.hasSkyLight()) && pLightType != LightLayer.BLOCK ? 0 : pLightType.surrounding;
        }
    }

    public int getRawBrightness(BlockPos pBlockPos, int pAmount)
    {
        if (!this.checkCoords(pBlockPos.getX(), 0, pBlockPos.getZ()))
        {
            return 0;
        }
        else if (pBlockPos.getY() < 0)
        {
            return 0;
        }
        else if (pBlockPos.getY() >= 256)
        {
            int k = 15 - pAmount;

            if (k < 0)
            {
                k = 0;
            }

            return k;
        }
        else
        {
            int i = (this.dimensionType.hasSkyLight() ? this.skylightmap[this.encodeCoords(pBlockPos)] : 0) - pAmount;
            int j = this.blocklightmap[this.encodeCoords(pBlockPos)];

            if (j > i)
            {
                i = j;
            }

            return i;
        }
    }

    public float getShade(Direction p_45522_, boolean p_45523_)
    {
        boolean flag = this.dimensionInfo.constantAmbientLight();

        if (!p_45523_)
        {
            return flag ? 0.9F : 1.0F;
        }
        else
        {
            switch (p_45522_)
            {
                case DOWN:
                    return flag ? 0.9F : 0.5F;

                case UP:
                    return flag ? 0.9F : 1.0F;

                case NORTH:
                case SOUTH:
                    return 0.8F;

                case WEST:
                case EAST:
                    return 0.6F;

                default:
                    return 1.0F;
            }
        }
    }

    public boolean hasChunk(int pChunkX, int pChunkZ)
    {
        return this.checkCoords(pChunkX * 16, 0, pChunkZ * 16);
    }

    public ChunkAccess getChunk(int pChunkX, int pChunkZ, ChunkStatus p_46825_, boolean p_46826_)
    {
        return null;
    }

    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ)
    {
        return 0;
    }

    public BlockPos getHeightmapPos(Heightmap.Types pHeightmapType, BlockPos pPos)
    {
        return BlockPos.ZERO;
    }

    public int getSkyDarken()
    {
        return 0;
    }

    public WorldBorder getWorldBorder()
    {
        return new WorldBorder();
    }

    public boolean isUnobstructed(Entity pEntity, VoxelShape p_45751_)
    {
        return false;
    }

    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_45776_, AABB p_45777_, Predicate<Entity> p_45778_)
    {
        return null;
    }

    public boolean isEmptyBlock(BlockPos pPos)
    {
        return this.getBlockState(pPos).isAir();
    }

    public Biome getNoiseBiome(int pX, int pY, int pZ)
    {
        if (!this.checkCoords(pX * 4, pY * 4, pZ * 4))
        {
            pX = Mth.clamp(pX, 0, this.xSize / 4 - 1);
            pY = Mth.clamp(pY, 0, this.ySize / 4 - 1);
            pZ = Mth.clamp(pZ, 0, this.zSize / 4 - 1);
        }

        return this.biomemap[(pY * (this.zSize / 4) + pZ) * (this.xSize / 4) + pX];
    }

    public int getDirectSignal(BlockPos pPos, Direction pDirection)
    {
        return 0;
    }

    public boolean isClientSide()
    {
        return false;
    }

    public int getSeaLevel()
    {
        return 63;
    }

    public LevelLightEngine getLightEngine()
    {
        return null;
    }

    public BiomeManager getBiomeManager()
    {
        return this.biomeManager;
    }

    public Biome getUncachedNoiseBiome(int pX, int pY, int pZ)
    {
        return null;
    }
}
