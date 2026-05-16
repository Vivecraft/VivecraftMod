package org.vivecraft.client.extensions;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.rendertype.RenderSetup;

import java.util.Map;

public interface RenderSetupExtension {

    RenderSetup vivecraft$setGpuTextures(Map<String, GpuTextureBinding> gpuTextures);

    record GpuTextureBinding(GpuTextureView texture, GpuSampler sampler) {}
}
