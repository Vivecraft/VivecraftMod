package org.vivecraft.mod_compat_vr.iris.mixin;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.compat.dh.LodRendererEvents$11")
public class IrisLodRenderEventsVRMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/compat/dh/IrisLodRenderProgram;fillUniformData(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;IF)V", ordinal = 1), method = "beforeRender", index = 0, remap = false)
    private Matrix4f vivecraft$correctProjectionCopySolid(Matrix4f dhProjection) {
        return vivecraft$correctProjection(dhProjection);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/compat/dh/IrisLodRenderProgram;fillUniformData(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;IF)V", ordinal = 2), method = "beforeRender", index = 0, remap = false)
    private Matrix4f vivecraft$correctProjectionCopyTranslucent(Matrix4f dhProjection) {
        return vivecraft$correctProjection(dhProjection);
    }

    @Unique
    private Matrix4f vivecraft$correctProjection(Matrix4f dhProjection) {
        if (!RenderPassType.isVanilla()) {
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT ||
                ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT) {
                // VR projections are not centered

                Matrix4f vrProjection = CapturedRenderingState.INSTANCE.getGbufferProjection();

                dhProjection.m20(vrProjection.m20());
                dhProjection.m21(vrProjection.m21());
            }
        }
        return dhProjection;
    }
}
