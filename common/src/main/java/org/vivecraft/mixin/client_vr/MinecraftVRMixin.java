package org.vivecraft.mixin.client_vr;


import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xevents;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.VivecraftClickEvent.VivecraftAction;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.extensions.*;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.menuworlds.MenuWorldDownloader;
import org.vivecraft.client_vr.menuworlds.MenuWorldExporter;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.sodium.SodiumHelper;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Timer;
import net.minecraft.client.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FrameTimer;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.vivecraft.client.utils.Utils.*;
import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.forward;
import static org.vivecraft.common.utils.Utils.logger;

import static java.lang.Math.pow;
import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

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

    @Final
    @Shadow
    public Gui gui;

    @Final
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
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    public abstract Entity getCameraEntity();

    @Shadow
    protected abstract void renderFpsMeter(GuiGraphics guiGraphics, ProfileResults profileResults);

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
    public abstract RenderTarget getMainRenderTarget();

    @Shadow private double gpuUtilization;

    @Shadow @Nullable public abstract ClientPacketListener getConnection();

    @Shadow @Final public LevelRenderer levelRenderer;

    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Shadow public abstract boolean isLocalServer();

    @Shadow public abstract IntegratedServer getSingleplayerServer();

    @Shadow static Minecraft instance;

    @Shadow public static Minecraft getInstance() {
        return instance;
    }

    @Unique private List<String> resourcepacks;

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0)
    public Overlay initVivecraft(Overlay overlay) {
        mc = getInstance(); // Assign early during Vivecraft's initialization to ensure subsequent accesses are safe.
        RenderPassManager.INSTANCE = new RenderPassManager((MainTarget) this.getMainRenderTarget());
        VRSettings.initSettings();

        // register a resource reload listener, to reload the menu world
        this.resourceManager.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            List<String> newPacks = resourceManager.listPacks().map(PackResources::packId).toList();
            if (!newPacks.equals(this.resourcepacks) &&
                dh.menuWorldRenderer != null &&
                dh.menuWorldRenderer.isReady()
            )
            {
                this.resourcepacks = newPacks;
                try {
                    dh.menuWorldRenderer.destroy();
                    dh.menuWorldRenderer.prepare();
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
        "lambda$new$4"} // forge
        , remap = false)
    public void initVROnLaunch(CallbackInfo ci) {
        // init vr after resource loading
        try {
            if (dh.vrSettings.vrEnabled) {
                vrEnabled = true;
                vrRunning = true;
                initializeVR();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // set initial resourcepacks
        this.resourcepacks = this.resourceManager.listPacks().map(PackResources::packId).toList();

        if (OptifineHelper.isOptifineLoaded() && dh.menuWorldRenderer != null && dh.menuWorldRenderer.isReady()) {
            // with optifine this texture somehow fails to load, so manually reload it
            try {
                this.textureManager.getTexture(Gui.GUI_ICONS_LOCATION).load(this.resourceManager);
            } catch (IOException e) {
                // if there was an error, just reload everything
                this.reloadResourcePacks();
            }
        }
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;delayedCrash:Ljava/util/function/Supplier;", shift = Shift.BEFORE), method = "destroy()V")
    public void destroy(CallbackInfo info) {
        try {
            // the game crashed probably not because of us, so keep the vr choice
            destroyVR(false);
        } catch (Exception ignored) {
        }
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V", cancellable = true)
    public void replaceTick(boolean bl, CallbackInfo callback) {
        if (vrEnabled) {
            initializeVR();
        } else if (vrInitialized) {
            destroyVR(true);
            this.resizeDisplay();
        }
        if (!vrInitialized) {
            return;
        }
        boolean vrActive = !dh.vrSettings.vrHotswitchingEnabled || dh.vr.isActive();
        if (vrRunning != vrActive && (ClientNetworking.serverAllowsVrSwitching || this.player == null)) {
            vrRunning = vrActive;
            if (vrActive) {
                if (this.player != null) {
                    dh.vrPlayer.snapRoomOriginToPlayerEntity(false, false);
                }
                // release mouse when switching to standing
                if (!dh.vrSettings.seated) {
                    this.mouseHandler.releaseMouse();
                    InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_NORMAL, this.mouseHandler.xpos(), this.mouseHandler.ypos());
                }
            } else {
                GuiHandler.guiPos_room = null;
                GuiHandler.guiRotation_room = null;
                GuiHandler.guiScale = 1.0F;
                if (this.player != null) {
                    VRPlayersClient.getInstance().disableVR(this.player.getUUID());
                }
                if (this.gameRenderer != null) {
                    this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
                }
                // grab/release mouse
                if (this.screen != null || this.level == null) {
                    this.mouseHandler.releaseMouse();
                    InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_NORMAL, this.mouseHandler.xpos(), this.mouseHandler.ypos());
                } else {
                    this.mouseHandler.grabMouse();
                    InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_DISABLED, this.mouseHandler.xpos(), this.mouseHandler.ypos());
                }
            }
            var connection = this.getConnection();
            if (connection != null) {
                connection.send(ClientNetworking.createVRActivePacket(vrActive));
            }
            // reload sound manager, to toggle HRTF between VR and NONVR one
            if (!instance.getSoundManager().getAvailableSounds().isEmpty()) {
                instance.getSoundManager().reload();
            }
            this.resizeDisplay();
        }
        if (!vrRunning) {
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
        if (vrRunning) {
            return 0L;
        }
        return constant;
    }

    //Replaces normal runTick
    public void newRunTick(boolean bl) {

        this.currentNanoTime = Util.getNanos();

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
//		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
//		this.mainRenderTarget.bindWrite(true);

        // v
        try {
            this.checkGLError("pre render setup ");
            dh.vrRenderer.setupRenderConfiguration();
            this.checkGLError("post render setup ");
        } catch (Exception exception1) {
            exception1.printStackTrace();
        }

        float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
        this.profiler.popPush("preRender");
        dh.vrPlayer.preRender(f);
        this.profiler.popPush("2D");
        //

        FogRenderer.setupNoFog();
//		this.profiler.push("display");
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
        this.mainRenderTarget.clear(ON_OSX);
        this.mainRenderTarget.bindWrite(true);

        // draw screen/gui to buffer
        RenderSystem.getModelViewStack().pushPose();
        ((GameRendererExtension) this.gameRenderer).setShouldDrawScreen(true);
        // only draw the gui when the level was rendered once, since some mods expect that
        ((GameRendererExtension) this.gameRenderer).setShouldDrawGui(bl && this.entityRenderDispatcher.camera != null);

        this.gameRenderer.render(f, this.currentNanoTime, false);
        // draw cursor
        if (instance.screen != null) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.setIdentity();
            poseStack.last().pose().translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix();

            int x = (int) (instance.mouseHandler.xpos() * instance.getWindow().getGuiScaledWidth() / instance.getWindow().getScreenWidth());
            int y = (int) (instance.mouseHandler.ypos() * instance.getWindow().getGuiScaledHeight() / instance.getWindow().getScreenHeight());
            ((GuiExtension) instance.gui).drawMouseMenuQuad(x, y);

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
        // draw debug pie
        this.drawProfiler();

        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        // generate mipmaps
        // TODO: does this do anything?
        this.mainRenderTarget.bindRead();
        ((RenderTargetExtension) this.mainRenderTarget).genMipMaps();
        this.mainRenderTarget.unbindRead();

        this.profiler.popPush("2D Keyboard");
        GuiGraphics guiGraphics = new GuiGraphics(instance, this.renderBuffers.bufferSource());
        if (KeyboardHandler.isShowing()
                && !dh.vrSettings.physicalKeyboard) {
            this.mainRenderTarget = KeyboardHandler.Framebuffer;
            this.mainRenderTarget.clear(ON_OSX);
            this.mainRenderTarget.bindWrite(true);
            ((GameRendererExtension) this.gameRenderer).drawScreen(f,
                    KeyboardHandler.UI, guiGraphics);
            guiGraphics.flush();
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
            this.mainRenderTarget.clear(ON_OSX);
            this.mainRenderTarget.bindWrite(true);
            ((GameRendererExtension) this.gameRenderer).drawScreen(f, RadialHandler.UI, guiGraphics);
            guiGraphics.flush();
        }

        this.profiler.pop();
        this.checkGLError("post 2d ");
        VRHotkeys.updateMovingThirdPersonCam();
        this.profiler.popPush("sound");
        dh.currentPass = RenderPass.CENTER;
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.pop();
        //

//		if ((double) j < Option.FRAMERATE_LIMIT.getMaxValue()) {
//			RenderSystem.limitDisplayFPS(j);
//		}

        if (!this.noRender) {
            List<RenderPass> list = dh.vrRenderer.getRenderPasses();

            dh.isFirstPass = true;
            for (RenderPass renderpass : list) {
                dh.currentPass = renderpass;

                switch (renderpass) {
                    case LEFT, RIGHT -> RenderPassManager.setWorldRenderPass(WorldRenderPass.stereoXR);
                    case CENTER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.center);
                    case THIRD -> RenderPassManager.setWorldRenderPass(WorldRenderPass.mixedReality);
                    case SCOPEL -> RenderPassManager.setWorldRenderPass(WorldRenderPass.leftTelescope);
                    case SCOPER -> RenderPassManager.setWorldRenderPass(WorldRenderPass.rightTelescope);
                    case CAMERA -> RenderPassManager.setWorldRenderPass(WorldRenderPass.camera);
                }

                this.profiler.push("Eye:" + dh.currentPass);
                this.profiler.push("setup");
                this.mainRenderTarget.bindWrite(true);
                this.profiler.pop();
                this.renderSingleView(f, bl);
                this.profiler.pop();

                if (dh.grabScreenShot &&
                    switch(renderpass){
                        case CAMERA, CENTER -> list.contains(renderpass);
                        case LEFT -> dh.vrSettings.displayMirrorLeftEye;
                        case RIGHT -> !dh.vrSettings.displayMirrorLeftEye;
                        default -> false;
                    }
                )
                {
                    this.mainRenderTarget.unbindWrite();
                    takeScreenshot(renderpass == RenderPass.CAMERA ?
                        dh.vrRenderer.cameraFramebuffer :
                        this.mainRenderTarget
                    );
                    this.window.updateDisplay();
                    dh.grabScreenShot = false;
                }

                dh.isFirstPass = false;
            }

            dh.vrPlayer.postRender(f);
            this.profiler.push("Display/Reproject");

            try {
                dh.vrRenderer.endFrame();
            } catch (RenderConfigException exception) {
                logger.error(exception.toString());
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

        ++dh.frameIndex;

        if (tick) {
            int i = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.pop();

            try {
                dh.vrRenderer.setupRenderConfiguration();
            } catch (RenderConfigException renderConfigException) {
                // TODO: could disabling VR here cause issues?
                instance.setScreen(new ErrorScreen("VR Render Error", Component.translatable("vivecraft.messages.rendersetupfailed", renderConfigException.error + "\nVR provider: " + dh.vr.getName())));
                destroyVR(true);
                return;
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }

            RenderPassManager.setGUIRenderPass();
            this.profiler.push("VR Poll/VSync");
            dh.vr.poll(dh.frameIndex);
            this.profiler.pop();
            dh.vrPlayer.postPoll();

            this.profiler.push("tick");

            // reset camera position, if there is on, since it only gets set at the start of rendering, and the last renderpass can be anywhere
            if (this.gameRenderer != null && this.gameRenderer.getMainCamera() != null) {
                if (this.gameRenderer.getMainCamera().getEntity() != null) {
                    this.gameRenderer.getMainCamera().setPosition(this.gameRenderer.getMainCamera().getEntity().getEyePosition());
                } else if (this.player != null){
                    this.gameRenderer.getMainCamera().setPosition(this.player.getEyePosition());
                }
            }

            for (int j = 0; j < min(10, i); ++j) {
                this.profiler.incrementCounter("clientTick");
                dh.vrPlayer.preTick();
                this.tick();
                dh.vrPlayer.postTick();
            }

            this.profiler.pop();
        } else {
            RenderPassManager.setGUIRenderPass();
            this.profiler.push("VR Poll/VSync");
            dh.vr.poll(dh.frameIndex);
            this.profiler.pop();
            dh.vrPlayer.postPoll();
        }
    }

    private void handleBadConfig(RenderConfigException renderconfigexception) {
        // unbind the rendertarget, to draw directly to the screen
        this.mainRenderTarget.unbindWrite();
        this.screen = null;
        RenderSystem.viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());

        /*if (this.overlay != null) {
            RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
            Matrix4f matrix4f = new Matrix4f().setOrtho(
                    0, (float) (this.window.getScreenWidth() / this.window.getGuiScale()),
                    (float) (this.window.getScreenHeight() / this.window.getGuiScale()), 0, 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack p = new PoseStack();
            p.translate(0, 0, -2000);
            this.overlay.render(p, 0, 0, 0.0F);
        } else */{
            if (VRHotkeys.isKeyDown(GLFW_KEY_Q)) {
                logger.warn("Resetting VR status!");
                Path file = Xplat.getConfigPath("vivecraft-config.properties");

                Properties properties = new Properties();
                try {
                    properties.load(Files.newInputStream(file));
                } catch (IOException ignored) {
                }

                properties.setProperty("vrStatus", "false");
                try {
                    properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                instance.stop();
            }
            this.notifyMirror(
                    LangHelper.get("vivecraft.messages.rendersetupfailed", renderconfigexception.error), true,
                    10000);
            this.drawNotifyMirror();

            if (dh.frameIndex % 300L == 0L) {
                logger.error(renderconfigexception.title + " " + renderconfigexception.error);
            }

            try {
                Thread.sleep(10L);
            } catch (InterruptedException ignored) {
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
            String string = (this.gpuUtilization > 0.0 ?
                " GPU: " + (this.gpuUtilization > 100.0 ? ChatFormatting.RED + "100%" : round(this.gpuUtilization) + "%") :
                ""
            );

            fps = this.frames;
            this.fpsString = String.format(
                Locale.ROOT,
                "%d fps T: %s%s%s%s B: %d%s",
                fps,
                k == 260 ? "inf" : k,
                this.options.enableVsync().get() ? " vsync" : "",
                this.options.graphicsMode().get(),
                switch(this.options.cloudStatus().get()){
                    case FAST -> " fast-clouds";
                    case FANCY -> " fancy-clouds";
                    default -> ""; // OFF
                },
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
        if (vrInitialized) {
            // restore vanilla post chains before the resize, or it will resize the wrong ones
            if (this.levelRenderer != null) {
                ((LevelRendererExtension) this.levelRenderer).restoreVanillaPostChains();
            }
            RenderPassManager.setVanillaRenderPass();
        }
    }

    public void drawProfiler() {
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            GuiGraphics guiGraphics = new GuiGraphics(instance, this.renderBuffers.bufferSource());
            this.renderFpsMeter(guiGraphics, this.fpsPieResults);
            guiGraphics.flush();
            this.profiler.pop();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "continueAttack(Z)V")
    public void swingArmContinueAttack(LocalPlayer player, InteractionHand hand) {
        if (vrRunning) {
            ((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            player.swing(hand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"), method = "continueAttack(Z)V")
    public void destroyseated(MultiPlayerGameMode gm) {
        if (!vrRunning || dh.vrSettings.seated || this.lastClick) {
            this.gameMode.stopDestroyBlock();
            this.lastClick = false;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), method = "startUseItem()V")
    public boolean seatedCheck(MultiPlayerGameMode gameMode) {
        return gameMode.isDestroying() && (!vrRunning || dh.vrSettings.seated);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER, opcode = PUTFIELD), method = "startUseItem()V")
    public void breakDelay(CallbackInfo info) {
        if (vrRunning) {
            switch(dh.vrSettings.rightclickDelay)
            {
                case SLOW -> this.rightClickDelay = 6;
                case SLOWER -> this.rightClickDelay = 8;
                case SLOWEST -> this.rightClickDelay = 10;
                default -> this.rightClickDelay = 4;
            }
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "startUseItem")
    public ItemStack handItemStore(ItemStack itemInHand) {
        this.itemInHand = itemInHand;
        return itemInHand;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem", locals = LocalCapture.CAPTURE_FAILHARD)
    public void activeHandSend(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand interactionHand) {
        if (vrRunning && (dh.vrSettings.seated || !TelescopeTracker.isTelescope(this.itemInHand))) {
            ClientNetworking.sendActiveHand((byte) interactionHand.ordinal());
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem")
    public HitResult activeHand2(Minecraft instance) {
        if (!vrRunning || dh.vrSettings.seated || !TelescopeTracker.isTelescope(this.itemInHand)) {
            return instance.hitResult;
        }
        return null;
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
    public void swingUse(LocalPlayer instance, InteractionHand interactionHand) {
        if (vrRunning) {
            ((PlayerExtension) instance).swingArm(interactionHand, VRFirstPersonArmSwing.Use);
        } else {
            instance.swing(interactionHand);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    public void vrTick(CallbackInfo info) {
        ++dh.tickCounter;

        // general chat notifications
        if (this.level != null) {
            if (!dh.showedUpdateNotification && UpdateChecker.hasUpdate && (dh.vrSettings.alwaysShowUpdates || !UpdateChecker.newestVersion.equals(dh.vrSettings.lastUpdate))) {
                dh.vrSettings.lastUpdate = UpdateChecker.newestVersion;
                dh.vrSettings.saveOptions();
                dh.showedUpdateNotification = true;
                message(Component.translatable("vivecraft.messages.updateAvailable", Component.literal(UpdateChecker.newestVersion).withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN))
                        .withStyle(style -> style
                        .withClickEvent(new VivecraftClickEvent(VivecraftAction.OPEN_SCREEN, new UpdateScreen()))
                        .withHoverEvent(new HoverEvent(Action.SHOW_TEXT, Component.translatable("vivecraft.messages.click")))));
            }
        }

        // VR enabled only chat notifications
        if (vrInitialized && this.level != null && dh.vrPlayer != null) {
            if (dh.vrPlayer.chatWarningTimer >= 0 && --dh.vrPlayer.chatWarningTimer == 0) {
                boolean showMessage = !ClientNetworking.displayedChatWarning || dh.vrSettings.showServerPluginMissingMessageAlways;

                if (dh.vrPlayer.teleportWarning) {
                    if(showMessage)
                        message(Component.translatable("vivecraft.messages.noserverplugin"));
                    dh.vrPlayer.teleportWarning = false;

                    // allow vr switching on vanilla server
                    ClientNetworking.serverAllowsVrSwitching = true;
                }
                if (dh.vrPlayer.vrSwitchWarning) {
                    if (showMessage)
                        message(Component.translatable("vivecraft.messages.novrhotswitchinglegacy"));
                    dh.vrPlayer.vrSwitchWarning = false;
                }
                ClientNetworking.displayedChatWarning = true;
            }
        }

        if (vrRunning) {

            if (dh.menuWorldRenderer.isReady()) {
                // update textures in the menu
                if (this.level == null) {
                    this.textureManager.tick();
                }
                dh.menuWorldRenderer.tick();
            }

            this.profiler.push("vrProcessInputs");
            dh.vr.processInputs();
            dh.vr.processBindings();

            this.profiler.popPush("vrInputActionsTick");

            for (VRInputAction vrinputaction : dh.vr.getInputActions()) {
                vrinputaction.tick();
            }

            if (dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY || dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON) {
                VRHotkeys.handleMRKeys();
            }

            if (this.level != null && dh.vrPlayer != null) {
                dh.vrPlayer.updateFreeMove();
            }
            this.profiler.pop();
        }

        this.profiler.push("vrPlayers");

        VRPlayersClient.getInstance().tick();

        if (VivecraftVRMod.keyExportWorld.consumeClick() && this.level != null && this.player != null)
        {
            Throwable error = null;
            try
            {
                final BlockPos blockpos = this.player.blockPosition();
                int size = 320;
                int offset = size/2;
                File file1 = new File(MenuWorldDownloader.customWorldFolder);
                file1.mkdirs();
                int i = 0;
                for (File fileChecked = new File(file1, "world" + i + ".mmw");
                     !fileChecked.exists();
                     fileChecked = new File(file1, "world" + (++i) + ".mmw")
                ){
                    final File file2 = fileChecked;
                    logger.info("Exporting world... area size: " + size);
                    logger.info("Saving to " + file2.getAbsolutePath());

                    if (this.isLocalServer())
                    {
                        final Level level = this.getSingleplayerServer().getLevel(this.player.level().dimension());
                        CompletableFuture<Throwable> completablefuture = this.getSingleplayerServer().submit(() -> {
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
                        MenuWorldExporter.saveAreaToFile(this.level, blockpos.getX() - offset, blockpos.getZ() - offset, size, size, blockpos.getY(), file2);
                        message(Component.translatable("vivecraft.messages.menuworldexportclientwarning"));
                    }

                    if (error == null)
                    {
                        message(Component.translatable("vivecraft.messages.menuworldexportcomplete.1", size));
                        message(Component.translatable("vivecraft.messages.menuworldexportcomplete.2", file2.getAbsolutePath()));
                    }
                }
            }
            catch (Throwable throwable)
            {
                throwable.printStackTrace();
                error = throwable;
            } finally {
                if (error != null) {
                    message(Component.translatable("vivecraft.messages.menuworldexporterror", error.getMessage()));
                }
            }
        }

        this.profiler.pop();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick")
    public void removePick(GameRenderer instance, float f) {
        if (!vrRunning) {
            instance.pick(f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
    public void vrMirrorOption(Options instance, CameraType cameraType) {
        if (vrRunning) {
            dh.vrSettings.setOptionValue(VrOptions.MIRROR_DISPLAY);
            this.notifyMirror(dh.vrSettings.getButtonDisplayString(VrOptions.MIRROR_DISPLAY), false, 3000);
            // this.levelRenderer.needsUpdate();
        } else {
            instance.setCameraType(cameraType);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
    public void noPosEffect(GameRenderer instance, Entity entity) {
        if (!vrRunning) {
            instance.checkEntityPostEffect(entity);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
    public void swingArmhandleKeybinds(LocalPlayer instance, InteractionHand interactionHand) {
        if (vrRunning) {
            ((PlayerExtension) this.player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            instance.swing(interactionHand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2), method = "handleKeybinds")
    public boolean vrKeyuse(KeyMapping instance) {
        return !(!instance.isDown() && (!vrRunning || ((!dh.bowTracker.isActive() || dh.vrSettings.seated) && !dh.eatingTracker.isEating())));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleKeybinds")
    public void activeHand(CallbackInfo ci) {
        if (vrRunning) {
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
        return vrRunning || instance.isMouseGrabbed();
    }

    @Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    public void roomScale(ClientLevel pLevelClient, CallbackInfo info) {
        if (vrRunning) {
            dh.vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
        }
    }

    @Inject(at = @At(value = "FIELD", opcode = PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
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
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(
                0.0F,
                this.window.getScreenWidth(),
                this.window.getScreenHeight(),
                0.0F,
                1000.0F,
                3000.0F
            ), VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().setIdentity();
            RenderSystem.getModelViewStack().last().pose().translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);

            if (this.mirrorNotifyClear) {
                RenderSystem.clearColor(0, 0, 0, 0);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
            }

            int i = this.window.getScreenWidth() / 22;
            ArrayList<String> arraylist = new ArrayList<>();

            if (this.mirrorNotifyText != null) {
                wordWrap(this.mirrorNotifyText, i, arraylist);
            }

            int j = 1;
            int k = 12;

            GuiGraphics guiGraphics = new GuiGraphics(instance, this.renderBuffers.bufferSource());
            for (String s : arraylist) {
                guiGraphics.drawString(this.font, s, 1, j, 16777215);
                j += 12;
            }
            guiGraphics.flush();
            RenderSystem.getModelViewStack().popPose();
        }
    }

    @Override
    public void notifyMirror(String text, boolean clear, int lengthMs) {
        this.mirroNotifyStart = System.currentTimeMillis();
        this.mirroNotifyLen = lengthMs;
        this.mirrorNotifyText = text;
        this.mirrorNotifyClear = clear;
    }

    private void checkGLError(String string) {
        // TODO optifine
        if (GlStateManager._getError() != 0) {
            System.err.println(string);
        }

    }

    private void renderSingleView(float nano, boolean renderworld) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
        RenderSystem.enableDepthTest();
        this.profiler.push("updateCameraAndRender");
        this.gameRenderer.render(nano, this.currentNanoTime, renderworld);
        this.profiler.pop();
        this.checkGLError("post game render " + dh.currentPass.name());
        switch(dh.currentPass){
            case LEFT, RIGHT ->
            {
                this.profiler.push("postprocesseye");
                RenderTarget rendertarget = this.mainRenderTarget;

                if (dh.vrSettings.useFsaa) {
                    RenderSystem.clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                    if (dh.currentPass == RenderPass.LEFT) {
                        dh.vrRenderer.framebufferEye0.bindWrite(true);
                    } else {
                        dh.vrRenderer.framebufferEye1.bindWrite(true);
                    }
                    RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
                    this.profiler.push("fsaa");
                    // DataHolder.getInstance().vrRenderer.doFSAA(Config.isShaders()); TODO
                    dh.vrRenderer.doFSAA(false);
                    rendertarget = dh.vrRenderer.fsaaLastPassResultFBO;
                    this.checkGLError("fsaa " + dh.currentPass.name());
                    this.profiler.pop();
                }

                if (dh.currentPass == RenderPass.LEFT) {
                    dh.vrRenderer.framebufferEye0.bindWrite(true);
                } else {
                    dh.vrRenderer.framebufferEye1.bindWrite(true);
                }

                if (dh.vrSettings.useFOVReduction && dh.vrPlayer.getFreeMove()) {
                    if ((this.player != null) && ((abs(this.player.zza) > 0.0F) || (abs(this.player.xxa) > 0.0F))) {
                        this.fov = (float) (this.fov - 0.05D);

                        if (this.fov < dh.vrSettings.fovReductionMin) {
                            this.fov = dh.vrSettings.fovReductionMin;
                        }
                    } else {
                        this.fov = (float) (this.fov + 0.01D);

                        if (this.fov > 0.8D) {
                            this.fov = 0.8F;
                        }
                    }
                } else {
                    this.fov = 1.0F;
                }

                VRShaders._FOVReduction_OffsetUniform.set(dh.vrSettings.fovRedutioncOffset);
                float red = 0.0F;
                float black = 0.0F;
                float blue = 0.0F;
                float time = Util.getMillis() / 1000.0F;

                if (this.player != null && this.level != null) {
                    GameRendererExtension GRE = (GameRendererExtension) this.gameRenderer;
                    if (GRE.wasInWater() != GRE.isInWater()) {
                        dh.watereffect = 2.3F;
                    } else {
                        if (GRE.isInWater()) {
                            dh.watereffect -= 0.008333334F;
                        } else {
                            dh.watereffect -= 0.016666668F;
                        }

                        if (dh.watereffect < 0.0F) {
                            dh.watereffect = 0.0F;
                        }
                    }

                    GRE.setWasInWater(GRE.isInWater());

                    if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                        if (!IrisHelper.hasWaterEffect()) {
                            dh.watereffect = 0.0F;
                        }
                    }

                    if (GRE.isInPortal()) {
                        dh.portaleffect = 1.0F;
                    } else {
                        dh.portaleffect -= 0.016666668F;

                        if (dh.portaleffect < 0.0F) {
                            dh.portaleffect = 0.0F;
                        }
                    }

                    ItemStack itemstack = this.player.getInventory().getArmor(3);

                    if ((itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem())
                        && (!itemstack.hasTag() || (itemstack.getTag().getInt("CustomModelData") == 0))) {
                        dh.pumpkineffect = 1.0F;
                    } else {
                        dh.pumpkineffect = 0.0F;
                    }

                    float hurtTimer = this.player.hurtTime - nano;
                    float healthpercent = 1.0F - (this.player.getHealth() / this.player.getMaxHealth());
                    healthpercent = (healthpercent - 0.5F) * 0.75F;

                    if (hurtTimer > 0.0F) { // hurt flash
                        hurtTimer = hurtTimer / this.player.hurtDuration;
                        hurtTimer = fma((float)sin(pow(hurtTimer, 4) * PI), 0.5F, healthpercent);
                        red = hurtTimer;
                    } else if (dh.vrSettings.low_health_indicator) { // red due to low health
                        red = (float) (healthpercent * abs(sin((2.5F * time) / ((1.0F - healthpercent) + 0.1D))));

                        if (this.player.isCreative()) {
                            red = 0.0F;
                        }
                    }

                    float freeze = this.player.getPercentFrozen();
                    if (freeze > 0) {
                        blue = red;
                        blue = max(freeze / 2, blue);
                        red = 0;
                    }

                    if (this.player.isSleeping() && black < 0.8D) {
                        black = 0.5F + 0.3F * this.player.getSleepTimer() * 0.01F;
                    }

                    if (dh.vr.isWalkingAbout && black < 0.8D) {
                        black = 0.5F;
                    }
                } else {
                    dh.watereffect = 0.0F;
                    dh.portaleffect = 0.0F;
                    dh.pumpkineffect = 0.0F;
                }

                if (dh.pumpkineffect > 0.0F) {
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
                VRShaders._Overlay_waterAmplitude.set(dh.watereffect);
                VRShaders._Overlay_portalAmplitutde.set(dh.portaleffect);
                VRShaders._Overlay_pumpkinAmplitutde.set(dh.pumpkineffect);

                VRShaders._Overlay_eye.set(dh.currentPass == RenderPass.LEFT ? 1 : -1);
                ((RenderTargetExtension) rendertarget).blitFovReduction(
                    VRShaders.fovReductionShader,
                    dh.vrRenderer.framebufferEye0.viewWidth,
                    dh.vrRenderer.framebufferEye0.viewHeight
                );
                GlStateManager._glUseProgram(0);
                this.checkGLError("post overlay" + dh.currentPass);
                this.profiler.pop();
            }
            case CAMERA ->
            {
                this.profiler.push("cameracopy");
                dh.vrRenderer.cameraFramebuffer.bindWrite(true);
                RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
                ((RenderTargetExtension) dh.vrRenderer.cameraRenderFramebuffer).blitToScreen(
                    0,
                    dh.vrRenderer.cameraFramebuffer.viewWidth,
                    dh.vrRenderer.cameraFramebuffer.viewHeight,
                    0,
                    true,
                    0.0F,
                    0.0F,
                    false
                );
                this.profiler.pop();
            }
        }

    }

    private void copyToMirror() {
        // TODO: fix mixed reality... again
        int left = 0;
        int width = this.window.getScreenWidth();
        int height = this.window.getScreenHeight();
        int top = 0;
        boolean disableBlend = true;
        float xCropFactor = 0.0F;
        float yCropFactor = 0.0F;
        boolean keepAspect = false;
        RenderTarget source = switch (dh.vrSettings.displayMirrorMode)
        {
            case MIXED_REALITY ->
            {
                if (VRShaders.depthMaskShader != null)
                {
                    this.doMixedRealityMirror();
                }
                else
                {
                    this.notifyMirror("Shader compile failed, see log", true, 10000);
                }
                yield null;
            }
            case DUAL ->
            {
                // run eye0
                width /= 2;
                if (dh.vrRenderer.framebufferEye0 != null)
                {
                    ((RenderTargetExtension)dh.vrRenderer.framebufferEye0).blitToScreen(
                        left,
                        width,
                        height,
                        top,
                        disableBlend,
                        xCropFactor,
                        yCropFactor,
                        keepAspect
                    );
                }
                left = width; // setup for eye1
                yield dh.vrRenderer.framebufferEye1;
            }
            case FIRST_PERSON -> dh.vrRenderer.framebufferUndistorted;
            case THIRD_PERSON -> dh.vrRenderer.framebufferMR;
            case GUI -> GuiHandler.guiFramebuffer;
            case OFF -> {
                if (dh.vr.isHMDTracking())
                {
                    this.notifyMirror("Mirror is OFF", true, 1000);
                }
                yield (!dh.vrSettings.displayMirrorLeftEye ?
                    dh.vrRenderer.framebufferEye1 :
                    dh.vrRenderer.framebufferEye0
                );
            }
            case SINGLE -> (!dh.vrSettings.displayMirrorLeftEye ?
                dh.vrRenderer.framebufferEye1 :
                dh.vrRenderer.framebufferEye0
            );
            case CROPPED ->
            {
                xCropFactor = 0.15F;
                yCropFactor = 0.15F;
                keepAspect = true;
                yield (!dh.vrSettings.displayMirrorLeftEye ?
                    dh.vrRenderer.framebufferEye1 :
                    dh.vrRenderer.framebufferEye0
                );
            }
        };
        // Debug
        // source = GuiHandler.guiFramebuffer;
        // source = DataHolder.getInstance().vrRenderer.telescopeFramebufferR;
        //
        if (source != null)
        {
            ((RenderTargetExtension)source).blitToScreen(
                left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect
            );
        }
    }

    private void doMixedRealityMirror() {
//		boolean flag = Config.isShaders();
//      boolean flag = false;
        boolean alphaMask = dh.vrSettings.mixedRealityUnityLike && dh.vrSettings.mixedRealityAlphaMask;

        if (!alphaMask) {
            RenderSystem.clearColor(
                dh.vrSettings.mixedRealityKeyColor.getR(),
                dh.vrSettings.mixedRealityKeyColor.getG(),
                dh.vrSettings.mixedRealityKeyColor.getB(),
                1.0F
            );
        } else {
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        }

        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
        Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getHeadPivot()
                .subtract(dh.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getPosition());
        Vector3f vector3 = new Vector3f().set(forward).mulProject(dh.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix());
        VRShaders._DepthMask_projectionMatrix.set(((GameRendererExtension) this.gameRenderer).getThirdPassProjectionMatrix());
        VRShaders._DepthMask_viewMatrix.set(dh.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix());
        VRShaders._DepthMask_hmdViewPosition.set((float) vec3.x, (float) vec3.y, (float) vec3.z);
        VRShaders._DepthMask_hmdPlaneNormal.set(-vector3.x, 0.0F, -vector3.z);
        VRShaders._DepthMask_keyColorUniform.set(
            dh.vrSettings.mixedRealityKeyColor.getR(),
            dh.vrSettings.mixedRealityKeyColor.getG(),
            dh.vrSettings.mixedRealityKeyColor.getB()
        );
        VRShaders._DepthMask_alphaModeUniform.set(alphaMask ? 1 : 0);
        RenderSystem.activeTexture(GL13C.GL_TEXTURE1);
        RenderSystem.setShaderTexture(0, dh.vrRenderer.framebufferMR.getColorTextureId());
        RenderSystem.activeTexture(GL13C.GL_TEXTURE2);

//		if (flag && Shaders.dfb != null) { TODO
//			GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(0));
//		} else {
        RenderSystem.setShaderTexture(1, dh.vrRenderer.framebufferMR.getDepthTextureId());

//		}

        RenderSystem.activeTexture(GL13C.GL_TEXTURE0);

        for (int i = 0; i < (alphaMask ? 3 : 2); ++i) {
            int j = this.window.getScreenWidth() / 2;
            int k = this.window.getScreenHeight();
            int l = this.window.getScreenWidth() / 2 * i;
            int i1 = 0;

            if (dh.vrSettings.mixedRealityUnityLike) {
                j = this.window.getScreenWidth() / 2;
                k = this.window.getScreenHeight() / 2;

                if (dh.vrSettings.mixedRealityAlphaMask && i == 2) {
                    l = this.window.getScreenWidth() / 2;
                    i1 = this.window.getScreenHeight() / 2;
                } else {
                    l = 0;
                    i1 = this.window.getScreenHeight() / 2 * (1 - i);
                }
            }

            VRShaders._DepthMask_resolutionUniform.set((float) j, k);
            VRShaders._DepthMask_positionUniform.set((float) l, i1);
            VRShaders._DepthMask_passUniform.set(i);
            ((RenderTargetExtension) dh.vrRenderer.framebufferMR).blitToScreen(
                VRShaders.depthMaskShader,
                l,
                j,
                k,
                i1,
                true,
                0.0F,
                0.0F,
                false
            );
        }

        GlStateManager._glUseProgram(0);

        if (dh.vrSettings.mixedRealityUnityLike) {
            ((RenderTargetExtension)(dh.vrSettings.mixedRealityUndistorted ?
                dh.vrRenderer.framebufferUndistorted :
                dh.vrRenderer.framebufferEye0
            )).blitToScreen(
                this.window.getScreenWidth() / 2,
                this.window.getScreenWidth() / 2,
                this.window.getScreenHeight() / 2,
                0,
                true,
                0.0F,
                0.0F,
                false
            );
        }
    }

}
