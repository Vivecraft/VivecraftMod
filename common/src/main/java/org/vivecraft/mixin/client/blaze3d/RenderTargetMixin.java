package org.vivecraft.mixin.client.blaze3d;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.vivecraft.client.extensions.RenderTargetExtension;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {

    @Unique
    private int vivecraft$texId = -1;
    @Unique
    private boolean vivecraft$linearFilter;
    @Unique
    private boolean vivecraft$mipmaps;
    @Unique
    private boolean vivecraft$stencil = false;

    @Override
    @Unique
    public void vivecraft$setStencil(boolean stencil) {
        this.vivecraft$stencil = stencil;
    }

    @Override
    @Unique
    public boolean vivecraft$hasStencil() {
        return this.vivecraft$stencil;
    }

    @Override
    @Unique
    public void vivecraft$setTexId(int texId) {
        this.vivecraft$texId = texId;
    }

    @Override
    @Unique
    public void vivecraft$setLinearFilter(boolean linearFilter) {
        this.vivecraft$linearFilter = linearFilter;
    }

    @Override
    @Unique
    public void vivecraft$setMipmaps(boolean mipmaps) {
        this.vivecraft$mipmaps = mipmaps;
    }

    @Override
    @Unique
    public boolean vivecraft$hasMipmaps() {
        return this.vivecraft$mipmaps;
    }

    @WrapOperation(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/TextureUtil;generateTextureId()I", remap = false, ordinal = 0))
    public int vivecraft$fixedTextureId(Operation<Integer> original) {
        return this.vivecraft$texId == -1 ? original.call() : this.vivecraft$texId;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), index = 2)
    public int vivecraft$modifyTexImage2DInternalformat(int internalformat) {
        return this.vivecraft$stencil ? GL30.GL_DEPTH32F_STENCIL8 : internalformat;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), index = 6)
    public int vivecraft$modifyTexImage2DFormat(int format) {
        return this.vivecraft$stencil ? GL30.GL_DEPTH_STENCIL : format;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", remap = false, ordinal = 0), index = 7)
    public int vivecraft$modifyTexImage2DType(int type) {
        return this.vivecraft$stencil ? GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV : type;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setFilterMode(I)V"))
    public int vivecraft$linearFiltering(int filterMode) {
        return this.vivecraft$linearFilter ? GL11.GL_LINEAR : filterMode;
    }

    @ModifyArg(method = "setFilterMode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V", remap = false, ordinal = 0), index = 2)
    public int vivecraft$modifyTextureMinFilter(int attachment) {
        if (this.vivecraft$mipmaps) {
            return attachment == GL11.GL_LINEAR ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_NEAREST_MIPMAP_NEAREST;
        } else {
            return attachment;
        }
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", remap = false, ordinal = 1), index = 1)
    public int vivecraft$modifyGlFramebufferTexture2DAttachment(int attachment) {
        return this.vivecraft$stencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : attachment;
    }
}
