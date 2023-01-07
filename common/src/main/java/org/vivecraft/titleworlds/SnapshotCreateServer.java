package org.vivecraft.titleworlds;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ModCheck;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.vivecraft.titleworlds.mixin.accessor.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.stream.Collectors;

public class SnapshotCreateServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;
    private final ClientLevel originLevel;

    public final ThreadLocal<Boolean> cancelSaveEntity = ThreadLocal.withInitial(() -> true);

    public SnapshotCreateServer(
            Thread thread,
            Minecraft minecraft,
            ClientLevel parentLevel,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            PackRepository packRepository,
            WorldStem worldStem
    ) {
        super(
                thread,
                levelStorageAccess,
                packRepository,
                worldStem,
                Proxy.NO_PROXY,
                minecraft.getFixerUpper(),
                null,
                null,
                null,
                LoggerChunkProgressListener::new
        );
        this.minecraft = minecraft;
        this.originLevel = parentLevel;
    }

    @Override
    protected boolean initServer() {
        this.setPlayerList(new PlayerList(this, this.registryAccess(), this.playerDataStorage, 1) {
            @Override
            public @NotNull CompoundTag getSingleplayerData() {
                assert Minecraft.getInstance().player != null;
                var tag = Minecraft.getInstance().player.saveWithoutId(new CompoundTag());
                tag.putString("Dimension", originLevel.dimension().location().toString());
                return tag;
            }
        });
        this.createLevels(new LoggerChunkProgressListener(11));

        var serverLevel = this.getLevel(this.originLevel.dimension());
        assert serverLevel != null;
        var serverChunkMap = serverLevel.getChunkSource().chunkMap;
        var serverEntityManager = ((ServerLevelAcc) serverLevel).getEntityManager();
        var serverEntityPermanentStorage = ((PersistentEntitySectionManagerAcc) serverEntityManager).getPermanentStorage();

        var clientLevelAcc = ((ClientLevelAcc) this.originLevel);
        var clientChunkCacheAcc = (ClientChunkCacheAcc) originLevel.getChunkSource();
        var clientChunkCacheStorageAcc = (ClientChunkCacheStorageAcc) (Object) clientChunkCacheAcc.getStorage();
        var clientChunks = clientChunkCacheStorageAcc.getChunks();
        var clientEntityManager = clientLevelAcc.getEntityStorage();
        var clientEntityStorage = ((TransientEntitySectionManagerAcc) clientEntityManager).getSectionStorage();
        var clientMapData = clientLevelAcc.getMapData();

        for (int k = 0; k < clientChunks.length(); ++k) {
            LevelChunk levelChunk = clientChunks.get(k);
            if (levelChunk != null) {
                ChunkPos chunkPos = levelChunk.getPos();

                CompoundTag chunkData = ChunkSerializer.write(serverLevel, levelChunk);
                serverChunkMap.write(chunkPos, chunkData);

                var entities = clientEntityStorage
                        .getExistingSectionsInChunk(chunkPos.toLong())
                        .flatMap(entitySection -> entitySection.getEntities().filter(EntityAccess::shouldBeSaved))
                        .collect(Collectors.toList());

                cancelSaveEntity.set(false);
                serverEntityPermanentStorage.storeEntities(new ChunkEntities<>(chunkPos, entities));
                cancelSaveEntity.set(true);
            }
        }

        File dataStorageFolder = this.storageSource.getDimensionPath(Level.OVERWORLD).resolve("data").toFile();
        int maxId = 0;
        for (var entry : clientMapData.entrySet()) {
            var id = entry.getKey();
            var mapData = entry.getValue();
            mapData.save(new File(dataStorageFolder, id + ".dat"));

            int idInt = Integer.parseInt(id.substring(4));
            if (idInt > maxId) {
                maxId = idInt;
            }
        }

        CompoundTag idCounts = new CompoundTag();
        idCounts.putInt("map", maxId);

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", idCounts);
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

        try {
            NbtIo.writeCompressed(compoundTag, new File(dataStorageFolder, "idcounts.dat"));
        } catch (IOException var4) {
            LOGGER.error("Could not save data {}", this, var4);
        }

        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());

        this.halt(false);
        return true;
    }


    /*
    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        this.storeGameTypes(nbt);
        nbt.putBoolean("seenCredits", this.seenCredits);
        if (this.enteredNetherPosition != null) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putDouble("x", this.enteredNetherPosition.x);
            compoundTag.putDouble("y", this.enteredNetherPosition.y);
            compoundTag.putDouble("z", this.enteredNetherPosition.z);
            nbt.put("enteredNetherPosition", compoundTag);
        }

        Entity compoundTag = this.getRootVehicle();
        Entity entity = this.getVehicle();
        if (entity != null && compoundTag != this && compoundTag.hasExactlyOnePlayerPassenger()) {
            CompoundTag compoundTag2 = new CompoundTag();
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag.save(compoundTag3);
            compoundTag2.putUUID("Attach", entity.getUUID());
            compoundTag2.put("Entity", compoundTag3);
            nbt.put("RootVehicle", compoundTag2);
        }

        nbt.put("recipeBook", this.recipeBook.toNbt());
        nbt.putString("Dimension", this.level.dimension().location().toString());
        if (this.respawnPosition != null) {
            nbt.putInt("SpawnX", this.respawnPosition.getX());
            nbt.putInt("SpawnY", this.respawnPosition.getY());
            nbt.putInt("SpawnZ", this.respawnPosition.getZ());
            nbt.putBoolean("SpawnForced", this.respawnForced);
            nbt.putFloat("SpawnAngle", this.respawnAngle);
            ResourceLocation.CODEC
                .encodeStart(NbtOps.INSTANCE, this.respawnDimension.location())
                .resultOrPartial(LOGGER::error)
                .ifPresent(tag -> nbt.put("SpawnDimension", tag));
        }
    }
    */

    @Override
    public File getServerDirectory() {
        return this.minecraft.gameDirectory;
    }

    @Override
    public void onServerCrash(CrashReport report) {
        this.minecraft.delayCrash(() -> report);
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 4;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport details) {
        details.setDetail("Type", "Snapshot Server");
        return details;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return false;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile profile) {
        return false;
    }
}
