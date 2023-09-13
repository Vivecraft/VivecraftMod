package org.vivecraft.mixin.client_vr.renderer;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client.Xevents;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.XRCamera;
import org.vivecraft.client_vr.render.VRWidgetHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.nio.file.Path;
import java.util.Calendar;

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
    @Unique
    private int vivecraft$polyblendsrca;
    @Unique
    private int vivecraft$polyblenddsta;
    @Unique
    private int vivecraft$polyblendsrcrgb;
    @Unique
    private int vivecraft$polyblenddstrgb;
    @Unique
    private boolean vivecraft$polyblend;
    @Unique
    private boolean vivecraft$polytex;
    @Unique
    private boolean vivecraft$polylight;
    @Unique
    private boolean vivecraft$polycull;
    @Unique
    private Vec3i vivecraft$tpUnlimitedColor = new Vec3i(-83, -40, -26);
    @Unique
    private Vec3i vivecraft$tpLimitedColor = new Vec3i(-51, -87, -51);
    @Unique
    private Vec3i vivecraft$tpInvalidColor = new Vec3i(83, 83, 83);

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
    @Unique
    public double vivecraft$getRveY() {
        return vivecraft$rveY;
    }

    @Override
    @Unique
    public float vivecraft$inBlock() {
        return vivecraft$inBlock;
    }

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
    public void vivecraft$noFOVchangeInVR(CallbackInfo ci){
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
            posestack.mulPoseMatrix(new Matrix4f().setPerspective(70f / 8f * 0.01745329238474369F, 1.0F, 0.05F, this.vivecraft$clipDistance));

        } else {
            if (this.zoom != 1.0F) {
                posestack.translate((double) this.zoomX, (double) (-this.zoomY), 0.0D);
                posestack.scale(this.zoom, this.zoom, 1.0F);
            }
            posestack.mulPoseMatrix(new Matrix4f().setPerspective((float) d * 0.01745329238474369F, (float) this.minecraft.getWindow().getScreenWidth()
                    / (float) this.minecraft.getWindow().getScreenHeight(), 0.05F, this.vivecraft$clipDistance));
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

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void vivecraft$matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        this.resetProjectionMatrix(this.getProjectionMatrix(minecraft.options.fov().get()));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
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
            this.vivecraft$renderFaceOverlay(f, this.vivecraft$stack);
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
            return;
        }
        if (!renderWorldIn || this.minecraft.level == null || vivecraft$isInMenuRoom()) {
            this.minecraft.getProfiler().push("MainMenu");
            GL11.glDisable(GL11.GL_STENCIL_TEST);

            PoseStack pMatrixStack = new PoseStack();
            vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, pMatrixStack);
            this.vivecraft$renderGuiLayer(partialTicks, true, pMatrixStack);

            if (KeyboardHandler.Showing) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.vivecraft$renderPhysicalKeyboard(partialTicks, pMatrixStack);
                } else {
                    this.vivecraft$render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, vivecraft$DATA_HOLDER.vrSettings.menuAlwaysFollowFace && vivecraft$isInMenuRoom(), pMatrixStack);
                }
            }

            if ((vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                    || vivecraft$DATA_HOLDER.vrSettings.mixedRealityRenderHands)
                    && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
                this.vivecraft$renderVRHands(partialTicks, true, true, true, true, pMatrixStack);
            }
        }
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
    private void vivecraft$noScaleItem(PoseStack poseStack, float x, float y, float z) {
        if (RenderPassType.isVanilla()) {
            poseStack.scale(x, y, z);
        }
    }

    @Inject(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$transformItem(int i, int j, float f, CallbackInfo ci, int k, float g, float h, float l, float m, float n, float o, float p, PoseStack posestack) {
        if (!RenderPassType.isVanilla()) {
            float sinN = Mth.sin(n) * 0.5F;
            posestack.translate(0, 0, sinN - 1.0);
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.THIRD) {
                sinN *= ClientDataHolderVR.getInstance().vrSettings.mixedRealityFov / 70.0;
            }
            vivecraft$applyVRModelView(ClientDataHolderVR.getInstance().currentPass, posestack);
            vivecraft$applystereo(ClientDataHolderVR.getInstance().currentPass, posestack);
            posestack.scale(sinN, sinN, sinN);
            posestack.mulPose(Axis.YP.rotationDegrees(-ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolderVR.getInstance().currentPass).getYaw()));
            posestack.mulPose(Axis.XP.rotationDegrees(-ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(ClientDataHolderVR.getInstance().currentPass).getPitch()));
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void vivecraft$renderpick(GameRenderer g, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }

        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.LEFT) {
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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1), method = "renderLevel")
    public void vivecraft$noHandProfiler(ProfilerFiller instance, String s) {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        this.minecraft.getProfiler().popPush("ShadersEnd"); //TODO needed?
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean vivecraft$noHandsVR(GameRenderer instance) {
        return RenderPassType.isVanilla() && renderHand;
    }

    @Inject(at = @At(value = "TAIL", shift = Shift.BEFORE), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void vivecraft$restoreVE(float f, long j, PoseStack p, CallbackInfo i) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        this.vivecraft$restoreRVEPos((LivingEntity) this.minecraft.getCameraEntity());
    }

    @Unique
    private void vivecraft$setupOverlayStatus(float partialTicks) {
        this.vivecraft$inBlock = 0.0F;
        this.vivecraft$inwater = false;
        this.vivecraft$onfire = false;

        if (!this.minecraft.player.isSpectator() && !this.vivecraft$isInMenuRoom() && this.minecraft.player.isAlive()) {
            Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(vivecraft$DATA_HOLDER.currentPass).getPosition();
            Triple<Float, BlockState, BlockPos> triple = ((ItemInHandRendererExtension) this.itemInHandRenderer).vivecraft$getNearOpaqueBlock(vec3, (double) this.vivecraft$minClipDistance);

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

    @Unique
    private void vivecraft$renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack) {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(mainCamera, partialTicks, false)));
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, poseStack);
        vivecraft$SetupRenderingAtController(c, poseStack);

        if (this.minecraft.getOverlay() == null) {
            this.minecraft.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        }

        Tesselator tesselator = Tesselator.getInstance();

        if (depthAlways && c == 0) {
            RenderSystem.depthFunc(519);
        } else {
            RenderSystem.depthFunc(515);
        }

        Vec3i vec3i = new Vec3i(64, 64, 64);
        byte b0 = -1;
        Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 vec31 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c).getDirection();
        Vec3 vec32 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c)
                .getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        vec32 = new Vec3(0.0D, 1.0D, 0.0D);
        vec31 = new Vec3(0.0D, 0.0D, -1.0D);
        Vec3 vec33 = new Vec3(vec3.x - vec31.x * 0.18D, vec3.y - vec31.y * 0.18D, vec3.z - vec31.z * 0.18D);

        if (this.minecraft.level != null) {
            float f = (float) this.minecraft.level.getMaxLocalRawBrightness(
                    BlockPos.containing(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition()));

            int i = ShadersHelper.ShaderLight();

            if (f < (float) i) {
                f = (float) i;
            }

            float f1 = f / (float) this.minecraft.level.getMaxLightLevel();
            vec3i = new Vec3i(Mth.floor(vec3i.getX() * f1), Mth.floor(vec3i.getY() * f1),
                    Mth.floor(vec3i.getZ() * f1));
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        this.vivecraft$renderBox(tesselator, vec3, vec33, -0.02F, 0.02F, -0.0125F, 0.0125F, vec32, vec3i, b0, poseStack);
        BufferUploader.drawWithShader(tesselator.getBuilder().end());
        poseStack.popPose();
        RenderSystem.depthFunc(515);
    }

    @Unique
    private void vivecraft$renderVRHands(float partialTicks, boolean renderright, boolean renderleft, boolean menuhandright,
                                         boolean menuhandleft, PoseStack poseStack) {
        this.minecraft.getProfiler().push("hands");
        // backup projection matrix, not doing that breaks sodium water on 1.19.3
        RenderSystem.backupProjectionMatrix();

        if (renderright) {
            this.minecraft.getItemRenderer();
            ClientDataHolderVR.ismainhand = true;

            if (menuhandright) {
                this.vivecraft$renderMainMenuHand(0, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack = new PoseStack();
                posestack.last().pose().identity();
                this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, posestack);
                this.vivecraft$renderVRHand_Main(posestack, partialTicks);
            }

            this.minecraft.getItemRenderer();
            ClientDataHolderVR.ismainhand = false;
        }

        if (renderleft) {
            if (menuhandleft) {
                this.vivecraft$renderMainMenuHand(1, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack1 = new PoseStack();
                posestack1.last().pose().identity();
                this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, posestack1);
                this.vivecraft$renderVRHand_Offhand(partialTicks, true, posestack1);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        this.minecraft.getProfiler().pop();
    }

    @Override
    @Unique
    public boolean vivecraft$isInWater() {
        return vivecraft$inwater;
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
    public Vec3 vivecraft$getControllerRenderPos(int c) {
        ClientDataHolderVR dataholder = vivecraft$DATA_HOLDER;
        if (!dataholder.vrSettings.seated) {
            return dataholder.vrPlayer.vrdata_world_render.getController(c).getPosition();
        } else {
            Vec3 vec3;

            if (this.minecraft.getCameraEntity() != null && this.minecraft.level != null) {
                Vec3 vec32 = dataholder.vrPlayer.vrdata_world_render.hmd.getDirection();
                vec32 = vec32.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                vec32 = new Vec3(vec32.x, 0.0D, vec32.z);
                vec32 = vec32.normalize();
                RenderPass renderpass = RenderPass.CENTER;
                vec3 = dataholder.vrPlayer.vrdata_world_render.getEye(renderpass).getPosition().add(
                        vec32.x * 0.3D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale,
                        -0.4D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale,
                        vec32.z * 0.3D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale);

                if (TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
                    if (c == 0 && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND)
                        vec3 = dataholder.vrPlayer.vrdata_world_render.eye0.getPosition()
                                .add(dataholder.vrPlayer.vrdata_world_render.hmd.getDirection()
                                        .scale(0.2 * dataholder.vrPlayer.vrdata_world_render.worldScale));
                    if (c == 1 && minecraft.player.getUsedItemHand() == InteractionHand.OFF_HAND)
                        vec3 = dataholder.vrPlayer.vrdata_world_render.eye1.getPosition()
                                .add(dataholder.vrPlayer.vrdata_world_render.hmd.getDirection()
                                        .scale(0.2 * dataholder.vrPlayer.vrdata_world_render.worldScale));
                }

            } else {
                Vec3 vec31 = dataholder.vrPlayer.vrdata_world_render.hmd.getDirection();
                vec31 = vec31.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                vec31 = vec31.normalize();
                vec3 = dataholder.vrPlayer.vrdata_world_render.hmd.getPosition().add(vec31.x * 0.3D, -0.4D,
                        vec31.z * 0.3D);
            }

            return vec3;
        }
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
    public void vivecraft$applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
        Matrix4f modelView = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(currentPass)
                .getMatrix().transposed().toMCMatrix();
        poseStack.last().pose().mul(modelView);
        poseStack.last().normal().mul(new Matrix3f(modelView));
    }

    @Override
    @Unique
    public void vivecraft$renderDebugAxes(int r, int g, int b, float radius) {
        this.vivecraft$setupPolyRendering(true);
        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        this.vivecraft$renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
        this.vivecraft$renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
        this.vivecraft$renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
        this.vivecraft$renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
        this.vivecraft$setupPolyRendering(false);
    }

    @Unique
    public void vivecraft$renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        tesselator.getBuilder().vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();

        for (int i = 0; i < edges + 1; i++) {
            float f = (float) i / (float) edges * (float) Math.PI * 2.0F;

            if (side != 0 && side != 1) {
                if (side != 2 && side != 3) {
                    if (side == 4 || side == 5) {
                        float f5 = (float) pos.x;
                        float f7 = (float) pos.y + (float) Math.cos((double) f) * radius;
                        float f9 = (float) pos.z + (float) Math.sin((double) f) * radius;
                        tesselator.getBuilder().vertex((double) f5, (double) f7, (double) f9).color(r, g, b, a)
                                .endVertex();
                    }
                } else {
                    float f4 = (float) pos.x + (float) Math.cos((double) f) * radius;
                    float f6 = (float) pos.y + (float) Math.sin((double) f) * radius;
                    float f8 = (float) pos.z;
                    tesselator.getBuilder().vertex((double) f4, (double) f6, (double) f8).color(r, g, b, a).endVertex();
                }
            } else {
                float f1 = (float) pos.x + (float) Math.cos((double) f) * radius;
                float f2 = (float) pos.y;
                float f3 = (float) pos.z + (float) Math.sin((double) f) * radius;
                tesselator.getBuilder().vertex((double) f1, (double) f2, (double) f3).color(r, g, b, a).endVertex();
            }
        }

        tesselator.end();
    }

    @Unique
    private void vivecraft$setupPolyRendering(boolean enable) {
//		boolean flag = Config.isShaders(); TODO
        boolean flag = false;

        if (enable) {
            this.vivecraft$polyblendsrca = GlStateManager.BLEND.srcAlpha;
            this.vivecraft$polyblenddsta = GlStateManager.BLEND.dstAlpha;
            this.vivecraft$polyblendsrcrgb = GlStateManager.BLEND.srcRgb;
            this.vivecraft$polyblenddstrgb = GlStateManager.BLEND.dstRgb;
            this.vivecraft$polyblend = GL43C.glIsEnabled(GL11.GL_BLEND);
            this.vivecraft$polytex = true;
            this.vivecraft$polylight = false;
            this.vivecraft$polycull = true;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // GlStateManager._disableLighting();
            RenderSystem.disableCull();

            if (flag) {
//				this.prog = Shaders.activeProgram; TODO
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
            }
        } else {
            RenderSystem.blendFuncSeparate(this.vivecraft$polyblendsrcrgb, this.vivecraft$polyblenddstrgb, this.vivecraft$polyblendsrca,
                    this.vivecraft$polyblenddsta);

            if (!this.vivecraft$polyblend) {
                RenderSystem.disableBlend();
            }

            if (this.vivecraft$polytex) {
            }

            if (this.vivecraft$polylight) {
                // GlStateManager._enableLighting();
            }

            if (this.vivecraft$polycull) {
                RenderSystem.enableCull();
            }

//			if (flag && this.polytex) {
//				Shaders.useProgram(this.prog); TODO
//			}
        }
    }

    @Override
    @Unique
    public void vivecraft$drawScreen(float f, Screen screen, GuiGraphics guiGraphics) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        screen.render(guiGraphics, 0, 0, f);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        this.minecraft.getMainRenderTarget().bindRead();
        ((RenderTargetExtension) this.minecraft.getMainRenderTarget()).vivecraft$genMipMaps();
        this.minecraft.getMainRenderTarget().unbindRead();
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
    public boolean vivecraft$isInPortal() {
        return this.vivecraft$inportal;
    }

    @Override
    @Unique
    public Matrix4f vivecraft$getThirdPassProjectionMatrix() {
        return vivecraft$thirdPassProjectionMatrix;
    }

    @Unique
    private void vivecraft$renderVRHand_Main(PoseStack matrix, float partialTicks) {
        matrix.pushPose();
        this.vivecraft$SetupRenderingAtController(0, matrix);
        ItemStack itemstack = this.minecraft.player.getMainHandItem();
        ItemStack itemstack1 = null; // this.minecraft.physicalGuiManager.getHeldItemOverride();

        if (itemstack1 != null) {
            itemstack = itemstack1;
        }

        if (vivecraft$DATA_HOLDER.climbTracker.isClimbeyClimb() && itemstack.getItem() != Items.SHEARS) {
            itemstack = itemstack1 == null ? this.minecraft.player.getOffhandItem() : itemstack1;
        }

        if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
            int i = 0;

            if (vivecraft$DATA_HOLDER.vrSettings.reverseShootingEye) {
                i = 1;
            }

            ItemStack itemstack2 = this.minecraft.player.getProjectile(this.minecraft.player.getMainHandItem());

            if (itemstack2 != ItemStack.EMPTY && !vivecraft$DATA_HOLDER.bowTracker.isNotched()) {
                itemstack = itemstack2;
            } else {
                itemstack = ItemStack.EMPTY;
            }
        } else if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.OFF_HAND)
                && vivecraft$DATA_HOLDER.bowTracker.isNotched()) {
            int j = 0;

            if (vivecraft$DATA_HOLDER.vrSettings.reverseShootingEye) {
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
        (this.itemInHandRenderer).renderArmWithItem(this.minecraft.player, partialTicks,
                0.0F, InteractionHand.MAIN_HAND, this.minecraft.player.getAttackAnim(partialTicks), itemstack, 0.0F,
                matrix, multibuffersource$buffersource,
                this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks));
        multibuffersource$buffersource.endBatch();
        this.lightTexture.turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        matrix.popPose();

        matrix.popPose();
    }

    @Unique
    private void vivecraft$renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack matrix) {
        // boolean flag = Config.isShaders();TODO
        boolean flag = false;
        boolean flag1 = false;

//		if (flag) {
//			flag1 = Shaders.isShadowPass;
//		}

        matrix.pushPose();
        this.vivecraft$SetupRenderingAtController(1, matrix);
        ItemStack itemstack = this.minecraft.player.getOffhandItem();
        ItemStack itemstack1 = null;// this.minecraft.physicalGuiManager.getOffhandOverride();

        if (itemstack1 != null) {
            itemstack = itemstack1;
        }

        if (vivecraft$DATA_HOLDER.climbTracker.isClimbeyClimb()
                && (itemstack == null || itemstack.getItem() != Items.SHEARS)) {
            itemstack = this.minecraft.player.getMainHandItem();
        }

        if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
            int i = 1;

            if (vivecraft$DATA_HOLDER.vrSettings.reverseShootingEye) {
                i = 0;
            }

            itemstack = this.minecraft.player.getMainHandItem();
        }

		if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
		}
        matrix.pushPose();

        this.lightTexture.turnOnLightLayer();
        MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
        this.itemInHandRenderer.renderArmWithItem(this.minecraft.player, partialTicks,
                0.0F, InteractionHand.OFF_HAND, this.minecraft.player.getAttackAnim(partialTicks), itemstack, 0.0F,
                matrix, multibuffersource$buffersource,
                this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks));
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
            this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, matrix);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            if (ClientNetworking.isLimitedSurvivalTeleport() && !vivecraft$DATA_HOLDER.vrPlayer.getFreeMove()
                    && this.minecraft.gameMode.hasMissTime()
                    && vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming
                    && !vivecraft$DATA_HOLDER.bowTracker.isActive(this.minecraft.player)) {
                matrix.pushPose();
                this.vivecraft$SetupRenderingAtController(1, matrix);
                Vec3 vec3 = new Vec3(0.0D, 0.005D, 0.03D);
                float f1 = 0.03F;
                float f;

                if (vivecraft$DATA_HOLDER.teleportTracker.isAiming()) {
                    f = 2.0F * (float) ((double) vivecraft$DATA_HOLDER.teleportTracker.getTeleportEnergy()
                            - 4.0D * vivecraft$DATA_HOLDER.teleportTracker.movementTeleportDistance) / 100.0F * f1;
                } else {
                    f = 2.0F * vivecraft$DATA_HOLDER.teleportTracker.getTeleportEnergy() / 100.0F * f1;
                }

                if (f < 0.0F) {
                    f = 0.0F;
                }
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                this.minecraft.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                this.vivecraft$renderFlatQuad(vec3.add(0.0D, 0.05001D, 0.0D), f, f, 0.0F, this.vivecraft$tpLimitedColor.getX(),
                        this.vivecraft$tpLimitedColor.getY(), this.vivecraft$tpLimitedColor.getZ(), 128, matrix);
                this.vivecraft$renderFlatQuad(vec3.add(0.0D, 0.05D, 0.0D), f1, f1, 0.0F, this.vivecraft$tpLimitedColor.getX(),
                        this.vivecraft$tpLimitedColor.getY(), this.vivecraft$tpLimitedColor.getZ(), 50, matrix);
                matrix.popPose();
            }

            if (vivecraft$DATA_HOLDER.teleportTracker.isAiming()) {
                RenderSystem.enableDepthTest();

                if (vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming) {
                    this.vivecraft$renderTeleportArc(vivecraft$DATA_HOLDER.vrPlayer, matrix);
                }

            }

            RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

            matrix.popPose();
        }
    }

    @Unique
    private void vivecraft$render2D(float par1, RenderTarget framebuffer, Vec3 pos, org.vivecraft.common.utils.math.Matrix4f rot,
                            boolean depthAlways, PoseStack poseStack) {
        if (!vivecraft$DATA_HOLDER.bowTracker.isDrawing) {
            boolean flag = this.vivecraft$isInMenuRoom();
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, poseStack);
            Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                    .getEye(vivecraft$DATA_HOLDER.currentPass).getPosition();
            Vec3 vec31 = new Vec3(0.0D, 0.0D, 0.0D);
            float f = GuiHandler.guiScale;
            VRPlayer vrplayer = vivecraft$DATA_HOLDER.vrPlayer;
            Vec3 guipos = VRPlayer.room_to_world_pos(pos, vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render);
            org.vivecraft.common.utils.math.Matrix4f matrix4f = org.vivecraft.common.utils.math.Matrix4f
                    .rotationY(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guirot = org.vivecraft.common.utils.math.Matrix4f.multiply(matrix4f, rot);

            poseStack.translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z));
            poseStack.mulPoseMatrix(guirot.toMCMatrix());
            poseStack.translate((float) vec31.x, (float) vec31.y, (float) vec31.z);
            float f1 = f * vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(f1, f1, f1);

            framebuffer.bindRead();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, framebuffer.getColorTextureId());

            float[] color = new float[]{1, 1, 1, 1};
            if (!flag) {
                if (this.minecraft.screen == null) {
                    color[3] = vivecraft$DATA_HOLDER.vrSettings.hudOpacity;
                }

                if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
                    color[3] *= 0.75F;
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                        GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.disableBlend();
            }

            if (depthAlways) {
                RenderSystem.depthFunc(519);
            } else {
                RenderSystem.depthFunc(515);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();


            if (this.minecraft.level != null) {
                if (((ItemInHandRendererExtension) this.itemInHandRenderer).vivecraft$isInsideOpaqueBlock(vec3)) {
                    vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition();
                }

                int i = ShadersHelper.ShaderLight();
                int j = Utils.getCombinedLightWithMin(this.minecraft.level, BlockPos.containing(vec3), i);
                this.vivecraft$drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, j, color,
                        poseStack.last().pose());
            } else {
                this.vivecraft$drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color, poseStack.last().pose());
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.depthFunc(515);
            RenderSystem.enableCull();

            poseStack.popPose();
        }
    }

    @Unique
    private void vivecraft$renderPhysicalKeyboard(float partialTicks, PoseStack poseStack) {
        if (!vivecraft$DATA_HOLDER.bowTracker.isDrawing) {
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            // RenderSystem.enableRescaleNormal();
            // Lighting.setupFor3DItems();

            this.minecraft.getProfiler().push("applyPhysicalKeyboardModelView");
            Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                    .getEye(vivecraft$DATA_HOLDER.currentPass).getPosition();
            VRPlayer vrplayer = vivecraft$DATA_HOLDER.vrPlayer;
            Vec3 guipos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room,
                    vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render);
            org.vivecraft.common.utils.math.Matrix4f matrix4f = org.vivecraft.common.utils.math.Matrix4f
                    .rotationY(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guirot = org.vivecraft.common.utils.math.Matrix4f.multiply(matrix4f,
                    KeyboardHandler.Rotation_room);
            poseStack.mulPoseMatrix(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                    .getEye(vivecraft$DATA_HOLDER.currentPass).getMatrix().transposed().toMCMatrix());
            poseStack.translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z));
            // GlStateManager._multMatrix(guirot.transposed().toFloatBuffer());
            poseStack.mulPoseMatrix(guirot.toMCMatrix());
            float f = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
            poseStack.scale(f, f, f);
            this.minecraft.getProfiler().pop();

            KeyboardHandler.physicalKeyboard.render(poseStack);
            // Lighting.turnOff();
            // RenderSystem.disableRescaleNormal();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Unique
    private void vivecraft$renderGuiLayer(float par1, boolean depthAlways, PoseStack pMatrix) {
        if (!vivecraft$DATA_HOLDER.bowTracker.isDrawing) {
            if (this.minecraft.screen != null || !this.minecraft.options.hideGui) {
                if (!RadialHandler.isShowing()) {
                    minecraft.getProfiler().push("GuiLayer");
                    // cache fog distance
                    float fogStart = RenderSystem.getShaderFogStart();

                    // remove nausea effect from projection matrix, for vanilla, nd posestack for iris
                    this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
                    pMatrix.pushPose();
                    pMatrix.setIdentity();
                    this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, pMatrix);

                    boolean flag = this.vivecraft$isInMenuRoom();

                    // render the screen always on top in the menu room to prevent z fighting
                    depthAlways |= flag;

                    PoseStack poseStack = RenderSystem.getModelViewStack();
                    poseStack.pushPose();
                    poseStack.setIdentity();
                    RenderSystem.applyModelViewMatrix();

                    if (flag) {
                        pMatrix.pushPose();
                        Vec3 eye = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                                .getEye(vivecraft$DATA_HOLDER.currentPass).getPosition();
                        pMatrix.translate((vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.origin.x - eye.x),
                                (vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.origin.y - eye.y),
                                (vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.origin.z - eye.z));

                        // remove world rotation or the room doesn't align with the screen
                        pMatrix.mulPose(Axis.YN.rotation(-vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians));

                        //System.out.println(eye + " eye");
                        //System.out.println(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.origin + " world");

						if (vivecraft$DATA_HOLDER.menuWorldRenderer.isReady()) {
							try {
								this.vivecraft$renderTechjarsAwesomeMainMenuRoom(pMatrix);
							} catch (Exception exception) {
								System.out.println("Error rendering main menu world, unloading to prevent more errors");
								exception.printStackTrace();
								vivecraft$DATA_HOLDER.menuWorldRenderer.destroy();
							}
						} else {
                            this.vivecraft$renderJrbuddasAwesomeMainMenuRoomNew(pMatrix);
						}
                        pMatrix.popPose();
                    }

                    Vec3 vec31 = GuiHandler.applyGUIModelView(vivecraft$DATA_HOLDER.currentPass, pMatrix);
                    GuiHandler.guiFramebuffer.bindRead();
                    RenderSystem.disableCull();
                    RenderSystem.setShaderTexture(0, GuiHandler.guiFramebuffer.getColorTextureId());

                    float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
                    if (!flag) {
                        if (this.minecraft.screen == null) {
                            color[3] = vivecraft$DATA_HOLDER.vrSettings.hudOpacity;
                        } else {
                            // disable fog for menus
                            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
                        }

                        if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
                            color[3] *= 0.75F;
                        }

                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DestFactor.ONE);
                        if (vivecraft$DATA_HOLDER.vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID && ShadersHelper.isShaderActive()) {
                            RenderSystem.disableBlend();
                        }
                    } else {
                        // enable blend for overlay transition in menuworld to not be jarring
                        RenderSystem.enableBlend();
                    }

                    if (depthAlways) {
                        RenderSystem.depthFunc(519);
                    } else {
                        RenderSystem.depthFunc(515);
                    }

                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();

                    // RenderSystem.disableLighting();

                    if (this.minecraft.level != null) {
                        if (((ItemInHandRendererExtension) this.itemInHandRenderer).vivecraft$isInsideOpaqueBlock(vec31)) {
                            vec31 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition();
                        }

                        int i = ShadersHelper.ShaderLight();
                        int j = Utils.getCombinedLightWithMin(this.minecraft.level, BlockPos.containing(vec31), i);
                        this.vivecraft$drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
                                (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, j, color,
                                pMatrix.last().pose());
                    } else {
                        this.vivecraft$drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
                                (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color,
                                pMatrix.last().pose());
                    }

                    // RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
                    // reset fog
                    RenderSystem.setShaderFogStart(fogStart);
                    RenderSystem.depthFunc(515);
                    RenderSystem.enableDepthTest();
                    // RenderSystem.defaultAlphaFunc();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableCull();
                    pMatrix.popPose();

                    poseStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                    minecraft.getProfiler().pop();
                }
            }
        }
    }

    @Unique
    public void vivecraft$SetupRenderingAtController(int controller, PoseStack matrix) {
        Vec3 vec3 = this.vivecraft$getControllerRenderPos(controller);
        vec3 = vec3.subtract(vivecraft$DATA_HOLDER.vrPlayer.getVRDataWorld()
                .getEye(vivecraft$DATA_HOLDER.currentPass).getPosition());
        matrix.translate((double) ((float) vec3.x), (double) ((float) vec3.y), (double) ((float) vec3.z));
        float sc = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
        if (minecraft.level != null && TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
            matrix.mulPoseMatrix(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getMatrix().inverted()
                    .transposed().toMCMatrix());
            MethodHolder.rotateDegXp(matrix, 90);
            matrix.translate(controller == 0 ? 0.075 * sc : -0.075 * sc, -0.025 * sc, 0.0325 * sc);
        } else {
            matrix.mulPoseMatrix(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(controller)
                    .getMatrix().inverted().transposed().toMCMatrix());
        }

        matrix.scale(sc, sc, sc);

    }

    @Unique
    private void vivecraft$renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a, PoseStack poseStack) {
        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        Vec3 vec3 = (new Vec3((double) (-width / 2.0F), 0.0D, (double) (height / 2.0F)))
                .yRot((float) Math.toRadians((double) (-yaw)));
        Vec3 vec31 = (new Vec3((double) (-width / 2.0F), 0.0D, (double) (-height / 2.0F)))
                .yRot((float) Math.toRadians((double) (-yaw)));
        Vec3 vec32 = (new Vec3((double) (width / 2.0F), 0.0D, (double) (-height / 2.0F)))
                .yRot((float) Math.toRadians((double) (-yaw)));
        Vec3 vec33 = (new Vec3((double) (width / 2.0F), 0.0D, (double) (height / 2.0F)))
                .yRot((float) Math.toRadians((double) (-yaw)));
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

    @Unique
    private void vivecraft$renderBox(Tesselator tes, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY,
                                     Vec3 up, Vec3i color, byte alpha, PoseStack poseStack) {
        Vec3 vec3 = start.subtract(end).normalize();
        Vec3 vec31 = vec3.cross(up);
        up = vec31.cross(vec3);
        Vec3 vec32 = new Vec3(vec31.x * (double) minX, vec31.y * (double) minX, vec31.z * (double) minX);
        vec31 = vec31.scale((double) maxX);
        Vec3 vec33 = new Vec3(up.x * (double) minY, up.y * (double) minY, up.z * (double) minY);
        up = up.scale((double) maxY);
        org.vivecraft.common.utils.lwjgl.Vector3f vector3f = Utils.convertToVector3f(vec3);
        org.vivecraft.common.utils.lwjgl.Vector3f vector3f1 = Utils.convertToVector3f(up.normalize());
        org.vivecraft.common.utils.lwjgl.Vector3f vector3f2 = Utils.convertToVector3f(vec31.normalize());
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

    @Unique
    private void vivecraft$renderJrbuddasAwesomeMainMenuRoomNew(PoseStack pMatrixStack) {
        int i = 4;
        float f = 2.5F;
        float f1 = 1.3F;
        Vector2f afloat = vivecraft$DATA_HOLDER.vr.getPlayAreaSize();
        if (afloat == null)
            afloat = new Vector2f(2, 2);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(519);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        pMatrixStack.pushPose();
        float f2 = afloat.x + f1;
        float f3 = afloat.y + f1;
        pMatrixStack.translate(-f2 / 2.0F, 0.0F, -f3 / 2.0F);

        Matrix4f matrix4f = pMatrixStack.last().pose();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float r, g, b, a;
        r = g = b = 0.8f;
        a = 1.0f;

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(0, i * f3).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f2, i * f3).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(i * f2, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, f, f3).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f3).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, 0).uv(i * f2, i * f3).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f2, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, f, f3).uv(i * f3, i * f).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(i * f3, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f3, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f3, i * f).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, 0).uv(0, i * f).color(r, g, b, a).normal(-1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(i * f2, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, 0).uv(i * f2, i * f).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f).color(r, g, b, a).normal(0, 0, 1).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, 0, f, f3).uv(0, i * f).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f2, i * f).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f2, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        pMatrixStack.popPose();

    }

    @Unique
    private void vivecraft$renderTechjarsAwesomeMainMenuRoom(PoseStack poseStack) {
        RenderSystem.setShaderColor(1f,1f,1f,1f);

        RenderSystem.enableDepthTest();
        //RenderSystem.enableTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableCull();

        poseStack.pushPose();

        int tzOffset = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
        vivecraft$DATA_HOLDER.menuWorldRenderer.time = vivecraft$DATA_HOLDER.menuWorldRenderer.fastTime
            ? (long)(vivecraft$DATA_HOLDER.menuWorldRenderer.ticks * 10L + 10 * minecraft.getFrameTime())
            : (long)((System.currentTimeMillis() + tzOffset - 21600000) / 86400000D * 24000D);

        vivecraft$DATA_HOLDER.menuWorldRenderer.fogRenderer.setupFogColor();
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        vivecraft$DATA_HOLDER.menuWorldRenderer.updateLightmap();
        vivecraft$DATA_HOLDER.menuWorldRenderer.render(poseStack);

        Vector2f area = vivecraft$DATA_HOLDER.vr.getPlayAreaSize();
        if (area != null) {
            poseStack.pushPose();
            float width = area.x;//(float)Math.ceil(area.x);
            float length = area.y;//(float)Math.ceil(area.y);

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
            float sun = vivecraft$DATA_HOLDER.menuWorldRenderer.getSkyDarken();
            RenderSystem.setShaderColor(sun, sun, sun, 0.3f);


            RenderSystem.defaultBlendFunc();
            RenderSystem.enableBlend();
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            poseStack.translate(-width / 2.0F, 0.0F, -length / 2.0F);
            bufferbuilder
                .vertex(matrix4f, 0, 0.005f, 0)
                .uv(0, 0)
                .color(1f,1f,1f,1f)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, 0, 0.005f, length)
                .uv(0, 4 * length)
                .color(1f,1f,1f,1f)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005f, length)
                .uv(4 * width, 4 * length)
                .color(1f,1f,1f,1f)
                .normal(0, 1, 0).endVertex();
            bufferbuilder
                .vertex(matrix4f, width, 0.005f, 0)
                .uv(4 * width, 0)
                .color(1f,1f,1f,1f)
                .normal(0, 1, 0).endVertex();

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    @Unique
    public void vivecraft$renderVRFabulous(float partialTicks, LevelRenderer worldrendererin, boolean menuhandright,
                                           boolean menuhandleft, PoseStack pMatrix) {
        if (ClientDataHolderVR.getInstance().currentPass == RenderPass.SCOPEL || ClientDataHolderVR.getInstance().currentPass == RenderPass.SCOPER)
            return;
        this.minecraft.getProfiler().popPush("VR");
        this.vivecraft$renderCrosshairAtDepth(!ClientDataHolderVR.getInstance().vrSettings.useCrosshairOcclusion, pMatrix);
        this.minecraft.getMainRenderTarget().unbindWrite();
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVROccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVROccludedFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVROccludedFramebuffer().bindWrite(true);

        if (this.vivecraft$shouldOccludeGui()) {
            this.vivecraft$renderGuiLayer(partialTicks, false, pMatrix);
            this.vivecraft$renderVrShadow(partialTicks, false, pMatrix);

            if (KeyboardHandler.Showing) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.vivecraft$renderPhysicalKeyboard(partialTicks, pMatrix);
                } else {
                    this.vivecraft$render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.vivecraft$render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, pMatrix);
            }
        }

        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVRUnoccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

        if (!this.vivecraft$shouldOccludeGui()) {
            this.vivecraft$renderGuiLayer(partialTicks, false, pMatrix);
            this.vivecraft$renderVrShadow(partialTicks, false, pMatrix);

            if (KeyboardHandler.Showing) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.vivecraft$renderPhysicalKeyboard(partialTicks, pMatrix);
                } else {
                    this.vivecraft$render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.vivecraft$render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, pMatrix);
            }
        }

        this.vivecraft$renderVRSelfEffects(partialTicks);
        VRWidgetHelper.renderVRThirdPersonCamWidget();
        VRWidgetHelper.renderVRHandheldCameraWidget();
        boolean flag = this.vivecraft$shouldRenderHands();
        this.vivecraft$renderVRHands(partialTicks, flag && menuhandright, flag && menuhandleft, true, true, pMatrix);
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVRHandsFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVRHandsFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).vivecraft$getAlphaSortVRHandsFramebuffer().bindWrite(true);
        this.vivecraft$renderVRHands(partialTicks, flag && !menuhandright, flag && !menuhandleft, false, false, pMatrix);
        RenderSystem.defaultBlendFunc();
        // RenderSystem.defaultAlphaFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // Lighting.turnBackOn();
        // Lighting.turnOff();
        this.minecraft.getMainRenderTarget().bindWrite(true);
    }

    @Override
    @Unique
    public void vivecraft$renderVrFast(float partialTicks, boolean secondpass, boolean menuright, boolean menuleft,
                                       PoseStack pMatrix) {
        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.SCOPEL
                || vivecraft$DATA_HOLDER.currentPass == RenderPass.SCOPER)
            return;
        this.minecraft.getProfiler().popPush("VR");
        this.lightTexture.turnOffLightLayer();

        if (secondpass) {
            this.vivecraft$renderVrShadow(partialTicks, !this.vivecraft$shouldOccludeGui(), pMatrix);
        }

        if (!secondpass) {
            this.vivecraft$renderCrosshairAtDepth(!vivecraft$DATA_HOLDER.vrSettings.useCrosshairOcclusion, pMatrix);
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRThirdPersonCamWidget();
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRHandheldCameraWidget();
        }

        if (secondpass && (Minecraft.getInstance().screen != null || !KeyboardHandler.Showing)) {
            this.vivecraft$renderGuiLayer(partialTicks, !this.vivecraft$shouldOccludeGui(), pMatrix);
        }

        if (secondpass && KeyboardHandler.Showing) {
            if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                this.vivecraft$renderPhysicalKeyboard(partialTicks, pMatrix);
            } else {
                this.vivecraft$render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room, !this.vivecraft$shouldOccludeGui(), pMatrix);
            }
        }

        if (secondpass && RadialHandler.isShowing()) {
            this.vivecraft$render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
                    !this.vivecraft$shouldOccludeGui(), pMatrix);
        }
        // render hands in second pass when gui is open
        boolean renderHandsSecond = RadialHandler.isShowing() || KeyboardHandler.Showing || Minecraft.getInstance().screen != null;
        if (secondpass == renderHandsSecond) {
            // should render hands in second pass if menus are open, else in the first pass
            // only render the hands only once
            this.vivecraft$renderVRHands(partialTicks, this.vivecraft$shouldRenderHands(), this.vivecraft$shouldRenderHands(), menuright, menuleft,
                    pMatrix);
        }
        this.vivecraft$renderVRSelfEffects(partialTicks);
    }

    @Unique
    private void vivecraft$drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color) {
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

    @Unique
    private void vivecraft$drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
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

    @Unique
    private void vivecraft$drawSizedQuadSolid(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
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

    @Unique
    private void vivecraft$drawSizedQuad(float displayWidth, float displayHeight, float size) {
        this.vivecraft$drawSizedQuad(displayWidth, displayHeight, size, new float[]{1, 1, 1, 1});
    }

    @Unique
    private void vivecraft$drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
                                                     float[] color, Matrix4f pMatrix) {
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

    @Unique
    private void vivecraft$drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
                                                    Matrix4f pMatrix) {
        this.vivecraft$drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix);
    }

    @Unique
    private void vivecraft$renderTeleportArc(VRPlayer vrPlayer, PoseStack poseStack) {
        if (vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.showBeam
                && vivecraft$DATA_HOLDER.teleportTracker.isAiming()
                && vivecraft$DATA_HOLDER.teleportTracker.movementTeleportArcSteps > 1) {
            this.minecraft.getProfiler().push("teleportArc");
            // boolean flag = Config.isShaders();
            boolean flag = false;
            RenderSystem.enableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            this.minecraft.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
            Tesselator tesselator = Tesselator.getInstance();
            tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            double d0 = vivecraft$DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset;
            Vec3 vec3 = vivecraft$DATA_HOLDER.teleportTracker.getDestination();
            boolean flag1 = vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D;
            byte b0 = -1;
            Vec3i vec3i;

            if (!flag1) {
                vec3i = new Vec3i(83, 75, 83);
                b0 = -128;
            } else {
                if (ClientNetworking.isLimitedSurvivalTeleport() && !this.minecraft.player.getAbilities().mayfly) {
                    vec3i = this.vivecraft$tpLimitedColor;
                } else {
                    vec3i = this.vivecraft$tpUnlimitedColor;
                }

                d0 = vivecraft$DATA_HOLDER.vrRenderer.getCurrentTimeSecs()
                        * (double) vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
                vivecraft$DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset = d0;
            }

            float f = vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
            int i = vivecraft$DATA_HOLDER.teleportTracker.movementTeleportArcSteps - 1;

            if (vivecraft$DATA_HOLDER.teleportTracker.vrMovementStyle.beamGrow) {
                i = (int) ((double) i * vivecraft$DATA_HOLDER.teleportTracker.movementTeleportProgress);
            }

            double d1 = 1.0D / (double) i;
            Vec3 vec31 = new Vec3(0.0D, 1.0D, 0.0D);

            for (int j = 0; j < i; ++j) {
                double d2 = (double) j / (double) i + d0 * d1;
                int k = Mth.floor(d2);
                d2 = d2 - (double) ((float) k);
                Vec3 vec32 = vivecraft$DATA_HOLDER.teleportTracker
                        .getInterpolatedArcPosition((float) (d2 - d1 * (double) 0.4F))
                        .subtract(this.minecraft.getCameraEntity().position());
                Vec3 vec33 = vivecraft$DATA_HOLDER.teleportTracker.getInterpolatedArcPosition((float) d2)
                        .subtract(this.minecraft.getCameraEntity().position());
                float f2 = (float) d2 * 2.0F;
                this.vivecraft$renderBox(tesselator, vec32, vec33, -f, f, (-1.0F + f2) * f, (1.0F + f2) * f, vec31, vec3i, b0,
                        poseStack);
            }

            tesselator.end();
            RenderSystem.disableCull();

            if (flag1 && vivecraft$DATA_HOLDER.teleportTracker.movementTeleportProgress >= 1.0D) {
                Vec3 vec34 = (new Vec3(vec3.x, vec3.y, vec3.z)).subtract(this.minecraft.getCameraEntity().position());
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

                this.vivecraft$renderFlatQuad(vec34.add(d4, d5, d3), 0.6F, 0.6F, 0.0F, (int) ((double) vec3i.getX() * 1.03D),
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

                this.vivecraft$renderFlatQuad(vec34.add(d4, d5, d3), 0.4F, 0.4F, 0.0F, (int) ((double) vec3i.getX() * 1.04D),
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

                this.vivecraft$renderFlatQuad(vec34.add(d4, d5, d3), 0.2F, 0.2F, 0.0F, (int) ((double) vec3i.getX() * 1.05D),
                        (int) ((double) vec3i.getY() * 1.05D), (int) ((double) vec3i.getZ() * 1.05D), 64, poseStack);
            }

            this.minecraft.getProfiler().pop();
            RenderSystem.enableCull();
        }
    }

    @Override
    @Unique
    public void vivecraft$drawEyeStencil(boolean flag1) {

        if (vivecraft$DATA_HOLDER.currentPass != RenderPass.SCOPEL
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.SCOPER) {
            if ((vivecraft$DATA_HOLDER.currentPass == RenderPass.LEFT
                    || vivecraft$DATA_HOLDER.currentPass == RenderPass.RIGHT)
                    && vivecraft$DATA_HOLDER.vrSettings.vrUseStencil) {
//				net.optifine.shaders.Program program = Shaders.activeProgram;
//
//				if (shaders && Shaders.dfb != null) {
//					Shaders.dfb.bindFramebuffer();
//					Shaders.useProgram(Shaders.ProgramNone);
//
//					for (int i = 0; i < Shaders.usedDepthBuffers; ++i) {
//						GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(i));
//						this.minecraft.vrRenderer.doStencil(false);
//					}
//
//					Shaders.useProgram(program);
//				} else {
                vivecraft$DATA_HOLDER.vrRenderer.doStencil(false);
//				}
            } else {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        } else {
            // No stencil for telescope
            // GameRendererVRMixin.DATA_HOLDER.vrRenderer.doStencil(true);
        }
    }

    @Unique
    private void vivecraft$renderFaceOverlay(float par1, PoseStack pMatrix) {
//		boolean flag = Config.isShaders();
        boolean flag = false;

//		if (flag) { TODO
//			Shaders.beginFPOverlay();
//		}

        if (this.vivecraft$inBlock > 0.0F) {
            this.vivecraft$renderFaceInBlock();
            this.vivecraft$renderGuiLayer(par1, true, pMatrix);

            if (KeyboardHandler.Showing) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.vivecraft$renderPhysicalKeyboard(par1, pMatrix);
                } else {
                    this.vivecraft$render2D(par1, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, true, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.vivecraft$render2D(par1, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
                        true, pMatrix);
            }

            if (this.vivecraft$inBlock >= 1.0F) {
                this.vivecraft$renderVRHands(par1, true, true, true, true, pMatrix);
            }
        }

//		if (flag) { TODO
//			Shaders.endFPOverlay();
//		}
    }

    @Unique
    private void vivecraft$renderFaceInBlock() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, vivecraft$inBlock());

        // orthographic matrix, (-1, -1) to (1, 1), near = 0.0, far 2.0
        Matrix4f mat = new Matrix4f();
        mat.m00(1.0F);
        mat.m11(1.0F);
        mat.m22(-1.0F);
        mat.m33(1.0F);
        mat.m32(-1.0F);

        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Unique
    private boolean vivecraft$shouldRenderCrosshair() {
        if (ClientDataHolderVR.viewonly) {
            return false;
        } else if (this.minecraft.level == null) {
            return false;
        } else if (this.minecraft.screen != null) {
            return false;
        } else {
            boolean flag = vivecraft$DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.ALWAYS
                    || (vivecraft$DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.WITH_HUD
                    && !this.minecraft.options.hideGui);

            if (!flag) {
                return false;
            } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.THIRD) {
                return false;
            } else if (vivecraft$DATA_HOLDER.currentPass != RenderPass.SCOPEL
                    && vivecraft$DATA_HOLDER.currentPass != RenderPass.SCOPER) {
                if (vivecraft$DATA_HOLDER.currentPass == RenderPass.CAMERA) {
                    return false;
                } else if (KeyboardHandler.Showing) {
                    return false;
                } else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
                    return false;
                } else if (vivecraft$DATA_HOLDER.bowTracker.isNotched()) {
                    return false;
                } else if (!vivecraft$DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract)
                        .isEnabledRaw(ControllerType.RIGHT)
                        && !VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.RIGHT)) {
                    if (!vivecraft$DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab)
                            .isEnabledRaw(ControllerType.RIGHT)
                            && !VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT)) {
                        if (vivecraft$DATA_HOLDER.teleportTracker.isAiming()) {
                            return false;
                        } else if (vivecraft$DATA_HOLDER.climbTracker.isGrabbingLadder(0)) {
                            return false;
                        } else {
                            return !(vivecraft$DATA_HOLDER.vrPlayer.worldScale > 15.0F);
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

    @Unique
    private void vivecraft$renderCrosshairAtDepth(boolean depthAlways, PoseStack poseStack) {
        if (this.vivecraft$shouldRenderCrosshair()) {
            this.minecraft.getProfiler().push("crosshair");
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec3 vec3 = this.vivecraft$crossVec;
            Vec3 vec31 = vec3.subtract(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPosition());
            float f = (float) vec31.length();
            float f1 = (float) ((double) (0.125F * vivecraft$DATA_HOLDER.vrSettings.crosshairScale)
                    * Math.sqrt((double) vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale));
            vec3 = vec3.add(vec31.normalize().scale(-0.01D));
            poseStack.pushPose();
            poseStack.setIdentity();
            vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, poseStack);

            Vec3 vec32 = vec3.subtract(this.minecraft.getCameraEntity().position());
            poseStack.translate(vec32.x, vec32.y, vec32.z);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockhitresult = (BlockHitResult) this.minecraft.hitResult;

                if (blockhitresult.getDirection() == Direction.DOWN) {
                    MethodHolder.rotateDeg(poseStack,
                            vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
                            0.0F);
                    MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
                } else if (blockhitresult.getDirection() == Direction.EAST) {
                    MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
                } else if (blockhitresult.getDirection() != Direction.NORTH
                        && blockhitresult.getDirection() != Direction.SOUTH) {
                    if (blockhitresult.getDirection() == Direction.UP) {
                        MethodHolder.rotateDeg(poseStack,
                                -vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F,
                                1.0F, 0.0F);
                        MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
                    } else if (blockhitresult.getDirection() == Direction.WEST) {
                        MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
                    }
                }
            } else {
                MethodHolder.rotateDeg(poseStack,
                        -vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
                        0.0F);
                MethodHolder.rotateDeg(poseStack,
                        -vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPitch(), 1.0F, 0.0F,
                        0.0F);
            }

            if (vivecraft$DATA_HOLDER.vrSettings.crosshairScalesWithDistance) {
                float f5 = 0.3F + 0.2F * f;
                f1 *= f5;
            }

            this.lightTexture.turnOnLightLayer();
            poseStack.scale(f1, f1, f1);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            // RenderSystem.disableLighting();
            RenderSystem.disableCull();

            if (depthAlways) {
                RenderSystem.depthFunc(519);
            } else {
                RenderSystem.depthFunc(515);
            }

            // boolean flag = Config.isShaders();
            boolean flag = false;
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            int i = LevelRenderer.getLightColor(this.minecraft.level, BlockPos.containing(vec3));
            float f2 = 1.0F;

            if (this.minecraft.hitResult == null || this.minecraft.hitResult.getType() == HitResult.Type.MISS) {
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
            RenderSystem.enableCull();
            RenderSystem.depthFunc(515);
            poseStack.popPose();
            this.minecraft.getProfiler().pop();
        }
    }

    @Unique
    private boolean vivecraft$shouldOccludeGui() {
        Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(vivecraft$DATA_HOLDER.currentPass)
                .getPosition();

        if (vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
            return !this.vivecraft$isInMenuRoom() && this.minecraft.screen == null && !KeyboardHandler.Showing
                    && !RadialHandler.isShowing() && vivecraft$DATA_HOLDER.vrSettings.hudOcclusion
                    && !((ItemInHandRendererExtension) this.itemInHandRenderer).vivecraft$isInsideOpaqueBlock(vec3);
        } else {
            return true;
        }
    }

    @Unique
    private void vivecraft$renderVrShadow(float par1, boolean depthAlways, PoseStack poseStack) {
        if (vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
            if (this.minecraft.player.isAlive()) {
                if (!(((PlayerExtension) this.minecraft.player).vivecraft$getRoomYOffsetFromPose() < 0.0D)) {
                    if (this.minecraft.player.getVehicle() == null) {
                        this.minecraft.getProfiler().push("vr shadow");
                        AABB aabb = this.minecraft.player.getBoundingBox();

                        if (vivecraft$DATA_HOLDER.vrSettings.vrShowBlueCircleBuddy && aabb != null) {

                            poseStack.pushPose();
                            poseStack.setIdentity();
                            RenderSystem.disableCull();
                            this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, poseStack);
                            Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render
                                    .getEye(vivecraft$DATA_HOLDER.currentPass).getPosition();
                            LocalPlayer localplayer = this.minecraft.player;
                            Vec3 vec31 = new Vec3(this.vivecraft$rvelastX + (this.vivecraft$rveX - this.vivecraft$rvelastX) * (double) par1,
                                    this.vivecraft$rvelastY + (this.vivecraft$rveY - this.vivecraft$rvelastY) * (double) par1,
                                    this.vivecraft$rvelastZ + (this.vivecraft$rveZ - this.vivecraft$rvelastZ) * (double) par1);
                            Vec3 vec32 = vec31.subtract(vec3).add(0.0D, 0.005D, 0.0D);
                            this.vivecraft$setupPolyRendering(true);
                            RenderSystem.enableDepthTest();

                            if (depthAlways) {
                                RenderSystem.depthFunc(519);
                            } else {
                                RenderSystem.depthFunc(515);
                            }

                            RenderSystem.setShader(GameRenderer::getPositionColorShader);
                            this.minecraft.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                            this.vivecraft$renderFlatQuad(vec32, (float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ),
                                    0.0F, 0, 0, 0, 64, poseStack);
                            RenderSystem.depthFunc(515);
                            this.vivecraft$setupPolyRendering(false);
                            poseStack.popPose();
                            RenderSystem.enableCull();
                        }
                        this.minecraft.getProfiler().pop();
                    }
                }
            }
        }
    }

    @Unique
    private boolean vivecraft$shouldRenderHands() {
        if (vivecraft$DATA_HOLDER.viewonly) {
            return false;
        } else if (vivecraft$DATA_HOLDER.currentPass == RenderPass.THIRD) {
            return vivecraft$DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;
        } else {
            return vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA;
        }
    }

    @Unique
    private void vivecraft$renderVRSelfEffects(float par1) {
        if (this.vivecraft$onfire && vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD
                && vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA) {
            this.vivecraft$renderFireInFirstPerson();
        }
        this.renderItemActivationAnimation(0, 0, par1);
    }

    @Unique
    private void vivecraft$renderFireInFirstPerson() {
        PoseStack posestack = new PoseStack();
        this.vivecraft$applyVRModelView(vivecraft$DATA_HOLDER.currentPass, posestack);
        this.vivecraft$applystereo(vivecraft$DATA_HOLDER.currentPass, posestack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(519);

        if (vivecraft$DATA_HOLDER.currentPass == RenderPass.THIRD
                || vivecraft$DATA_HOLDER.currentPass == RenderPass.CAMERA) {
            RenderSystem.depthFunc(515);
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
        float f7 = Mth.lerp(f6, f, f2);
        float f8 = Mth.lerp(f6, f1, f2);
        float f9 = Mth.lerp(f6, f3, f5);
        float f10 = Mth.lerp(f6, f4, f5);
        float f11 = 1.0F;
        float f12 = 0.3F;
        float f13 = (float) (vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getHeadPivot().y
                - vivecraft$getRveY());

        for (int i = 0; i < 4; ++i) {
            posestack.pushPose();
            posestack.mulPose(Axis.YP.rotationDegrees(
                    (float) i * 90.0F - vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getBodyYaw()));
            posestack.translate(0.0D, (double) (-f13), 0.0D);
            Matrix4f matrix4f = posestack.last().pose();
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -f12, 0.0F, -f12).uv(f8, f10).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, f12, 0.0F, -f12).uv(f7, f10).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, f12, f13, -f12).uv(f7, f9).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -f12, f13, -f12).uv(f8, f9).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            posestack.popPose();
        }

        RenderSystem.depthFunc(515);
        RenderSystem.disableBlend();
    }

    @Unique
    private void vivecraft$applystereo(RenderPass currentPass, PoseStack matrix) {
        if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
            Vec3 vec3 = vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition()
                    .subtract(vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER)
                            .getPosition());
            matrix.translate((double) ((float) (-vec3.x)), (double) ((float) (-vec3.y)), (double) ((float) (-vec3.z)));
        }
    }

    @Override
    @Unique
    public void vivecraft$DrawScopeFB(PoseStack matrixStackIn, int i) {
        if (ClientDataHolderVR.getInstance().currentPass != RenderPass.SCOPEL && ClientDataHolderVR.getInstance().currentPass != RenderPass.SCOPER) {
            //this.lightTexture.turnOffLightLayer();
            matrixStackIn.pushPose();
            RenderSystem.enableDepthTest();

            if (i == 0) {
                ClientDataHolderVR.getInstance().vrRenderer.telescopeFramebufferR.bindRead();
                RenderSystem.setShaderTexture(0, ClientDataHolderVR.getInstance().vrRenderer.telescopeFramebufferR.getColorTextureId());
            } else {
                ClientDataHolderVR.getInstance().vrRenderer.telescopeFramebufferL.bindRead();
                RenderSystem.setShaderTexture(0, ClientDataHolderVR.getInstance().vrRenderer.telescopeFramebufferL.getColorTextureId());
            }

            float scale = 0.0785F;
            //actual framebuffer
            float f = TelescopeTracker.viewPercent(i);
            // this.drawSizedQuad(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());
            this.vivecraft$drawSizedQuadSolid(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());

            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
            RenderSystem.enableBlend();
            matrixStackIn.translate(0.0D, 0.0D, 0.00001D);
            int light = LevelRenderer.getLightColor(this.minecraft.level, BlockPos.containing(ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getController(i).getPosition()));
            this.vivecraft$drawSizedQuadWithLightmap(720.0F, 720.0F, scale, light, matrixStackIn.last().pose());

            matrixStackIn.popPose();
            this.lightTexture.turnOnLightLayer();
        }
    }
}