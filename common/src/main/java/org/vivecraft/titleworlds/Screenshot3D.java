package org.vivecraft.titleworlds;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static org.vivecraft.titleworlds.TitleWorldsMod.levelSource;

public class Screenshot3D {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd+HH_mm_ss");

    public static String take3DScreenshot(ClientLevel originLevel, @Nullable String name) {
        if (name == null) {
            name = "3D_screenshot+" + DATE_FORMAT.format(new Date());
        }
//        TODO Screenshot.grab();
//        TODO FileUtil.findAvailableName

        ClientLevel.ClientLevelData originLevelData = originLevel.getLevelData();

        LevelSettings levelSettings = new LevelSettings(
                name,
                GameType.CREATIVE,
                false,
                originLevelData.getDifficulty(),
                true,
                originLevelData.getGameRules(),
                DataPackConfig.DEFAULT
        );

        RegistryAccess registryAccess = originLevel.registryAccess();

        WritableRegistry<LevelStem> levelStems = new MappedRegistry<>(
                Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null
        );
        levelStems.register(
                ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, originLevel.dimension().location()),
                new LevelStem(
                        originLevel.dimensionTypeRegistration(),
                        new FlatLevelSource(
                                registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                                new FlatLevelGeneratorSettings(Optional.empty(), registryAccess.registryOrThrow(Registry.BIOME_REGISTRY))
                        )
                ),
                Lifecycle.stable()
        );

        WorldGenSettings worldGenSettings = new WorldGenSettings(
                0,
                false,
                false,
                WorldGenSettings.withOverworld(
                        registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
                        levelStems,
                        new FlatLevelSource(
                                registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                                new FlatLevelGeneratorSettings(Optional.empty(), registryAccess.registryOrThrow(Registry.BIOME_REGISTRY))
                        )
                )
        );

        createSnapshotWorldAndSave(
                name,
                originLevel,
                levelStorageAccess -> levelSettings::getDataPackConfig,
                levelStorageAccess -> (resourceManager, dataPackConfig) -> {
                    RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
                    DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
                    DynamicOps<JsonElement> dynamicOps2 = RegistryOps.createAndLoad(JsonOps.INSTANCE, writable, resourceManager);
                    DataResult<WorldGenSettings> dataResult = WorldGenSettings.CODEC
                            .encodeStart(dynamicOps, worldGenSettings)
                            .setLifecycle(Lifecycle.stable())
                            .flatMap(jsonElement -> WorldGenSettings.CODEC.parse(dynamicOps2, jsonElement));
                    WorldGenSettings worldGenSettings2 = dataResult.getOrThrow(
                            false, Util.prefix("Error reading worldgen settings after loading data packs: ", LOGGER::error)
                    );
                    var levelData = new PrimaryLevelData(levelSettings, worldGenSettings2, dataResult.lifecycle());
//                    levelData.setSpawn(originLevel.getSharedSpawnPos(), originLevel.getSharedSpawnAngle());
                    levelData.setSpawn(Minecraft.getInstance().player.blockPosition(), Minecraft.getInstance().player.yHeadRot);
                    levelData.setDayTime(originLevelData.getDayTime());
                    levelData.setGameTime(originLevelData.getGameTime());
//                                        loadedPlayerTag
//                                        levelData.setClearWeatherTime();
                    levelData.setRaining(originLevelData.isRaining());
//                                        levelData.setRainTime();
                    levelData.setThundering(originLevelData.isThundering());
//                                        levelData.setThunderTime();
//                                        levelData.setInitialized();
//                                        levelData.setWorldBorder();
//                                        levelData.setEndDragonFightData();
//                                        levelData.setCustomBossEvents();
//                                        levelData.setWanderingTraderSpawnDelay();
//                                        levelData.setWanderingTraderSpawnChance();
//                                        levelData.setWanderingTraderId();
//                                        knownServerBrands
//                                        wasModded
//                                        scheduledEvents
                    return Pair.of(levelData, writable.freeze());
                }
        );
        return name;
    }

    private static void createSnapshotWorldAndSave(
            String worldName,
            ClientLevel originLevel,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> dataPackSettingsGetter,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> function
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = levelSource.createAccess(worldName);
        } catch (IOException var22) {
            LOGGER.warn("Failed to read level {} data", worldName, var22);
            SystemToast.onWorldAccessFailure(minecraft, worldName);
            minecraft.setScreen(null);
            return;
        }

        PackRepository packRepository = new PackRepository(
                PackType.SERVER_DATA,
                new ServerPacksSource(),
                new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
        );

        WorldStem worldStem;
        try {
            worldStem = minecraft.makeWorldStem(
                    packRepository,
                    false,
                    dataPackSettingsGetter.apply(levelStorageAccess),
                    function.apply(levelStorageAccess)
            );
        } catch (Exception var21) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", var21);

            try {
                packRepository.close();
                levelStorageAccess.close();
            } catch (IOException var17) {
                LOGGER.warn("Failed to unlock access to level {}", worldName, var17);
            }

            return;
        }

        WorldData exception = worldStem.worldData();

        try {
            RegistryAccess.Frozen iOException3 = worldStem.registryAccess();
            levelStorageAccess.saveDataTag(iOException3, exception);
            worldStem.updateGlobals();
            var server = MinecraftServer.spin(
                    thread -> new SnapshotCreateServer(thread, minecraft, originLevel, levelStorageAccess, packRepository, worldStem)
            );
            server.halt(true);
        } catch (Throwable var20) {
            CrashReport yggdrasilAuthenticationService = CrashReport.forThrowable(var20, "Starting integrated server");
            CrashReportCategory minecraftSessionService = yggdrasilAuthenticationService.addCategory("Starting integrated server");
            minecraftSessionService.setDetail("Level ID", worldName);
            minecraftSessionService.setDetail("Level Name", exception.getLevelName());
            throw new ReportedException(yggdrasilAuthenticationService);
        }
    }
}
