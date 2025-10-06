package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client.extensions.RenderTargetExtension;

import java.util.function.Supplier;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtension {

    @Shadow
    public int width;
    @Shadow
    public int height;
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

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/textures/TextureFormat;III)Lcom/mojang/blaze3d/textures/GpuTexture;", remap = false), index = 4, remap = true)
    private int vivecraft$mipLevels(
        Supplier<String> labelSupplier, TextureFormat textureFormat, int width, int height, int mipLevels)
    {
        return this.vivecraft$mipmaps && !textureFormat.hasDepthAspect() ?
            Math.max(Mth.log2(this.width), Mth.log2(this.height)) : mipLevels;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setFilterMode(Lcom/mojang/blaze3d/textures/FilterMode;Z)V"))
    private FilterMode vivecraft$linearFilter(FilterMode filterMode) {
        return this.vivecraft$linearFilter ? FilterMode.LINEAR : filterMode;
    }

    @ModifyArg(method = "setFilterMode(Lcom/mojang/blaze3d/textures/FilterMode;Z)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;setTextureFilter(Lcom/mojang/blaze3d/textures/FilterMode;Z)V", remap = false), index = 1, remap = true)
    private boolean vivecraft$useMipMaps(boolean mipmaps) {
        return mipmaps || this.vivecraft$mipmaps;
    }
}
