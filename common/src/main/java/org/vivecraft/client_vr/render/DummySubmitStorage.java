package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.FeatureRenderPhase;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DummySubmitStorage extends SubmitNodeCollection implements SubmitNodeCollector {

    public static final DummySubmitStorage INSTANCE = new DummySubmitStorage();

    private DummySubmitStorage() {}

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {}

    @Override
    public void submitNameTag(
        PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name, boolean seeThrough,
        int lightCoords, CameraRenderState camera)
    {}

    @Override
    public void submitText(
        PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow,
        Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor)
    {}

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {}

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {}

    @Override
    public <S> void submitModel(
        Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords,
        int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor,
        ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay)
    {}

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState, int outlineColor)
    {}

    @Override
    public void submitBlockModel(
        PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> modelParts, int[] tintLayers,
        int lightCoords, int overlayCoords, int outlineColor)
    {}

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, int progress) {}

    @Override
    public void submitShapeOutline(
        PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width,
        boolean afterTerrain)
    {}

    @Override
    public void submitItem(
        PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor,
        int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType)
    {}

    @Override
    public void submitCustomGeometry(
        PoseStack poseStack, RenderType renderType,
        CustomGeometryRenderer customGeometryRenderer)
    {}

    @Override
    public void submitQuadParticleGroup(QuadParticleRenderState particles) {}

    @Override
    public void submitGizmoPrimitives(DrawableGizmoPrimitives.Group group, CameraRenderState camera, boolean onTop) {}

    @Override
    public List<FeatureRenderPhase<?>> allPhases() {
        return super.allPhases();
    }

    @Override
    public void submitModelPart(
        ModelPart modelPart, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords,
        TextureAtlasSprite sprite, int tintedColor, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        int outlineColor)
    {}

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }
}
