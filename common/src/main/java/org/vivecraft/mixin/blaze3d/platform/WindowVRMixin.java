package org.vivecraft.mixin.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public abstract class WindowVRMixin {
	

	@Shadow
	private int width;
	@Shadow
	private int height;
	@Final
	@Shadow
	private WindowEventHandler eventHandler;
	
	@Shadow
	public abstract int getScreenWidth();
	@Shadow
	public abstract int getScreenHeight();
	
	@Redirect(at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;", remap = false), 
			method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/ScreenManager;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;Ljava/lang/String;)V")
	public GLFWFramebufferSizeCallback removebuffer(long l, GLFWFramebufferSizeCallbackI cl) {
		return null;
	}

	@Inject(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false, shift = At.Shift.BEFORE), cancellable = true)
	public void disableSwap(boolean bl, CallbackInfo ci) {
		GLFW.glfwSwapInterval(0);
		ci.cancel();
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWidth()I"), method = "onFramebufferResize(JII)V")
	public int widthCorrection(Window w) {
		return this.getScreenWidth();
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getHeight()I"), method = "onFramebufferResize(JII)V")
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
	@ModifyVariable(at = @At("HEAD"), method = "calculateScale", argsOnly = true)
	private boolean noSpecialUnicodeScale(boolean forceUnicode){
		return false;
	}
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lcom/mojang/blaze3d/platform/Window;framebufferWidth:I"), method = "calculateScale")
	private int widthRedirectCalculateScale(Window instance){
		return this.width;
	}
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I"), method = "calculateScale")
	private int heightRedirectCalculateScale(Window instance){
		return this.height;
	}

	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lcom/mojang/blaze3d/platform/Window;framebufferWidth:I"), method = "setGuiScale")
	private int widthRedirectSetGuiScale(Window instance){
		return this.width;
	}
	@Redirect(at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I"), method = "setGuiScale")
	private int heightRedirectSetGuiScale(Window instance){
		return this.height;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public int getWidth() {
		return Minecraft.getInstance().getMainRenderTarget().viewWidth;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public int getHeight() {
		return Minecraft.getInstance().getMainRenderTarget().viewHeight;
	}

}
