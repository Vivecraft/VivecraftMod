package org.vivecraft.forge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(ForgeGui.class)
public abstract class ForgeIngameGuiVRMixin {
    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$noOverlaysOnGui(
        NamedGuiOverlay overlay, GuiGraphics guiGraphics, CallbackInfoReturnable<Boolean> cir)
    {
        if (RenderPassType.isGuiOnly() &&
            (overlay == VanillaGuiOverlay.VIGNETTE.type() ||
                overlay == VanillaGuiOverlay.SPYGLASS.type() ||
                overlay == VanillaGuiOverlay.HELMET.type() ||
                overlay == VanillaGuiOverlay.FROSTBITE.type() ||
                overlay == VanillaGuiOverlay.PORTAL.type()
            ))
        {
            cir.setReturnValue(true);
        }
    }

    @ModifyExpressionValue(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean vivecraft$toggleableTabListForge(boolean original) {
        return original || ((GuiExtension) this).vivecraft$getShowPlayerList();
    }
}
