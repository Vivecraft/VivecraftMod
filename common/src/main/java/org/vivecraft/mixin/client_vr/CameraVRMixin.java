package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Mixin(Camera.class)
public abstract class CameraVRMixin {
    @Unique
    private static final float vivecraft$MIN_CLIP_DISTANCE = 0.02F;

    @Shadow
    @Final
    public Projection projection;

    @Shadow
    private Entity entity;

    @Shadow
    private Vec3 position;

    @Shadow
    private float oldFovModifier;

    @Shadow
    private float fovModifier;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float depthFar;

    @Shadow
    private @Nullable Level level;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    protected abstract void setupPerspective(float zNear, float zFar, float fov, float width, float height);

    @Shadow
    protected abstract void alignWithEntity(float partialTicks);

    @Shadow
    private boolean detached;

    @ModifyExpressionValue(method = {"update", "createProjectionMatrixForCulling"}, at = @At(value = "CONSTANT", args = "floatValue=0.05F"))
    private float vivecraft$shorterNear(float original) {
        return RenderPassType.isVanilla() ? original : vivecraft$MIN_CLIP_DISTANCE;
    }

    @WrapOperation(method = "setupPerspective", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"))
    private void vivecraft$vrFov(
        Projection instance, float zNear, float zFar, float fov, float width, float height, Operation<Void> original)
    {
        if (!RenderPassType.isVanilla()) {
            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
            if (MethodHolder.isInMenuRoom()) {
                // use 16 Chunks as minimum, to have no issues with clipping in the menuworld
                zFar = Math.max(zFar, 1024.0F);
            }

            switch (dataHolder.currentPass) {
                case THIRD, CENTER -> {
                    if (dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                        height = width / dataHolder.vrSettings.mixedRealityAspectRatio;
                    } else {
                        if (ShadersHelper.needsSameSizeBuffers()) {
                            // in this case the default aspect is wrong, since it has the aspect of the vr view
                            WindowExtension window = (WindowExtension) (Object) this.minecraft.getWindow();
                            width = window.vivecraft$getActualScreenWidth();
                            height = window.vivecraft$getActualScreenHeight();
                        }
                    }
                }
                case CAMERA -> {
                    width = dataHolder.vrRenderer.cameraFramebuffer.width;
                    height = dataHolder.vrRenderer.cameraFramebuffer.height;
                }
                case SCOPEL, SCOPER -> width = height = 720;
            }

            fov = switch (dataHolder.currentPass) {
                case THIRD -> dataHolder.vrSettings.mixedRealityFov;
                case CAMERA -> dataHolder.vrSettings.handCameraFov;
                case SCOPEL, SCOPER -> (70F / 8F);
                default -> fov;
            };
        }
        original.call(instance, zNear, zFar, fov, width, height);
    }

    // RETURN instead of TAIL, because TAIL goes into the if check for some reason
    @Inject(method = "update", at = @At("RETURN"))
    private void vivecraft$alwaysSetupProjection(CallbackInfo ci, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        // we always need the perspecive projection and position set up, even outside levels
        if (!RenderPassType.isVanilla() && (this.entity == null || this.level == null)) {
            this.setupPerspective(vivecraft$MIN_CLIP_DISTANCE, this.depthFar,
                this.minecraft.options.fov().get(), this.minecraft.getWindow().getWidth(),
                this.minecraft.getWindow().getHeight());
            this.alignWithEntity(deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }

    @WrapOperation(method = "createProjectionMatrixForCulling", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;"))
    private Matrix4f vivecraft$vrCullingProjection(
        Matrix4f instance, float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne,
        Operation<Matrix4f> original)
    {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        if (RenderPassType.isVanilla() ||
            (dataHolder.currentPass != RenderPass.LEFT && dataHolder.currentPass != RenderPass.RIGHT))
        {
            return original.call(instance, fovy, aspect, zNear, zFar, zZeroToOne);
        } else {
            return instance.set(
                dataHolder.vrRenderer.getCachedProjectionMatrix(dataHolder.currentPass.ordinal(), zNear, zFar));
        }
    }

    @WrapOperation(method = {"getViewRotationProjectionMatrix", "extractRenderState"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;getMatrix(Lorg/joml/Matrix4f;)Lorg/joml/Matrix4f;"))
    private Matrix4f vivecraft$vrProjection(
        Projection instance, Matrix4f dest, Operation<Matrix4f> original)
    {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        if (RenderPassType.isVanilla() ||
            (dataHolder.currentPass != RenderPass.LEFT && dataHolder.currentPass != RenderPass.RIGHT))
        {
            original.call(instance, dest);
            if (VRState.VR_RUNNING && dataHolder.currentPass == RenderPass.THIRD) {
                ShaderHelper.THIRD_PASS_PROJECTION_MATRIX.set(dest);
            }
            return dest;
        } else {
            return dest.set(dataHolder.vrRenderer.getCachedProjectionMatrix(dataHolder.currentPass.ordinal(),
                this.projection.zNear(), this.projection.zFar()));
        }
    }

    @Inject(method = "alignWithEntity", at = @At("HEAD"), cancellable = true)
    private void vivecraft$setOrientation(float partialTicks, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
            RenderPass renderpass = dataholder.currentPass;

            VRData.VRDevicePose eye = dataholder.vrPlayer.getVRDataWorld().getEye(renderpass);
            this.setPosition(eye.getPosition());
            // we cannot set the rotation to the full matrix, because particles would rotate with the head
            // instead of being world up oriented
            this.setRotation(eye.getYaw(), -eye.getPitch());

            if (ClientDataHolderVR.getInstance().isFirstPass || ShadersHelper.isSlowMode()) {
                ShadersHelper.SHADOW_CAMERA_POSITION = this.position;
            }

            // no detaching in VR please
            this.detached = false;
            ci.cancel();
        }
    }

    @Inject(method = "getViewRotationMatrix", at = @At("HEAD"), cancellable = true)
    private void vivecraft$vrModelView(Matrix4f dest, CallbackInfoReturnable<Matrix4f> cir) {
        if (!RenderPassType.isVanilla()) {
            cir.setReturnValue(dest.set(RenderHelper.getVRModelView(ClientDataHolderVR.getInstance().currentPass)));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void vivecraft$vrTick(CallbackInfo ci) {
        // make sure the camera is at the head for tick
        if (!RenderPassType.isVanilla()) {
            this.setPosition(
                ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition());
        }
    }

    @Inject(method = "tickFov", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noFOVChangeInVR(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.oldFovModifier = this.fovModifier = 1.0f;
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "isDetached", at = @At("RETURN"))
    private boolean vivecraft$renderSelf(boolean isDetached) {
        if (RenderPassType.isVanilla()) {
            return isDetached;
        }
        // the detached state is used to check if the player should be rendered, we only want that in external passes
        boolean renderSelf = RenderPass.renderPlayer(ClientDataHolderVR.getInstance().currentPass);
        // don't render the player in first person when sleeping
        renderSelf &= !(RenderPass.isFirstPerson(ClientDataHolderVR.getInstance().currentPass) &&
            this.entity instanceof LivingEntity && ((LivingEntity) this.entity).isSleeping()
        );
        // isDetached is only true if some other mod has changed it since update
        return renderSelf || isDetached;
    }
}
