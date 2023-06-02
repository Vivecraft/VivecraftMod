package org.vivecraft.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(ForgeGui.class)
public abstract class ForgeIngameGuiVRMixin{
    @Inject(method = "pre", at = @At("HEAD"), remap = false, cancellable = true)
    private void noStuff(NamedGuiOverlay overlay, PoseStack poseStack, CallbackInfoReturnable<Boolean> info) {
        if (RenderPassType.isGuiOnly() && (overlay == VanillaGuiOverlay.VIGNETTE.type() || overlay == VanillaGuiOverlay.SPYGLASS.type() || overlay == VanillaGuiOverlay.HELMET.type() || overlay == VanillaGuiOverlay.FROSTBITE.type() || overlay == VanillaGuiOverlay.PORTAL.type())) {
            info.setReturnValue(true);
        }
    }

    @Redirect(method = "renderPlayerList(IILcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    public boolean toggleableTabListForge(KeyMapping instance) {
        return instance.isDown() || ((GuiExtension)this).getShowPlayerList();
    }
}
