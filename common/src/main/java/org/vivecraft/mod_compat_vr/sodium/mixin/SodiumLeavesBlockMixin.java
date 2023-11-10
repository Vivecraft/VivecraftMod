package org.vivecraft.mod_compat_vr.sodium.mixin;

import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;

// priority 1100 so we inject after sodium
@Mixin(value = LeavesBlock.class, priority = 1100)
public class SodiumLeavesBlockMixin {
    // fix menu world leaves rendering issue on sodium
    @Inject(method = "skipRendering(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
    void vivecraft$sodiumLeavesFix(CallbackInfoReturnable<Boolean> cir) {
        if (ClientDataHolderVR.getInstance().menuWorldRenderer != null && ClientDataHolderVR.getInstance().menuWorldRenderer.isOnBuilderThread()) {
            cir.setReturnValue(false);
        }
    }
}
