package org.vivecraft.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.SubmitNodeCollectionExtension;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin implements SubmitNodeCollectionExtension {
    @Shadow
    public abstract void submitCustomGeometry(
        PoseStack poseStack, RenderType renderType,
        SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer);

    @Shadow
    private boolean wasUsed;
    @Unique
    private final CustomFeatureRenderer.Storage vivecraft$lateCustomGeometrySubmits = new CustomFeatureRenderer.Storage();

    @Unique
    @Override
    public CustomFeatureRenderer.Storage vivecraft$getLateCustomGeometrySubmits() {
        return this.vivecraft$lateCustomGeometrySubmits;
    }

    @Unique
    @Override
    public void vivecraft$submitLateCustomGeometry(
        PoseStack poseStack, RenderType renderType,
        SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer)
    {
        // only rendertypoes with blending need to be handled
        if (renderType.hasBlending()) {
            this.wasUsed = true;
            this.vivecraft$lateCustomGeometrySubmits.add(poseStack, renderType, customGeometryRenderer);
        } else {
            submitCustomGeometry(poseStack, renderType, customGeometryRenderer);
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void vivecraft$clearLateCustomGeometry(CallbackInfo ci) {
        this.vivecraft$lateCustomGeometrySubmits.clear();
    }

    @Inject(method = "endFrame", at = @At("TAIL"))
    private void vivecraft$endLateCustomGeometry(CallbackInfo ci) {
        this.vivecraft$lateCustomGeometrySubmits.endFrame();
    }
}
