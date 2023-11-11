package org.vivecraft.mod_compat_vr.cataclysm.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.L_Ender.cataclysm.client.event.ClientEvent")
public class ClientEventMixin {
    @Inject(at = @At("HEAD"), method = "onGetFluidRenderType", remap = false, cancellable = true)
    private void vivecraft$fixNoPlayer(CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            ci.cancel();
        }
    }
}
