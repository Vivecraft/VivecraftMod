package org.vivecraft.render;

import java.util.Random;
import java.util.function.Function;

import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.Utils;

import com.example.examplemod.DataHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
//import net.optifine.model.QuadBounds;

public class VRWidgetHelper
{
    private static final Random random = new Random();
    public static boolean debug = false;

    public static void renderVRThirdPersonCamWidget()
    {
        DataHolder dataHolder = DataHolder.getInstance();

        if (dataHolder.vrSettings.mixedRealityRenderCameraModel)
        {
            if ((dataHolder.currentPass == RenderPass.LEFT || dataHolder.currentPass == RenderPass.RIGHT) && (dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || dataHolder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON))
            {
                float f = 0.35F;

                if (dataHolder.interactTracker.isInCamera() && !VRHotkeys.isMovingThirdPersonCam())
                {
                    f *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, f, RenderPass.THIRD, GameRenderer.thirdPersonCameraModel, GameRenderer.thirdPersonCameraDisplayModel, () ->
                {
                    minecraft.vrRenderer.framebufferMR.bindRead();
                    RenderSystem.setShaderTexture(0, minecraft.vrRenderer.framebufferMR.getColorTextureId());
                }, (face) ->
                {
                    if (face == Direction.NORTH)
                    {
                        return VRWidgetHelper.DisplayFace.MIRROR;
                    }
                    else {
                        return face == Direction.SOUTH ? VRWidgetHelper.DisplayFace.NORMAL : VRWidgetHelper.DisplayFace.NONE;
                    }
                });
            }
        }
    }

    public static void renderVRHandheldCameraWidget()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();

        if (dataHolder.currentPass != RenderPass.CAMERA && dataHolder.cameraTracker.isVisible())
        {
            float f = 0.25F;

            if (dataHolder.interactTracker.isInHandheldCamera() && !dataHolder.cameraTracker.isMoving())
            {
                f *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, f, RenderPass.CAMERA, CameraTracker.cameraModel, CameraTracker.cameraDisplayModel, () ->
            {
                if (minecraft.getItemInHandRenderer().getNearOpaqueBlock(dataHolder.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(), (double)minecraft.gameRenderer.minClipDistance) == null)
                {
                	dataHolder.vrRenderer.cameraFramebuffer.bindRead();
                    RenderSystem.setShaderTexture(0, dataHolder.vrRenderer.cameraFramebuffer.getColorTextureId());
                }
                else {
                	RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/black.png"));
                }
            }, (face) ->
            {
                return face == Direction.SOUTH ? VRWidgetHelper.DisplayFace.NORMAL : VRWidgetHelper.DisplayFace.NONE;
            });
        }
    }

    public static void renderVRCameraWidget(float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, ModelResourceLocation model, ModelResourceLocation displayModel, Runnable displayBindFunc, Function<Direction, VRWidgetHelper.DisplayFace> displayFaceFunc)
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        minecraft.gameRenderer.applyVRModelView(dataHolder.currentPass, poseStack);

        Vec3 vec3 = dataHolder.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 vec31 = dataHolder.vrPlayer.vrdata_world_render.getEye(dataHolder.currentPass).getPosition();
        Vec3 vec32 = vec3.subtract(vec31);

        poseStack.translate(vec32.x, vec32.y, vec32.z);
        poseStack.mulPoseMatrix(dataHolder.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix().toMCMatrix());
        scale = scale * dataHolder.vrPlayer.vrdata_world_render.worldScale;
        poseStack.scale(scale, scale, scale);

        if (debug)
        {
        	poseStack.rotateDeg(180.0F, 0.0F, 1.0F, 0.0F);
            minecraft.gameRenderer.renderDebugAxes(0, 0, 0, 0.08F);
            poseStack.rotateDeg(180.0F, 0.0F, 1.0F, 0.0F);
        }

        poseStack.translate(offsetX, offsetY, offsetZ);
        RenderSystem.applyModelViewMatrix();

        BlockPos blockpos = new BlockPos(minecraft.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition());
        int i = Utils.getCombinedLightWithMin(minecraft.level, blockpos, 0);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        if (minecraft.level != null)
            RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
        else
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
        minecraft.getBlockRenderer().getModelRenderer().renderModel((new PoseStack()).last(), bufferbuilder, (BlockState)null, minecraft.getModelManager().getModel(model), 1.0F, 1.0F, 1.0F, i, OverlayTexture.NO_OVERLAY);
        tesselator.end();

        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.disableBlend();
        //GlStateManager.alphaFunc(519, 0.0F);
        displayBindFunc.run();
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);

        BufferBuilder bufferbuilder1 = tesselator.getBuilder();
        bufferbuilder1.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);

        for (BakedQuad bakedquad : minecraft.getModelManager().getModel(displayModel).getQuads((BlockState)null, (Direction)null, random))
        {
            if (displayFaceFunc.apply(bakedquad.getDirection()) != VRWidgetHelper.DisplayFace.NONE && bakedquad.getSprite().getName().equals(new ResourceLocation("vivecraft:transparent")))
            {
            	//TODO Optifine
//                QuadBounds quadbounds = bakedquad.getQuadBounds();
//                boolean flag = displayFaceFunc.apply(bakedquad.getDirection()) == VRWidgetHelper.DisplayFace.MIRROR;
//                int j = LightTexture.pack(15, 15);
//                bufferbuilder1.vertex(flag ? (double)quadbounds.getMaxX() : (double)quadbounds.getMinX(), (double)quadbounds.getMinY(), (double)quadbounds.getMinZ()).uv(flag ? 1.0F : 0.0F, 0.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
//                bufferbuilder1.vertex(flag ? (double)quadbounds.getMinX() : (double)quadbounds.getMaxX(), (double)quadbounds.getMinY(), (double)quadbounds.getMinZ()).uv(flag ? 0.0F : 1.0F, 0.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
//                bufferbuilder1.vertex(flag ? (double)quadbounds.getMinX() : (double)quadbounds.getMaxX(), (double)quadbounds.getMaxY(), (double)quadbounds.getMinZ()).uv(flag ? 0.0F : 1.0F, 1.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
//                bufferbuilder1.vertex(flag ? (double)quadbounds.getMaxX() : (double)quadbounds.getMinX(), (double)quadbounds.getMaxY(), (double)quadbounds.getMinZ()).uv(flag ? 1.0F : 0.0F, 1.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).normal(0.0F, 0.0F, flag ? -1.0F : 1.0F).endVertex();
            }
        }

        tesselator.end();
        RenderSystem.enableBlend();
        //GlStateManager.alphaFunc(519, 0.1F);
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
