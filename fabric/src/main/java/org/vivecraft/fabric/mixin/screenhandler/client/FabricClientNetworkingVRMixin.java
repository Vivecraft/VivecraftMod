package org.vivecraft.fabric.mixin.screenhandler.client;

import net.fabricmc.fabric.impl.screenhandler.client.ClientNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

@Mixin(ClientNetworking.class)
public class FabricClientNetworkingVRMixin {
    @Inject(at = @At("HEAD"), method = "openScreen", remap = false)
    private void markScreenActiveFabric(CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
