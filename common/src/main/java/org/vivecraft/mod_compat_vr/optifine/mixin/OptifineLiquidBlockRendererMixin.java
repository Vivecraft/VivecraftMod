package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(LiquidBlockRenderer.class)
public class OptifineLiquidBlockRendererMixin {
    /**
     * menuworld fix
     */
    @ModifyExpressionValue(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/optifine/Config;isRenderRegions()Z", remap = false), remap = true)
    private boolean vivecraft$optifineChunkClipping(boolean renderRegionsEnabled) {
        return renderRegionsEnabled && (ClientDataHolderVR.getInstance().menuWorldRenderer == null ||
            !ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()
        );
    }
}
