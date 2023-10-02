package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.VivecraftClickEvent.VivecraftAction;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.VRState;
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
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRPassHelper;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.client.Minecraft.ON_OSX;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.vivecraft.client.utils.Utils.*;
import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.forward;
import static org.vivecraft.common.utils.Utils.logger;

@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin implements MinecraftExtension {

    @Unique
    private boolean vivecraft$lastClick;

    @Unique
    private ItemStack vivecraft$itemInHand; //Captured item

    @Unique
    private long vivecraft$mirroNotifyStart;

    @Unique
    private long vivecraft$mirroNotifyLen;

    @Unique
    private boolean vivecraft$mirrorNotifyClear;

    @Unique
    private String vivecraft$mirrorNotifyText;

    @Unique
    private List<String> vivecraft$resourcepacks;

    @Shadow
    private ProfilerFiller profiler;

    @Shadow
    @Final
    private Window window;

    @Shadow
    private boolean pause;

    @Shadow
    private float pausePartialTick;

    @Final
    @Shadow
    private Timer timer;

    @Shadow
    private ProfileResults fpsPieResults;

    @Shadow
    private int rightClickDelay;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Mutable
    @Final
    @Shadow
    private RenderTarget mainRenderTarget;

    @Shadow
    protected abstract void renderFpsMeter(GuiGraphics guiGraphics, ProfileResults profileResults);

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            opcode = PUTSTATIC,
            target = "Lnet/minecraft/client/Minecraft;instance:Lnet/minecraft/client/Minecraft;"
        )
    )
    private void vivecraft$captureMinecraftInstance(Minecraft value, Operation<Void> original) {
        original.call(VRState.mc = value); // Assign early to ensure subsequent accesses are safe.
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            opcode = PUTFIELD,
            target = "Lnet/minecraft/client/Minecraft;mainRenderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"
        )
    )
    private void vivecraft$captureMainRenderTarget(Minecraft instance, RenderTarget value, Operation<RenderTarget> original) {
        RenderPassManager.INSTANCE = new RenderPassManager((MainTarget) value);
        original.call(instance, value);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "<init>", index = 0)
    public Overlay vivecraft$initVivecraft(Overlay overlay) {
        VRSettings.initSettings();

        // register a resource reload listener, to reload the menu world
        this.resourceManager.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            List<String> newPacks = resourceManager.listPacks().map(PackResources::packId).toList();
            if (!newPacks.equals(this.vivecraft$resourcepacks) && dh.menuWorldRenderer != null &&
                dh.menuWorldRenderer.isReady()
            ) {
                this.vivecraft$resourcepacks = newPacks;
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
        "method_53522", // fabric
        "lambda$new$6"} // forge
        , remap = false)
    public void vivecraft$initVROnLaunch(CallbackInfo ci) {
        // init vr after resource loading
        try {
            if (dh.vrSettings.vrEnabled) {
                vrEnabled = true;
                initializeVR();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // set initial resourcepacks
        this.vivecraft$resourcepacks = this.resourceManager.listPacks().map(PackResources::packId).toList();

        if (OptifineHelper.isOptifineLoaded() && dh.menuWorldRenderer != null && dh.menuWorldRenderer.isReady()) {
            // with optifine this texture somehow fails to load, so manually reload it
            try {
                this.textureManager.getTexture(Gui.CROSSHAIR_SPRITE).load(this.resourceManager);
            } catch (IOException e) {
                // if there was an error, just reload everything
                mc.reloadResourcePacks();
            }
        }
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;delayedCrash:Ljava/util/function/Supplier;", shift = Shift.BEFORE), method = "destroy()V")
    public void vivecraft$destroy(CallbackInfo info) {
        try {
            // the game crashed probably not because of us, so keep the vr choice
            destroyVR(false);
        } catch (Exception ignored) {
        }
    }

    @Inject(at = @At("HEAD"), method = "runTick(Z)V")
    public void vivecraft$toggleVRState(boolean tick, CallbackInfo callback) {
        if (vrEnabled) {
            initializeVR();
        } else if (vrInitialized) {
            this.vivecraft$switchVRState(false);
            destroyVR(true);
        }
        if (!vrInitialized) {
            return;
        }
        boolean vrActive = !dh.vrSettings.vrHotswitchingEnabled || dh.vr.isActive();
        if (vrRunning != vrActive && (ClientNetworking.serverAllowsVrSwitching || mc.player == null)) {
            this.vivecraft$switchVRState(vrActive);
        }
        if (vrRunning) {
            ++dh.frameIndex;

            // reset camera position, if there is one, since it only gets set at the start of rendering, and the last renderpass can be anywhere
            if (mc.gameRenderer != null && mc.gameRenderer.getMainCamera() != null && mc.level != null && mc.getCameraEntity() != null) {
                mc.gameRenderer.getMainCamera().setup(mc.level, mc.getCameraEntity(), false, false, this.pause ? this.pausePartialTick : this.timer.partialTick);
            }

            this.profiler.push("VR Poll/VSync");
            dh.vr.poll(dh.frameIndex);
            this.profiler.pop();
            dh.vrPlayer.postPoll();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.BEFORE), method = "runTick")
    public void vivecraft$preTickTasks(CallbackInfo ci) {
        if (vrRunning) {
            dh.vrPlayer.preTick();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER), method = "runTick")
    public void vivecraft$postTickTasks(CallbackInfo ci) {
        if (vrRunning) {
            dh.vrPlayer.postTick();
        }
    }

    @Inject(at = @At(value = "CONSTANT", args = "stringValue=render"), method = "runTick")
    public void vivecraft$preRender(CallbackInfo ci) {
        if (vrRunning) {
            this.profiler.push("preRender");
            dh.vrPlayer.preRender(this.pause ? this.pausePartialTick : this.timer.partialTick);
            VRHotkeys.updateMovingThirdPersonCam();
            this.profiler.pop();
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), method = "runTick")
    public boolean vivecraft$setupRenderGUI(boolean renderLevel) {
        if (vrRunning) {

            this.profiler.push("setupRenderConfiguration");
            try {
                VRPassHelper.checkGLError("pre render setup ");
                dh.vrRenderer.setupRenderConfiguration();
                VRPassHelper.checkGLError("post render setup ");
            } catch (RenderConfigException renderConfigException) {
                this.vivecraft$switchVRState(false);
                destroyVR(true);
                mc.setScreen(new ErrorScreen("VR Render Error", renderConfigException.error));
                this.profiler.pop();
                return renderLevel;
            } catch (Exception exception2) {
                exception2.printStackTrace();
            }
            this.profiler.pop();

            RenderPassManager.setGUIRenderPass();
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
            this.mainRenderTarget.clear(ON_OSX);
            this.mainRenderTarget.bindWrite(true);

            // draw screen/gui to buffer
            RenderSystem.getModelViewStack().pushPose();
            ((GameRendererExtension) mc.gameRenderer).vivecraft$setShouldDrawScreen(true);
            // only draw the gui when the level was rendered once, since some mods expect that
            ((GameRendererExtension) mc.gameRenderer).vivecraft$setShouldDrawGui(renderLevel && this.entityRenderDispatcher.camera != null);
            return false;
        } else {
            return renderLevel;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 4, shift = Shift.AFTER), method = "runTick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$renderVRPasses(boolean renderLevel, CallbackInfo ci, long nanoTime) {
        if (vrRunning) {

            // draw cursor on Gui Layer
            if (mc.screen != null) {
                PoseStack poseStack = RenderSystem.getModelViewStack();
                poseStack.pushPose();
                poseStack.setIdentity();
                poseStack.last().pose().translate(0.0F, 0.0F, -2000.0F);
                RenderSystem.applyModelViewMatrix();

                int x = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
                int y = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());
                ((GuiExtension) mc.gui).vivecraft$drawMouseMenuQuad(x, y);

                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }

            // draw debug pie
            this.vivecraft$drawProfiler();
            // reset that, do not draw it again on something else
            this.fpsPieResults = null;

            // generate mipmaps
            // TODO: does this do anything?
            this.mainRenderTarget.bindRead();
            ((RenderTargetExtension) this.mainRenderTarget).vivecraft$genMipMaps();
            this.mainRenderTarget.unbindRead();

            this.profiler.popPush("2D Keyboard");
            float actualPartialTicks = this.pause ? this.pausePartialTick : this.timer.partialTick;
            GuiGraphics guiGraphics = new GuiGraphics(mc, this.renderBuffers.bufferSource());
            if (KeyboardHandler.isShowing() && !dh.vrSettings.physicalKeyboard) {
                this.mainRenderTarget = KeyboardHandler.Framebuffer;
                this.mainRenderTarget.clear(ON_OSX);
                this.mainRenderTarget.bindWrite(true);
                RenderHelper.drawScreen(actualPartialTicks, KeyboardHandler.UI, guiGraphics);
                guiGraphics.flush();
            }

            this.profiler.popPush("Radial Menu");
            if (RadialHandler.isShowing()) {
                this.mainRenderTarget = RadialHandler.Framebuffer;
                this.mainRenderTarget.clear(ON_OSX);
                this.mainRenderTarget.bindWrite(true);
                RenderHelper.drawScreen(actualPartialTicks, RadialHandler.UI, guiGraphics);
                guiGraphics.flush();
            }
            this.profiler.pop();
            VRPassHelper.checkGLError("post 2d ");

            // render the different vr passes
            List<RenderPass> list = dh.vrRenderer.getRenderPasses();
            dh.isFirstPass = true;
            for (RenderPass renderpass : list) {
                dh.currentPass = renderpass;

                switch (renderpass) {
                    case LEFT, RIGHT -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.stereoXR);
                    }
                    case CENTER -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.center);
                    }
                    case THIRD -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.mixedReality);
                    }
                    case SCOPEL -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.leftTelescope);
                    }
                    case SCOPER -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.rightTelescope);
                    }
                    case CAMERA -> {
                        RenderPassManager.setWorldRenderPass(WorldRenderPass.camera);
                    }
                }

                this.profiler.push("Eye:" + renderpass);
                this.profiler.push("setup");
                this.mainRenderTarget.bindWrite(true);
                this.profiler.pop();
                VRPassHelper.renderSingleView(renderpass, actualPartialTicks, nanoTime, renderLevel);
                this.profiler.pop();

                if (dh.grabScreenShot &&
                    switch (renderpass) {
                        case CAMERA, CENTER -> {
                            yield list.contains(renderpass);
                        }
                        case LEFT -> {
                            yield dh.vrSettings.displayMirrorLeftEye;
                        }
                        case RIGHT -> {
                            yield !dh.vrSettings.displayMirrorLeftEye;
                        }
                        default -> {
                            yield false;
                        }
                    }
                ) {
                    this.mainRenderTarget.unbindWrite();
                    takeScreenshot(
                        renderpass == RenderPass.CAMERA ?
                        dh.vrRenderer.cameraFramebuffer :
                        this.mainRenderTarget
                    );
                    this.window.updateDisplay();
                    dh.grabScreenShot = false;
                }

                dh.isFirstPass = false;
            }

            dh.vrPlayer.postRender(actualPartialTicks);
            this.profiler.push("Display/Reproject");

            try {
                dh.vrRenderer.endFrame();
            } catch (RenderConfigException exception) {
                logger.error(exception.toString());
            }
            this.profiler.pop();
            VRPassHelper.checkGLError("post submit ");
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V"), method = "runTick")
    public void vivecraft$blitMirror(RenderTarget instance, int width, int height) {
        if (!vrRunning) {
            instance.blitToScreen(width, height);
        } else {
            this.profiler.push("mirror");
            this.vivecraft$copyToMirror();
            this.vivecraft$drawNotifyMirror();
            VRPassHelper.checkGLError("post-mirror ");
        }
    }

    // the VR runtime handles the frame limit, no need to manually limit it 60fps
    @ModifyConstant(constant = @Constant(longValue = 16), method = "doWorldLoad", expect = 0)
    private long vivecraft$noWaitOnLevelLoadFabric(long constant) {
        if (vrRunning) {
            return 0L;
        }
        return constant;
    }

    @Inject(at = @At("HEAD"), method = "resizeDisplay")
    void vivecraft$restoreVanillaState(CallbackInfo ci) {
        if (vrInitialized) {
            // restore vanilla post chains before the resize, or it will resize the wrong ones
            if (mc.levelRenderer != null) {
                ((LevelRendererExtension) mc.levelRenderer).vivecraft$restoreVanillaPostChains();
            }
            RenderPassManager.setVanillaRenderPass();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "continueAttack(Z)V")
    public void vivecraft$swingArmContinueAttack(LocalPlayer player, InteractionHand hand) {
        if (vrRunning) {
            ((PlayerExtension) player).vivecraft$swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            player.swing(hand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"), method = "continueAttack(Z)V")
    public void vivecraft$destroyseated(MultiPlayerGameMode gm) {
        if (!vrRunning || dh.vrSettings.seated || this.vivecraft$lastClick) {
            mc.gameMode.stopDestroyBlock();
            this.vivecraft$lastClick = false;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), method = "startUseItem()V")
    public boolean vivecraft$seatedCheck(MultiPlayerGameMode gameMode) {
        return gameMode.isDestroying() && (!vrRunning || dh.vrSettings.seated);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER, opcode = PUTFIELD), method = "startUseItem()V")
    public void vivecraft$breakDelay(CallbackInfo info) {
        if (vrRunning) {
            this.rightClickDelay = switch (dh.vrSettings.rightclickDelay) {
                case SLOW -> {
                    yield 6;
                }
                case SLOWER -> {
                    yield 8;
                }
                case SLOWEST -> {
                    yield 10;
                }
                default -> {
                    yield 4;
                }
            };
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "startUseItem")
    public ItemStack vivecraft$handItemStore(ItemStack itemInHand) {
        this.vivecraft$itemInHand = itemInHand;
        return itemInHand;
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$activeHandSend(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand interactionHand) {
        if (vrRunning && (dh.vrSettings.seated || !TelescopeTracker.isTelescope(this.vivecraft$itemInHand))) {
            ClientNetworking.sendActiveHand((byte) interactionHand.ordinal());
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem")
    public HitResult vivecraft$activeHand2(Minecraft instance) {
        if (!vrRunning || dh.vrSettings.seated || !TelescopeTracker.isTelescope(this.vivecraft$itemInHand)) {
            return instance.hitResult;
        }
        return null;
    }


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
    public void vivecraft$swingUse(LocalPlayer instance, InteractionHand interactionHand) {
        if (vrRunning) {
            ((PlayerExtension) instance).vivecraft$swingArm(interactionHand, VRFirstPersonArmSwing.Use);
        } else {
            instance.swing(interactionHand);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    public void vivecraft$vrTick(CallbackInfo info) {
        ++dh.tickCounter;

        // general chat notifications
        if (mc.level != null) {
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
        if (vrInitialized && mc.level != null && dh.vrPlayer != null) {
            if (dh.vrPlayer.chatWarningTimer >= 0 && --dh.vrPlayer.chatWarningTimer == 0) {
                boolean showMessage = !ClientNetworking.displayedChatWarning || dh.vrSettings.showServerPluginMissingMessageAlways;

                if (dh.vrPlayer.teleportWarning) {
                    if (showMessage) {
                        message(Component.translatable("vivecraft.messages.noserverplugin"));
                    }
                    dh.vrPlayer.teleportWarning = false;

                    // allow vr switching on vanilla server
                    ClientNetworking.serverAllowsVrSwitching = true;
                }
                if (dh.vrPlayer.vrSwitchWarning) {
                    if (showMessage) {
                        message(Component.translatable("vivecraft.messages.novrhotswitchinglegacy"));
                    }
                    dh.vrPlayer.vrSwitchWarning = false;
                }
                ClientNetworking.displayedChatWarning = true;
            }
        }

        if (vrRunning) {

            if (dh.menuWorldRenderer.isReady()) {
                // update textures in the menu
                if (mc.level == null) {
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

            if (mc.level != null && dh.vrPlayer != null) {
                dh.vrPlayer.updateFreeMove();
            }
            this.profiler.pop();
        }

        this.profiler.push("vrPlayers");

        VRPlayersClient.getInstance().tick();

        if (VivecraftVRMod.keyExportWorld.consumeClick() && mc.level != null && mc.player != null) {
            Throwable error = null;
            try {
                final BlockPos blockpos = mc.player.blockPosition();
                int size = 320;
                int offset = size / 2;
                File file1 = new File(MenuWorldDownloader.customWorldFolder);
                file1.mkdirs();
                int i = 0;
                for (File fileChecked = new File(file1, "world" + i + ".mmw");
                     !fileChecked.exists();
                     fileChecked = new File(file1, "world" + (++i) + ".mmw")
                ) {
                    final File file2 = fileChecked;
                    logger.info("Exporting world... area size: " + size);
                    logger.info("Saving to " + file2.getAbsolutePath());

                    if (mc.isLocalServer()) {
                        final Level level = mc.getSingleplayerServer().getLevel(mc.player.level().dimension());
                        CompletableFuture<Throwable> completablefuture = mc.getSingleplayerServer().submit(() -> {
                            try {
                                MenuWorldExporter.saveAreaToFile(level, blockpos.getX() - offset, blockpos.getZ() - offset, size, size, blockpos.getY(), file2);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                return throwable;
                            }
                            return null;
                        });

                        error = completablefuture.get();
                    } else {
                        MenuWorldExporter.saveAreaToFile(mc.level, blockpos.getX() - offset, blockpos.getZ() - offset, size, size, blockpos.getY(), file2);
                        message(Component.translatable("vivecraft.messages.menuworldexportclientwarning"));
                    }

                    if (error == null) {
                        message(Component.translatable("vivecraft.messages.menuworldexportcomplete.1", size));
                        message(Component.translatable("vivecraft.messages.menuworldexportcomplete.2", file2.getAbsolutePath()));
                    }
                }
            } catch (Throwable throwable) {
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
    public void vivecraft$removePick(GameRenderer instance, float f) {
        if (!vrRunning) {
            instance.pick(f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
    public void vivecraft$vrMirrorOption(Options instance, CameraType cameraType) {
        if (vrRunning) {
            dh.vrSettings.setOptionValue(VrOptions.MIRROR_DISPLAY);
            this.vivecraft$notifyMirror(dh.vrSettings.getButtonDisplayString(VrOptions.MIRROR_DISPLAY), false, 3000);
            // this.levelRenderer.needsUpdate();
        } else {
            instance.setCameraType(cameraType);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
    public void vivecraft$noPosEffect(GameRenderer instance, Entity entity) {
        if (!vrRunning) {
            instance.checkEntityPostEffect(entity);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
    public void vivecraft$swingArmhandleKeybinds(LocalPlayer player, InteractionHand interactionHand) {
        if (vrRunning) {
            ((PlayerExtension) mc.player).vivecraft$swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
        } else {
            player.swing(interactionHand);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2), method = "handleKeybinds")
    public boolean vivecraft$vrKeyuse(KeyMapping instance) {
        return !(!instance.isDown() && (!vrRunning || ((!dh.bowTracker.isActive() || dh.vrSettings.seated) && !dh.eatingTracker.isEating())));
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleKeybinds")
    public void vivecraft$activeHand(CallbackInfo ci) {
        if (vrRunning) {
            ClientNetworking.sendActiveHand((byte) mc.player.getUsedItemHand().ordinal());
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"), method = "handleKeybinds")
    public void vivecraft$attackDown(CallbackInfo ci) {
        // detect, if the attack buttun was used to testroy blocks
        this.vivecraft$lastClick = true;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), method = "handleKeybinds")
    public boolean vivecraft$vrAlwaysGrapped(MouseHandler instance) {
        return vrRunning || instance.isMouseGrabbed();
    }

    @Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
    public void vivecraft$roomScale(ClientLevel pLevelClient, CallbackInfo info) {
        if (vrRunning) {
            dh.vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
        }
    }

    @Inject(at = @At(value = "FIELD", opcode = PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 0), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
    public void vivecraft$onOpenScreen(Screen pGuiScreen, CallbackInfo info) {
        GuiHandler.onScreenChanged(mc.screen, pGuiScreen, true);
    }

    @Inject(at = @At("TAIL"), method = "setOverlay")
    public void vivecraft$onOverlaySet(Overlay overlay, CallbackInfo ci) {
        GuiHandler.onScreenChanged(mc.screen, mc.screen, true);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    public void vivecraft$onCloseScreen(Screen screen, CallbackInfo info) {
        if (screen == null) {
            GuiHandler.guiAppearOverBlockActive = false;
        }
    }

    @Override
    @Unique
    public void vivecraft$notifyMirror(String text, boolean clear, int lengthMs) {
        this.vivecraft$mirroNotifyStart = System.currentTimeMillis();
        this.vivecraft$mirroNotifyLen = lengthMs;
        this.vivecraft$mirrorNotifyText = text;
        this.vivecraft$mirrorNotifyClear = clear;
    }

    @Unique
    private void vivecraft$drawNotifyMirror() {
        if (System.currentTimeMillis() < this.vivecraft$mirroNotifyStart + this.vivecraft$mirroNotifyLen) {
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

            if (this.vivecraft$mirrorNotifyClear) {
                RenderSystem.clearColor(0, 0, 0, 0);
                RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, ON_OSX);
            }

            int i = this.window.getScreenWidth() / 22;
            ArrayList<String> arraylist = new ArrayList<>();

            if (this.vivecraft$mirrorNotifyText != null) {
                wordWrap(this.vivecraft$mirrorNotifyText, i, arraylist);
            }

            int j = 1;
            int k = 12;

            GuiGraphics guiGraphics = new GuiGraphics(mc, this.renderBuffers.bufferSource());
            for (String s : arraylist) {
                guiGraphics.drawString(mc.font, s, 1, j, 16777215);
                j += 12;
            }
            guiGraphics.flush();
            RenderSystem.getModelViewStack().popPose();
        }
    }

    @Unique
    private void vivecraft$switchVRState(boolean vrActive) {
        vrRunning = vrActive;
        if (vrActive) {
            if (mc.player != null) {
                dh.vrPlayer.snapRoomOriginToPlayerEntity(false, false);
            }
            // release mouse when switching to standing
            if (!dh.vrSettings.seated) {
                mc.mouseHandler.releaseMouse();
                InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_NORMAL, mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
            }
        } else {
            GuiHandler.guiPos_room = null;
            GuiHandler.guiRotation_room = null;
            GuiHandler.guiScale = 1.0F;

            if (mc.player != null) {
                VRPlayersClient.getInstance().disableVR(mc.player.getUUID());
            }
            if (mc.gameRenderer != null) {
                mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
            }
            // grab/release mouse
            if (mc.screen != null || mc.level == null) {
                mc.mouseHandler.releaseMouse();
                InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_NORMAL, mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
            } else {
                mc.mouseHandler.grabMouse();
                InputConstants.grabOrReleaseMouse(this.window.getWindow(), GLFW_CURSOR_DISABLED, mc.mouseHandler.xpos(), mc.mouseHandler.ypos());
            }
        }
        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            connection.send(ClientNetworking.createVRActivePacket(vrActive));
        }
        // reload sound manager, to toggle HRTF between VR and NONVR one
        if (!mc.getSoundManager().getAvailableSounds().isEmpty()) {
            mc.getSoundManager().reload();
        }
        mc.resizeDisplay();
    }

    @Unique
    private void vivecraft$drawProfiler() {
        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            GuiGraphics guiGraphics = new GuiGraphics(mc, this.renderBuffers.bufferSource());
            this.renderFpsMeter(guiGraphics, this.fpsPieResults);
            guiGraphics.flush();
            this.profiler.pop();
        }
    }

    @Unique
    private void vivecraft$copyToMirror() {
        // TODO: fix mixed reality... again
        int left = 0;
        int width = this.window.getScreenWidth();
        int height = this.window.getScreenHeight();
        int top = 0;
        boolean disableBlend = true;
        float xCropFactor = 0.0F;
        float yCropFactor = 0.0F;
        boolean keepAspect = false;
        RenderTarget source = switch (dh.vrSettings.displayMirrorMode) {
            case MIXED_REALITY -> {
                if (VRShaders.depthMaskShader != null) {
                    this.vivecraft$doMixedRealityMirror();
                } else {
                    this.vivecraft$notifyMirror("Shader compile failed, see log", true, 10000);
                }
                yield null;
            }
            case DUAL -> {
                // run eye0
                width /= 2;
                if (dh.vrRenderer.framebufferEye0 != null) {
                    ((RenderTargetExtension) dh.vrRenderer.framebufferEye0).vivecraft$blitToScreen(
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
            case FIRST_PERSON -> {
                yield dh.vrRenderer.framebufferUndistorted;
            }
            case THIRD_PERSON -> {
                yield dh.vrRenderer.framebufferMR;
            }
            case GUI -> {
                yield GuiHandler.guiFramebuffer;
            }
            case OFF -> {
                if (dh.vr.isHMDTracking()) {
                    this.vivecraft$notifyMirror("Mirror is OFF", true, 1000);
                }
                yield (!dh.vrSettings.displayMirrorLeftEye ?
                       dh.vrRenderer.framebufferEye1 :
                       dh.vrRenderer.framebufferEye0
                );
            }
            case SINGLE -> {
                yield !dh.vrSettings.displayMirrorLeftEye ?
                      dh.vrRenderer.framebufferEye1 :
                      dh.vrRenderer.framebufferEye0;
            }
            case CROPPED -> {
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
        // source = dh.vrRenderer.telescopeFramebufferR;
        //
        if (source != null) {
            ((RenderTargetExtension) source).vivecraft$blitToScreen(
                left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect
            );
        }
    }

    @Unique
    private void vivecraft$doMixedRealityMirror() {
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
        Vector3f vector3 = dh.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix().transformProject(forward, new Vector3f());
        VRShaders._DepthMask_projectionMatrix.set(((GameRendererExtension) mc.gameRenderer).vivecraft$getThirdPassProjectionMatrix());
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
            ((RenderTargetExtension) dh.vrRenderer.framebufferMR).vivecraft$blitToScreen(
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
            ((RenderTargetExtension) (
                dh.vrSettings.mixedRealityUndistorted ?
                dh.vrRenderer.framebufferUndistorted :
                dh.vrRenderer.framebufferEye0
            )).vivecraft$blitToScreen(
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
