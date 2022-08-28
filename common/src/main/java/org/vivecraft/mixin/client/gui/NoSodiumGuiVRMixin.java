package org.vivecraft.mixin.client.gui;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public class NoSodiumGuiVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;useFancyGraphics()Z"), method = "render")
    public boolean noVignette() {
        return false;
    }
}
