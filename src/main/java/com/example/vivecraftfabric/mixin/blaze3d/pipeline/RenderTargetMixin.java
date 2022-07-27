package com.example.vivecraftfabric.mixin.blaze3d.pipeline;

import java.nio.IntBuffer;

import com.example.vivecraftfabric.GlStateHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.example.vivecraftfabric.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {

	@Unique
	private int frameBufferId;
	@Unique
	private int texid = -1;
	@Unique
	public String name = "Default";
	@Unique
	private boolean linearFilter;
	@Unique
	public boolean blitLegacy = false;
	@Shadow
	private int depthBufferId;
	@Shadow
	private boolean useDepth;
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
	protected abstract void clear(boolean onMacIn);
	@Shadow
	protected abstract void checkStatus();
	@Shadow
	protected abstract void setFilterMode(int i);

	@Overwrite
	public void blitToScreen(int pWidth, int pHeight, boolean p_83960_) {
		this.blitToScreen(0, pWidth, pHeight, 0, p_83960_, 0.0F, 0.0F, false);
	}
	
	@Override
	public int getDepthBufferId() {
		return depthBufferId;
	}
	
	@Override
	public void setBlitLegacy(boolean b) {
		blitLegacy = b;
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
	
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void createBuffers(int i, int j, boolean bl) {
		RenderSystem.assertOnGameThreadOrInit();
		int k = RenderSystem.maxSupportedTextureSize();
		if (i > 0 && i <= k && j > 0 && j <= k) {
			this.viewWidth = i;
			this.viewHeight = j;
			this.width = i;
			this.height = j;
			this.frameBufferId = GlStateManager.glGenFramebuffers();
			if (this.texid == -1) {
				this.colorTextureId = TextureUtil.generateTextureId();
			} else {
				this.colorTextureId = this.texid;
			}
			if (this.useDepth) {
				this.depthBufferId = TextureUtil.generateTextureId();
				GlStateManager._bindTexture(this.depthBufferId);
				GlStateManager._texParameter(3553, 10241, linearFilter ? GL11.GL_LINEAR : 9728);
				GlStateManager._texParameter(3553, 10240, linearFilter ? GL11.GL_LINEAR : 9728);
				GlStateManager._texParameter(3553, 34892, 0);
				GlStateManager._texParameter(3553, 10242, 33071);
				GlStateManager._texParameter(3553, 10243, 33071);
                GlStateManager._texImage2D(3553, 0, 36013, this.width, this.height, 0, 34041, 36269, null);			}
			if (linearFilter)
				this.setFilterMode(GL11.GL_LINEAR);
			else
				this.setFilterMode(9728);
			GlStateManager._bindTexture(this.colorTextureId);
			GlStateManager._texParameter(3553, 10242, 33071);
			GlStateManager._texParameter(3553, 10243, 33071);
			GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, (IntBuffer)null);
			GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
			GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
			if (this.useDepth) {
				GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, 36096, 3553, this.depthBufferId, 0);
                GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, 36128, 3553, this.depthBufferId, 0);
			}

			this.checkStatus();
			this.clear(bl);
			this.unbindRead();
		} else {
			throw new IllegalArgumentException("Window " + i + "x" + j + " size out of bounds (max. size: " + k + ")");
		}
	}


	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void bindWrite(boolean bl) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				this._bindWrite(true);
			});
		} else {
			this._bindWrite(true);
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void _bindWrite(boolean bl) {
		RenderSystem.assertOnGameThreadOrInit();
		GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
		if (true) {
			GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
		}
	}
	

	public void blitToScreen(int left, int width, int height, int top, boolean disableBlend, float xCropFactor,
			float yCropFactor, boolean keepAspect) {
		RenderSystem.assertOnGameThreadOrInit();
		if (!RenderSystem.isInInitPhase()) {
			RenderSystem.recordRenderCall(() -> {
				this._blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
			});
		} else {
			this._blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
		}

	}

	private void _blitToScreen(int left, int width, int height, int top, boolean bl, float xCropFactor,
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

		if (!blitLegacy) {
			ShaderInstance shaderinstance = minecraft.gameRenderer.blitShader;
			shaderinstance.setSampler("DiffuseSampler", this.colorTextureId);
			Matrix4f matrix4f = Matrix4f.orthographic((float) width, (float) (-height), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);

			if (shaderinstance.MODEL_VIEW_MATRIX != null) {
				shaderinstance.MODEL_VIEW_MATRIX.set(Matrix4f.createTranslateMatrix(0.0F, 0.0F, -2000.0F));
			}

			if (shaderinstance.PROJECTION_MATRIX != null) {
				shaderinstance.PROJECTION_MATRIX.set(matrix4f);
			}

			shaderinstance.apply();

			Tesselator tesselator = RenderSystem.renderThreadTesselator();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex((double) f4, (double) f3, 0.0D).uv(xCropFactor, yCropFactor).color(255, 255, 255, 255)
					.endVertex();
			bufferbuilder.vertex((double) f2, (double) f3, 0.0D).uv(f8 - xCropFactor, yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex((double) f2, (double) f5, 0.0D).uv(f8 - xCropFactor, f9 - yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			bufferbuilder.vertex((double) f4, (double) f5, 0.0D).uv(xCropFactor, f9 - yCropFactor)
					.color(255, 255, 255, 255).endVertex();
			bufferbuilder.end();
			BufferUploader._endInternal(bufferbuilder);
			shaderinstance.clear();
		} else {
			this.bindRead();
			GlStateHelper.disableAlphaTest();
			GlStateManager._disableBlend();
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL43.glLoadIdentity();
			GL11.glOrtho(0.0D, (double) width, (double) height, 0.0D, 1000.0D, 3000.0D);
			GL43.glMatrixMode(5888);
			GL43.glLoadIdentity();
			GL11.glTranslatef(0.0F, 0.0F, -2000);
			GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager._clearDepth(1.0D);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(xCropFactor, yCropFactor);
			GL11.glVertex3f(f4, f3, 0.0F);
			GL11.glTexCoord2f(f8 - xCropFactor, yCropFactor);
			GL11.glVertex3f(f2, f3, 0.0F);
			GL11.glTexCoord2f(f8 - xCropFactor, f9 - yCropFactor);
			GL11.glVertex3f(f2, f5, 0.0F);
			GL11.glTexCoord2f(xCropFactor, f9 - yCropFactor);
			GL11.glVertex3f(f4, f5, 0.0F);
			GL11.glEnd();
			unbindRead();
		}

		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

}
