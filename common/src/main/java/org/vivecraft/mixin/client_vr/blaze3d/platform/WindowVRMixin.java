package org.vivecraft.mixin.client_vr.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.VRState;

@Mixin(Window.class)
public abstract class WindowVRMixin {

	// TODO: check if that actually works like that with sodium extras adaptive sync
	@ModifyVariable(method = "updateVsync", ordinal = 0, at = @At("HEAD"), argsOnly = true)
	boolean overwriteVsync(boolean v) {
		if (VRState.vrRunning) {
			return false;
		}
		return v;
	}

	@Inject(method = {/*"getScreenWidth", */"getWidth"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftWidth(CallbackInfoReturnable<Integer> cir) {
		if (shouldOverrideSide()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderWidth());
//			} else {
				var mainTarget = Minecraft.getInstance().getMainRenderTarget();
				cir.setReturnValue(mainTarget.viewWidth);
//			}
		}
	}

	@Inject(method = {/*"getScreenHeight",*/ "getHeight"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftHeight(CallbackInfoReturnable<Integer> cir) {
		if (shouldOverrideSide()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderHeight());
//			} else {
				var mainTarget = Minecraft.getInstance().getMainRenderTarget();
				cir.setReturnValue(mainTarget.viewHeight);
//			}
		}
	}

	@Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
	void getScaledHeight(CallbackInfoReturnable<Integer> cir) {
		if (shouldOverrideSide()) {
			cir.setReturnValue(GuiHandler.scaledHeight);
		}
	}

	@Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
	void getScaledWidth(CallbackInfoReturnable<Integer> cir) {
		if (shouldOverrideSide()) {
			cir.setReturnValue(GuiHandler.scaledWidth);
		}
	}

	@Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
	void getScaleFactor(CallbackInfoReturnable<Double> cir) {
		if (shouldOverrideSide()) {
			cir.setReturnValue((double) GuiHandler.guiScaleFactor);
		}
	}

	@Inject(method = "onResize", at = @At("HEAD"))
	private void reinitFrameBuffers(long l, int i, int j, CallbackInfo ci){
		if (VRState.vrEnabled) {
			ClientDataHolderVR.getInstance().vrRenderer.reinitFrameBuffers("Main Window Resized");
		}
	}

	@Unique
	private boolean shouldOverrideSide() {
		//MCXR:         return mcxrGameRenderer.overrideWindowSize || (mcxrGameRenderer.isXrMode() && mcxrGameRenderer.reloadingDepth > 0);
		return VRState.vrRunning;
	}
}
