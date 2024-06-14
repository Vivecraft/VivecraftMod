package org.vivecraft.forge.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V"), method = "renderLevel")
    public void vivecraft$forgeCameraRotation(Camera instance, float yaw, float pitch, float roll) {
        if (RenderPassType.isVanilla() || ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT && ClientDataHolderVR.getInstance().currentPass != RenderPass.RIGHT) {
            instance.setRotation(yaw, pitch, roll);
        }
    }
}
