package org.vivecraft.client_vr.menuworlds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class MenuWorldExporter {
	public static final int VERSION = 5;
	public static final int MIN_VERSION = 5;

	private static final Gson GSON = new Gson();

	public static byte[] saveArea(Level level, int xMin, int zMin, int xSize, int zSize, int ground) throws IOException {
		BlockStateMapper blockStateMapper = new BlockStateMapper();
		BiomeMapper biomeMapper = new BiomeMapper();

		int ySize = level.getHeight();
		int[] blocks = new int[xSize * ySize * zSize];
		byte[] skylightmap = new byte[xSize * ySize * zSize];
		byte[] blocklightmap = new byte[xSize * ySize * zSize];
		int[] biomemap = new int[(xSize * ySize * zSize) / 64];

		for (int x = xMin; x < xMin + xSize; x++) {
			int xl = x - xMin;
			for (int z = zMin; z < zMin + zSize; z++) {
				int zl = z - zMin;
				for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
					int yl = y - level.getMinBuildHeight();
					int index3 = (yl * zSize + zl) * xSize + xl;
					BlockPos pos3 = new BlockPos(x, y, z);
					BlockState state = level.getBlockState(pos3);
					blocks[index3] = blockStateMapper.getId(state);
					skylightmap[index3] = (byte)level.getBrightness(LightLayer.SKY, pos3);
					blocklightmap[index3] = (byte)level.getBrightness(LightLayer.BLOCK, pos3);

					if (x % 4 == 0 && y % 4 == 0 && z % 4 == 0) {
						int indexBiome = ((yl / 4) * (zSize / 4) + (zl / 4)) * (xSize / 4) + (xl / 4);
						// getNoiseBiome expects pre-divided coordinates
						biomemap[indexBiome] = biomeMapper.getId(level.getNoiseBiome(x / 4, y / 4, z / 4).value());
					}
				}
			}
		}
		
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(data);

		// store player rotation, so that the world can be rotated to where the player looked at
		dos.writeInt(switch (Minecraft.getInstance().player.getDirection()) {
			case SOUTH -> 180;
			case WEST -> -90;
			case EAST -> 90;
			default -> 0; // also NORTH
		});
		// is raining
		dos.writeBoolean(level.getRainLevel(0) > 0.0F);
		// is thunder
		dos.writeBoolean(level.getThunderLevel(0) > 0.0F);

		dos.writeInt(xSize);
		dos.writeInt(ySize);
		dos.writeInt(zSize);
		dos.writeInt(ground);

		// write the whole Dimension, easier to handle, than getting it from somewhere
		dos.writeUTF(DimensionSerializer.DimensionToJson(level.dimensionType()));

		if (level instanceof ServerLevel)
			dos.writeBoolean(((ServerLevel)level).isFlat());
		else
			dos.writeBoolean(((ClientLevel)level).getLevelData().isFlat);

		if (level instanceof ServerLevel)
			dos.writeLong(((ServerLevel)level).getSeed());
		else
			dos.writeLong(level.getBiomeManager().biomeZoomSeed); // not really correct :/

		blockStateMapper.writePalette(dos);
		biomeMapper.writeBiomes(dos);

		for (int i = 0; i < blocks.length; i++) {
			dos.writeInt(blocks[i]);
		}

		for (int i = 0; i < skylightmap.length; i++) {
			dos.writeByte(skylightmap[i] | (blocklightmap[i] << 4));
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

		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(data.toByteArray());
		deflater.finish();
		byte[] buffer = new byte[1048576];
		while (!deflater.finished()) {
			int len = deflater.deflate(buffer);
			output.write(buffer, 0, len);
		}
		
		return output.toByteArray();
	}
	
	public static void saveAreaToFile(Level level, int xMin, int zMin, int xSize, int zSize, int ground, File file) throws IOException {
		byte[] bytes = saveArea(level, xMin, zMin, xSize, zSize, ground);
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

		//facing direction
		int rotation = dis.readInt();

		// is raining
		boolean rain = dis.readBoolean();
		// is thunder
		boolean thunder = dis.readBoolean();

		int xSize = dis.readInt();
		int ySize = dis.readInt();
		int zSize = dis.readInt();
		int ground = dis.readInt();

		DimensionType dimensionType = DimensionSerializer.JsonToDimension(dis.readUTF());

		boolean isFlat;
		isFlat = dis.readBoolean();

		long seed = dis.readLong();

		BlockStateMapper blockStateMapper = new BlockStateMapper();
		blockStateMapper.readPalette(dis);
		BiomeMapper biomeMapper = new BiomeMapper();
		biomeMapper.readBiomes(dis);

		BlockState[] blocks = new BlockState[xSize * ySize * zSize];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = blockStateMapper.getState(dis.readInt());
		}
		short[][] heightmap = new short[xSize][zSize];
		for (int y = 0; y < ySize; y++) {
			int yIndex = y * zSize;
			for (int z = 0; z < zSize; z++) {
				int zIndex = (yIndex + z) * xSize;
				for (int x = 0; x < xSize; x++) {
					int index = zIndex + x;
					if (blocks[index].getMaterial().blocksMotion() || !blocks[index].getFluidState().isEmpty()) {
						heightmap[x][z] = (short)(y + 1);
					}
				}
			}
		}

		byte[] skylightmap = new byte[xSize * ySize * zSize];
		byte[] blocklightmap = new byte[xSize * ySize * zSize];
		for (int i = 0; i < skylightmap.length; i++) {
			int b = dis.readByte() & 0xFF;
			skylightmap[i] = (byte)(b & 15);
			blocklightmap[i] = (byte)(b >> 4);
		}

		Biome[] biomemap = new Biome[(xSize * ySize * zSize) / 64];
		for (int i = 0; i < biomemap.length; i++) {
			biomemap[i] = biomeMapper.getBiome(dis.readInt());
		}

		return new FakeBlockAccess(header.version, seed, blocks, skylightmap, blocklightmap, biomemap, heightmap, xSize, ySize, zSize, ground, dimensionType, isFlat, rotation, rain, thunder);
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

	// Just version for now, but could have future use
	public static class Header {
		public static final int SIZE = 8;

		public int version;
		public int uncompressedSize;

		public void read(DataInputStream dis) throws IOException {
			version = dis.readInt();
			uncompressedSize = dis.readInt();
		}

		public void write(DataOutputStream dos) throws IOException {
			dos.writeInt(version);
			dos.writeInt(uncompressedSize);
		}
	}

	private static class BlockStateMapper {
		CrudeIncrementalIntIdentityHashBiMap<BlockState> paletteMap = CrudeIncrementalIntIdentityHashBiMap.create(256);

		int getId(BlockState state) {
			int id = paletteMap.getId(state);
			if (id == -1) {
				return paletteMap.add(state);
			} else {
				return id;
			}
		}

		BlockState getState(int id) {
			return paletteMap.byId(id);
		}

		void readPalette(DataInputStream dis) throws IOException {
			paletteMap.clear();
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				CompoundTag tag = CompoundTag.TYPE.load(dis, 0, NbtAccounter.UNLIMITED);
				paletteMap.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag));
			}
		}

		void writePalette(DataOutputStream dos) throws IOException {
			dos.writeInt(paletteMap.size());
			for (int i = 0; i < paletteMap.size(); i++) {
				CompoundTag tag = NbtUtils.writeBlockState(paletteMap.byId(i));
				tag.write(dos);
			}
		}
	}

	private static class BiomeMapper {
		CrudeIncrementalIntIdentityHashBiMap<Biome> paletteMap = CrudeIncrementalIntIdentityHashBiMap.create(256);

		int getId(Biome biome) {
			int id = paletteMap.getId(biome);
			if (id == -1) {
				return paletteMap.add(biome);
			} else {
				return id;
			}
		}

		Biome getBiome(int id) {
			return paletteMap.byId(id);
		}

		void readBiomes(DataInputStream dis) throws IOException {
			paletteMap.clear();
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				paletteMap.add(BiomeSerializer.JsonToBiome(dis.readUTF()));
			}
		}

		void writeBiomes(DataOutputStream dos) throws IOException {
			dos.writeInt(paletteMap.size());
			for (int i = 0; i < paletteMap.size(); i++) {
				dos.writeUTF(BiomeSerializer.BiomeToJson(paletteMap.byId(i)));
			}
		}
	}

	private static class DimensionSerializer {
		public static String DimensionToJson(DimensionType object) {
			DataResult<JsonElement> dataResult = DimensionType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, object);
			JsonElement jsonElement = Util.getOrThrow(dataResult, string -> new EncoderException("Failed to encode: " + string + " " + object));
			return GSON.toJson(jsonElement);
		}

		public static DimensionType JsonToDimension(String json) {
			JsonElement jsonElement = GsonHelper.fromJson(GSON, json, JsonElement.class);
			// fix json, if it was not from this mc version
			jsonElement = fixJsonRead(jsonElement);
			DataResult<DimensionType> dataResult = DimensionType.DIRECT_CODEC.parse(JsonOps.INSTANCE, jsonElement);
			return Util.getOrThrow(dataResult, string -> new DecoderException("Failed to decode json: " + string));
		}

		private static JsonElement fixJsonRead(JsonElement jsonElement){
			// 1.18 doesn't have those
			if (!jsonElement.getAsJsonObject().has("monster_spawn_block_light_limit")){
				jsonElement.getAsJsonObject().add("monster_spawn_block_light_limit", new JsonPrimitive(0));
			}
			if (!jsonElement.getAsJsonObject().has("monster_spawn_light_level")){
				jsonElement.getAsJsonObject().add("monster_spawn_light_level", new JsonPrimitive(7));
			}
			return jsonElement;
		}
	}

	private static class BiomeSerializer {
		public static String BiomeToJson(Biome object) {
			DataResult<JsonElement> dataResult = Biome.NETWORK_CODEC.encodeStart(JsonOps.INSTANCE, object);
			JsonElement jsonElement = Util.getOrThrow(dataResult, string -> new EncoderException("Failed to encode: " + string + " " + object));
			return GSON.toJson(jsonElement);
		}

		public static Biome JsonToBiome(String json) {
			JsonElement jsonElement = GsonHelper.fromJson(GSON, json, JsonElement.class);
			// fix json, if it was not from this mc version
			jsonElement = fixJsonRead(jsonElement);
			DataResult<Biome> dataResult = Biome.NETWORK_CODEC.parse(JsonOps.INSTANCE, jsonElement);
			return Util.getOrThrow(dataResult, string -> new DecoderException("Failed to decode json: " + string));
		}

		private static JsonElement fixJsonRead(JsonElement jsonElement){
			try {
				Set<String> keys = jsonElement.getAsJsonObject().keySet();
				// remove biome mood_sound, not needed, and not cross version compatible
				if (keys.contains("effects")) {
					JsonObject effects = jsonElement.getAsJsonObject().getAsJsonObject("effects");
					// prevent concurrent modification exceptions
					for (String subKey : effects.keySet().toArray(new String[0])) {
						if (subKey.equals("mood_sound") || subKey.equals("additions_sound") || subKey.equals("music")) {
							// fix sound ids if they are wrong
							if (effects.get(subKey).getAsJsonObject().get("sound").isJsonPrimitive()) {
								String id = effects.get(subKey).getAsJsonObject().get("sound").getAsString();
								effects.get(subKey).getAsJsonObject().remove("sound");
								JsonObject object = new JsonObject();
								object.add("sound_id", new JsonPrimitive(id));
								effects.get(subKey).getAsJsonObject().add("sound", object);
							}
						} else if (subKey.equals("ambient_sound")) {
							// fix sound ids if they are wrong
							if (effects.get(subKey).isJsonPrimitive()) {
								String id = effects.get(subKey).getAsString();
								effects.remove(subKey);
								JsonObject object = new JsonObject();
								object.add("sound_id", new JsonPrimitive(id));
								effects.add(subKey, object);
							}
						}
					}
				}
				// fix legacy precipitation
				if (keys.contains("precipitation")) {
					jsonElement.getAsJsonObject().add("has_precipitation", new JsonPrimitive(!"none".equals(jsonElement.getAsJsonObject().get("precipitation").getAsString())));
					jsonElement.getAsJsonObject().remove("precipitation");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return jsonElement;
		}
	}
}
