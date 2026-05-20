package org.vivecraft.mixin.client_vr.renderer.blockentity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(TheEndGatewayRenderer.class)
public class TheEndGatewayRendererVRMixin {
    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/EndGatewayRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;endGateway()Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType vivecraft$VRShaderOverride(RenderType renderType) {
        return RenderPassType.isVanilla() ? renderType : VRRenderTypes.endGateWayVR();
    }
}
