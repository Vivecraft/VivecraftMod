package org.vivecraft.client.extensions;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public interface RenderSetupExtension {

    /**
     * allows binding raw GpuTextureBinding to samplers, instead of resourcelocations
     *
     * @param gpuTextures sampler/texture map to apply
     * @return this RenderSetup for method chaining
     */
    RenderSetup vivecraft$setGpuTextures(Map<String, GpuTextureBinding> gpuTextures);

    /**
     * sets a Fog override, to render things without fog mid level
     *
     * @param fogOverride FogMode to use as override
     * @return this RenderSetup for method chaining
     */
    RenderSetup vivecraft$setFogOverride(FogRenderer.FogMode fogOverride);

    /**
     * @return the fog override, if one is set
     */
    @Nullable
    FogRenderer.FogMode vivecraft$getFogOverride();

    /**
     * sets a Fog override, to render things without fog mid level
     *
     * @return this RenderSetup for method chaining
     */
    RenderSetup vivecraft$setUndistorted();

    /**
     * @return if this pass should render undistorted
     */
    boolean vivecraft$getUndistorted();

    record GpuTextureBinding(GpuTextureView texture, Supplier<GpuSampler> sampler) {}
}
