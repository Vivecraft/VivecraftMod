package org.vivecraft.mixin.client.gui.components;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EditBox.class)
public abstract class EditBoxVRMixin extends AbstractWidget{
	
	public EditBoxVRMixin(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
		super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
	}
	
	@Shadow
	public abstract void setFocus(boolean p_94179_);

	//TODO test
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;canLoseFocus:Z"), method = "Lnet/minecraft/client/gui/components/EditBox;mouseClicked(DDI)Z", locals = LocalCapture.CAPTURE_FAILHARD)
	public void focus(double d, double e, int i, CallbackInfoReturnable<Boolean> cir, int f, boolean bl) {
		if (!this.isFocused())
        {
            this.setFocus(bl);
        }
	}

}
