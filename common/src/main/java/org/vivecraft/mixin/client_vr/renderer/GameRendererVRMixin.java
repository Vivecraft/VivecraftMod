package org.vivecraft.mixin.client_vr.renderer;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
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
import org.vivecraft.Xevents;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.render.helpers.DebugRenderHelper;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.VRArmHelper;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
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
    private boolean vivecraft$inwater;
    @Unique
    private float vivecraft$inBlock = 0.0F;
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
            if (vivecraft$DATA_HOLDER.blockModule.isActive(0)) {
                // no block outline when the main arm has interaction
                cir.setReturnValue(false);
            } else if (vivecraft$DATA_HOLDER.teleportTracker.isAiming() ||
                vivecraft$DATA_HOLDER.vrSettings.renderBlockOutlineMode == VRSettings.RenderPointerElement.NEVER)
            {
                // don't render outline when aiming with tp, or the user disabled it
                cir.setReturnValue(false);
            } else if (vivecraft$DATA_HOLDER.vrSettings.renderBlockOutlineMode ==
                VRSettings.RenderPointerElement.ALWAYS)
            {
                // skip vanilla check and always render the outline
                cir.setReturnValue(true);
            }
            // VRSettings.RenderPointerElement.WITH_HUD uses the vanilla behaviour
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V"))
    private void vivecraft$renderFaceOverlay(
        GameRenderer instance, DeltaTracker deltaTracker, Operation<Void> original)
    {
        original.call(instance, deltaTracker);
        if (!RenderPassType.isVanilla() && vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD &&
            vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA)
        {
            VREffectsHelper.renderFaceOverlay(deltaTracker.getGameTimeDeltaPartialTick(false));
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"))
    private boolean vivecraft$noEffectInThird(boolean effectActive) {
        return effectActive && vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD;
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
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            Profiler.get().push("MainMenu");
            GL11.glDisable(GL11.GL_STENCIL_TEST);

            RenderSystem.getModelViewStack().pushMatrix().identity();
            RenderHelper.applyVRModelView(vivecraft$DATA_HOLDER.currentPass, RenderSystem.getModelViewStack());

            vivecraft$resetProjectionMatrix(partialTick);

            VREffectsHelper.renderGuiLayer(partialTick, true);

            DebugRenderHelper.renderDebug(partialTick);

            if (KeyboardHandler.SHOWING) {
                if (vivecraft$DATA_HOLDER.vrSettings.physicalKeyboard) {
                    VREffectsHelper.renderPhysicalKeyboard(partialTick);
                } else {
                    VREffectsHelper.render2D(partialTick, KeyboardHandler.FRAMEBUFFER, KeyboardHandler.POS_ROOM,
                        KeyboardHandler.ROTATION_ROOM,
                        vivecraft$DATA_HOLDER.vrSettings.menuAlwaysFollowFace && MethodHolder.isInMenuRoom());
                }
            }

            if (vivecraft$DATA_HOLDER.currentPass != RenderPass.CAMERA &&
                (vivecraft$DATA_HOLDER.currentPass != RenderPass.THIRD ||
                    vivecraft$DATA_HOLDER.vrSettings.mixedRealityRenderHands
                ))
            {
                VRArmHelper.renderVRHands(partialTick, true, true, true, true);
            }
            RenderSystem.getModelViewStack().popMatrix();
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

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float vivecraft$reduceNauseaAffect(float delta, float start, float end, Operation<Float> original) {
        if (!RenderPassType.isVanilla()) {
            // scales down the effect from (1,0.65) to (1,0.9)
            return original.call(delta, start, end) * 0.4F;
        } else {
            return original.call(delta, start, end);
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
    public void vivecraft$setupOverlayStatus() {
        this.vivecraft$inBlock = 0.0F;
        this.vivecraft$inwater = false;

        if (this.minecraft.player != null && !this.minecraft.player.isSpectator() && !MethodHolder.isInMenuRoom() &&
            this.minecraft.player.isAlive())
        {
            Vec3 cameraPos = vivecraft$DATA_HOLDER.vrPlayer.getVRDataWorld().getEye(vivecraft$DATA_HOLDER.currentPass)
                .getPosition();
            Triple<Float, BlockState, BlockPos> triple = VREffectsHelper.getNearOpaqueBlock(cameraPos,
                this.mainCamera.projection.zNear());

            if (triple != null &&
                !Xevents.INSTANCE.renderBlockOverlay(this.minecraft.player, new PoseStack(), triple.getMiddle(),
                    triple.getRight()))
            {
                this.vivecraft$inBlock = triple.getLeft();
            } else {
                this.vivecraft$inBlock = 0.0F;
            }

            this.vivecraft$inwater = this.minecraft.player.isEyeInFluid(FluidTags.WATER) &&
                !Xevents.INSTANCE.renderWaterOverlay(this.minecraft.player, new PoseStack());
        }
    }

    @Override
    @Unique
    public boolean vivecraft$isInWater() {
        return this.vivecraft$inwater;
    }

    @Override
    @Unique
    public float vivecraft$isInBlock() {
        return this.vivecraft$inBlock;
    }

    @Override
    @Unique
    public void vivecraft$resetProjectionMatrix(float partialTick) {
        RenderSystem.setProjectionMatrix(this.levelProjectionMatrixBuffer.getBuffer(
            this.gameRenderState.levelRenderState.cameraRenderState.projectionMatrix), ProjectionType.PERSPECTIVE);
    }
}
