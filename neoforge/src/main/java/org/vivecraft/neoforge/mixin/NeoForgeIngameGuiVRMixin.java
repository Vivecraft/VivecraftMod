package org.vivecraft.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.NamedGuiOverlay;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(ExtendedGui.class)
public abstract class NeoForgeIngameGuiVRMixin {
    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$noStuff(
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
    private boolean vivecraft$toggleableTabListNeoForge(boolean original) {
        return original || ((GuiExtension) this).vivecraft$getShowPlayerList();
    }
}
