package org.vivecraft.client_vr.menuworlds;

import com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.vivecraft.client_vr.settings.VRSettings;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.vivecraft.client.Xplat;

public class MenuWorldExporter {
	public static final int VERSION = 5;
	public static final int MIN_VERSION = 2;

	private static final DataFixer dataFixer = DataFixers.getDataFixer();

	public static byte[] saveArea(Level level, int xMin, int zMin, int xSize, int zSize, int ground) throws IOException {
		BlockStateMapper blockStateMapper = new BlockStateMapper();
		PaletteBiomeMapper biomeMapper = new PaletteBiomeMapper();

		int yMin = level.getMinBuildHeight();
		int ySize = level.getHeight();
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
		dos.writeInt(xSize);
		dos.writeInt(ySize);
		dos.writeInt(zSize);
		dos.writeInt(ground);
		dos.writeUTF(level.dimensionType().effectsLocation().toString());

		if (level instanceof ServerLevel)
			dos.writeBoolean(((ServerLevel)level).isFlat());
		else
			dos.writeBoolean(((ClientLevel)level).getLevelData().isFlat);

		dos.writeBoolean(level.dimensionType().hasSkyLight());

		if (level instanceof ServerLevel)
			dos.writeLong(((ServerLevel)level).getSeed());
		else
			dos.writeLong(level.getBiomeManager().biomeZoomSeed); // not really correct :/

		dos.writeInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion());

		dos.writeBoolean(level.dimensionType().fixedTime().isPresent());
		if (level.dimensionType().fixedTime().isPresent())
			dos.writeLong(level.dimensionType().fixedTime().getAsLong());
		dos.writeBoolean(level.dimensionType().hasCeiling());
		dos.writeInt(level.dimensionType().minY());
		dos.writeFloat(level.dimensionType().ambientLight());

		dos.writeFloat(switch (Minecraft.getInstance().player.getDirection()) {
			case SOUTH -> 180.0f;
			case WEST -> -90.0f;
			case EAST -> 90.0f;
			default -> 0.0f; // also NORTH
		});

		dos.writeBoolean(level.getRainLevel(1.0f) > 0.0f);
		dos.writeBoolean(level.getThunderLevel(1.0f) > 0.0f);

