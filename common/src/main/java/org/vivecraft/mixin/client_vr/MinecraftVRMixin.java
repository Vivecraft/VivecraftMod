package org.vivecraft.mixin.client_vr;


import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.*;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.extensions.*;
import org.vivecraft.client_vr.menuworlds.MenuWorldDownloader;
import org.vivecraft.client_vr.menuworlds.MenuWorldExporter;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.Xevents;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.common.utils.math.Vector3;
//import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
//import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
//import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, MinecraftExtension {

    @Unique
    private boolean lastClick;
    @Unique
    private ItemStack itemInHand; //Captured item

    public MinecraftVRMixin(String string) {
        super(string);
    }

    @Unique
    private long mirroNotifyStart;

    @Unique
    private long mirroNotifyLen;

    @Unique
    private boolean mirrorNotifyClear;

    @Unique
    private String mirrorNotifyText;

    @Unique
    private float fov = 1.0F;

    @Unique
    private long currentNanoTime;

    @Shadow
    protected int missTime;

    @Final
    @Shadow
    public Gui gui;

    @Shadow
    @Final
    public File gameDirectory;

    @Shadow
    public Options options;

    @Shadow
    public Screen screen;

    @Shadow
    private ProfilerFiller profiler;

    @Shadow
    @Final
    private Window window;

    @Shadow
    private Overlay overlay;

    @Final
    @Shadow
    public Font font;

    @Final
    @Shadow
    public static boolean ON_OSX;

    @Shadow
    private boolean pause;

    @Shadow
    private float pausePartialTick;

    @Final
    @Shadow
    private Timer timer;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    public ClientLevel level;

    @Shadow
    public RenderTarget mainRenderTarget;

    @Final
    @Shadow
    private SoundManager soundManager;

    @Shadow
    public boolean noRender;

    @Shadow
    public LocalPlayer player;

    @Shadow
    private ProfileResults fpsPieResults;

    @Shadow
    private int rightClickDelay;

    @Shadow
    public MultiPlayerGameMode gameMode;

    @Shadow
    private CompletableFuture<Void> pendingReload;

    @Shadow
    @Final
    private Queue<Runnable> progressTasks;

    @Shadow
    @Final
    public MouseHandler mouseHandler;

    @Shadow
    private int frames;

    @Shadow
    private IntegratedServer singleplayerServer;

    @Shadow
    @Final
    public FrameTimer frameTimer;

    @Shadow
    private long lastNanoTime;

    @Shadow
    private long lastTime;

    @Shadow
    public String fpsString;

    @Shadow
    private static int fps;

    @Shadow
    public abstract Entity getCameraEntity();

    @Shadow
    protected abstract void renderFpsMeter(PoseStack poseStack, ProfileResults fpsPieResults2);

    @Shadow
    public abstract boolean hasSingleplayerServer();

    @Shadow
    protected abstract int getFramerateLimit();

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract CompletableFuture<Void> reloadResourcePacks();

    @Shadow
    public abstract void stop();

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    protected abstract boolean startAttack();

    @Shadow
    public abstract RenderTarget getMainRenderTarget();

    @Shadow private double gpuUtilization;

    @Shadow @Nullable public abstract ClientPacketListener getConnection();

    @Shadow @Final public LevelRenderer levelRenderer;

    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow public abstract boolean isLocalServer();

    @Shadow public abstract IntegratedServer getSingleplayerServer();

    @Shadow @Final private ToastComponent toast;
    @Unique private List<String> resourcepacks;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0)
    public Overlay initVivecraft(Overlay overlay) {
        RenderPassManager.INSTANCE = new RenderPassManager((MainTarget) this.getMainRenderTarget());
        VRSettings.initSettings((Minecraft) (Object) this, this.gameDirectory);

        // register a resource reload listener, to reload the menu world
        resourceManager.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            List<String> newPacks = resourceManager.listPacks().map(PackResources::getName).toList();
            if ((resourcepacks == null || !resourcepacks.equals(newPacks)) &&
                ClientDataHolderVR.getInstance().menuWorldRenderer != null
                && ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()) {
                resourcepacks = newPacks;
                try {
                    ClientDataHolderVR.getInstance().menuWorldRenderer.destroy();
                    ClientDataHolderVR.getInstance().menuWorldRenderer.prepare();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        return overlay;
    }

    // on first resource load finished
    @Inject(at = @At("HEAD"), method = {
        "method_24040", // fabric
        "lambda$new$3"} // forge
        , remap = false)
    public void initVROnLaunch(CallbackInfo ci) {
        // init vr after resource loading
        try {
            if (ClientDataHolderVR.getInstance().vrSettings.vrEnabled) {
                VRState.vrEnabled = true;
                VRState.vrRunning = true;
                VRState.initializeVR();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // set initial resourcepacks
        resourcepacks = resourceManager.listPacks().map(PackResources::getName).toList();

        if (OptifineHelper.isOptifineLoaded() && ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()) {
            // with optifine this texture somehow fails to load, so manually reload it
            try {
                textureManager.getTexture(Gui.GUI_ICONS_LOCATION).load(resourceManager);
            } catch (IOException e) {
                // if there was an error, just reload everything
                reloadResourcePacks();
            }
        }
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;delayedCrash:Ljava/util/function/Supplier;", shift = Shift.BEFORE), method = "destroy()V")
    public void destroy(CallbackInfo info) {
        try {
            // the game crashed probably not because of us, so keep the vr choice
            VRState.destroyVR(false);
        } catch (Exception ignored) {
        }
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V", cancellable = true)
    public void replaceTick(boolean bl, CallbackInfo callback) {
        if (VRState.vrEnabled) {
            VRState.initializeVR();
        } else if (VRState.vrInitialized) {
                VRState.destroyVR(true);
                resizeDisplay();
        }
        if (!VRState.vrInitialized) {
            return;
        }
        boolean vrActive = !ClientDataHolderVR.getInstance().vrSettings.vrHotswitchingEnabled || ClientDataHolderVR.getInstance().vr.isActive();
        if (VRState.vrRunning != vrActive && (ClientNetworking.serverAllowsVrSwitching || player == null)) {
            VRState.vrRunning = vrActive;
            if (vrActive) {
                if (player != null) {
                    ClientDataHolderVR.getInstance().vrPlayer.snapRoomOriginToPlayerEntity(player, false, false);
                }
                // release mouse when switching to standing
                if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
                    mouseHandler.releaseMouse();
                    InputConstants.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_NORMAL, mouseHandler.xpos(), mouseHandler.ypos());
                }
            } else {
                GuiHandler.guiPos_room = null;
                GuiHandler.guiRotation_room = null;
                GuiHandler.guiScale = 1.0F;
                if (player != null) {
                    VRPlayersClient.getInstance().disableVR(player.getUUID());
                }
                if (gameRenderer != null) {
                    gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
                }
                // grab/release mouse
                if (screen != null || level == null) {
                    mouseHandler.releaseMouse();
                    InputConstants.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_NORMAL, mouseHandler.xpos(), mouseHandler.ypos());
                } else {
                    mouseHandler.grabMouse();
                    InputConstants.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_DISABLED, mouseHandler.xpos(), mouseHandler.ypos());
                }
            }
            var connection = this.getConnection();
            if (connection != null) {
                connection.send(ClientNetworking.createVRActivePacket(vrActive));
            }
            // reload sound manager, to toggle HRTF between VR and NONVR one
            if (!Minecraft.getInstance().getSoundManager().getAvailableSounds().isEmpty()) {
                Minecraft.getInstance().getSoundManager().reload();
            }
            resizeDisplay();
        }
        if (!VRState.vrRunning) {
            return;
        }
        if (SodiumHelper.isLoaded()) {
            SodiumHelper.preRenderMinecraft();
        }
        this.preRender(bl);
        this.newRunTick(bl);
        this.postRender();
        RenderPassManager.setVanillaRenderPass();
        if (SodiumHelper.isLoaded()) {
            SodiumHelper.postRenderMinecraft();
        }
        callback.cancel();
    }

    // the VR runtime handles the frame limit, no need to manually limit it 60fps
    @ModifyConstant(constant = @Constant(longValue = 16), method = "doWorldLoad", expect = 0)
    private long noWaitOnLevelLoadFabric(long constant) {
        if (VRState.vrRunning) {
            return 0L;
        }
        return constant;
    }

    //Replaces normal runTick
    public void newRunTick(boolean bl) {

        currentNanoTime = Util.getNanos();

        // v
        this.profiler.push("setupRenderConfiguration");
        //
        this.mouseHandler.turnPlayer();
        this.window.setErrorSection("Render");
        this.profiler.push("sound");
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.pop();
//		this.profiler.push("render");
//		PoseStack i = RenderSystem.getModelViewStack();
//		i.pushPose();
//		RenderSystem.applyModelViewMatrix();
//		RenderSystem.clear(16640, ON_OSX);
//		this.mainRenderTarget.bindWrite(true);

        // v
        try {
            this.checkGLError("pre render setup ");
            ClientDataHolderVR.getInstance().vrRenderer.setupRenderConfiguration();
            this.checkGLError("post render setup ");
        } catch (Exception exception1) {
            exception1.printStackTrace();
        }

        float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
        this.profiler.popPush("preRender");
        ClientDataHolderVR.getInstance().vrPlayer.preRender(f);
        this.profiler.popPush("2D");
        //

        FogRenderer.setupNoFog();
//		this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
//		this.profiler.pop();

        // v
        this.profiler.push("Gui");
        RenderPassManager.setGUIRenderPass();
        this.gameRenderer.getMainCamera().setup(this.level, this.getCameraEntity(), false, false, f);

        //

        if (!this.noRender) {
//			this.profiler.popPush("gameRenderer");
//			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
            Xevents.onRenderTickStart(this.pause ? this.pausePartialTick : this.timer.partialTick);
        }

//		if (this.fpsPieResults != null) {
//			this.profiler.push("fpsPie");
//			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
//			this.profiler.pop();
//		}

        // v
        this.profiler.push("gui setup");
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        this.mainRenderTarget = GuiHandler.guiFramebuffer;
        this.mainRenderTarget.clear(Minecraft.ON_OSX);
        this.mainRenderTarget.bindWrite(true);

        // draw screen/gui to buffer
        RenderSystem.getModelViewStack().pushPose();
        ((GameRendererExtension) this.gameRenderer).setShouldDrawScreen(true);
        // only draw the gui when the level was rendered once, since some mods expect that
        ((GameRendererExtension) this.gameRenderer).setShouldDrawGui(bl && this.entityRenderDispatcher.camera != null);

        this.gameRenderer.render(f, currentNanoTime, false);
        // draw cursor
        if (Minecraft.getInstance().screen != null) {
            int x = (int) (Minecraft.getInstance().mouseHandler.xpos() * (double) Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double) Minecraft.getInstance().getWindow().getScreenWidth());
            int y = (int) (Minecraft.getInstance().mouseHandler.ypos() * (double) Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double) Minecraft.getInstance().getWindow().getScreenHeight());
            ((GuiExtension) Minecraft.getInstance().gui).drawMouseMenuQuad(x, y);
        }
        // pre 1.19.4 toasts are not drawn in GameRenderer::render
        if (!this.noRender) {
            this.profiler.push("toasts");
            this.toast.render(new PoseStack());
            this.profiler.pop();
        }

        // draw debug pie
        drawProfiler();

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        // generate mipmaps
        // TODO: does this do anything?
        mainRenderTarget.bindRead();
        ((RenderTargetExtension) mainRenderTarget).genMipMaps();
        mainRenderTarget.unbindRead();

        this.profiler.popPush("2D Keyboard");
        if (KeyboardHandler.Showing
                && !ClientDataHolderVR.getInstance().vrSettings.physicalKeyboard) {
            this.mainRenderTarget = KeyboardHandler.Framebuffer;
            this.mainRenderTarget.clear(Minecraft.ON_OSX);
            this.mainRenderTarget.bindWrite(true);
            ((GameRendererExtension) this.gameRenderer).drawScreen(f,
                    KeyboardHandler.UI, new PoseStack());
        }
        //

//		this.profiler.push("blit");
//		this.mainRenderTarget.unbindWrite();
//		i.popPose();
//		i.pushPose();
//		RenderSystem.applyModelViewMatrix();
//		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
//		i.popPose();
//		RenderSystem.applyModelViewMatrix();
//		this.profiler.popPush("updateDisplay");
//		this.window.updateDisplay();

        // v
        this.profiler.popPush("Radial Menu");
        if (RadialHandler.isShowing()) {
            this.mainRenderTarget = RadialHandler.Framebuffer;
            this.mainRenderTarget.clear(Minecraft.ON_OSX);
            this.mainRenderTarget.bindWrite(true);
            ((GameRendererExtension) this.gameRenderer).drawScreen(f, RadialHandler.UI, new PoseStack());
        }

        this.profiler.pop();
        this.checkGLError("post 2d ");
        VRHotkeys.updateMovingThirdPersonCam();
        this.profiler.popPush("sound");
        ClientDataHolderVR.getInstance().currentPass = RenderPass.CENTER;
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.pop();
        //

//		if ((double) j < Option.FRAMERATE_LIMIT.getMaxValue()) {
//			RenderSystem.limitDisplayFPS(j);
//		}

        if (!this.noRender) {
            List<RenderPass> list = ClientDataHolderVR.getInstance().vrRenderer.getRenderPasses();

            ClientDataHolderVR.getInstance().isFirstPass = true;
            for (RenderPass renderpass : list) {
                ClientDataHolderVR.getInstance().currentPass = renderpass;

                switch (renderpass) {
                    case LEFT, RIGHT -> RenderPassManager.setWorldRenderPass(WorldRenderPass.stereoXR);
                    case CENTER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.center);
                    case THIRD -> RenderPassManager.setWorldRenderPass(WorldRenderPass.mixedReality);
                    case SCOPEL -> RenderPassManager.setWorldRenderPass(WorldRenderPass.leftTelescope);
                    case SCOPER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.rightTelescope);
                    case CAMERA -> RenderPassManager.setWorldRenderPass(WorldRenderPass.camera);
                }

                this.profiler.push("Eye:" + ClientDataHolderVR.getInstance().currentPass);
                this.profiler.push("setup");
                this.mainRenderTarget.bindWrite(true);
                this.profiler.pop();
                this.renderSingleView(renderpass, f, bl);
                this.profiler.pop();

                if (ClientDataHolderVR.getInstance().grabScreenShot) {
                    boolean flag;

                    if (list.contains(RenderPass.CAMERA)) {
                        flag = renderpass == RenderPass.CAMERA;
                    } else if (list.contains(RenderPass.CENTER)) {
                        flag = renderpass == RenderPass.CENTER;
                    } else {
                        flag = ClientDataHolderVR.getInstance().vrSettings.displayMirrorLeftEye ? renderpass == RenderPass.LEFT
                                : renderpass == RenderPass.RIGHT;
                    }

                    if (flag) {
                        RenderTarget rendertarget = this.mainRenderTarget;

                        if (renderpass == RenderPass.CAMERA) {
                            rendertarget = ClientDataHolderVR.getInstance().vrRenderer.cameraFramebuffer;
                        }

                        this.mainRenderTarget.unbindWrite();
                        Utils.takeScreenshot(rendertarget);
                        this.window.updateDisplay();
                        ClientDataHolderVR.getInstance().grabScreenShot = false;
                    }
                }

                ClientDataHolderVR.getInstance().isFirstPass = false;
            }

            ClientDataHolderVR.getInstance().vrPlayer.postRender(f);
            this.profiler.push("Display/Reproject");

            try {
                ClientDataHolderVR.getInstance().vrRenderer.endFrame();
            } catch (RenderConfigException exception) {
                VRSettings.logger.error(exception.toString());
            }

            this.profiler.pop();
            this.checkGLError("post submit ");

            if (!this.noRender) {
                Xevents.onRenderTickEnd(this.pause ? this.pausePartialTick : this.timer.partialTick);
            }

            this.profiler.push("mirror");
            this.mainRenderTarget.unbindWrite();
            this.copyToMirror();
            this.drawNotifyMirror();
            this.checkGLError("post-mirror ");
            this.profiler.pop();
        }
    }

    public void preRender(boolean tick) {
        this.window.setErrorSection("Pre render");
        if (this.window.shouldClose()) {
            this.stop();
        }

        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            CompletableFuture<Void> completableFuture = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> completableFuture.complete(null));
        }

        Runnable runnable;
        while ((runnable = this.progressTasks.poll()) != null) {
            runnable.run();
        }

        ++ClientDataHolderVR.getInstance().frameIndex;

        if (tick) {
            int i = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.pop();

            try {
                ClientDataHolderVR.getInstance().vrRenderer.setupRenderConfiguration();
            } catch (RenderConfigException renderConfigException) {
                // TODO: could disabling VR here cause issues?
                Minecraft.getInstance().setScreen(new ErrorScreen("VR Render Error", Component.translatable("vivecraft.messages.rendersetupfailed", renderConfigException.error + "\nVR provider: " + ClientDataHolderVR.getInstance().vr.getName())));
                VRState.destroyVR(true);
                return;
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }

            RenderPassManager.setGUIRenderPass();
            this.profiler.push("VR Poll/VSync");
            ClientDataHolderVR.getInstance().vr.poll(ClientDataHolderVR.getInstance().frameIndex);
            this.profiler.pop();
            ClientDataHolderVR.getInstance().vrPlayer.postPoll();

            this.profiler.push("tick");

            // reset camera position, if there is on, since it only gets set at the start of rendering, and the last renderpass can be anywhere
            if (gameRenderer != null && gameRenderer.getMainCamera() != null) {
                if (gameRenderer.getMainCamera().getEntity() != null) {
                    gameRenderer.getMainCamera().setPosition(gameRenderer.getMainCamera().getEntity().getEyePosition());
                } else if (player != null){
                    gameRenderer.getMainCamera().setPosition(player.getEyePosition());
                }
            }

            for (int j = 0; j < Math.min(10, i); ++j) {
                this.profiler.incrementCounter("clientTick");
                ClientDataHolderVR.getInstance().vrPlayer.preTick();
                this.tick();
                ClientDataHolderVR.getInstance().vrPlayer.postTick();
            }

            this.profiler.pop();
        } else {
            RenderPassManager.setGUIRenderPass();
            this.profiler.push("VR Poll/VSync");
            ClientDataHolderVR.getInstance().vr.poll(ClientDataHolderVR.getInstance().frameIndex);
            this.profiler.pop();
            ClientDataHolderVR.getInstance().vrPlayer.postPoll();
        }
    }

    private void handleBadConfig(RenderConfigException renderconfigexception) {
        // unbind the rendertarget, to draw directly to the screen
        this.mainRenderTarget.unbindWrite();
        this.screen = null;
        RenderSystem.viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());

        if (this.overlay != null) {
            RenderSystem.clear(256, ON_OSX);
            Matrix4f matrix4f = Matrix4f.orthographic(
                    0, (float) (this.window.getScreenWidth() / this.window.getGuiScale()),
                    (float) (this.window.getScreenHeight() / this.window.getGuiScale()), 0, 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(matrix4f);
            PoseStack p = new PoseStack();
            p.translate(0, 0, -2000);
            this.overlay.render(p, 0, 0, 0.0F);
        } else {
            if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_Q)) {
                System.out.println("Resetting VR status!");
                Path file = Xplat.getConfigPath("vivecraft-config.properties");

                Properties properties = new Properties();
                try {
                    properties.load(Files.newInputStream(file));
                } catch (IOException e) {
                }

                properties.setProperty("vrStatus", "false");
                try {
                    properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Minecraft.getInstance().stop();
            }
            this.notifyMirror(
                    LangHelper.get("vivecraft.messages.rendersetupfailed", renderconfigexception.error), true,
                    10000);
            this.drawNotifyMirror();

            if (ClientDataHolderVR.getInstance().frameIndex % 300L == 0L) {
                System.out.println(renderconfigexception.title + " " + renderconfigexception.error);
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException interruptedexception) {
            }
        }

        this.window.updateDisplay();
    }

    public void postRender() {
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        int k = this.getFramerateLimit();

        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean bl3 = this.hasSingleplayerServer()
                && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
                && !this.singleplayerServer.isPublished();
        if (this.pause != bl3) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            } else {
                this.timer.partialTick = this.pausePartialTick;
            }

            this.pause = bl3;
        }

        long n = Util.getNanos();
        long o = n - this.lastNanoTime;
