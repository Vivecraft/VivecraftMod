package org.vivecraft.forge.mixin.network;

import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.PlayMessages.OpenContainer;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenContainer.class)
public class ForgeOpenContainerVRMixin {
    @Inject(at = @At("HEAD"), method = "handle", remap = false)
    private static void markScreenActiveForge(OpenContainer msg, Supplier<Context> ctx, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
