package org.vivecraft.mod_compat_vr.iris.mixin;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/compat/dh/IrisLodRenderProgram;fillUniformData(Lorg/joml/Matrix4fc;Lorg/joml/Matrix4fc;IF)V", ordinal = 1), method = "beforeRender", index = 0, remap = false)
    private Matrix4fc vivecraft$correctProjectionCopySolid(Matrix4fc dhProjection) {
        return vivecraft$correctProjection(dhProjection);
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/compat/dh/IrisLodRenderProgram;fillUniformData(Lorg/joml/Matrix4fc;Lorg/joml/Matrix4fc;IF)V", ordinal = 2), method = "beforeRender", index = 0, remap = false)
    private Matrix4fc vivecraft$correctProjectionCopyTranslucent(Matrix4fc dhProjection) {
        return vivecraft$correctProjection(dhProjection);
    }

    @Unique
    private Matrix4fc vivecraft$correctProjection(Matrix4fc dhProjection) {
        if (!RenderPassType.isVanilla()) {
            if (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT ||
                ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT) {
                // VR projections are not centered

                Matrix4fc vrProjection = CapturedRenderingState.INSTANCE.getGbufferProjection();
                Matrix4f dhProijectMutable = new Matrix4f(dhProjection);
                dhProijectMutable.m00(vrProjection.m00());
                dhProijectMutable.m11(vrProjection.m11());
                dhProijectMutable.m20(vrProjection.m20());
                dhProijectMutable.m21(vrProjection.m21());
                return dhProijectMutable;
            }
        }
        return dhProjection;
    }
}
