package org.vivecraft.mixin.client_vr.renderer.blockentity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.EndCubeSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(EndCubeSpecialRenderer.class)
public class EndCubeSpecialRendererVRMixin {
    @ModifyExpressionValue(method = "submit", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/special/EndCubeSpecialRenderer;renderType:Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType vivecraft$VRShaderOverride(RenderType renderType) {
        if (!RenderPassType.isVanilla()) {
            if (renderType == RenderTypes.endGateway()) {
                return VRRenderTypes.endGateWayVR();
            } else if (renderType == RenderTypes.endPortal()) {
                return VRRenderTypes.endPortalVR();
            }
        }
        return renderType;
    }
}
