package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

import java.util.function.Function;
import java.util.function.Supplier;

public class VRWidgetHelper {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static final RandomSource RANDOM = RandomSource.create();
    private static final Identifier TRANSPARENT_TEXTURE = Identifier.parse("vivecraft:transparent");
    private static final ItemStackRenderState ITEM_STACK_RENDER_STATE = new ItemStackRenderState();
    public static boolean DEBUG = false;

    /**
     * renders the third person camcorder
     */
    public static void renderVRThirdPersonCamWidget() {
        if (!DATA_HOLDER.vrSettings.mixedRealityRenderCameraModel) return;
        if (DATA_HOLDER.currentPass == RenderPass.LEFT || DATA_HOLDER.currentPass == RenderPass.RIGHT) {
            if ((DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ) && (!DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera || !DATA_HOLDER.cameraTracker.isVisible()))
            {
                float scale = 0.35F;

                // bigger when interact ready
                if (DATA_HOLDER.thirdCamModule.isActive() && !VRHotkeys.isMovingThirdPersonCam()) {
                    scale *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, scale, RenderPass.THIRD,
                    ClientDataHolderVR.THIRD_PERSON_CAMERA_MODEL, ClientDataHolderVR.THIRD_PERSON_CAMERA_DISPLAY_MODEL,
                    () -> DATA_HOLDER.vrRenderer.framebufferMR.getColorTextureView(), (face) -> {
                        if (face == Direction.NORTH) {
                            return DisplayFace.MIRROR;
                        } else {
                            return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
                        }
                    });
            }
        }
    }

    /**
     * renders the screenshot camera
     */
    public static void renderVRHandheldCameraWidget() {
        if (DATA_HOLDER.currentPass != RenderPass.CAMERA && DATA_HOLDER.cameraTracker.isVisible()) {
            float scale = 0.25F;

            // bigger when interact ready
            if (DATA_HOLDER.screenCamModule.isActive() && !DATA_HOLDER.cameraTracker.isMoving()) {
                scale *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, scale, RenderPass.CAMERA,
                CameraTracker.CAMERA_MODEL, CameraTracker.CAMERA_DISPLAY_MODEL, () -> {
                    if (VREffectsHelper.getNearOpaqueBlock(
                        DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(),
                        MC.gameRenderer.getMainCamera().projection.zNear()) == null)
                    {
                        return DATA_HOLDER.vrRenderer.cameraFramebuffer.getColorTextureView();
                    } else {
                        return RenderHelper.getGpuTexture(RenderHelper.BLACK_TEXTURE);
                    }
                }, (face) -> face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE);
        }
    }

