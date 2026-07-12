package org.vivecraft.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public interface SubmitNodeCollectionExtension {
    CustomFeatureRenderer.Storage vivecraft$getLateCustomGeometrySubmits();

    void vivecraft$submitLateCustomGeometry(
        PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer);

    List<SubmitNodeStorage.TextSubmit> vivecraft$getLateTextSubmits();

    void vivecraft$submitLateText(
        PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow,
        Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor);
}
