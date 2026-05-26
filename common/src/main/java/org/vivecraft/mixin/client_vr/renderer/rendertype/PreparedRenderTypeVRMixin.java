package org.vivecraft.mixin.client_vr.renderer.rendertype;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.PreparedRenderTypeExtension;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.mixin.client_vr.renderer.GameRendererAccessor;

@Mixin(PreparedRenderType.class)
public class PreparedRenderTypeVRMixin implements PreparedRenderTypeExtension {

    @Unique
    private FogRenderer.FogMode vivecraft$fogOverride;

    @Unique
    private boolean vivecraft$undistorted;

    @Override
    @Unique
    public void vivecraft$setFogOverride(FogRenderer.FogMode fogOverride) {
        this.vivecraft$fogOverride = fogOverride;
    }

    @Override
    public void vivecraft$setUndistorted() {
        this.vivecraft$undistorted = true;
    }

    @Inject(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    public void vivecraft$applyUniformOverrides(CallbackInfo ci, @Local RenderPass renderPass) {
        if (this.vivecraft$fogOverride != null) {
            renderPass.setUniform("Fog", ((GameRendererAccessor) Minecraft.getInstance().gameRenderer).getFogRenderer()
                .getBuffer(this.vivecraft$fogOverride));
        }
        if (this.vivecraft$undistorted && RenderSystem.getProjectionType() == ProjectionType.PERSPECTIVE) {
            renderPass.setUniform("Projection", VRShaders.UNDISTORTED_PROJ_BUFFER);
        }
    }
}
