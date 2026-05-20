package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

@Pseudo
@Mixin(targets = "net.optifine.shaders.Shaders")
public class ShadersVRMixin {

    @Shadow
    static double cameraPositionX;
    @Shadow
    static double cameraPositionY;
    @Shadow
    static double cameraPositionZ;

    @ModifyExpressionValue(method = "setProgramUniforms", at = @At(value = "CONSTANT", args = "floatValue=0.05F"))
    private static float vivecraft$nearPlane(float original) {
        return RenderPassType.isVanilla() ? original : 0.02F;
    }

    @WrapOperation(method = "setCameraShadow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;position()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 vivecraft$positionCameraForShadows(Camera camera, Operation<Vec3> original) {
        if (!VRState.VR_RUNNING || ShadersHelper.isSlowMode() || ClientDataHolderVR.getInstance().isFirstPass) {
            ShadersHelper.SHADOW_CAMERA_POSITION = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        }
        if (RenderPassType.isVanilla() || ShadersHelper.isSlowMode()) {
            return original.call(camera);
        } else {
            return ShadersHelper.SHADOW_CAMERA_POSITION;
        }
    }

    @ModifyVariable(method = "setCameraShadow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;", shift = At.Shift.AFTER))
    private static PoseStack vivecraft$offsetShadow(PoseStack shadowModelViewMat) {
        // always set the position, since all passes use the same targets
        ShadersHelper.setShadowCameraPosition(true, (float) cameraPositionX, (float) cameraPositionY,
            (float) cameraPositionZ);

        OptifineHelper.updateUniforms();

        if (!RenderPassType.isVanilla() && !ShadersHelper.isSlowMode()) {
            Vec3 offset = Minecraft.getInstance().gameRenderer.getMainCamera().position()
                .subtract(ShadersHelper.SHADOW_CAMERA_POSITION);
            shadowModelViewMat.translate((float) offset.x, (float) offset.y, (float) offset.z);
        }
        return shadowModelViewMat;
    }

    @WrapOperation(method = "setCameraOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
    private static double vivecraft$sameX(Entity entity, Operation<Double> original) {
        if (RenderPassType.isVanilla() || ShadersHelper.isSlowMode()) {
            return original.call(entity);
        } else {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().hmd.getPosition().x;
        }
    }

    @WrapOperation(method = "setCameraOffset", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
    private static double vivecraft$sameZ(Entity entity, Operation<Double> original) {
        if (RenderPassType.isVanilla() || ShadersHelper.isSlowMode()) {
            return original.call(entity);
        } else {
            return ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().hmd.getPosition().z;
        }
    }

    @Inject(method = {"beginClouds", "endClouds"}, at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noCloudsInMenu(CallbackInfo ci) {
        // don't render the clouds with shaders in the menu world
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.isRendering())
        {
            ci.cancel();
        }
    }

    @WrapOperation(method = "beginRender", at = @At(value = "FIELD", target = "Lnet/optifine/shaders/Shaders;frameCounter:I", opcode = Opcodes.PUTSTATIC, ordinal = 0))
    private static void vivecraft$onlyOneFrameIncrement(int frameCounter, Operation<Void> original) {
        if (!VRState.VR_RUNNING || ShadersHelper.isSlowMode() || ClientDataHolderVR.getInstance().isFirstPass) {
            original.call(frameCounter);
        }
    }

    @Inject(method = "beginRender", at = @At(value = "CONSTANT", args = "stringValue=beginRender"))
    private static void vivecraft$invalidateVivecraftUniforms(CallbackInfo ci) {
        OptifineHelper.UNIFORMS_UPDATED = false;
    }

    @Inject(method = "beginRender", at = @At("TAIL"))
    private static void vivecraft$updateVivecraftUniforms(CallbackInfo ci) {
        // calling it a second time, if it wasn't called in the shadow pass
        OptifineHelper.updateUniforms();
    }

    @Inject(method = "setProgramUniforms", at = @At("TAIL"))
    private static void vivecraft$setVivecraftUniforms(CallbackInfo ci) {
        OptifineHelper.setUniforms();
    }
}
