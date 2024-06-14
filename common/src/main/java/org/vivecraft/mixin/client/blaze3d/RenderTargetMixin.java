package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.render.VRShaders;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {

    @Unique
    private int vivecraft$texid = -1;
    @Unique
    private boolean vivecraft$linearFilter;
    @Unique
    private boolean vivecraft$useStencil = false;

    @Shadow
    public int frameBufferId;
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
    public abstract void clear(boolean onMacIn);

    @Override
    public void vivecraft$setUseStencil(boolean useStencil) {
        this.vivecraft$useStencil = useStencil;
    }

    @Override
    public boolean vivecraft$getUseStencil() {
        return vivecraft$useStencil;
    }

    @Override
    public void vivecraft$setTextid(int texid) {
        this.vivecraft$texid = texid;
    }

    @Override
    public void vivecraft$isLinearFilter(boolean linearFilter) {
        this.vivecraft$linearFilter = linearFilter;
    }

    @Override
    public String toString() {
        String stringbuilder = "\n" +
            "Size:   " + this.viewWidth + " x " + this.viewHeight + "\n" +
            "FB ID:  " + this.frameBufferId + "\n" +
            "Tex ID: " + this.colorTextureId + "\n";
        return stringbuilder;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", remap = false, ordinal = 0), method = "createBuffers")
    public int vivecraft$genTextureId() {
        if (this.vivecraft$texid == -1) {
            return TextureUtil.generateTextureId();
        } else {
            return this.vivecraft$texid;
        }
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 2)
    public int vivecraft$modifyTexImage2DInternalformat(int internalformat) {
        return vivecraft$useStencil ? GL30.GL_DEPTH32F_STENCIL8 : internalformat;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 6)
    public int vivecraft$modifyTexImage2DFormat(int format) {
        return vivecraft$useStencil ? GL30.GL_DEPTH_STENCIL : format;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), method = "createBuffers", index = 7)
    public int vivecraft$modifyTexImage2DType(int type) {
        return vivecraft$useStencil ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV : type;
    }

    @ModifyConstant(method = "createBuffers", constant = @Constant(intValue = 9728))
    public int vivecraft$changeTextPar(int i) {
        return vivecraft$linearFilter ? GL11.GL_LINEAR : i;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", remap = false, ordinal = 1), method = "createBuffers", index = 1)
    public int vivecraft$modifyGlFramebufferTexture2DAttachment(int attachment) {
        return vivecraft$useStencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment;
    }

    public void vivecraft$blitToScreen(int left, int width, int height, int top, boolean disableBlend, float xCropFactor, float yCropFactor, boolean keepAspect) {
        RenderSystem.assertOnRenderThread();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                this.vivecraft$_blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
            });
        } else {
            this.vivecraft$_blitToScreen(left, width, height, top, disableBlend, xCropFactor, yCropFactor, keepAspect);
        }
    }

    @Override
    public void vivecraft$blitFovReduction(ShaderInstance instance, int width, int height) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.viewport(0, 0, width, height);
        RenderSystem.disableBlend();
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
        Matrix4f matrix4f = new Matrix4f().setOrtho(0, width, height, 0, 1000.0f, 3000.0f);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
        if (instance.MODEL_VIEW_MATRIX != null) {
            instance.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0.0f, 0.0f, -2000.0f));
        }
        if (instance.PROJECTION_MATRIX != null) {
            instance.PROJECTION_MATRIX.set(matrix4f);
        }
        instance.apply();
        float f = width;
        float g = height;
        float h = (float) this.viewWidth / (float) this.width;
        float k = (float) this.viewHeight / (float) this.height;
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, instance.getVertexFormat());
        if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX) {
            bufferBuilder.addVertex(0.0F, g, 0.0F).setUv(0.0F, 0.0F);
            bufferBuilder.addVertex(f, g, 0.0F).setUv(h, 0.0F);
            bufferBuilder.addVertex(f, 0.0F, 0.0F).setUv(h, k);
            bufferBuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, k);
        } else if (instance.getVertexFormat() == DefaultVertexFormat.POSITION_TEX_COLOR) {
            bufferBuilder.addVertex(0.0F, g, 0.0F).setUv(0.0F, 0.0F).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(f, g, 0.0F).setUv(h, 0.0F).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(f, 0.0F, 0.0F).setUv(h, k).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, k).setColor(255, 255, 255, 255);
        } else {
            throw new IllegalStateException("Unexpected vertex format " + instance.getVertexFormat());
        }
        BufferUploader.draw(bufferBuilder.buildOrThrow());
        instance.clear();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }

    @Unique
    private void vivecraft$_blitToScreen(int left, int width, int height, int top, boolean bl, float xCropFactor, float yCropFactor, boolean keepAspect) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.viewport(left, top, width, height);
        if (bl) {
            RenderSystem.disableBlend();
        }

        float drawAspect = (float) width / (float) height;
        float bufferAspect = (float) this.viewWidth / (float) this.viewHeight;

        float xMin = xCropFactor;
        float yMin = yCropFactor;
        float xMax = 1.0F - xCropFactor;
        float yMax = 1.0F - yCropFactor;


        if (keepAspect) {
            if (drawAspect > bufferAspect) {
                // destination is wider than the buffer
                float heightAspect = ((float) height / (float) width) * (0.5F - yCropFactor);

                yMin = 0.5F - heightAspect;
                yMax = 0.5F + heightAspect;
            } else {
                // destination is taller than the buffer
                float widthAspect = drawAspect * (0.5F - xCropFactor);

                xMin = 0.5F - widthAspect;
                xMax = 0.5F + widthAspect;
            }
        }


        ShaderInstance instance = VRShaders.blitAspectShader;
        instance.setSampler("DiffuseSampler", this.colorTextureId);

        instance.apply();

        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, instance.getVertexFormat());
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(xMin, yMin);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F).setUv(xMax, yMin);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F).setUv(xMax, yMax);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F).setUv(xMin, yMax);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        instance.clear();

        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
    }
}
