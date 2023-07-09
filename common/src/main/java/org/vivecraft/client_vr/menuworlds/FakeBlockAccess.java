package org.vivecraft.client_vr.menuworlds;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.*;
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

public class FakeBlockAccess implements LevelReader {
	private int version;
	private long seed;
	private DimensionType dimensionType;
	private boolean isFlat;
	private BlockState[] blocks;
	private byte[] skylightmap;
	private byte[] blocklightmap;
	private Biome[] biomemap;

	private short[][] heightmap;
	private int xSize;
	private int ySize;
	private int zSize;
	private float ground;

	// same as ground, but includes an optional vertical view offset
	public float effectiveGround;

	private float rotation;
	private boolean rain;
	private boolean thunder;

	private BiomeManager biomeManager;
	private DimensionSpecialEffects dimensionInfo;
	
	public FakeBlockAccess(int version, long seed, BlockState[] blocks, byte[] skylightmap, byte[] blocklightmap, Biome[] biomemap, short[][] heightmap, int xSize, int ySize, int zSize, int ground, DimensionType dimensionType, boolean isFlat, float rotation, boolean rain, boolean thunder) {
		this.version = version;
		this.seed = seed;
		this.blocks = blocks;
		this.skylightmap = skylightmap;
		this.blocklightmap = blocklightmap;
		this.biomemap = biomemap;
		this.heightmap = heightmap;
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
		this.ground = ground - dimensionType.minY();
		this.dimensionType = dimensionType;
		this.isFlat = isFlat;

		this.rotation = rotation;
		this.rain = rain;
		this.thunder = thunder;

		this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(seed));
		this.dimensionInfo = DimensionSpecialEffects.forType(dimensionType);

