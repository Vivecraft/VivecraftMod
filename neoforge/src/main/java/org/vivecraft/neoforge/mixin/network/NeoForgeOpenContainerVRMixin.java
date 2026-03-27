package org.vivecraft.neoforge.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(targets = "net.neoforged.neoforge.client.network.ClientPayloadHandler")
public class NeoForgeOpenContainerVRMixin {
    @Inject(method = "handle(Lnet/neoforged/neoforge/network/payload/AdvancedOpenScreenPayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V", at = @At("HEAD"))
    private static void vivecraft$markScreenActiveNeoForge(CallbackInfo ci) {
        GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = true;
    }
}
