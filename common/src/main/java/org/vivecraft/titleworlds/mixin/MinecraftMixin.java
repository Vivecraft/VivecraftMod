package org.vivecraft.titleworlds.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.*;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.titleworlds.extensions.MinecraftTitleworldExtension;
import oshi.util.tuples.Triplet;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin extends ReentrantBlockableEventLoop<Runnable> implements MinecraftTitleworldExtension {

    @Shadow
    @Nullable
    public ClientLevel level;

    public MinecraftMixin(String string) {
        super(string);
    }

    @Shadow
    public abstract void setScreen(@Nullable Screen guiScreen);

    @Shadow
    @Final
    private AtomicReference<StoringChunkProgressListener> progressListener;

    @Shadow
    @Final
    private Proxy proxy;

    @Shadow
    @Final
    public File gameDirectory;

    @Shadow
    private @Nullable IntegratedServer singleplayerServer;

    @Shadow
    private boolean isLocalServer;

    @Shadow
    @Final
    private Queue<Runnable> progressTasks;

    @Shadow
    protected abstract void runTick(boolean renderLevel);

    @Shadow
    private @Nullable Connection pendingConnection;

    @Shadow
    public abstract User getUser();

    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    private volatile boolean running;

    @Shadow
    private static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        throw new UnsupportedOperationException();
    }

    @Unique
    private boolean closingLevel;

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("Title World Loader");

    /**
     * Called when joining / leaving a server
     */
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    void preClearLevel(Screen screen, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            if (activeLoadingFuture != null) {
                while (!activeLoadingFuture.isDone()) {
                    this.runAllTasks();
                    this.runTick(false);
                }
                activeLoadingFuture = null;
                if (cleanup != null) {
                    cleanup.run();
                }
            }

            if (singleplayerServer != null) {
                // Ensure the server has initialized so we don't orphan it
                while (!this.singleplayerServer.isReady()) {
                    this.runAllTasks();
                    this.runTick(false);
                }
                if (this.pendingConnection != null || this.level != null) {
                    // Wait for connection to establish so it can be killed cleanly on this.level.disconnect();
                    while (this.pendingConnection != null) {
                        this.runAllTasks();
                        this.runTick(false);
                    }
                }
                this.singleplayerServer.halt(false);
            }
        } else {
            this.closingLevel = this.level != null;
        }

        if (this.level != null) {
            this.level.disconnect();
        }
    }

    /**
     * Called when joining / leaving a server
     */
    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    void postClearLevel(Screen screen, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            TitleWorldsMod.LOGGER.info("Closing Title World");
            TitleWorldsMod.state.isTitleWorld = false;
            TitleWorldsMod.state.pause = false;
        } else if (this.closingLevel && this.running) {
            TitleWorldsMod.LOGGER.info("Loading Title World");
            tryLoadTitleWorld();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    void setScreen(Screen guiScreen, CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld) {
            if (this.screen instanceof TitleScreen && guiScreen instanceof TitleScreen) {
                ci.cancel();
            } else if (guiScreen == null) {
                setScreen(new TitleScreen());
                ci.cancel();
            } else if (guiScreen instanceof ProgressScreen || guiScreen instanceof ReceivingLevelScreen) {
                ci.cancel();
            }
        }
    }

    @Unique
    private static final Random random = new Random();

    @SuppressWarnings("UnusedReturnValue")
    @Unique
    public boolean tryLoadTitleWorld() {
        try {
            List<LevelSummary> list = TitleWorldsMod.levelSource.getLevelList();
            if (list.isEmpty()) {
                LOGGER.info("TitleWorlds folder is empty");
                return false;
            }
            this.loadTitleWorld(list.get(random.nextInt(list.size())).getLevelId(), WorldStem.DataPackConfigSupplier::loadFromWorld, WorldStem.WorldDataSupplier::loadFromWorld);
            return true;
        } catch (ExecutionException | InterruptedException | LevelStorageException e) {
            LOGGER.error("Exception when loading title world", e);
            return false;
        }
    }

    @Unique
    @Nullable
    private Future<?> activeLoadingFuture = null;

    @Unique
    @Nullable
    private Runnable cleanup = null;

    @Unique
    private void loadTitleWorld(String levelName,
                                Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> dataPackConfigSupplier,
                                Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> worldDataSupplier
    ) throws ExecutionException, InterruptedException {
        LOGGER.info("Loading title world");
        TitleWorldsMod.state.isTitleWorld = ClientDataHolder.getInstance().vrSettings.menuWorldSelection;
        TitleWorldsMod.state.pause = false;

        var worldResourcesFuture
                = CompletableFuture.supplyAsync(() -> openWorldResources(levelName, dataPackConfigSupplier, worldDataSupplier, false), Util.backgroundExecutor());

        activeLoadingFuture = worldResourcesFuture;
        cleanup = () -> {
            try {
                var worldResources = worldResourcesFuture.get();
                worldResources.getA().close();
                worldResources.getB().close();
            } catch (InterruptedException | ExecutionException | IOException e) {
                LOGGER.error("Exception caught when cleaning up async world load stage 1", e);
            }
        };

        LOGGER.info("Loading world resources");
        while (!worldResourcesFuture.isDone()) {
            this.runAllTasks();
            this.runTick(false);
            if (!TitleWorldsMod.state.isTitleWorld) {
                return;
            }
        }

        var worldResources = worldResourcesFuture.get();
        LevelStorageSource.LevelStorageAccess levelStorageAccess = worldResources.getA();
        PackRepository packRepository = worldResources.getB();
        CompletableFuture<WorldStem> worldStemCompletableFuture = worldResources.getC();

        activeLoadingFuture = worldStemCompletableFuture;
        cleanup = () -> {
            try {
                levelStorageAccess.close();
                packRepository.close();
                worldStemCompletableFuture.get().close();
            } catch (InterruptedException | ExecutionException | IOException e) {
                TitleWorldsMod.LOGGER.error("Exception caught when cleaning up async world load stage 2", e);
            }
        };

        LOGGER.info("Waiting for WorldStem to load");
        while (!worldStemCompletableFuture.isDone()) {
            this.runAllTasks();
            this.runTick(false);
            if (!TitleWorldsMod.state.isTitleWorld) {
                return;
            }
        }

        WorldStem worldStem = worldStemCompletableFuture.get();

        this.progressListener.set(null);

        LOGGER.info("Starting server");
        activeLoadingFuture = CompletableFuture.runAsync(() -> startSingleplayerServer(levelName, levelStorageAccess, worldStem, packRepository), Util.backgroundExecutor());
        cleanup = null;

        while (singleplayerServer == null || !this.singleplayerServer.isReady()) {
            this.runAllTasks();
            this.runTick(false);
            if (!TitleWorldsMod.state.isTitleWorld) {
                return;
            }
        }

        LOGGER.info("Joining singleplayer server");
        var joinServerFuture = CompletableFuture.runAsync(this::joinSingleplayerServer, Util.backgroundExecutor());

        activeLoadingFuture = joinServerFuture;

        while (!joinServerFuture.isDone()) {
            this.runAllTasks();
            this.runTick(false);
            if (!TitleWorldsMod.state.isTitleWorld) {
                return;
            }
        }
        activeLoadingFuture = null;

        LOGGER.info("Logging into title world");
        TitleWorldsMod.state.finishedLoading = true;
    }

    @Unique
    private Triplet<LevelStorageSource.LevelStorageAccess, PackRepository, CompletableFuture<WorldStem>> openWorldResources(
            String levelName,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.DataPackConfigSupplier> dataPackConfigSupplierFunction,
            Function<LevelStorageSource.LevelStorageAccess, WorldStem.WorldDataSupplier> worldDataSupplierFunction,
            boolean vanillaOnly
    ) {
        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = TitleWorldsMod.levelSource.createAccess(levelName);
        } catch (IOException var21) {
            throw new RuntimeException("Failed to read data");
        }

        var dataPackConfigSupplier = dataPackConfigSupplierFunction.apply(levelStorageAccess);
        var worldDataSupplier = worldDataSupplierFunction.apply(levelStorageAccess);
        var packRepository = createPackRepository(levelStorageAccess);
        var initConfig = new WorldStem.InitConfig(packRepository, Commands.CommandSelection.INTEGRATED, 2, vanillaOnly);
        var completableFuture = WorldStem.load(
                initConfig, dataPackConfigSupplier, worldDataSupplier, Util.backgroundExecutor(), this
        );

        return new Triplet<>(levelStorageAccess, packRepository, completableFuture);
    }

    @Unique
    private void startSingleplayerServer(
            String levelName,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            WorldStem worldStem,
            PackRepository packRepository
    ) {
        WorldData worldData = worldStem.worldData();
        this.progressListener.set(null);

        try {
            RegistryAccess.Frozen registryAccess = worldStem.registryAccess();
            levelStorageAccess.saveDataTag(registryAccess, worldData);
            worldStem.updateGlobals();
            YggdrasilAuthenticationService iOException4 = new YggdrasilAuthenticationService(this.proxy);
            MinecraftSessionService minecraftSessionService = iOException4.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = iOException4.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            gameProfileCache.setExecutor((Minecraft) (Object) this);
            SkullBlockEntity.setup(gameProfileCache, minecraftSessionService, this);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = MinecraftServer.spin(thread -> new IntegratedServer(thread, (Minecraft) (Object) this, levelStorageAccess, packRepository, worldStem, minecraftSessionService, gameProfileRepository, gameProfileCache, i -> {
                StoringChunkProgressListener storingChunkProgressListener = new StoringChunkProgressListener(i);
                this.progressListener.set(storingChunkProgressListener);
                return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
            }));
            this.isLocalServer = true;
        } catch (Throwable var19) {
            CrashReport minecraftSessionService = CrashReport.forThrowable(var19, "Starting integrated server");
            CrashReportCategory gameProfileRepository = minecraftSessionService.addCategory("Starting integrated server");
            gameProfileRepository.setDetail("Level ID", levelName);
            gameProfileRepository.setDetail("Level Name", worldData.getLevelName());
            throw new ReportedException(minecraftSessionService);
        }
    }

    @Unique
    private void joinSingleplayerServer() {
        SocketAddress minecraftSessionService = this.singleplayerServer.getConnection().startMemoryChannel();
        Connection pendingConnection = Connection.connectToLocalServer(minecraftSessionService);

        pendingConnection.setListener(
                new ClientHandshakePacketListenerImpl(
                        pendingConnection,
                        (Minecraft) (Object) this,
                        null,
                        component -> {
                        }
                )
        );

        pendingConnection.send(new ClientIntentionPacket(minecraftSessionService.toString(), 0, ConnectionProtocol.LOGIN));

        //this.pendingConnection must be set before sending ServerboundHelloPacket or a rare crash can occur
        this.pendingConnection = pendingConnection;
        pendingConnection.send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
    }
}
