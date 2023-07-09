package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client.utils.Utils;

import java.util.function.Function;

public class VRWidgetHelper
{
    private static final RandomSource random = RandomSource.create();
    public static boolean debug = false;

    public static void renderVRThirdPersonCamWidget()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (dataholder.vrSettings.mixedRealityRenderCameraModel)
        {
            if ((dataholder.currentPass == RenderPass.LEFT || dataholder.currentPass == RenderPass.RIGHT) && (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON))
            {
                float f = 0.35F;

                if (dataholder.interactTracker.isInCamera() && !VRHotkeys.isMovingThirdPersonCam())
                {
                    f *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, f, RenderPass.THIRD, ClientDataHolderVR.thirdPersonCameraModel, ClientDataHolderVR.thirdPersonCameraDisplayModel, () ->
                {
                	dataholder.vrRenderer.framebufferMR.bindRead();
                    RenderSystem.setShaderTexture(0, dataholder.vrRenderer.framebufferMR.getColorTextureId());
                }, (face) ->
                {
                    if (face == Direction.NORTH)
                    {
                        return DisplayFace.MIRROR;
                    }
                    else {
                        return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
                    }
                });
            }
        }
    }

    public static void renderVRHandheldCameraWidget()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (dataholder.currentPass != RenderPass.CAMERA && dataholder.cameraTracker.isVisible())
        {
            float f = 0.25F;

            if (dataholder.interactTracker.isInHandheldCamera() && !dataholder.cameraTracker.isMoving())
            {
                f *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, f, RenderPass.CAMERA, CameraTracker.cameraModel, CameraTracker.cameraDisplayModel, () ->
            {
                if (((ItemInHandRendererExtension) minecraft.getEntityRenderDispatcher().getItemInHandRenderer()).getNearOpaqueBlock(dataholder.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(), (double)((GameRendererExtension) minecraft.gameRenderer).getMinClipDistance()) == null)
                {
                	dataholder.vrRenderer.cameraFramebuffer.bindRead();
                    RenderSystem.setShaderTexture(0, dataholder.vrRenderer.cameraFramebuffer.getColorTextureId());
                }
                else {
                	RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/black.png"));
                }
            }, (face) ->
            {
                return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
            });
        }
    }

    public static void renderVRCameraWidget(float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, ModelResourceLocation model, ModelResourceLocation displayModel, Runnable displayBindFunc, Function<Direction, DisplayFace> displayFaceFunc)
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        ((GameRendererExtension) minecraft.gameRenderer).applyVRModelView(dataholder.currentPass, poseStack);

        Vec3 vec3 = dataholder.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 vec31 = dataholder.vrPlayer.vrdata_world_render.getEye(dataholder.currentPass).getPosition();
        Vec3 vec32 = vec3.subtract(vec31);

        poseStack.translate(vec32.x, vec32.y, vec32.z);
        poseStack.mulPoseMatrix(dataholder.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix().toMCMatrix());
        scale = scale * dataholder.vrPlayer.vrdata_world_render.worldScale;
        poseStack.scale(scale, scale, scale);

        if (debug)
        {
        	MethodHolder.rotateDeg(poseStack, 180.0F, 0.0F, 1.0F, 0.0F);
            ((GameRendererExtension) minecraft.gameRenderer).renderDebugAxes(0, 0, 0, 0.08F);
            MethodHolder.rotateDeg(poseStack, 180.0F, 0.0F, 1.0F, 0.0F);
        }

        poseStack.translate(offsetX, offsetY, offsetZ);
        RenderSystem.applyModelViewMatrix();

        BlockPos blockpos = new BlockPos(dataholder.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition());
        int i = Utils.getCombinedLightWithMin(minecraft.level, blockpos, 0);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        if (minecraft.level != null)
            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        else
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        minecraft.getBlockRenderer().getModelRenderer().renderModel((new PoseStack()).last(), bufferbuilder, (BlockState)null, minecraft.getModelManager().getModel(model), 1.0F, 1.0F, 1.0F, i, OverlayTexture.NO_OVERLAY);
        tesselator.end();

        RenderSystem.disableBlend();
        displayBindFunc.run();
        RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);

        BufferBuilder bufferbuilder1 = tesselator.getBuilder();
        bufferbuilder1.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        for (BakedQuad bakedquad : minecraft.getModelManager().getModel(displayModel).getQuads((BlockState)null, (Direction)null, random))
        {
            if (displayFaceFunc.apply(bakedquad.getDirection()) != DisplayFace.NONE && bakedquad.getSprite().getName().equals(new ResourceLocation("vivecraft:transparent")))
            {
                int[] vertexList = bakedquad.getVertices();
                boolean flag = displayFaceFunc.apply(bakedquad.getDirection()) == DisplayFace.MIRROR;
                int j = LightTexture.pack(15, 15);
                int step = vertexList.length / 4;
                bufferbuilder1.vertex(
                                Float.intBitsToFloat(vertexList[0]),
                                Float.intBitsToFloat(vertexList[1]),
                                Float.intBitsToFloat(vertexList[2]))
                        .color(1.0F, 1.0F, 1.0F, 1.0F)
                        .uv(flag ? 1.0F : 0.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(j)
                        .normal(0.0F, 0.0F, flag ? -1.0F : 1.0F)
                        .endVertex();
                bufferbuilder1.vertex(
                                Float.intBitsToFloat(vertexList[step]),
                                Float.intBitsToFloat(vertexList[step+1]),
                                Float.intBitsToFloat(vertexList[step+2]))
                        .color(1.0F, 1.0F, 1.0F, 1.0F)
                        .uv(flag ? 1.0F : 0.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(j)
                        .normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
                bufferbuilder1.vertex(
                                Float.intBitsToFloat(vertexList[step*2]),
                                Float.intBitsToFloat(vertexList[step*2+1]),
                                Float.intBitsToFloat(vertexList[step*2+2]))
                        .color(1.0F, 1.0F, 1.0F, 1.0F)
                        .uv(flag ? 0.0F : 1.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(j)
                        .normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
                bufferbuilder1.vertex(
                                Float.intBitsToFloat(vertexList[step*3]),
                                Float.intBitsToFloat(vertexList[step*3+1]),
                                Float.intBitsToFloat(vertexList[step*3+2]))
                        .color(1.0F, 1.0F, 1.0F, 1.0F)
                        .uv(flag ? 0.0F : 1.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY)
                        .uv2(j)
                        .normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
            }
        }

        tesselator.end();
        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableBlend();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static enum DisplayFace
    {
        NONE,
        NORMAL,
        MIRROR;
    }
}
