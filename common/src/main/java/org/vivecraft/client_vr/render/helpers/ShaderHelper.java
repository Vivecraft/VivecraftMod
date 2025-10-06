package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.MirrorNotification;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class ShaderHelper {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static float FOV_REDUCTION = 1.0F;
    private static float WATER_EFFECT;
    private static boolean WAS_IN_WATER;
    private static float PUMPKIN_EFFECT;
    private static float PORTAL_EFFECT;
    private static float RED;
    private static float BLACK;
    private static float BLUE;
    private static float TIME;

    private static GpuBuffer SCREEN_VBO;

    /**
     * renders a fullscreen quad with the given RenderPipeline, and the given RenderTarget bound as "Sampler0"
     *
     * @param instance      RenderPipeline to use to render
     * @param uniformSetter consumer to set the uniforms
     * @param target        texture to write to, if {@code null} will write to the main target
     */
    public static void renderFullscreenQuad(
        @NotNull RenderPipeline instance,
        @NotNull Consumer<com.mojang.blaze3d.systems.RenderPass> uniformSetter,
        @Nullable GpuTexture target)
    {
        if (instance.getVertexFormat() != DefaultVertexFormat.POSITION_TEX) {
            throw new IllegalStateException("Vertex format needs to be 'POSITION_TEX'");
        }

        GpuBuffer quad = getFullscreenQuad();
        RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexGpuBuffer = indexBuffer.getBuffer(6);

        try (com.mojang.blaze3d.systems.RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(
                target != null ? target : MC.getMainRenderTarget().getColorTexture(), OptionalInt.empty()))
        {
            renderPass.setPipeline(instance);
            renderPass.setVertexBuffer(0, quad);
            uniformSetter.accept(renderPass);

            renderPass.setIndexBuffer(indexGpuBuffer, indexBuffer.type());
            renderPass.drawIndexed(0, 6);
        }
    }

    /**
     * tessellates a fullscreen quad and returns it
     */
    private static GpuBuffer getFullscreenQuad() {
        if (SCREEN_VBO == null) {
            BufferBuilder builder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
            builder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
            builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
            builder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);

            try (MeshData meshData = builder.buildOrThrow()) {
                SCREEN_VBO = RenderSystem.getDevice()
                    .createBuffer(() -> "fullscreen vr vertex buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE,
                        meshData.vertexBuffer());
            }
        }
        return SCREEN_VBO;
    }

    /**
     * does post-processing for the vr pass
     * this includes red damage indicator
     * blue freeze indicator
     * screen dimming when sleeping
     * fov reduction when walking
     * water and portal wobbles
     *
     * @param eye         RenderPass that is being post processed, LEFT or RIGHT
     * @param source      RenderTarget that holds the rendered image
     * @param target      RenderTarget to write to
     * @param partialTick current partial tick
     */
    public static void doVrPostProcess(RenderPass eye, RenderTarget source, RenderTarget target, float partialTick) {
        if (eye == RenderPass.LEFT) {
            // only update these once per frame, or the effects are twice as fast
            // and could be out of sync between the eyes

            // status effects
            RED = 0.0F;
            BLACK = 0.0F;
            BLUE = 0.0F;
            TIME = (float) Util.getMillis() / 1000.0F;

            PUMPKIN_EFFECT = 0.0F;
            PORTAL_EFFECT = 0.0F;

            if (MC.player != null && MC.level != null) {
                boolean isInWater = ((GameRendererExtension) MC.gameRenderer).vivecraft$isInWater();
                if (DATA_HOLDER.vrSettings.waterEffect && WAS_IN_WATER != isInWater) {
                    // water state changed, start effect
                    WATER_EFFECT = 2.3F;
                } else {
                    if (isInWater) {
                        // slow falloff in water
                        WATER_EFFECT -= 1F / 120F;
                    } else {
                        // fast falloff outside water
                        WATER_EFFECT -= 1F / 60F;
                    }

                    if (WATER_EFFECT < 0.0F) {
                        WATER_EFFECT = 0.0F;
                    }
                }

                WAS_IN_WATER = isInWater;

                if (IrisHelper.isLoaded() && !IrisHelper.hasWaterEffect()) {
                    WATER_EFFECT = 0.0F;
                }

                float portalTime = Mth.lerp(partialTick, MC.player.oPortalEffectIntensity,
                    MC.player.portalEffectIntensity);
                if (DATA_HOLDER.vrSettings.portalEffect &&
                    // vanilla check for portal overlay
                    portalTime > 0.0F)
                {
                    PORTAL_EFFECT = portalTime;
                } else {
                    PORTAL_EFFECT = 0.0F;
                }

                ItemStack itemstack = MC.player.getItemBySlot(EquipmentSlot.HEAD);

                if (DATA_HOLDER.vrSettings.pumpkinEffect && itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem() &&
                    (!itemstack.has(DataComponents.CUSTOM_MODEL_DATA)))
                {
                    PUMPKIN_EFFECT = 1.0F;
                } else {
                    PUMPKIN_EFFECT = 0.0F;
                }

                float hurtTimer = (float) MC.player.hurtTime - partialTick;
                float healthPercent = 1.0F - MC.player.getHealth() / MC.player.getMaxHealth();
                healthPercent = (healthPercent - 0.5F) * 0.75F;

                if (DATA_HOLDER.vrSettings.hitIndicator && hurtTimer > 0.0F) { // hurt flash
                    hurtTimer = hurtTimer / (float) MC.player.hurtDuration;
                    hurtTimer = healthPercent +
                        Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * Mth.PI) * 0.5F;
                    RED = hurtTimer;
                } else if (DATA_HOLDER.vrSettings.lowHealthIndicator) { // red due to low health
                    RED = healthPercent * Mth.abs(Mth.sin((2.5F * TIME) / (1.0F - healthPercent + 0.1F)));

                    if (MC.player.isCreative()) {
                        RED = 0.0F;
                    }
                }

                float freeze = MC.player.getPercentFrozen();
                if (DATA_HOLDER.vrSettings.freezeEffect && freeze > 0) {
                    BLUE = RED;
                    BLUE = Math.max(freeze / 2, BLUE);
                    RED = 0;
                }

                if (MC.player.isSleeping()) {
                    BLACK = 0.5F + 0.3F * MC.player.getSleepTimer() * 0.01F;
                }

                if (DATA_HOLDER.vr.isWalkingAbout && BLACK < 0.8F) {
                    BLACK = 0.5F;
                }

                // fov reduction when moving
                if (DATA_HOLDER.vrSettings.useFOVReduction && DATA_HOLDER.vrPlayer.getFreeMove()) {
                    if (Math.abs(MC.player.zza) > 0.0F || Math.abs(MC.player.xxa) > 0.0F) {
                        FOV_REDUCTION = FOV_REDUCTION - 0.05F;
                    } else {
                        FOV_REDUCTION = FOV_REDUCTION + 0.01F;
                    }
                    FOV_REDUCTION = Mth.clamp(FOV_REDUCTION, DATA_HOLDER.vrSettings.fovReductionMin, 0.8F);
                } else {
                    FOV_REDUCTION = 1.0F;
                }
            } else {
                WATER_EFFECT = 0.0F;
                FOV_REDUCTION = 1.0F;
            }
        }

        renderFullscreenQuad(VRShaders.POST_PROCESSING_PIPELINE, renderPass -> {
            if (PUMPKIN_EFFECT > 0.0F) {
                renderPass.setUniform(VRShaders.POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM, 0.3F);
                renderPass.setUniform(VRShaders.POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM, 0.0F);
            } else {
                renderPass.setUniform(VRShaders.POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM, FOV_REDUCTION);
                renderPass.setUniform(VRShaders.POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM, 0.06F);
            }

            renderPass.setUniform(VRShaders.POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM,
                DATA_HOLDER.vrSettings.fovRedutioncOffset);

            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM, RED);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM, BLUE);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM, BLACK);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_TIME_UNIFORM, TIME);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM, WATER_EFFECT);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM, PORTAL_EFFECT);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM, PUMPKIN_EFFECT);
            renderPass.setUniform(VRShaders.POST_PROCESSING_OVERLAY_EYE_UNIFORM, eye == RenderPass.LEFT ? 1 : -1);
            renderPass.bindSampler(VRShaders.POST_PROCESSING_COLOR_SAMPLER, source.getColorTexture());
        }, target.getColorTexture());
    }

    /**
     * draws the desktop mirror to the bound buffer
     */
    public static void drawMirror() {
        if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF &&
            DATA_HOLDER.vr.isHMDTracking())
        {
            // no mirror, only show when headset is not tracking, to be able to see the menu with the headset off
            MirrorNotification.notify("Mirror is OFF", true, 1000);
        } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            ShaderHelper.doMixedRealityMirror();
        } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.DUAL &&
            (!DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera ||
                !DATA_HOLDER.cameraTracker.isVisible()
            ))
        {
            // show both eyes side by side
            RenderTarget leftEye = DATA_HOLDER.vrRenderer.framebufferEye0;
            RenderTarget rightEye = DATA_HOLDER.vrRenderer.framebufferEye1;

            int screenWidth = MC.mainRenderTarget.width / 2;
            int screenHeight = MC.mainRenderTarget.height;

            if (leftEye != null) {
                ShaderHelper.blitToScreen(leftEye, 0, screenWidth, screenHeight, 0, 0.0F, 0.0F, false);
            }

            if (rightEye != null) {
                ShaderHelper.blitToScreen(rightEye, screenWidth, screenWidth, screenHeight, 0, 0.0F, 0.0F, false);
            }
        } else {
            // general single buffer case
            float xCrop = 0.0F;
            float yCrop = 0.0F;
            boolean keepAspect = false;
            RenderTarget source = DATA_HOLDER.vrRenderer.framebufferEye0;

            if (DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera &&
                DATA_HOLDER.cameraTracker.isVisible())
            {
                source = DATA_HOLDER.vrRenderer.cameraFramebuffer;
                keepAspect = true;
            } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
                source = DATA_HOLDER.vrRenderer.framebufferUndistorted;
            } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                source = DATA_HOLDER.vrRenderer.framebufferMR;
            } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.GUI) {
                source = GuiHandler.GUI_FRAMEBUFFER;
            } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.SINGLE ||
                DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF)
            {
                if (!DATA_HOLDER.vrSettings.displayMirrorLeftEye) {
                    source = DATA_HOLDER.vrRenderer.framebufferEye1;
                }
            } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.CROPPED) {
                if (!DATA_HOLDER.vrSettings.displayMirrorLeftEye) {
                    source = DATA_HOLDER.vrRenderer.framebufferEye1;
                }

                xCrop = DATA_HOLDER.vrSettings.mirrorCrop;
                yCrop = DATA_HOLDER.vrSettings.mirrorCrop;
                keepAspect = true;
            }
            // Debug
            // source = DataHolder.getInstance().vrRenderer.telescopeFramebufferR;
            //
            if (source != null) {
                ShaderHelper.blitToScreen(source,
                    0, MC.mainRenderTarget.width,
                    MC.mainRenderTarget.height, 0,
                    xCrop, yCrop, keepAspect);
            }
        }

        // draw mirror text
        MirrorNotification.render();
    }

    public static void doMixedRealityMirror() {
        Vector3f camPlayer = DATA_HOLDER.vrPlayer.vrdata_room_pre.getHeadPivotF()
            .sub(DATA_HOLDER.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getPositionF());

        // transpose, because camera rotations are transposed
        Matrix4f viewMatrix = DATA_HOLDER.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix().transpose();
        Vector3f cameraLook = DATA_HOLDER.vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getDirection();
        // only horizontal
        cameraLook.set(-cameraLook.x, 0.0F, -cameraLook.z);

        boolean alphaMask =
            DATA_HOLDER.vrSettings.mixedRealityUnityLike && DATA_HOLDER.vrSettings.mixedRealityAlphaMask;

        renderFullscreenQuad(VRShaders.MIXED_REALITY_PIPELINE, renderPass -> {
            // set uniforms
            renderPass.setUniform(VRShaders.MIXED_REALITY_PROJECTION_MATRIX_UNIFORM,
                ((GameRendererExtension) MC.gameRenderer).vivecraft$getThirdPassProjectionMatrix());
            renderPass.setUniform(VRShaders.MIXED_REALITY_VIEW_MATRIX_UNIFORM, viewMatrix);

            renderPass.setUniform(VRShaders.MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM,
                camPlayer.x, camPlayer.y, camPlayer.z);
            renderPass.setUniform(VRShaders.MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM,
                cameraLook.x, cameraLook.y, cameraLook.z);

            if (!alphaMask) {
                renderPass.setUniform(VRShaders.MIXED_REALITY_KEY_COLOR_UNIFORM,
                    (float) DATA_HOLDER.vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
                    (float) DATA_HOLDER.vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
                    (float) DATA_HOLDER.vrSettings.mixedRealityKeyColor.getBlue() / 255.0F);
            } else {
                renderPass.setUniform(VRShaders.MIXED_REALITY_KEY_COLOR_UNIFORM, 0F, 0F, 0F);
            }
            renderPass.setUniform(VRShaders.MIXED_REALITY_ALPHA_MODE_UNIFORM, alphaMask ? 1 : 0);

            renderPass.setUniform(VRShaders.MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM,
                DATA_HOLDER.vrSettings.mixedRealityUnityLike ? 1 : 0);


            // bind textures
            renderPass.bindSampler(VRShaders.MIXED_REALITY_THIRD_COLOR_SAMPLER,
                DATA_HOLDER.vrRenderer.framebufferMR.getColorTexture());
            renderPass.bindSampler(VRShaders.MIXED_REALITY_THIRD_DEPTH_SAMPLER,
                DATA_HOLDER.vrRenderer.framebufferMR.getDepthTexture());

            if (DATA_HOLDER.vrSettings.mixedRealityUnityLike) {
                RenderTarget source;
                if (DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera && DATA_HOLDER.cameraTracker.isVisible()) {
                    source = DATA_HOLDER.vrRenderer.cameraFramebuffer;
                } else if (DATA_HOLDER.vrSettings.mixedRealityUndistorted) {
                    source = DATA_HOLDER.vrRenderer.framebufferUndistorted;
                } else {
                    if (DATA_HOLDER.vrSettings.displayMirrorLeftEye) {
                        source = DATA_HOLDER.vrRenderer.framebufferEye0;
                    } else {
                        source = DATA_HOLDER.vrRenderer.framebufferEye1;
                    }
                }
                renderPass.bindSampler(VRShaders.MIXED_REALITY_FIRST_COLOR_SAMPLER, source.getColorTexture());
            }
        }, null);
    }

    /**
     * uses a lanczos filter to scale the source RenderTarget to the secondPass RenderTarget size
     *
     * @param source     RenderTarget with the low/high resolution frame
     * @param firstPass  RenderTarget with source height and target width, for the intermediary step
     * @param secondPass RenderTarget with the target size
     */
    public static void doFSAA(RenderTarget source, RenderTarget firstPass, RenderTarget secondPass) {
        if (firstPass == null) {
            DATA_HOLDER.vrRenderer.reinitFrameBuffers("FSAA Setting Changed");
        } else {
            // first pass, horizontal
            renderFullscreenQuad(VRShaders.LANCZOS_PIPELINE, renderPass -> {
                renderPass.bindSampler(VRShaders.LANCZOS_COLOR_SAMPLER, source.getColorTexture());
                renderPass.bindSampler(VRShaders.LANCZOS_DEPTH_SAMPLER, source.getDepthTexture());
                renderPass.setUniform(VRShaders.LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM,
                    1.0F / (3.0F * (float) firstPass.viewWidth));
                renderPass.setUniform(VRShaders.LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM, 0.0F);
            }, firstPass.getColorTexture());

            // second pass, vertical
            renderFullscreenQuad(VRShaders.LANCZOS_PIPELINE, renderPass -> {
                renderPass.bindSampler(VRShaders.LANCZOS_COLOR_SAMPLER, firstPass.getColorTexture());
                renderPass.bindSampler(VRShaders.LANCZOS_DEPTH_SAMPLER, firstPass.getDepthTexture());
                renderPass.setUniform(VRShaders.LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM, 0.0F);
                renderPass.setUniform(VRShaders.LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM,
                    1.0F / (3.0F * (float) secondPass.viewHeight));
            }, secondPass.getColorTexture());
        }
    }

    /**
     * blits the given {@code source} RenderTarget to the screen/bound buffer<br>
     * the {@code source} is drawn to the rectangle at {@code left},{@code top} with a size of {@code width},{@code height}<br>
     * if {@code xCropFactor} or {@code yCropFactor} are non 0 the {@code source} gets zoomed in
     *
     * @param source      RenderTarget to draw to the screen
     * @param left        left edge of the target area
     * @param width       width of the target area
     * @param height      height of the target area
     * @param top         top edge of the target area
     * @param xCropFactor vertical crop factor for the {@code source}
     * @param yCropFactor horizontal crop factor for the {@code source}
     * @param keepAspect  keeps the aspect ratio in takt when cropping the buffer
     */
    public static void blitToScreen(
        RenderTarget source, int left, int width, int height, int top, float xCropFactor, float yCropFactor,
        boolean keepAspect)
    {
        RenderSystem.assertOnRenderThread();

        float drawAspect = (float) width / (float) height;
        float bufferAspect = (float) source.viewWidth / (float) source.viewHeight;

        float xMin = xCropFactor;
        float yMin = yCropFactor;
        float xMax = 1.0F - xCropFactor;
        float yMax = 1.0F - yCropFactor;

        if (keepAspect) {
            if (drawAspect > bufferAspect) {
                // destination is wider than the buffer
                float heightAspect = (bufferAspect / drawAspect) * (0.5F - yCropFactor);

                yMin = 0.5F - heightAspect;
                yMax = 0.5F + heightAspect;
            } else {
                // destination is taller than the buffer
                float widthAspect = (drawAspect / bufferAspect) * (0.5F - xCropFactor);

                xMin = 0.5F - widthAspect;
                xMax = 0.5F + widthAspect;
            }
        }

        BufferBuilder bufferBuilder = Tesselator.getInstance()
            .begin(VertexFormat.Mode.QUADS, VRShaders.BLIT_VR_PIPELINE.getVertexFormat());

        // position quad
        float xMinPos = (float) left / MC.getMainRenderTarget().viewWidth * 2F - 1F;
        float yMinPos = (float) top / MC.getMainRenderTarget().viewHeight * 2F - 1F;
        float xMaxPos = xMinPos + (float) width / MC.getMainRenderTarget().viewWidth * 2F;
        float yMaxPos = yMinPos + (float) height / MC.getMainRenderTarget().viewHeight * 2F;

        bufferBuilder.addVertex(xMinPos, yMinPos, 0.0F).setUv(xMin, yMin);
        bufferBuilder.addVertex(xMaxPos, yMinPos, 0.0F).setUv(xMax, yMin);
        bufferBuilder.addVertex(xMaxPos, yMaxPos, 0.0F).setUv(xMax, yMax);
        bufferBuilder.addVertex(xMinPos, yMaxPos, 0.0F).setUv(xMin, yMax);

        try (MeshData meshData = bufferBuilder.buildOrThrow()) {
            GpuBuffer gpuBuffer = VRShaders.BLIT_VR_PIPELINE.getVertexFormat()
                .uploadImmediateVertexBuffer(meshData.vertexBuffer());

            GpuBuffer indexBuffer;
            VertexFormat.IndexType indexType;
            if (meshData.indexBuffer() == null) {
                RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(
                    VertexFormat.Mode.QUADS);
                indexBuffer = autoStorageIndexBuffer.getBuffer(6);
                indexType = autoStorageIndexBuffer.type();
            } else {
                indexBuffer = VRShaders.BLIT_VR_PIPELINE.getVertexFormat()
                    .uploadImmediateIndexBuffer(meshData.indexBuffer());
                indexType = meshData.drawState().indexType();
            }

            try (com.mojang.blaze3d.systems.RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(MC.getMainRenderTarget().getColorTexture(), OptionalInt.empty()))
            {
                renderPass.setPipeline(VRShaders.BLIT_VR_PIPELINE);
                renderPass.setVertexBuffer(0, gpuBuffer);

                renderPass.bindSampler(VRShaders.BLIT_VR_COLOR_SAMPLER, source.getColorTexture());

                renderPass.setIndexBuffer(indexBuffer, indexType);
                renderPass.drawIndexed(0, 6);
            }
        }
    }
}
