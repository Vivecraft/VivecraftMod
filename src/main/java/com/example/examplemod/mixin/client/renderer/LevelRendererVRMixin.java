package com.example.examplemod.mixin.client.renderer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.examplemod.DataHolder;
import com.example.examplemod.GameRendererExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererVRMixin {
	
	@Nullable
	public RenderTarget alphaSortVROccludedFramebuffer;
	@Nullable
	public RenderTarget alphaSortVRUnoccludedFramebuffer;
	@Nullable
	public RenderTarget alphaSortVRHandsFramebuffer;
	public float selR;
	public float selG;
	public float selB;
	
	@Shadow
	private Minecraft minecraft;
	
	public void reload(ResourceManager pResourceManager, CallbackInfo info) {
		DataHolder.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
		info.cancel();
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE), 
			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V ")
	public void stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
		this.minecraft.getProfiler().popPush("stencil");
		//TODO shader
		((GameRendererExtension) gameRenderer).drawEyeStencil(false);
	}

}
