package org.vivecraft.neoforge.mixin.network;

import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PlayMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(PlayMessages.OpenContainer.class)
public class NeoForgeOpenContainerVRMixin {
    @Inject(at = @At("HEAD"), method = "handle", remap = false)
    private static void vivecraft$markScreenActiveNeoForge(PlayMessages.OpenContainer msg, NetworkEvent.Context ctx, CallbackInfoReturnable<Boolean> cir) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
