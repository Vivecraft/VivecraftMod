package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.MirrorNotification;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.renderstates.PostProcessRenderState;
import org.vivecraft.client_vr.render.ubos.LanczosUBO;
import org.vivecraft.client_vr.render.ubos.MixedRealityUBO;
import org.vivecraft.client_vr.render.ubos.PostProcessUBO;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShaderHelper {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();

    private static GpuBuffer SCREEN_UV_VBO;
    private static GpuBuffer SCREEN_VBO;

    public static final Matrix4f THIRD_PASS_PROJECTION_MATRIX = new Matrix4f();

    /**
     * renders a fullscreen quad with the given RenderPipeline, and the given RenderTarget bound as "Sampler0"
     *
     * @param instance      RenderPipeline to use to render
     * @param uniformSetter consumer to set the uniforms
     * @param target        texture to write to, if {@code null} will write to the main target
     */
    public static void renderFullscreenQuad(
        @NotNull Supplier<String> name,
        @NotNull RenderPipeline instance,
        @NotNull Consumer<com.mojang.blaze3d.systems.RenderPass> uniformSetter,
        @Nullable GpuTextureView target)
    {
        GpuBuffer quad = getFullscreenQuad(instance.getVertexFormat());
        RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexGpuBuffer = indexBuffer.getBuffer(6);

        try (com.mojang.blaze3d.systems.RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(name,
                target != null ? target : MC.getMainRenderTarget().getColorTextureView(), OptionalInt.empty()))
        {
            renderPass.setPipeline(instance);
            renderPass.setVertexBuffer(0, quad);
            uniformSetter.accept(renderPass);

            renderPass.setIndexBuffer(indexGpuBuffer, indexBuffer.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
    }

    /**
     * tessellates a fullscreen quad and returns it
     */
    private static GpuBuffer getFullscreenQuad(VertexFormat format) {
        if (format == DefaultVertexFormat.POSITION_TEX) {
            if (SCREEN_UV_VBO == null) {
                BufferBuilder builder = Tesselator.getInstance()
                    .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                builder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
                builder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
                builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
                builder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);

                try (MeshData meshData = builder.buildOrThrow()) {
                    SCREEN_UV_VBO = RenderSystem.getDevice()
                        .createBuffer(() -> "fullscreen uv vr vertex buffer", GpuBuffer.USAGE_VERTEX,
                            meshData.vertexBuffer());
                }
            }
            return SCREEN_UV_VBO;
        } else if (format == DefaultVertexFormat.POSITION) {
            if (SCREEN_VBO == null) {
                BufferBuilder builder = Tesselator.getInstance()
                    .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                builder.addVertex(-1.0F, -1.0F, 0.0F);
                builder.addVertex(1.0F, -1.0F, 0.0F);
                builder.addVertex(1.0F, 1.0F, 0.0F);
                builder.addVertex(-1.0F, 1.0F, 0.0F);

                try (MeshData meshData = builder.buildOrThrow()) {
                    SCREEN_VBO = RenderSystem.getDevice()
                        .createBuffer(() -> "fullscreen vr vertex buffer", GpuBuffer.USAGE_VERTEX,
                            meshData.vertexBuffer());
                }
            }
            return SCREEN_VBO;
        }
        throw new IllegalStateException("Unsupported Vertex format: " + format);
    }

    /**
     * does post-processing for the vr pass
     * this includes red damage indicator
     * blue freeze indicator
     * screen dimming when sleeping
     * fov reduction when walking
     * water and portal wobbles
     *
     * @param eye              RenderPass that is being post processed, LEFT or RIGHT
     * @param source           RenderTarget that holds the rendered image
     * @param target           RenderTarget to write to
     * @param postProcessState post processing render state
     */
    public static void doVrPostProcess(
        RenderPass eye, RenderTarget source, RenderTarget target, PostProcessRenderState postProcessState)
    {
        VRShaders.POST_PROCESS_UBO.updateBuffer(
            postProcessState.pumpkinEffect > 0.0F ? 0.3F : postProcessState.fovReduction,
            DATA_HOLDER.vrSettings.fovRedutioncOffset,
            postProcessState.pumpkinEffect > 0.0F ? 0.0F : 0.06F,
            postProcessState.waterEffect,
            postProcessState.portalEffect,
            postProcessState.time,
            postProcessState.pumpkinEffect,
            postProcessState.red,
            postProcessState.blue,
            postProcessState.black,
            eye == RenderPass.LEFT ? 1 : -1
        );

        renderFullscreenQuad(() -> "Vive postprocessing", VRShaders.POST_PROCESSING_PIPELINE, renderPass -> {
            renderPass.setUniform(PostProcessUBO.UBO_NAME, VRShaders.POST_PROCESS_UBO.getBuffer());
            renderPass.bindTexture(VRShaders.POST_PROCESSING_COLOR_SAMPLER, source.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        }, target.getColorTextureView());
        VRShaders.POST_PROCESS_UBO.endFrame();
    }

    /**
     * draws the desktop mirror to the bound buffer
     */
    public static void drawMirror() {
        if (DATA_HOLDER.vrSettings.renderAllPasses) {
            int screenWidth = MC.mainRenderTarget.width / 4;
            int screenHeight = MC.mainRenderTarget.height / 2;
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 2; y++) {
                    RenderTarget target = switch (RenderPass.values()[x + 4 * y]) {
                        case LEFT -> DATA_HOLDER.vrRenderer.framebufferEye0;
                        case RIGHT -> DATA_HOLDER.vrRenderer.framebufferEye1;
                        case CENTER -> DATA_HOLDER.vrRenderer.framebufferUndistorted;
                        case THIRD -> DATA_HOLDER.vrRenderer.framebufferMR;
                        case GUI -> GuiHandler.GUI_FRAMEBUFFER;
                        case SCOPER -> DATA_HOLDER.vrRenderer.telescopeFramebufferR;
                        case SCOPEL -> DATA_HOLDER.vrRenderer.telescopeFramebufferL;
                        case CAMERA -> DATA_HOLDER.vrRenderer.cameraFramebuffer;
                        default -> null;
                    };
                    if (target != null) {
                        ShaderHelper.blitToScreen(target, screenWidth * x, screenWidth,
                            screenHeight, screenHeight * y, 0.0F, 0.0F, false, false);
                    }
                }
            }
        } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF &&
            DATA_HOLDER.vr.isHMDTracking())
        {
            // no mirror, only show when headset is not tracking, to be able to see the menu with the headset off
            if (DATA_HOLDER.vrSettings.showMirrorOffText) {
                MirrorNotification.notify(I18n.get("vivecraft.messages.mirroroff"), true, 1000);
            } else {
                // just clear it
                RenderSystem.getDevice().createCommandEncoder()
                    .clearColorTexture(MC.mainRenderTarget.getColorTexture(), 0xFF000000);
            }
        } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            ShaderHelper.doMixedRealityMirror();
        } else if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.DUAL &&
            (!DATA_HOLDER.vrSettings.displayMirrorUseScreenshotCamera ||
                !DATA_HOLDER.cameraTracker.isVisible()
            ))
        {
            // show both eyes side by side
            RenderTarget leftEye = DATA_HOLDER.vrSettings.dualMirrorSwap ? DATA_HOLDER.vrRenderer.framebufferEye1 :
                DATA_HOLDER.vrRenderer.framebufferEye0;
            RenderTarget rightEye = DATA_HOLDER.vrSettings.dualMirrorSwap ? DATA_HOLDER.vrRenderer.framebufferEye0 :
                DATA_HOLDER.vrRenderer.framebufferEye1;

            int screenWidth = MC.mainRenderTarget.width / 2;
            int screenHeight = MC.mainRenderTarget.height;

            if (leftEye != null) {
                ShaderHelper.blitToScreen(leftEye, 0, screenWidth, screenHeight, 0, 0.0F, 0.0F,
                    DATA_HOLDER.vrSettings.dualMirrorCrop, false);
            }

            if (rightEye != null) {
                ShaderHelper.blitToScreen(rightEye, screenWidth, screenWidth, screenHeight, 0, 0.0F, 0.0F,
                    DATA_HOLDER.vrSettings.dualMirrorCrop,
                    false);
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
                    xCrop, yCrop, keepAspect, false);
            }
            if (source != GuiHandler.GUI_FRAMEBUFFER) {
                blitGui();
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

        int guiMask = 0;
        if (DATA_HOLDER.vrSettings.guiOnMirror == VRSettings.MirrorGui.ALWAYS ||
            (DATA_HOLDER.vrSettings.guiOnMirror == VRSettings.MirrorGui.HUD_ONLY && MC.screen == null))
        {
            guiMask = switch (DATA_HOLDER.vrSettings.mixedRealityGui) {
                case FIRST -> VRShaders.MIXED_REALITY_GUI_FIRST;
                case THIRD -> VRShaders.MIXED_REALITY_GUI_THIRD;
                case BOTH -> VRShaders.MIXED_REALITY_GUI_FIRST | VRShaders.MIXED_REALITY_GUI_THIRD;
                case SEPARATE -> VRShaders.MIXED_REALITY_GUI_SEPARATE;
            };
        }

        VRShaders.MIXED_REALITY_UBO.updateBuffer(
            THIRD_PASS_PROJECTION_MATRIX,
            viewMatrix,
            camPlayer, cameraLook,
            DATA_HOLDER.vrSettings.mixedRealityUnityLike,
            alphaMask ? MathUtils.ZERO : new Vector3f(
                DATA_HOLDER.vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
                DATA_HOLDER.vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
                DATA_HOLDER.vrSettings.mixedRealityKeyColor.getBlue() / 255.0F),
            alphaMask,
            guiMask
        );

        GpuTextureView black = RenderHelper.getGpuTexture(RenderHelper.BLACK_TEXTURE);

        renderFullscreenQuad(() -> "Vive mixed reality", VRShaders.MIXED_REALITY_PIPELINE, renderPass -> {
            // set uniforms
            renderPass.setUniform(MixedRealityUBO.UBO_NAME, VRShaders.MIXED_REALITY_UBO.getBuffer());

            // bind textures
            renderPass.bindTexture(VRShaders.MIXED_REALITY_THIRD_COLOR_SAMPLER,
                DATA_HOLDER.vrRenderer.framebufferMR.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            renderPass.bindTexture(VRShaders.MIXED_REALITY_THIRD_DEPTH_SAMPLER,
                DATA_HOLDER.vrRenderer.framebufferMR.getDepthTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));

            renderPass.bindTexture(VRShaders.MIXED_REALITY_GUI_COLOR_SAMPLER,
                GuiHandler.GUI_FRAMEBUFFER.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

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
                renderPass.bindTexture(VRShaders.MIXED_REALITY_FIRST_COLOR_SAMPLER, source.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            } else {
                renderPass.bindTexture(VRShaders.MIXED_REALITY_FIRST_COLOR_SAMPLER, black,
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            }
        }, null);
        VRShaders.MIXED_REALITY_UBO.endFrame();
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
            VRShaders.LANCZOS_UBO.updateBuffer(1.0F / (3.0F * (float) firstPass.width), 0F);

            renderFullscreenQuad(() -> "Vive Lanczos 1", VRShaders.LANCZOS_PIPELINE, renderPass -> {
                renderPass.bindTexture(VRShaders.LANCZOS_COLOR_SAMPLER, source.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                renderPass.bindTexture(VRShaders.LANCZOS_DEPTH_SAMPLER, source.getDepthTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                renderPass.setUniform(LanczosUBO.UBO_NAME, VRShaders.LANCZOS_UBO.getBuffer());
            }, firstPass.getColorTextureView());
            VRShaders.LANCZOS_UBO.endFrame();

            VRShaders.LANCZOS_UBO.updateBuffer(0F, 1.0F / (3.0F * (float) secondPass.height));
            // second pass, vertical
            renderFullscreenQuad(() -> "Vive Lanczos 2", VRShaders.LANCZOS_PIPELINE, renderPass -> {
                renderPass.bindTexture(VRShaders.LANCZOS_COLOR_SAMPLER, firstPass.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
                renderPass.bindTexture(VRShaders.LANCZOS_DEPTH_SAMPLER, firstPass.getDepthTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                renderPass.setUniform(LanczosUBO.UBO_NAME, VRShaders.LANCZOS_UBO.getBuffer());
            }, secondPass.getColorTextureView());
        }
    }

    /**
     * blits the gui to the mirror with alpha blending
     * the gui is centered in the middle and at the bottom, scaled to completely fit
     */
    public static void blitGui() {
        if (DATA_HOLDER.vrSettings.guiOnMirror == VRSettings.MirrorGui.OFF ||
            (DATA_HOLDER.vrSettings.guiOnMirror == VRSettings.MirrorGui.HUD_ONLY && MC.screen != null))
        {
            return;
        }

        float mirrorAspect = (float) MC.mainRenderTarget.width / (float) MC.mainRenderTarget.height;
        float guiAspect = (float) GuiHandler.GUI_FRAMEBUFFER.width / (float) GuiHandler.GUI_FRAMEBUFFER.height;

        float xMin = 0;
        float yMin = 0;
        float xMax = 1.0F;
        float yMax = 1.0F;

        if (mirrorAspect > guiAspect) {
            // mirror is wider than the gui
            // limit the width, so the complete height is filled
            float aspect = (guiAspect / mirrorAspect) * 0.5F;

            xMin = 0.5F - aspect;
            xMax = 0.5F + aspect;
        } else {
            // mirror is taller than the gui
            // limit the height, so the complete width is filled
            // and shift the gui to the bottom
            yMax = (mirrorAspect / guiAspect);
        }

        int x = (int) (xMin * MC.mainRenderTarget.width);
        int y = (int) (yMin * MC.mainRenderTarget.height);
        int width = (int) (xMax * MC.mainRenderTarget.width) - x;
        int height = (int) (yMax * MC.mainRenderTarget.height) - y;

        blitToScreen(GuiHandler.GUI_FRAMEBUFFER, x, width, height, y, 0, 0, true, true);
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
     * @param blend       if alpha blending should be used
     */
    public static void blitToScreen(
        RenderTarget source, int left, int width, int height, int top, float xCropFactor, float yCropFactor,
        boolean keepAspect, boolean blend)
    {
        RenderSystem.assertOnRenderThread();

        float drawAspect = (float) width / (float) height;
        float bufferAspect = (float) source.width / (float) source.height;

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
        float xMinPos = (float) left / MC.getMainRenderTarget().width * 2F - 1F;
        float yMinPos = (float) top / MC.getMainRenderTarget().height * 2F - 1F;
        float xMaxPos = xMinPos + (float) width / MC.getMainRenderTarget().width * 2F;
        float yMaxPos = yMinPos + (float) height / MC.getMainRenderTarget().height * 2F;

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
                .createRenderPass(() -> "Vive Blit", MC.getMainRenderTarget().getColorTextureView(),
                    OptionalInt.empty()))
            {
                if (blend) {
                    renderPass.setPipeline(VRShaders.BLIT_VR_BLEND_PIPELINE);
                } else {
                    renderPass.setPipeline(VRShaders.BLIT_VR_PIPELINE);
                }
                renderPass.setVertexBuffer(0, gpuBuffer);

                renderPass.bindTexture(VRShaders.BLIT_VR_COLOR_SAMPLER, source.getColorTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

                renderPass.setIndexBuffer(indexBuffer, indexType);
                renderPass.drawIndexed(0, 0, 6, 1);
            }
        }
    }

    /**
     * blits the given {@code source} RenderTarget to the given {@code target} RenderTarget buffer
     *
     * @param source RenderTarget to copy
     * @param target RenderTarget to draw to
     * @param blend  if alpha blending should be used
     */
    public static void blit(RenderTarget source, RenderTarget target, boolean blend) {
        RenderSystem.assertOnRenderThread();

        renderFullscreenQuad(() -> "Vive Blit",
            blend ? VRShaders.BLIT_VR_BLEND_PIPELINE : VRShaders.BLIT_VR_PIPELINE,
            pass -> pass.bindTexture(VRShaders.BLIT_VR_COLOR_SAMPLER, source.getColorTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
            target.getColorTextureView());
    }
}
