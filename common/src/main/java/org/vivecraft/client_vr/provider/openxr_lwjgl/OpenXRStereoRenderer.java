package org.vivecraft.client_vr.provider.openxr_lwjgl;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.openxr.XR10.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * VRRenderer implementation for OpenXR using D3D11 swapchains + WGL_NV_DX_interop2.
 *
 * Rendering pipeline:
 * 1. Minecraft renders to mod-owned OpenGL textures (LeftEyeTextureId, RightEyeTextureId)
 * 2. In endFrame(), we acquire D3D11 swapchain images from OpenXR
 * 3. Lock the interop GL textures (registered from D3D11 textures) for GL access
 * 4. Blit from mod-owned GL textures to the interop GL textures
 * 5. Unlock the interop textures (flushes to D3D11)
 * 6. Release swapchain images and call xrEndFrame
 */
public class OpenXRStereoRenderer extends VRRenderer {

    private final MCOpenXR openxr;

    // OpenXR swapchains (D3D11-backed)
    private XrSwapchain leftSwapchain;
    private XrSwapchain rightSwapchain;

    // D3D11 swapchain texture pointers (runtime-owned, one per swapchain image)
    private long[] leftD3D11Textures;
    private long[] rightD3D11Textures;

    // Intermediate D3D11 textures that WE own (one per eye, created with SHARED flag for interop)
    private long leftIntermediateD3D11;
    private long rightIntermediateD3D11;

    // OpenGL texture names registered via WGL_NV_DX_interop2 for the intermediate textures
    private int leftInteropGLTexture;
    private int rightInteropGLTexture;

    // WGL interop handles for the intermediate textures (one per eye)
    private long leftInteropHandle;
    private long rightInteropHandle;

    // DXGI format used for swapchain (stored so intermediate textures match)
    private int swapchainDxgiFormat;

    // Framebuffer objects for blitting
    private int leftBlitFBO;
    private int rightBlitFBO;
    private int readBlitFBO;  // Shared read FBO reused every frame

    // Swapchain dimensions
    private int swapchainWidth;
    private int swapchainHeight;

    // Composition layer data
    private XrCompositionLayerProjectionView.Buffer projectionLayerViews;

    public OpenXRStereoRenderer(MCVR vr) {
        super(vr);
        this.openxr = (MCOpenXR) vr;
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution == null) {
            XrViewConfigurationView.Buffer viewConfigs = this.openxr.getViewConfigs();
            if (viewConfigs == null || viewConfigs.capacity() < 2) {
                this.resolution = new Tuple<>(2048, 2048);
            } else {
                int width = viewConfigs.get(0).recommendedImageRectWidth();
                int height = viewConfigs.get(0).recommendedImageRectHeight();
                this.resolution = new Tuple<>(width, height);
                VRSettings.LOGGER.info("Vivecraft: OpenXR Render Res {}x{}", width, height);
            }
            this.ss = 1.0F; // No SteamVR supersampling; user controls via mod settings
        }
        return this.resolution;
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        XrView.Buffer views = this.openxr.getViews();
        if (views == null || this.openxr.getViewCount() < 2) {
            // Fallback: standard symmetric projection
            return new Matrix4f().setPerspective(
                (float) Math.toRadians(90.0), 1.0F, nearClip, farClip);
        }

