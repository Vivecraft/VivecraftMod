package org.vivecraft.mixin.client_vr.renderer;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xevents;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRWidgetHelper;
import org.vivecraft.client_vr.render.XRCamera;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_vr.settings.VRSettings.RenderPointerElement;
import org.vivecraft.client_vr.settings.VRSettings.ShaderGUIRender;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11C;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import javax.annotation.CheckForNull;
import java.nio.file.Path;
import java.util.Calendar;

import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.common.utils.Utils.convertToVector3f;
import static org.vivecraft.common.utils.Utils.logger;

import static org.joml.Math.*;
import static org.joml.RoundingMode.FLOOR;

import static net.minecraft.client.Minecraft.ON_OSX;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public abstract class GameRendererVRMixin
        implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {

    @Unique
    public float minClipDistance = 0.02F;
    @Unique
    public Vec3 crossVec;
    @Unique
    public Matrix4f thirdPassProjectionMatrix = new Matrix4f();
    @Unique
    public boolean inwater;
    @Unique
    public boolean wasinwater;
    @Unique
    public boolean inportal;
    @Unique
    public boolean onfire;
    @Unique
    public float inBlock = 0.0F;
    @Unique
    public double rveX;
    @Unique
    public double rveY;
    @Unique
    public double rveZ;
    @Unique
    public double rvelastX;
    @Unique
    public double rvelastY;
    @Unique
    public double rvelastZ;
    @Unique
    public double rveprevX;
    @Unique
    public double rveprevY;
    @Unique
    public double rveprevZ;
    @Unique
    public float rveyaw;
    @Unique
    public float rvepitch;
    @Unique
    private float rvelastyaw;
    @Unique
    private float rvelastpitch;
    @Unique
    private float rveHeight;
    @Unique
    private boolean cached;
    @Unique
    private int polyblendsrca;
    @Unique
    private int polyblenddsta;
    @Unique
    private int polyblendsrcrgb;
    @Unique
    private int polyblenddstrgb;
    // private net.optifine.shaders.Program prog;
    @Unique
    private boolean polyblend;
    @Unique
    private boolean polytex;
    @Unique
    private boolean polylight;
    @Unique
    private boolean polycull;
    @Unique
    private Vec3i tpUnlimitedColor = new Vec3i(-83, -40, -26);
    @Unique
    private Vec3i tpLimitedColor = new Vec3i(-51, -87, -51);
    @Unique
    private Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

    @Unique // TODO added by optifine...
    private float clipDistance = 128.0F;

    @Unique
    private PoseStack stack;

    @Shadow
    private float renderDistance;

    @Shadow
    @Final
    private LightTexture lightTexture;
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
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    @Final
    public ItemInHandRenderer itemInHandRenderer;
    @Shadow
    private int tick;
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
    public abstract OverlayTexture overlayTexture();

    @Shadow
    @Final
    private Camera mainCamera;

    @Override
    public double getRveY()
    {
        return this.rveY;
    }

    @Override
    public float inBlock()
    {
        return this.inBlock;
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"))
    Camera replaceCamera()
    {
        return new XRCamera();
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"), method = "pick")
    public ClientLevel appendCheck(Minecraft instance)
    {
        if (!vrRunning)
        {
            return instance.level;
        }
        return dh.vrPlayer.vrdata_world_render == null ? null : instance.level;
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 0)
    public Vec3 rayTrace(Vec3 original)
    {
        if (!vrRunning)
        {
            return original;
        }
        mc.hitResult = dh.vrPlayer.rayTraceBlocksVR(dh.vrPlayer.vrdata_world_render, 0, mc.gameMode.getPickRange(), false);
        this.crossVec = dh.vrPlayer.AimedPointAtDistance(dh.vrPlayer.vrdata_world_render, 0, mc.gameMode.getPickRange());
        return dh.vrPlayer.vrdata_world_render.getController(0).getPosition();
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 vrVec31(Vec3 original)
    {
        if (!vrRunning)
        {
            return original;
        }
        return dh.vrPlayer.vrdata_world_render.getController(0).getDirection();
    }

    //TODO Vivecraft add riding check in case your hand is somewhere inappropriate

    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void noFOVchangeInVR(CallbackInfo ci)
    {
        if (!RenderPassType.isVanilla())
        {
            this.oldFov = this.fov = 1.0F;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info)
    {
        // Vivecraft: using this on the main menu
        if (mc.level == null || this.isInMenuRoom())
        {
            info.setReturnValue(Double.valueOf(mc.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void projection(double d, CallbackInfoReturnable<Matrix4f> info)
    {
        if (!vrRunning) {
            return;
        }
        PoseStack posestack = new PoseStack();
        this.setupClipPlanes();
        Matrix4f view = posestack.last().pose();
        info.setReturnValue(switch (dh.currentPass) {
            case LEFT -> { yield view.mul(dh.vrRenderer.eyeproj[0]); }
            case RIGHT -> { yield view.mul(dh.vrRenderer.eyeproj[1]); }
            case THIRD ->
            {
                this.thirdPassProjectionMatrix.set(view.perspective(
                    toRadians(dh.vrSettings.mixedRealityFov),
                    dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY ?
                        dh.vrSettings.mixedRealityAspectRatio :
                        (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight(),
                    this.minClipDistance,
                    this.clipDistance
                ));
                yield view;
            }
            case CAMERA ->
            {
                yield view.perspective(
                    toRadians(dh.vrSettings.handCameraFov),
                    (float) dh.vrRenderer.cameraFramebuffer.viewWidth / (float) dh.vrRenderer.cameraFramebuffer.viewHeight,
                    this.minClipDistance,
                    this.clipDistance
                );
            }
            case SCOPEL, SCOPER -> { yield view.perspective(toRadians(70f / 8f), 1.0F, 0.05F, this.clipDistance); }
            default -> {
                if (this.zoom != 1.0F) {
                    view.translate(this.zoomX, -this.zoomY, 0.0F);
                    posestack.scale(this.zoom, this.zoom, 1.0F);
                }
                yield view.perspective(
                    (float) toRadians(d),
                    (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getScreenHeight(),
                    0.05F,
                    this.clipDistance
                );
            }
        });
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean focus(Minecraft instance)
    {
        return vrRunning || instance.isWindowActive();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void pause(Minecraft instance, boolean bl)
    {
        if (!vrRunning || dh.currentPass == RenderPass.LEFT)
        {
            instance.pauseGame(bl);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long active()
    {
        if (!vrRunning || dh.currentPass == RenderPass.LEFT)
        {
            return Util.getMillis();
        }
        else
        {
            return this.lastActiveTime;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info)
    {
        this.resetProjectionMatrix(this.getProjectionMatrix(mc.options.fov().get()));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
    public PoseStack newStack(PoseStack poseStack)
    {
        this.stack = poseStack;
        return poseStack;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void renderoverlay(float f, long l, boolean bl, CallbackInfo ci)
    {
        if (vrRunning && dh.currentPass != RenderPass.THIRD && dh.currentPass != RenderPass.CAMERA)
        {
            this.renderFaceOverlay(f, this.stack);
        }
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean effect(GameRenderer instance)
    {
        return this.effectActive && dh.currentPass != RenderPass.THIRD;
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void noScreenshotInMenu(Path path, CallbackInfo ci)
    {
        if (vrRunning && this.isInMenuRoom())
        {
            ci.cancel();
        }
    }

    @Unique
    private boolean shouldDrawScreen = false;
    @Unique
    private boolean shouldDrawGui = false;

    @Override
    public void setShouldDrawScreen(boolean shouldDrawScreen)
    {
        this.shouldDrawScreen = shouldDrawScreen;
    }

    @Override
    public void setShouldDrawGui(boolean shouldDrawGui)
    {
        this.shouldDrawGui = shouldDrawGui;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.BEFORE, ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
    public void mainMenu(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info)
    {
        if (RenderPassType.isVanilla())
        {
            return;
        }

        if (!renderWorldIn && this.shouldDrawScreen)
        {
            this.shouldDrawScreen = false;
            return;
        }
        if (!renderWorldIn || mc.level == null || this.isInMenuRoom())
        {
            mc.getProfiler().push("MainMenu");
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);

            PoseStack pMatrixStack = new PoseStack();
            this.applyVRModelView(dh.currentPass, pMatrixStack);
            this.renderGuiLayer(partialTicks, true, pMatrixStack);

            if (KeyboardHandler.isShowing()) {
                if (dh.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, pMatrixStack);
                } else {
                    this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, dh.vrSettings.menuAlwaysFollowFace && this.isInMenuRoom(), pMatrixStack);
                }
            }

            if ((dh.currentPass != RenderPass.THIRD || dh.vrSettings.mixedRealityRenderHands) &&
                dh.currentPass != RenderPass.CAMERA
            )
            {
                this.renderVRHands(partialTicks, true, true, true, true, pMatrixStack);
            }
        }
        mc.getProfiler().pop();
        info.cancel();
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render(FJZ)V", ordinal = 0, argsOnly = true)
    private boolean renderGui(boolean doRender)
    {
        if (RenderPassType.isVanilla()) {
            return doRender;
        }
        return this.shouldDrawGui;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void noItemActivationAnimationOnGUI(GameRenderer instance, int i, int j, float f)
    {
        if (RenderPassType.isVanilla()) {
            this.renderItemActivationAnimation(i, j, f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"), method = "render(FJZ)V")
    private void noGUIwithViewOnly(Gui instance, GuiGraphics guiGraphics, float f)
    {
        if (RenderPassType.isVanilla() || !dh.viewonly)
        {
            instance.render(guiGraphics, f);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void noConfusionOverlayOnGUI(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (dh.currentPass == RenderPass.GUI)
        {
            ci.cancel();
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void noTranslateItem(PoseStack poseStack, float x, float y, float z)
    {
        if (RenderPassType.isVanilla()) {
            poseStack.translate(x, y, z);
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void noScaleItem(PoseStack poseStack, float x, float y, float z)
    {
        if (RenderPassType.isVanilla())
        {
            poseStack.scale(x, y, z);
        }
    }

    @Inject(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void transformItem(int i, int j, float f, CallbackInfo ci, int k, float g, float h, float l, float m, float n, float o, float p, PoseStack posestack)
    {
        if (!RenderPassType.isVanilla())
        {
            float sinN = sin(n) * 0.5F;
            posestack.last().pose().translate(0, 0, sinN - 1.0F);
            if (dh.currentPass == RenderPass.THIRD)
            {
                sinN *= dh.vrSettings.mixedRealityFov / 70.0;
            }
            this.applyVRModelView(dh.currentPass, posestack);
            this.applystereo(dh.currentPass, posestack);
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
    public void renderpick(GameRenderer g, float pPartialTicks)
    {
        if (RenderPassType.isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }

        if (dh.currentPass == RenderPass.LEFT) {
            this.pick(pPartialTicks);

            if (mc.hitResult != null && mc.hitResult.getType() != Type.MISS) {
                this.crossVec = mc.hitResult.getLocation();
            }

            if (mc.screen == null) {
                dh.teleportTracker.updateTeleportDestinations();
            }
        }

        this.cacheRVEPos((LivingEntity) mc.getCameraEntity());
        this.setupRVE();
        this.setupOverlayStatus(pPartialTicks);
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    public void removeBobHurt(PoseStack poseStack, float f, CallbackInfo ci)
    {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    void cancelBobView(PoseStack matrixStack, float f, CallbackInfo ci)
    {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @ModifyVariable(at = @At("STORE"), method = "renderLevel")
    public int reduceNauseaSpeed(int oldVal)
    {
        if (!RenderPassType.isVanilla()) {
            return oldVal / 5;
        } else {
            return oldVal;
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 1), ordinal = 3, method = "renderLevel")
    public float reduceNauseaAffect(float oldVal)
    {
        if (!RenderPassType.isVanilla()) {
            // scales down the effect from (1,0.65) to (1,0.9)
            return 1.0F - (1.0F - oldVal) * 0.25F;
        } else {
            return oldVal;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1), method = "renderLevel")
    public void noHandProfiler(ProfilerFiller instance, String s)
    {
        GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        mc.getProfiler().popPush("ShadersEnd"); //TODO needed?
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean noHandsVR(GameRenderer instance)
    {
        return RenderPassType.isVanilla() && this.renderHand;
    }

    @Inject(at = @At(value = "TAIL", shift = Shift.BEFORE), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void restoreVE(float f, long j, PoseStack p, CallbackInfo i)
    {
        if (RenderPassType.isVanilla()) {
            return;
        }
        this.restoreRVEPos((LivingEntity) mc.getCameraEntity());
    }

    private void setupOverlayStatus(float partialTicks)
    {
        this.inBlock = 0.0F;
        this.inwater = false;
        this.onfire = false;

        if (!mc.player.isSpectator() && !this.isInMenuRoom() && mc.player.isAlive()) {
            Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition();
            Triple<Float, BlockState, BlockPos> triple = ((ItemInHandRendererExtension) this.itemInHandRenderer).getNearOpaqueBlock(vec3, (double) this.minClipDistance);

            if (triple != null && !Xevents.renderBlockOverlay(mc.player, new PoseStack(), triple.getMiddle(), triple.getRight())) {
                this.inBlock = triple.getLeft();
            } else {
                this.inBlock = 0.0F;
            }

            this.inwater = mc.player.isEyeInFluid(FluidTags.WATER) && !Xevents.renderWaterOverlay(mc.player, new PoseStack());
            this.onfire = dh.currentPass != RenderPass.THIRD
                    && dh.currentPass != RenderPass.CAMERA && mc.player.isOnFire() && !Xevents.renderFireOverlay(mc.player, new PoseStack());
        }
    }

    @Override
    public void setupRVE()
    {
        if (this.cached) {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass);
            Vec3 vec3 = vrdata$vrdevicepose.getPosition();
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
    public void cacheRVEPos(LivingEntity e)
    {
        if (mc.getCameraEntity() != null) {
            if (!this.cached) {
                this.rveX = e.getX();
                this.rveY = e.getY();
                this.rveZ = e.getZ();
                this.rvelastX = e.xOld;
                this.rvelastY = e.yOld;
                this.rvelastZ = e.zOld;
                this.rveprevX = e.xo;
                this.rveprevY = e.yo;
                this.rveprevZ = e.zo;
                this.rveyaw = e.yHeadRot;
                this.rvepitch = e.getXRot();
                this.rvelastyaw = e.yHeadRotO;
                this.rvelastpitch = e.xRotO;
                this.rveHeight = e.getEyeHeight();
                this.cached = true;
            }
        }
    }

    void renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack)
    {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, false)));
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        this.applyVRModelView(dh.currentPass, poseStack);
        this.SetupRenderingAtController(c, poseStack);

        if (mc.getOverlay() == null) {
            mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        }

        Tesselator tesselator = Tesselator.getInstance();

        if (depthAlways && c == 0) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        Vec3i vec3i = new Vec3i(64, 64, 64);
        byte b0 = -1;
        Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 vec31 = dh.vrPlayer.vrdata_world_render.getController(c).getDirection();
        Vec3 vec32 = dh.vrPlayer.vrdata_world_render.getController(c).getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        vec32 = new Vec3(0.0D, 1.0D, 0.0D);
        vec31 = new Vec3(0.0D, 0.0D, -1.0D);
        Vec3 vec33 = new Vec3(vec3.x - vec31.x * 0.18D, vec3.y - vec31.y * 0.18D, vec3.z - vec31.z * 0.18D);

        if (mc.level != null) {
            float f = mc.level.getMaxLocalRawBrightness(
                BlockPos.containing(dh.vrPlayer.vrdata_world_render.hmd.getPosition())
            );

            int i = ShadersHelper.ShaderLight();

            if (f < (float) i) {
                f = (float) i;
            }

            float f1 = f / (float) mc.level.getMaxLightLevel();
            vec3i = new Vec3i(
                roundUsing(vec3i.getX() * f1, FLOOR),
                roundUsing(vec3i.getY() * f1, FLOOR),
                roundUsing(vec3i.getZ() * f1, FLOOR)
            );
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        this.renderBox(tesselator, vec3, vec33, -0.02F, 0.02F, -0.0125F, 0.0125F, vec32, vec3i, b0, poseStack);
        BufferUploader.drawWithShader(tesselator.getBuilder().end());
        poseStack.popPose();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    private void renderVRHands(float partialTicks, boolean renderright, boolean renderleft, boolean menuhandright,
                               boolean menuhandleft, PoseStack poseStack)
    {
        mc.getProfiler().push("hands");
        // backup projection matrix, not doing that breaks sodium water on 1.19.3
        RenderSystem.backupProjectionMatrix();

        if (renderright) {
            mc.getItemRenderer();
            dh.ismainhand = true;

            if (menuhandright) {
                this.renderMainMenuHand(0, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack = new PoseStack();
                posestack.last().pose().identity();
                this.applyVRModelView(dh.currentPass, posestack);
                this.renderVRHand_Main(posestack, partialTicks);
            }

            mc.getItemRenderer();
            dh.ismainhand = false;
        }

        if (renderleft) {
            if (menuhandleft) {
                this.renderMainMenuHand(1, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack1 = new PoseStack();
                posestack1.last().pose().identity();
                this.applyVRModelView(dh.currentPass, posestack1);
                this.renderVRHand_Offhand(partialTicks, true, posestack1);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        mc.getProfiler().pop();
    }

    @Override
    public boolean isInWater()
    {
        return this.inwater;
    }

    @Override
    public boolean isInMenuRoom()
    {
        return mc.level == null ||
            mc.screen instanceof WinScreen ||
            mc.screen instanceof ReceivingLevelScreen ||
            mc.screen instanceof ProgressScreen ||
            mc.screen instanceof GenericDirtMessageScreen ||
            dh.integratedServerLaunchInProgress ||
            mc.getOverlay() != null;
    }

    @Override
    public boolean willBeInMenuRoom(Screen newScreen)
    {
        return mc.level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof ReceivingLevelScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericDirtMessageScreen ||
            dh.integratedServerLaunchInProgress ||
            mc.getOverlay() != null;
    }

    @Override
    public Vec3 getControllerRenderPos(int c)
    {
        if (!dh.vrSettings.seated) {
            return dh.vrPlayer.vrdata_world_render.getController(c).getPosition();
        } else {
            Vec3 vec3;

            if (mc.getCameraEntity() != null && mc.level != null) {
                Vec3 vec32 = dh.vrPlayer.vrdata_world_render.hmd.getDirection();
                vec32 = vec32.yRot(toRadians(c == 0 ? -35.0F : 35.0F));
                vec32 = new Vec3(vec32.x, 0.0D, vec32.z);
                vec32 = vec32.normalize();
                RenderPass renderpass = RenderPass.CENTER;
                vec3 = dh.vrPlayer.vrdata_world_render.getEye(renderpass).getPosition().add(
                        vec32.x * 0.3D * (double) dh.vrPlayer.vrdata_world_render.worldScale,
                        -0.4D * (double) dh.vrPlayer.vrdata_world_render.worldScale,
                        vec32.z * 0.3D * (double) dh.vrPlayer.vrdata_world_render.worldScale);

                if (TelescopeTracker.isTelescope(mc.player.getUseItem())) {
                    if (c == 0 && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND)
                        vec3 = dh.vrPlayer.vrdata_world_render.eye0.getPosition()
                                .add(dh.vrPlayer.vrdata_world_render.hmd.getDirection()
                                        .scale(0.2 * dh.vrPlayer.vrdata_world_render.worldScale));
                    if (c == 1 && mc.player.getUsedItemHand() == InteractionHand.OFF_HAND)
                        vec3 = dh.vrPlayer.vrdata_world_render.eye1.getPosition()
                                .add(dh.vrPlayer.vrdata_world_render.hmd.getDirection()
                                        .scale(0.2 * dh.vrPlayer.vrdata_world_render.worldScale));
                }

            } else {
                Vec3 vec31 = dh.vrPlayer.vrdata_world_render.hmd.getDirection();
                vec31 = vec31.yRot(toRadians(c == 0 ? -35.0F : 35.0F));
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                vec31 = vec31.normalize();
                vec3 = dh.vrPlayer.vrdata_world_render.hmd.getPosition().add(vec31.x * 0.3D, -0.4D,
                        vec31.z * 0.3D);
            }

            return vec3;
        }
    }

    @Override @CheckForNull
    public Vec3 getCrossVec() {
        return this.crossVec;
    }

    @Override
    public void setupClipPlanes() {
        this.renderDistance = (float) (mc.options.getEffectiveRenderDistance() * 16);

//		if (Config.isFogOn()) { TODO
//			this.renderDistance *= 0.95F;
//		}

        this.clipDistance = this.renderDistance + 1024.0F;

    }

    @Override
    public float getMinClipDistance() {
        return this.minClipDistance;
    }

    @Override
    public float getClipDistance() {
        return this.clipDistance;
    }

    @Override
    public void applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
        Matrix4f modelView = dh.vrPlayer.vrdata_world_render.getEye(currentPass).getMatrix();
        poseStack.last().pose().mul(modelView);
        poseStack.last().normal().mul(new Matrix3f(modelView));
    }

    @Override
    public void renderDebugAxes(int r, int g, int b, float radius) {
        this.setupPolyRendering(true);
        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        this.renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
        this.setupPolyRendering(false);
    }

    public void renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        tesselator.getBuilder().vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();

        for (int i = 0; i < edges + 1; i++) {
            float f = (float) i / (float) edges * (float) PI * 2.0F;

            if (side != 0 && side != 1) {
                if (side != 2 && side != 3) {
                    if (side == 4 || side == 5) {
                        float f5 = (float) pos.x;
                        float f7 = (float) pos.y + cos(f) * radius;
                        float f9 = (float) pos.z + sin(f) * radius;
                        tesselator.getBuilder().vertex(f5, f7, f9).color(r, g, b, a).endVertex();
                    }
                } else {
                    float f4 = (float) pos.x + cos(f) * radius;
                    float f6 = (float) pos.y + sin(f) * radius;
                    float f8 = (float) pos.z;
                    tesselator.getBuilder().vertex(f4, f6, f8).color(r, g, b, a).endVertex();
                }
            } else {
                float f1 = (float) pos.x + cos(f) * radius;
                float f2 = (float) pos.y;
                float f3 = (float) pos.z + sin(f) * radius;
                tesselator.getBuilder().vertex(f1, f2, f3).color(r, g, b, a).endVertex();
            }
        }

        tesselator.end();
    }

    private void setupPolyRendering(boolean enable) {
        // boolean shadersMod = false; // Config.isShaders(); TODO
        // boolean shadersModShadowPass = false;

        if (enable) {
            this.polyblendsrca = GlStateManager.BLEND.srcAlpha;
            this.polyblenddsta = GlStateManager.BLEND.dstAlpha;
            this.polyblendsrcrgb = GlStateManager.BLEND.srcRgb;
            this.polyblenddstrgb = GlStateManager.BLEND.dstRgb;
            this.polyblend = GL11C.glIsEnabled(GL11C.GL_BLEND);
            // this.polytex = GL11C.glIsEnabled(GL11C.GL_TEXTURE_2D);
            // this.polylight = false;
            this.polycull = GL11C.glIsEnabled(GL11C.GL_CULL_FACE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // GlStateManager._disableLighting();
            RenderSystem.disableCull();

            // if (shadersMod) {
                // this.prog = Shaders.activeProgram; TODO
                // Shaders.useProgram(Shaders.ProgramTexturedLit);
            // }
        } else {
            RenderSystem.blendFuncSeparate(this.polyblendsrcrgb, this.polyblenddstrgb, this.polyblendsrca, this.polyblenddsta);

            if (!this.polyblend) {
                RenderSystem.disableBlend();
            }

            // if (this.polytex) {
            // }

            // if (this.polylight) {
                // GlStateManager._enableLighting();
            // }

            if (this.polycull) {
                RenderSystem.enableCull();
            }

            // if (shadersMod && this.polytex) {
                // Shaders.useProgram(this.prog); TODO
            // }
        }
    }

    @Override
    public void drawScreen(float f, Screen screen, GuiGraphics guiGraphics) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.last().pose().translate(0.0F, 0.0F, -2000.0F);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.blendFuncSeparate(
            SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE
        );
        screen.render(guiGraphics, 0, 0, f);
        RenderSystem.blendFuncSeparate(
            SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE
        );
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        mc.getMainRenderTarget().bindRead();
        ((RenderTargetExtension) mc.getMainRenderTarget()).genMipMaps();
        mc.getMainRenderTarget().unbindRead();
    }

    @Override
    public boolean wasInWater() {
        return this.wasinwater;
    }

    @Override
    public void setWasInWater(boolean b) {
        this.wasinwater = b;
    }

    @Override
    public boolean isInPortal() {
        return this.inportal;
    }

    @Override
    public Matrix4f getThirdPassProjectionMatrix() {
        return this.thirdPassProjectionMatrix;
    }

    private void renderVRHand_Main(PoseStack matrix, float partialTicks) {
        matrix.pushPose();
        this.SetupRenderingAtController(0, matrix);
        ItemStack itemstack = mc.player.getMainHandItem();
        ItemStack itemstack1 = null; // mc.physicalGuiManager.getHeldItemOverride();

        if (itemstack1 != null) {
            itemstack = itemstack1;
        }

        if (dh.climbTracker.isClimbeyClimb() && itemstack.getItem() != Items.SHEARS) {
            itemstack = itemstack1 == null ? mc.player.getOffhandItem() : itemstack1;
        }

        if (BowTracker.isHoldingBow(InteractionHand.MAIN_HAND)) {
            int i = 0;

            if (dh.vrSettings.reverseShootingEye) {
                i = 1;
            }

            ItemStack itemstack2 = mc.player.getProjectile(mc.player.getMainHandItem());

            if (itemstack2 != ItemStack.EMPTY && !dh.bowTracker.isNotched()) {
                itemstack = itemstack2;
            } else {
                itemstack = ItemStack.EMPTY;
            }
        } else if (BowTracker.isHoldingBow(InteractionHand.OFF_HAND)
                && dh.bowTracker.isNotched()) {
            int j = 0;

            if (dh.vrSettings.reverseShootingEye) {
                j = 1;
            }

            itemstack = ItemStack.EMPTY;
        }

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
        }
        matrix.pushPose();

        this.lightTexture.turnOnLightLayer();
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
        (this.itemInHandRenderer).renderArmWithItem(mc.player, partialTicks,
                0.0F, InteractionHand.MAIN_HAND, mc.player.getAttackAnim(partialTicks), itemstack, 0.0F,
                matrix, multibuffersource$buffersource,
                mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTicks));
        multibuffersource$buffersource.endBatch();
        this.lightTexture.turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        matrix.popPose();

        matrix.popPose();
    }

    private void renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack matrix) {
        // boolean shadersMod = Config.isShaders();TODO
        boolean shadersMod = false;
        boolean shadersModShadowPass = false;

//		if (shadersMod) {
//			shadersModShadowPass = Shaders.isShadowPass;
//		}

        matrix.pushPose();
        this.SetupRenderingAtController(1, matrix);
        ItemStack itemstack = mc.player.getOffhandItem();
        ItemStack itemstack1 = null;// mc.physicalGuiManager.getOffhandOverride();

        if (itemstack1 != null) {
            itemstack = itemstack1;
        }

        if (dh.climbTracker.isClimbeyClimb()
                && (itemstack == null || itemstack.getItem() != Items.SHEARS)) {
            itemstack = mc.player.getMainHandItem();
        }

        if (BowTracker.isHoldingBow(InteractionHand.MAIN_HAND)) {
            int i = 1;

            if (dh.vrSettings.reverseShootingEye) {
                i = 0;
            }

            itemstack = mc.player.getMainHandItem();
        }

		if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
		}
        matrix.pushPose();

        this.lightTexture.turnOnLightLayer();
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
        this.itemInHandRenderer.renderArmWithItem(mc.player, partialTicks,
                0.0F, InteractionHand.OFF_HAND, mc.player.getAttackAnim(partialTicks), itemstack, 0.0F,
                matrix, multibuffersource$buffersource,
                mc.getEntityRenderDispatcher().getPackedLightCoords(mc.player, partialTicks));
        multibuffersource$buffersource.endBatch();
        this.lightTexture.turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        matrix.popPose();

        matrix.popPose();

        if (renderTeleport) {
            matrix.pushPose();
            matrix.setIdentity();
            this.applyVRModelView(dh.currentPass, matrix);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                SourceFactor.SRC_ALPHA,
                DestFactor.ONE_MINUS_SRC_ALPHA,
                SourceFactor.ONE,
                DestFactor.ONE_MINUS_SRC_ALPHA
            );
            if (ClientNetworking.isLimitedSurvivalTeleport() && !dh.vrPlayer.getFreeMove()
                    && mc.gameMode.hasMissTime()
                    && dh.teleportTracker.vrMovementStyle.arcAiming
                    && !dh.bowTracker.isActive()) {
                matrix.pushPose();
                this.SetupRenderingAtController(1, matrix);
                Vec3 vec3 = new Vec3(0.0D, 0.005D, 0.03D);
                float f1 = 0.03F;
                float f;

                if (dh.teleportTracker.isAiming()) {
                    f = 2.0F * (float) ((double) dh.teleportTracker.getTeleportEnergy()
                        - 4.0D * dh.teleportTracker.movementTeleportDistance) / 100.0F * f1;
                } else {
                    f = 2.0F * dh.teleportTracker.getTeleportEnergy() / 100.0F * f1;
                }

                if (f < 0.0F) {
                    f = 0.0F;
                }
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                this.renderFlatQuad(vec3.add(0.0D, 0.05001D, 0.0D), f, f, 0.0F, this.tpLimitedColor.getX(),
                        this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 128, matrix);
                this.renderFlatQuad(vec3.add(0.0D, 0.05D, 0.0D), f1, f1, 0.0F, this.tpLimitedColor.getX(),
                        this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 50, matrix);
                matrix.popPose();
            }

            if (dh.teleportTracker.isAiming()) {
                RenderSystem.enableDepthTest();

                if (dh.teleportTracker.vrMovementStyle.arcAiming) {
                    this.renderTeleportArc(matrix);
                }

            }

            RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

            matrix.popPose();
        }
    }

    void render2D(float par1, RenderTarget framebuffer, Vec3 pos, Matrix4f rot, boolean depthAlways, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing) {
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            this.applyVRModelView(dh.currentPass, poseStack);
            Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition();
            float f = GuiHandler.guiScale;
            Vec3 guipos = VRPlayer.room_to_world_pos(pos, dh.vrPlayer.vrdata_world_render);

            poseStack.last().pose()
                .translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z))
                .mul(rot)
                .rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians)
                .translate(0.0F, 0.0F, 0.0F);
            float f1 = f * dh.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(f1, f1, f1);

            framebuffer.bindRead();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, framebuffer.getColorTextureId());

            float[] color = new float[]{1, 1, 1, 1};
            if (!this.isInMenuRoom()) {
                if (mc.screen == null) {
                    color[3] = dh.vrSettings.hudOpacity;
                }

                if (mc.player != null && mc.player.isShiftKeyDown()) {
                    color[3] *= 0.75F;
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    SourceFactor.SRC_ALPHA,
                    DestFactor.ONE_MINUS_SRC_ALPHA,
                    SourceFactor.ONE_MINUS_DST_ALPHA,
                    DestFactor.ONE
                );
            } else {
                RenderSystem.disableBlend();
            }

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();


            if (mc.level != null) {
                if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec3)) {
                    vec3 = dh.vrPlayer.vrdata_world_render.hmd.getPosition();
                }

                int i = ShadersHelper.ShaderLight();
                int j = Utils.getCombinedLightWithMin(mc.level, BlockPos.containing(vec3), i);
                this.drawSizedQuadWithLightmap((float) mc.getWindow().getGuiScaledWidth(),
                        (float) mc.getWindow().getGuiScaledHeight(), 1.5F, j, color,
                        poseStack.last().pose());
            } else {
                this.drawSizedQuad((float) mc.getWindow().getGuiScaledWidth(),
                        (float) mc.getWindow().getGuiScaledHeight(), 1.5F, color, poseStack.last().pose());
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.enableCull();

            poseStack.popPose();
        }
    }

    void renderPhysicalKeyboard(float partialTicks, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing) {
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            // RenderSystem.enableRescaleNormal();
            // Lighting.setupFor3DItems();

            mc.getProfiler().push("applyPhysicalKeyboardModelView");
            Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition();
            Vec3 guipos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room, dh.vrPlayer.vrdata_world_render);
            poseStack.last().pose()
                .mul(dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getMatrix())
                .translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z))
                .mul(new Matrix4f(KeyboardHandler.Rotation_room).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians));
            float f = dh.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(f, f, f);
            mc.getProfiler().pop();

            KeyboardHandler.physicalKeyboard.render(poseStack);
            // Lighting.turnOff();
            // RenderSystem.disableRescaleNormal();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private void renderGuiLayer(float par1, boolean depthAlways, PoseStack poseStack) {
        if (!dh.bowTracker.isDrawing && (mc.screen != null || !mc.options.hideGui) && !RadialHandler.isShowing())
        {
            mc.getProfiler().push("GuiLayer");
            // cache fog distance
            float fogStart = RenderSystem.getShaderFogStart();

            // remove nausea effect from projection matrix, for vanilla, nd posestack for iris
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            this.applyVRModelView(dh.currentPass, poseStack);

            boolean flag = this.isInMenuRoom();

            // render the screen always on top in the menu room to prevent z fighting
            depthAlways |= flag;

            PoseStack poseStack1 = RenderSystem.getModelViewStack();
            poseStack1.pushPose();
            poseStack1.setIdentity();
            RenderSystem.applyModelViewMatrix();

            if (flag)
            {
                poseStack.pushPose();
                Vec3 eye = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass).getPosition();
                poseStack.last().pose()
                    .translate(
                        (float) (dh.vrPlayer.vrdata_world_render.origin.x - eye.x),
                        (float) (dh.vrPlayer.vrdata_world_render.origin.y - eye.y),
                        (float) (dh.vrPlayer.vrdata_world_render.origin.z - eye.z)
                    )
                    // remove world rotation or the room doesn't align with the screen
                    .rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
                poseStack.last().normal().rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);

                //logger.info(eye + " eye");
                //logger.info(dh.vrPlayer.vrdata_world_render.origin + " world");

                if (dh.menuWorldRenderer.isReady())
                {
                    try
                    {
                        this.renderTechjarsAwesomeMainMenuRoom(poseStack);
                    } catch (Exception exception)
                    {
                        logger.info("Error rendering main menu world, unloading to prevent more errors");
                        exception.printStackTrace();
                        dh.menuWorldRenderer.destroy();
                    }
                }
                else
                {
                    this.renderJrbuddasAwesomeMainMenuRoomNew(poseStack);
                }
                poseStack.popPose();
            }

            Vec3 vec31 = GuiHandler.applyGUIModelView(dh.currentPass, poseStack);
            GuiHandler.guiFramebuffer.bindRead();
            RenderSystem.disableCull();
            RenderSystem.setShaderTexture(0, GuiHandler.guiFramebuffer.getColorTextureId());

            float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
            if (!flag)
            {
                if (mc.screen == null)
                {
                    color[3] = dh.vrSettings.hudOpacity;
                }
                else
                {
                    // disable fog for menus
                    RenderSystem.setShaderFogStart(Float.MAX_VALUE);
                }

                if (mc.player != null && mc.player.isShiftKeyDown())
                {
                    color[3] *= 0.75F;
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    SourceFactor.SRC_ALPHA,
                    DestFactor.ONE_MINUS_SRC_ALPHA,
                    SourceFactor.ONE_MINUS_DST_ALPHA,
                    DestFactor.ONE
                );
                if (dh.vrSettings.shaderGUIRender == ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID &&
                    ShadersHelper.isShaderActive())
                {
                    RenderSystem.disableBlend();
                }
            }
            else
            {
                // enable blend for overlay transition in menuworld to not be jarring
                RenderSystem.enableBlend();
            }

            if (depthAlways)
            {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            }
            else
            {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();

            // RenderSystem.disableLighting();

            if (mc.level != null)
            {
                if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec31))
                {
                    vec31 = dh.vrPlayer.vrdata_world_render.hmd.getPosition();
                }

                int i = ShadersHelper.ShaderLight();
                int j = Utils.getCombinedLightWithMin(mc.level, BlockPos.containing(vec31), i);
                this.drawSizedQuadWithLightmap((float) mc.getWindow().getGuiScaledWidth(),
                    (float) mc.getWindow().getGuiScaledHeight(), 1.5F, j, color,
                    poseStack.last().pose()
                );
            }
            else
            {
                this.drawSizedQuad((float) mc.getWindow().getGuiScaledWidth(),
                    (float) mc.getWindow().getGuiScaledHeight(), 1.5F, color,
                    poseStack.last().pose()
                );
            }

            // RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
            // reset fog
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.enableDepthTest();
            // RenderSystem.defaultAlphaFunc();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            poseStack.popPose();

            poseStack1.popPose();
            RenderSystem.applyModelViewMatrix();
            mc.getProfiler().pop();
        }
    }

    public void SetupRenderingAtController(int controller, PoseStack matrix)
    {
        Vec3 vec3 = this.getControllerRenderPos(controller);
        vec3 = vec3.subtract(dh.vrPlayer.getVRDataWorld()
                .getEye(dh.currentPass).getPosition());
        matrix.last().pose().translate((float) vec3.x, (float) vec3.y, (float) vec3.z);
        float sc = dh.vrPlayer.vrdata_world_render.worldScale;
        if (mc.level != null && TelescopeTracker.isTelescope(mc.player.getUseItem())) {
            matrix.last().pose()
                .mul(dh.vrPlayer.vrdata_world_render.hmd.getMatrix())
                .rotateX(toRadians(90.0F))
                .translate(controller == 0 ? 0.075F * sc : -0.075F * sc, -0.025F * sc, 0.0325F * sc);
            matrix.last().normal().rotateX(toRadians(90.0F));
        } else {
            matrix.last().pose().mul(dh.vrPlayer.vrdata_world_render.getController(controller).getMatrix());
        }

        matrix.scale(sc, sc, sc);

    }

    public void renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a, PoseStack poseStack)
    {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        Vec3 vec3 = (new Vec3(-width / 2.0F, 0.0D, height / 2.0F)).yRot(toRadians(-yaw));
        Vec3 vec31 = new Vec3(-width / 2.0F, 0.0D, -height / 2.0F).yRot(toRadians(-yaw));
        Vec3 vec32 = new Vec3(width / 2.0F, 0.0D, -height / 2.0F).yRot(toRadians(-yaw));
        Vec3 vec33 = new Vec3(width / 2.0F, 0.0D, height / 2.0F).yRot(toRadians(-yaw));
        Matrix4f mat = poseStack.last().pose();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + vec3.x), (float) pos.y, (float) (pos.z + vec3.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + vec31.x), (float) pos.y, (float) (pos.z + vec31.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + vec32.x), (float) pos.y, (float) (pos.z + vec32.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.getBuilder().vertex(mat, (float) (pos.x + vec33.x), (float) pos.y, (float) (pos.z + vec33.z))
                .color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
        tesselator.end();

    }

    private void renderBox(Tesselator tes, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY, Vec3 up,
        Vec3i color, byte alpha, PoseStack poseStack
    )
    {
        Vec3 vec3 = start.subtract(end).normalize();
        Vec3 vec31 = vec3.cross(up);
        up = vec31.cross(vec3);
        Vec3 vec32 = new Vec3(vec31.x * (double) minX, vec31.y * (double) minX, vec31.z * (double) minX);
        vec31 = vec31.scale((double) maxX);
        Vec3 vec33 = new Vec3(up.x * (double) minY, up.y * (double) minY, up.z * (double) minY);
        up = up.scale((double) maxY);
        Vector3f vector3f = convertToVector3f(vec3);
        Vector3f vector3f1 = convertToVector3f(up.normalize());
        Vector3f vector3f2 = convertToVector3f(vec31.normalize());
        Vec3 vec34 = start.add(vec31.x + vec33.x, vec31.y + vec33.y, vec31.z + vec33.z);
        Vec3 vec35 = start.add(vec31.x + up.x, vec31.y + up.y, vec31.z + up.z);
        Vec3 vec36 = start.add(vec32.x + vec33.x, vec32.y + vec33.y, vec32.z + vec33.z);
        Vec3 vec37 = start.add(vec32.x + up.x, vec32.y + up.y, vec32.z + up.z);
        Vec3 vec38 = end.add(vec31.x + vec33.x, vec31.y + vec33.y, vec31.z + vec33.z);
        Vec3 vec39 = end.add(vec31.x + up.x, vec31.y + up.y, vec31.z + up.z);
        Vec3 vec310 = end.add(vec32.x + vec33.x, vec32.y + vec33.y, vec32.z + vec33.z);
        Vec3 vec311 = end.add(vec32.x + up.x, vec32.y + up.y, vec32.z + up.z);
        BufferBuilder bufferbuilder = tes.getBuilder();
        Matrix4f mat = poseStack.last().pose();
        bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
                .endVertex();
        bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
                .endVertex();
    }

    private void renderJrbuddasAwesomeMainMenuRoomNew(PoseStack pMatrixStack) {
        int repeat = 4;
        float height = 2.5F;
        float oversize = 1.3F;
        Vector2f area = dh.vr.getPlayAreaSize();
        if (area == null)
            area = new Vector2f(2, 2);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        pMatrixStack.pushPose();
        float width = area.x + oversize;
        float length = area.y + oversize;
        Matrix4f matrix4f = pMatrixStack.last().pose()
            .translate(-width / 2.0F, 0.0F, -length / 2.0F);

        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, length).uv(0.0F, repeat * length).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0.0F, length).uv(repeat * width, repeat * length).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0.0F, 0.0F).uv(repeat * width, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 1.0F, 0.0F).endVertex();

        bufferbuilder.vertex(matrix4f, 0.0F, height, length).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, height, 0.0F).uv(0.0F, repeat * length).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0.0F).uv(repeat * width, repeat * length).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * width, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, -1.0F, 0.0F).endVertex();

        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, height, 0.0F).uv(0.0F, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, height, length).uv(repeat * length, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, length).uv(repeat * length, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(1.0F, 0.0F, 0.0F).endVertex();

        bufferbuilder.vertex(matrix4f, width, 0.0F, 0.0F).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0.0F, length).uv(repeat * length, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * length, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0.0F).uv(0.0F, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(-1.0F, 0.0F, 0.0F).endVertex();

        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, 0.0F).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0.0F, 0.0F).uv(repeat * width, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0.0F).uv(repeat * width, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, height, 0.0F).uv(0.0F, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, 1.0F).endVertex();

        bufferbuilder.vertex(matrix4f, 0.0F, 0.0F, length).uv(0.0F, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, 0.0F, height, length).uv(0.0F, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * width, repeat * height).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0.0F, length).uv(repeat * width, 0.0F).color(0.8F, 0.8F, 0.8F, 1.0F).normal(0.0F, 0.0F, -1.0F).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        pMatrixStack.popPose();

    }

    private void renderTechjarsAwesomeMainMenuRoom(PoseStack poseStack) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.enableDepthTest();
        //RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();

        poseStack.pushPose();

        int tzOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        dh.menuWorldRenderer.time = dh.menuWorldRenderer.fastTime
            ? (long)(dh.menuWorldRenderer.ticks * 10L + 10 * mc.getFrameTime())
            : (long)((System.currentTimeMillis() + tzOffset - 21600000) / 86400000D * 24000D);

        dh.menuWorldRenderer.fogRenderer.setupFogColor();
        RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT, ON_OSX);

        dh.menuWorldRenderer.updateLightmap();
        dh.menuWorldRenderer.render(poseStack);

        Vector2f area = dh.vr.getPlayAreaSize();
        if (area != null) {
            poseStack.pushPose();
            float width = area.x;//(float)ceil(area.x);
            float length = area.y;//(float)ceil(area.y);

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
            float sun = dh.menuWorldRenderer.getSkyDarken();
            RenderSystem.setShaderColor(sun, sun, sun, 0.3f);


            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);
            bufferbuilder
                .vertex(matrix4f, 0, 0.005F, 0)
                .uv(0, 0)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, 0, 0.005F, length)
                .uv(0, 4 * length)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005F, length)
                .uv(4 * width, 4 * length)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005F, 0)
                .uv(4 * width, 0)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .normal(0, 1, 0).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.defaultBlendFunc();
    }

    public void renderVRFabulous(float partialTicks, LevelRenderer worldrendererin, boolean menuHandRight, boolean menuHandLeft,
        PoseStack pMatrix
    )
    {
        if (dh.currentPass == RenderPass.SCOPEL || dh.currentPass == RenderPass.SCOPER)
            return;
        mc.getProfiler().popPush("VR");
        this.renderCrosshairAtDepth(!dh.vrSettings.useCrosshairOcclusion, pMatrix);
        mc.getMainRenderTarget().unbindWrite();
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().bindWrite(true);

        if (this.shouldOccludeGui()) {
            this.renderGuiLayer(partialTicks, false, pMatrix);
            this.renderVrShadow(partialTicks, false, pMatrix);

            if (KeyboardHandler.isShowing()) {
                if (dh.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, pMatrix);
                } else {
                    this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, pMatrix);
            }
        }

        ((LevelRendererExtension) worldrendererin).getAlphaSortVRUnoccludedFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

        if (!this.shouldOccludeGui()) {
            this.renderGuiLayer(partialTicks, false, pMatrix);
            this.renderVrShadow(partialTicks, false, pMatrix);

            if (KeyboardHandler.isShowing()) {
                if (dh.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, pMatrix);
                } else {
                    this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, pMatrix);
            }
        }

        this.renderVRSelfEffects(partialTicks);
        VRWidgetHelper.renderVRThirdPersonCamWidget();
        VRWidgetHelper.renderVRHandheldCameraWidget();
        boolean should = this.shouldRenderHands();
        this.renderVRHands(partialTicks, should && menuHandRight, should && menuHandLeft, true, true, pMatrix);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().clear(ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().copyDepthFrom(mc.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().bindWrite(true);
        this.renderVRHands(partialTicks, should && !menuHandRight, should && !menuHandLeft, false, false, pMatrix);
        RenderSystem.defaultBlendFunc();
        // RenderSystem.defaultAlphaFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // Lighting.turnBackOn();
        // Lighting.turnOff();
        mc.getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void renderVrFast(float partialTicks, boolean secondpass, boolean menuright, boolean menuleft, PoseStack pMatrix)
    {
        if (dh.currentPass == RenderPass.SCOPEL || dh.currentPass == RenderPass.SCOPER)
            return;
        mc.getProfiler().popPush("VR");
        this.lightTexture.turnOffLightLayer();

        if (secondpass) {
            this.renderVrShadow(partialTicks, !this.shouldOccludeGui(), pMatrix);
        }

        if (!secondpass) {
            this.renderCrosshairAtDepth(!dh.vrSettings.useCrosshairOcclusion, pMatrix);
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRThirdPersonCamWidget();
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRHandheldCameraWidget();
        }

        if (secondpass && (mc.screen != null || !KeyboardHandler.isShowing())) {
            this.renderGuiLayer(partialTicks, !this.shouldOccludeGui(), pMatrix);
        }

        if (secondpass && KeyboardHandler.isShowing()) {
            if (dh.vrSettings.physicalKeyboard) {
                this.renderPhysicalKeyboard(partialTicks, pMatrix);
            } else {
                this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room, !this.shouldOccludeGui(), pMatrix);
            }
        }

        if (secondpass && RadialHandler.isShowing()) {
            this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
                    !this.shouldOccludeGui(), pMatrix);
        }
        // render hands in second pass when gui is open
        boolean renderHandsSecond = RadialHandler.isShowing() || KeyboardHandler.isShowing() || mc.screen != null;
        if (secondpass == renderHandsSecond) {
            // should render hands in second pass if menus are open, else in the first pass
            // only render the hands only once
            this.renderVRHands(partialTicks, this.shouldRenderHands(), this.shouldRenderHands(), menuright, menuleft, pMatrix);
        }
        this.renderVRSelfEffects(partialTicks);
    }

    public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color) {
        float f = displayHeight / displayWidth;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        bufferbuilder.vertex((double) (-(size / 2.0F)), (double) (-(size * f) / 2.0F), 0.0D).uv(0.0F, 0.0F)
                .color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double) (size / 2.0F), (double) (-(size * f) / 2.0F), 0.0D).uv(1.0F, 0.0F)
                .color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double) (size / 2.0F), (double) (size * f / 2.0F), 0.0D).uv(1.0F, 1.0F)
                .color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex((double) (-(size / 2.0F)), (double) (size * f / 2.0F), 0.0D).uv(0.0F, 1.0F)
                .color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        float f = displayHeight / displayWidth;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void drawSizedQuadSolid(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);
        this.lightTexture.turnOnLightLayer();
        this.overlayTexture().setupOverlayColor();
        float f = displayHeight / displayWidth;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        int light = LightTexture.pack(15, 15);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        this.lightTexture.turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }


    public void drawSizedQuad(float displayWidth, float displayHeight, float size) {
        this.drawSizedQuad(displayWidth, displayHeight, size, new float[]{1, 1, 1, 1});
    }

    public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti, float[] color,
        Matrix4f pMatrix
    )
    {
        RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        float f = displayHeight / displayWidth;
        this.lightTexture.turnOnLightLayer();
        this.overlayTexture().setupOverlayColor();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
                .uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lighti).normal(0, 0, 1).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        this.lightTexture.turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }

    public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti, Matrix4f pMatrix) {
        this.drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix);
    }

    private void renderTeleportArc(PoseStack poseStack) {
        if (dh.teleportTracker.vrMovementStyle.showBeam && dh.teleportTracker.isAiming() &&
            dh.teleportTracker.movementTeleportArcSteps > 1
        )
        {
            mc.getProfiler().push("teleportArc");
            // boolean flag = Config.isShaders();
            boolean flag = false;
            // boolean isShader = Config.isShaders();
            // boolean isShader = false;
            RenderSystem.enableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
            Tesselator tesselator = Tesselator.getInstance();
            tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            double d0 = dh.teleportTracker.lastTeleportArcDisplayOffset;
            Vec3 vec3 = dh.teleportTracker.getDestination();
            boolean flag1 = vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D;
            byte b0 = -1;
            Vec3i vec3i;

            if (!flag1) {
                vec3i = new Vec3i(83, 75, 83);
                b0 = -128;
            } else {
                if (ClientNetworking.isLimitedSurvivalTeleport() && !mc.player.getAbilities().mayfly) {
                    vec3i = this.tpLimitedColor;
                } else {
                    vec3i = this.tpUnlimitedColor;
                }

                d0 = dh.vrRenderer.getCurrentTimeSecs()
                        * (double) dh.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
                dh.teleportTracker.lastTeleportArcDisplayOffset = d0;
            }

            float f = dh.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
            int i = dh.teleportTracker.movementTeleportArcSteps - 1;

            if (dh.teleportTracker.vrMovementStyle.beamGrow) {
                i = (int) ((double) i * dh.teleportTracker.movementTeleportProgress);
            }

            double d1 = 1.0D / (double) i;
            Vec3 vec31 = new Vec3(0.0D, 1.0D, 0.0D);

            for (int j = 0; j < i; ++j) {
                double d2 = (double) j / (double) i + d0 * d1;
                int k = roundUsing(d2, FLOOR);
                d2 = d2 - (double) ((float) k);
                Vec3 vec32 = dh.teleportTracker
                        .getInterpolatedArcPosition((float) (d2 - d1 * (double) 0.4F))
                        .subtract(mc.getCameraEntity().position());
                Vec3 vec33 = dh.teleportTracker.getInterpolatedArcPosition((float) d2)
                        .subtract(mc.getCameraEntity().position());
                float f2 = (float) d2 * 2.0F;
                this.renderBox(tesselator, vec32, vec33, -f, f, (-1.0F + f2) * f, (1.0F + f2) * f, vec31, vec3i, b0,
                        poseStack);
            }

            tesselator.end();
            RenderSystem.disableCull();

            if (flag1 && dh.teleportTracker.movementTeleportProgress >= 1.0D) {
                Vec3 vec34 = (new Vec3(vec3.x, vec3.y, vec3.z)).subtract(mc.getCameraEntity().position());
                int l = 1;
                float f1 = 0.01F;
                double d4 = 0.0D;
                double d5 = 0.0D;
                double d3 = 0.0D;

                if (l == 0) {
                    d5 -= (double) f1;
                }

                if (l == 1) {
                    d5 += (double) f1;
                }

                if (l == 2) {
                    d3 -= (double) f1;
                }

                if (l == 3) {
                    d3 += (double) f1;
                }

                if (l == 4) {
                    d4 -= (double) f1;
                }

                if (l == 5) {
                    d4 += (double) f1;
                }

                this.renderFlatQuad(vec34.add(d4, d5, d3), 0.6F, 0.6F, 0.0F, (int) ((double) vec3i.getX() * 1.03D),
                        (int) ((double) vec3i.getY() * 1.03D), (int) ((double) vec3i.getZ() * 1.03D), 64, poseStack);

                if (l == 0) {
                    d5 -= (double) f1;
                }

                if (l == 1) {
                    d5 += (double) f1;
                }

                if (l == 2) {
                    d3 -= (double) f1;
                }

                if (l == 3) {
                    d3 += (double) f1;
                }

                if (l == 4) {
                    d4 -= (double) f1;
                }

                if (l == 5) {
                    d4 += (double) f1;
                }

                this.renderFlatQuad(vec34.add(d4, d5, d3), 0.4F, 0.4F, 0.0F, (int) ((double) vec3i.getX() * 1.04D),
                        (int) ((double) vec3i.getY() * 1.04D), (int) ((double) vec3i.getZ() * 1.04D), 64, poseStack);

                if (l == 0) {
                    d5 -= (double) f1;
                }

                if (l == 1) {
                    d5 += (double) f1;
                }

                if (l == 2) {
                    d3 -= (double) f1;
                }

                if (l == 3) {
                    d3 += (double) f1;
                }

                if (l == 4) {
                    d4 -= (double) f1;
                }

                if (l == 5) {
                    d4 += (double) f1;
                }

                this.renderFlatQuad(vec34.add(d4, d5, d3), 0.2F, 0.2F, 0.0F, (int) ((double) vec3i.getX() * 1.05D),
                        (int) ((double) vec3i.getY() * 1.05D), (int) ((double) vec3i.getZ() * 1.05D), 64, poseStack);
            }

            mc.getProfiler().pop();
            RenderSystem.enableCull();
        }
    }

    @Override
    public void drawEyeStencil(boolean flag1) {

        if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
            if ((dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT) && dh.vrSettings.vrUseStencil) {
//				net.optifine.shaders.Program program = Shaders.activeProgram;
//
//				if (shaders && Shaders.dfb != null) {
//					Shaders.dfb.bindFramebuffer();
//					Shaders.useProgram(Shaders.ProgramNone);
//
//					for (int i = 0; i < Shaders.usedDepthBuffers; ++i) {
//						GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(i));
//						mc.vrRenderer.doStencil(false);
//					}
//
//					Shaders.useProgram(program);
//				} else {
                dh.vrRenderer.doStencil(false);
//				}
            } else {
                GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            }
        }
        else
        {
            // No stencil for telescope
            // dh.vrRenderer.doStencil(true);
        }
    }

    private void renderFaceOverlay(float par1, PoseStack pMatrix)
    {
        // boolean shadersMod = Config.isShaders();
        // boolean shadersMod = false;

        // if (shadersMod) { TODO
            // Shaders.beginFPOverlay();
        // }

        if (this.inBlock > 0.0F)
        {
            this.renderFaceInBlock();
            this.renderGuiLayer(par1, true, pMatrix);

            if (KeyboardHandler.isShowing())
            {
                if (dh.vrSettings.physicalKeyboard)
                {
                    this.renderPhysicalKeyboard(par1, pMatrix);
                }
                else
                {
                    this.render2D(par1, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room, KeyboardHandler.Rotation_room,
                        true, pMatrix
                    );
                }
            }

            if (RadialHandler.isShowing())
            {
                this.render2D(par1, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room, true,
                    pMatrix
                );
            }

            if (this.inBlock >= 1.0F)
            {
                this.renderVRHands(par1, true, true, true, true, pMatrix);
            }
        }

        // if (shadersMod) { TODO
            // Shaders.endFPOverlay();
        // }
    }

    private void renderFaceInBlock()
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, ((GameRendererExtension) mc.gameRenderer).inBlock());

        // orthographic matrix, (-1, -1) to (1, 1), near = 0.0, far 2.0
        Matrix4f mat = new Matrix4f();
        mat.m00(1.0F);
        mat.m11(1.0F);
        mat.m22(-1.0F);
        mat.m33(1.0F);
        mat.m32(-1.0F);

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean shouldRenderCrosshair()
    {
        if (dh.viewonly) {
            return false;
        } else if (mc.level == null) {
            return false;
        } else if (mc.screen != null) {
            return false;
        } else {
            boolean flag = dh.vrSettings.renderInGameCrosshairMode == RenderPointerElement.ALWAYS
                    || (dh.vrSettings.renderInGameCrosshairMode == RenderPointerElement.WITH_HUD
                    && !mc.options.hideGui);

            if (!flag) {
                return false;
            } else if (dh.currentPass == RenderPass.THIRD) {
                return false;
            } else if (dh.currentPass != RenderPass.SCOPEL
                    && dh.currentPass != RenderPass.SCOPER) {
                if (dh.currentPass == RenderPass.CAMERA) {
                    return false;
                } else if (KeyboardHandler.isShowing()) {
                    return false;
                } else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
                    return false;
                } else if (dh.bowTracker.isNotched()) {
                    return false;
                } else if (!dh.vr.getInputAction(VivecraftVRMod.keyVRInteract)
                        .isEnabledRaw(ControllerType.RIGHT)
                        && !VivecraftVRMod.keyVRInteract.isDown(ControllerType.RIGHT)) {
                    if (!dh.vr.getInputAction(VivecraftVRMod.keyClimbeyGrab)
                            .isEnabledRaw(ControllerType.RIGHT)
                            && !VivecraftVRMod.keyClimbeyGrab.isDown(ControllerType.RIGHT)) {
                        if (dh.teleportTracker.isAiming()) {
                            return false;
                        } else if (dh.climbTracker.isGrabbingLadder(0)) {
                            return false;
                        } else {
                            return !(dh.vrPlayer.worldScale > 15.0F);
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private void renderCrosshairAtDepth(boolean depthAlways, PoseStack poseStack) {
        if (this.shouldRenderCrosshair())
        {
            mc.getProfiler().push("crosshair");
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec3 vec3 = this.crossVec;
            Vec3 vec31 = vec3.subtract(dh.vrPlayer.vrdata_world_render.getController(0).getPosition());
            float f = (float) vec31.length();
            float f1 = (float) ((double) (0.125F * dh.vrSettings.crosshairScale)
                    * sqrt((double) dh.vrPlayer.vrdata_world_render.worldScale));
            vec3 = vec3.add(vec31.normalize().scale(-0.01D));
            poseStack.pushPose();
            poseStack.setIdentity();
            this.applyVRModelView(dh.currentPass, poseStack);

            Vec3 vec32 = vec3.subtract(mc.getCameraEntity().position());
            poseStack.last().pose().translate((float) vec32.x, (float) vec32.y, (float) vec32.z);

            if (mc.hitResult instanceof BlockHitResult hit && mc.hitResult.getType() == Type.BLOCK) {
                switch (hit.getDirection())
                {
                    case DOWN -> {
                        float ang1 = toRadians(dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                        float ang2 = toRadians(-90.0F);
                        poseStack.last().pose().rotateY(ang1).rotateX(ang2);
                        poseStack.last().normal().rotateY(ang1).rotateX(ang2);
                    }
                    case UP ->
                    {
                        float ang1 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                        float ang2 = toRadians(90.0F);
                        poseStack.last().pose().rotateY(ang1).rotateX(ang2);
                        poseStack.last().normal().rotateY(ang1).rotateX(ang2);
                    }
                    case WEST ->
                    {
                        float ang = toRadians(90.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                    case EAST ->
                    {
                        float ang = toRadians(-90.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                    case SOUTH ->
                    {
                        float ang = toRadians(180.0F);
                        poseStack.last().pose().rotateY(ang);
                        poseStack.last().normal().rotateY(ang);
                    }
                }
            }
            else
            {
                float ang1 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getYaw());
                float ang2 = toRadians(-dh.vrPlayer.vrdata_world_render.getController(0).getPitch());
                poseStack.last().pose()
                    .rotateY(ang1)
                    .rotateX(ang2);
                poseStack.last().normal()
                    .rotateY(ang1)
                    .rotateX(ang2);
            }

            if (dh.vrSettings.crosshairScalesWithDistance)
            {
                float f5 = 0.3F + 0.2F * f;
                f1 *= f5;
            }

            this.lightTexture.turnOnLightLayer();
            poseStack.scale(f1, f1, f1);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            // RenderSystem.disableLighting();

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            // boolean shadersMod = Config.isShaders();
            // boolean shadersMod = false;
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA
            );
            int i = LevelRenderer.getLightColor(mc.level, BlockPos.containing(vec3));
            float f2 = 1.0F;

            if (mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
                f2 = 0.5F;
            }

            RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
            float f3 = 0.00390625F;
            float f4 = 0.00390625F;

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

            bufferbuilder.vertex(poseStack.last().pose(), -1.0F, 1.0F, 0.0F).color(f2, f2, f2, 1.0F)
                    .uv(0.0F, 15.0F * f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), 1.0F, 1.0F, 0.0F).color(f2, f2, f2, 1.0F)
                    .uv(15.0F * f3, 15.0F * f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), 1.0F, -1.0F, 0.0F).color(f2, f2, f2, 1.0F)
                    .uv(15.0F * f3, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(poseStack.last().pose(), -1.0F, -1.0F, 0.0F).color(f2, f2, f2, 1.0F)
                    .uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            poseStack.popPose();
            mc.getProfiler().pop();
        }
    }

    public boolean shouldOccludeGui() {
        Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getEye(dh.currentPass)
                .getPosition();

        if (dh.currentPass != RenderPass.THIRD
                && dh.currentPass != RenderPass.CAMERA) {
            return !this.isInMenuRoom() && mc.screen == null && !KeyboardHandler.isShowing()
                    && !RadialHandler.isShowing() && dh.vrSettings.hudOcclusion
                    && !((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec3);
        } else {
            return true;
        }
    }

    private void renderVrShadow(float par1, boolean depthAlways, PoseStack poseStack) {
        if (dh.currentPass != RenderPass.THIRD && dh.currentPass != RenderPass.CAMERA && mc.player.isAlive() &&
            !(((PlayerExtension) mc.player).getRoomYOffsetFromPose() < 0.0D) &&
            mc.player.getVehicle() == null
        )
        {
            mc.getProfiler().push("vr shadow");
            AABB aabb = mc.player.getBoundingBox();

            if (dh.vrSettings.vrShowBlueCircleBuddy && aabb != null)
            {

                poseStack.pushPose();
                poseStack.setIdentity();
                RenderSystem.disableCull();
                this.applyVRModelView(dh.currentPass, poseStack);
                Vec3 vec3 = dh.vrPlayer.vrdata_world_render
                    .getEye(dh.currentPass).getPosition();
                Vec3 vec31 = new Vec3(
                    this.rvelastX + (this.rveX - this.rvelastX) * (double) par1,
                    this.rvelastY + (this.rveY - this.rvelastY) * (double) par1,
                    this.rvelastZ + (this.rveZ - this.rvelastZ) * (double) par1
                );
                Vec3 vec32 = vec31.subtract(vec3).add(0.0D, 0.005D, 0.0D);
                this.setupPolyRendering(true);
                RenderSystem.enableDepthTest();

                if (depthAlways)
                {
                    RenderSystem.depthFunc(GL11C.GL_ALWAYS);
                }
                else
                {
                    RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                }

                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                this.renderFlatQuad(vec32, (float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ),
                    0.0F, 0, 0, 0, 64, poseStack
                );
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                this.setupPolyRendering(false);
                poseStack.popPose();
                RenderSystem.enableCull();
            }
            mc.getProfiler().pop();
        }
    }

    public boolean shouldRenderHands()
    {
        if (dh.viewonly) {
            return false;
        } else if (dh.currentPass == RenderPass.THIRD) {
            return dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY;
        } else {
            return dh.currentPass != RenderPass.CAMERA;
        }
    }

    private void renderVRSelfEffects(float par1)
    {
        if (this.onfire && dh.currentPass != RenderPass.THIRD
                && dh.currentPass != RenderPass.CAMERA) {
            this.renderFireInFirstPerson();
        }
        this.renderItemActivationAnimation(0, 0, par1);
    }

    private void renderFireInFirstPerson()
    {
        PoseStack posestack = new PoseStack();
        this.applyVRModelView(dh.currentPass, posestack);
        this.applystereo(dh.currentPass, posestack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        if (dh.currentPass == RenderPass.THIRD || dh.currentPass == RenderPass.CAMERA) {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.enableDepthTest();

//		if (SmartAnimations.isActive()) { TODO
//			SmartAnimations.spriteRendered(textureatlassprite);
//		}

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
        float f = textureatlassprite.getU0();
        float f1 = textureatlassprite.getU1();
        float f2 = (f + f1) / 2.0F;
        float f3 = textureatlassprite.getV0();
        float f4 = textureatlassprite.getV1();
        float f5 = (f3 + f4) / 2.0F;
        float f6 = textureatlassprite.uvShrinkRatio();
        float f7 = lerp(f, f2, f6);
        float f8 = lerp(f1, f2, f6);
        float f9 = lerp(f3, f5, f6);
        float f10 = lerp(f4, f5, f6);
        float f11 = 1.0F;
        float f12 = 0.3F;
        float f13 = (float) (dh.vrPlayer.vrdata_world_render.getHeadPivot().y
                - ((GameRendererExtension) mc.gameRenderer).getRveY());

        for (int i = 0; i < 4; ++i) {
            posestack.pushPose();
            float ang = toRadians(90.0F * i - dh.vrPlayer.vrdata_world_render.getBodyYaw());
            posestack.last().normal().rotateY(ang);
            Matrix4f matrix4f = posestack.last().pose().rotateY(ang).translate(0.0F, -f13, 0.0F);
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -f12, 0.0F, -f12).uv(f8, f10).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, f12, 0.0F, -f12).uv(f7, f10).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, f12, f13, -f12).uv(f7, f9).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -f12, f13, -f12).uv(f8, f9).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            posestack.popPose();
        }

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public void applystereo(RenderPass currentPass, PoseStack matrix)
    {
        if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
            Vec3 vec3 = dh.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition()
                    .subtract(dh.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER)
                            .getPosition());
            matrix.last().pose().translate((float) -vec3.x, (float) -vec3.y, (float) -vec3.z);
        }
    }

    @Override
    public void restoreRVEPos(LivingEntity e)
    {
        if (e != null) {
            e.setPosRaw(this.rveX, this.rveY, this.rveZ);
            e.xOld = this.rvelastX;
            e.yOld = this.rvelastY;
            e.zOld = this.rvelastZ;
            e.xo = this.rveprevX;
            e.yo = this.rveprevY;
            e.zo = this.rveprevZ;
            e.setYRot(this.rveyaw);
            e.setXRot(this.rvepitch);
            e.yRotO = this.rvelastyaw;
            e.xRotO = this.rvelastpitch;
            e.yHeadRot = this.rveyaw;
            e.yHeadRotO = this.rvelastyaw;
            e.eyeHeight = this.rveHeight;
            this.cached = false;
        }
    }

    @Override
    public void DrawScopeFB(PoseStack matrixStackIn, int i)
    {
        if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
            //this.lightTexture.turnOffLightLayer();
            matrixStackIn.pushPose();
            RenderSystem.enableDepthTest();

            if (i == 0) {
                dh.vrRenderer.telescopeFramebufferR.bindRead();
                RenderSystem.setShaderTexture(0, dh.vrRenderer.telescopeFramebufferR.getColorTextureId());
            } else {
                dh.vrRenderer.telescopeFramebufferL.bindRead();
                RenderSystem.setShaderTexture(0, dh.vrRenderer.telescopeFramebufferL.getColorTextureId());
            }

            float scale = 0.0785F;
            //actual framebuffer
            float f = TelescopeTracker.viewPercent(i);
            // this.drawSizedQuad(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());
            this.drawSizedQuadSolid(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());

            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
            RenderSystem.enableBlend();
            matrixStackIn.last().pose().translate(0.0F, 0.0F, 0.00001F);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(dh.vrPlayer.vrdata_world_render.getController(i).getPosition()));
            this.drawSizedQuadWithLightmap(720.0F, 720.0F, scale, light, matrixStackIn.last().pose());

            matrixStackIn.popPose();
            this.lightTexture.turnOnLightLayer();
        }
    }
}