		// set the ground to the height of the center block
		BlockPos pos = new BlockPos(0, (int)this.ground, 0);
		BlockState standing = blocks[encodeCoords(pos)];
		this.ground += Math.max(standing.getCollisionShape(this, pos).max(Direction.Axis.Y), 0.0);
		this.effectiveGround = this.ground;
	}
	
	private int encodeCoords(int x, int z) {
		return z * xSize + x;
	}
	
	private int encodeCoords(int x, int y, int z) {
		return ((y+(int)effectiveGround) * zSize + (z+zSize/2)) * xSize + (x+xSize/2);
	}

	private int encodeCoords(BlockPos pos) {
		return encodeCoords(pos.getX(), pos.getY(), pos.getZ());
	}
	
	private boolean checkCoords(int x, int y, int z) {
		return x >= -xSize/2 && y >= -(int)effectiveGround && z >= -zSize/2 && x < xSize/2 && y < ySize-(int)effectiveGround && z < zSize/2;
	}

	private boolean checkCoords(BlockPos pos) {
		return checkCoords(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public float getGround() {
		return effectiveGround;
	}

	public void setGroundOffset(float offset) {
		effectiveGround = ground + offset;
	}
	
	public int getXSize() {
		return xSize;
	}

	public int getYSize() {
		return ySize;
	}

	public int getZSize() {
		return zSize;
	}

	public long getSeed() {
		return seed;
	}

	public float getRotation() {
		return rotation;
	}
	public boolean getRain() {
		return rain;
	}
	public boolean getThunder() {
		return thunder;
	}

	@Override
	public DimensionType dimensionType() {
		return dimensionType;
	}

	public DimensionSpecialEffects getDimensionReaderInfo() {
		return dimensionInfo;
	}

	public double getVoidFogYFactor() {
		return isFlat ? 1.0D : 0.03125D;
	}

	public double getHorizon() {
		return isFlat ? -effectiveGround : 63.0D-effectiveGround+getMinBuildHeight();
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (!checkCoords(pos))
			return Blocks.BEDROCK.defaultBlockState();

		BlockState state = blocks[encodeCoords(pos)];
		return state != null ? state : Blocks.AIR.defaultBlockState();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null; // You're a funny guy, I kill you last
	}

	@Override
	public int getBlockTint(BlockPos blockPosIn, ColorResolver colorResolverIn) {
		int i = Minecraft.getInstance().options.biomeBlendRadius().get();

		if (i == 0)
		{
			return colorResolverIn.getColor(this.getBiome(blockPosIn).value(), blockPosIn.getX(), blockPosIn.getZ());
		}
		else
		{
			int j = (i * 2 + 1) * (i * 2 + 1);
			int k = 0;
			int l = 0;
			int i1 = 0;
			Cursor3D cursor3D = new Cursor3D(blockPosIn.getX() - i, blockPosIn.getY(), blockPosIn.getZ() - i, blockPosIn.getX() + i, blockPosIn.getY(), blockPosIn.getZ() + i);
			int j1;

			for (BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(); cursor3D.advance(); i1 += j1 & 255)
			{
				blockpos$mutable.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
				j1 = colorResolverIn.getColor(this.getBiome(blockpos$mutable).value(), (double)blockpos$mutable.getX(), (double)blockpos$mutable.getZ());
				k += (j1 & 16711680) >> 16;
				l += (j1 & 65280) >> 8;
			}

			return (k / j & 255) << 16 | (l / j & 255) << 8 | i1 / j & 255;
		}
	}

	@Override
	public int getBrightness(LightLayer type, BlockPos pos) {
		if (!checkCoords(pos))
			return (type != LightLayer.SKY || !this.dimensionType.hasSkyLight()) && type != LightLayer.BLOCK ? 0 : type.surrounding;

		if (type == LightLayer.SKY)
			return this.dimensionType.hasSkyLight() ? skylightmap[encodeCoords(pos)] : 0;
		else
			return type == LightLayer.BLOCK ? blocklightmap[encodeCoords(pos)] : type.surrounding;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		if (!checkCoords(pos.getX(), 0, pos.getZ()))
			return 0;

		if (pos.getY() < 0) {
			return 0;
		} else if (pos.getY() >= 256) {
			int light = 15 - amount;
			if (light < 0)
				light = 0;
			return light;
		} else {
			int light = (this.dimensionType.hasSkyLight() ? skylightmap[encodeCoords(pos)] : 0) - amount;
			int blockLight = blocklightmap[encodeCoords(pos)];

			if (blockLight > light)
				light = blockLight;
			return light;
		}
	}

	@Override
	public float getShade(Direction face, boolean shade) {
		boolean flag = this.dimensionInfo.constantAmbientLight(); // isNether?? yeah mate nice hard-coding

		if (!shade) {
			return flag ? 0.9F : 1.0F;
		}
		else {
			switch (face) {
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

	@Override
	public boolean hasChunk(int x, int z) {
		return checkCoords(x * 16, 0, z * 16); // :thonk:
	}

	@Override
	public ChunkAccess getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
		return null; // �\_(?)_/�
	}

	@Override
	public int getHeight(Heightmap.Types heightmapType, int x, int z) {
		if (heightmapType == Heightmap.Types.MOTION_BLOCKING) {
			return getHeightBlocking(x, z);
		}
		return 0; // �\_(?)_/�
	}

	public int getHeightBlocking(int x, int z) {
		return heightmap[x+xSize/2][z+zSize/2] - (int)effectiveGround;
	}

	@Override
	public BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
		return BlockPos.ZERO; // �\_(?)_/�
	}

	@Override
	public int getSkyDarken() {
		return 0; // idk this is just what RenderChunkCache does
	}

	@Override
	public WorldBorder getWorldBorder() {
		return new WorldBorder();
	}

	@Override
	public boolean isUnobstructed(Entity entityIn, VoxelShape shape) {
		return false; // ???
	}

	@Override
	public List<VoxelShape> getEntityCollisions(@Nullable Entity entityIn, AABB aabb) {
		return Collections.emptyList(); // nani
	}

	@Override
	public boolean isEmptyBlock(BlockPos pos) {
		return this.getBlockState(pos).isAir();
	}

	@Override
	public Holder<Biome> getNoiseBiome(int x, int y, int z) {
		int xMoved = x + xSize/8;
		int yMoved = y + (int)effectiveGround/4;
		int zMoved = z + zSize/8;
		if (!checkCoords(x * 4, y * 4, z * 4)) {
			xMoved = Mth.clamp(xMoved, 0, xSize / 4 - 1);
			yMoved = Mth.clamp(yMoved, 0, (ySize-(int)effectiveGround) / 4 - 1);
			zMoved = Mth.clamp(zMoved, 0, zSize / 4 - 1);
		}
		return Holder.direct(biomemap[(yMoved * (zSize / 4) + zMoved) * (xSize / 4) + xMoved]);
	}

	@Override
	public int getDirectSignal(BlockPos pos, Direction direction) {
		return 0;
	}

	@Override
	public boolean isClientSide() {
		return false;
	}

	@Override
	public int getSeaLevel() {
		return (int)(63-effectiveGround+getMinBuildHeight()); // magic number
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return null; // uh?
	}

	@Override
	public BiomeManager getBiomeManager() {
		return biomeManager;
	}

	@Override
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
		return null; // don't need this
	}
}
