package com.example.vivecraftfabric.mixin.client.gui.components;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

@Mixin(EditBox.class)
public abstract class EditBoxVRMixin extends AbstractWidget{
	
	public EditBoxVRMixin(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
		super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
	}
	
	@Shadow
	public abstract void setFocus(boolean p_94179_);

	//TODO test
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/EditBox;canLoseFocus:Z"), method = "Lnet/minecraft/client/gui/components/EditBox;mouseClicked(DDI)Z")
	public void focus(double p_94125_, double p_94126_, int p_94127_, CallbackInfoReturnable<Boolean> info, boolean flag) {
		if (!this.isFocused())
        {
            this.setFocus(flag);
        }
	}

}