//        if (bl2) {
//            this.savedCpuDuration = o;
//        }

        this.frameTimer.logFrameDuration(o);
        this.lastNanoTime = n;
        this.profiler.push("fpsUpdate");
//        if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
//            this.gpuUtilization = (double)this.currentFrameProfile.get() * 100.0 / (double)this.savedCpuDuration;
//        }

        while(Util.getMillis() >= this.lastTime + 1000L) {
            String string;
            if (this.gpuUtilization > 0.0) {
                string = " GPU: " + (this.gpuUtilization > 100.0 ? ChatFormatting.RED + "100%" : Math.round(this.gpuUtilization) + "%");
            } else {
                string = "";
            }

            fps = this.frames;
            this.fpsString = String.format(
                    Locale.ROOT,
                    "%d fps T: %s%s%s%s B: %d%s",
                    fps,
                    k == 260 ? "inf" : k,
                    this.options.enableVsync().get() ? " vsync" : "",
                    this.options.graphicsMode().get(),
                    this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
                    this.options.biomeBlendRadius().get(),
                    string
            );
            this.lastTime += 1000L;
            this.frames = 0;
        }

        this.profiler.pop();
    }

    @Inject(at = @At("HEAD"), method = "resizeDisplay")
    void restoreVanillaState(CallbackInfo ci) {
        if (VRState.vrInitialized) {
            // restore vanilla post chains before the resize, or it will resize the wrong ones
            if (levelRenderer != null) {
                ((LevelRendererExtension) levelRenderer).restoreVanillaPostChains();
            }
            RenderPassManager.setVanillaRenderPass();
        }
    }

    public void drawProfiler() {
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
            this.profiler.pop();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "continueAttack(Z)V")
    public void swingArmContinueAttack(LocalPlayer player, InteractionHand hand) {
        if (VRState.vrRunning) {
            ((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            player.swing(hand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"), method = "continueAttack(Z)V")
    public void destroyseated(MultiPlayerGameMode gm) {
        if (!VRState.vrRunning || ClientDataHolderVR.getInstance().vrSettings.seated || lastClick) {
            this.gameMode.stopDestroyBlock();
            lastClick = false;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), method = "startUseItem()V")
    public boolean seatedCheck(MultiPlayerGameMode gameMode) {
        return gameMode.isDestroying() && (!VRState.vrRunning || ClientDataHolderVR.getInstance().vrSettings.seated);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD), method = "startUseItem()V")
    public void breakDelay(CallbackInfo info) {
        if (VRState.vrRunning) {
            if (ClientDataHolderVR.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.VANILLA)
                this.rightClickDelay = 4;
            else if (ClientDataHolderVR.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOW)
                this.rightClickDelay = 6;
            else if (ClientDataHolderVR.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWER)
                this.rightClickDelay = 8;
            else if (ClientDataHolderVR.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWEST)
                this.rightClickDelay = 10;
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "startUseItem")
    public ItemStack handItemStore(ItemStack itemInHand) {
        this.itemInHand = itemInHand;
        return itemInHand;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem", locals = LocalCapture.CAPTURE_FAILHARD)
    public void activeHandSend(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand interactionHand) {
        if (VRState.vrRunning && (ClientDataHolderVR.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemInHand))) {
            ClientNetworking.sendActiveHand((byte) interactionHand.ordinal());
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem")
    public HitResult activeHand2(Minecraft instance) {
        if (!VRState.vrRunning || ClientDataHolderVR.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemInHand)) {
            return instance.hitResult;
        }
        return null;
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
    public void swingUse(LocalPlayer instance, InteractionHand interactionHand) {
        if (VRState.vrRunning) {
            ((PlayerExtension) instance).swingArm(interactionHand, VRFirstPersonArmSwing.Use);
        } else {
            instance.swing(interactionHand);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    public void vrTick(CallbackInfo info) {
        ++ClientDataHolderVR.getInstance().tickCounter;

        // general chat notifications
        if (this.level != null) {
            if (!ClientDataHolderVR.getInstance().showedUpdateNotification && UpdateChecker.hasUpdate && (ClientDataHolderVR.getInstance().vrSettings.alwaysShowUpdates || !UpdateChecker.newestVersion.equals(ClientDataHolderVR.getInstance().vrSettings.lastUpdate))) {
                ClientDataHolderVR.getInstance().vrSettings.lastUpdate = UpdateChecker.newestVersion;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
                ClientDataHolderVR.getInstance().showedUpdateNotification = true;
                this.gui.getChat().addMessage(Component.translatable("vivecraft.messages.updateAvailable", Component.literal(UpdateChecker.newestVersion).withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN))
                        .withStyle(style -> style
                        .withClickEvent(new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN, new UpdateScreen()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("vivecraft.messages.click")))));
            }
        }

        // VR enabled only chat notifications
        if (VRState.vrInitialized && this.level != null && ClientDataHolderVR.getInstance().vrPlayer != null) {
            if (ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer >= 0 && --ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer == 0) {
                boolean showMessage = !ClientNetworking.displayedChatWarning || ClientDataHolderVR.getInstance().vrSettings.showServerPluginMissingMessageAlways;

                if (ClientDataHolderVR.getInstance().vrPlayer.teleportWarning) {
                    if(showMessage)
                        this.gui.getChat().addMessage(Component.translatable("vivecraft.messages.noserverplugin"));
                    ClientDataHolderVR.getInstance().vrPlayer.teleportWarning = false;

                    // allow vr switching on vanilla server
                    ClientNetworking.serverAllowsVrSwitching = true;
                }
                if (ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning) {
                    if (showMessage)
                        this.gui.getChat().addMessage(Component.translatable("vivecraft.messages.novrhotswitchinglegacy"));
                    ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning = false;
                }
                ClientNetworking.displayedChatWarning = true;
            }
        }

        if (VRState.vrRunning) {

            if (ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()) {
                // update textures in the menu
                if (this.level == null) {
                    this.textureManager.tick();
                }
                ClientDataHolderVR.getInstance().menuWorldRenderer.tick();
            }

            this.profiler.push("vrProcessInputs");
            ClientDataHolderVR.getInstance().vr.processInputs();
            ClientDataHolderVR.getInstance().vr.processBindings();

            this.profiler.popPush("vrInputActionsTick");

            for (VRInputAction vrinputaction : ClientDataHolderVR.getInstance().vr.getInputActions()) {
                vrinputaction.tick();
            }

            if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                VRHotkeys.handleMRKeys();
            }

            if (this.level != null && ClientDataHolderVR.getInstance().vrPlayer != null) {
                ClientDataHolderVR.getInstance().vrPlayer.updateFreeMove();
            }
            this.profiler.pop();
        }

        this.profiler.push("vrPlayers");

        VRPlayersClient.getInstance().tick();

        if (VivecraftVRMod.INSTANCE.keyExportWorld.consumeClick() && level != null && player != null)
        {
            Throwable error = null;
            try
            {
                final BlockPos blockpos = player.blockPosition();
                int size = 320;
                int offset = size/2;
                File file1 = new File(MenuWorldDownloader.customWorldFolder);
                file1.mkdirs();
                int i = 0;

                while (true)
                {
                    final File file2 = new File(file1, "world" + i + ".mmw");

                    if (!file2.exists())
                    {
                        VRSettings.logger.info("Exporting world... area size: " + size);
                        VRSettings.logger.info("Saving to " + file2.getAbsolutePath());

                        if (isLocalServer())
                        {
                            final Level level = getSingleplayerServer().getLevel(player.level.dimension());
                            CompletableFuture<Throwable> completablefuture = getSingleplayerServer().submit(() -> {
                                try
                                {
                                    MenuWorldExporter.saveAreaToFile(level, blockpos.getX() - offset, blockpos.getZ() - offset, size, size, blockpos.getY(), file2);
                                }
                                catch (Throwable throwable)
                                {
                                    throwable.printStackTrace();
                                    return throwable;
                                }
                                return null;
                            });

                            error = completablefuture.get();
                        }
                        else
                        {
                            MenuWorldExporter.saveAreaToFile(level, blockpos.getX() - offset, blockpos.getZ() - offset, size, size, blockpos.getY(), file2);
                            gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexportclientwarning"));
                        }

                        if (error == null) {
                            gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexportcomplete.1", size));
                            gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexportcomplete.2", file2.getAbsolutePath()));
                        }
                        break;
                    }

                    ++i;
                }
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                error = throwable;
            } finally {
                if (error != null) {
                    gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexporterror", error.getMessage()));
                }
            }
        }

        this.profiler.pop();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick")
    public void removePick(GameRenderer instance, float f) {
        if (!VRState.vrRunning) {
            instance.pick(f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
    public void vrMirrorOption(Options instance, CameraType cameraType) {
        if (VRState.vrRunning) {
            ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
            this.notifyMirror(ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
            // this.levelRenderer.needsUpdate();
        } else {
            instance.setCameraType(cameraType);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
    public void noPosEffect(GameRenderer instance, Entity entity) {
        if (!VRState.vrRunning) {
            instance.checkEntityPostEffect(entity);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
    public void swingArmhandleKeybinds(LocalPlayer instance, InteractionHand interactionHand) {
        if (VRState.vrRunning) {
            ((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            instance.swing(interactionHand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2), method = "handleKeybinds")
    public boolean vrKeyuse(KeyMapping instance) {
        return !(!instance.isDown() && (!VRState.vrRunning || ((!ClientDataHolderVR.getInstance().bowTracker.isActive(this.player) || ClientDataHolderVR.getInstance().vrSettings.seated) && !ClientDataHolderVR.getInstance().autoFood.isEating())));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleKeybinds")
    public void activeHand(CallbackInfo ci) {
        if (VRState.vrRunning) {
            ClientNetworking.sendActiveHand((byte) this.player.getUsedItemHand().ordinal());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"), method = "handleKeybinds")
    public void attackDown(CallbackInfo ci) {
        // detect, if the attack buttun was used to testroy blocks
        this.lastClick = true;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), method = "handleKeybinds")
    public boolean vrAlwaysGrapped(MouseHandler instance) {
        return VRState.vrRunning || instance.isMouseGrabbed();
    }

    @Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    public void roomScale(ClientLevel pLevelClient, CallbackInfo info) {
        if (VRState.vrRunning) {
            ClientDataHolderVR.getInstance().vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
        }
    }

    @Inject(at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = At.Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void onOpenScreen(Screen pGuiScreen, CallbackInfo info) {
        GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
    }

    @Inject(at = @At("TAIL"), method = "setOverlay")
    public void onOverlaySet(Overlay overlay, CallbackInfo ci) {
        GuiHandler.onScreenChanged(this.screen, this.screen, true);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    public void onCloseScreen(Screen screen, CallbackInfo info) {
        if (screen == null) {
            GuiHandler.guiAppearOverBlockActive = false;
        }
    }

    private void drawNotifyMirror() {
        if (System.currentTimeMillis() < this.mirroNotifyStart + this.mirroNotifyLen) {
            RenderSystem.viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());
            Matrix4f matrix4f = Matrix4f.orthographic(0.0F, (float) this.window.getScreenWidth(),
                    (float) this.window.getScreenHeight(), 0.0F, 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(matrix4f);
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.getModelViewStack().translate(0, 0, -2000);
            RenderSystem.applyModelViewMatrix();
            PoseStack p = new PoseStack();
            p.scale(3, 3, 3);
            RenderSystem.clear(256, ON_OSX);

            if (this.mirrorNotifyClear) {
                RenderSystem.clearColor(0, 0, 0, 0);
                RenderSystem.clear(16384, ON_OSX);
            }

            int i = this.window.getScreenWidth() / 22;
            ArrayList<String> arraylist = new ArrayList<>();

            if (this.mirrorNotifyText != null) {
                Utils.wordWrap(this.mirrorNotifyText, i, arraylist);
            }

            int j = 1;
            int k = 12;

            for (String s : arraylist) {
                this.font.draw(p, s, 1.0F, (float) j, 16777215);
                j += 12;
            }
            RenderSystem.getModelViewStack().popPose();
        }
    }

    @Override
    public void notifyMirror(String text, boolean clear, int lengthMs) {
        this.mirroNotifyStart = System.currentTimeMillis();
        this.mirroNotifyLen = (long) lengthMs;
        this.mirrorNotifyText = text;
        this.mirrorNotifyClear = clear;
    }

    private void checkGLError(String string) {
        // TODO optifine
        if (GlStateManager._getError() != 0) {
            System.err.println(string);
        }

    }

    private void renderSingleView(RenderPass eye, float nano, boolean renderworld) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, ON_OSX);
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
        this.profiler.push("updateCameraAndRender");
        this.gameRenderer.render(nano, currentNanoTime, renderworld);
        this.profiler.pop();
        this.checkGLError("post game render " + eye.name());

        if (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT
                || ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT) {
            this.profiler.push("postprocesseye");
            RenderTarget rendertarget = this.mainRenderTarget;

            if (ClientDataHolderVR.getInstance().vrSettings.useFsaa) {
                RenderSystem.clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                if (eye == RenderPass.LEFT) {
                    ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0.bindWrite(true);
                } else {
                    ClientDataHolderVR.getInstance().vrRenderer.framebufferEye1.bindWrite(true);
                }
                RenderSystem.clear(16384, ON_OSX);
                this.profiler.push("fsaa");
                // DataHolder.getInstance().vrRenderer.doFSAA(Config.isShaders()); TODO
                ClientDataHolderVR.getInstance().vrRenderer.doFSAA(false);
                rendertarget = ClientDataHolderVR.getInstance().vrRenderer.fsaaLastPassResultFBO;
                this.checkGLError("fsaa " + eye.name());
                this.profiler.pop();
            }

            if (eye == RenderPass.LEFT) {
                ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0.bindWrite(true);
            } else {
                ClientDataHolderVR.getInstance().vrRenderer.framebufferEye1.bindWrite(true);
            }

            if (ClientDataHolderVR.getInstance().vrSettings.useFOVReduction
                    && ClientDataHolderVR.getInstance().vrPlayer.getFreeMove()) {
                if (this.player != null && (Math.abs(this.player.zza) > 0.0F || Math.abs(this.player.xxa) > 0.0F)) {
                    this.fov = (float) ((double) this.fov - 0.05D);

                    if (this.fov < ClientDataHolderVR.getInstance().vrSettings.fovReductionMin) {
                        this.fov = ClientDataHolderVR.getInstance().vrSettings.fovReductionMin;
                    }
                } else {
                    this.fov = (float) ((double) this.fov + 0.01D);

                    if ((double) this.fov > 0.8D) {
                        this.fov = 0.8F;
                    }
                }
            } else {
                this.fov = 1.0F;
            }

            VRShaders._FOVReduction_OffsetUniform.set(
                    ClientDataHolderVR.getInstance().vrSettings.fovRedutioncOffset);
            float red = 0.0F;
            float black = 0.0F;
            float blue = 0.0F;
            float time = (float) Util.getMillis() / 1000.0F;

            if (this.player != null && this.level != null) {
                if (((GameRendererExtension) this.gameRenderer)
                        .wasInWater() != ((GameRendererExtension) this.gameRenderer).isInWater()) {
                    ClientDataHolderVR.getInstance().watereffect = 2.3F;
                } else {
                    if (((GameRendererExtension) this.gameRenderer).isInWater()) {
                        ClientDataHolderVR.getInstance().watereffect -= 0.008333334F;
                    } else {
                        ClientDataHolderVR.getInstance().watereffect -= 0.016666668F;
                    }

                    if (ClientDataHolderVR.getInstance().watereffect < 0.0F) {
                        ClientDataHolderVR.getInstance().watereffect = 0.0F;
                    }
                }

                ((GameRendererExtension) this.gameRenderer)
                        .setWasInWater(((GameRendererExtension) this.gameRenderer).isInWater());

                if (Xplat
                        .isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                    if (!IrisHelper.hasWaterEffect()) {
                        ClientDataHolderVR.getInstance().watereffect = 0.0F;
                    }
                }

                if (((GameRendererExtension) this.gameRenderer).isInPortal()) {
                    ClientDataHolderVR.getInstance().portaleffect = 1.0F;
                } else {
                    ClientDataHolderVR.getInstance().portaleffect -= 0.016666668F;

                    if (ClientDataHolderVR.getInstance().portaleffect < 0.0F) {
                        ClientDataHolderVR.getInstance().portaleffect = 0.0F;
                    }
                }

                ItemStack itemstack = this.player.getInventory().getArmor(3);

                if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
                        && (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
                    ClientDataHolderVR.getInstance().pumpkineffect = 1.0F;
                } else {
                    ClientDataHolderVR.getInstance().pumpkineffect = 0.0F;
                }

                float hurtTimer = (float) this.player.hurtTime - nano;
                float healthpercent = 1.0F - this.player.getHealth() / this.player.getMaxHealth();
                healthpercent = (healthpercent - 0.5F) * 0.75F;

                if (hurtTimer > 0.0F) { // hurt flash
                    hurtTimer = hurtTimer / (float) this.player.hurtDuration;
                    hurtTimer = healthpercent
                            + Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * (float) Math.PI) * 0.5F;
                    red = hurtTimer;
                } else if (ClientDataHolderVR.getInstance().vrSettings.low_health_indicator) { // red due to low health
                    red = (float) ((double) healthpercent
                            * Math.abs(Math.sin((double) (2.5F * time) / ((double) (1.0F - healthpercent) + 0.1D))));

                    if (this.player.isCreative()) {
                        red = 0.0F;
                    }
                }

                float freeze = this.player.getPercentFrozen();
                if (freeze > 0) {
                    blue = red;
                    blue = Math.max(freeze / 2, blue);
                    red = 0;
                }

                if (this.player.isSleeping() && (double) black < 0.8D) {
                    black = 0.5F + 0.3F * this.player.getSleepTimer() * 0.01F;
                }

                if (ClientDataHolderVR.getInstance().vr.isWalkingAbout && (double) black < 0.8D) {
                    black = 0.5F;
                }
            } else {
                ClientDataHolderVR.getInstance().watereffect = 0.0F;
                ClientDataHolderVR.getInstance().portaleffect = 0.0F;
                ClientDataHolderVR.getInstance().pumpkineffect = 0.0F;
            }

            if (ClientDataHolderVR.getInstance().pumpkineffect > 0.0F) {
                VRShaders._FOVReduction_RadiusUniform.set(0.3F);
                VRShaders._FOVReduction_BorderUniform.set(0.0F);
            } else {
                VRShaders._FOVReduction_RadiusUniform.set(this.fov);
                VRShaders._FOVReduction_BorderUniform.set(0.06F);
            }

            VRShaders._Overlay_HealthAlpha.set(red);
            VRShaders._Overlay_FreezeAlpha.set(blue);
            VRShaders._Overlay_BlackAlpha.set(black);
            VRShaders._Overlay_time.set(time);
            VRShaders._Overlay_waterAmplitude.set(ClientDataHolderVR.getInstance().watereffect);
            VRShaders._Overlay_portalAmplitutde.set(ClientDataHolderVR.getInstance().portaleffect);
            VRShaders._Overlay_pumpkinAmplitutde.set(
                    ClientDataHolderVR.getInstance().pumpkineffect);
            RenderPass renderpass = ClientDataHolderVR.getInstance().currentPass;

            VRShaders._Overlay_eye.set(
                    ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT ? 1 : -1);
            ((RenderTargetExtension) rendertarget).blitFovReduction(VRShaders.fovReductionShader, ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0.viewWidth,
                    ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0.viewHeight);
            GlStateManager._glUseProgram(0);
            this.checkGLError("post overlay" + eye);
            this.profiler.pop();
        }

        if (ClientDataHolderVR.getInstance().currentPass == RenderPass.CAMERA) {
            this.profiler.push("cameracopy");
            ClientDataHolderVR.getInstance().vrRenderer.cameraFramebuffer.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(16640, ON_OSX);
            ((RenderTargetExtension) ClientDataHolderVR.getInstance().vrRenderer.cameraRenderFramebuffer).blitToScreen(0,
                    ClientDataHolderVR.getInstance().vrRenderer.cameraFramebuffer.viewWidth,
                    ClientDataHolderVR.getInstance().vrRenderer.cameraFramebuffer.viewHeight, 0, true, 0.0F, 0.0F, false);
            this.profiler.pop();
        }
    }

    private void copyToMirror() {
        // TODO: fix mixed reality... again
        if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF
                && ClientDataHolderVR.getInstance().vr.isHMDTracking()) {
            this.notifyMirror("Mirror is OFF", true, 1000);
        } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            if (VRShaders.depthMaskShader != null) {
                this.doMixedRealityMirror();
            } else {
                this.notifyMirror("Shader compile failed, see log", true, 10000);
            }
        } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.DUAL) {
            RenderTarget rendertarget = ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0;
            RenderTarget rendertarget1 = ClientDataHolderVR.getInstance().vrRenderer.framebufferEye1;

            if (rendertarget != null) {
                ((RenderTargetExtension) rendertarget).blitToScreen(0, this.window.getScreenWidth() / 2,
                        this.window.getScreenHeight(), 0, true, 0.0F, 0.0F, false);
            }

            if (rendertarget1 != null) {
                ((RenderTargetExtension) rendertarget1).blitToScreen(this.window.getScreenWidth() / 2,
                        this.window.getScreenWidth() / 2, this.window.getScreenHeight(), 0, true, 0.0F, 0.0F, false);
            }
        } else {
            float xcrop = 0.0F;
            float ycrop = 0.0F;
            boolean ar = false;
            RenderTarget source = ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0;

            if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
                source = ClientDataHolderVR.getInstance().vrRenderer.framebufferUndistorted;
            } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                source = ClientDataHolderVR.getInstance().vrRenderer.framebufferMR;
            } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.GUI) {
                source = GuiHandler.guiFramebuffer;
            } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.SINGLE
                    || ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF) {
                if (!ClientDataHolderVR.getInstance().vrSettings.displayMirrorLeftEye)
                    source = ClientDataHolderVR.getInstance().vrRenderer.framebufferEye1;
            } else if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.CROPPED) {
                if (!ClientDataHolderVR.getInstance().vrSettings.displayMirrorLeftEye)
                    source = ClientDataHolderVR.getInstance().vrRenderer.framebufferEye1;

                xcrop = 0.15F;
                ycrop = 0.15F;
                ar = true;
            }
            // Debug
            // source = GuiHandler.guiFramebuffer;
            // source = DataHolder.getInstance().vrRenderer.telescopeFramebufferR;
            //
            if (source != null) {
                ((RenderTargetExtension) source).blitToScreen(0, this.window.getScreenWidth(),
                        this.window.getScreenHeight(), 0, true, xcrop, ycrop, ar);
            }
        }
    }

    private void doMixedRealityMirror() {
//		boolean flag = Config.isShaders();
        boolean flag = false;
        boolean flag1 = ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike
                && ClientDataHolderVR.getInstance().vrSettings.mixedRealityAlphaMask;

        if (!flag1) {
            RenderSystem.clearColor(
                    (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
                    (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
                    (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F, 1.0F);
        } else {
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        }

        RenderSystem.clear(16640, ON_OSX);
        Vec3 vec3 = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getHeadPivot()
                .subtract(ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getPosition());
        Matrix4f matrix4f = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD)
                .getMatrix().transposed().toMCMatrix();
        Vector3 vector3 = ClientDataHolderVR.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix()
                .transform(Vector3.forward());
        VRShaders._DepthMask_projectionMatrix.set(((GameRendererExtension) this.gameRenderer).getThirdPassProjectionMatrix());
        VRShaders._DepthMask_viewMatrix.set(matrix4f);
        VRShaders._DepthMask_hmdViewPosition.set((float) vec3.x, (float) vec3.y,
                (float) vec3.z);
        VRShaders._DepthMask_hmdPlaneNormal.set(-vector3.getX(), 0.0F, -vector3.getZ());
        VRShaders._DepthMask_keyColorUniform.set(
                (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
                (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
                (float) ClientDataHolderVR.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F);
        VRShaders._DepthMask_alphaModeUniform.set(flag1 ? 1 : 0);
        RenderSystem.activeTexture(33985);
        RenderSystem.setShaderTexture(0, ClientDataHolderVR.getInstance().vrRenderer.framebufferMR.getColorTextureId());
        RenderSystem.activeTexture(33986);

//		if (flag && Shaders.dfb != null) { TODO
//			GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(0));
//		} else {
        RenderSystem.setShaderTexture(1, ClientDataHolderVR.getInstance().vrRenderer.framebufferMR.getDepthTextureId());

//		}

        RenderSystem.activeTexture(33984);

        for (int i = 0; i < (flag1 ? 3 : 2); ++i) {
            int j = this.window.getScreenWidth() / 2;
            int k = this.window.getScreenHeight();
            int l = this.window.getScreenWidth() / 2 * i;
            int i1 = 0;

            if (ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike) {
                j = this.window.getScreenWidth() / 2;
                k = this.window.getScreenHeight() / 2;

                if (ClientDataHolderVR.getInstance().vrSettings.mixedRealityAlphaMask && i == 2) {
                    l = this.window.getScreenWidth() / 2;
                    i1 = this.window.getScreenHeight() / 2;
                } else {
                    l = 0;
                    i1 = this.window.getScreenHeight() / 2 * (1 - i);
                }
            }

            VRShaders._DepthMask_resolutionUniform.set((float) j, (float) k);
            VRShaders._DepthMask_positionUniform.set((float) l, (float) i1);
            VRShaders._DepthMask_passUniform.set(i);
            ((RenderTargetExtension) ClientDataHolderVR.getInstance().vrRenderer.framebufferMR).blitToScreen(VRShaders.depthMaskShader, l, j, k, i1, true,
                    0.0F, 0.0F, false);
        }

        GlStateManager._glUseProgram(0);

        if (ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike) {
            if (ClientDataHolderVR.getInstance().vrSettings.mixedRealityUndistorted) {
                ((RenderTargetExtension) ClientDataHolderVR.getInstance().vrRenderer.framebufferUndistorted).blitToScreen(
                        this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
                        this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
            } else {
                ((RenderTargetExtension) ClientDataHolderVR.getInstance().vrRenderer.framebufferEye0).blitToScreen(
                        this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
                        this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
            }
        }
    }

}
