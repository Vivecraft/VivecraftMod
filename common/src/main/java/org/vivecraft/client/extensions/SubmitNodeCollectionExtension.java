package org.vivecraft.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface SubmitNodeCollectionExtension {
    CustomFeatureRenderer.Storage vivecraft$getLateCustomGeometrySubmits();

    void vivecraft$submitLateCustomGeometry(
        PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer);
}
