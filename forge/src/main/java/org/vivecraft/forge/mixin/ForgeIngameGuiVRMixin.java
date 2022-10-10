package org.vivecraft.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeGui.class)
public abstract class ForgeIngameGuiVRMixin {
    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void noStuff(NamedGuiOverlay overlay, PoseStack poseStack, CallbackInfoReturnable<Boolean> info) {
        if (overlay == VanillaGuiOverlay.VIGNETTE.type() || overlay == VanillaGuiOverlay.SPYGLASS.type() || overlay == VanillaGuiOverlay.HELMET.type() || overlay == VanillaGuiOverlay.FROSTBITE.type() || overlay == VanillaGuiOverlay.PORTAL.type()) {
            info.setReturnValue(true);
        }
    }
}
