package org.vivecraft.mixin.client_vr.gui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Mixin(EditBox.class)
public abstract class EditBoxVRMixin extends AbstractWidget {

    @Shadow
    private boolean canLoseFocus;

    public EditBoxVRMixin(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
        super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;canLoseFocus:Z"), method = "mouseClicked(DDI)Z")
    public boolean vivecraft$focus(EditBox instance) {
        return canLoseFocus || !this.isFocused();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I"), method = "mouseClicked(DDI)Z")
    public void vivecraft$openKeyboard(double d, double e, int i, CallbackInfoReturnable<Boolean> cir) {
        if (VRState.vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }
}
