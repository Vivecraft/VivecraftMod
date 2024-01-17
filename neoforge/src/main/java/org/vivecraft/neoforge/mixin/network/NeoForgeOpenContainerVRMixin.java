package org.vivecraft.neoforge.mixin.network;

import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(ClientPayloadHandler.class)
public class NeoForgeOpenContainerVRMixin {
    @Inject(at = @At("HEAD"), method = "handle(Lnet/neoforged/neoforge/network/payload/AdvancedOpenScreenPayload;Lnet/neoforged/neoforge/network/handling/PlayPayloadContext;)V", remap = false)
    private void vivecraft$markScreenActiveNeoForge(AdvancedOpenScreenPayload msg, PlayPayloadContext context, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
