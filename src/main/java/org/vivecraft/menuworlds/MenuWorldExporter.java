package org.vivecraft.menuworlds;

import com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

import org.vivecraft.reflection.MCReflection;

public class MenuWorldExporter {
	public static final int VERSION = 5;
	public static final int MIN_VERSION = 2;

	public static byte[] saveArea(Level world, int xMin, int zMin, int xSize, int zSize, int ground) throws IOException {
		BlockStateMapper mapper = new BlockStateMapper();

		int yMin = world.getMinBuildHeight();
		int ySize = world.getMaxBuildHeight() - yMin;
		int[] blocks = new int[xSize * ySize * zSize];
		byte[] skylightmap = new byte[xSize * ySize * zSize];
		byte[] blocklightmap = new byte[xSize * ySize * zSize];
		int[] biomemap = new int[(xSize * ySize * zSize) / 64];
		for (int x = xMin; x < xMin + xSize; x++) {
			int xl = x - xMin;
			for (int z = zMin; z < zMin + zSize; z++) {
				int zl = z - zMin;
				for (int y = yMin; y < yMin + ySize; y++) {
                    int yl = y - yMin;
					int index3 = (yl * zSize + zl) * xSize + xl;
					BlockPos pos3 = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos3);
					blocks[index3] = mapper.getId(state);
					skylightmap[index3] = (byte)world.getBrightness(LightLayer.SKY, pos3);
					blocklightmap[index3] = (byte)world.getBrightness(LightLayer.BLOCK, pos3);

					if (x % 4 == 0 && y % 4 == 0 && z % 4 == 0) {
						int indexBiome = ((yl / 4) * (zSize / 4) + (zl / 4)) * (xSize / 4) + (xl / 4);
						// getNoiseBiome expects pre-divided coordinates
						biomemap[indexBiome] = BuiltinRegistries.BIOME.getId(world.getNoiseBiome(x / 4, y / 4, z / 4));
					}
				}
			}
		}

		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(data);
		dos.writeInt(xSize);
		dos.writeInt(ySize);
		dos.writeInt(zSize);
		dos.writeInt(ground - yMin);
		dos.writeUTF(world.dimensionType().effectsLocation().toString());

		if (world instanceof ServerLevel)
			dos.writeBoolean(((ServerLevel)world).isFlat());
		else
			dos.writeBoolean((boolean)MCReflection.ClientWorldInfo_isFlat.get(world.getLevelData()));

		dos.writeBoolean(world.dimensionType().hasSkyLight()); // technically not needed now but keeping it just in case

		if (world instanceof ServerLevel)
			dos.writeLong(((ServerLevel)world).getSeed());
		else
			dos.writeLong((long)MCReflection.BiomeManager_seed.get(world.getBiomeManager())); // not really correct :/

		mapper.writePalette(dos);

		for (int i = 0; i < blocks.length; i++) {
			dos.writeInt(blocks[i]);
		}

		for (int i = 0; i < skylightmap.length; i++) {
			dos.writeByte(skylightmap[i] | blocklightmap[i] << 4);
		}

		for (int i = 0; i < biomemap.length; i++) {
			dos.writeInt(biomemap[i]);
		}

		Header header = new Header();
		header.version = VERSION;
		header.uncompressedSize = data.size();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		DataOutputStream headerStream = new DataOutputStream(output);
		header.write(headerStream);

		Deflater deflater = new Deflater(9);
		deflater.setInput(data.toByteArray());
		deflater.finish();
		byte[] buffer = new byte[1048576];
		while (!deflater.finished()) {
			int len = deflater.deflate(buffer);
			output.write(buffer, 0, len);
		}

