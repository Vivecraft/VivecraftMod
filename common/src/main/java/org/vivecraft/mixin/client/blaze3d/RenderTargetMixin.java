package org.vivecraft.mixin.client.blaze3d;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
//import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.ShaderInstance;

import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Debug(export = true)
@Mixin(com.mojang.blaze3d.pipeline.RenderTarget.class)
public abstract class RenderTargetMixin implements org.vivecraft.client.extensions.RenderTargetExtension {

	@Unique
	private int texid = -1;
	@Unique
	private boolean linearFilter;
	@Unique
	private boolean useStencil = false;
	@Shadow
	public int frameBufferId;
	@Shadow
	public boolean useDepth;
	@Shadow
	public int width;
	@Shadow
	public int height;
	@Shadow
	public int viewHeight;
	@Shadow
	public int viewWidth;
	@Shadow
	protected int colorTextureId;
	@Shadow
	public abstract void unbindRead();
	@Shadow
	public abstract void bindRead();
	@Shadow
	public abstract void clear(boolean onMacIn);
	@Shadow
	public abstract void checkStatus();
	@Shadow
	public abstract void setFilterMode(int i);
	@Shadow
	private void _bindWrite(boolean b){};

	@Shadow public abstract void unbindWrite();

	@Override
	public void setUseStencil(boolean useStencil){
		this.useStencil = useStencil;
	}

	@Override
	public boolean getUseStencil(){
		return this.useStencil;
	}

	@Override
	public void clearWithColor(float r, float g, float b, float a, boolean isMac) {
		RenderSystem.assertOnRenderThreadOrInit();
		this._bindWrite(true);
		RenderSystem.clearColor(r, g, b, a);
		int i = GL11C.GL_COLOR_BUFFER_BIT;
		if (this.useDepth) {
			RenderSystem.clearDepth(1.0);
			i |= GL11C.GL_DEPTH_BUFFER_BIT;
		}
		RenderSystem.clear(i, isMac);
		this.unbindWrite();
	}

	@Override
	public void setTextid(int texid){
		this.texid = texid;
	}

	@Override
	public void isLinearFilter(boolean linearFilter){
		this.linearFilter = linearFilter;
	}

