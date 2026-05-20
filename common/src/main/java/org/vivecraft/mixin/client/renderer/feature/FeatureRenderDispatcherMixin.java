package org.vivecraft.mixin.client.renderer.feature;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.CustomFeatureRendererExtension;
import org.vivecraft.client.extensions.FeatureRenderDispatcherExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(FeatureRenderDispatcher.class)
public class FeatureRenderDispatcherMixin implements FeatureRenderDispatcherExtension {

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Shadow
    @Final
    private CustomFeatureRenderer customFeatureRenderer;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Unique
    @Override
    public void vivecraft$renderLate() {
        for (SubmitNodeCollection collection : this.submitNodeStorage.getSubmitsPerOrder().values()) {
            ((CustomFeatureRendererExtension) this.customFeatureRenderer).vivecraft$renderLate(collection,
                this.bufferSource);
        }
    }

    @Inject(method = "renderAllFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentParticles()V"))
    private void vivecraft$renderLateFeatures(CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            this.vivecraft$renderLate();
        }
    }
}
