package com.example.examplemod.mixin.blaze3d.pipeline;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.example.examplemod.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
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

    public boolean blitLegacy = false;
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
    
	private String name;
	private String frameBufferId;

    @Shadow
    protected abstract void _blitToScreen(int i, int j, boolean bl);

    @Shadow
    public abstract void unbindRead();

    @Shadow
    public abstract void bindRead();

    public void blitToScreen(int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        if (!RenderSystem.isInInitPhase()) {
            RenderSystem.recordRenderCall(() -> {
                this._blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
            });
        } else {
            this._blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
        }

    }


    private void _blitToScreen(int left, int width, int height, int top, boolean bl, float xCropFactor, float yCropFactor, boolean keepAspect) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
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
            bufferbuilder.vertex((double) f4, (double) f3, 0.0D).uv(xCropFactor, yCropFactor).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex((double) f2, (double) f3, 0.0D).uv(f8 - xCropFactor, yCropFactor).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex((double) f2, (double) f5, 0.0D).uv(f8 - xCropFactor, f9 - yCropFactor).color(255, 255, 255, 255).endVertex();
            bufferbuilder.vertex((double) f4, (double) f5, 0.0D).uv(xCropFactor, f9 - yCropFactor).color(255, 255, 255, 255).endVertex();
            bufferbuilder.end();
            BufferUploader._endInternal(bufferbuilder);
            shaderinstance.clear();
        } else {
            this.bindRead();
            //GlStateManager.disableAlphaTest();
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

}
