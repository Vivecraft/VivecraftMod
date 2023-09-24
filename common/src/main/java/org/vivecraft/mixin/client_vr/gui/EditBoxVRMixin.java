package org.vivecraft.mixin.client_vr.gui;

import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.components.EditBox.class)
public abstract class EditBoxVRMixin extends net.minecraft.client.gui.components.AbstractWidget {

	@Shadow
	private boolean canLoseFocus;

	public EditBoxVRMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@Inject(at = @At("HEAD"), method = "onClick")
	public void openKeyboard(double d, double e, CallbackInfo ci) {
		if (vrRunning) {
			KeyboardHandler.setOverlayShowing(true);
		}
	}

}
