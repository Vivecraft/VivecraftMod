package org.vivecraft.mixin.client_vr.renderer;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;

// higher priority to apply before iris modelview alteration
@Mixin(value = GameRenderer.class, priority = 900)
public abstract class GameRendererVRMixin
    implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension
{

    @Unique
    private static final ClientDataHolderVR vivecraft$DATA_HOLDER = ClientDataHolderVR.getInstance();

    @Unique
    private double vivecraft$rveX;
    @Unique
    private double vivecraft$rveY;
    @Unique
    private double vivecraft$rveZ;
    @Unique
    private double vivecraft$rvelastX;
    @Unique
    private double vivecraft$rvelastY;
    @Unique
    private double vivecraft$rvelastZ;
    @Unique
    private double vivecraft$rveprevX;
    @Unique
    private double vivecraft$rveprevY;
    @Unique
    private double vivecraft$rveprevZ;
    @Unique
    private float vivecraft$rveyaw;
    @Unique
    private float vivecraft$rvepitch;
    @Unique
    private float vivecraft$rvelastyaw;
    @Unique
    private float vivecraft$rvelastpitch;
    @Unique
    private float vivecraft$rveHeight;
    @Unique
    private boolean vivecraft$cached;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    @Final
    private ProjectionMatrixBuffer levelProjectionMatrixBuffer;

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Inject(method = "resize", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private void vivecraft$restoreVanillaState(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED) {
            if (VRState.VR_RUNNING) {
                RenderPassManager.setGUIRenderPass();
            } else {
                RenderPassManager.setVanillaRenderPass();
            }
        }
    }

