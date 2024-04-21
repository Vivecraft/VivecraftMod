package org.vivecraft.mixin.client_vr.renderer;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.Xevents;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.XRCamera;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(GameRenderer.class)
public abstract class GameRendererVRMixin
    implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {

    @Unique
    private static final ClientDataHolderVR vivecraft$DATA_HOLDER = ClientDataHolderVR.getInstance();
    @Unique
    public float vivecraft$minClipDistance = 0.02F;
    @Unique
    public Vec3 vivecraft$crossVec;
    @Unique
    public Matrix4f vivecraft$thirdPassProjectionMatrix = new Matrix4f();
    @Unique
    public boolean vivecraft$inwater;
    @Unique
    public boolean vivecraft$wasinwater;
    @Unique
    public boolean vivecraft$inportal;
    @Unique
    public boolean vivecraft$onfire;
    @Unique
    public float vivecraft$inBlock = 0.0F;
    @Unique
    public double vivecraft$rveX;
    @Unique
    public double vivecraft$rveY;
    @Unique
    public double vivecraft$rveZ;
    @Unique
    public double vivecraft$rvelastX;
    @Unique
    public double vivecraft$rvelastY;
    @Unique
    public double vivecraft$rvelastZ;
    @Unique
    public double vivecraft$rveprevX;
    @Unique
    public double vivecraft$rveprevY;
    @Unique
    public double vivecraft$rveprevZ;
    @Unique
    public float vivecraft$rveyaw;
    @Unique
    public float vivecraft$rvepitch;
    @Unique
    private float vivecraft$rvelastyaw;
    @Unique
    private float vivecraft$rvelastpitch;
    @Unique
    private float vivecraft$rveHeight;
    @Unique
    private boolean vivecraft$cached;

    @Unique // TODO added by optifine...
    private float vivecraft$clipDistance = 128.0F;

    @Unique
    private PoseStack vivecraft$stack;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float renderDistance;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float fov;

    @Shadow
    private float oldFov;
    @Shadow
    private boolean renderHand;

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double fov);

    @Shadow
    protected abstract double getFov(Camera mainCamera2, float partialTicks, boolean b);

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f projectionMatrix);

    @Shadow
    protected abstract void renderItemActivationAnimation(int i, int j, float par1);

    @Shadow
    public abstract void pick(float f);

    @Shadow
    private boolean effectActive;

    @Shadow
    private long lastActiveTime;

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    private int itemActivationTicks;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"))
    public Camera vivecraft$replaceCamera() {
        return new XRCamera();
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"), method = "pick")
    public ClientLevel vivecraft$appendCheck(Minecraft instance) {
        if (!VRState.vrRunning) {
            return instance.level;
        }
        return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render == null ? null : instance.level;
    }

    @Inject(at = @At("HEAD"), method = {"shutdownEffect", "checkEntityPostEffect", "cycleEffect", "loadEffect"})
    public void vivecraft$shutdownEffect(CallbackInfo ci) {
        if (VRState.vrInitialized) {
            RenderPassManager.setVanillaRenderPass();
            if (WorldRenderPass.stereoXR != null && WorldRenderPass.stereoXR.postEffect != null) {
                WorldRenderPass.stereoXR.postEffect.close();
                WorldRenderPass.stereoXR.postEffect = null;
            }
            if (WorldRenderPass.center != null && WorldRenderPass.center.postEffect != null) {
                WorldRenderPass.center.postEffect.close();
                WorldRenderPass.center.postEffect = null;
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;resize(II)V", shift = Shift.AFTER), method = "loadEffect")
    public void vivecraft$loadEffect(ResourceLocation resourceLocation, CallbackInfo ci) throws IOException {
        if (VRState.vrInitialized) {
            if (WorldRenderPass.stereoXR != null) {
                WorldRenderPass.stereoXR.postEffect = WorldRenderPass.createPostChain(resourceLocation, WorldRenderPass.stereoXR.target);
            }
            if (WorldRenderPass.center != null) {
                WorldRenderPass.center.postEffect = WorldRenderPass.createPostChain(resourceLocation, WorldRenderPass.center.target);
            }
        }
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 0)
    public Vec3 vivecraft$rayTrace(Vec3 original) {
        if (!VRState.vrRunning) {
            return original;
        }
        this.minecraft.hitResult = vivecraft$DATA_HOLDER.vrPlayer.rayTraceBlocksVR(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render, 0, this.minecraft.gameMode.getPickRange(), false);
        this.vivecraft$crossVec = vivecraft$DATA_HOLDER.vrPlayer.AimedPointAtDistance(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render, 0, this.minecraft.gameMode.getPickRange());
        return vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPosition();
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 vivecraft$vrVec31(Vec3 original) {
        if (!VRState.vrRunning) {
            return original;
        }
        return vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getDirection();
    }

    //TODO Vivecraft add riding check in case your hand is somewhere inappropriate

    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void vivecraft$noFOVchangeInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.oldFov = this.fov = 1.0f;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void vivecraft$fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
        if (this.minecraft.level == null || vivecraft$isInMenuRoom()) { // Vivecraft: using this on the main menu
            info.setReturnValue(Double.valueOf(this.minecraft.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void vivecraft$projection(double d, CallbackInfoReturnable<Matrix4f> info) {
        if (!VRState.vrRunning) {
            return;
        }
        PoseStack posestack = new PoseStack();
        vivecraft$setupClipPlanes();
        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.LEFT) {
            posestack.mulPoseMatrix(vivecraft$DATA_HOLDER.vrRenderer.eyeproj[0]);
        } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.RIGHT) {
            posestack.mulPoseMatrix(vivecraft$DATA_HOLDER.vrRenderer.eyeproj[1]);
        } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.THIRD) {
            if (vivecraft$DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                posestack.mulPoseMatrix(
                    new Matrix4f().setPerspective(vivecraft$DATA_HOLDER.vrSettings.mixedRealityFov * 0.01745329238474369F,
                        vivecraft$DATA_HOLDER.vrSettings.mixedRealityAspectRatio, this.vivecraft$minClipDistance,
                        this.vivecraft$clipDistance));
            } else {
                posestack.mulPoseMatrix(
                    new Matrix4f().setPerspective(vivecraft$DATA_HOLDER.vrSettings.mixedRealityFov * 0.01745329238474369F,
                        (float) this.minecraft.getWindow().getScreenWidth()
                            / (float) this.minecraft.getWindow().getScreenHeight(),
                        this.vivecraft$minClipDistance, this.vivecraft$clipDistance));
            }
            this.vivecraft$thirdPassProjectionMatrix = new Matrix4f(posestack.last().pose());
        } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.CAMERA) {
            posestack.mulPoseMatrix(new Matrix4f().setPerspective(vivecraft$DATA_HOLDER.vrSettings.handCameraFov * 0.01745329238474369F,
                (float) vivecraft$DATA_HOLDER.vrRenderer.cameraFramebuffer.viewWidth
                    / (float) vivecraft$DATA_HOLDER.vrRenderer.cameraFramebuffer.viewHeight,
                this.vivecraft$minClipDistance, this.vivecraft$clipDistance));
        } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.SCOPEL
            || vivecraft$DATA_HOLDER.currentPass == RenderPass.SCOPER) {
            posestack.mulPoseMatrix(new Matrix4f().setPerspective(70f / 8f * 0.01745329238474369F, 1.0F, this.vivecraft$minClipDistance, this.vivecraft$clipDistance));
        } else {
            if (this.zoom != 1.0F) {
                posestack.translate(this.zoomX, -this.zoomY, 0.0D);
                posestack.scale(this.zoom, this.zoom, 1.0F);
            }
            posestack.mulPoseMatrix(new Matrix4f().setPerspective((float) d * 0.01745329238474369F, (float) this.minecraft.getWindow().getScreenWidth()
                / (float) this.minecraft.getWindow().getScreenHeight(), this.vivecraft$minClipDistance, this.vivecraft$clipDistance));
        }
        info.setReturnValue(posestack.last().pose());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean vivecraft$focus(Minecraft instance) {
        return VRState.vrRunning || instance.isWindowActive();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void vivecraft$pause(Minecraft instance, boolean bl) {
        if (!VRState.vrRunning || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            instance.pauseGame(bl);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long vivecraft$active() {
        if (!VRState.vrRunning || ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT) {
            return Util.getMillis();
        } else {
            return this.lastActiveTime;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false, shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void vivecraft$matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        this.resetProjectionMatrix(this.getProjectionMatrix(minecraft.options.fov().get()));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    @Inject(at = @At("HEAD"), method = "shouldRenderBlockOutline", cancellable = true)
    public void vivecraft$shouldDrawBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning) {
            if (vivecraft$DATA_HOLDER.teleportTracker.isAiming() || vivecraft$DATA_HOLDER.vrSettings.renderBlockOutlineMode == VRSettings.RenderPointerElement.NEVER) {
                // don't render outline when aiming with tp, or the user disabled it
                cir.setReturnValue(false);
            } else if (vivecraft$DATA_HOLDER.vrSettings.renderBlockOutlineMode == VRSettings.RenderPointerElement.ALWAYS) {
                // skip vanilla check and always render the outline
                cir.setReturnValue(true);
            }
            // VRSettings.RenderPointerElement.WITH_HUD uses the vanilla behaviour
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
    public PoseStack vivecraft$newStack(PoseStack poseStack) {
        this.vivecraft$stack = poseStack;
        return poseStack;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void vivecraft$renderoverlay(float f, long l, boolean bl, CallbackInfo ci) {
        if (VRState.vrRunning && vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
            && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
            VREffectsHelper.renderFaceOverlay(f, this.vivecraft$stack);
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean vivecraft$effect(GameRenderer instance) {
        return this.effectActive && ClientDataHolderVR.getInstance().currentPass != RenderPass.THIRD;
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void vivecraft$noScreenshotInMenu(Path path, CallbackInfo ci) {
        if (VRState.vrRunning && vivecraft$isInMenuRoom()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean vivecraft$shouldDrawScreen = false;
    @Unique
    private boolean vivecraft$shouldDrawGui = false;

    @Override
    @Unique
    public void vivecraft$setShouldDrawScreen(boolean shouldDrawScreen) {
        this.vivecraft$shouldDrawScreen = shouldDrawScreen;
    }

    @Override
    @Unique
    public void vivecraft$setShouldDrawGui(boolean shouldDrawGui) {
        this.vivecraft$shouldDrawGui = shouldDrawGui;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.BEFORE, ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
    public void vivecraft$mainMenu(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!renderWorldIn && vivecraft$shouldDrawScreen) {
            vivecraft$shouldDrawScreen = false;
            if (vivecraft$shouldDrawGui) {
                // when the gui is rendered it is expected that something got pushed to the profiler before
                // so do that now
                this.minecraft.getProfiler().push("vanillaGuiSetup");
            }
            return;
        }
        if (!renderWorldIn || this.minecraft.level == null || vivecraft$isInMenuRoom()) {
            if (!renderWorldIn || this.minecraft.level == null) {
                // no "level" got pushed so do a manual push
                this.minecraft.getProfiler().push("MainMenu");
            } else {
                // do a popPush
                this.minecraft.getProfiler().popPush("MainMenu");
            }
            GL11.glDisable(GL11.GL_STENCIL_TEST);

            PoseStack pMatrixStack = new PoseStack();
            RenderHelper.applyVRModelView(vivecraft$DATA_HOLDER.currentPass, pMatrixStack);
            VREffectsHelper.renderGuiLayer(partialTicks, true, pMatrixStack);

            if (KeyboardHandler.Showing) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    VREffectsHelper.renderPhysicalKeyboard(partialTicks, pMatrixStack);
                } else {
                    VREffectsHelper.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room, vivecraft$DATA_HOLDER.vrSettings.menuAlwaysFollowFace && vivecraft$isInMenuRoom(), pMatrixStack);
                }
            }

            if ((vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                || vivecraft$DATA_HOLDER.vrSettings.mixedRealityRenderHands)
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
                VRArmHelper.renderVRHands(partialTicks, true, true, true, true, pMatrixStack);
            }
        }
        // pop the "level" push, since that would happen after this
        this.minecraft.getProfiler().pop();
        info.cancel();
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render(FJZ)V", ordinal = 0, argsOnly = true)
    private boolean vivecraft$renderGui(boolean doRender) {
        if (RenderPassType.isVanilla()) {
            return doRender;
        }
        return vivecraft$shouldDrawGui;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void vivecraft$noItemActivationAnimationOnGUI(GameRenderer instance, int i, int j, float f) {
        if (RenderPassType.isVanilla()) {
            renderItemActivationAnimation(i, j, f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"), method = "render(FJZ)V")
    private void vivecraft$noGUIwithViewOnly(Gui instance, GuiGraphics guiGraphics, float f) {
        if (RenderPassType.isVanilla() || !ClientDataHolderVR.viewonly) {
            instance.render(guiGraphics, f);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void vivecraft$noConfusionOverlayOnGUI(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.GUI) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void vivecraft$noTranslateItem(PoseStack poseStack, float x, float y, float z) {
        if (RenderPassType.isVanilla()) {
            poseStack.translate(x, y, z);
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void vivecraft$noScaleItem(PoseStack poseStack, float x, float y, float z, int width, int height, float partialTicks) {
        if (RenderPassType.isVanilla()) {
            poseStack.scale(x, y, z);
        } else {
            // need to do stuff twice, because redirects have no access to locals
            int i = 40 - this.itemActivationTicks;
            float g = ((float) i + partialTicks) / 40.0f;
            float h = g * g;
            float l = g * h;
            float m = 10.25f * l * h - 24.95f * h * h + 25.5f * l - 13.8f * h + 4.0f * g;
            float n = m * (float) Math.PI;
            float sinN = Mth.sin(n) * 0.5F;
            poseStack.translate(0, 0, sinN - 1.0);
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.THIRD) {
                sinN *= (float) (ClientDataHolderVR.getInstance().vrSettings.mixedRealityFov / 70.0);
            }
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass, poseStack);
            RenderHelper.applyStereo(ClientDataHolderVR.getInstance().currentPass, poseStack);
            poseStack.scale(sinN, sinN, sinN);
            poseStack.mulPose(Axis.YP.rotationDegrees(-ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolderVR.getInstance().currentPass).getYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(-ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolderVR.getInstance().currentPass).getPitch()));
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void vivecraft$renderpick(GameRenderer g, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }

        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.LEFT
            && !(Xplat.isModLoaded("immersive_portals") && ImmersivePortalsHelper.isRenderingPortal())) {
            this.pick(pPartialTicks);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
                this.vivecraft$crossVec = this.minecraft.hitResult.getLocation();
            }

            if (this.minecraft.screen == null) {
                vivecraft$DATA_HOLDER.teleportTracker.updateTeleportDestinations((GameRenderer) (Object) this, this.minecraft,
                    this.minecraft.player);
            }
        }

        this.vivecraft$cacheRVEPos((LivingEntity) this.minecraft.getCameraEntity());
        this.vivecraft$setupRVE();
        this.vivecraft$setupOverlayStatus(pPartialTicks);
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    public void vivecraft$removeBobHurt(PoseStack poseStack, float f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelBobView(PoseStack matrixStack, float f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "renderLevel")
    public int vivecraft$reduceNauseaSpeed(int oldVal) {
        if (!RenderPassType.isVanilla()) {
            return oldVal / 5;
        } else {
            return oldVal;
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 1), ordinal = 3, method = "renderLevel")
    public float vivecraft$reduceNauseaAffect(float oldVal) {
        if (!RenderPassType.isVanilla()) {
            // scales down the effect from (1,0.65) to (1,0.9)
            return 1f - (1f - oldVal) * 0.25f;
        } else {
            return oldVal;
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean vivecraft$noHandsVR(GameRenderer instance) {
        return RenderPassType.isVanilla() && renderHand;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel")
    public void vivecraft$disableStencil(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            VREffectsHelper.disableStencilTest();
        }
    }

    @Inject(at = @At(value = "TAIL", shift = Shift.BEFORE), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void vivecraft$restoreVE(float f, long j, PoseStack p, CallbackInfo i) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        this.vivecraft$restoreRVEPos((LivingEntity) this.minecraft.getCameraEntity());
    }

    @Override
    @Unique
    public void vivecraft$setupRVE() {
        if (this.vivecraft$cached) {
            VRData.VRDevicePose vrdata$vrdevicepose = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                .getEye(vivecraft$DATA_HOLDER.currentPass);
            Vec3 vec3 = vrdata$vrdevicepose.getPosition();
            LivingEntity livingentity = (LivingEntity) this.minecraft.getCameraEntity();
            livingentity.setPosRaw(vec3.x, vec3.y, vec3.z);
            livingentity.xOld = vec3.x;
            livingentity.yOld = vec3.y;
            livingentity.zOld = vec3.z;
            livingentity.xo = vec3.x;
            livingentity.yo = vec3.y;
            livingentity.zo = vec3.z;
            livingentity.setXRot(-vrdata$vrdevicepose.getPitch());
            livingentity.xRotO = livingentity.getXRot();
            livingentity.setYRot(vrdata$vrdevicepose.getYaw());
            livingentity.yHeadRot = livingentity.getYRot();
            livingentity.yHeadRotO = livingentity.getYRot();
            livingentity.eyeHeight = 0.0001F;
        }
    }

    @Override
    @Unique
    public void vivecraft$cacheRVEPos(LivingEntity e) {
        if (this.minecraft.getCameraEntity() != null) {
            if (!this.vivecraft$cached) {
                this.vivecraft$rveX = e.getX();
                this.vivecraft$rveY = e.getY();
                this.vivecraft$rveZ = e.getZ();
                this.vivecraft$rvelastX = e.xOld;
                this.vivecraft$rvelastY = e.yOld;
                this.vivecraft$rvelastZ = e.zOld;
                this.vivecraft$rveprevX = e.xo;
                this.vivecraft$rveprevY = e.yo;
                this.vivecraft$rveprevZ = e.zo;
                this.vivecraft$rveyaw = e.yHeadRot;
                this.vivecraft$rvepitch = e.getXRot();
                this.vivecraft$rvelastyaw = e.yHeadRotO;
                this.vivecraft$rvelastpitch = e.xRotO;
                this.vivecraft$rveHeight = e.getEyeHeight();
                this.vivecraft$cached = true;
            }
        }
    }

    @Override
    @Unique
    public void vivecraft$restoreRVEPos(LivingEntity e) {
        if (e != null) {
            e.setPosRaw(this.vivecraft$rveX, this.vivecraft$rveY, this.vivecraft$rveZ);
            e.xOld = this.vivecraft$rvelastX;
            e.yOld = this.vivecraft$rvelastY;
            e.zOld = this.vivecraft$rvelastZ;
            e.xo = this.vivecraft$rveprevX;
            e.yo = this.vivecraft$rveprevY;
            e.zo = this.vivecraft$rveprevZ;
            e.setYRot(this.vivecraft$rveyaw);
            e.setXRot(this.vivecraft$rvepitch);
            e.yRotO = this.vivecraft$rvelastyaw;
            e.xRotO = this.vivecraft$rvelastpitch;
            e.yHeadRot = this.vivecraft$rveyaw;
            e.yHeadRotO = this.vivecraft$rvelastyaw;
            e.eyeHeight = this.vivecraft$rveHeight;
            this.vivecraft$cached = false;
        }
    }

    @Override
    @Unique
    public double vivecraft$getRveY() {
        return vivecraft$rveY;
    }

    @Override
    @Unique
    public Vec3 vivecraft$getRvePos(float partialTicks) {
        return new Vec3(
            Mth.lerp(partialTicks, this.vivecraft$rvelastX, this.vivecraft$rveX),
            Mth.lerp(partialTicks, this.vivecraft$rvelastY, this.vivecraft$rveY),
            Mth.lerp(partialTicks, this.vivecraft$rvelastZ, this.vivecraft$rveZ)
        );
    }

    @Unique
    private void vivecraft$setupOverlayStatus(float partialTicks) {
        this.vivecraft$inBlock = 0.0F;
        this.vivecraft$inwater = false;
        this.vivecraft$onfire = false;

        if (!this.minecraft.player.isSpectator() && !this.vivecraft$isInMenuRoom() && this.minecraft.player.isAlive()) {
            Vec3 vec3 = RenderHelper.getSmoothCameraPosition(vivecraft$DATA_HOLDER.currentPass, vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render);
            Triple<Float, BlockState, BlockPos> triple = VREffectsHelper.getNearOpaqueBlock(vec3, this.vivecraft$minClipDistance);

            if (triple != null && !Xevents.renderBlockOverlay(this.minecraft.player, new PoseStack(), triple.getMiddle(), triple.getRight())) {
                this.vivecraft$inBlock = triple.getLeft();
            } else {
                this.vivecraft$inBlock = 0.0F;
            }

            this.vivecraft$inwater = this.minecraft.player.isEyeInFluid(FluidTags.WATER) && !Xevents.renderWaterOverlay(this.minecraft.player, new PoseStack());
            this.vivecraft$onfire = vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA && this.minecraft.player.isOnFire() && !Xevents.renderFireOverlay(this.minecraft.player, new PoseStack());
        }
    }

    @Override
    @Unique
    public boolean vivecraft$isInWater() {
        return vivecraft$inwater;
    }

    @Override
    @Unique
    public boolean vivecraft$wasInWater() {
        return vivecraft$wasinwater;
    }

    @Override
    @Unique
    public void vivecraft$setWasInWater(boolean b) {
        this.vivecraft$wasinwater = b;
    }

    @Override
    @Unique
    public boolean vivecraft$isOnFire() {
        return vivecraft$onfire;
    }

    @Override
    @Unique
    public boolean vivecraft$isInPortal() {
        return this.vivecraft$inportal;
    }

    @Override
    @Unique
    public float vivecraft$isInBlock() {
        return vivecraft$inBlock;
    }

    @Override
    @Unique
    public boolean vivecraft$isInMenuRoom() {
        return this.minecraft.level == null ||
            this.minecraft.screen instanceof WinScreen ||
            this.minecraft.screen instanceof ReceivingLevelScreen ||
            this.minecraft.screen instanceof ProgressScreen ||
            this.minecraft.screen instanceof GenericDirtMessageScreen ||
            ClientDataHolderVR.getInstance().integratedServerLaunchInProgress ||
            this.minecraft.getOverlay() != null;
    }

    @Override
    @Unique
    public boolean vivecraft$willBeInMenuRoom(Screen newScreen) {
        return this.minecraft.level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof ReceivingLevelScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericDirtMessageScreen ||
            ClientDataHolderVR.getInstance().integratedServerLaunchInProgress ||
            this.minecraft.getOverlay() != null;
    }

    @Override
    @Unique
    public Vec3 vivecraft$getCrossVec() {
        return vivecraft$crossVec;
    }

    @Override
    @Unique
    public void vivecraft$setupClipPlanes() {
        this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);

//		if (Config.isFogOn()) { TODO
//			this.renderDistance *= 0.95F;
//		}

        this.vivecraft$clipDistance = this.renderDistance + 1024.0F;
    }

    @Override
    @Unique
    public float vivecraft$getMinClipDistance() {
        return this.vivecraft$minClipDistance;
    }

    @Override
    @Unique
    public float vivecraft$getClipDistance() {
        return this.vivecraft$clipDistance;
    }

    @Override
    @Unique
    public Matrix4f vivecraft$getThirdPassProjectionMatrix() {
        return vivecraft$thirdPassProjectionMatrix;
    }

    @Override
    @Unique
    public void vivecraft$resetProjectionMatrix(float partialTicks) {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
    }
}