    /**
     * renders a camera model with screen
     *
     * @param offsetX         model x offset
     * @param offsetY         model y offset
     * @param offsetZ         model z offset
     * @param scale           size of the model
     * @param renderPass      RenderPass this camera shows, the camera will be placed there
     * @param model           camera model to render
     * @param displayModel    model of the display that shows the camera view
     * @param displaySupFunc  function that supplies the camera buffer, or something else
     * @param displayFaceFunc function that specifies if the view should be mirrored, normal or not shown at all
     */
    public static void renderVRCameraWidget(
        float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, Identifier model,
        Identifier displayModel, Supplier<GpuTextureView> displaySupFunc,
        Function<Direction, DisplayFace> displayFaceFunc)
    {

        PoseStack poseStack = new PoseStack();

        // model position relative to the view position
        Vec3 widgetPosition = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 eye = MC.gameRenderer.getMainCamera().position();
        Vector3f widgetOffset = MathUtils.subtractToVector3f(widgetPosition, eye);

        // orient and scale model
        poseStack.translate(widgetOffset.x, widgetOffset.y, widgetOffset.z);

        Matrix4f rotation = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix();
        poseStack.last().pose().mul(rotation);
        poseStack.last().normal().mul(new Matrix3f(rotation));

        scale = scale * DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
        poseStack.scale(scale, scale, scale);

        // show orientation
        if (DEBUG) {
            DebugRenderHelper.renderLocalAxes(poseStack.last().pose());
        }

        // apply model offset
        poseStack.translate(offsetX, offsetY, offsetZ);

        // lighting for the model
        BlockPos blockpos = BlockPos.containing(
            DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition());
        int combinedLight = ClientUtils.getCombinedLightWithMin(MC.level, blockpos, 0);

        // render camera model
        VertexConsumer consumer;

        ITEM_STACK_RENDER_STATE.clear();
        MC.getModelManager().getItemModel(model)
            .update(ITEM_STACK_RENDER_STATE, ItemStack.EMPTY, MC.getItemModelResolver(), ItemDisplayContext.GROUND,
                null, null, 0);

        if (!ITEM_STACK_RENDER_STATE.isEmpty() && !ITEM_STACK_RENDER_STATE.layers[0].prepareQuadList().isEmpty()) {
            // we use block models, so the camera texture is on the regular block atlas
            RenderType renderType = RenderTypes.entityCutout(TextureAtlas.LOCATION_ITEMS);
            // TODO 26.1 figure out how to render items
//            ItemRenderer.renderItem(ItemDisplayContext.GROUND, poseStack, MC.renderBuffers().bufferSource(),
//                combinedLight, OverlayTexture.NO_OVERLAY, new int[]{},
//                ITEM_STACK_RENDER_STATE.layers[0].prepareQuadList(), renderType, ItemStackRenderState.FoilType.NONE);

            MC.renderBuffers().bufferSource().endBatch(renderType);
        }

        // render camera display
        ITEM_STACK_RENDER_STATE.clear();
        MC.getModelManager().getItemModel(displayModel)
            .update(ITEM_STACK_RENDER_STATE, ItemStack.EMPTY, MC.getItemModelResolver(), ItemDisplayContext.GROUND,
                null, null, 0);

        if (!ITEM_STACK_RENDER_STATE.isEmpty() && !ITEM_STACK_RENDER_STATE.layers[0].prepareQuadList().isEmpty()) {
            RenderType renderType = VRRenderTypes.entitySolidNoCardinalLight(displaySupFunc.get(), true);
            consumer = MC.renderBuffers().bufferSource().getBuffer(renderType);

            // need to render this manually, because the uvs in the model are for the atlas texture, and not fullscreen
            for (BakedQuad bakedquad : ITEM_STACK_RENDER_STATE.layers[0].prepareQuadList()) {
                if (displayFaceFunc.apply(bakedquad.direction()) != DisplayFace.NONE &&
                    bakedquad.materialInfo().sprite().contents().name().equals(TRANSPARENT_TEXTURE))
                {
                    boolean mirrored = displayFaceFunc.apply(bakedquad.direction()) == DisplayFace.MIRROR;
                    consumer.addVertex(
                            poseStack.last().pose(),
                            bakedquad.position(0).x(),
                            bakedquad.position(0).y(),
                            bakedquad.position(0).z())
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 1.0F : 0.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    consumer.addVertex(
                            poseStack.last().pose(),
                            bakedquad.position(1).x(),
                            bakedquad.position(1).y(),
                            bakedquad.position(1).z())
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 1.0F : 0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    consumer.addVertex(
                            poseStack.last().pose(),
                            bakedquad.position(2).x(),
                            bakedquad.position(2).y(),
                            bakedquad.position(2).z())
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 0.0F : 1.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    consumer.addVertex(
                            poseStack.last().pose(),
                            bakedquad.position(3).x(),
                            bakedquad.position(3).y(),
                            bakedquad.position(3).z())
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 0.0F : 1.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                }
            }
            MC.renderBuffers().bufferSource().endBatch(renderType);
        }
    }

    public enum DisplayFace {
        NONE,
        NORMAL,
        MIRROR
    }
}
