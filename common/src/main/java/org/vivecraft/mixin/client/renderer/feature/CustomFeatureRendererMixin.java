package org.vivecraft.mixin.client.renderer.feature;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.extensions.CustomFeatureRendererExtension;
import org.vivecraft.client.extensions.SubmitNodeCollectionExtension;

import java.util.List;
import java.util.Map;

@Mixin(CustomFeatureRenderer.class)
public class CustomFeatureRendererMixin implements CustomFeatureRendererExtension {

    @Unique
    @Override
    public void vivecraft$renderLate(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource) {
        Map<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> map = ((SubmitNodeCollectionExtension) nodeCollection).vivecraft$getLateCustomGeometrySubmits().translucentCustomGeometrySubmits;

        // only need to process the translucent ones, solds are not late
        for (Map.Entry<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> entry : map.entrySet()) {
            for (SubmitNodeStorage.CustomGeometrySubmit customGeometrySubmit : entry.getValue()) {
                customGeometrySubmit.customGeometryRenderer()
                    .render(customGeometrySubmit.pose(), bufferSource.getBuffer(entry.getKey()));
            }
        }
    }
}
