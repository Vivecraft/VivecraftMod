package org.vivecraft.titleworlds.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.*;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
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
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

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

    @Unique
    private boolean closingLevel;

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("Title World Loader");


    /**
     * no pausing in Titleworld please
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;isPauseScreen()Z"), method = "runTick")
    boolean noPauseInTitleworld(Screen instance){
        if (TitleWorldsMod.state.isTitleWorld) {
            return false;
        } else {
            return instance.isPauseScreen();
        }
    }

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
            if (VRState.vrInitialized && ClientDataHolderVR.getInstance().vrSettings.menuWorldSelection) {
                tryLoadTitleWorld();
            }
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
        TitleWorldsMod.LOGGER.info("Loading Title World");
        try {
            List<LevelStorageSource.LevelDirectory> list = TitleWorldsMod.levelSource.findLevelCandidates().levels();
            if (list.isEmpty()) {
                LOGGER.info("TitleWorlds folder is empty");
                return false;
            }
            this.loadTitleWorld(list.get(random.nextInt(list.size())).directoryName());
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

    /**
     * Instead of loading the world synchronously, we load as much as possible off thread
     * This allows 2 things
     * <p>
     * 1. The client thread can load the game while load files are being loaded in a different thread
     * 2. The game can render the progress screen
     * <p>
     * This does come at a cost of extra complexity but can cut down load times significantly
     * <p>
     * Each future is split at a point where synchronous access is needed
     * TODO is there a different way to do that?
     */
    @Unique
    private void loadTitleWorld(String levelName) throws ExecutionException, InterruptedException {
        LOGGER.info("Loading title world");
        TitleWorldsMod.state.isTitleWorld = true;
        TitleWorldsMod.state.pause = false;
        var worldResourcesFuture= CompletableFuture.supplyAsync(() -> {
                try {
                    return openWorldResources(levelName, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            });

        activeLoadingFuture = worldResourcesFuture;
        cleanup = () -> {
            try {
                var worldResources = worldResourcesFuture.get();
                worldResources.getA().close();
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
        if (worldResourcesFuture.get() == null) {
            return;
        }

        var worldResources = worldResourcesFuture.get();
        LevelStorageSource.LevelStorageAccess levelStorageAccess = worldResources.getA();
        PackRepository packRepository = worldResources.getB();
        WorldStem worldStem = worldResources.getC();

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
    private Triplet<LevelStorageSource.LevelStorageAccess, PackRepository, WorldStem> openWorldResources(
            String levelName,
            boolean vanillaOnly
    ) throws Exception{
        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = TitleWorldsMod.levelSource.createAccess(levelName);
        } catch (IOException var21) {
            throw new RuntimeException("Failed to read data");
        }

        var packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);

        return new Triplet<>(levelStorageAccess, packRepository, new WorldOpenFlows(Minecraft.getInstance(), TitleWorldsMod.levelSource).loadWorldStem(levelStorageAccess, vanillaOnly));
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
            RegistryAccess.Frozen registryAccess = worldStem.registries().compositeAccess();
            levelStorageAccess.saveDataTag(registryAccess, worldData);
            worldStem.dataPackResources().updateRegistryTags(registryAccess);

            Services services = Services.create(new YggdrasilAuthenticationService(this.proxy), this.gameDirectory);
            services.profileCache().setExecutor((Minecraft) (Object) this);
            SkullBlockEntity.setup(services, this);
            GameProfileCache.setUsesAuthentication(false);

            this.singleplayerServer = MinecraftServer.spin(thread -> new IntegratedServer(thread, (Minecraft) (Object) this, levelStorageAccess, packRepository, worldStem, services, i -> {
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
                        null,
                        false,
                        null,
                        component -> {
                        }
                )
        );

        pendingConnection.send(new ClientIntentionPacket(minecraftSessionService.toString(), 0, ConnectionProtocol.LOGIN));

        //this.pendingConnection must be set before sending ServerboundHelloPacket or a rare crash can occur
        this.pendingConnection = pendingConnection;
        pendingConnection.send(new ServerboundHelloPacket(this.getUser().getName(), Optional.ofNullable(this.getUser().getProfileId())));
    }

}
