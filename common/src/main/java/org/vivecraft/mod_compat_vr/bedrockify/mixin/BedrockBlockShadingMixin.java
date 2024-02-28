package org.vivecraft.mod_compat_vr.bedrockify.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;

@Pseudo
@Mixin(targets = "me.juancarloscp52.bedrockify.client.features.bedrockShading.BedrockBlockShading")
public class BedrockBlockShadingMixin {
    @Inject(at = @At("HEAD"), method = "getBlockShade", cancellable = true, remap = false)
    private void vivecraft$MenuNetherBlockShade(Direction direction, CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getInstance().player == null && direction == Direction.DOWN) {
            MenuWorldRenderer menuWorldRenderer = ClientDataHolderVR.getInstance().menuWorldRenderer;
            if (menuWorldRenderer != null && menuWorldRenderer.getLevel() != null) {
                // change brightness based on nether or not
                cir.setReturnValue(menuWorldRenderer.getLevel().getDimensionReaderInfo().constantAmbientLight() ? 0.9f : 0.87f);
            }
        }
    }
}
