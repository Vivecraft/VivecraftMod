package org.vivecraft.mixin.client_vr.renderer.blockentity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(TheEndGatewayRenderer.class)
public class TheEndGatewayRendererVRMixin {
    @Inject(method = "renderType", at = @At("HEAD"), cancellable = true)
    private void vivecraft$VRShaderOverride(CallbackInfoReturnable<RenderType> cir) {
        if (!RenderPassType.isVanilla()) {
            cir.setReturnValue(VRRenderTypes.endGateWayVR());
        }
    }
}
