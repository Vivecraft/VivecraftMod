package org.vivecraft.mixin.client;

import net.minecraft.client.ResourceLoadStateTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(ResourceLoadStateTracker.class)
public abstract class ResourceLoadStateTrackerMixin {
    @Inject(at = @At("HEAD"), method = "startReload")
    void vivecraft$startReloadMixin(CallbackInfo ci) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null) {
            ClientDataHolderVR.getInstance().menuWorldRenderer.cancelBuilding();
        }
    }
}
