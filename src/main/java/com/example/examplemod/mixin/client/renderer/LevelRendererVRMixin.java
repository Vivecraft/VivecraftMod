package com.example.examplemod.mixin.client.renderer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.example.examplemod.DataHolder;
import com.example.examplemod.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable{
	
	@Unique
	@Nullable
	public RenderTarget alphaSortVROccludedFramebuffer;
	@Unique
	@Nullable
	public RenderTarget alphaSortVRUnoccludedFramebuffer;
	@Unique
	@Nullable
	public RenderTarget alphaSortVRHandsFramebuffer;
	@Unique
	public float selR;
	@Unique
	public float selG;
	@Unique
	public float selB;
	
	@Shadow
	private Minecraft minecraft;
	@Shadow
	private ClientLevel level;
	@Shadow
	private PostChain transparencyChain;
	@Shadow
	private RenderTarget translucentTarget;
	@Shadow
	private RenderTarget itemEntityTarget;
	@Shadow
	private RenderTarget particlesTarget;
	@Shadow
	private RenderTarget weatherTarget;
	@Shadow
	private RenderTarget cloudsTarget;
	@Shadow
	private PostChain entityEffect;
	@Shadow
	private RenderTarget entityTarget;
	@Shadow
	private boolean needsFullRenderChunkUpdate;
	
	public int rainX() {
		return 0;
	}
	
	public int rainY() {
		return 0;
	}
	
	public int rainZ() {
		return 0;
	}
	
	@Overwrite
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		DataHolder.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
	}
	
	public void clearTint() {
		
	}
	
//	@Inject(at = @At(value = "FIELD", target = "needsUpdate:Z", ordinal = 1, shift = Shift.AFTER), method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V")
//	public void alwaysUpdateCull(Camera camera, Frustum frustum, boolean bl, int i, boolean bl2, CallbackInfo info) {
//		this.needsFullRenderChunkUpdate = true;
//	}
//
//	public void lightupdate() {
//		if (DataHolder.getInstance().currentPass == RenderPass.LEFT) {
//			this.level.getProfiler().popPush("light_updates");
//			this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
//		}
//		this.setShaderGroup();
//	}
//	
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE), 
//			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V ")
//	public void stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
//		this.minecraft.getProfiler().popPush("stencil");
//		//TODO shader
//		((GameRendererExtension) gameRenderer).drawEyeStencil(false);
//	}
//	
//	//TODO fixed in vivecraft?
//	public void lighting() {
//		Matrix4f matrix4f1 = new Matrix4f();
//		matrix4f1.setIdentity();
//	}
//	
//	
//	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"), method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
//	public boolean drawSelf(Camera camera) {
//		boolean renderSelf = DataHolder.getInstance().currentPass == RenderPass.THIRD && DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || DataHolder.getInstance().currentPass == RenderPass.CAMERA;
//		renderSelf = renderSelf | (DataHolder.getInstance().vrSettings.shouldRenderSelf || DataHolder.getInstance().vrSettings.tmpRenderSelf);
//		return camera.isDetached() || renderSelf;
//	}
//	
//	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = Shift.BEFORE, ordinal = 13), 
//			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
//	public void renderFast(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
//		boolean menuHandleft = ((GameRendererExtension) gameRenderer).isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
//		boolean menuhandright = menuHandleft || DataHolder.getInstance().interactTracker.hotbar >= 0 && DataHolder.getInstance().vrSettings.vrTouchHotbar;
//		((GameRendererExtension) gameRenderer).renderVrFast(f, false, menuhandright, menuHandleft, poseStack);
//	}
	
	public void setShaderGroup() {
		this.transparencyChain = null;
		this.translucentTarget = null;
		this.itemEntityTarget = null;
		this.particlesTarget = null;
		this.weatherTarget = null;
		this.cloudsTarget = null;
		this.alphaSortVRHandsFramebuffer = null;
		this.alphaSortVROccludedFramebuffer = null;
		this.alphaSortVRUnoccludedFramebuffer = null;
		PostChain postchain = DataHolder.getInstance().vrRenderer.alphaShaders.get(((RenderTargetExtension) this.minecraft.getMainRenderTarget()).getName());

		if (postchain != null) {
			this.transparencyChain = postchain;
			this.translucentTarget = postchain.getTempTarget("translucent");
			this.itemEntityTarget = postchain.getTempTarget("itemEntity");
			this.particlesTarget = postchain.getTempTarget("particles");
			this.weatherTarget = postchain.getTempTarget("weather");
			this.cloudsTarget = postchain.getTempTarget("clouds");
			this.alphaSortVRHandsFramebuffer = postchain.getTempTarget("vrhands");
			this.alphaSortVROccludedFramebuffer = postchain.getTempTarget("vroccluded");
			this.alphaSortVRUnoccludedFramebuffer = postchain.getTempTarget("vrunoccluded");
		}

		this.entityEffect = null;
		this.entityTarget = null;
		PostChain postchain2 = DataHolder.getInstance().vrRenderer.entityShaders.get(((RenderTargetExtension) this.minecraft.getMainRenderTarget()).getName());

		if (postchain2 != null) {
			this.entityEffect = postchain2;
			this.entityTarget = postchain2.getTempTarget("final");
		}
	}

}
