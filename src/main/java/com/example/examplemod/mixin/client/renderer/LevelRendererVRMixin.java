package com.example.examplemod.mixin.client.renderer;

import javax.annotation.Nullable;

import com.example.examplemod.LevelRendererExtension;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.render.RenderPass;
import org.vivecraft.settings.VRSettings;

import com.example.examplemod.DataHolder;
import com.example.examplemod.GameRendererExtension;
import com.example.examplemod.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererVRMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {
	
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

	@Unique
	private Entity capturedEntity;
	
	@Final
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
	@Final
	@Shadow
	private RenderBuffers renderBuffers;
	@Unique
	private Entity renderedEntity;

	@Shadow
	protected abstract void renderHitOutline(PoseStack poseStack, VertexConsumer buffer, Entity entity, double d, double e, double g, BlockPos blockpos, BlockState blockstate);

	public int rainX() {
		return 0;
	}
	
	public int rainY() {
		return 0;
	}
	
	public int rainZ() {
		return 0;
	}

	@Override
	public Entity getRenderedEntity() {
		return this.capturedEntity;
	}
	
	@Overwrite
	public void onResourceManagerReload(ResourceManager pResourceManager) {
		DataHolder.getInstance().vrRenderer.reinitFrameBuffers("Resource Reload");
	}

//	@Redirect(at = @At(value = "INVOKE", target = "graphicsChanged()V"), method = "allChanged()V")
//	public void removeGraphich(LevelRenderer l) {
//
//	}
	
	public void clearTint() {
		
	}
	
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;needsFullRenderChunkUpdate:Z", ordinal = 1, shift = Shift.AFTER), method = "setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V")
	public void alwaysUpdateCull(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo info) {
		this.needsFullRenderChunkUpdate = true;
	}

	public void lightupdate() {
		if (DataHolder.getInstance().currentPass == RenderPass.LEFT) {
			this.level.getProfiler().popPush("light_updates");
			this.minecraft.level.getChunkSource().getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);
		}
		this.setShaderGroup();
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE), 
			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V ")
	public void stencil(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
		this.minecraft.getProfiler().popPush("stencil");
		//TODO shader
		((GameRendererExtension) gameRenderer).drawEyeStencil(false);
	}
	
	//TODO fixed in vivecraft?
	public void lighting() {
		Matrix4f matrix4f1 = new Matrix4f();
		matrix4f1.setIdentity();
	}
	
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z"), method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
	public boolean drawSelf(Camera camera) {
		boolean renderSelf = DataHolder.getInstance().currentPass == RenderPass.THIRD && DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON || DataHolder.getInstance().currentPass == RenderPass.CAMERA;
		renderSelf = renderSelf | (DataHolder.getInstance().vrSettings.shouldRenderSelf || DataHolder.getInstance().vrSettings.tmpRenderSelf);
		return camera.isDetached() || renderSelf;
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;tickCount:I"), method = "renderLevel")
	public int captureEntity(Entity instance) {
		this.capturedEntity = instance;
		return instance.tickCount;
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;tickCount:I", shift = Shift.BEFORE), method = "renderLevel")
	public void restoreLoc1(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		if (capturedEntity == camera.getEntity()) {
			((GameRendererExtension)gameRenderer).restoreRVEPos((LivingEntity)capturedEntity);
		}
		this.renderedEntity = capturedEntity;
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"), method = "renderLevel")
	public void restoreLoc2(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		if (capturedEntity == camera.getEntity()) {
			((GameRendererExtension)gameRenderer).restoreRVEPos((LivingEntity)capturedEntity);
			((GameRendererExtension)gameRenderer).setupRVE();
		}
		this.renderedEntity = null;
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"), method = "renderLevel")
	public void interactOutline(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
		selR = selG = selB = 1f;
		Vec3 vec3 = camera.getPosition();
		double d = vec3.x();
		double e = vec3.y();
		double g = vec3.z();
		for (int c=0;c<2;c++) {
			if(DataHolder.getInstance().interactTracker.isInteractActive(c) && (DataHolder.getInstance().interactTracker.inBlockHit[c] != null || DataHolder.getInstance().interactTracker.bukkit[c])) {
				BlockPos blockpos = DataHolder.getInstance().interactTracker.inBlockHit[c] != null ? DataHolder.getInstance().interactTracker.inBlockHit[c].getBlockPos() : new BlockPos(DataHolder.getInstance().vrPlayer.vrdata_world_render.getController(c).getPosition());
				BlockState blockstate = this.level.getBlockState(blockpos);
				this.renderHitOutline(poseStack, this.renderBuffers.bufferSource().getBuffer(RenderType.lines()), camera.getEntity(), d, e, g, blockpos, blockstate);
				if (c==0) {
					bl = false; //don't draw both
				}
			}
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 13), method = "renderLevel")
	public void blackOutline(ProfilerFiller instance, String s) {
		selR = selG = selB = 0f;
	}



	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = Shift.BEFORE, ordinal = 14),
			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
	public void renderFast1(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
		boolean menuHandleft = ((GameRendererExtension) gameRenderer).isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
		boolean menuhandright = menuHandleft || DataHolder.getInstance().interactTracker.hotbar >= 0 && DataHolder.getInstance().vrSettings.vrTouchHotbar;
		((GameRendererExtension) gameRenderer).renderVrFast(f, false, menuhandright, menuHandleft, poseStack);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", ordinal = 1),
			method = "renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lcom/mojang/math/Matrix4f;)V")
	public void renderFast2(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo info) {
		boolean menuHandleft = ((GameRendererExtension) gameRenderer).isInMenuRoom() || this.minecraft.screen != null || KeyboardHandler.Showing;
		boolean menuhandright = menuHandleft || DataHolder.getInstance().interactTracker.hotbar >= 0 && DataHolder.getInstance().vrSettings.vrTouchHotbar;
		((GameRendererExtension) gameRenderer).renderVrFast(f, false, menuhandright, menuHandleft, poseStack);
	}
	
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
