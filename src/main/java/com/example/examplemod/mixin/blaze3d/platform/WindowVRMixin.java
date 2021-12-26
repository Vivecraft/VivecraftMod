package com.example.examplemod.mixin.blaze3d.platform;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;

import net.minecraft.client.Minecraft;

@Mixin(Window.class)
public abstract class WindowVRMixin {
	

	@Shadow
	private int width;
	@Shadow
	private int height;
	@Shadow
	private WindowEventHandler eventHandler;
	@Shadow
	private double guiScale;
	@Shadow
	private int guiScaledWidth;
	@Shadow
	private int guiScaledHeight;
	
	@Shadow
	public abstract int getScreenWidth();
	@Shadow
	protected abstract int getScreenHeight();

	@ModifyArg(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 4, remap = false), 
			method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)V",
			index = 1)
	public int windowhint(int i) {
		return 204802;
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V", ordinal = 5, remap = false), 
			method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)V")
	public void removewindowhint(int i, int i2) {
		return;
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;", remap = false), 
			method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)V", remap = false)
	public GLFWFramebufferSizeCallback removebuffer(long l, GLFWFramebufferSizeCallbackI cl) {
		return null;
	}
	
	public void glfwSwapInterval() {
		
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "getWidth()I"), method = "onFramebufferResize(JII)V")
	public int widthCorrection(Window w) {
		return this.getScreenWidth();
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "getHeight()I"), method = "onFramebufferResize(JII)V")
	public int heightCorrection(Window w) {
		return this.getScreenHeight();
	}

	@Inject(at =  @At("HEAD"), method = "onResize(JII)V", cancellable = true)
	public void resize(long pWindowPointer, int p_85429_, int pWindowWidth, CallbackInfo info) {
		if (pWindowWidth * p_85429_ != 0) {
			this.width = p_85429_;
			this.height = pWindowWidth;
			this.eventHandler.resizeDisplay();
		}
		info.cancel();
	}
	
//	@Redirect(at = @At(value = "FIELD", target = "framebufferWidth:I"), method = "calculateScale(IZ)V")
//	public int bufferWidthCorrection(Window w) {
//		return this.width;
//	}
//	
//	@Redirect(at = @At(value = "FIELD", target = "framebufferHeight:I"), method = "calculateScale(IZ)V")
//	public int bufferHeightCorrection(Window w) {
//		return this.height;
//	}
	
	public int calculateScale(int pGuiScale, boolean bl) {
		int i;
		for (i = 1; i != pGuiScale && i < this.width && i < this.height && this.width / (i + 1) >= 320 && this.height / (i + 1) >= 240; ++i) {

		}

		if (bl && i % 2 != 0) {
//			++j;
		}

		return i;
	}
	
	@Inject(at = @At("HEAD"), method = "getWidth()I", cancellable = true)
	public void changeGetWidth(CallbackInfoReturnable<Integer> i) {
		//It should not be null, something is wrong
		if (Minecraft.getInstance().getMainRenderTarget() != null)
			i.setReturnValue(Minecraft.getInstance().getMainRenderTarget().viewWidth);
	}

	@Inject(at = @At("HEAD"), method = "getHeight()I", cancellable = true)
	public void changeGetHeight(CallbackInfoReturnable<Integer> i) { 
		//It should not be null, something is wrong
		if (Minecraft.getInstance().getMainRenderTarget() != null)
			i.setReturnValue(Minecraft.getInstance().getMainRenderTarget().viewHeight);
	}
	
	public void setGuiScale(double pScaleFactor) {
		this.guiScale = pScaleFactor;

		int i = (int) ((double) this.width / pScaleFactor);
		this.guiScaledWidth = (double) this.width / pScaleFactor > (double) i ? i + 1 : i;
		int j = (int) ((double) this.height / pScaleFactor);
		this.guiScaledHeight = (double) this.height / pScaleFactor > (double) j ? j + 1 : j;
	}

}
