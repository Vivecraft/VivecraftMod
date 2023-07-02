package org.vivecraft.forge.mixin.network;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PlayMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

import java.util.function.Supplier;

@Mixin(PlayMessages.OpenContainer.class)
public class ForgeOpenContainerVRMixin {
    @Inject(at = @At("HEAD"), method = "handle", remap = false)
    private static void markScreenActiveForge(PlayMessages.OpenContainer msg, Supplier<NetworkEvent.Context> ctx, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
