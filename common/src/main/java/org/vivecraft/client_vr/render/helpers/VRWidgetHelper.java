package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

import java.util.function.Function;

public class VRWidgetHelper {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static final RandomSource RANDOM = RandomSource.create();
    private static final ResourceLocation TRANSPARENT_TEXTURE = ResourceLocation.parse("vivecraft:transparent");
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
                if (DATA_HOLDER.interactTracker.isInCamera() && !VRHotkeys.isMovingThirdPersonCam()) {
                    scale *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, scale, RenderPass.THIRD,
                    ClientDataHolderVR.THIRD_PERSON_CAMERA_MODEL, ClientDataHolderVR.THIRD_PERSON_CAMERA_DISPLAY_MODEL,
                    () -> {
                        DATA_HOLDER.vrRenderer.framebufferMR.bindRead();
                        RenderSystem.setShaderTexture(0, DATA_HOLDER.vrRenderer.framebufferMR.getColorTextureId());
                    }, (face) -> {
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
            if (DATA_HOLDER.interactTracker.isInHandheldCamera() && !DATA_HOLDER.cameraTracker.isMoving()) {
                scale *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, scale, RenderPass.CAMERA,
                CameraTracker.CAMERA_MODEL, CameraTracker.CAMERA_DISPLAY_MODEL, () -> {
                    if (VREffectsHelper.getNearOpaqueBlock(
                        DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(),
                        ((GameRendererExtension) MC.gameRenderer).vivecraft$getMinClipDistance()) == null)
                    {
                        DATA_HOLDER.vrRenderer.cameraFramebuffer.bindRead();
                        RenderSystem.setShaderTexture(0, DATA_HOLDER.vrRenderer.cameraFramebuffer.getColorTextureId());
                    } else {
                        RenderSystem.setShaderTexture(0, RenderHelper.BLACK_TEXTURE);
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
     * @param displayBindFunc function that binds the camera buffer, or something else
     * @param displayFaceFunc function that specifies if the view should be mirrored, normal or not shown at all
     */
    public static void renderVRCameraWidget(
        float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, ResourceLocation model,
        ResourceLocation displayModel, Runnable displayBindFunc, Function<Direction, DisplayFace> displayFaceFunc)
    {

        PoseStack poseStack = new PoseStack();

        // model position relative to the view position
        Vec3 widgetPosition = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 eye = MC.gameRenderer.getMainCamera().getPosition();
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

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        // we use block models, so the camera texture is on the regular block atlas
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        if (MC.level != null) {
            RenderSystem.setShader(CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL);
        } else {
            RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        }
        MC.gameRenderer.lightTexture().turnOnLightLayer();

        // render camera model
        BufferBuilder bufferBuilder;

        ITEM_STACK_RENDER_STATE.clear();
        MC.getModelManager().getItemModel(model)
            .update(ITEM_STACK_RENDER_STATE, ItemStack.EMPTY, MC.getItemModelResolver(), ItemDisplayContext.GROUND,
                null, null, 0);

        if (!ITEM_STACK_RENDER_STATE.isEmpty() && ITEM_STACK_RENDER_STATE.layers[0].model != null) {
            bufferBuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            MC.getBlockRenderer().getModelRenderer()
                .renderModel(poseStack.last(), bufferBuilder, null, ITEM_STACK_RENDER_STATE.layers[0].model, 1.0F, 1.0F,
                    1.0F, combinedLight, OverlayTexture.NO_OVERLAY);
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        }

        // render camera display
        RenderSystem.disableBlend();
        displayBindFunc.run();
        RenderSystem.setShader(CoreShaders.RENDERTYPE_ENTITY_SOLID);

        ITEM_STACK_RENDER_STATE.clear();
        MC.getModelManager().getItemModel(displayModel)
            .update(ITEM_STACK_RENDER_STATE, ItemStack.EMPTY, MC.getItemModelResolver(), ItemDisplayContext.GROUND,
                null, null, 0);

        if (!ITEM_STACK_RENDER_STATE.isEmpty() && ITEM_STACK_RENDER_STATE.layers[0].model != null) {
            bufferBuilder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

            // need to render this manually, because the uvs in the model are for the atlas texture, and not fullscreen
            for (BakedQuad bakedquad : ITEM_STACK_RENDER_STATE.layers[0].model.getQuads(null, null, RANDOM)) {
                if (displayFaceFunc.apply(bakedquad.getDirection()) != DisplayFace.NONE &&
                    bakedquad.getSprite().contents().name().equals(TRANSPARENT_TEXTURE))
                {
                    int[] vertexList = bakedquad.getVertices();
                    boolean mirrored = displayFaceFunc.apply(bakedquad.getDirection()) == DisplayFace.MIRROR;
                    int step = vertexList.length / 4;
                    bufferBuilder.addVertex(
                            poseStack.last().pose(),
                            Float.intBitsToFloat(vertexList[0]),
                            Float.intBitsToFloat(vertexList[1]),
                            Float.intBitsToFloat(vertexList[2]))
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 1.0F : 0.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    bufferBuilder.addVertex(
                            poseStack.last().pose(),
                            Float.intBitsToFloat(vertexList[step]),
                            Float.intBitsToFloat(vertexList[step + 1]),
                            Float.intBitsToFloat(vertexList[step + 2]))
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 1.0F : 0.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    bufferBuilder.addVertex(
                            poseStack.last().pose(),
                            Float.intBitsToFloat(vertexList[step * 2]),
                            Float.intBitsToFloat(vertexList[step * 2 + 1]),
                            Float.intBitsToFloat(vertexList[step * 2 + 2]))
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 0.0F : 1.0F, 0.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                    bufferBuilder.addVertex(
                            poseStack.last().pose(),
                            Float.intBitsToFloat(vertexList[step * 3]),
                            Float.intBitsToFloat(vertexList[step * 3 + 1]),
                            Float.intBitsToFloat(vertexList[step * 3 + 2]))
                        .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                        .setUv(mirrored ? 0.0F : 1.0F, 1.0F)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(0.0F, 1.0F, 0.0F);
                }
            }
            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        }

        MC.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableBlend();
    }

    public enum DisplayFace {
        NONE,
        NORMAL,
        MIRROR
    }
}
