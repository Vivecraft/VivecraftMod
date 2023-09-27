package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenVRMixin {

    @Shadow
    protected boolean isQuickCrafting;

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;isQuickCrafting:Z"), method = "mouseDragged")
    public boolean vivecraft$shift(AbstractContainerScreen instance) {
        return this.isQuickCrafting && !Screen.hasShiftDown();
    }
}
