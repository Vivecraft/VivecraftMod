package org.vivecraft.forge.mixin;

import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;useFancyGraphics()Z", remap = true), method = "lambda$static$0", remap = false)
    private static boolean noVignette() {
        return false;
    }
}
