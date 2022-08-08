package org.vivecraft.render;

import java.util.Random;
import java.util.function.Function;

import net.minecraft.client.renderer.LightTexture;
import org.vivecraft.gameplay.trackers.CameraTracker;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.Utils;

import com.example.vivecraftfabric.DataHolder;
import com.example.vivecraftfabric.GameRendererExtension;
import com.example.vivecraftfabric.GlStateHelper;
import com.example.vivecraftfabric.ItemInHandRendererExtension;
import com.example.vivecraftfabric.MethodHolder;
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

public class VRWidgetHelper
{
    private static final Random random = new Random();
    public static boolean debug = false;

    public static void renderVRThirdPersonCamWidget()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataholder = DataHolder.getInstance();

        if (dataholder.vrSettings.mixedRealityRenderCameraModel)
        {
            if ((dataholder.currentPass == RenderPass.LEFT || dataholder.currentPass == RenderPass.RIGHT) && (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON))
            {
                float f = 0.35F;

                if (dataholder.interactTracker.isInCamera() && !VRHotkeys.isMovingThirdPersonCam())
                {
                    f *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, f, RenderPass.THIRD, DataHolder.thirdPersonCameraModel, DataHolder.thirdPersonCameraDisplayModel, () ->
                {
                	dataholder.vrRenderer.framebufferMR.bindRead();
                    RenderSystem.setShaderTexture(0, dataholder.vrRenderer.framebufferMR.getColorTextureId());
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
        DataHolder dataholder = DataHolder.getInstance();

        if (dataholder.currentPass != RenderPass.CAMERA && dataholder.cameraTracker.isVisible())
        {
            float f = 0.25F;

            if (dataholder.interactTracker.isInHandheldCamera() && !dataholder.cameraTracker.isMoving())
            {
                f *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, f, RenderPass.CAMERA, CameraTracker.cameraModel, CameraTracker.cameraDisplayModel, () ->
            {
                if (((ItemInHandRendererExtension) minecraft.getItemInHandRenderer()).getNearOpaqueBlock(dataholder.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(), (double)((GameRendererExtension) minecraft.gameRenderer).getMinClipDistance()) == null)
                {
                	dataholder.vrRenderer.cameraFramebuffer.bindRead();
                    RenderSystem.setShaderTexture(0, dataholder.vrRenderer.cameraFramebuffer.getColorTextureId());
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
        DataHolder dataholder = DataHolder.getInstance();
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
        GlStateHelper.alphaFunc(519, 0.0F);
        displayBindFunc.run();
        RenderSystem.setShader(GameRenderer::getPositionTexLightmapColorShader);

        BufferBuilder bufferbuilder1 = tesselator.getBuilder();
        bufferbuilder1.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR);

        for (BakedQuad bakedquad : minecraft.getModelManager().getModel(displayModel).getQuads((BlockState)null, (Direction)null, random))
        {
            if (displayFaceFunc.apply(bakedquad.getDirection()) != VRWidgetHelper.DisplayFace.NONE && bakedquad.getSprite().getName().equals(new ResourceLocation("vivecraft:transparent")))
            {
                QuadBounds quadbounds = new QuadBounds(bakedquad.getVertices());
                boolean flag = displayFaceFunc.apply(bakedquad.getDirection()) == VRWidgetHelper.DisplayFace.MIRROR;
                int j = LightTexture.pack(15, 15);
                bufferbuilder1.vertex(flag ? (double)quadbounds.getMaxX() : (double)quadbounds.getMinX(), (double)quadbounds.getMinY(), (double)quadbounds.getMinZ()).uv(flag ? 1.0F : 0.0F, 0.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                bufferbuilder1.vertex(flag ? (double)quadbounds.getMinX() : (double)quadbounds.getMaxX(), (double)quadbounds.getMinY(), (double)quadbounds.getMinZ()).uv(flag ? 0.0F : 1.0F, 0.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                bufferbuilder1.vertex(flag ? (double)quadbounds.getMinX() : (double)quadbounds.getMaxX(), (double)quadbounds.getMaxY(), (double)quadbounds.getMinZ()).uv(flag ? 0.0F : 1.0F, 1.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                bufferbuilder1.vertex(flag ? (double)quadbounds.getMaxX() : (double)quadbounds.getMinX(), (double)quadbounds.getMaxY(), (double)quadbounds.getMinZ()).uv(flag ? 1.0F : 0.0F, 1.0F).uv2(j).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            }
        }

        tesselator.end();
        RenderSystem.enableBlend();
        GlStateHelper.alphaFunc(519, 0.1F);
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static enum DisplayFace
    {
        NONE,
        NORMAL,
        MIRROR;
    }

    //Optifine
    static class QuadBounds {
        private float minZ;
        private float maxX;
        private float maxY;
        private float maxZ;
        private float minX;
        private float minY;

        public QuadBounds(int[] vertexData) {
            int step = vertexData.length / 4;
            for (int i = 0; i < 4; ++i) {
                int pos = i * step;
                float x = Float.intBitsToFloat(vertexData[pos + 0]);
                float y = Float.intBitsToFloat(vertexData[pos + 1]);
                float z = Float.intBitsToFloat(vertexData[pos + 2]);
                if (this.minX > x) {
                    this.minX = x;
                }
                if (this.minY > y) {
                    this.minY = y;
                }
                if (this.minZ > z) {
                    this.minZ = z;
                }
                if (this.maxX < x) {
                    this.maxX = x;
                }
                if (this.maxY < y) {
                    this.maxY = y;
                }
                if (!(this.maxZ < z)) continue;
                this.maxZ = z;
            }
        }

        public float getMinX() {
            return this.minX;
        }

        public float getMinY() {
            return this.minY;
        }

        public float getMinZ() {
            return this.minZ;
        }

        public float getMaxX() {
            return this.maxX;
        }

        public float getMaxY() {
            return this.maxY;
        }

        public float getMaxZ() {
            return this.maxZ;
        }
    }
}
