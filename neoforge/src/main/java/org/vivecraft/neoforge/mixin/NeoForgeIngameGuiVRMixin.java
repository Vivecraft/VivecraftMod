package org.vivecraft.neoforge.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.NamedGuiOverlay;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(ExtendedGui.class)
public abstract class NeoForgeIngameGuiVRMixin {
    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void vivecraft$noStuff(NamedGuiOverlay overlay, GuiGraphics guiGraphics, CallbackInfoReturnable<Boolean> info) {
        if (RenderPassType.isGuiOnly() && (overlay == VanillaGuiOverlay.VIGNETTE.type() || overlay == VanillaGuiOverlay.SPYGLASS.type() || overlay == VanillaGuiOverlay.HELMET.type() || overlay == VanillaGuiOverlay.FROSTBITE.type() || overlay == VanillaGuiOverlay.PORTAL.type())) {
            info.setReturnValue(true);
        }
    }

    @Redirect(method = "renderPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    public boolean vivecraft$toggleableTabListNeoForge(KeyMapping instance) {
        return instance.isDown() || ((GuiExtension) this).vivecraft$getShowPlayerList();
    }
}
