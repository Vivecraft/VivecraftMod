package org.vivecraft.mixin.client_vr.gui.screens;

import org.vivecraft.client_vr.ClientDataHolderVR;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.VRState;

@Mixin(Screen.class)
public abstract class ScreenVRMixin extends AbstractContainerEventHandler implements Widget {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;fillGradient(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V"), method = "renderBackground(Lcom/mojang/blaze3d/vertex/PoseStack;I)V")
    public void vrBackground(Screen instance, PoseStack poseStack, int i, int j, int k, int l, int m, int n) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().vrSettings != null && !ClientDataHolderVR.getInstance().vrSettings.menuBackground) {
            this.fillGradient(poseStack, i, j, k, l, 0, 0);
        } else {
            this.fillGradient(poseStack, i, j, k, l, m, n);
        }
    }

}