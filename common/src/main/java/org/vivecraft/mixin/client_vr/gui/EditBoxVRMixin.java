package org.vivecraft.mixin.client_vr.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Mixin(EditBox.class)
public abstract class EditBoxVRMixin extends AbstractWidget{

	@Shadow
	private boolean canLoseFocus;

	public EditBoxVRMixin(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
		super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
	}

	@Inject(at = @At(value = "HEAD"), method = "onClick")
	public void openKeyboard(double d, double e, CallbackInfo ci) {
		if (VRState.vrRunning) {
			KeyboardHandler.setOverlayShowing(true);
		}
	}

}
