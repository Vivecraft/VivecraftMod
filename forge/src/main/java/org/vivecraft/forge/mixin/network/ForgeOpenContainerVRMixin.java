package org.vivecraft.forge.mixin.network;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.packets.OpenContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(OpenContainer.class)
public class ForgeOpenContainerVRMixin {
    @Inject(at = @At("HEAD"), method = "handle", remap = false)
    private static void markScreenActiveForge(OpenContainer msg, CustomPayloadEvent.Context ctx, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
