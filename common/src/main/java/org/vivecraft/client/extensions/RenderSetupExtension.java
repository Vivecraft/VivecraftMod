package org.vivecraft.client.extensions;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;

import java.util.Map;

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
     * sets a Fog override, to render things without fog mid level
     *
     * @return this RenderSetup for method chaining
     */
    RenderSetup vivecraft$setUndistorted();

    /**
     * applies the uniform overrides, if any is set
     *
     * @param renderPass RenderPass to apply the unoiform overrides to
     */
    void vivecraft$applyUniformOverrides(RenderPass renderPass);

    record GpuTextureBinding(GpuTextureView texture, GpuSampler sampler) {}
}
