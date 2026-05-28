package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.render.renderstates.CameraWidgetRenderState;
import org.vivecraft.client_vr.render.rendertypes.VRRenderTypes;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class VRWidgetHelper {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static final Identifier TRANSPARENT_TEXTURE = Identifier.parse("vivecraft:transparent");
    public static boolean DEBUG = false;

    /**
     * renders the third person camcorder
     *
     * @param output      SubmitNodeCollector to submit the rendercall to
     * @param cameraState camera view renderstate to get the camera position
     * @param widgetState camera widget renderstate to use for rendering
     */
    public static void renderVRThirdPersonCamWidget(
        SubmitNodeCollector output, CameraRenderState cameraState, CameraWidgetRenderState widgetState,
        PoseStack poseStack)
    {
        if (widgetState.visible) {
            renderVRCameraWidget(output, cameraState, widgetState, poseStack,
                () -> DATA_HOLDER.vrRenderer.framebufferMR.getColorTextureView(), (face) -> {
                    if (face == Direction.NORTH) {
                        return DisplayFace.MIRROR;
                    } else {
                        return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
                    }
                });
        }
    }

    /**
     * renders the screenshot camera
     *
     * @param output      SubmitNodeCollector to submit the rendercall to
     * @param cameraState camera view renderstate to get the camera position
     * @param widgetState camera widget renderstate to use for rendering
     */
    public static void renderVRHandheldCameraWidget(
        SubmitNodeCollector output, CameraRenderState cameraState, CameraWidgetRenderState widgetState,
        PoseStack poseStack)
    {
        if (widgetState.visible) {
            renderVRCameraWidget(output, cameraState, widgetState, poseStack,
                () -> {
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
     * extracts the third person camcorder
     *
     * @param cameraState renderstate to write into
     * @param player      to get the current level for lighting
     */
    public static void extractVRThirdPersonCamWidget(
        CameraWidgetRenderState cameraState, @Nullable LocalPlayer player)
    {
        cameraState.visible = DATA_HOLDER.vrSettings.mixedRealityRenderCameraModel &&
            (DATA_HOLDER.currentPass == RenderPass.LEFT || DATA_HOLDER.currentPass == RenderPass.RIGHT) &&
            (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY ||
                DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON
            ) && (!DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera || !DATA_HOLDER.cameraTracker.isVisible());

        if (cameraState.visible) {
            float scale = 0.35F;

            // bigger when interact ready
            if (DATA_HOLDER.thirdCamModule.isActive() && !VRHotkeys.isMovingThirdPersonCam()) {
                scale *= 1.03F;
            }

            extractVRCameraWidget(-0.748F, -0.438F, -0.06F, scale, RenderPass.THIRD,
                ClientDataHolderVR.THIRD_PERSON_CAMERA_MODEL, ClientDataHolderVR.THIRD_PERSON_CAMERA_DISPLAY_MODEL,
                cameraState, player);
        }
    }

    /**
     * extracts the screenshot camera
     *
     * @param cameraState renderstate to write into
     * @param player      to get the current level for lighting
     */
    public static void extractVRHandheldCameraWidget(
        CameraWidgetRenderState cameraState, @Nullable LocalPlayer player)
    {
        cameraState.visible = DATA_HOLDER.currentPass != RenderPass.CAMERA && DATA_HOLDER.cameraTracker.isVisible();
        if (cameraState.visible) {
            float scale = 0.25F;

            // bigger when interact ready
            if (DATA_HOLDER.screenCamModule.isActive() && !DATA_HOLDER.cameraTracker.isMoving()) {
                scale *= 1.03F;
            }

            extractVRCameraWidget(-0.5F, -0.25F, -0.22F, scale, RenderPass.CAMERA,
                CameraTracker.CAMERA_MODEL, CameraTracker.CAMERA_DISPLAY_MODEL, cameraState, player);
        }
    }

    /**
     * extracts a camera model with screen
     *
     * @param offsetX      model x offset
     * @param offsetY      model y offset
     * @param offsetZ      model z offset
     * @param scale        size of the model
     * @param renderPass   RenderPass this camera shows, the camera will be placed there
     * @param model        camera model to render
     * @param displayModel model of the display that shows the camera view
     * @param widgetState  widget render state to store data in
     * @param player       to get the current level for lighting
     */
    private static void extractVRCameraWidget(
        float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, Identifier model,
        Identifier displayModel, CameraWidgetRenderState widgetState, @Nullable LocalPlayer player)
    {

        // model position relative to the view position
        widgetState.pos = DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();

        // orient and scale model
        widgetState.modelMatrix.set(DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix());

        scale = scale * DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
        widgetState.modelMatrix.scale(scale, scale, scale);

        // show orientation
        if (DEBUG) {
            DebugRenderHelper.renderLocalAxes(widgetState.pos, widgetState.modelMatrix);
        }

        // apply model offset
        widgetState.modelMatrix.translate(offsetX + 0.5F, offsetY + 0.5F, offsetZ + 0.5F);

        // lighting for the model
        widgetState.combinedLight = player == null ? LightCoordsUtil.FULL_BRIGHT :
            ClientUtils.getCombinedLightWithMin((ClientLevel) player.level(),
                BlockPos.containing(DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition()), 0);

        widgetState.cameraModelState.clear();
        MC.getModelManager().getItemModel(model)
            .update(widgetState.cameraModelState, ItemStack.EMPTY, MC.getItemModelResolver(), ItemDisplayContext.GROUND,
                null, null, 0);

        widgetState.displayModelState.clear();
        MC.getModelManager().getItemModel(displayModel)
            .update(widgetState.displayModelState, ItemStack.EMPTY, MC.getItemModelResolver(),
                ItemDisplayContext.GROUND, null, null, 0);
    }

    /**
     * redners a camera model with screen
     *
     * @param output          SubmitNodeCollector to submit the rendercall to
     * @param widgetState     camera widget renderstate to use for rendering
     * @param displaySupFunc  function that supplies the camera buffer, or something else
     * @param displayFaceFunc function that specifies if the view should be mirrored, normal or not shown at all
     */
    private static void renderVRCameraWidget(
        SubmitNodeCollector output, CameraRenderState cameraState, CameraWidgetRenderState widgetState,
        PoseStack poseStack, Supplier<GpuTextureView> displaySupFunc, Function<Direction, DisplayFace> displayFaceFunc)
    {
        poseStack.pushPose();
        poseStack.translate(
            widgetState.pos.x - cameraState.pos.x,
            widgetState.pos.y - cameraState.pos.y,
            widgetState.pos.z - cameraState.pos.z
        );
        poseStack.mulPose(widgetState.modelMatrix);

        // render camera model
        if (!widgetState.cameraModelState.isEmpty()) {
            widgetState.cameraModelState.submit(poseStack, output, widgetState.combinedLight,
                OverlayTexture.NO_OVERLAY, 0);
        }

        // render camera display
        if (!widgetState.displayModelState.isEmpty() &&
            !widgetState.displayModelState.layers[0].prepareQuadList().isEmpty())
        {
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            output.submitCustomGeometry(poseStack,
                VRRenderTypes.entitySolidNoCardinalLight(displaySupFunc.get(), true),
                (pose, consumer) -> {
                    // need to render this manually, because the uvs in the model are for the atlas texture, and not fullscreen
                    for (BakedQuad bakedquad : widgetState.displayModelState.layers[0].prepareQuadList()) {
                        if (displayFaceFunc.apply(bakedquad.direction()) != DisplayFace.NONE &&
                            bakedquad.materialInfo().sprite().contents().name().equals(TRANSPARENT_TEXTURE))
                        {
                            boolean mirrored = displayFaceFunc.apply(bakedquad.direction()) == DisplayFace.MIRROR;
                            consumer.addVertex(
                                    pose,
                                    bakedquad.position(0).x(),
                                    bakedquad.position(0).y(),
                                    bakedquad.position(0).z())
                                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                                .setUv(mirrored ? 1.0F : 0.0F, 1.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setNormal(0.0F, 1.0F, 0.0F);
                            consumer.addVertex(
                                    pose,
                                    bakedquad.position(1).x(),
                                    bakedquad.position(1).y(),
                                    bakedquad.position(1).z())
                                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                                .setUv(mirrored ? 1.0F : 0.0F, 0.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setNormal(0.0F, 1.0F, 0.0F);
                            consumer.addVertex(
                                    pose,
                                    bakedquad.position(2).x(),
                                    bakedquad.position(2).y(),
                                    bakedquad.position(2).z())
                                .setColor(1.0F, 1.0F, 1.0F, 1.0F)
                                .setUv(mirrored ? 0.0F : 1.0F, 0.0F)
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setNormal(0.0F, 1.0F, 0.0F);
                            consumer.addVertex(
                                    pose,
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
                });
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public enum DisplayFace {
        NONE,
        NORMAL,
        MIRROR
    }
}
