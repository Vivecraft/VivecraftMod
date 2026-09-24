package org.vivecraft.mod_compat_vr.mine_and_slash.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;

@Pseudo
@Mixin(targets = "com.robertx22.mine_and_slash.event_hooks.ontick.SmoothTeleportRender")
public class SmoothTeleportRenderVRMixin {
    @Inject(method = "offset", at = @At("HEAD"), cancellable = true)
    private static void vivecraft$noTPinVR(CallbackInfoReturnable<Vec3> cir, @Local(argsOnly = true) Player player) {
        if (VRState.VR_RUNNING && player == Minecraft.getInstance().player) {
            // no smooth teleport in VR, it doesn't work with the camera offset between passes
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}
