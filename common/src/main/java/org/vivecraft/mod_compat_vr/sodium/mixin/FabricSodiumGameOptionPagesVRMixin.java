package org.vivecraft.mod_compat_vr.sodium.mixin;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Pseudo
@Mixin(targets = {
    "me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages",
    "net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages"
})
public class FabricSodiumGameOptionPagesVRMixin {

    @Inject(at = @At("HEAD"), method = "lambda$quality$23", remap = false)
    private static void vivecraft$initframe(Options opts, GraphicsStatus value, CallbackInfo ci) {
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
        }
    }
}
