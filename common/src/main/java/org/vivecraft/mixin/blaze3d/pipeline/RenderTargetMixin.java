package org.vivecraft.mixin.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.vivecraft.extensions.RenderTargetExtension;
import org.vivecraft.mixin.blaze3d.systems.RenderSystemAccessor;

@Debug(export = true)
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {

	@Unique
	private int texid = -1;
	@Unique
	public String name = "Default";
	@Unique
	private boolean linearFilter;
	@Unique
	private boolean useStencil = false;
	@Shadow
	public int frameBufferId;
	@Shadow
	protected int depthBufferId;
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

	@Shadow public abstract void unbindWrite();

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void blitToScreen(int pWidth, int pHeight, boolean p_83960_) {
		this.blitToScreen(0, pWidth, pHeight, 0, p_83960_, 0.0F, 0.0F, false);
	}
	
	@Override
	public int getDepthBufferId() {
		return depthBufferId;
	}

	@Override
	public void setName(String name) {
		this.name=name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setUseStencil(boolean useStencil){
		this.useStencil = useStencil;
	}

	@Override
	public boolean getUseStencil(){
		return useStencil;
	}

	@Override
	public void clearWithColor(float r, float g, float b, float a, boolean isMac) {
		RenderSystem.assertOnRenderThreadOrInit();
		this._bindWrite(true);
		GlStateManager._clearColor(r, g, b, a);
		int i = 16384;
		if (this.useDepth) {
			GlStateManager._clearDepth(1.0);
			i |= 0x100;
		}
		GlStateManager._clear(i, isMac);
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
		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append("\n");
		if (this.name != null) {

			stringbuilder.append("Name:   " + this.name).append("\n");
		}
		stringbuilder.append("Size:   " + this.viewWidth + " x " + this.viewHeight).append("\n");
		stringbuilder.append("FB ID:  " + this.frameBufferId).append("\n");
		stringbuilder.append("Tex ID: " + this.colorTextureId).append("\n");
		return stringbuilder.toString();
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
//				GlStateManager._texParameter(3553, 10241, linearFilter ? GL11.GL_LINEAR : 9728);
//				GlStateManager._texParameter(3553, 10240, linearFilter ? GL11.GL_LINEAR : 9728);
//				GlStateManager._texParameter(3553, 34892, 0);
//				GlStateManager._texParameter(3553, 10242, 33071);
//				GlStateManager._texParameter(3553, 10243, 33071);
//              GlStateManager._texImage2D(3553, 0, 36013, this.width, this.height, 0, 34041, 36269, null);
// 			}
//			if (linearFilter)
//				this.setFilterMode(GL11.GL_LINEAR);
//			else
//				this.setFilterMode(9728);
//			GlStateManager._bindTexture(this.colorTextureId);
//			GlStateManager._texParameter(3553, 10242, 33071);
//			GlStateManager._texParameter(3553, 10243, 33071);
//			GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, (IntBuffer)null);
//			GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
//			GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
//			if (this.useDepth) {
//				GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, 36096, 3553, this.depthBufferId, 0);
//              GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, 36128, 3553, this.depthBufferId, 0);
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
		return useStencil ? GL30.GL_DEPTH32F_STENCIL8 : internalformat;
	}
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0), method = "createBuffers", index = 6)
	public int modifyTexImage2DFormat(int format) {
		return useStencil ? GL30.GL_DEPTH_STENCIL : format;
	}
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0), method = "createBuffers", index = 7)
	public int modifyTexImage2DType(int type) {
		return useStencil ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV : type;
	}

	@ModifyConstant(method = "createBuffers", constant = @Constant(intValue = 9728))
	public int changeTextPar(int i) {
		return linearFilter ? GL11.GL_LINEAR : i;
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", ordinal = 1), method = "createBuffers", index = 1)
	public int modifyGlFramebufferTexture2DAttachment(int attachment) {
		return useStencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void _bindWrite(boolean bl) {
		RenderSystem.assertOnGameThreadOrInit();
		GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
		if (bl) {
			GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
		}
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
		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._viewport(0, 0, width, height);
		GlStateManager._disableBlend();
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, this.colorTextureId);
		if (instance == null) {
			instance = minecraft.gameRenderer.blitShader;
			instance.setSampler("DiffuseSampler", this.colorTextureId);
		} else {
			for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
				int l = RenderSystem.getShaderTexture(k);
				instance.setSampler("Sampler" + k, l);
			}
		}
		Matrix4f matrix4f = Matrix4f.orthographic(width, -height, 1000.0f, 3000.0f);
		RenderSystem.setProjectionMatrix(matrix4f);
		if (instance.MODEL_VIEW_MATRIX != null) {
			instance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0f, 0.0f, -2000.0f));
		}
		if (instance.PROJECTION_MATRIX != null) {
			instance.PROJECTION_MATRIX.set(matrix4f);
		}
		instance.apply();
		float f = width;
		float g = height;
		float h = (float)this.viewWidth / (float)this.width;
		float k = (float)this.viewHeight / (float)this.height;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, instance.getVertexFormat());
		if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX) {
			bufferBuilder.vertex(0.0, g, 0.0).uv(0.0f, 0.0f).endVertex();
			bufferBuilder.vertex(f, g, 0.0).uv(h, 0.0f).endVertex();
			bufferBuilder.vertex(f, 0.0, 0.0).uv(h, k).endVertex();
			bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0f, k).endVertex();
		} else if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX_COLOR) {
			bufferBuilder.vertex(0.0, g, 0.0).uv(0.0f, 0.0f).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(f, g, 0.0).uv(h, 0.0f).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(f, 0.0, 0.0).uv(h, k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0f, k).color(255, 255, 255, 255).endVertex();
		} else {
			throw new IllegalStateException("Unexpected vertex format " + instance.getVertexFormat());
		}
		BufferUploader.draw(bufferBuilder.end());
		instance.clear();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

	private void _blitToScreen(ShaderInstance instance, int left, int width, int height, int top, boolean bl, float xCropFactor,
			float yCropFactor, boolean keepAspect) {
		RenderSystem.assertOnGameThreadOrInit();
		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._viewport(left, top, width, height);
		if (bl) {
			GlStateManager._disableBlend();
		}

		Minecraft minecraft = Minecraft.getInstance();

		float f = (float) width / (float) height;
		float f1 = (float) this.viewWidth / (float) this.viewHeight;
		float f2 = (float) width;
		float f3 = (float) height;
		float f4 = 0.0F;
		float f5 = 0.0F;

		if (keepAspect) {
			if (f > f1) {
				float f6 = (float) width / (float) this.viewWidth;
				f4 = 0.0F;
				f2 = (float) width;
				f5 = (float) height / 2.0F - (float) this.viewHeight / 2.0F * f6;
				f3 = (float) height / 2.0F + (float) this.viewHeight / 2.0F * f6;
			} else {
				float f10 = (float) height / (float) this.viewHeight;
				f4 = (float) width / 2.0F - (float) this.viewWidth / 2.0F * f10;
				f2 = (float) width / 2.0F + (float) this.viewWidth / 2.0F * f10;
				f5 = 0.0F;
				f3 = (float) height;
			}
		}

		float f11 = (float) width;
		float f7 = (float) height;
		float f8 = (float) this.viewWidth / (float) this.width;
		float f9 = (float) this.viewHeight / (float) this.height;

			if (instance == null) {
				instance = minecraft.gameRenderer.blitShader;
				instance.setSampler("DiffuseSampler", this.colorTextureId);
			} else {
				for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
					int l = RenderSystem.getShaderTexture(k);
					instance.setSampler("Sampler" + k, l);
				}
			}
			Matrix4f matrix4f = Matrix4f.orthographic((float) width, (float) (-height), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);

			if (instance.MODEL_VIEW_MATRIX != null) {
				instance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
			}

			if (instance.PROJECTION_MATRIX != null) {
				instance.PROJECTION_MATRIX.set(matrix4f);
			}

			instance.apply();

			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, instance.getVertexFormat());
			bufferbuilder.vertex((double) f4, (double) f3, 0.0D).uv(xCropFactor, yCropFactor).color(255, 255, 255, 255)
					.endVertex();
			bufferbuilder.vertex((double) f2, (double) f3, 0.0D).uv(f8 - xCropFactor, yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex((double) f2, (double) f5, 0.0D).uv(f8 - xCropFactor, f9 - yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex((double) f4, (double) f5, 0.0D).uv(xCropFactor, f9 - yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			BufferUploader.draw(bufferbuilder.end());
			instance.clear();

		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

}