	@Override
	public String toString() {
		return "\n" +
			"Size:   " + this.viewWidth + " x " + this.viewHeight + "\n" +
			"FB ID:  " + this.frameBufferId + "\n" +
			"Tex ID: " + this.colorTextureId + "\n";
	}
	

//	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I"), method = "createBuffers(IIZ)V")
//	public int buffer() {
//		if (this.texid == -1) {
//			return this.colorTextureId = TextureUtil.generateTextureId();
//		} else {
//			return this.colorTextureId = this.texid;
//		}
//	}
	
//	/**
//	 * @author
//	 * @reason
//	 */
//	@Overwrite
//	public void createBuffers(int i, int j, boolean bl) {
//		RenderSystem.assertOnGameThreadOrInit();
//		int k = RenderSystem.maxSupportedTextureSize();
//		if (i > 0 && i <= k && j > 0 && j <= k) {
//			this.viewWidth = i;
//			this.viewHeight = j;
//			this.width = i;
//			this.height = j;
//			this.frameBufferId = GlStateManager.glGenFramebuffers();
//			if (this.texid == -1) {
//				this.colorTextureId = TextureUtil.generateTextureId();
//			} else {
//				this.colorTextureId = this.texid;
//			}
//			if (this.useDepth) {
//				this.depthBufferId = TextureUtil.generateTextureId();
//				GlStateManager._bindTexture(this.depthBufferId);
//				GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, linearFilter ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
//				GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, linearFilter ? GL11C.GL_LINEAR : GL11C.GL_NEAREST);
//				GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL14C.GL_TEXTURE_COMPARE_MODE, 0);
//				GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
//				GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
//              GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL30C.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, GL30C.GL_DEPTH_STENCIL, GL30C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
// 			}
//			if (linearFilter)
//				this.setFilterMode(GL11C.GL_LINEAR);
//			else
//				this.setFilterMode(GL11C.GL_NEAREST);
//			GlStateManager._bindTexture(this.colorTextureId);
//			GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL12C.GL_CLAMP_TO_EDGE);
//			GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL12C.GL_CLAMP_TO_EDGE);
//			GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, this.width, this.height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (IntBuffer)null);
//			GlStateManager._glBindFramebuffer(GL30C.GL_FRAMEBUFFER, this.frameBufferId);
//			GlStateManager._glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, this.colorTextureId, 0);
//			if (this.useDepth) {
//				GlStateManager._glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_DEPTH_ATTACHMENT, GL11C.GL_TEXTURE_2D, this.depthBufferId, 0);
//              GlStateManager._glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_STENCIL_ATTACHMENT, GL11C.GL_TEXTURE_2D, this.depthBufferId, 0);
//			}
//
//			this.checkStatus();
//			this.clear(bl);
//			this.unbindRead();
//		} else {
//			throw new IllegalArgumentException("Window " + i + "x" + j + " size out of bounds (max. size: " + k + ")");
//		}
//	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", ordinal = 0), method = "createBuffers")
	public int genTextureId() {
		if (this.texid == -1) {
			return TextureUtil.generateTextureId();
		} else {
			return this.texid;
		}
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0), method = "createBuffers", index = 2)
	public int modifyTexImage2DInternalformat(int internalformat) {
		return this.useStencil ? GL30C.GL_DEPTH32F_STENCIL8 : internalformat;
	}
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0), method = "createBuffers", index = 6)
	public int modifyTexImage2DFormat(int format) {
		return this.useStencil ? GL30C.GL_DEPTH_STENCIL : format;
	}
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0), method = "createBuffers", index = 7)
	public int modifyTexImage2DType(int type) {
		return this.useStencil ? GL30C.GL_FLOAT_32_UNSIGNED_INT_24_8_REV : type;
	}

	@ModifyConstant(method = "createBuffers", constant = @Constant(intValue = 9728))
	public int changeTextPar(int i) {
		return this.linearFilter ? GL11C.GL_LINEAR : i;
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", ordinal = 1), method = "createBuffers", index = 1)
	public int modifyGlFramebufferTexture2DAttachment(int attachment) {
		return this.useStencil ? GL30C.GL_DEPTH_STENCIL_ATTACHMENT : attachment;
	}

	public void blitToScreen(ShaderInstance instance, int left, int width, int height, int top, boolean disableBlend, float xCropFactor,
			float yCropFactor, boolean keepAspect) {
		RenderSystem.assertOnGameThreadOrInit();
		if (!RenderSystem.isInInitPhase()) {
			RenderSystem.recordRenderCall(() -> {
				this._blitToScreen(instance, left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
			});
		} else {
			this._blitToScreen(instance, left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
		}

	}

	@Override
	public void blitFovReduction(ShaderInstance instance, int width, int height) {
		RenderSystem.assertOnRenderThread();
		RenderSystem.colorMask(true, true, true, false);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.viewport(0, 0, width, height);
		RenderSystem.disableBlend();
		RenderSystem.setShaderTexture(0, this.colorTextureId);
		if (instance == null) {
			instance = mc.gameRenderer.blitShader;
			instance.setSampler("DiffuseSampler", this.colorTextureId);
		} else {
			for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
				int l = RenderSystem.getShaderTexture(k);
				instance.setSampler("Sampler" + k, l);
			}
		}
		Matrix4f matrix4f = new Matrix4f().setOrtho(0, width, height, 0,1000.0F, 3000.0F);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		if (instance.MODEL_VIEW_MATRIX != null) {
			instance.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
		}
		if (instance.PROJECTION_MATRIX != null) {
			instance.PROJECTION_MATRIX.set(matrix4f);
		}
		instance.apply();
		float h = (float)this.viewWidth / this.width;
		float k = (float)this.viewHeight / this.height;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(Mode.QUADS, instance.getVertexFormat());
		if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX) {
			bufferBuilder.vertex(0.0D, height, 0.0D).uv(0.0F, 0.0F).endVertex();
			bufferBuilder.vertex(width, height, 0.0D).uv(h, 0.0F).endVertex();
			bufferBuilder.vertex(width, 0.0D, 0.0D).uv(h, k).endVertex();
			bufferBuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, k).endVertex();
		} else if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX_COLOR) {
			bufferBuilder.vertex(0.0D, height, 0.0D).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(width, height, 0.0D).uv(h, 0.0F).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(width, 0.0D, 0.0D).uv(h, k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, k).color(255, 255, 255, 255).endVertex();
		} else {
			throw new IllegalStateException("Unexpected vertex format " + instance.getVertexFormat());
		}
		BufferUploader.draw(bufferBuilder.end());
		instance.clear();
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
	}

	private void _blitToScreen(ShaderInstance instance, int left, int width, int height, int top, boolean bl, float xCropFactor,
			float yCropFactor, boolean keepAspect) {
		RenderSystem.assertOnGameThreadOrInit();
		RenderSystem.colorMask(true, true, true, false);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.viewport(left, top, width, height);
		if (bl) {
			RenderSystem.disableBlend();
		}


		float f = (float) width / height;
		float f1 = (float) this.viewWidth / this.viewHeight;
		float f2 = width;
		float f3 = height;
		float f4 = 0.0F;
		float f5 = 0.0F;

		if (keepAspect) {
			if (f > f1) {
				float f6 = (float) width / this.viewWidth;
				f4 = 0.0F;
				f2 = width;
				f5 = height / 2.0F - this.viewHeight / 2.0F * f6;
				f3 = height / 2.0F + this.viewHeight / 2.0F * f6;
			} else {
				float f10 = (float) height / this.viewHeight;
				f4 = width / 2.0F - this.viewWidth / 2.0F * f10;
				f2 = width / 2.0F + this.viewWidth / 2.0F * f10;
				f5 = 0.0F;
				f3 = height;
			}
		}

		float f11 = width;
		float f7 = height;
		float f8 = (float) this.viewWidth / this.width;
		float f9 = (float) this.viewHeight / this.height;

			if (instance == null) {
				instance = mc.gameRenderer.blitShader;
				instance.setSampler("DiffuseSampler", this.colorTextureId);
			} else {
				for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
					int l = RenderSystem.getShaderTexture(k);
					instance.setSampler("Sampler" + k, l);
				}
			}
			Matrix4f matrix4f = new Matrix4f().setOrtho(0, width, height, 0, 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);

			if (instance.MODEL_VIEW_MATRIX != null) {
				instance.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
			}

			if (instance.PROJECTION_MATRIX != null) {
				instance.PROJECTION_MATRIX.set(matrix4f);
			}

			instance.apply();

			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			bufferbuilder.begin(Mode.QUADS, instance.getVertexFormat());
			bufferbuilder.vertex(f4, f3, 0.0D).uv(xCropFactor, yCropFactor).color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex(f2, f3, 0.0D).uv(f8 - xCropFactor, yCropFactor).color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex(f2, f5, 0.0D).uv(f8 - xCropFactor, f9 - yCropFactor).color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex(f4, f5, 0.0D).uv(xCropFactor, f9 - yCropFactor).color(255, 255, 255, 255).endVertex();
			BufferUploader.draw(bufferbuilder.end());
			instance.clear();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
	}

}
