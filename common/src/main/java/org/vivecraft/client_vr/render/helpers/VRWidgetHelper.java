package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

import java.util.function.Function;

public class VRWidgetHelper {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static final RandomSource RANDOM = RandomSource.create();
    private static final ResourceLocation TRANSPARENT_TEXTURE = new ResourceLocation("vivecraft:transparent");
    public static boolean DEBUG = false;

    /**
     * renders the third person camcorder
     */
    public static void renderVRThirdPersonCamWidget(PoseStack poseStack) {
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

                renderVRCameraWidget(poseStack, -0.748F, -0.438F, -0.06F, scale, RenderPass.THIRD,
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
    public static void renderVRHandheldCameraWidget(PoseStack poseStack) {
        if (DATA_HOLDER.currentPass != RenderPass.CAMERA && DATA_HOLDER.cameraTracker.isVisible()) {
            float scale = 0.25F;

            // bigger when interact ready
            if (DATA_HOLDER.screenCamModule.isActive() && !DATA_HOLDER.cameraTracker.isMoving()) {
                scale *= 1.03F;
            }

            renderVRCameraWidget(poseStack, -0.5F, -0.25F, -0.22F, scale, RenderPass.CAMERA,
                CameraTracker.CAMERA_MODEL, CameraTracker.CAMERA_DISPLAY_MODEL, () -> {
                    if (VREffectsHelper.getNearOpaqueBlock(
                        DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(),
                        ((GameRendererExtension) MC.gameRenderer).vivecraft$getMinClipDistance()) == null)
                    {
                        DATA_HOLDER.vrRenderer.cameraFramebuffer.bindRead();
                        RenderSystem.setShaderTexture(0, DATA_HOLDER.vrRenderer.cameraFramebuffer.getColorTextureId());
                    } else {
                        ShadersHelper.bindTexture(RenderHelper.BLACK_TEXTURE);
                    }
                }, (face) -> face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE);
        }
    }

    /**
     * renders a camera model with screen
     *
     * @param poseStack       PoseStack used for positioning
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
        PoseStack poseStack, float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass,
        ModelResourceLocation model, ModelResourceLocation displayModel, Runnable displayBindFunc,
        Function<Direction, DisplayFace> displayFaceFunc)
    {

        poseStack.pushPose();

        // model position relative to the view position
        Vec3 widgetPosition = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 eye = MC.gameRenderer.getMainCamera().getPosition();
        Vector3f widgetOffset = MathUtils.subtractToVector3f(widgetPosition, eye);

        // orient and scale model
        poseStack.translate(widgetOffset.x, widgetOffset.y, widgetOffset.z);

        Matrix4f rotation = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix();
        poseStack.mulPoseMatrix(rotation);
        poseStack.last().normal().mul(new Matrix3f(rotation));

        scale = scale * DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
        poseStack.scale(scale, scale, scale);

        // show orientation
        if (DEBUG) {
            DebugRenderHelper.renderLocalAxes(poseStack);
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
        ShadersHelper.bindTexture(InventoryMenu.BLOCK_ATLAS);
        if (MC.level != null) {
            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        }
        MC.gameRenderer.lightTexture().turnOnLightLayer();
        MC.gameRenderer.overlayTexture().setupOverlayColor();


        // render camera model
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        MC.getBlockRenderer().getModelRenderer()
            .renderModel(poseStack.last(), bufferBuilder, null, MC.getModelManager().getModel(model), 1.0F, 1.0F, 1.0F,
                combinedLight, OverlayTexture.NO_OVERLAY);
        BufferUploader.drawWithShader(bufferBuilder.end());

        // render camera display
        RenderSystem.disableBlend();
        displayBindFunc.run();
        RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // need to render this manually, because the uvs in the model are for the atlas texture, and not fullscreen
        for (BakedQuad bakedquad : MC.getModelManager().getModel(displayModel).getQuads(null, null, RANDOM)) {
            if (displayFaceFunc.apply(bakedquad.getDirection()) != DisplayFace.NONE &&
                bakedquad.getSprite().contents().name().equals(TRANSPARENT_TEXTURE))
            {
                int[] vertexList = bakedquad.getVertices();
                boolean mirrored = displayFaceFunc.apply(bakedquad.getDirection()) == DisplayFace.MIRROR;
                int step = vertexList.length / 4;
                bufferBuilder.vertex(
                        poseStack.last().pose(),
                        Float.intBitsToFloat(vertexList[0]),
                        Float.intBitsToFloat(vertexList[1]),
                        Float.intBitsToFloat(vertexList[2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(mirrored ? 1.0F : 0.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferBuilder.vertex(
                        poseStack.last().pose(),
                        Float.intBitsToFloat(vertexList[step]),
                        Float.intBitsToFloat(vertexList[step + 1]),
                        Float.intBitsToFloat(vertexList[step + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(mirrored ? 1.0F : 0.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferBuilder.vertex(
                        poseStack.last().pose(),
                        Float.intBitsToFloat(vertexList[step * 2]),
                        Float.intBitsToFloat(vertexList[step * 2 + 1]),
                        Float.intBitsToFloat(vertexList[step * 2 + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(mirrored ? 0.0F : 1.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(0.0F, 1.0F, 0.0F).endVertex();
                bufferBuilder.vertex(
                        poseStack.last().pose(),
                        Float.intBitsToFloat(vertexList[step * 3]),
                        Float.intBitsToFloat(vertexList[step * 3 + 1]),
                        Float.intBitsToFloat(vertexList[step * 3 + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(mirrored ? 0.0F : 1.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(0.0F, 1.0F, 0.0F).endVertex();
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());

        MC.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableBlend();

        poseStack.popPose();
    }

    public enum DisplayFace {
        NONE,
        NORMAL,
        MIRROR
    }
}
