package org.vivecraft.mod_compat_vr.optifine.mixin;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;

@ClassDependentMixin("net.optifine.Config")
@Mixin(DefaultVertexFormat.class)
public class OptifineDefaultVertexFormatMixin {
    @Inject(method = "updateVertexFormats", at = @At("TAIL"))
    private static void vivecraft$rebuildMenuWorld(CallbackInfo ci) {
        if (VRState.VR_INITIALIZED && ClientDataHolderVR.getInstance().menuWorldRenderer != null &&
            ClientDataHolderVR.getInstance().menuWorldRenderer.getLevel() != null)
        {
            // rebuild menuworld, because optifine hard links the vertex formats to the terrain pipeline
            ClientDataHolderVR.getInstance().menuWorldRenderer.destroy();
            ClientDataHolderVR.getInstance().menuWorldRenderer.prepare();
        }
    }
}
