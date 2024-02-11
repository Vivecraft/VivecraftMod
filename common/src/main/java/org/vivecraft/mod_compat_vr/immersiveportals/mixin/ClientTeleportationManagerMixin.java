package org.vivecraft.mod_compat_vr.immersiveportals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;
import qouteall.imm_ptl.core.teleportation.TeleportationUtil;

@Mixin(ClientTeleportationManager.class)
public class ClientTeleportationManagerMixin {
    @Shadow
    private static long lastTeleportGameTime;

    @Shadow
    public static long tickTimeForTeleportation;

    @Inject(method = "teleportPlayer", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onlyOneTpPerFrame(TeleportationUtil.Teleportation teleportation, float partialTicks, CallbackInfo ci) {
        if (lastTeleportGameTime == tickTimeForTeleportation) {
            ci.cancel();
        }
    }
}
