package org.vivecraft.mixin.client_vr.renderer.rendertype;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.RenderSetupExtension;

import java.util.HashMap;
import java.util.Map;

@Mixin(RenderSetup.class)
public class RenderSetupVRMixin implements RenderSetupExtension {

    @Unique
    private Map<String, GpuTextureBinding> vivecraft$gpuTextures;

    @Override
    public RenderSetup vivecraft$setGpuTextures(Map<String, GpuTextureBinding> gpuTextures) {
        this.vivecraft$gpuTextures = gpuTextures;
        return (RenderSetup) (Object) this;
    }

    @ModifyReturnValue(method = "getTextures", at = @At("RETURN"))
    private Map<String, RenderSetup.TextureAndSampler> vivecraft$addGpuTextures(
        Map<String, RenderSetup.TextureAndSampler> original)
    {
        if (this.vivecraft$gpuTextures != null && !this.vivecraft$gpuTextures.isEmpty()) {
            if (original.isEmpty()) {
                // if it is empty it is unmodifiable
                original = new HashMap<>();
            }
            for (Map.Entry<String, GpuTextureBinding> entry : this.vivecraft$gpuTextures.entrySet()) {
                original.put(entry.getKey(),
                    new RenderSetup.TextureAndSampler(entry.getValue().texture(), entry.getValue().sampler()));
            }
        }
        return original;
    }
}
