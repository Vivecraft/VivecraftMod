package org.vivecraft.mixin.client_vr.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_xr.XRState;

@Mixin(Window.class)
public abstract class WindowVRMixin {

// NotFixed
//	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;", remap = false),
//			method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)V")
//	public GLFWFramebufferSizeCallback removebuffer(long l, GLFWFramebufferSizeCallbackI cl) {
//		return null;
//	}

	@ModifyVariable(method = "updateVsync", ordinal = 0, at = @At("HEAD"), argsOnly = true)
	boolean overwriteVsync(boolean v) {
		if (XRState.isXr) {
			return false;
		}
		return v;
	}

// NotFixed
//	@Inject(at =  @At("HEAD"), method = "onResize(JII)V", cancellable = true)
//	public void resize(long pWindowPointer, int p_85429_, int pWindowWidth, CallbackInfo info) {
//		if (pWindowWidth * p_85429_ != 0) {
//			this.width = p_85429_;
//			this.height = pWindowWidth;
//			this.eventHandler.resizeDisplay();
//		}
//		info.cancel();
//	}

	@Inject(method = {"getScreenWidth", "getWidth"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftWidth(CallbackInfoReturnable<Integer> cir) {
		if (isCustomFramebuffer()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderWidth());
//			} else {
				var mainTarget = Minecraft.getInstance().getMainRenderTarget();
				cir.setReturnValue(mainTarget.viewWidth);
//			}
		}
	}

	@Inject(method = {"getScreenHeight", "getHeight"}, at = @At("HEAD"), cancellable = true)
	void getVivecraftHeight(CallbackInfoReturnable<Integer> cir) {
		if (isCustomFramebuffer()) {
//			if (mcxrGameRenderer.reloadingDepth > 0) {
//				var swapchain = MCXRPlayClient.OPEN_XR_STATE.session.swapchain;
//				cir.setReturnValue(swapchain.getRenderHeight());
//			} else {
				var mainTarget = Minecraft.getInstance().getMainRenderTarget();
				cir.setReturnValue(mainTarget.viewHeight);
//			}
		}
	}

// NotFixed
//	@Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
//	void getScaledHeight(CallbackInfoReturnable<Integer> cir) {
//		if (isCustomFramebuffer()) {
//			cir.setReturnValue(FGM.scaledHeight);
//		}
//	}
//
//	@Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
//	void getScaledWidth(CallbackInfoReturnable<Integer> cir) {
//		if (isCustomFramebuffer()) {
//			cir.setReturnValue(FGM.scaledWidth);
//		}
//	}
//
//	@Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
//	void getScaleFactor(CallbackInfoReturnable<Double> cir) {
//		if (isCustomFramebuffer()) {
//			cir.setReturnValue(FGM.guiScale);
//		}
//	}


	@Unique
	private boolean isCustomFramebuffer() {
		//MCXR:         return mcxrGameRenderer.overrideWindowSize || (mcxrGameRenderer.isXrMode() && mcxrGameRenderer.reloadingDepth > 0);
		return false;
	}
}
