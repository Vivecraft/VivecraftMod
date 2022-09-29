package org.vivecraft.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeIngameGui.class)
public abstract class ForgeIngameGuiVRMixin {

    @Inject(method = "pre(Lnet/minecraftforge/client/gui/IIngameOverlay;Lcom/mojang/blaze3d/vertex/PoseStack;)Z", at = @At("HEAD"), remap = false, cancellable = true)
    private void noStuff(IIngameOverlay overlay, PoseStack poseStack, CallbackInfoReturnable<Boolean> info) {
        if (overlay == ForgeIngameGui.VIGNETTE_ELEMENT || overlay == ForgeIngameGui.SPYGLASS_ELEMENT || overlay == ForgeIngameGui.HELMET_ELEMENT || overlay == ForgeIngameGui.FROSTBITE_ELEMENT || overlay == ForgeIngameGui.PORTAL_ELEMENT) {
            info.setReturnValue(true);
        }
    }
}
