package org.vivecraft.mixin.client_vr.renderer.rendertype;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.extensions.RenderSetupExtension;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.mixin.client_vr.renderer.GameRendererAccessor;

import java.util.List;
import java.util.Map;

@Mixin(RenderSetup.class)
public class RenderSetupVRMixin implements RenderSetupExtension {

    @Unique
    private FogRenderer.FogMode vivecraft$fogOverride;

    @Unique
    private boolean vivecraft$undistorted;

    @Unique
    private Map<String, GpuTextureBinding> vivecraft$gpuTextures;

    @Override
    @Unique
    public RenderSetup vivecraft$setGpuTextures(Map<String, GpuTextureBinding> gpuTextures) {
        this.vivecraft$gpuTextures = gpuTextures;
        return (RenderSetup) (Object) this;
    }

    @Override
    @Unique
    public RenderSetup vivecraft$setFogOverride(FogRenderer.FogMode fogOverride) {
        this.vivecraft$fogOverride = fogOverride;
        return (RenderSetup) (Object) this;
    }

    @Override
    public RenderSetup vivecraft$setUndistorted() {
        this.vivecraft$undistorted = true;
        return (RenderSetup) (Object) this;
    }

    @Override
    @Unique
    public void vivecraft$applyUniformOverrides(RenderPass renderPass) {
        if (this.vivecraft$fogOverride != null) {
            renderPass.setUniform("Fog", ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getFogRenderer()
                .getBuffer(this.vivecraft$fogOverride));
        }
        if (this.vivecraft$undistorted && RenderSystem.getProjectionType() == ProjectionType.PERSPECTIVE) {
            renderPass.setUniform("Projection", VRShaders.UNDISTORTED_PROJ_BUFFER);
        }
    }

    @ModifyReturnValue(method = "prepareTextures", at = @At("RETURN"))
    private List<PreparedRenderType.Texture> vivecraft$addGpuTextures(List<PreparedRenderType.Texture> original) {
        if (this.vivecraft$gpuTextures != null && !this.vivecraft$gpuTextures.isEmpty()) {
            ImmutableList.Builder<PreparedRenderType.Texture> textures = ImmutableList.builderWithExpectedSize(
                original.size() + this.vivecraft$gpuTextures.size());

            textures.addAll(original);

            for (Map.Entry<String, GpuTextureBinding> entry : this.vivecraft$gpuTextures.entrySet()) {
                textures.add(new PreparedRenderType.Texture(entry.getKey(), entry.getValue().texture(), entry.getValue()
                    .sampler().get()));
            }
            return textures.build();
        } else {
            return original;
        }
    }
}
