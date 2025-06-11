package org.vivecraft.forge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", remap = false), remap = true)
    private void vivecraft$removeAnglesInternal(
        Camera instance, float yaw, float pitch, float roll, Operation<Void> original)
    {
        if (RenderPassType.isVanilla() || (ClientDataHolderVR.getInstance().currentPass != RenderPass.LEFT &&
            ClientDataHolderVR.getInstance().currentPass != RenderPass.RIGHT
        ))
        {
            original.call(instance, yaw, pitch, roll);
        }
    }
}
