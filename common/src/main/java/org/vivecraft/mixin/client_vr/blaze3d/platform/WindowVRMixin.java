package org.vivecraft.mixin.client_vr.blaze3d.platform;

import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.mojang.blaze3d.platform.Window.class)
public abstract class WindowVRMixin {

	// TODO: check if that actually works like that with sodium extras adaptive sync
	@ModifyVariable(method = "updateVsync", ordinal = 0, at = @At("HEAD"), argsOnly = true)
	boolean overwriteVsync(boolean v) {
		return !vrRunning && v;
	}

	@Inject(method = {/*"getScreenWidth", */"getWidth"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftWidth(CallbackInfoReturnable<Integer> cir) {
		if (this.shouldOverrideSide()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderWidth());
//			} else {
				cir.setReturnValue(mc.getMainRenderTarget().viewWidth);
//			}
		}
	}

	@Inject(method = {/*"getScreenHeight",*/ "getHeight"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftHeight(CallbackInfoReturnable<Integer> cir) {
		if (this.shouldOverrideSide()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderHeight());
//			} else {
				cir.setReturnValue(mc.getMainRenderTarget().viewHeight);
//			}
		}
	}

	@Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
	void getScaledHeight(CallbackInfoReturnable<Integer> cir) {
		if (this.shouldOverrideSide()) {
			cir.setReturnValue(GuiHandler.scaledHeight);
		}
	}

	@Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
	void getScaledWidth(CallbackInfoReturnable<Integer> cir) {
		if (this.shouldOverrideSide()) {
			cir.setReturnValue(GuiHandler.scaledWidth);
		}
	}

	@Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
	void getScaleFactor(CallbackInfoReturnable<Double> cir) {
		if (this.shouldOverrideSide()) {
			cir.setReturnValue((double) GuiHandler.guiScaleFactor);
		}
	}

	@Inject(method = "onResize", at = @At("HEAD"))
	private void resizeFrameBuffers(long l, int i, int j, CallbackInfo ci){
		if (vrEnabled) {
			dh.vrRenderer.resizeFrameBuffers("Main Window Resized");
		}
	}

	@Unique
	private boolean shouldOverrideSide() {
		//MCXR:         return mcxrGameRenderer.overrideWindowSize || (mcxrGameRenderer.isXrMode() && mcxrGameRenderer.reloadingDepth > 0);
		return vrRunning;
	}
}
