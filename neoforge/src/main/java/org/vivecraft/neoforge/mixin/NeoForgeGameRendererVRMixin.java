package org.vivecraft.neoforge.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(value = GameRenderer.class)
public class NeoForgeGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V"), method = "renderLevel")
    public void vivecraft$neoforgeCameraRotation(Camera instance, float yaw, float pitch) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT && ClientDataHolderVR.getInstance().currentPass != RenderPass.RIGHT) {
            instance.setAnglesInternal(yaw, pitch);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationZ(F)Lorg/joml/Matrix4f;"), method = "renderLevel")
    private Matrix4f vivecraft$neoforgeCameraRoll(Matrix4f instance, float roll) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT && ClientDataHolderVR.getInstance().currentPass != RenderPass.RIGHT) {
            instance.rotationZ(roll);
        }
        return instance;
    }
}