        XrView view = views.get(eyeType);
        return OpenXRUtil.fovToProjectionMatrix(view.fov(), nearClip, Math.min(farClip, Float.MAX_VALUE));
    }

    @Override
    public void createRenderTexture(int width, int height) {
        // Create mod-owned textures (same as OpenVR) for the framebuffers
        int boundTextureId = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);

        // Left eye texture
        this.LeftEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.LeftEyeTextureId);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0,
            GL11C.GL_RGBA, GL11C.GL_INT, null);

        // Right eye texture
        this.RightEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.RightEyeTextureId);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0,
            GL11C.GL_RGBA, GL11C.GL_INT, null);

        GlStateManager._bindTexture(boundTextureId);

        // Create OpenXR swapchains (D3D11-backed)
        this.swapchainWidth = width;
        this.swapchainHeight = height;
        createSwapchains(width, height);

        // Create blit FBOs (pre-allocated, reused every frame)
        this.leftBlitFBO = glGenFramebuffers();
        this.rightBlitFBO = glGenFramebuffers();
        this.readBlitFBO = glGenFramebuffers();

        this.lastError = RenderHelper.checkGLError("create OpenXR render textures");
    }

    private void createSwapchains(int width, int height) {
        XrSession session = this.openxr.getSession();
        if (session == null) return;

        try (MemoryStack stack = stackPush()) {
            // Enumerate supported swapchain formats (these are DXGI_FORMAT values for D3D11)
            IntBuffer formatCount = stack.callocInt(1);
            xrEnumerateSwapchainFormats(session, formatCount, null);
            long[] formats = new long[formatCount.get(0)];
            if (formats.length > 0) {
                var formatBuffer = stack.callocLong(formats.length);
                formatCount.put(0, formats.length);
                xrEnumerateSwapchainFormats(session, formatCount, formatBuffer);
                StringBuilder fmtStr = new StringBuilder();
                for (int i = 0; i < formats.length; i++) {
                    formats[i] = formatBuffer.get(i);
                    if (i > 0) fmtStr.append(", ");
                    fmtStr.append(formats[i]);
                }
                VRSettings.LOGGER.info("Vivecraft: OpenXR D3D11 swapchain formats: {}", fmtStr);
            }

            // Pick format: prefer non-SRGB for better WGL_NV_DX_interop2 compatibility.
            // SRGB formats (29, 91) have known sharing limitations with the interop extension.
            // DXGI_FORMAT_R8G8B8A8_UNORM = 28 (preferred)
            // DXGI_FORMAT_B8G8R8A8_UNORM = 87
            // DXGI_FORMAT_R8G8B8A8_UNORM_SRGB = 29 (fallback)
            // DXGI_FORMAT_B8G8R8A8_UNORM_SRGB = 91 (fallback)
            long chosenFormat = 28; // DXGI_FORMAT_R8G8B8A8_UNORM default
            // First pass: look for non-SRGB formats
            for (long fmt : formats) {
                if (fmt == 28) { // DXGI_FORMAT_R8G8B8A8_UNORM - best choice
                    chosenFormat = 28;
                    break;
                } else if (fmt == 87) { // DXGI_FORMAT_B8G8R8A8_UNORM
                    chosenFormat = 87;
                    break;
                }
            }
            // Second pass: if no non-SRGB found, accept SRGB
            if (chosenFormat == 28) {
                boolean foundExact = false;
                for (long fmt : formats) {
                    if (fmt == 28) { foundExact = true; break; }
                }
                if (!foundExact) {
                    for (long fmt : formats) {
                        if (fmt == 29 || fmt == 87 || fmt == 91) {
                            chosenFormat = fmt;
                            break;
                        }
                    }
                }
            }
            VRSettings.LOGGER.info("Vivecraft: OpenXR D3D11 swapchain chosen format: {} (DXGI_FORMAT)",
                chosenFormat);

            this.swapchainDxgiFormat = (int) chosenFormat;

            // Create left swapchain
            this.leftSwapchain = createSwapchain(session, width, height, chosenFormat, stack);
            this.leftD3D11Textures = enumerateD3D11SwapchainImages(this.leftSwapchain, stack);

            // Create right swapchain
            this.rightSwapchain = createSwapchain(session, width, height, chosenFormat, stack);
            this.rightD3D11Textures = enumerateD3D11SwapchainImages(this.rightSwapchain, stack);

            VRSettings.LOGGER.info("Vivecraft: OpenXR D3D11 swapchains created ({} images each)",
                this.leftD3D11Textures.length);

            // Create INTERMEDIATE D3D11 textures that we own (one per eye).
            // Runtime-owned swapchain textures often can't be directly registered with
            // WGL_NV_DX_interop2, so we create our own textures with D3D11_RESOURCE_MISC_SHARED
            // flag, register those for interop, render into them via GL, then CopyResource
            // from intermediate -> swapchain texture each frame.
            D3D11InteropHelper interop = this.openxr.getD3D11Interop();
            if (interop != null) {
                // Create intermediate textures
                this.leftIntermediateD3D11 = interop.createTexture2D(width, height, this.swapchainDxgiFormat);
                this.rightIntermediateD3D11 = interop.createTexture2D(width, height, this.swapchainDxgiFormat);

                if (this.leftIntermediateD3D11 == 0 || this.rightIntermediateD3D11 == 0) {
                    VRSettings.LOGGER.error("Vivecraft: Failed to create intermediate D3D11 textures!");
                } else {
                    // Register the intermediate textures (not the swapchain textures!) for GL interop
                    this.leftInteropGLTexture = GlStateManager._genTexture();
                    this.leftInteropHandle = interop.registerTexture(
                        this.leftIntermediateD3D11, this.leftInteropGLTexture);
                    VRSettings.LOGGER.info("Vivecraft: Left intermediate: D3D11=0x{} -> GL={} (interop=0x{})",
                        Long.toHexString(this.leftIntermediateD3D11),
                        this.leftInteropGLTexture, Long.toHexString(this.leftInteropHandle));

                    this.rightInteropGLTexture = GlStateManager._genTexture();
                    this.rightInteropHandle = interop.registerTexture(
                        this.rightIntermediateD3D11, this.rightInteropGLTexture);
                    VRSettings.LOGGER.info("Vivecraft: Right intermediate: D3D11=0x{} -> GL={} (interop=0x{})",
                        Long.toHexString(this.rightIntermediateD3D11),
                        this.rightInteropGLTexture, Long.toHexString(this.rightInteropHandle));

                    if (this.leftInteropHandle == 0 || this.rightInteropHandle == 0) {
                        VRSettings.LOGGER.error("Vivecraft: Failed to register intermediate textures for interop!");
                    }
                }
            }

            // Allocate composition layer views
            this.projectionLayerViews = XrCompositionLayerProjectionView.calloc(2);
            for (int i = 0; i < 2; i++) {
                this.projectionLayerViews.get(i)
                    .type(XR_TYPE_COMPOSITION_LAYER_PROJECTION_VIEW);
            }
        }
    }

    private XrSwapchain createSwapchain(XrSession session, int width, int height, long format,
                                         MemoryStack stack)
    {
        XrSwapchainCreateInfo createInfo = XrSwapchainCreateInfo.calloc(stack)
            .type(XR_TYPE_SWAPCHAIN_CREATE_INFO)
            .usageFlags(XR_SWAPCHAIN_USAGE_COLOR_ATTACHMENT_BIT | XR_SWAPCHAIN_USAGE_TRANSFER_DST_BIT)
            .format(format)
            .sampleCount(1)
            .width(width)
            .height(height)
            .faceCount(1)
            .arraySize(1)
            .mipCount(1);

        PointerBuffer swapchainPtr = stack.callocPointer(1);
        int result = xrCreateSwapchain(session, createInfo, swapchainPtr);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: Failed to create OpenXR swapchain: {}",
                OpenXRUtil.resultToString(result));
            return null;
        }
        return new XrSwapchain(swapchainPtr.get(0), session);
    }

    /**
     * Enumerates D3D11 swapchain images as raw texture pointers.
     * Since LWJGL doesn't have XrSwapchainImageD3D11KHR, we use raw ByteBuffers.
     *
     * Layout of XrSwapchainImageD3D11KHR (64-bit):
     *   XrStructureType type;          // offset 0, 4 bytes
     *   [4 bytes padding]
     *   void* next;                    // offset 8, 8 bytes
     *   ID3D11Texture2D* texture;      // offset 16, 8 bytes
     * Total: 24 bytes per image
     */
    private long[] enumerateD3D11SwapchainImages(XrSwapchain swapchain, MemoryStack stack) {
        if (swapchain == null) return new long[0];

        IntBuffer imageCount = stack.callocInt(1);
        xrEnumerateSwapchainImages(swapchain, imageCount, null);
        int count = imageCount.get(0);

        // Allocate raw buffer for D3D11 swapchain images
        int structSize = 24; // sizeof(XrSwapchainImageD3D11KHR) on 64-bit
        ByteBuffer images = MemoryUtil.memCalloc(count * structSize);

        // Set type field for each image
        for (int i = 0; i < count; i++) {
            images.putInt(i * structSize, D3D11InteropHelper.XR_TYPE_SWAPCHAIN_IMAGE_D3D11_KHR);
        }

        imageCount.put(0, count);
        xrEnumerateSwapchainImages(swapchain, imageCount,
            XrSwapchainImageBaseHeader.create(MemoryUtil.memAddress(images), count));

        long[] textures = new long[count];
        for (int i = 0; i < count; i++) {
            // Read the ID3D11Texture2D* pointer at offset 16 within each struct
            textures[i] = MemoryUtil.memGetAddress(MemoryUtil.memAddress(images) + (long) i * structSize + 16);
        }

        MemoryUtil.memFree(images);
        return textures;
    }

    @Override
    public void endFrame() throws RenderConfigException {
        if (!this.openxr.isFrameStarted()) return;

        XrSession session = this.openxr.getSession();
        XrFrameState frameState = this.openxr.getFrameState();

        if (session == null || frameState == null) {
            this.openxr.setFrameStarted(false);
            return;
        }

        D3D11InteropHelper interop = this.openxr.getD3D11Interop();

        // Wrap everything in try-finally to guarantee xrEndFrame is always called when
        // xrBeginFrame was called. Orphaning a begun frame causes the runtime to stall
        // (xrWaitFrame blocks forever on the next call).
        boolean frameShouldRender = false;
        boolean frameEnded = false;
        try (MemoryStack stack = stackPush()) {
            frameShouldRender = frameState.shouldRender()
                && this.leftSwapchain != null && this.rightSwapchain != null
                && interop != null && this.leftInteropHandle != 0 && this.rightInteropHandle != 0;

            if (frameShouldRender) {
                // === Left eye ===
                // 1. Acquire the swapchain image (tells runtime which D3D11 texture slot to use)
                int leftIdx = acquireAndWaitSwapchainImage(this.leftSwapchain, stack);
                if (leftIdx >= 0 && leftIdx < this.leftD3D11Textures.length) {
                    // 2. Lock our intermediate texture for GL access
                    boolean locked = interop.lockObjects(this.leftInteropHandle);
                    if (locked) {
                        // 3. Blit from mod-owned GL texture -> intermediate GL texture (interop-registered)
                        blitTexture(this.LeftEyeTextureId, this.leftInteropGLTexture,
                            this.leftBlitFBO, this.swapchainWidth, this.swapchainHeight);

                        // 4. Flush GL commands before unlocking
                        GL11C.glFlush();

                        // 5. Unlock (flushes GL writes to the D3D11 intermediate texture)
                        interop.unlockObjects(this.leftInteropHandle);

                        // 6. CopyResource: intermediate D3D11 texture -> runtime swapchain D3D11 texture
                        interop.copyResource(this.leftD3D11Textures[leftIdx], this.leftIntermediateD3D11);
                    }

                    // 7. Release the swapchain image (always, even if lock failed, to keep swapchain valid)
                    releaseSwapchainImage(this.leftSwapchain, stack);
                }

                // === Right eye ===
                int rightIdx = acquireAndWaitSwapchainImage(this.rightSwapchain, stack);
                if (rightIdx >= 0 && rightIdx < this.rightD3D11Textures.length) {
                    boolean locked = interop.lockObjects(this.rightInteropHandle);
                    if (locked) {
                        blitTexture(this.RightEyeTextureId, this.rightInteropGLTexture,
                            this.rightBlitFBO, this.swapchainWidth, this.swapchainHeight);

                        GL11C.glFlush();
                        interop.unlockObjects(this.rightInteropHandle);

                        interop.copyResource(this.rightD3D11Textures[rightIdx], this.rightIntermediateD3D11);
                    }

                    releaseSwapchainImage(this.rightSwapchain, stack);
                }

                // Build composition layer
                XrView.Buffer views = this.openxr.getViews();
                if (views != null && this.openxr.getViewCount() >= 2) {
                    for (int eye = 0; eye < 2; eye++) {
                        XrCompositionLayerProjectionView layerView = this.projectionLayerViews.get(eye);
                        layerView.pose(views.get(eye).pose());
                        layerView.fov(views.get(eye).fov());
                        layerView.subImage()
                            .swapchain(eye == 0 ? this.leftSwapchain : this.rightSwapchain)
                            .imageArrayIndex(0);
                        layerView.subImage().imageRect()
                            .offset(XrOffset2Di.calloc(stack).set(0, 0))
                            .extent(XrExtent2Di.calloc(stack)
                                .set(this.swapchainWidth, this.swapchainHeight));
                    }

                    XrCompositionLayerProjection layer = XrCompositionLayerProjection.calloc(stack)
                        .type(XR_TYPE_COMPOSITION_LAYER_PROJECTION)
                        .space(this.openxr.getAppSpace())
                        .views(this.projectionLayerViews);

                    PointerBuffer layersPtr = stack.callocPointer(1);
                    layersPtr.put(0, XrCompositionLayerBaseHeader.create(layer.address()));

                    XrFrameEndInfo endInfo = XrFrameEndInfo.calloc(stack)
                        .type(XR_TYPE_FRAME_END_INFO)
                        .displayTime(frameState.predictedDisplayTime())
                        .environmentBlendMode(XR_ENVIRONMENT_BLEND_MODE_OPAQUE)
                        .layers(layersPtr);

                    int result = xrEndFrame(session, endInfo);
                    frameEnded = true;
                    if (result < 0) {
                        VRSettings.LOGGER.error("Vivecraft: xrEndFrame failed: {}",
                            OpenXRUtil.resultToString(result));
                    }
                }
            }

            // If we didn't submit a frame with layers, still end it (empty frame)
            if (!frameEnded) {
                XrFrameEndInfo endInfo = XrFrameEndInfo.calloc(stack)
                    .type(XR_TYPE_FRAME_END_INFO)
                    .displayTime(frameState.predictedDisplayTime())
                    .environmentBlendMode(XR_ENVIRONMENT_BLEND_MODE_OPAQUE);
                xrEndFrame(session, endInfo);
            }
        } catch (Exception e) {
            // If anything went wrong, try to end the frame to avoid runtime stall.
            // This is a last-resort safety net.
            if (!frameEnded) {
                try (MemoryStack stack = stackPush()) {
                    XrFrameEndInfo endInfo = XrFrameEndInfo.calloc(stack)
                        .type(XR_TYPE_FRAME_END_INFO)
                        .displayTime(frameState.predictedDisplayTime())
                        .environmentBlendMode(XR_ENVIRONMENT_BLEND_MODE_OPAQUE);
                    xrEndFrame(session, endInfo);
                } catch (Exception ignored) {
                    // Nothing more we can do
                }
            }
            VRSettings.LOGGER.error("Vivecraft: Error in OpenXR endFrame", e);
        } finally {
            this.openxr.setFrameStarted(false);
        }
    }

    private int acquireAndWaitSwapchainImage(XrSwapchain swapchain, MemoryStack stack) {
        XrSwapchainImageAcquireInfo acquireInfo = XrSwapchainImageAcquireInfo.calloc(stack)
            .type(XR_TYPE_SWAPCHAIN_IMAGE_ACQUIRE_INFO);

        IntBuffer indexBuf = stack.callocInt(1);
        int result = xrAcquireSwapchainImage(swapchain, acquireInfo, indexBuf);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: xrAcquireSwapchainImage failed: {}",
                OpenXRUtil.resultToString(result));
            return -1;
        }

        XrSwapchainImageWaitInfo waitInfo = XrSwapchainImageWaitInfo.calloc(stack)
            .type(XR_TYPE_SWAPCHAIN_IMAGE_WAIT_INFO)
            .timeout(XR_INFINITE_DURATION);

        result = xrWaitSwapchainImage(swapchain, waitInfo);
        if (result < 0) {
            VRSettings.LOGGER.error("Vivecraft: xrWaitSwapchainImage failed: {}",
                OpenXRUtil.resultToString(result));
            // Must still release the acquired image to avoid leaving swapchain in broken state.
            // OpenXR requires acquire → wait → release sequence; skipping release would
            // cause all subsequent acquires to fail.
            releaseSwapchainImage(swapchain, stack);
            return -1;
        }

        return indexBuf.get(0);
    }

    private void releaseSwapchainImage(XrSwapchain swapchain, MemoryStack stack) {
        XrSwapchainImageReleaseInfo releaseInfo = XrSwapchainImageReleaseInfo.calloc(stack)
            .type(XR_TYPE_SWAPCHAIN_IMAGE_RELEASE_INFO);
        xrReleaseSwapchainImage(swapchain, releaseInfo);
    }

    private void blitTexture(int srcTexture, int dstTexture, int blitFBO, int width, int height) {
        // Bind dst texture to the write FBO
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, blitFBO);
        GL30C.glFramebufferTexture2D(GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D,
            dstTexture, 0);

        // Bind src texture to the pre-allocated read FBO (no per-frame allocation)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, this.readBlitFBO);
        GL30C.glFramebufferTexture2D(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D,
            srcTexture, 0);

        // Blit with Y-flip: OpenGL has origin at bottom-left, but D3D11/OpenXR expects top-left.
        // Flip the destination Y coordinates (draw from top to bottom) so the image appears
        // right-side-up when the D3D11 texture is submitted to the OpenXR compositor.
        GL30C.glBlitFramebuffer(
            0, 0, width, height,       // src: bottom-left to top-right (normal GL orientation)
            0, height, width, 0,       // dst: top-left to bottom-right (flipped Y)
            GL11C.GL_COLOR_BUFFER_BIT, GL11C.GL_NEAREST);

        // Unbind
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    @Override
    public boolean providesStencilMask() {
        return false; // Can be implemented later with XR_KHR_visibility_mask
    }

    @Override
    public String getName() {
        return "OpenXR";
    }

    @Override
    protected void destroyBuffers() {
        super.destroyBuffers();

        if (this.LeftEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.LeftEyeTextureId);
            this.LeftEyeTextureId = -1;
        }
        if (this.RightEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.RightEyeTextureId);
            this.RightEyeTextureId = -1;
        }
        if (this.leftBlitFBO > 0) {
            glDeleteFramebuffers(this.leftBlitFBO);
            this.leftBlitFBO = 0;
        }
        if (this.rightBlitFBO > 0) {
            glDeleteFramebuffers(this.rightBlitFBO);
            this.rightBlitFBO = 0;
        }
        if (this.readBlitFBO > 0) {
            glDeleteFramebuffers(this.readBlitFBO);
            this.readBlitFBO = 0;
        }
    }

    @Override
    public void destroy() {
        // Unregister interop textures before destroying anything
        D3D11InteropHelper interop = this.openxr.getD3D11Interop();
        if (interop != null) {
            if (this.leftInteropHandle != 0) {
                interop.unregisterTexture(this.leftInteropHandle);
                this.leftInteropHandle = 0;
            }
            if (this.rightInteropHandle != 0) {
                interop.unregisterTexture(this.rightInteropHandle);
                this.rightInteropHandle = 0;
            }
        }

        // Delete the interop GL texture names
        if (this.leftInteropGLTexture > 0) {
            GlStateManager._deleteTexture(this.leftInteropGLTexture);
            this.leftInteropGLTexture = 0;
        }
        if (this.rightInteropGLTexture > 0) {
            GlStateManager._deleteTexture(this.rightInteropGLTexture);
            this.rightInteropGLTexture = 0;
        }

        // Release intermediate D3D11 textures (these are ours, not the runtime's)
        if (this.leftIntermediateD3D11 != 0) {
            D3D11InteropHelper.releaseTexture(this.leftIntermediateD3D11);
            this.leftIntermediateD3D11 = 0;
        }
        if (this.rightIntermediateD3D11 != 0) {
            D3D11InteropHelper.releaseTexture(this.rightIntermediateD3D11);
            this.rightIntermediateD3D11 = 0;
        }

        super.destroy();

        // Destroy swapchains (must happen before session is destroyed).
        // If the session was already destroyed by MCOpenXR.destroy(), these calls
        // will fail gracefully — we catch and log any errors.
        try {
            if (this.leftSwapchain != null) {
                xrDestroySwapchain(this.leftSwapchain);
                this.leftSwapchain = null;
            }
        } catch (Exception e) {
            VRSettings.LOGGER.warn("Vivecraft: Error destroying left swapchain: {}", e.getMessage());
            this.leftSwapchain = null;
        }
        try {
            if (this.rightSwapchain != null) {
                xrDestroySwapchain(this.rightSwapchain);
                this.rightSwapchain = null;
            }
        } catch (Exception e) {
            VRSettings.LOGGER.warn("Vivecraft: Error destroying right swapchain: {}", e.getMessage());
            this.rightSwapchain = null;
        }
        if (this.projectionLayerViews != null) {
            this.projectionLayerViews.free();
            this.projectionLayerViews = null;
        }

        // Now that swapchains and interop textures are cleaned up,
        // it's safe to destroy the XR session, instance, and D3D11 interop.
        this.openxr.destroySessionAndInterop();
    }
}
