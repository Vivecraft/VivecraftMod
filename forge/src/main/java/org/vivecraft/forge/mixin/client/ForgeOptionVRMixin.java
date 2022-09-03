package org.vivecraft.forge.mixin.client;

import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;

@Mixin(Option.class)
public class ForgeOptionVRMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;allChanged()V", remap = true), method = "m_193661_", remap = false)
    private static void reinit(Options options, Option option, GraphicsStatus graphicsStatus, CallbackInfo ci) {
        ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("gfx setting change");
    }

}