		blockStateMapper.writePalette(dos);
		biomeMapper.writePalette(dos, level.registryAccess());

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
			dimName = switch (dimId) {
				case -1 -> BuiltinDimensionTypes.NETHER_EFFECTS;
				case 1 -> BuiltinDimensionTypes.END_EFFECTS;
				default -> BuiltinDimensionTypes.OVERWORLD_EFFECTS;
			};
		} else {
			dimName = new ResourceLocation(dis.readUTF());
		}

		boolean isFlat;

		if (header.version < 4) // old format
			isFlat = dis.readUTF().equals("flat");
		else
			isFlat = dis.readBoolean();

		boolean dimHasSkyLight = dis.readBoolean();

		long seed = 0;
		if (header.version >= 3)
			seed = dis.readLong();

		int dataVersion;
		if (header.version == 2)
			dataVersion = 1631; // assume 1.13.2
		else if (header.version == 3)
			dataVersion = 2230; // assume 1.15.2
		else if (header.version == 4)
			dataVersion = 2586; // assume 1.16.5
		else
			dataVersion = dis.readInt(); // v5+ stores the real data version

		if (dataVersion > SharedConstants.getCurrentVersion().getDataVersion().getVersion())
			VRSettings.logger.warn("Data version is newer than current, this menu world may not load correctly.");

		OptionalLong dimFixedTime = OptionalLong.empty();
		boolean dimHasCeiling;
		int dimMinY;
		float dimAmbientLight;

		if (header.version < 5) { // fill in missing values
			if (BuiltinDimensionTypes.NETHER_EFFECTS.equals(dimName)) {
				dimFixedTime = OptionalLong.of(18000L);
				dimHasCeiling = true;
				dimMinY = 0;
				dimAmbientLight = 0.1f;
			} else if (BuiltinDimensionTypes.END_EFFECTS.equals(dimName)) {
				dimFixedTime = OptionalLong.of(6000L);
				dimHasCeiling = false;
				dimMinY = 0;
				dimAmbientLight = 0.0f;
			} else { // overworld/default
				dimHasCeiling = false;
				dimMinY = 0; // pre-v5 worlds don't have deeper underground
				dimAmbientLight = 0.0f;
			}
		} else {
			if (dis.readBoolean())
				dimFixedTime = OptionalLong.of(dis.readLong());
			dimHasCeiling = dis.readBoolean();
			dimMinY = dis.readInt();
			dimAmbientLight = dis.readFloat();
		}

		DimensionType dimensionType = new DimensionType(dimFixedTime, dimHasSkyLight, dimHasCeiling, false, false, 1.0, true, false, dimMinY, ySize, ySize, BlockTags.INFINIBURN_OVERWORLD, dimName, dimAmbientLight, new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0));

		float rotation = 0.0f;
		boolean rain = false;
		boolean thunder = false;

		if (header.version >= 5) {
			rotation = dis.readFloat();
			rain = dis.readBoolean();
			thunder = dis.readBoolean();
		}

		BlockStateMapper blockStateMapper = new BlockStateMapper();
		blockStateMapper.readPalette(dis, dataVersion);

		BiomeMapper biomeMapper;
		if (header.version >= 5) {
			biomeMapper = new PaletteBiomeMapper();
			((PaletteBiomeMapper)biomeMapper).readPalette(dis);
		} else {
			biomeMapper = new LegacyBiomeMapper();
		}

		BlockState[] blocks = new BlockState[xSize * ySize * zSize];
		for (int i = 0; i < blocks.length; i++) {
			blocks[i] = blockStateMapper.getState(dis.readInt());
		}

		short[][] heightmap = new short[xSize][zSize];
		for (int x = 0; x < xSize; x++) {
			for (int z = 0; z < zSize; z++) {
				for (int y = ySize - 1; y >= 0; y--) {
					int index = (y * zSize + z) * xSize + x;
					if (blocks[index].getMaterial().blocksMotion() || !blocks[index].getFluidState().isEmpty()) {
						heightmap[x][z] = (short)(y + 1);
						break;
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

		Biome[] biomemap = new Biome[xSize * ySize * zSize / 64];
		if (header.version == 2) {
			Biome[] tempBiomemap = new Biome[xSize * zSize];
			for (int i = 0; i < tempBiomemap.length; i++) {
				tempBiomemap[i] = biomeMapper.getBiome(dis.readInt());
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
				biomemap[i] = biomeMapper.getBiome(dis.readInt());
			}
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

	private static <T extends Enum & StringRepresentable> Optional<T> decodeEnum(Class<T> type, String input) {
		return Arrays.stream(type.getEnumConstants()).filter(e -> input.equals(((StringRepresentable)e).getSerializedName())).findFirst();
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
		CrudeIncrementalIntIdentityHashBiMap<BlockState> paletteMap = CrudeIncrementalIntIdentityHashBiMap.create(256);

		private BlockStateMapper() {
		}

		int getId(BlockState state) {
			int id = this.paletteMap.getId(state);
			return id == -1 ? this.paletteMap.add(state) : id;
		}

		BlockState getState(int id) {
			return this.paletteMap.byId(id);
		}

		void readPalette(DataInputStream dis, int dataVersion) throws IOException {
			this.paletteMap.clear();
			int size = dis.readInt();

			for (int i = 0; i < size; i++) {
				CompoundTag tag = CompoundTag.TYPE.load(dis, 0, NbtAccounter.UNLIMITED);
				tag = (CompoundTag)dataFixer.update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, tag), dataVersion, SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getValue();
				this.paletteMap.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag));
			}
		}

		void writePalette(DataOutputStream dos) throws IOException {
			dos.writeInt(this.paletteMap.size());

			for (int i = 0; i < this.paletteMap.size(); i++) {
				CompoundTag compoundtag = NbtUtils.writeBlockState(this.paletteMap.byId(i));
				compoundtag.write(dos);
			}
		}
	}

	private interface BiomeMapper {
		int getId(Biome biome);
		Biome getBiome(int id);
	}

	private static final BiomeGenerationSettings dummyGenerationSettings = new BiomeGenerationSettings.PlainBuilder().build();
	private static final MobSpawnSettings dummyMobSpawnSettings = new MobSpawnSettings.Builder().build();

	private static class PaletteBiomeMapper implements BiomeMapper {
		CrudeIncrementalIntIdentityHashBiMap<Biome> paletteMap = CrudeIncrementalIntIdentityHashBiMap.create(256);

		private PaletteBiomeMapper() {
		}

		@Override
		public int getId(Biome biome) {
			int id = this.paletteMap.getId(biome);
			return id == -1 ? this.paletteMap.add(biome) : id;
		}

		@Override
		public Biome getBiome(int id) {
			return this.paletteMap.byId(id);
		}

		void readPalette(DataInputStream dis) throws IOException {
			this.paletteMap.clear();
			int size = dis.readInt();

			for (int i = 0; i < size; i++) {
				Biome.BiomeBuilder builder = new Biome.BiomeBuilder();

				dis.readUTF(); // registry key, not actually used though, just for reference

				builder.hasPrecipitation(dis.readBoolean());
				builder.temperature(dis.readFloat());
				decodeEnum(Biome.TemperatureModifier.class, dis.readUTF()).ifPresent(builder::temperatureAdjustment);
				builder.downfall(dis.readFloat());

				BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder();
				effectsBuilder.fogColor(dis.readInt());
				effectsBuilder.waterColor(dis.readInt());
				effectsBuilder.waterFogColor(dis.readInt());
				effectsBuilder.skyColor(dis.readInt());

				if (dis.readBoolean())
					effectsBuilder.foliageColorOverride(dis.readInt());

				if (dis.readBoolean())
					effectsBuilder.grassColorOverride(dis.readInt());

				decodeEnum(BiomeSpecialEffects.GrassColorModifier.class, dis.readUTF()).ifPresent(effectsBuilder::grassColorModifier);

				if (dis.readBoolean()) {
					ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(dis.readUTF()));
					float probability = dis.readFloat();
					if (particleType instanceof ParticleOptions)
						effectsBuilder.ambientParticle(new AmbientParticleSettings((ParticleOptions)particleType, probability));
				}

				Biome biome = builder.specialEffects(effectsBuilder.build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build();
				this.paletteMap.add(biome);
			}
		}

		void writePalette(DataOutputStream dos, RegistryAccess registryAccess) throws IOException {
			dos.writeInt(this.paletteMap.size());

			for (int i = 0; i < this.paletteMap.size(); i++) {
				Biome biome = this.paletteMap.byId(i);

				dos.writeUTF(registryAccess.registryOrThrow(Registries.BIOME).getKey(biome).toString());

				Biome.ClimateSettings climateSettings = Xplat.getBiomeClimateSettings(biome);

				dos.writeBoolean(climateSettings.hasPrecipitation());
				dos.writeFloat(climateSettings.temperature());
				dos.writeUTF(climateSettings.temperatureModifier().getSerializedName());
				dos.writeFloat(climateSettings.downfall());

				BiomeSpecialEffects specialEffects = Xplat.getBiomeEffects(biome);

				dos.writeInt(specialEffects.getFogColor());
				dos.writeInt(specialEffects.getWaterColor());
				dos.writeInt(specialEffects.getWaterFogColor());
				dos.writeInt(specialEffects.getSkyColor());

				dos.writeBoolean(specialEffects.getFoliageColorOverride().isPresent());
				if (specialEffects.getFoliageColorOverride().isPresent())
					dos.writeInt(specialEffects.getFoliageColorOverride().get());

				dos.writeBoolean(specialEffects.getGrassColorOverride().isPresent());
				if (specialEffects.getGrassColorOverride().isPresent())
					dos.writeInt(specialEffects.getGrassColorOverride().get());

				dos.writeUTF(specialEffects.getGrassColorModifier().getSerializedName());

				dos.writeBoolean(specialEffects.getAmbientParticleSettings().isPresent());
				if (specialEffects.getAmbientParticleSettings().isPresent()) {
					AmbientParticleSettings ambientParticleSettings = specialEffects.getAmbientParticleSettings().get();
					dos.writeUTF(BuiltInRegistries.PARTICLE_TYPE.getKey(ambientParticleSettings.getOptions().getType()).toString());
					dos.writeFloat(ambientParticleSettings.probability);
				}
			}
		}
	}

	private static class LegacyBiomeMapper implements BiomeMapper {
		private static final Map<Integer, Biome> map = new HashMap<>();
		static {
			// big hard-coded map of biomes from 1.16
			// the commented line here is just a builder reference
			//map.put(0, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(1).temperatureAdjustment(Biome.TemperatureModifier.NONE).downfall(1).specialEffects(new BiomeSpecialEffects.Builder().fogColor(1).waterColor(1).waterFogColor(1).skyColor(1).foliageColorOverride(1).grassColorOverride(1).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.NONE).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation("")), 0)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());

			// plains
			map.put(1, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7907327).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// the_void
			map.put(127, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// ocean
			map.put(0, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// desert
			map.put(2, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// mountains
			map.put(3, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// forest
			map.put(4, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7972607).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// taiga
			map.put(5, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233983).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// swamp
			map.put(6, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(6388580).waterFogColor(2302743).skyColor(7907327).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// river
			map.put(7, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// nether_wastes
			map.put(8, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(3344392).waterColor(4159204).waterFogColor(329011).skyColor(7254527).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// the_end
			map.put(9, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(10518688).waterColor(4159204).waterFogColor(329011).skyColor(0).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// frozen_ocean
			map.put(10, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(3750089).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// frozen_river
			map.put(11, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(3750089).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_tundra
			map.put(12, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_mountains
			map.put(13, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// mushroom_fields
			map.put(14, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.900000f).downfall(1.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// mushroom_field_shore
			map.put(15, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.900000f).downfall(1.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// beach
			map.put(16, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7907327).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// desert_hills
			map.put(17, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// wooded_hills
			map.put(18, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7972607).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// taiga_hills
			map.put(19, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233983).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// mountain_edge
			map.put(20, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// jungle
			map.put(21, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// jungle_hills
			map.put(22, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// jungle_edge
			map.put(23, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// deep_ocean
			map.put(24, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// stone_shore
			map.put(25, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_beach
			map.put(26, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.050000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// birch_forest
			map.put(27, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8037887).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// birch_forest_hills
			map.put(28, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8037887).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// dark_forest
			map.put(29, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7972607).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_taiga
			map.put(30, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8625919).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_taiga_hills
			map.put(31, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8625919).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// giant_tree_taiga
			map.put(32, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.300000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8168447).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// giant_tree_taiga_hills
			map.put(33, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.300000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8168447).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// wooded_mountains
			map.put(34, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// savanna
			map.put(35, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.200000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7711487).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// savanna_plateau
			map.put(36, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7776511).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// badlands
			map.put(37, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// wooded_badlands_plateau
			map.put(38, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// badlands_plateau
			map.put(39, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// small_end_islands
			map.put(40, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(10518688).waterColor(4159204).waterFogColor(329011).skyColor(0).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// end_midlands
			map.put(41, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(10518688).waterColor(4159204).waterFogColor(329011).skyColor(0).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// end_highlands
			map.put(42, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(10518688).waterColor(4159204).waterFogColor(329011).skyColor(0).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// end_barrens
			map.put(43, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(10518688).waterColor(4159204).waterFogColor(329011).skyColor(0).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// warm_ocean
			map.put(44, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4445678).waterFogColor(270131).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// lukewarm_ocean
			map.put(45, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4566514).waterFogColor(267827).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// cold_ocean
			map.put(46, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// deep_warm_ocean
			map.put(47, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4445678).waterFogColor(270131).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// deep_lukewarm_ocean
			map.put(48, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4566514).waterFogColor(267827).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// deep_cold_ocean
			map.put(49, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// deep_frozen_ocean
			map.put(50, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(3750089).waterFogColor(329011).skyColor(8103167).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// sunflower_plains
			map.put(129, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7907327).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// desert_lakes
			map.put(130, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// gravelly_mountains
			map.put(131, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// flower_forest
			map.put(132, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7972607).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// taiga_mountains
			map.put(133, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233983).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// swamp_hills
			map.put(134, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(6388580).waterFogColor(2302743).skyColor(7907327).foliageColorOverride(6975545).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// ice_spikes
			map.put(140, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8364543).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// modified_jungle
			map.put(149, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// modified_jungle_edge
			map.put(151, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// tall_birch_forest
			map.put(155, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8037887).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// tall_birch_hills
			map.put(156, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8037887).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// dark_forest_hills
			map.put(157, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7972607).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// snowy_taiga_mountains
			map.put(158, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4020182).waterFogColor(329011).skyColor(8625919).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// giant_spruce_taiga
			map.put(160, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233983).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// giant_spruce_taiga_hills
			map.put(161, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233983).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// modified_gravelly_mountains
			map.put(162, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(8233727).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// shattered_savanna
			map.put(163, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.100000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7776767).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// shattered_savanna_plateau
			map.put(164, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7776511).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// eroded_badlands
			map.put(165, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// modified_wooded_badlands_plateau
			map.put(166, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// modified_badlands_plateau
			map.put(167, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7254527).foliageColorOverride(10387789).grassColorOverride(9470285).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// bamboo_jungle
			map.put(168, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// bamboo_jungle_hills
			map.put(169, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(12638463).waterColor(4159204).waterFogColor(329011).skyColor(7842047).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// soul_sand_valley
			map.put(170, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(1787717).waterColor(4159204).waterFogColor(329011).skyColor(7254527).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation("minecraft:ash")), 0.006250f)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// crimson_forest
			map.put(171, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(3343107).waterColor(4159204).waterFogColor(329011).skyColor(7254527).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation("minecraft:crimson_spore")), 0.025000f)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// warped_forest
			map.put(172, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(1705242).waterColor(4159204).waterFogColor(329011).skyColor(7254527).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation("minecraft:warped_spore")), 0.014280f)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
			// basalt_deltas
			map.put(173, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f).specialEffects(new BiomeSpecialEffects.Builder().fogColor(6840176).waterColor(4159204).waterFogColor(4341314).skyColor(7254527).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation("minecraft:white_ash")), 0.118093f)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).build());
		}

		private LegacyBiomeMapper() {
		}

		@Override
		public int getId(Biome biome) {
			throw new UnsupportedOperationException("this mapper does not support reversing biomes to IDs");
		}

		@Override
		public Biome getBiome(int id) {
			Biome biome = map.get(id);
			return biome != null ? biome : map.get(1);
		}
	}
}
