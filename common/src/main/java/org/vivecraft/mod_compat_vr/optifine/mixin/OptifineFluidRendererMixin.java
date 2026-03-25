package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;

@ClassDependentMixin("net.optifine.Config")
@Mixin(FluidRenderer.class)
public class OptifineFluidRendererMixin {
    /**
     * menuworld fix
     */
    @ModifyExpressionValue(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/optifine/Config;isRenderRegions()Z"))
    private boolean vivecraft$optifineChunkClipping(boolean renderRegionsEnabled) {
        return renderRegionsEnabled && (ClientDataHolderVR.getInstance().menuWorldRenderer == null ||
            !ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()
        );
    }
}
