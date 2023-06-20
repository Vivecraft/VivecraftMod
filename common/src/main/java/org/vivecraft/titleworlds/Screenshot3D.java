package org.vivecraft.titleworlds;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.*;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.vivecraft.titleworlds.TitleWorldsMod.levelSource;

public class Screenshot3D {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd+HH_mm_ss");

    public static String saveTitleworld(ClientLevel originLevel, @Nullable String name) {
        if (name == null) {
            name = "TitleWorld+" + DATE_FORMAT.format(new Date());
        }
//        TODO Screenshot.grab();
//        TODO FileUtil.findAvailableName

        createSnapshotWorldAndSave(
                name,
                originLevel
        );
        return name;
    }

    private static void createSnapshotWorldAndSave(
            String worldName,
            ClientLevel originLevel
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

        PackRepository packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

        WorldStem worldStem;
        try {
            ClientLevel.ClientLevelData originLevelData = originLevel.getLevelData();


            // try to minimize server load
            GameRules modifiedRules = originLevelData.getGameRules().copy();
            modifiedRules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
            modifiedRules.getRule(GameRules.RULE_MOBGRIEFING).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DOMOBLOOT).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DOENTITYDROPS).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            modifiedRules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);
            //modifiedRules.getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, null);
            modifiedRules.getRule(GameRules.RULE_SPAWN_RADIUS).set(0, null);
            modifiedRules.getRule(GameRules.RULE_DISABLE_RAIDS).set(true, null);
            modifiedRules.getRule(GameRules.RULE_DOINSOMNIA).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DO_WARDEN_SPAWNING).set(false, null);
            modifiedRules.getRule(GameRules.RULE_DO_VINES_SPREAD).set(false, null);


            LevelSettings levelSettings = new LevelSettings(
                worldName,
                GameType.SPECTATOR,
                false,
                originLevelData.getDifficulty(),
                true,
                modifiedRules,
                WorldDataConfiguration.DEFAULT
            );

            WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, levelSettings.getDataConfiguration(), false, false);
            worldStem = new WorldOpenFlows(Minecraft.getInstance(), levelSource).loadWorldDataBlocking(packConfig, dataLoadContext -> {
                WorldDimensions.Complete complete = (WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen()))
                    .bake(dataLoadContext.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
                var levelData = new PrimaryLevelData(
                    levelSettings,
                    new WorldOptions(0L, false, false),
                    complete.specialWorldProperty(),
                    complete.lifecycle());

                // lock to 90°
                float closestAngle = switch (Minecraft.getInstance().player.getDirection()) {
                    case NORTH -> 180.0F;
                    case SOUTH -> 0.0F;
                    case WEST -> 90.0F;
                    case EAST -> -90.0F;
                    default -> Minecraft.getInstance().player.yHeadRot;
                };

                levelData.setSpawn(Minecraft.getInstance().player.blockPosition(), closestAngle);
                levelData.setDayTime(originLevelData.getDayTime());
                levelData.setGameTime(originLevelData.getGameTime());
//                loadedPlayerTag
//                levelData.setClearWeatherTime();
                levelData.setRaining(originLevelData.isRaining());
                levelData.setThundering(originLevelData.isThundering());
//                levelData.setInitialized();
//                levelData.setWorldBorder();
//                levelData.setEndDragonFightData();
//                levelData.setCustomBossEvents();
//                levelData.setWanderingTraderSpawnDelay();
//                levelData.setWanderingTraderSpawnChance();
//                levelData.setWanderingTraderId();
//                knownServerBrands
//                wasModded
//                scheduledEvents
                return new WorldLoader.DataLoadOutput<>(
                    levelData,
                    complete.dimensionsRegistryAccess());
            }, WorldStem::new);
        } catch (Exception var21) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", var21);

            try {
                levelStorageAccess.close();
            } catch (IOException var17) {
                LOGGER.warn("Failed to unlock access to level {}", worldName, var17);
            }

            return;
        }

        WorldData exception = worldStem.worldData();

        try {
            RegistryAccess.Frozen registry = worldStem.registries().compositeAccess();
            levelStorageAccess.saveDataTag(registry, exception);
            worldStem.dataPackResources().updateRegistryTags(registry);
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
