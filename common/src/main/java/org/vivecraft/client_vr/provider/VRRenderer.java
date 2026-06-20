package org.vivecraft.client_vr.provider;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.vivecraft.Xplat;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client.utils.StencilHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.resolutioncontrol.ResolutionControlHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public abstract class VRRenderer {
    // projection matrices
    public Matrix4f[] eyeProj = new Matrix4f[2];
    public Matrix4f[] eyeReverseProj = new Matrix4f[2];
    protected float lastFarClip = 0F;
    protected float lastReverseFarClip = 0F;

    // render buffers
    public final VRTextureTarget[] framebufferEye = new VRTextureTarget[2];
    public RenderTarget framebufferMR;
    public RenderTarget framebufferUndistorted;
    public RenderTarget framebufferVrRender;
    public RenderTarget fsaaFirstPassResultFBO;
    public RenderTarget fsaaLastPassResultFBO;
    public RenderTarget cameraFramebuffer;
    public RenderTarget cameraRenderFramebuffer;
    public RenderTarget telescopeFramebufferL;
    public RenderTarget telescopeFramebufferR;
    public RenderTarget mirrorFramebuffer;

    // Stencil mesh buffer for each eye
    protected float[][] hiddenMeshVertices = new float[2][];
    private final GpuBuffer[] bakedHiddenMesh = new GpuBuffer[2];

    // variables to check setting changes that need framebuffers reinits/resizes
    private boolean improvedTransparency = false;
    protected VRSettings.MirrorMode lastMirror;
    public long lastWindow = 0L;
    public int mirrorFBHeight;
    public int mirrorFBWidth;
    protected boolean reinitFrameBuffers = true;
    protected boolean resizeFrameBuffers = false;
    public float renderScale;

    // render resolution set by the VR runtime, includes the supersampling factor
    protected Vector2ic resolution;

    // supersampling set by the vr runtime
    public float ss = -1.0F;
    protected MCVR vr;

    // last error caused by this renderer
    protected String lastError = "";

    private final ProjectionMatrixBuffer stencilProjectionMatrix = new ProjectionMatrixBuffer("stencil");

    public VRRenderer(MCVR vr) {
        this.vr = vr;
    }

    /**
     * @throws RenderConfigException if the current graphics setup is unsupported
     */
    public void checkCapabilities() throws RenderConfigException {
        this.checkIfSupportedGpu();
    }

    /**
     * creates the textures needed for the VR runtime to submit the frames
     *
     * @param width  width of the texture
     * @param height height of the texture
     */
    public abstract void createRenderTexture(int width, int height);

    /**
     * gets the cached projection matrix if the farClip distance matches with the last, else gets a new one from the VR runtime
     *
     * @param eyeType  which eye to get the projection matrix for, 0 = Left, 1 = Right
     * @param nearClip near clip plane of the projection matrix
     * @param farClip  far clip plane of the projection matrix
     * @return the projection matrix
     */
    public Matrix4f getCachedProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (farClip != this.lastFarClip) {
            this.lastFarClip = farClip;
            // fetch both at the same time to make sure they use the same clip planes
            this.eyeProj[0] = this.getProjectionMatrix(0, nearClip, farClip);
            this.eyeProj[1] = this.getProjectionMatrix(1, nearClip, farClip);
        }

        return this.eyeProj[eyeType];
    }

    /**
     * gets the cached reversed projection matrix if the farClip distance matches with the last, else gets a new one from the VR runtime
     *
     * @param eyeType  which eye to get the projection matrix for, 0 = Left, 1 = Right
     * @param nearClip near clip plane of the projection matrix
     * @param farClip  far clip plane of the projection matrix
     * @return the reversed projection matrix
     */
    public Matrix4f getCachedReverseProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (farClip != this.lastReverseFarClip) {
            this.lastReverseFarClip = farClip;
            // fetch both at the same time to make sure they use the same clip planes
            this.eyeReverseProj[0] = this.getProjectionMatrix(0, farClip, nearClip);
            this.eyeReverseProj[1] = this.getProjectionMatrix(1, farClip, nearClip);
        }

        return this.eyeReverseProj[eyeType];
    }

    /**
     * gets the projection matrix from the vr runtime with the given parameters
     *
     * @param eyeType  which eye to get the projection matrix for, 0 = Left, 1 = Right
     * @param nearClip near clip plane of the projection matrix
     * @param farClip  far clip plane of the projection matrix
     * @return the projection matrix
     */
    protected abstract Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip);

    /**
     * this is the last thing to call after all passes are rendered.
     * Submits the rendered VR views to the VR runtime
     *
     * @throws RenderConfigException when the VR runtime throws any errors
     */
    public abstract void endFrame() throws RenderConfigException;

    /**
     * @return if this VRRenderer provides stencil for the left/right RenderPass
     */
    public abstract boolean providesStencilMask();

    /**
     * gets an array with the vertex info of the stencil mesh, if there is one provided by this renderer
     *
     * @param eye which eye the stencil should be for
     * @return the stencil for that eye, if available
     */
    public float[] getStencilMask(RenderPass eye) {
        if (eye == RenderPass.LEFT || eye == RenderPass.RIGHT) {
            return eye == RenderPass.LEFT ? this.hiddenMeshVertices[0] : this.hiddenMeshVertices[1];
        } else {
            return null;
        }
    }

    /**
     * gets a baked GpuBuffer of the stencil mesh, if there is one provided by this renderer
     *
     * @param eye which eye the stencil should be for
     * @return the stencil for that eye, if available
     */
    public GpuBuffer getBakedStencilMask(RenderPass eye) {
        if (eye == RenderPass.LEFT || eye == RenderPass.RIGHT) {
            if (this.bakedHiddenMesh[0] == null || this.bakedHiddenMesh[1] == null) {
                this.bakeStencilMasks();
            }
            return eye == RenderPass.LEFT ? this.bakedHiddenMesh[0] : this.bakedHiddenMesh[1];
        } else {
            return null;
        }
    }

    /**
     * bakes the stencil mesh for both eyes, if they are available
     */
    public void bakeStencilMasks() {
        if (this.bakedHiddenMesh[0] != null) {
            this.bakedHiddenMesh[0].close();
            this.bakedHiddenMesh[0] = null;
        }
        if (this.bakedHiddenMesh[1] != null) {
            this.bakedHiddenMesh[1].close();
            this.bakedHiddenMesh[1] = null;
        }
        for (int i = 0; i < this.hiddenMeshVertices.length; ++i) {
            float[] vertices = this.hiddenMeshVertices[i];
            if (vertices == null) continue;
            try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                vertices.length / 2 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()))
            {
                BufferBuilder builder = new BufferBuilder(byteBufferBuilder, PrimitiveTopology.TRIANGLES,
                    DefaultVertexFormat.POSITION_COLOR);

                for (int v = 0; v < vertices.length; v += 2) {
                    builder.addVertex(vertices[v], vertices[v + 1], 0.0F)
                        .setColor(0, 0, 0, 255);
                }

                try (MeshData meshData = builder.buildOrThrow()) {
                    String eye = i == 0 ? "Left" : "Right";
                    this.bakedHiddenMesh[i] = RenderSystem.getDevice()
                        .createBuffer(() -> eye + " Stencil", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX,
                            meshData.vertexBuffer());
                }
            }
        }
    }

    /**
     * sets up the stencil rendering, and draws the stencil
     *
     * @param inverse if the stencil covered part, or the inverse of it should be drawn
     */
    public void doStencil(boolean inverse) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        // setup stencil for writing
        // TODO 1.21.5 no stencil for now
        /*
        if (StencilHelper.stencilBufferSupported()) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            RenderSystem.stencilMask(0xFF); // Write to stencil buffer
        }

        if (inverse) {
            // clear whole image for total mask in color, stencil, depth
            RenderSystem.clearStencil(0xFF);
            RenderSystem.clearDepth(0);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF); // Set any stencil to 0
            RenderSystem.colorMask(false, false, false, true);
        } else {
            // clear whole image for total transparency
            RenderSystem.clearStencil(0);
            RenderSystem.clearDepth(1);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF); // Set any stencil to 1
            RenderSystem.colorMask(true, true, true, true);
        }

        if (StencilHelper.stencilBufferSupported()) {
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        } else {
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT);
        }

        RenderSystem.clearStencil(0);
        */

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(this.stencilProjectionMatrix.getBuffer(
                new Matrix4f().setOrtho(0.0F, 1.0F, 0.0F, 1.0F, 20.0F, 0.0F)),
            ProjectionType.ORTHOGRAPHIC);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().identity();
        if (inverse) {
            // draw on far clip
            RenderSystem.getModelViewStack().translate(0, 0, -20);
        }

        if (dataholder.currentPass == RenderPass.SCOPEL || dataholder.currentPass == RenderPass.SCOPER) {
            drawCircle(1.0F, 1.0F);
        } else if (providesStencilMask() &&
            (dataholder.currentPass == RenderPass.LEFT || dataholder.currentPass == RenderPass.RIGHT))
        {
            drawMask();
        }

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.getModelViewStack().popMatrix();

        // TODO 1.21.5 stencil
        /*
        if (StencilHelper.stencilBufferSupported()) {
            RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 255, 1);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.stencilMask(0); // Dont Write to stencil buffer
        }
        */
    }

    /**
     * triangulates a circle and draws it
     *
     * @param width  width of the circle in screen pixels
     * @param height height of the circle in screen pixels
     */
    private void drawCircle(float width, float height) {
        // TODO
        /*RenderType renderType = VRRenderTypes.triangleFanAlways();
        VertexConsumer builder = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);

        final float edges = 32.0F;
        float radius = width / 2.0F;

        // put middle vertex
        builder.addVertex(radius, radius, 0.0F)
            .setColor(0, 0, 0, 255);

        // put outer vertices
        for (int i = 0; i < edges + 1; i++) {
            float startAngle = (float) i / edges * Mth.TWO_PI;
            builder.addVertex(
                    radius + Mth.cos(startAngle) * radius,
                    radius + Mth.sin(startAngle) * radius,
                    0.0F)
                .setColor(0, 0, 0, 255);
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(renderType);*/
    }

    /**
     * draws the stencil provided by the VR runtime
     */
    private void drawMask() {
        float[] verts = getStencilMask(ClientDataHolderVR.getInstance().currentPass);
        if (verts == null) {
            return;
        }
        GpuBuffer buffer = getBakedStencilMask(ClientDataHolderVR.getInstance().currentPass);
        if (buffer == null) {
            return;
        }

        int count = verts.length / 2;

        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.TRIANGLES);
        GpuBuffer indexBuffer = autoIndices.getBuffer(count);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
            .writeTransform(RenderSystem.getModelViewMatrixCopy());

        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        try (com.mojang.blaze3d.systems.RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(() -> "Stencil " + ClientDataHolderVR.getInstance().currentPass,
                target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty()))
        {
            renderPass.setPipeline(VRShaders.TRIANGLES_ALWAYS);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, buffer.slice());
            renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
            renderPass.drawIndexed(count, 1, 0, 0, 0);
        }
    }

    /**
     * @return String of any error that happened during init of the VR provider
     */
    public String getInitError() {
        return this.vr.initStatus;
    }

    /**
     * @return String with the last error, if there was one
     */
    public String getLastError() {
        return this.lastError;
    }

    /**
     * @return name of the VRRenderer
     */
    public abstract String getName();

    /**
     * @param includeNonRendered if set, will also include passes that are used with the current settings, but currently not rendered
     * @return a list of passes that need to be rendered
     */
    public List<RenderPass> getRenderPasses(boolean includeNonRendered) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        List<RenderPass> passes = new ArrayList<>();

        // Always do these for obvious reasons
        passes.add(RenderPass.LEFT);
        passes.add(RenderPass.RIGHT);

        // only do these, if the window is not minimized
        WindowExtension window = (WindowExtension) (Object) minecraft.getWindow();
        if (includeNonRendered ||
            window.vivecraft$getActualScreenWidth() > 0 && window.vivecraft$getActualScreenHeight() > 0)
        {
            if (dataholder.vrSettings.renderAllPasses) {
                passes.add(RenderPass.CENTER);
                passes.add(RenderPass.THIRD);
            } else if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
                passes.add(RenderPass.CENTER);
            } else if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                if (dataholder.vrSettings.mixedRealityUndistorted && dataholder.vrSettings.mixedRealityUnityLike) {
                    passes.add(RenderPass.CENTER);
                }

                passes.add(RenderPass.THIRD);
            } else if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                passes.add(RenderPass.THIRD);
            }
        }

        if (minecraft.player != null) {
            if (TelescopeTracker.isTelescope(minecraft.player.getMainHandItem()) && TelescopeTracker.isViewing(0)) {
                passes.add(RenderPass.SCOPER);
            }

            if (TelescopeTracker.isTelescope(minecraft.player.getOffhandItem()) && TelescopeTracker.isViewing(1)) {
                passes.add(RenderPass.SCOPEL);
            }

            if (dataholder.cameraTracker.isVisible() || dataholder.vrSettings.renderAllPasses) {
                passes.add(RenderPass.CAMERA);
            }
        }

        return passes;
    }

    /**
     * @return resolution of the headset view
     */
    public abstract Vector2ic getRenderTextureSizes();

    /**
     * calculates the resolution of first/third person mirror view
     *
     * @param eyeFBWidth      headset view width
     * @param eyeFBHeight     headset view height
     * @param resolutionScale render scale from 3rd party mods
     * @return resolution of the desktop view mirror
     */
    public Vector2i getMirrorTextureSize(int eyeFBWidth, int eyeFBHeight, float resolutionScale) {
        this.mirrorFBWidth = (int) Math.ceil(
            ((WindowExtension) (Object) Minecraft.getInstance().getWindow()).vivecraft$getActualScreenWidth() *
                resolutionScale);
        this.mirrorFBHeight = (int) Math.ceil(
            ((WindowExtension) (Object) Minecraft.getInstance().getWindow()).vivecraft$getActualScreenHeight() *
                resolutionScale);

        if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            this.mirrorFBWidth = this.mirrorFBWidth / 2;

            if (ClientDataHolderVR.getInstance().vrSettings.mixedRealityUnityLike) {
                this.mirrorFBHeight = this.mirrorFBHeight / 2;
            }
        }

        if (ShadersHelper.needsSameSizeBuffers()) {
            this.mirrorFBWidth = eyeFBWidth;
            this.mirrorFBHeight = eyeFBHeight;
        }
        return new Vector2i(this.mirrorFBWidth, this.mirrorFBHeight);
    }

    /**
     * calculates the resolution of the telescope view
     *
     * @param eyeFBWidth  headset view width
     * @param eyeFBHeight headset view height
     * @return resolution of the telescope view
     */
    public Vector2i getTelescopeTextureSize(int eyeFBWidth, int eyeFBHeight) {
        int telescopeFBwidth = 720;
        int telescopeFBheight = 720;

        if (ShadersHelper.needsSameSizeBuffers()) {
            telescopeFBwidth = eyeFBWidth;
            telescopeFBheight = eyeFBHeight;
        }
        return new Vector2i(telescopeFBwidth, telescopeFBheight);
    }

    /**
     * calculates the resolution of the screenshot camera view
     *
     * @param eyeFBWidth  headset view width
     * @param eyeFBHeight headset view height
     * @return resolution of the screenshot camera view
     */
    public Vector2i getCameraTextureSize(int eyeFBWidth, int eyeFBHeight) {
        int cameraFBwidth = Math.round(1920.0F * ClientDataHolderVR.getInstance().vrSettings.handCameraResScale);
        int cameraFBheight = Math.round(1080.0F * ClientDataHolderVR.getInstance().vrSettings.handCameraResScale);

        if (ShadersHelper.needsSameSizeBuffers()) {
            // correct for camera aspect, since that is 16:9
            float aspect = (float) cameraFBwidth / (float) cameraFBheight;
            if (aspect > (float) (eyeFBWidth / eyeFBHeight)) {
                cameraFBwidth = eyeFBWidth;
                cameraFBheight = Math.round((float) eyeFBWidth / aspect);
            } else {
                cameraFBwidth = Math.round((float) eyeFBHeight * aspect);
                cameraFBheight = eyeFBHeight;
            }
        }
        return new Vector2i(cameraFBwidth, cameraFBheight);
    }

    /**
     * @return if this is successfully initialized
     */
    public boolean isInitialized() {
        return this.vr.initSuccess;
    }

    /**
     * method to tell the vrRenderer, that render buffers changed
     * when all framebuffers are initialized a simple resize is called, else they are completely reinitialized
     *
     * @param cause cause that gets logged
     */
    public void reinitFrameBuffersMaybe(String cause) {
        if (this.allFramebuffersInitialized()) {
            this.resizeFrameBuffers(cause);
        } else {
            this.reinitFrameBuffers(cause);
        }
    }

    /**
     * method to tell the vrRenderer, that render buffers changed and need to be regenerated next frame
     *
     * @param cause cause that gets logged
     */
    public void reinitFrameBuffers(String cause) {
        if (!this.reinitFrameBuffers) {
            // only print the first cause
            VRSettings.LOGGER.info("Vivecraft: Reinit Render: {}", cause);
        }
        this.reinitFrameBuffers = true;
    }

    /**
     * method to tell the vrRenderer, that render buffers size changed and just need to be resized next frame
     *
     * @param cause cause that gets logged
     */
    public void resizeFrameBuffers(String cause) {
        if (!cause.isEmpty() && !this.resizeFrameBuffers) {
            VRSettings.LOGGER.info("Vivecraft: Resizing Buffers: {}", cause);
        }
        this.resizeFrameBuffers = true;
    }

    /**
     * @return if all framebuffers should be available
     */
    private boolean allFramebuffersInitialized() {
        // shaders have all passes created, only need a resize
        // full reload as well, to minimize reloads when changing settings
        return ShadersHelper.isShaderActive() || ClientDataHolderVR.getInstance().vrSettings.fullReloadOnInit;
    }

    /**
     * sets up rendering, and makes sure all buffers are generated and sized correctly
     *
     * @throws RenderConfigException in case something failed to initialize or the gpu vendor is unsupported
     * @throws IOException           can be thrown by the WorldRenderPass init when trying to load the shaders
     */
    public void setupRenderConfiguration() throws RenderConfigException, IOException {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        // check if window is still the same
        if (minecraft.getWindow().handle() != this.lastWindow) {
            this.lastWindow = minecraft.getWindow().handle();
            this.reinitFrameBuffers("Window Handle Changed");
        }

        if (this.lastMirror != dataholder.vrSettings.displayMirrorMode) {
            this.reinitFrameBuffersMaybe("Mirror Changed");
            this.lastMirror = dataholder.vrSettings.displayMirrorMode;
        }

        if ((this.framebufferMR == null || this.framebufferUndistorted == null) && this.allFramebuffersInitialized()) {
            this.reinitFrameBuffers("All buffers needed, but some buffers not initialized");
        }
        if (minecraft.options.improvedTransparency().get() != this.improvedTransparency) {
            this.improvedTransparency = minecraft.options.improvedTransparency().get();
            this.reinitFrameBuffers("improvedTransparency setting changed to: " + this.improvedTransparency);
        }

        if (this.resizeFrameBuffers && !this.reinitFrameBuffers) {
            Vector2ic size = this.getRenderTextureSizes();
            int eyew = size.x();
            int eyeh = size.y();

            float resolutionScale =
                ResolutionControlHelper.isLoaded() ? ResolutionControlHelper.getCurrentScaleFactor() : 1.0F;

            this.renderScale = (float) Math.sqrt(dataholder.vrSettings.renderScaleFactor) * resolutionScale;
            int eyeFBWidth = (int) Math.ceil(eyew * this.renderScale);
            int eyeFBHeight = (int) Math.ceil(eyeh * this.renderScale);

            Vector2i mirrorSize = getMirrorTextureSize(eyeFBWidth, eyeFBHeight, resolutionScale);
            Vector2i telescopeSize = getTelescopeTextureSize(eyeFBWidth, eyeFBHeight);
            Vector2i cameraSize = getCameraTextureSize(eyeFBWidth, eyeFBHeight);

            // main render target
            if (dataholder.vrSettings.vrUseStencil && StencilHelper.stencilBufferSupported()) {
                ((RenderTargetExtension) WorldRenderPass.STEREO_XR.target)
                    .vivecraft$setStencil(!Xplat.INSTANCE.enableRenderTargetStencil(WorldRenderPass.STEREO_XR.target));
            } else {
                ((RenderTargetExtension) WorldRenderPass.STEREO_XR.target).vivecraft$setStencil(false);
            }
            WorldRenderPass.STEREO_XR.resize(eyeFBWidth, eyeFBHeight);
            if (dataholder.vrSettings.useFsaa) {
                this.fsaaFirstPassResultFBO.resize(eyew, eyeFBHeight);
            }

            // mirror
            if (mirrorSize.x > 0 && mirrorSize.y > 0) {
                if (WorldRenderPass.CENTER != null) {
                    WorldRenderPass.CENTER.resize(mirrorSize.x, mirrorSize.y);
                }
                if (WorldRenderPass.MIXED_REALITY != null) {
                    WorldRenderPass.MIXED_REALITY.resize(mirrorSize.x, mirrorSize.y);
                }
                this.mirrorFramebuffer.resize(
                    Math.max(1, ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth()),
                    Math.max(1, ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight()));
            }

            // telescopes
            WorldRenderPass.LEFT_TELESCOPE.resize(telescopeSize.x, telescopeSize.y);
            WorldRenderPass.RIGHT_TELESCOPE.resize(telescopeSize.x, telescopeSize.y);

            // camera
            this.cameraFramebuffer.resize(cameraSize.x, cameraSize.y);
            if (ShadersHelper.needsSameSizeBuffers()) {
                WorldRenderPass.CAMERA.resize(eyeFBWidth, eyeFBHeight);
            } else {
                WorldRenderPass.CAMERA.resize(cameraSize.x, cameraSize.y);
            }

            // resize gui, if changed
            boolean mipmaps = dataholder.vrSettings.guiMipmaps;
            boolean mipmapChanged =
                mipmaps != ((RenderTargetExtension) GuiHandler.GUI_FRAMEBUFFER).vivecraft$hasMipmaps();
            if (GuiHandler.updateResolution() || mipmapChanged) {
                ((RenderTargetExtension) GuiHandler.GUI_FRAMEBUFFER).vivecraft$setMipmaps(mipmaps);
                GuiHandler.GUI_FRAMEBUFFER.resize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);

                ((RenderTargetExtension) RadialHandler.FRAMEBUFFER).vivecraft$setMipmaps(mipmaps);
                RadialHandler.FRAMEBUFFER.resize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);

                ((RenderTargetExtension) KeyboardHandler.FRAMEBUFFER).vivecraft$setMipmaps(mipmaps);
                KeyboardHandler.FRAMEBUFFER.resize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);
                if (minecraft.gui.screen() != null) {
                    int guiWidth = minecraft.getWindow().getGuiScaledWidth();
                    int guiHeight = minecraft.getWindow().getGuiScaledHeight();
                    minecraft.gui.screen().init(guiWidth, guiHeight);
                }
            }
            // need to recall this, for PostChains to get the right resize
            ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$resize();

            this.resizeFrameBuffers = false;
        }

        if (this.reinitFrameBuffers) {
            GraphicsHelper.INSTANCE.checkError("Start Init");

            if (!this.isInitialized()) {
                throw new RenderConfigException(
                    Component.translatable("vivecraft.messages.renderiniterror", this.getName()),
                    Component.literal(this.getInitError()));
            }

            Vector2ic size = this.getRenderTextureSizes();
            int eyew = size.x();
            int eyeh = size.y();

            destroyBuffers();

            this.createRenderTexture(eyew, eyeh);

            if (this.framebufferEye[0] == null || this.framebufferEye[1] == null) {
                throw new RenderConfigException(
                    Component.translatable("vivecraft.messages.renderiniterror", this.getName()),
                    Component.literal(this.getLastError()));
            }

            VRSettings.LOGGER.info("Vivecraft: VR Provider supplied texture resolution: {} x {}", eyew, eyeh);

            GraphicsHelper.INSTANCE.checkError("Render Texture setup");

            float resolutionScale =
                ResolutionControlHelper.isLoaded() ? ResolutionControlHelper.getCurrentScaleFactor() : 1.0F;

            this.renderScale = (float) Math.sqrt(dataholder.vrSettings.renderScaleFactor) * resolutionScale;
            int eyeFBWidth = (int) Math.ceil(eyew * this.renderScale);
            int eyeFBHeight = (int) Math.ceil(eyeh * this.renderScale);

            this.framebufferVrRender = VRTextureTarget.builder("3D Render")
                .withSize(eyeFBWidth, eyeFBHeight)
                .withDepth()
                .withStencil(dataholder.vrSettings.vrUseStencil && StencilHelper.stencilBufferSupported())
                .build();
            WorldRenderPass.STEREO_XR = new WorldRenderPass(this.framebufferVrRender);
            VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferVrRender);
            GraphicsHelper.INSTANCE.checkError("3D framebuffer setup");

            getMirrorTextureSize(eyeFBWidth, eyeFBHeight, resolutionScale);

            List<RenderPass> list = this.getRenderPasses(true);

            VRSettings.LOGGER.info("Vivecraft: Active RenderPasses: {}",
                list.stream().map(Enum::toString).collect(Collectors.joining(", ")));

            // make sure these are valid, even when not needed
            if (list.contains(RenderPass.THIRD) || this.allFramebuffersInitialized()) {
                this.framebufferMR = VRTextureTarget.builder("Mixed Reality Render")
                    .withSize(Math.max(1, this.mirrorFBWidth), Math.max(1, this.mirrorFBHeight))
                    .withDepth()
                    .build();
                WorldRenderPass.MIXED_REALITY = new WorldRenderPass(this.framebufferMR);
                VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferMR);
                GraphicsHelper.INSTANCE.checkError("Mixed reality framebuffer setup");
            }

            if (list.contains(RenderPass.CENTER) || this.allFramebuffersInitialized()) {
                this.framebufferUndistorted = VRTextureTarget.builder("Undistorted View Render")
                    .withSize(Math.max(1, this.mirrorFBWidth), Math.max(1, this.mirrorFBHeight))
                    .withDepth()
                    .build();
                WorldRenderPass.CENTER = new WorldRenderPass(this.framebufferUndistorted);
                VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferUndistorted);
                GraphicsHelper.INSTANCE.checkError("Undistorted view framebuffer setup");
            }
            this.mirrorFramebuffer = VRTextureTarget.builder("Mirror")
                .withSize(
                    Math.max(1, ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth()),
                    Math.max(1, ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight()))
                .withClearColor(0F, 0F, 0F, 1F)
                .build();

            GuiHandler.updateResolution();
            GuiHandler.GUI_FRAMEBUFFER = VRTextureTarget.builder("GUI")
                .withSize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT)
                .withDepth()
                .withMipmaps(dataholder.vrSettings.guiMipmaps)
                .build();
            VRSettings.LOGGER.info("Vivecraft: {}", GuiHandler.GUI_FRAMEBUFFER);
            GraphicsHelper.INSTANCE.checkError("GUI framebuffer setup");

            KeyboardHandler.FRAMEBUFFER = VRTextureTarget.builder("Keyboard")
                .withSize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT)
                .withDepth()
                .withMipmaps(dataholder.vrSettings.guiMipmaps)
                .build();
            VRSettings.LOGGER.info("Vivecraft: {}", KeyboardHandler.FRAMEBUFFER);
            GraphicsHelper.INSTANCE.checkError("Keyboard framebuffer setup");

            RadialHandler.FRAMEBUFFER = VRTextureTarget.builder("Radial Menu")
                .withSize(GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT)
                .withDepth()
                .withMipmaps(dataholder.vrSettings.guiMipmaps)
                .build();
            VRSettings.LOGGER.info("Vivecraft: {}", RadialHandler.FRAMEBUFFER);
            GraphicsHelper.INSTANCE.checkError("Radial framebuffer setup");


            Vector2i telescopeSize = getTelescopeTextureSize(eyeFBWidth, eyeFBHeight);

            this.telescopeFramebufferR = VRTextureTarget.builder("TelescopeR")
                .withSize(telescopeSize.x, telescopeSize.y)
                .withDepth()
                .withClearColor(0F, 0F, 0F, 1F)
                .build();
            WorldRenderPass.RIGHT_TELESCOPE = new WorldRenderPass(this.telescopeFramebufferR);
            VRSettings.LOGGER.info("Vivecraft: {}", this.telescopeFramebufferR);
            GraphicsHelper.INSTANCE.checkError("TelescopeR framebuffer setup");

            this.telescopeFramebufferL = VRTextureTarget.builder("TelescopeL")
                .withSize(telescopeSize.x, telescopeSize.y)
                .withDepth()
                .withClearColor(0F, 0F, 0F, 1F)
                .build();
            WorldRenderPass.LEFT_TELESCOPE = new WorldRenderPass(this.telescopeFramebufferL);
            VRSettings.LOGGER.info("Vivecraft: {}", this.telescopeFramebufferL);
            GraphicsHelper.INSTANCE.checkError("TelescopeL framebuffer setup");

            Vector2i cameraSize = getCameraTextureSize(eyeFBWidth, eyeFBHeight);
            int cameraRenderFBwidth = cameraSize.x;
            int cameraRenderFBheight = cameraSize.y;

            if (ShadersHelper.needsSameSizeBuffers()) {
                cameraRenderFBwidth = eyeFBWidth;
                cameraRenderFBheight = eyeFBHeight;
            }

            this.cameraFramebuffer = VRTextureTarget.builder("Handheld Camera")
                .withSize(cameraSize.x, cameraSize.y)
                .withDepth()
                .withClearColor(0F, 0F, 0F, 1F)
                .build();
            VRSettings.LOGGER.info("Vivecraft: {}", this.cameraFramebuffer);
            GraphicsHelper.INSTANCE.checkError("Camera framebuffer setup");

            this.cameraRenderFramebuffer = VRTextureTarget.builder("Handheld Camera Render")
                .withSize(cameraRenderFBwidth, cameraRenderFBheight)
                .withDepth()
                .build();
            WorldRenderPass.CAMERA = new WorldRenderPass(this.cameraRenderFramebuffer);
            VRSettings.LOGGER.info("Vivecraft: {}", this.cameraRenderFramebuffer);
            GraphicsHelper.INSTANCE.checkError("Camera render framebuffer setup");

            if (dataholder.vrSettings.useFsaa) {
                try {
                    GraphicsHelper.INSTANCE.checkError("pre FSAA FBO creation");
                    this.fsaaFirstPassResultFBO = VRTextureTarget.builder("FSAA Pass1 FBO")
                        .withSize(eyew, eyeFBHeight)
                        .withDepth()
                        .build();
                    this.fsaaLastPassResultFBO = VRTextureTarget.builder("FSAA Pass2 FBO")
                        .withSize(eyew, eyeh)
                        .withDepth()
                        .build();

                    VRSettings.LOGGER.info("Vivecraft: {}", this.fsaaFirstPassResultFBO);
                    VRSettings.LOGGER.info("Vivecraft: {}", this.fsaaLastPassResultFBO);
                    GraphicsHelper.INSTANCE.checkError("FSAA FBO creation");
                } catch (Exception exception) {
                    // FSAA failed to initialize so don't use it
                    dataholder.vrSettings.useFsaa = false;
                    dataholder.vrSettings.saveOptions();
                    VRSettings.LOGGER.error("Vivecraft: FSAA init failed: ", exception);
                    // redo the setup next frame
                    this.reinitFrameBuffers = true;
                    return;
                }
            }

            RenderPassManager.setGUIRenderPass();
            // update post effect chain
            minecraft.gameRenderer.checkEntityPostEffect(minecraft.getCameraEntity());

            if (minecraft.gui.screen() != null) {
                int w = minecraft.getWindow().getGuiScaledWidth();
                int h = minecraft.getWindow().getGuiScaledHeight();
                minecraft.gui.screen().init(w, h);
            }

            long windowPixels =
                (long) ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth() *
                    ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight();
            long mirrorPixels = (long) this.mirrorFBWidth * (long) this.mirrorFBHeight;

            long vrPixels = (long) eyeFBWidth * (long) eyeFBHeight;
            long pixelsPerFrame = vrPixels * 2L;

            if (list.contains(RenderPass.CENTER)) {
                pixelsPerFrame += mirrorPixels;
            }

            if (list.contains(RenderPass.THIRD)) {
                pixelsPerFrame += mirrorPixels;
            }

            VRSettings.LOGGER.info("""
                    Vivecraft:
                    New VR render config:
                    VR target: {}x{} [{}MP]
                    Render target: {}x{} [Render scale: {}%, {}MP]
                    Main window: {}x{} [{}MP]
                    Total shaded pixels per frame: {}MP (eye stencil not accounted for)""",
                eyew, eyeh, String.format("%.1f", (eyew * eyeh) / 1000000.0F),
                eyeFBWidth, eyeFBHeight, dataholder.vrSettings.renderScaleFactor * 100.0F,
                String.format("%.1f", vrPixels / 1000000.0F),
                ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenWidth(),
                ((WindowExtension) (Object) minecraft.getWindow()).vivecraft$getActualScreenHeight(),
                String.format("%.1f", windowPixels / 1000000.0F),
                String.format("%.1f", pixelsPerFrame / 1000000.0F));

            // updates the size of the outline target
            minecraft.levelRenderer.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());

            if (ClientDataHolderVR.getInstance().vrSettings.fullReloadOnInit) {
                // do a full reload
                minecraft.reloadResourcePacks();
            }

            ShadersHelper.maybeReloadShaders();

            this.reinitFrameBuffers = false;
            this.resizeFrameBuffers = false;
        }
    }

    private void checkIfSupportedGpu() throws RenderConfigException {
        // intel drivers have issues with interop on windows so throw an error
        if (Util.getPlatform() == Util.OS.WINDOWS &&
            RenderSystem.getDevice().getDeviceInfo().name().toLowerCase().contains("intel") &&
            ClientDataHolderVR.getInstance().vrSettings.blockIntelWindows)
        {
            StringBuilder gpus = new StringBuilder();
            boolean onlyIntel = true;
            for (GraphicsCard gpu : (new SystemInfo()).getHardware().getGraphicsCards()) {
                gpus.append("\n");
                if (gpu.getVendor().toLowerCase().contains("intel") ||
                    gpu.getName().toLowerCase().contains("intel"))
                {
                    gpus.append("§c❌§r ");
                } else if (gpu.getVendor().toLowerCase().contains("amd") ||
                    gpu.getName().toLowerCase().contains("amd") ||
                    gpu.getVendor().toLowerCase().contains("nvidia") ||
                    gpu.getName().toLowerCase().contains("nvidia"))
                {
                    onlyIntel = false;
                    gpus.append("§a✔§r ");
                }
                gpus.append(gpu.getVendor()).append(": ").append(gpu.getName());
            }
            Component message;
            message = Component.translatable("vivecraft.messages.intelgraphics1",
                Component.literal(RenderSystem.getDevice().getDeviceInfo().name())
                    .withStyle(ChatFormatting.GOLD),
                gpus.toString(),
                onlyIntel ? Component.empty() :
                    Component.translatable("vivecraft.messages.intelgraphics2",
                        Component.literal("https://www.vivecraft.org/faq/#gpu")
                            .withStyle(style -> style.withUnderlined(true)
                                .withColor(ChatFormatting.GREEN)
                                .withHoverEvent(new HoverEvent.ShowText(CommonComponents.GUI_OPEN_IN_BROWSER))
                                .withClickEvent(new ClickEvent.OpenUrl(
                                    ClientUtils.parseUri("https://www.vivecraft.org/faq/#gpu"))))));

            throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblegpu"), message);
        }
    }

    /**
     * only destroys the render buffers, everything else stays in takt
     */
    protected void destroyBuffers() {
        if (this.framebufferVrRender != null) {
            WorldRenderPass.STEREO_XR.close();
            WorldRenderPass.STEREO_XR = null;
            this.framebufferVrRender.destroyBuffers();
            this.framebufferVrRender = null;
        }

        if (this.framebufferMR != null) {
            WorldRenderPass.MIXED_REALITY.close();
            WorldRenderPass.MIXED_REALITY = null;
            this.framebufferMR.destroyBuffers();
            this.framebufferMR = null;
        }

        if (this.framebufferUndistorted != null) {
            WorldRenderPass.CENTER.close();
            WorldRenderPass.CENTER = null;
            this.framebufferUndistorted.destroyBuffers();
            this.framebufferUndistorted = null;
        }

        if (GuiHandler.GUI_FRAMEBUFFER != null) {
            GuiHandler.GUI_FRAMEBUFFER.destroyBuffers();
            GuiHandler.GUI_FRAMEBUFFER = null;
        }

        if (KeyboardHandler.FRAMEBUFFER != null) {
            KeyboardHandler.FRAMEBUFFER.destroyBuffers();
            KeyboardHandler.FRAMEBUFFER = null;
        }

        if (RadialHandler.FRAMEBUFFER != null) {
            RadialHandler.FRAMEBUFFER.destroyBuffers();
            RadialHandler.FRAMEBUFFER = null;
        }

        if (this.telescopeFramebufferL != null) {
            WorldRenderPass.LEFT_TELESCOPE.close();
            WorldRenderPass.LEFT_TELESCOPE = null;
            this.telescopeFramebufferL.destroyBuffers();
            this.telescopeFramebufferL = null;
        }

        if (this.telescopeFramebufferR != null) {
            WorldRenderPass.RIGHT_TELESCOPE.close();
            WorldRenderPass.RIGHT_TELESCOPE = null;
            this.telescopeFramebufferR.destroyBuffers();
            this.telescopeFramebufferR = null;
        }

        if (this.cameraFramebuffer != null) {
            this.cameraFramebuffer.destroyBuffers();
            this.cameraFramebuffer = null;
        }

        if (this.cameraRenderFramebuffer != null) {
            WorldRenderPass.CAMERA.close();
            WorldRenderPass.CAMERA = null;
            this.cameraRenderFramebuffer.destroyBuffers();
            this.cameraRenderFramebuffer = null;
        }

        if (this.fsaaFirstPassResultFBO != null) {
            this.fsaaFirstPassResultFBO.destroyBuffers();
            this.fsaaFirstPassResultFBO = null;
        }

        if (this.fsaaLastPassResultFBO != null) {
            this.fsaaLastPassResultFBO.destroyBuffers();
            this.fsaaLastPassResultFBO = null;
        }

        for (int i = 0; i < 2; i++) {
            if (this.framebufferEye[i] != null) {
                this.framebufferEye[i].destroyBuffers();
                this.framebufferEye[i] = null;
            }
        }

        if (this.mirrorFramebuffer != null) {
            this.mirrorFramebuffer.destroyBuffers();
            this.mirrorFramebuffer = null;
        }
    }

    /**
     * destroys everything the Renderer has allocated
     */
    public void destroy() {
        destroyBuffers();
        this.stencilProjectionMatrix.close();
        if (this.bakedHiddenMesh[0] != null) {
            this.bakedHiddenMesh[0].close();
            this.bakedHiddenMesh[0] = null;
        }
        if (this.bakedHiddenMesh[1] != null) {
            this.bakedHiddenMesh[1].close();
            this.bakedHiddenMesh[1] = null;
        }
    }
}