    @ModifyVariable(method = "resize", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int vivecraft$guiWidth(int width) {
        return VRState.VR_RUNNING ? GuiHandler.GUI_WIDTH : width;
    }

    @ModifyVariable(method = "resize", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int vivecraft$guiHeight(int height) {
        return VRState.VR_RUNNING ? GuiHandler.GUI_HEIGHT : height;
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void vivecraft$shouldDrawBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (!RenderPassType.isVanilla()) {
            switch (this.vivecraft$getVRRenderState().showOutline) {
                case NEVER -> cir.setReturnValue(false);
                case ALWAYS -> cir.setReturnValue(true);
                case null, default -> {}
            }
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
    private void vivecraft$renderFaceOverlay(
        GameRenderer instance, DeltaTracker deltaTracker, Operation<Void> original)
    {
        original.call(instance, deltaTracker);
        VRRenderState vrState = this.vivecraft$getVRRenderState();
        if (!RenderPassType.isVanilla() && vrState.currentPass != RenderPass.THIRD &&
            vrState.currentPass != RenderPass.CAMERA)
        {
            VREffectsHelper.renderFaceOverlay(this.submitNodeStorage, this.featureRenderDispatcher,
                this.gameRenderState.levelRenderState.cameraRenderState, vrState);
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"))
    private boolean vivecraft$noEffectInThird(boolean effectActive) {
        return effectActive && this.vivecraft$getVRRenderState().currentPass != RenderPass.THIRD;
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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"), cancellable = true)
    private void vivecraft$mainMenu(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!renderLevel && this.vivecraft$shouldDrawScreen) {
            return;
        }
        if (!renderLevel || this.minecraft.level == null || MethodHolder.isInMenuRoom()) {
            Profiler.get().push("MainMenu");
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            VREffectsHelper.renderMenuRoom(this.featureRenderDispatcher, this.submitNodeStorage,
                this.gameRenderState.levelRenderState);
            Profiler.get().pop();
        }
        // pop the "render" push, since we cancel early
        Profiler.get().pop();
        ci.cancel();
    }

    @ModifyArg(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V"), index = 1)
    private boolean vivecraft$renderGui(boolean shouldRenderLevel) {
        if (RenderPassType.isVanilla()) {
            return shouldRenderLevel;
        } else {
            if (!shouldRenderLevel) {
                // we still need the camera setup outside a level
                this.mainCamera.extractRenderState(this.gameRenderState.levelRenderState.cameraRenderState, 0);
            }
            return this.vivecraft$shouldDrawGui;
        }
    }

    @WrapWithCondition(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V"))
    private boolean vivecraft$noGUIWithViewOnly(
        GameRenderer instance, DeltaTracker deltaTracker, boolean shouldRenderLevel, boolean resourcesLoaded)
    {
        return RenderPassType.isVanilla() || (!vivecraft$DATA_HOLDER.viewOnly && this.vivecraft$shouldDrawScreen);
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void vivecraft$extractVRState(CallbackInfo ci, @Local(ordinal = 0) float partialTick) {
        if (VRState.VR_RUNNING) {
            vivecraft$getVRRenderState().extract(this.minecraft.player, partialTick);
        }
    }

    @Inject(method = "takeAutoScreenshot", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noScreenshotInMenu(CallbackInfo ci) {
        if (VRState.VR_RUNNING && MethodHolder.isInMenuRoom()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelBobHurt(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelBobView(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(FLorg/joml/Vector3fc;)Lorg/joml/Matrix4f;"), index = 0)
    private float vivecraft$reduceNauseaSpeed(float oldVal) {
        if (!RenderPassType.isVanilla()) {
            return oldVal * 0.2F;
        } else {
            return oldVal;
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float vivecraft$reduceNauseaAffect(float a, float b, Operation<Float> original) {
        if (!RenderPassType.isVanilla()) {
            // scales down the effect from (1,0.65) to (1,0.9)
            return original.call(a, b) * 0.4F;
        } else {
            return original.call(a, b);
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V"))
    private void vivecraft$undistortedProj(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            VRShaders.setUndistortedProj(this.gameRenderState.levelRenderState.cameraRenderState.projectionMatrix);
        }
    }

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
    private boolean vivecraft$noDepthClearInVR(CommandEncoder instance, GpuTexture gpuTexture, double clearDepth) {
        return RenderPassType.isVanilla();
    }


    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderScreenEffect(ZZFLnet/minecraft/client/renderer/SubmitNodeCollector;Z)V"))
    private boolean vivecraft$noScreenEffectsInVR(
        ScreenEffectRenderer instance, boolean isFirstPerson, boolean isSleeping, float partialTicks,
        SubmitNodeCollector submitNodeCollector, boolean hideGui)
    {
        return RenderPassType.isVanilla();
    }

    @WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;render3dCrosshair(Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V"))
    private boolean vivecraft$noDebugCrosshairInVR(
        DebugScreenOverlay instance, CameraRenderState cameraState, int guiScale)
    {
        return RenderPassType.isVanilla();
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noHandsInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void vivecraft$disableStencil(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            VREffectsHelper.disableStencilTest();
        }
    }

    @Override
    @Unique
    public void vivecraft$setupRVE() {
        this.vivecraft$setupRVEAtDevice(
            vivecraft$DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(vivecraft$DATA_HOLDER.currentPass));
    }

    @Override
    @Unique
    public void vivecraft$setupRVEAtDevice(VRData.VRDevicePose eyePose) {
        if (this.vivecraft$cached) {
            Vec3 eye = eyePose.getPosition();
            Entity entity = this.minecraft.getCameraEntity();
            entity.setPosRaw(eye.x, eye.y, eye.z);
            entity.xOld = eye.x;
            entity.yOld = eye.y;
            entity.zOld = eye.z;
            entity.xo = eye.x;
            entity.yo = eye.y;
            entity.zo = eye.z;
            entity.setXRot(-eyePose.getPitch());
            entity.xRotO = entity.getXRot();
            entity.setYRot(eyePose.getYaw());
            entity.yRotO = entity.getYRot();
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.yHeadRot = entity.getYRot();
                livingEntity.yHeadRotO = entity.getYRot();
            }
            // non 0 to fix some division by 0 issues
            entity.eyeHeight = 0.0001F;
        }
    }

    @Override
    @Unique
    public void vivecraft$cacheRVEPos(Entity entity) {
        if (this.minecraft.getCameraEntity() != null && !this.vivecraft$cached) {
            this.vivecraft$rveX = entity.getX();
            this.vivecraft$rveY = entity.getY();
            this.vivecraft$rveZ = entity.getZ();
            this.vivecraft$rvelastX = entity.xOld;
            this.vivecraft$rvelastY = entity.yOld;
            this.vivecraft$rvelastZ = entity.zOld;
            this.vivecraft$rveprevX = entity.xo;
            this.vivecraft$rveprevY = entity.yo;
            this.vivecraft$rveprevZ = entity.zo;
            this.vivecraft$rvepitch = entity.getXRot();
            this.vivecraft$rvelastpitch = entity.xRotO;
            this.vivecraft$rveHeight = entity.eyeHeight;
            if (entity instanceof LivingEntity livingEntity) {
                this.vivecraft$rveyaw = livingEntity.yHeadRot;
                this.vivecraft$rvelastyaw = livingEntity.yHeadRotO;
            } else {
                this.vivecraft$rveyaw = entity.getYRot();
                this.vivecraft$rvelastyaw = entity.yRotO;
            }
            this.vivecraft$cached = true;
        }
    }

    @Override
    @Unique
    public void vivecraft$restoreRVEPos(Entity entity) {
        if (entity != null) {
            entity.setPosRaw(this.vivecraft$rveX, this.vivecraft$rveY, this.vivecraft$rveZ);
            entity.xOld = this.vivecraft$rvelastX;
            entity.yOld = this.vivecraft$rvelastY;
            entity.zOld = this.vivecraft$rvelastZ;
            entity.xo = this.vivecraft$rveprevX;
            entity.yo = this.vivecraft$rveprevY;
            entity.zo = this.vivecraft$rveprevZ;
            entity.setXRot(this.vivecraft$rvepitch);
            entity.xRotO = this.vivecraft$rvelastpitch;
            entity.setYRot(this.vivecraft$rveyaw);
            entity.yRotO = this.vivecraft$rvelastyaw;
            entity.eyeHeight = this.vivecraft$rveHeight;
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.yHeadRot = this.vivecraft$rveyaw;
                livingEntity.yHeadRotO = this.vivecraft$rvelastyaw;
            }
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
    public Vec3 vivecraft$getRvePos() {
        return new Vec3(this.vivecraft$rveX, this.vivecraft$rveY, this.vivecraft$rveZ);
    }

    @Override
    @Unique
    public Vec3 vivecraft$getRvePos(float partialTick) {
        return new Vec3(
            Mth.lerp(partialTick, this.vivecraft$rvelastX, this.vivecraft$rveX),
            Mth.lerp(partialTick, this.vivecraft$rvelastY, this.vivecraft$rveY),
            Mth.lerp(partialTick, this.vivecraft$rvelastZ, this.vivecraft$rveZ)
        );
    }

    @Override
    @Unique
    public void vivecraft$resetProjectionMatrix(float partialTick) {
        RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(
            this.gameRenderState.levelRenderState.cameraRenderState.projectionMatrix), ProjectionType.PERSPECTIVE);
    }

    @Unique
    private VRRenderState vivecraft$getVRRenderState() {
        return ((LevelRenderStateExtension) this.gameRenderState.levelRenderState).vivecraft$getVRRenderState();
    }
}
