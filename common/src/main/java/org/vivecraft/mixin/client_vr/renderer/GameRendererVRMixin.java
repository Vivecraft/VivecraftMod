package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.Xevents;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.XRCamera;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.nio.file.Path;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.convertToVec3;
import static org.vivecraft.common.utils.Utils.convertToVector3f;

@Mixin(GameRenderer.class)
public abstract class GameRendererVRMixin
    implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {

    @Unique
    public float vivecraft$minClipDistance = 0.02F;

    // TODO: @Nonnull and remove @CheckForNull
    @Unique
    public Vector3f vivecraft$crossVec;
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

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"))
    public Camera vivecraft$replaceCamera() {
        return new XRCamera();
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"), method = "pick")
    public ClientLevel vivecraft$appendCheck(Minecraft instance) {
        if (!vrRunning) {
            return instance.level;
        }
        return dh.vrPlayer.vrdata_world_render == null ? null : instance.level;
    }

    @Inject(at = @At("HEAD"), method = {"shutdownEffect", "checkEntityPostEffect", "cycleEffect", "loadEffect"})
    public void vivecraft$shutdownEffect(CallbackInfo ci) {
        if (vrInitialized) {
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
        if (vrInitialized) {
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
        if (!vrRunning) {
            return original;
        }
        mc.hitResult = dh.vrPlayer.rayTraceBlocksVR(dh.vrPlayer.vrdata_world_render, 0, mc.gameMode.getPickRange(), false);
        Vector3f dest = new Vector3f();
        this.vivecraft$getCrossVec(dest);
        this.vivecraft$crossVec = dh.vrPlayer.AimedPointAtDistance(dh.vrPlayer.vrdata_world_render, 0, mc.gameMode.getPickRange(), dest);
        return convertToVec3(dh.vrPlayer.vrdata_world_render.getController(0).getPosition(new Vector3f()));
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 vivecraft$vrVec31(Vec3 original) {
        if (!vrRunning) {
            return original;
        }
        return convertToVec3(dh.vrPlayer.vrdata_world_render.getController(0).getDirection(new Vector3f()));
    }

    //TODO Vivecraft add riding check in case your hand is somewhere inappropriate

    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void vivecraft$noFOVchangeInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.oldFov = this.fov = 1.0F;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void vivecraft$fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
        // Vivecraft: using this on the main menu
        if (mc.level == null || this.vivecraft$isInMenuRoom()) {
            info.setReturnValue(Double.valueOf(mc.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void vivecraft$projection(double d, CallbackInfoReturnable<Matrix4f> info) {
        if (!vrRunning) {
            return;
        }
        PoseStack posestack = new PoseStack();
        this.vivecraft$setupClipPlanes();
        Matrix4f view = posestack.last().pose();
        info.setReturnValue(switch (dh.currentPass) {
            case LEFT -> {
                yield view.mul(dh.vrRenderer.eyeproj[0]);
            }
            case RIGHT -> {
                yield view.mul(dh.vrRenderer.eyeproj[1]);
            }
            case THIRD -> {
                this.vivecraft$thirdPassProjectionMatrix.set(view.perspective(
                    toRadians(dh.vrSettings.mixedRealityFov),
                    dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY ?
                    dh.vrSettings.mixedRealityAspectRatio :
                    (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight(),
                    this.vivecraft$minClipDistance,
                    this.vivecraft$clipDistance
                ));
                yield view;
            }
            case CAMERA -> {
                yield view.perspective(
                    toRadians(dh.vrSettings.handCameraFov),
                    (float) dh.vrRenderer.cameraFramebuffer.viewWidth / (float) dh.vrRenderer.cameraFramebuffer.viewHeight,
                    this.vivecraft$minClipDistance,
                    this.vivecraft$clipDistance
                );
            }
            case SCOPEL, SCOPER -> {
                yield view.perspective(toRadians(70f / 8F), 1.0F, 0.05F, this.vivecraft$clipDistance);
            }
            default -> {
                if (this.zoom != 1.0F) {
                    view.translate(this.zoomX, -this.zoomY, 0.0F);
                    posestack.scale(this.zoom, this.zoom, 1.0F);
                }
                yield view.perspective(
                    toRadians((float) d),
                    (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight(),
                    0.05F,
                    this.vivecraft$clipDistance
                );
            }
        });
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean vivecraft$focus(Minecraft instance) {
        return vrRunning || instance.isWindowActive();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void vivecraft$pause(Minecraft instance, boolean bl) {
        if (!vrRunning || dh.currentPass == RenderPass.LEFT) {
            instance.pauseGame(bl);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long vivecraft$active() {
        if (!vrRunning || dh.currentPass == RenderPass.LEFT) {
            return Util.getMillis();
        } else {
            return this.lastActiveTime;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", shift = Shift.AFTER), method = "render(FJZ)V")
    public void vivecraft$matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        this.resetProjectionMatrix(this.getProjectionMatrix(mc.options.fov().get()));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
    public PoseStack vivecraft$newStack(PoseStack poseStack) {
        this.vivecraft$stack = poseStack;
        return poseStack;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.AFTER), method = "render(FJZ)V")
    public void vivecraft$renderoverlay(float f, long l, boolean bl, CallbackInfo ci) {
        if (vrRunning && dh.currentPass != RenderPass.THIRD && dh.currentPass != RenderPass.CAMERA) {
            VREffectsHelper.renderFaceOverlay(f, this.vivecraft$stack);
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean vivecraft$effect(GameRenderer instance) {
        return this.effectActive && dh.currentPass != RenderPass.THIRD;
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void vivecraft$noScreenshotInMenu(Path path, CallbackInfo ci) {
        if (vrRunning && this.vivecraft$isInMenuRoom()) {
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.BEFORE, ordinal = 6), method = "render(FJZ)V", cancellable = true)
    public void vivecraft$mainMenu(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!renderWorldIn && this.vivecraft$shouldDrawScreen) {
            this.vivecraft$shouldDrawScreen = false;
            return;
        }
        if (!renderWorldIn || mc.level == null || this.vivecraft$isInMenuRoom()) {
            mc.getProfiler().push("MainMenu");
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);

            PoseStack pMatrixStack = new PoseStack();
            RenderHelper.applyVRModelView(dh.currentPass, pMatrixStack);
            VREffectsHelper.renderGuiLayer(partialTicks, true, pMatrixStack);

            if (KeyboardHandler.isShowing()) {
                if (dh.vrSettings.physicalKeyboard) {
                    VREffectsHelper.renderPhysicalKeyboard(partialTicks, pMatrixStack);
                } else {
                    VREffectsHelper.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room, dh.vrSettings.menuAlwaysFollowFace && this.vivecraft$isInMenuRoom(), pMatrixStack);
                }
            }

            if (switch (dh.currentPass) {
                case THIRD -> {
                    yield dh.vrSettings.mixedRealityRenderHands;
                }
                case CAMERA -> {
                    yield false;
                }
                default -> {
                    yield true;
                }
            }) {
                VRArmHelper.renderVRHands(partialTicks, true, true, true, true, pMatrixStack);
            }
        }
        mc.getProfiler().pop();
        info.cancel();
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render(FJZ)V", ordinal = 0, argsOnly = true)
    private boolean vivecraft$renderGui(boolean doRender) {
        if (RenderPassType.isVanilla()) {
            return doRender;
        }
        return this.vivecraft$shouldDrawGui;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void vivecraft$noItemActivationAnimationOnGUI(GameRenderer instance, int i, int j, float f) {
        if (RenderPassType.isVanilla()) {
            this.renderItemActivationAnimation(i, j, f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"), method = "render(FJZ)V")
    private void vivecraft$noGUIwithViewOnly(Gui instance, GuiGraphics guiGraphics, float f) {
        if (RenderPassType.isVanilla() || !dh.viewonly) {
            instance.render(guiGraphics, f);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void vivecraft$noConfusionOverlayOnGUI(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (dh.currentPass == RenderPass.GUI) {
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
    private void vivecraft$noScaleItem(PoseStack poseStack, float x, float y, float z) {
        if (RenderPassType.isVanilla()) {
            poseStack.scale(x, y, z);
        }
    }

    @Inject(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$transformItem(int i, int j, float f, CallbackInfo ci, int k, float g, float h, float l, float m, float n, float o, float p, PoseStack posestack) {
        if (!RenderPassType.isVanilla()) {
            float sinN = sin(n) * 0.5F;
            posestack.last().pose().translate(0, 0, sinN - 1.0F);
            if (dh.currentPass == RenderPass.THIRD) {
                sinN *= dh.vrSettings.mixedRealityFov / 70.0;
            }
            RenderHelper.applyVRModelView(dh.currentPass, posestack);
            RenderHelper.applyStereo(dh.currentPass, posestack);
            posestack.scale(sinN, sinN, sinN);
            float angY = toRadians(-dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getYaw());
            float angX = toRadians(-dh.vrPlayer.getVRDataWorld().getEye(dh.currentPass).getPitch());
            posestack.last().pose()
                .rotateY(angY)
                .rotateX(angX);
            posestack.last().normal()
                .rotateY(angY)
                .rotateX(angX);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void vivecraft$renderpick(GameRenderer g, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }

        if (dh.currentPass == RenderPass.LEFT) {
            this.pick(pPartialTicks);

            if (mc.hitResult != null && mc.hitResult.getType() != Type.MISS) {
                Vector3f dest = new Vector3f();
                this.vivecraft$getCrossVec(dest);
                this.vivecraft$crossVec = convertToVector3f(mc.hitResult.getLocation(), dest);
            }

            if (mc.screen == null) {
                dh.teleportTracker.updateTeleportDestinations();
            }
        }

        this.vivecraft$cacheRVEPos((LivingEntity) mc.getCameraEntity());
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

    @ModifyVariable(at = @At("STORE"), method = "renderLevel")
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
            return 1.0F - (1.0F - oldVal) * 0.25F;
        } else {
            return oldVal;
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean vivecraft$noHandsVR(GameRenderer instance) {
        return RenderPassType.isVanilla() && this.renderHand;
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
        this.vivecraft$restoreRVEPos((LivingEntity) mc.getCameraEntity());
    }

    @Override
    @Unique
    public void vivecraft$setupRVE() {
        if (this.vivecraft$cached) {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass);
            Vector3f vec3 = vrdata$vrdevicepose.getPosition(new Vector3f());
            LivingEntity livingentity = (LivingEntity) mc.getCameraEntity();
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
        if (mc.getCameraEntity() != null) {
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
        return this.vivecraft$rveY;
    }

    @Override
    @Unique
    public Vector3f vivecraft$getRvePos(float partialTicks, Vector3f dest) {
        return dest.set(
            lerp(this.vivecraft$rvelastX, this.vivecraft$rveX, partialTicks),
            lerp(this.vivecraft$rvelastY, this.vivecraft$rveY, partialTicks),
            lerp(this.vivecraft$rvelastZ, this.vivecraft$rveZ, partialTicks)
        );
    }

    @Unique
    private void vivecraft$setupOverlayStatus(float partialTicks) {
        this.vivecraft$inBlock = 0.0F;
        this.vivecraft$inwater = false;
        this.vivecraft$onfire = false;

        if (!mc.player.isSpectator() && !this.vivecraft$isInMenuRoom() && mc.player.isAlive()) {
            Triple<Float, BlockState, BlockPos> triple = VREffectsHelper.getNearOpaqueBlock(dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition(new Vector3f()), this.vivecraft$minClipDistance);

            if (triple != null && !Xevents.renderBlockOverlay(mc.player, new PoseStack(), triple.getMiddle(), triple.getRight())) {
                this.vivecraft$inBlock = triple.getLeft();
            } else {
                this.vivecraft$inBlock = 0.0F;
            }

            this.vivecraft$inwater = mc.player.isEyeInFluid(FluidTags.WATER) && !Xevents.renderWaterOverlay(mc.player, new PoseStack());
            this.vivecraft$onfire = dh.currentPass != RenderPass.THIRD
                && dh.currentPass != RenderPass.CAMERA && mc.player.isOnFire() && !Xevents.renderFireOverlay(mc.player, new PoseStack());
        }
    }

    @Override
    @Unique
    public boolean vivecraft$isInWater() {
        return this.vivecraft$inwater;
    }

    @Override
    @Unique
    public boolean vivecraft$wasInWater() {
        return this.vivecraft$wasinwater;
    }

    @Override
    @Unique
    public void vivecraft$setWasInWater(boolean b) {
        this.vivecraft$wasinwater = b;
    }

    @Override
    @Unique
    public boolean vivecraft$isOnFire() {
        return this.vivecraft$onfire;
    }

    @Override
    @Unique
    public boolean vivecraft$isInPortal() {
        return this.vivecraft$inportal;
    }

    @Override
    @Unique
    public float vivecraft$isInBlock() {
        return this.vivecraft$inBlock;
    }

    @Override
    @Unique
    public boolean vivecraft$isInMenuRoom() {
        return mc.level == null ||
            mc.screen instanceof WinScreen ||
            mc.screen instanceof ReceivingLevelScreen ||
            mc.screen instanceof ProgressScreen ||
            mc.screen instanceof GenericDirtMessageScreen ||
            dh.integratedServerLaunchInProgress ||
            mc.getOverlay() != null;
    }

    @Override
    @Unique
    public boolean vivecraft$willBeInMenuRoom(Screen newScreen) {
        return mc.level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof ReceivingLevelScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericDirtMessageScreen ||
            dh.integratedServerLaunchInProgress ||
            mc.getOverlay() != null;
    }

    @Override
    @Unique
    @CheckForNull
    public Vector3f vivecraft$getCrossVec(Vector3f dest) {
        return this.vivecraft$crossVec != null ? dest.set(this.vivecraft$crossVec) : null;
    }

    @Override
    @Unique
    public void vivecraft$setupClipPlanes() {
        this.renderDistance = (float) (mc.options.getEffectiveRenderDistance() * 16);

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
        return this.vivecraft$thirdPassProjectionMatrix;
    }

    @Override
    @Unique
    public void vivecraft$resetProjectionMatrix(float partialTicks) {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
    }
}
