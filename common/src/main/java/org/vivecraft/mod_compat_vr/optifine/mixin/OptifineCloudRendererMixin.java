package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;

@ClassDependentMixin("net.optifine.Config")
@Mixin(CloudRenderer.class)
public class OptifineCloudRendererMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/optifine/Config;isShaders()Z"), remap = false)
    private boolean vivecraft$rebuildMenuWorld(boolean isShaders) {
        // don't render the clouds with shaders in the menu world
        return isShaders && (ClientDataHolderVR.getInstance().menuWorldRenderer == null ||
            !ClientDataHolderVR.getInstance().menuWorldRenderer.isRendering()
        );
    }
}
