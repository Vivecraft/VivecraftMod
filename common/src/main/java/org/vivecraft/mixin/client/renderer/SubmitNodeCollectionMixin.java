package org.vivecraft.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.extensions.SubmitNodeCollectionExtension;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin implements SubmitNodeCollectionExtension {

    @Shadow
    @Final
    public SimpleFeatureRenderPhase afterTerrain;
    @Shadow
    @Final
    public SimpleFeatureRenderPhase solid;

    @Unique
    @Override
    public void vivecraft$submitLateCustomGeometry(
        PoseStack poseStack, RenderType renderType,
        SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer)
    {
        CustomFeatureRenderer.Submit submit = new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType,
            customGeometryRenderer);
        // only rendertypoes with blending need to be handled
        if (renderType.hasBlending()) {
            this.afterTerrain.submit(submit);
        } else {
            this.solid.submit(submit);
        }
    }

    @Unique
    @Override
    public void vivecraft$submitLateText(
        PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow,
        Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor)
    {
        this.afterTerrain.submit(
            new TextFeatureRenderer.Submit(new Matrix4f(poseStack.last().pose()), x, y, string, dropShadow, displayMode,
                lightCoords, color, backgroundColor, outlineColor));
    }
}
