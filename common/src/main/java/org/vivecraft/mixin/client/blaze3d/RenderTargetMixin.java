package org.vivecraft.mixin.client.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.GpuFormat;
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
    public void vivecraft$setMipmaps(boolean mipmaps) {
        this.vivecraft$mipmaps = mipmaps;
    }

    @Override
    @Unique
    public boolean vivecraft$hasMipmaps() {
        return this.vivecraft$mipmaps;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/renderpearl/api/GpuFormat;IIII)Lcom/mojang/renderpearl/api/textures/GpuTexture;"), index = 6)
    private int vivecraft$mipLevels(
        Supplier<String> labelSupplier, int usageFlags, GpuFormat textureFormat, int width, int height,
        int depthLayers, int mipLevels)
    {
        return this.vivecraft$mipmaps && !textureFormat.hasDepthAspect() ?
            Math.max(Mth.log2(this.width), Mth.log2(this.height)) : mipLevels;
    }
}
