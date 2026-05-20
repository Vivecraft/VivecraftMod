package org.vivecraft.client_vr.menuworlds;

import com.google.common.io.Files;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timeline;
import net.minecraft.world.timeline.Timelines;
import org.vivecraft.Xplat;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class MenuWorldExporter {
    public static final int VERSION = 6;
    public static final int MIN_VERSION = 2;

    private static final DataFixer DATA_FIXER = DataFixers.getDataFixer();

    public static byte[] saveArea(
        Level level, int xMin, int zMin, int xSize, int zSize, int ground) throws IOException
    {
        BlockStateMapper blockStateMapper = new BlockStateMapper();
        PaletteBiomeMapper biomeMapper = new PaletteBiomeMapper();

        int yMin = level.getMinY();
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
                    skylightmap[index3] = (byte) level.getBrightness(LightLayer.SKY, pos3);
                    blocklightmap[index3] = (byte) level.getBrightness(LightLayer.BLOCK, pos3);

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
        dos.writeUTF(level.dimensionTypeRegistration().unwrapKey().orElseGet(() -> {
            VRSettings.LOGGER.error("couldn't export dimension id, falling back to overworld");
            return BuiltinDimensionTypes.OVERWORLD;
        }).identifier().toString());

        if (level instanceof ServerLevel) {
            dos.writeBoolean(((ServerLevel) level).isFlat());
        } else {
            dos.writeBoolean(((ClientLevel) level).getLevelData().isFlat);
        }

        dos.writeBoolean(level.dimensionType().hasSkyLight());

        if (level instanceof ServerLevel) {
            dos.writeLong(((ServerLevel) level).getSeed());
        } else {
            dos.writeLong(level.getBiomeManager().biomeZoomSeed); // not really correct :/
        }

        dos.writeInt(SharedConstants.getCurrentVersion().dataVersion().version());

        dos.writeBoolean(level.dimensionType().hasFixedTime());
        if (level.dimensionType().hasFixedTime()) {
            // TODO 1.21.11 there is no value for this anymore, just a boolean
            dos.writeLong(0);
        }
        dos.writeBoolean(level.dimensionType().hasCeiling());
        dos.writeInt(level.dimensionType().minY());
        dos.writeFloat(level.dimensionType().ambientLight());
        boolean cloudPresent =
            ARGB.alpha(level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_COLOR, Vec3.ZERO)) > 0;
        dos.writeBoolean(cloudPresent);
        if (cloudPresent) {
            // offset by 0.33f to be at the old block level again
            dos.writeInt(
                (int) (level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, Vec3.ZERO) - 0.33f));
        }

        dos.writeFloat(switch (Minecraft.getInstance().player.getDirection()) {
            case SOUTH -> 180.0f;
            case WEST -> -90.0f;
            case EAST -> 90.0f;
            default -> 0.0f; // also NORTH
        });

        dos.writeBoolean(level.getRainLevel(1.0f) > 0.0f);
        dos.writeBoolean(level.getThunderLevel(1.0f) > 0.0f);

        blockStateMapper.writePalette(dos);
        biomeMapper.writePalette(dos, level.registryAccess(), level.dimensionType());

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

    public static void saveAreaToFile(
        Level world, int xMin, int zMin, int xSize, int zSize, int ground, File file) throws IOException
    {
        byte[] bytes = saveArea(world, xMin, zMin, xSize, zSize, ground);
        Files.write(bytes, file);
    }

    public static FakeBlockAccess loadWorld(
        byte[] data) throws IOException, DataFormatException, ExecutionException, InterruptedException
    {
        Header header = new Header();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            header.read(dis);
        }
        if (header.version > VERSION || header.version < MIN_VERSION) {
            throw new DataFormatException("Unsupported menu world version: " + header.version);
        }

        ByteBuffer buf = ByteBuffer.allocate(header.uncompressedSize).order(ByteOrder.BIG_ENDIAN);
        try (Inflater inflater = new Inflater()) {
            inflater.setInput(data, Header.SIZE, data.length - Header.SIZE);
            inflater.inflate(buf);
        }
        buf.rewind();
        DataInput di = new DataInputBuffer(buf);

        int xSize = buf.getInt();
        int ySize = buf.getInt();
        int zSize = buf.getInt();
        int ground = buf.getInt();

        Identifier dimName;
        if (header.version < 4) { // old format
            int dimId = buf.getInt();
            dimName = switch (dimId) {
                case -1 -> BuiltinDimensionTypes.NETHER.identifier();
                case 1 -> BuiltinDimensionTypes.END.identifier();
                default -> BuiltinDimensionTypes.OVERWORLD.identifier();
            };
        } else {
            dimName = Identifier.parse(di.readUTF());
        }

        boolean isFlat;

        if (header.version < 4) // old format
        {
            isFlat = di.readUTF().equals("flat");
        } else {
            isFlat = buf.get() != 0;
        }

        boolean dimHasSkyLight = buf.get() != 0;

        long seed = 0;
        if (header.version >= 3) {
            seed = buf.getLong();
        }

        int dataVersion;
        if (header.version == 2) {
            dataVersion = 1631; // assume 1.13.2
        } else if (header.version == 3) {
            dataVersion = 2230; // assume 1.15.2
        } else if (header.version == 4) {
            dataVersion = 2586; // assume 1.16.5
        } else {
            dataVersion = buf.getInt(); // v5+ stores the real data version
        }

        if (dataVersion > SharedConstants.getCurrentVersion().dataVersion().version()) {
            VRSettings.LOGGER.warn(
                "Vivecraft: Menuworld data version is newer than current, this menu world may not load correctly.");
        }

        OptionalLong dimFixedTime = OptionalLong.empty();
        boolean dimHasCeiling;
        int dimMinY;
        float dimAmbientLight;
        Optional<Integer> cloudHeight = Optional.empty();
        DimensionType.Skybox skybox = DimensionType.Skybox.OVERWORLD;
        CardinalLighting.Type cardinalLightingType = CardinalLighting.Type.DEFAULT;

        if (header.version < 5) { // fill in missing values
            if (BuiltinDimensionTypes.NETHER.identifier().equals(dimName)) {
                dimFixedTime = OptionalLong.of(18000L);
                dimHasCeiling = true;
                dimMinY = 0;
                dimAmbientLight = 0.1f;
                skybox = DimensionType.Skybox.NONE;
                cardinalLightingType = CardinalLighting.Type.NETHER;
            } else if (BuiltinDimensionTypes.END.identifier().equals(dimName)) {
                dimFixedTime = OptionalLong.of(6000L);
                dimHasCeiling = false;
                dimMinY = 0;
                dimAmbientLight = 0.0f;
                skybox = DimensionType.Skybox.END;
            } else { // overworld/default
                dimHasCeiling = false;
                dimMinY = 0; // pre-v5 worlds don't have deeper underground
                dimAmbientLight = 0.0f;
            }
        } else {
            if (buf.get() != 0) {
                dimFixedTime = OptionalLong.of(buf.getLong());
            }
            dimHasCeiling = buf.get() != 0;
            dimMinY = buf.getInt();
            dimAmbientLight = buf.getFloat();

            // TODO 1.21.11 store those?
            if (dimHasCeiling && !dimHasSkyLight) {
                // nether
                skybox = DimensionType.Skybox.NONE;
                cardinalLightingType = CardinalLighting.Type.NETHER;
            } else if (dimFixedTime.isPresent()) {
                // end
                skybox = DimensionType.Skybox.END;
            }
        }

        if (header.version < 6) {
            if (dimHasSkyLight) { // might be an issue for modded dimensions but whatever
                cloudHeight = Optional.of(192);
            }
        } else {
            if (buf.get() != 0) {
                cloudHeight = Optional.of(buf.getInt());
            }
        }

        // TODO 1.21.11 store those?
        EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder();
        if (cloudHeight.isPresent()) {
            attributes.set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8f));
            attributes.set(EnvironmentAttributes.CLOUD_HEIGHT, cloudHeight.get() + 0.33F);
        }

        HolderGetter<Timeline> timelines = VanillaRegistries.createLookup().lookup(Registries.TIMELINE).orElse(null);
        HolderSet<Timeline> timeline = HolderSet.empty();

        switch (skybox) {
            case NONE -> {
                attributes.set(EnvironmentAttributes.FOG_START_DISTANCE, 10.0f);
                attributes.set(EnvironmentAttributes.FOG_END_DISTANCE, 96.0f);
                attributes.set(EnvironmentAttributes.SKY_LIGHT_COLOR, Timelines.NIGHT_SKY_LIGHT_COLOR);
                attributes.set(EnvironmentAttributes.SKY_LIGHT_LEVEL, 4.0f);
                attributes.set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0f);
                attributes.set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -13621215);
            }
            case OVERWORLD -> {
                attributes.set(EnvironmentAttributes.FOG_COLOR, -4138753);
                attributes.set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8f));
                attributes.set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -16119286);
                timeline = timelines != null ?
                    HolderSet.direct(timelines.getOrThrow(Timelines.OVERWORLD_DAY),
                        timelines.getOrThrow(Timelines.MOON)) :
                    HolderSet.empty();
            }
            case END -> {
                attributes.set(EnvironmentAttributes.FOG_COLOR, -15199464);
                attributes.set(EnvironmentAttributes.SKY_LIGHT_COLOR, -5480243);
                attributes.set(EnvironmentAttributes.SKY_COLOR, -16777216);
                attributes.set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0f);
                attributes.set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, -12630209);
            }
        }

        // TODO 26.1 should also be stored probably
        attributes.set(EnvironmentAttributes.BLOCK_LIGHT_TINT, DimensionDefaults.BLOCK_LIGHT_TINT);

        if (dataVersion < 4554 && BuiltinDimensionTypes.END.identifier().equals(dimName)) {
            dimAmbientLight = 0.25f; // pre-1.21.9 end worlds are too dark
        }

        DimensionType dimensionType = new DimensionType(dimFixedTime.isPresent(), dimHasSkyLight, dimHasCeiling, false,
            1.0,
            dimMinY, ySize, ySize, BlockTags.INFINIBURN_OVERWORLD, dimAmbientLight,
            new DimensionType.MonsterSettings(ConstantInt.of(0), 0), skybox, cardinalLightingType, attributes.build(),
            timeline, Optional.empty());

        float rotation = 0.0f;
        boolean rain = false;
        boolean thunder = false;

        if (header.version >= 5) {
            rotation = buf.getFloat();
            rain = buf.get() != 0;
            thunder = buf.get() != 0;
        }

        BlockStateMapper blockStateMapper = new BlockStateMapper();
        blockStateMapper.readPalette(di, dataVersion);

        BiomeMapper biomeMapper;
        if (header.version >= 5) {
            biomeMapper = new PaletteBiomeMapper();
            ((PaletteBiomeMapper) biomeMapper).readPalette(di);
        } else {
            biomeMapper = new LegacyBiomeMapper();
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int segmentSize = 16;

        BlockState[] blocks = new BlockState[xSize * ySize * zSize];
        for (int s = 0; s < ySize / segmentSize; s++) {
            int segment = s;
            int size = xSize * zSize * segmentSize;
            ByteBuffer bufSlice = buf.slice();
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = size * segment; i < size * segment + size; i++) {
                    blocks[i] = blockStateMapper.getState(bufSlice.getInt());
                }
            }, Util.backgroundExecutor()));
            buf.position(buf.position() + size * 4);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        futures.clear();

        short[][] heightmap = new short[xSize][zSize];
        for (int s = 0; s < xSize; s += segmentSize) {
            int x = s;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int z = 0; z < zSize; z++) {
                    for (int y = ySize - 1; y >= 0; y--) {
                        int index = (y * zSize + z) * xSize + x;
                        if (blocks[index].blocksMotion() || !blocks[index].getFluidState().isEmpty()) {
                            heightmap[x][z] = (short) (y + 1);
                            break;
                        }
                    }
                }
            }, Util.backgroundExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        futures.clear();

        byte[] lightdata = new byte[xSize * ySize * zSize];
        buf.get(lightdata);
        byte[] skylightmap = new byte[xSize * ySize * zSize];
        byte[] blocklightmap = new byte[xSize * ySize * zSize];
        for (int i = 0; i < lightdata.length; i++) {
            int b = lightdata[i] & 0xFF;
            skylightmap[i] = (byte) (b & 15);
            blocklightmap[i] = (byte) (b >> 4);
        }

        Biome[] biomemap = new Biome[xSize * ySize * zSize / 64];
        if (header.version == 2) {
            Biome[] tempBiomemap = new Biome[xSize * zSize];
            for (int i = 0; i < tempBiomemap.length; i++) {
                tempBiomemap[i] = biomeMapper.getBiome(buf.getInt());
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
                biomemap[i] = biomeMapper.getBiome(buf.getInt());
            }
        }

        return new FakeBlockAccess(header.version, seed, blocks, skylightmap, blocklightmap, biomemap, heightmap, xSize,
            ySize, zSize, ground, dimensionType, isFlat, rotation, rain, thunder);
    }

    public static FakeBlockAccess loadWorld(
        InputStream is) throws IOException, DataFormatException, ExecutionException, InterruptedException
    {
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
        return Arrays.stream(type.getEnumConstants()).filter(e -> input.equals(e.getSerializedName())).findFirst();
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

        void readPalette(DataInput dis, int dataVersion) throws IOException {
            this.paletteMap.clear();
            int size = dis.readInt();

            for (int i = 0; i < size; i++) {
                CompoundTag tag = CompoundTag.TYPE.load(dis, NbtAccounter.unlimitedHeap());
                tag = (CompoundTag) DATA_FIXER.update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, tag),
                    dataVersion, SharedConstants.getCurrentVersion().dataVersion().version()).getValue();
                this.paletteMap.add(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, tag));
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

    private static final BiomeGenerationSettings DUMMY_GENERATION_SETTINGS = new BiomeGenerationSettings.PlainBuilder().build();
    private static final MobSpawnSettings DUMMY_MOB_SPAWN_SETTINGS = new MobSpawnSettings.Builder().build();

    private static class PaletteBiomeMapper implements BiomeMapper {
        private final CrudeIncrementalIntIdentityHashBiMap<Biome> paletteMap = CrudeIncrementalIntIdentityHashBiMap.create(
            256);

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

        void readPalette(DataInput dis) throws IOException {
            this.paletteMap.clear();
            int size = dis.readInt();

            for (int i = 0; i < size; i++) {
                Biome.BiomeBuilder builder = new Biome.BiomeBuilder();

                String biomeId = dis.readUTF(); // registry key, not actually used though, just for reference

                builder.hasPrecipitation(dis.readBoolean());
                builder.temperature(dis.readFloat());
                decodeEnum(Biome.TemperatureModifier.class, dis.readUTF()).ifPresent(builder::temperatureAdjustment);
                builder.downfall(dis.readFloat());

                BiomeSpecialEffects.Builder effectsBuilder = new BiomeSpecialEffects.Builder();
                int fogColor = dis.readInt();
                if (fogColor != 10518688 || !isEndBiome(biomeId)) {
                    builder.setAttribute(EnvironmentAttributes.FOG_COLOR, fogColor);
                }
                effectsBuilder.waterColor(dis.readInt());
                builder.setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, dis.readInt());
                int skyColor = dis.readInt();
                if (skyColor != 7254527 || !isNetherBiome(biomeId)) {
                    builder.setAttribute(EnvironmentAttributes.SKY_COLOR, skyColor);
                }

                if (dis.readBoolean()) {
                    effectsBuilder.foliageColorOverride(dis.readInt());
                }

                if (dis.readBoolean()) {
                    effectsBuilder.grassColorOverride(dis.readInt());
                }

                decodeEnum(BiomeSpecialEffects.GrassColorModifier.class, dis.readUTF()).ifPresent(
                    effectsBuilder::grassColorModifier);

                if (dis.readBoolean()) {
                    Optional<Holder.Reference<ParticleType<?>>> particleTypeRef = BuiltInRegistries.PARTICLE_TYPE.get(
                        Identifier.parse(dis.readUTF()));
                    float probability = dis.readFloat();
                    particleTypeRef.ifPresent(particleType -> {
                        if (particleType.value() instanceof ParticleOptions) {
                            builder.setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES,
                                AmbientParticle.of((ParticleOptions) particleType.value(), probability));
                        }
                    });
                }

                Biome biome = builder.specialEffects(effectsBuilder.build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS).build();
                this.paletteMap.add(biome);
            }
        }

        void writePalette(
            DataOutputStream dos, RegistryAccess registryAccess, DimensionType dimensionType) throws IOException
        {
            dos.writeInt(this.paletteMap.size());

            for (int i = 0; i < this.paletteMap.size(); i++) {
                Biome biome = this.paletteMap.byId(i);

                dos.writeUTF(registryAccess.lookupOrThrow(Registries.BIOME).getKey(biome).toString());

                Biome.ClimateSettings climateSettings = Xplat.INSTANCE.getBiomeClimateSettings(biome);

                dos.writeBoolean(climateSettings.hasPrecipitation());
                dos.writeFloat(climateSettings.temperature());
                dos.writeUTF(climateSettings.temperatureModifier().getSerializedName());
                dos.writeFloat(climateSettings.downfall());

                BiomeSpecialEffects specialEffects = Xplat.INSTANCE.getBiomeEffects(biome);

                dos.writeInt(getAttributeValue(biome, EnvironmentAttributes.FOG_COLOR, dimensionType));
                dos.writeInt(specialEffects.waterColor());
                dos.writeInt(getAttributeValue(biome, EnvironmentAttributes.WATER_FOG_COLOR, dimensionType));
                dos.writeInt(getAttributeValue(biome, EnvironmentAttributes.SKY_COLOR, dimensionType));

                dos.writeBoolean(specialEffects.foliageColorOverride().isPresent());
                if (specialEffects.foliageColorOverride().isPresent()) {
                    dos.writeInt(specialEffects.foliageColorOverride().get());
                }

                dos.writeBoolean(specialEffects.grassColorOverride().isPresent());
                if (specialEffects.grassColorOverride().isPresent()) {
                    dos.writeInt(specialEffects.grassColorOverride().get());
                }

                dos.writeUTF(specialEffects.grassColorModifier().getSerializedName());

                dos.writeBoolean(biome.getAttributes().get(EnvironmentAttributes.AMBIENT_PARTICLES) != null);
                if (biome.getAttributes().get(EnvironmentAttributes.AMBIENT_PARTICLES) != null) {
                    // TODO 1.21.11 there can be multiple particles now
                    List<AmbientParticle> particles = getAttributeValue(biome, EnvironmentAttributes.AMBIENT_PARTICLES,
                        dimensionType);
                    dos.writeUTF(BuiltInRegistries.PARTICLE_TYPE.getKey(particles.getFirst().particle().getType())
                        .toString());
                    dos.writeFloat(particles.getFirst().probability());
                }
            }
        }

        private <Value> Value getAttributeValue(
            Biome biome, EnvironmentAttribute<Value> attribute, DimensionType dimensionType)
        {
            Value val = attribute.defaultValue();
            if (dimensionType.attributes().get(attribute) != null) {
                val = dimensionType.attributes().get(attribute).applyModifier(val);
            }
            if (biome.getAttributes().get(attribute) != null) {
                val = biome.getAttributes().get(attribute).applyModifier(val);
            }
            return val;
        }

        private boolean isEndBiome(String biomeId) {
            return "the_end".equals(biomeId) ||
                "small_end_islands".equals(biomeId) ||
                "end_midlands".equals(biomeId) ||
                "end_highlands".equals(biomeId) ||
                "end_barrens".equals(biomeId);
        }

        private boolean isNetherBiome(String biomeId) {
            return "nether_wastes".equals(biomeId) ||
                "soul_sand_valley".equals(biomeId) ||
                "crimson_forest".equals(biomeId) ||
                "warped_forest".equals(biomeId) ||
                "basalt_deltas".equals(biomeId);
        }
    }

    private static class LegacyBiomeMapper implements BiomeMapper {
        private static final Map<Integer, Biome> MAP = new HashMap<>();

        static {
            // big hard-coded map of biomes from 1.16
            // the commented line here is just a builder reference
            // map.put(0, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(1).temperatureAdjustment(Biome.TemperatureModifier.NONE).downfall(1).specialEffects(new BiomeSpecialEffects.Builder().waterColor(1).foliageColorOverride(1).grassColorOverride(1).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.NONE).ambientParticle(new AmbientParticleSettings((ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new Identifier("")), 0)).build()).generationSettings(dummyGenerationSettings).mobSpawnSettings(dummyMobSpawnSettings).setAttribute(EnvironmentAttributes.FOG_COLOR, 1).setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 1).setAttribute(EnvironmentAttributes.SKY_COLOR, 1).build());

            // plains
            MAP.put(1, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327).build());
            // the_void
            MAP.put(127, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // ocean
            MAP.put(0, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // desert
            MAP.put(2, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // mountains
            MAP.put(3, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // forest
            MAP.put(4, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7972607).build());
            // taiga
            MAP.put(5, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233983).build());
            // swamp
            MAP.put(6, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(6388580).foliageColorOverride(6975545)
                        .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 2302743)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327).build());
            // river
            MAP.put(7, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // nether_wastes
            MAP.put(8, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 3344392)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011).build());
            // the_end
            MAP.put(9, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0).build());
            // frozen_ocean
            MAP.put(10, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f)
                .temperatureAdjustment(Biome.TemperatureModifier.FROZEN).specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(3750089).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // frozen_river
            MAP.put(11, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(3750089).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // snowy_tundra
            MAP.put(12, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // snowy_mountains
            MAP.put(13, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // mushroom_fields
            MAP.put(14, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.900000f).downfall(1.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // mushroom_field_shore
            MAP.put(15, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.900000f).downfall(1.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // beach
            MAP.put(16, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327).build());
            // desert_hills
            MAP.put(17, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // wooded_hills
            MAP.put(18, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7972607).build());
            // taiga_hills
            MAP.put(19, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233983).build());
            // mountain_edge
            MAP.put(20, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // jungle
            MAP.put(21, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // jungle_hills
            MAP.put(22, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // jungle_edge
            MAP.put(23, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // deep_ocean
            MAP.put(24, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // stone_shore
            MAP.put(25, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // snowy_beach
            MAP.put(26, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.050000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // birch_forest
            MAP.put(27, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8037887).build());
            // birch_forest_hills
            MAP.put(28, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8037887).build());
            // dark_forest
            MAP.put(29, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204)
                        .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST)
                        .build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7972607).build());
            // snowy_taiga
            MAP.put(30, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8625919).build());
            // snowy_taiga_hills
            MAP.put(31, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8625919).build());
            // giant_tree_taiga
            MAP.put(32, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.300000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8168447).build());
            // giant_tree_taiga_hills
            MAP.put(33, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.300000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8168447).build());
            // wooded_mountains
            MAP.put(34, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // savanna
            MAP.put(35, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.200000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7711487).build());
            // savanna_plateau
            MAP.put(36, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7776511).build());
            // badlands
            MAP.put(37, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // wooded_badlands_plateau
            MAP.put(38, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // badlands_plateau
            MAP.put(39, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // small_end_islands
            MAP.put(40, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0).build());
            // end_midlands
            MAP.put(41, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0).build());
            // end_highlands
            MAP.put(42, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0).build());
            // end_barrens
            MAP.put(43, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0).build());
            // warm_ocean
            MAP.put(44, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4445678).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 270131)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // lukewarm_ocean
            MAP.put(45, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4566514).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 267827)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // cold_ocean
            MAP.put(46, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // deep_warm_ocean
            MAP.put(47, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4445678).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 270131)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // deep_lukewarm_ocean
            MAP.put(48, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4566514).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 267827)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // deep_cold_ocean
            MAP.put(49, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // deep_frozen_ocean
            MAP.put(50, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.500000f).downfall(0.500000f)
                .temperatureAdjustment(Biome.TemperatureModifier.FROZEN).specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(3750089).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8103167).build());
            // sunflower_plains
            MAP.put(129, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327).build());
            // desert_lakes
            MAP.put(130, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // gravelly_mountains
            MAP.put(131, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // flower_forest
            MAP.put(132, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7972607).build());
            // taiga_mountains
            MAP.put(133, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233983).build());
            // swamp_hills
            MAP.put(134, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.800000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(6388580).foliageColorOverride(6975545)
                        .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 2302743)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7907327).build());
            // ice_spikes
            MAP.put(140, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.000000f).downfall(0.500000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8364543).build());
            // modified_jungle
            MAP.put(149, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // modified_jungle_edge
            MAP.put(151, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // tall_birch_forest
            MAP.put(155, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8037887).build());
            // tall_birch_hills
            MAP.put(156, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.600000f).downfall(0.600000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8037887).build());
            // dark_forest_hills
            MAP.put(157, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.700000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204)
                        .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST)
                        .build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7972607).build());
            // snowy_taiga_mountains
            MAP.put(158, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(-0.500000f).downfall(0.400000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4020182).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8625919).build());
            // giant_spruce_taiga
            MAP.put(160, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233983).build());
            // giant_spruce_taiga_hills
            MAP.put(161, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.250000f).downfall(0.800000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233983).build());
            // modified_gravelly_mountains
            MAP.put(162, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.200000f).downfall(0.300000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 8233727).build());
            // shattered_savanna
            MAP.put(163, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.100000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7776767).build());
            // shattered_savanna_plateau
            MAP.put(164, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7776511).build());
            // eroded_badlands
            MAP.put(165, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // modified_wooded_badlands_plateau
            MAP.put(166, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // modified_badlands_plateau
            MAP.put(167, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).foliageColorOverride(10387789)
                        .grassColorOverride(9470285).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7254527).build());
            // bamboo_jungle
            MAP.put(168, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // bamboo_jungle_hills
            MAP.put(169, new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.950000f).downfall(0.900000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12638463)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 7842047).build());
            // soul_sand_valley
            MAP.put(170, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 1787717)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(
                    (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(Identifier.parse("minecraft:ash"))
                        .orElseThrow().value(),
                    0.006250f)).build());
            // crimson_forest
            MAP.put(171, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 3343107)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES,
                    AmbientParticle.of((ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(
                        Identifier.parse("minecraft:crimson_spore")).orElseThrow().value(), 0.025000f)).build());
            // warped_forest
            MAP.put(172, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build()).generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 1705242)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 329011)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES,
                    AmbientParticle.of((ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(
                        Identifier.parse("minecraft:warped_spore")).orElseThrow().value(), 0.014280f)).build());
            // basalt_deltas
            MAP.put(173, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.000000f).downfall(0.000000f)
                .specialEffects(
                    new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                .generationSettings(
                    DUMMY_GENERATION_SETTINGS).mobSpawnSettings(DUMMY_MOB_SPAWN_SETTINGS)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 6840176)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 4341314)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES,
                    AmbientParticle.of((ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(
                        Identifier.parse("minecraft:white_ash")).orElseThrow().value(), 0.118093f)).build());
        }

        private LegacyBiomeMapper() {
        }

        @Override
        public int getId(Biome biome) {
            throw new UnsupportedOperationException("this mapper does not support reversing biomes to IDs");
        }

        @Override
        public Biome getBiome(int id) {
            Biome biome = MAP.get(id);
            return biome != null ? biome : MAP.get(1);
        }
    }

    private static class DataInputBuffer implements DataInput {
        final ByteBuffer buffer;

        private DataInputBuffer(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void readFully(byte[] b) {
            readFully(b, 0, b.length);
        }

        @Override
        public void readFully(byte[] b, int off, int len) {
            this.buffer.get(b, off, len);
        }

        @Override
        public int skipBytes(int n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean readBoolean() {
            return this.buffer.get() != 0;
        }

        @Override
        public byte readByte() {
            return this.buffer.get();
        }

        @Override
        public int readUnsignedByte() {
            return this.buffer.get() & 0xFF;
        }

        @Override
        public short readShort() {
            return this.buffer.getShort();
        }

        @Override
        public int readUnsignedShort() {
            return this.buffer.getShort() & 0xFFFF;
        }

        @Override
        public char readChar() {
            return this.buffer.getChar();
        }

        @Override
        public int readInt() {
            return this.buffer.getInt();
        }

        @Override
        public long readLong() {
            return this.buffer.getLong();
        }

        @Override
        public float readFloat() {
            return this.buffer.getFloat();
        }

        @Override
        public double readDouble() {
            return this.buffer.getDouble();
        }

        @Override
        public String readLine() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String readUTF() throws IOException {
            return DataInputStream.readUTF(this);
        }
    }
}
