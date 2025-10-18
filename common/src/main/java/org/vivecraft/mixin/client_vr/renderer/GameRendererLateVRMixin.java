package org.vivecraft.mixin.client_vr.renderer;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

// late to be after cinnabar
@Mixin(value = GameRenderer.class, priority = 1100)
public abstract class GameRendererLateVRMixin implements GameRendererExtension {

    @Unique
    private static final float vivecraft$MIN_CLIP_DISTANCE = 0.02F;
    @Unique
    private Matrix4f vivecraft$thirdPassProjectionMatrix = new Matrix4f();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Group(name = "projection", min = 1, max = 1)
    @WrapOperation(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;", remap = false), remap = true, require = 0, expect = 0)
    private Matrix4f vivecraft$customProjectionMatrixCinnabar(
        Matrix4f instance, float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne,
        Operation<Matrix4f> original)
    {
        return vivecraft$customProjectionMatrix(instance, fovy, aspect, zNear, zFar, zZeroToOne, original);
    }

    @Group(name = "projection", min = 1, max = 1)
    @WrapOperation(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;", remap = false), remap = true, require = 0, expect = 0)
    private Matrix4f vivecraft$customProjectionMatrix(
        Matrix4f instance, float fovy, float aspect, float zNear, float zFar, Operation<Matrix4f> original)
    {
        return vivecraft$customProjectionMatrix(instance, fovy, aspect, zNear, zFar, null, original);
    }

    @Unique
    private Matrix4f vivecraft$customProjectionMatrix(
        Matrix4f instance, float fovy, float aspect, float zNear, float zFar, Boolean zZeroToOne,
        Operation<Matrix4f> original)
    {
        if (!RenderPassType.isVanilla()) {
            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
            zNear = vivecraft$MIN_CLIP_DISTANCE;
            if (MethodHolder.isInMenuRoom()) {
                // use 16 Chunks as minimum, to have no issues with clipping in the menuworld
                zFar = Math.max(zFar, 1024.0F);
            }

            if (dataHolder.currentPass == RenderPass.LEFT ||
                dataHolder.currentPass == RenderPass.RIGHT)
            {
                return instance.mul(dataHolder.vrRenderer.getCachedProjectionMatrix(
                    dataHolder.currentPass.ordinal(), zNear, zFar, zZeroToOne != null && zZeroToOne));
            }

            aspect = switch (dataHolder.currentPass) {
                case THIRD, CENTER -> {
                    if (dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                        yield dataHolder.vrSettings.mixedRealityAspectRatio;
                    } else {
                        if (ShadersHelper.needsSameSizeBuffers()) {
                            // in this case the default aspect is wrong, since it has the aspect of the vr view
                            WindowExtension window = (WindowExtension) (Object) this.minecraft.getWindow();
                            yield (float) window.vivecraft$getActualScreenWidth() /
                                window.vivecraft$getActualScreenHeight();
                        } else {
                            yield aspect;
                        }
                    }
                }
                case CAMERA -> (float) dataHolder.vrRenderer.cameraFramebuffer.viewWidth /
                    (float) dataHolder.vrRenderer.cameraFramebuffer.viewHeight;
                case SCOPEL, SCOPER -> 1.0F;
                default -> aspect;
            };

            fovy = switch (dataHolder.currentPass) {
                case THIRD -> Mth.DEG_TO_RAD * dataHolder.vrSettings.mixedRealityFov;
                case CAMERA -> Mth.DEG_TO_RAD * dataHolder.vrSettings.handCameraFov;
                case SCOPEL, SCOPER -> Mth.DEG_TO_RAD * (70F / 8F);
                default -> fovy;
            };
        }

        Matrix4f proj = zZeroToOne == null ? original.call(instance, fovy, aspect, zNear, zFar) :
            original.call(instance, fovy, aspect, zNear, zFar, zZeroToOne);

        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().currentPass == RenderPass.THIRD) {
            this.vivecraft$thirdPassProjectionMatrix = proj;
        }
        return proj;
    }

    @Override
    @Unique
    public float vivecraft$getMinClipDistance() {
        return vivecraft$MIN_CLIP_DISTANCE;
    }

    @Override
    @Unique
    public Matrix4f vivecraft$getThirdPassProjectionMatrix() {
        return this.vivecraft$thirdPassProjectionMatrix;
    }
}