		return output.toByteArray();
	}

	public static void saveAreaToFile(Level world, int xMin, int zMin, int xSize, int zSize, int ground, File file) throws IOException {
		byte[] bytes = saveArea(world, xMin, zMin, xSize, zSize, ground);
		Files.write(bytes, file);
	}

	public static FakeBlockAccess loadWorld(byte[] data) throws IOException, DataFormatException {
		Header header = new Header();
		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
			header.read(dis);
		}
		if (header.version > VERSION || header.version < MIN_VERSION)
			throw new DataFormatException("Unsupported menu world version: " + header.version);

		Inflater inflater = new Inflater();
		inflater.setInput(data, Header.SIZE, data.length - Header.SIZE);
		ByteArrayOutputStream output = new ByteArrayOutputStream(header.uncompressedSize);
		byte[] buffer = new byte[1048576];
		while (!inflater.finished()) {
			int len = inflater.inflate(buffer);
			output.write(buffer, 0, len);
		}

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(output.toByteArray()));
		int xSize = dis.readInt();
		int ySize = dis.readInt();
		int zSize = dis.readInt();
		int ground = dis.readInt();

		ResourceLocation dimName;
		if (header.version < 4) { // old format
			int dimId = dis.readInt();
			switch (dimId) {
				case -1:
					dimName = new ResourceLocation("the_nether");
					break;
				case 1:
					dimName = new ResourceLocation("the_end");
					break;
				default:
					dimName = new ResourceLocation("overworld");
					break;
			}
		} else {
			dimName = new ResourceLocation(dis.readUTF());
		}

		Registry<DimensionType> dimRegistry = RegistryAccess.builtin().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
		ResourceKey<DimensionType> dimKey = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, dimName);
		DimensionType dimensionType = dimRegistry.get(dimKey);

		if (dimensionType == null)
			dimensionType = dimRegistry.get(DimensionType.OVERWORLD_LOCATION);

		boolean isFlat;

		if (header.version < 4) // old format
			isFlat = dis.readUTF().equals("flat");
		else
			isFlat = dis.readBoolean();

		dis.readBoolean(); // legacy hasSkyLight

		long seed = 0;
		if (header.version >= 3)
			seed = dis.readLong();

		BlockStateMapper mapper = new BlockStateMapper();
		mapper.readPalette(dis);

		BlockState[] blocks = new BlockState[xSize * ySize * zSize];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = mapper.getState(dis.readInt());
		}

		byte[] skylightmap = new byte[xSize * ySize * zSize];
		byte[] blocklightmap = new byte[xSize * ySize * zSize];
		for (int i = 0; i < skylightmap.length; i++) {
			int b = dis.readByte() & 0xFF;
			skylightmap[i] = (byte)(b & 15);
			blocklightmap[i] = (byte)(b >> 4);
		}

		Biome[] biomemap = new Biome[xSize * ySize * zSize / 64];
		if (header.version == 2) {
			Biome[] tempBiomemap = new Biome[xSize * zSize];
			for (int i = 0; i < tempBiomemap.length; i++) {
				tempBiomemap[i] = getBiome(dis.readInt());
			}
			for (int x = 0; x < xSize / 4; x++) {
				for (int z = 0; z < zSize / 4; z++) {
					biomemap[z * (xSize / 4) + x] = tempBiomemap[(z * 4) * xSize + (x * 4)];
				}
			}
			int yStride = (xSize / 4) * (zSize / 4);
			for (int y = 1; y < ySize / 4; y++) {
				System.arraycopy(biomemap, 0, biomemap, yStride * y, yStride);
			}
		} else {
			for (int i = 0; i < biomemap.length; i++) {
				biomemap[i] = getBiome(dis.readInt());
			}
		}

		return new FakeBlockAccess(header.version, seed, blocks, skylightmap, blocklightmap, biomemap, xSize, ySize, zSize, ground, dimensionType, isFlat);
	}

	public static FakeBlockAccess loadWorld(InputStream is) throws IOException, DataFormatException {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		byte[] buffer = new byte[1048576];
		int count;
		while ((count = is.read(buffer)) != -1) {
			data.write(buffer, 0, count);
		}
		return loadWorld(data.toByteArray());
	}

	public static int readVersion(File file) throws IOException {
		try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
			Header header = new Header();
			header.read(dis);
			return header.version;
		}
	}

	private static Biome getBiome(int biomeId) {
		Biome biome = BuiltinRegistries.BIOME.byId(biomeId);
		return biome != null ? biome : BuiltinRegistries.BIOME.get(Biomes.PLAINS);
	}

	public static class Header {
		public static final int SIZE = 8;

		public int version;
		public int uncompressedSize;

		public void read(DataInputStream dis) throws IOException {
			this.version = dis.readInt();
			this.uncompressedSize = dis.readInt();
		}

		public void write(DataOutputStream dos) throws IOException {
			dos.writeInt(this.version);
			dos.writeInt(this.uncompressedSize);
		}
	}

	private static class BlockStateMapper {
		//CrudeIncrementalIntIdentityHashBiMap<BlockState> paletteMap = new CrudeIncrementalIntIdentityHashBiMap<BlockState>(256);

		private BlockStateMapper() {
		}

		int getId(BlockState state) {
			return 0;
//			int id = this.paletteMap.getId(state);
//			return id == -1 ? this.paletteMap.add(state) : id;
		}

		BlockState getState(int id) {
			return null;
//			return this.paletteMap.byId(id);
		}

		void readPalette(DataInputStream dis) throws IOException {
//			this.paletteMap.clear();
//			int size = dis.readInt();
//
//			for (int i = 0; i < size; i++) {
//				CompoundTag compoundtag = CompoundTag.TYPE.load(dis, 0, NbtAccounter.UNLIMITED);
//				this.paletteMap.add(NbtUtils.readBlockState(compoundtag));
//			}
		}

		void writePalette(DataOutputStream dos) throws IOException {
//			dos.writeInt(this.paletteMap.size());
//
//			for (int i = 0; i < this.paletteMap.size(); i++) {
//				CompoundTag compoundtag = NbtUtils.writeBlockState(this.paletteMap.byId(i));
//				compoundtag.write(dos);
//			}
		}
	}
}
