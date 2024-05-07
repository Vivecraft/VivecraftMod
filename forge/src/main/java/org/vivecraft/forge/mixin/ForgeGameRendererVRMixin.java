package org.vivecraft.forge.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false), method = "renderLevel")
    public void vivecraft$forgeInternal(Camera camera, float yaw, float pitch) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT && ClientDataHolderVR.getInstance().currentPass != RenderPass.RIGHT) {
            camera.setAnglesInternal(yaw, pitch);
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationXYZ(FFF)Lorg/joml/Matrix4f;", remap = false), method = "renderLevel", index = 0, remap = true)
    public float vivecraft$nullifyXRotation(float xRot) {
        return RenderPassType.isVanilla() ? xRot : 0F;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationXYZ(FFF)Lorg/joml/Matrix4f;", remap = false), method = "renderLevel", index = 1, remap = true)
    public float vivecraft$nullifyYRotation(float yRot) {
        return RenderPassType.isVanilla() ? yRot : 0F;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationXYZ(FFF)Lorg/joml/Matrix4f;", remap = false), method = "renderLevel", index = 2, remap = true)
    public float vivecraft$nullifyZRotation(float zRot) {
        return RenderPassType.isVanilla() ? zRot : 0F;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"), method = "renderLevel", index = 1)
    public Matrix4f vivecraft$applyModelView(Matrix4f matrix) {
        if (!RenderPassType.isVanilla()) {
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass, matrix);
        }
        return matrix;
    }
}
