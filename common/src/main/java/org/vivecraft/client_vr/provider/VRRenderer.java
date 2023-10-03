package org.vivecraft.client_vr.provider;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Matrix4f;
import org.joml.RoundingMode;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.ShaderHelper;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.resolutioncontrol.ResolutionControlHelper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.Minecraft.ON_OSX;
import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.logger;

public abstract class VRRenderer {
    public static final String RENDER_SETUP_FAILURE_MESSAGE = "Failed to initialise stereo rendering plugin: ";
    public RenderTarget cameraFramebuffer;
    public RenderTarget cameraRenderFramebuffer;
    protected int dispLastWidth;
    protected int dispLastHeight;
    public Matrix4f[] eyeproj = new Matrix4f[]{new Matrix4f(), new Matrix4f()};
    public RenderTarget framebufferEye0;
    public RenderTarget framebufferEye1;
    public RenderTarget framebufferMR;
    public RenderTarget framebufferUndistorted;
    public RenderTarget framebufferVrRender;
    public RenderTarget fsaaFirstPassResultFBO;
    public RenderTarget fsaaLastPassResultFBO;
    protected float[][] hiddenMesheVertecies = new float[2][];
    public ResourceKey<DimensionType> lastDimensionId = BuiltinDimensionTypes.OVERWORLD;
    public int lastDisplayFBHeight = 0;
    public int lastDisplayFBWidth = 0;
    public boolean lastEnableVsync = true;
    public boolean lastFogFancy = true;
    public boolean lastFogFast = false;
    private GraphicsStatus previousGraphics;
    public int lastGuiScale = 0;
    protected MirrorMode lastMirror;
    public int lastRenderDistanceChunks = -1;
    public long lastWindow = 0L;
    public float lastWorldScale = 0.0F;
    protected int LeftEyeTextureId = -1;
    protected int RightEyeTextureId = -1;
    public int mirrorFBHeight;
    public int mirrorFBWidth;
    protected boolean reinitFramebuffers = true;
    protected boolean resizeFrameBuffers = false;
    protected boolean acceptReinits = true;
    public boolean reinitShadersFlag = false;
    public float renderScale;
    protected Tuple<Integer, Integer> resolution;
    public float ss = -1.0F;
    public RenderTarget telescopeFramebufferL;
    public RenderTarget telescopeFramebufferR;
    protected MCVR vr;

    public VRRenderer(MCVR vr) {
        this.vr = vr;
    }

    protected void checkGLError(String message) {
        //Config.checkGlError(message); TODO
        if (GlStateManager._getError() != 0) {
            logger.error(message);
        }
    }

    public abstract void createRenderTexture(int var1, int var2);

    public abstract Matrix4f getProjectionMatrix(int eyeType, double nearClip, double farClip, Matrix4f dest);

    public abstract void endFrame() throws RenderConfigException;

    public boolean providesStencilMask() {
        return false;
    }

    public void deleteRenderTextures() {
        if (this.LeftEyeTextureId > 0) {
            RenderSystem.deleteTexture(this.LeftEyeTextureId);
        }

        if (this.RightEyeTextureId > 0) {
            RenderSystem.deleteTexture(this.RightEyeTextureId);
        }

        this.LeftEyeTextureId = this.RightEyeTextureId = -1;
    }

    public void doStencil(boolean inverse) {

        //setup stencil for writing
        GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_REPLACE);
        RenderSystem.stencilMask(0xFF); // Write to stencil buffer

        if (inverse) {
            //clear whole image for total mask in color, stencil, depth
            RenderSystem.clearStencil(0xFF);
            RenderSystem.clearDepth(0);

            RenderSystem.stencilFunc(GL11C.GL_ALWAYS, 0, 0xFF); // Set any stencil to 0
            RenderSystem.colorMask(false, false, false, true);
        } else {
            //clear whole image for total transparency
            RenderSystem.clearStencil(0);
            RenderSystem.clearDepth(1);

            RenderSystem.stencilFunc(GL11C.GL_ALWAYS, 0xFF, 0xFF); // Set any stencil to 1
            RenderSystem.colorMask(true, true, true, true);
        }

        RenderSystem.clear(GL11C.GL_DEPTH_BUFFER_BIT | GL11C.GL_STENCIL_BUFFER_BIT, false);

        RenderSystem.clearStencil(0);
        RenderSystem.clearDepth(1);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.disableCull();

        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);


        RenderTarget fb = mc.getMainRenderTarget();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, fb.viewWidth, 0.0F, fb.viewHeight, 0.0F, 20.0F), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        if (inverse) //draw on far clip
        {
            RenderSystem.getModelViewStack().last().pose().translate(0.0F, 0.0F, -20.0F);
        }
        RenderSystem.applyModelViewMatrix();
        int s = GlStateManager._getInteger(GL20C.GL_CURRENT_PROGRAM);

        if (dh.currentPass == RenderPass.SCOPEL || dh.currentPass == RenderPass.SCOPER) {
            drawCircle(fb.viewWidth, fb.viewHeight);
        } else if (dh.currentPass == RenderPass.LEFT || dh.currentPass == RenderPass.RIGHT) {
            drawMask();
        }

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.getModelViewStack().popPose();

        RenderSystem.depthMask(true); // Do write to depth buffer
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        ProgramManager.glUseProgram(s);
        RenderSystem.stencilFunc(GL11C.GL_NOTEQUAL, 255, 1);
        RenderSystem.stencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
        RenderSystem.stencilMask(0); // Dont Write to stencil buffer
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
    FloatBuffer buffer2 = MemoryUtil.memAllocFloat(16);

    public void doFSAA(boolean hasShaders) {
        if (this.fsaaFirstPassResultFBO == null) {
            this.reinitFrameBuffers("FSAA Setting Changed");
        } else {
            RenderSystem.disableBlend();
            // set to always, so that we can skip the clear
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);

            // first pass
            this.fsaaFirstPassResultFBO.bindWrite(true);

            RenderSystem.setShaderTexture(0, framebufferVrRender.getColorTextureId());
            RenderSystem.setShaderTexture(1, framebufferVrRender.getDepthTextureId());

            RenderSystem.activeTexture(GL13C.GL_TEXTURE1);
            this.framebufferVrRender.bindRead();
            RenderSystem.activeTexture(GL13C.GL_TEXTURE2);
            RenderSystem.bindTexture(framebufferVrRender.getDepthTextureId());
            RenderSystem.activeTexture(GL13C.GL_TEXTURE0);

            VRShaders.lanczosShader.setSampler("Sampler0", RenderSystem.getShaderTexture(0));
            VRShaders.lanczosShader.setSampler("Sampler1", RenderSystem.getShaderTexture(1));
            VRShaders._Lanczos_texelWidthOffsetUniform.set(1.0F / (3.0F * (float) this.fsaaFirstPassResultFBO.viewWidth));
            VRShaders._Lanczos_texelHeightOffsetUniform.set(0.0F);
            VRShaders.lanczosShader.apply();

            this.drawQuad();

            // second pass
            this.fsaaLastPassResultFBO.bindWrite(true);
            RenderSystem.setShaderTexture(0, this.fsaaFirstPassResultFBO.getColorTextureId());
            RenderSystem.setShaderTexture(1, this.fsaaFirstPassResultFBO.getDepthTextureId());

            RenderSystem.activeTexture(GL13C.GL_TEXTURE1);
            this.fsaaFirstPassResultFBO.bindRead();
            RenderSystem.activeTexture(GL13C.GL_TEXTURE2);
            RenderSystem.bindTexture(fsaaFirstPassResultFBO.getDepthTextureId());
            RenderSystem.activeTexture(GL13C.GL_TEXTURE0);

            VRShaders.lanczosShader.setSampler("Sampler0", RenderSystem.getShaderTexture(0));
            VRShaders.lanczosShader.setSampler("Sampler1", RenderSystem.getShaderTexture(1));
            VRShaders._Lanczos_texelWidthOffsetUniform.set(0.0F);
            VRShaders._Lanczos_texelHeightOffsetUniform.set(1.0F / (3.0F * (float) this.fsaaLastPassResultFBO.viewHeight));
            VRShaders.lanczosShader.apply();

            this.drawQuad();

            // Clean up time
            VRShaders.lanczosShader.clear();
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }
    }

    private void drawCircle(float width, float height) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        int i = 32;
        float f = width / 2;
        builder.vertex(f, f, 0.0D).endVertex();
        for (int j = 0; j < i + 1; ++j) {
            double f1 = (double) j / i * PI * 2.0D;
            double f2 = f + cos(f1) * f;
            double f3 = f + sin(f1) * f;
            builder.vertex(f2, f3, 0.0D).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
    }

    private void drawMask() {
        float[] verts = getStencilMask(dh.currentPass);
        if (verts == null) {
            return;
        }

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/black.png"));

        for (int i = 0; i < verts.length; i += 2) {
            builder.vertex(verts[i] * dh.vrRenderer.renderScale, verts[i + 1] * dh.vrRenderer.renderScale, 0.0F).endVertex();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferUploader.drawWithShader(builder.end());
    }

    private void drawQuad() {
        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(-1.0F, -1.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
        builder.vertex(1.0F, -1.0F, 0.0F).uv(1.0F, 0.0F).endVertex();
        builder.vertex(1.0F, 1.0F, 0.0F).uv(1.0F, 1.0F).endVertex();
        builder.vertex(-1.0F, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
        BufferUploader.draw(builder.end());
    }

    public double getCurrentTimeSecs() {
        return System.nanoTime() / 1.0E9D;
    }

    public double getFrameTiming() {
        return this.getCurrentTimeSecs();
    }

    public String getinitError() {
        return this.vr.initStatus;
    }

    public String getLastError() {
        return "";
    }

    public String getName() {
        return "Default Renderer Name";
    }

    public List<RenderPass> getRenderPasses() {
        List<RenderPass> list = new ArrayList<>();
        list.add(RenderPass.LEFT);
        list.add(RenderPass.RIGHT);

        // only do these, if the window is not minimized
        if (mc.getWindow().getScreenWidth() > 0 && mc.getWindow().getScreenHeight() > 0) {
            if (dh.vrSettings.displayMirrorMode == MirrorMode.FIRST_PERSON) {
                list.add(RenderPass.CENTER);
            } else if (dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY) {
                if (dh.vrSettings.mixedRealityUndistorted && dh.vrSettings.mixedRealityUnityLike) {
                    list.add(RenderPass.CENTER);
                }

                list.add(RenderPass.THIRD);
            } else if (dh.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON) {
                list.add(RenderPass.THIRD);
            }
        }

        if (mc.player != null) {
            if (TelescopeTracker.isTelescope(mc.player.getMainHandItem()) && TelescopeTracker.isViewing(0)) {
                list.add(RenderPass.SCOPER);
            }

            if (TelescopeTracker.isTelescope(mc.player.getOffhandItem()) && TelescopeTracker.isViewing(1)) {
                list.add(RenderPass.SCOPEL);
            }

            if (dh.cameraTracker.isVisible()) {
                list.add(RenderPass.CAMERA);
            }
        }

        return list;
    }

    public abstract Tuple<Integer, Integer> getRenderTextureSizes();

    public Tuple<Integer, Integer> getMirrorTextureSize(int eyeFBWidth, int eyeFBHeight, float resolutionScale) {
        mirrorFBWidth = roundUsing(mc.getWindow().getScreenWidth() * resolutionScale, RoundingMode.CEILING);
        mirrorFBHeight = roundUsing(mc.getWindow().getScreenHeight() * resolutionScale, RoundingMode.CEILING);

        if (dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY) {
            mirrorFBWidth = mirrorFBWidth / 2;

            if (dh.vrSettings.mixedRealityUnityLike) {
                mirrorFBHeight = mirrorFBHeight / 2;
            }
        }

        if (ShadersHelper.needsSameSizeBuffers()) {
            mirrorFBWidth = eyeFBWidth;
            mirrorFBHeight = eyeFBHeight;
        }
        return new Tuple<>(mirrorFBWidth, mirrorFBHeight);
    }

    public Tuple<Integer, Integer> getTelescopeTextureSize(int eyeFBWidth, int eyeFBHeight) {
        int telescopeFBwidth = 720;
        int telescopeFBheight = 720;

        if (ShadersHelper.needsSameSizeBuffers()) {
            telescopeFBwidth = eyeFBWidth;
            telescopeFBheight = eyeFBHeight;
        }
        return new Tuple<>(telescopeFBwidth, telescopeFBheight);
    }

    public Tuple<Integer, Integer> getCameraTextureSize(int eyeFBWidth, int eyeFBHeight) {
        int cameraFBwidth = round(1920.0F * dh.vrSettings.handCameraResScale);
        int cameraFBheight = round(1080.0F * dh.vrSettings.handCameraResScale);

        if (ShadersHelper.needsSameSizeBuffers()) {
            // correct for camera aspect, since that is 16:9
            float aspect = (float) cameraFBwidth / (float) cameraFBheight;
            if (aspect > (float) (eyeFBWidth / eyeFBHeight)) {
                cameraFBwidth = eyeFBWidth;
                cameraFBheight = round((float) eyeFBWidth / aspect);
            } else {
                cameraFBwidth = round((float) eyeFBHeight * aspect);
                cameraFBheight = eyeFBHeight;
            }
        }
        return new Tuple<>(cameraFBwidth, cameraFBheight);
    }

    public float[] getStencilMask(RenderPass eye) {
        if (this.hiddenMesheVertecies != null && (eye == RenderPass.LEFT || eye == RenderPass.RIGHT)) {
            return eye == RenderPass.LEFT ? this.hiddenMesheVertecies[0] : this.hiddenMesheVertecies[1];
        } else {
            return null;
        }
    }

    public boolean isInitialized() {
        return this.vr.initSuccess;
    }

    public void reinitFrameBuffers(String cause) {
        if (acceptReinits) {
            if (!reinitFramebuffers) {
                // only print the first cause
                logger.info("Reinit Render: {}", cause);
            }
            this.reinitFramebuffers = true;
        }
    }

    public void resizeFrameBuffers(String cause) {
        if (!cause.isEmpty() && !this.resizeFrameBuffers) {
            logger.info("Resizing Buffers: " + cause);
        }
        this.resizeFrameBuffers = true;
    }

    public void setupRenderConfiguration() throws Exception {

        if (mc.getWindow().getWindow() != this.lastWindow) {
            this.lastWindow = mc.getWindow().getWindow();
            this.reinitFrameBuffers("Window Handle Changed");
        }

        if (this.lastEnableVsync != mc.options.enableVsync().get()) {
            this.reinitFrameBuffers("VSync Changed");
            this.lastEnableVsync = mc.options.enableVsync().get();
        }

        if (this.lastMirror != dh.vrSettings.displayMirrorMode) {
            if (!ShadersHelper.isShaderActive()) {
                // don't reinit with shaders, not needed
                this.reinitFrameBuffers("Mirror Changed");
            } else {
                // mixed reality is half size, so a resize is needed
                if (lastMirror == MirrorMode.MIXED_REALITY
                    || dh.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY) {
                    this.resizeFrameBuffers("Mirror Changed");
                }
            }
            this.lastMirror = dh.vrSettings.displayMirrorMode;
        }

        if ((framebufferMR == null || framebufferUndistorted == null) && ShadersHelper.isShaderActive()) {
            this.reinitFrameBuffers("Shaders on, but some buffers not initialized");
        }
        if (mc.options.graphicsMode().get() != previousGraphics) {
            previousGraphics = mc.options.graphicsMode().get();
            dh.vrRenderer.reinitFrameBuffers("gfx setting change");
        }

        if (this.resizeFrameBuffers && !this.reinitFramebuffers) {
            resizeFrameBuffers = false;
            Tuple<Integer, Integer> tuple = this.getRenderTextureSizes();
            int eyew = tuple.getA();
            int eyeh = tuple.getB();

            float resolutionScale = ResolutionControlHelper.isLoaded() ? ResolutionControlHelper.getCurrentScaleFactor() : 1.0F;

            this.renderScale = sqrt(dh.vrSettings.renderScaleFactor) * resolutionScale;
            int eyeFBWidth = roundUsing(eyew * this.renderScale, RoundingMode.CEILING);
            int eyeFBHeight = roundUsing(eyeh * this.renderScale, RoundingMode.CEILING);

            Tuple<Integer, Integer> mirrorSize = getMirrorTextureSize(eyeFBWidth, eyeFBHeight, resolutionScale);
            Tuple<Integer, Integer> telescopeSize = getTelescopeTextureSize(eyeFBWidth, eyeFBHeight);
            Tuple<Integer, Integer> cameraSize = getCameraTextureSize(eyeFBWidth, eyeFBHeight);

            // main render target
            ((RenderTargetExtension) WorldRenderPass.stereoXR.target).vivecraft$setUseStencil(dh.vrSettings.vrUseStencil);
            WorldRenderPass.stereoXR.resize(eyeFBWidth, eyeFBHeight);
            if (dh.vrSettings.useFsaa) {
                this.fsaaFirstPassResultFBO.resize(eyew, eyeFBHeight, ON_OSX);
            }

            // mirror
            if (WorldRenderPass.center != null) {
                WorldRenderPass.center.resize(mirrorSize.getA(), mirrorSize.getB());
            }
            if (WorldRenderPass.mixedReality != null) {
                WorldRenderPass.mixedReality.resize(mirrorSize.getA(), mirrorSize.getB());
            }

            // telescopes
            WorldRenderPass.leftTelescope.resize(telescopeSize.getA(), telescopeSize.getB());
            WorldRenderPass.rightTelescope.resize(telescopeSize.getA(), telescopeSize.getB());

            // camera
            cameraFramebuffer.resize(cameraSize.getA(), cameraSize.getB(), ON_OSX);
            if (ShadersHelper.needsSameSizeBuffers()) {
                WorldRenderPass.camera.resize(eyeFBWidth, eyeFBHeight);
            } else {
                WorldRenderPass.camera.resize(cameraSize.getA(), cameraSize.getB());
            }
        }

        if (this.reinitFramebuffers) {
            this.reinitShadersFlag = true;
            this.checkGLError("Start Init");

            if (GlUtil.getRenderer().toLowerCase().contains("intel")) //Optifine
            {
                throw new RenderConfigException("Incompatible", Component.translatable("vivecraft.messages.intelgraphics", GlUtil.getRenderer()));
            }

            if (!this.isInitialized()) {
                throw new RenderConfigException("Failed to initialise stereo rendering plugin: " + this.getName(), Component.literal(this.getinitError()));
            }

            Tuple<Integer, Integer> tuple = this.getRenderTextureSizes();
            int eyew = tuple.getA();
            int eyeh = tuple.getB();

            destroy();

            if (this.LeftEyeTextureId == -1) {
                this.createRenderTexture(eyew, eyeh);

                if (this.LeftEyeTextureId == -1) {
                    throw new RenderConfigException("Failed to initialise stereo rendering plugin: " + this.getName(), Component.literal(this.getLastError()));
                }

                logger.info("Provider supplied render texture IDs: {} {}", this.LeftEyeTextureId, this.RightEyeTextureId);
                logger.info("Provider supplied texture resolution: {} x {}", eyew, eyeh);
            }

            this.checkGLError("Render Texture setup");

            if (this.framebufferEye0 == null) {
                this.framebufferEye0 = new VRTextureTarget("L Eye", eyew, eyeh, false, false, this.LeftEyeTextureId, false, true, false);
                logger.info(this.framebufferEye0.toString());
                this.checkGLError("Left Eye framebuffer setup");
            }

            if (this.framebufferEye1 == null) {
                this.framebufferEye1 = new VRTextureTarget("R Eye", eyew, eyeh, false, false, this.RightEyeTextureId, false, true, false);
                logger.info(this.framebufferEye1.toString());
                this.checkGLError("Right Eye framebuffer setup");
            }

            float resolutionScale = ResolutionControlHelper.isLoaded() ? ResolutionControlHelper.getCurrentScaleFactor() : 1.0F;

            this.renderScale = (float) sqrt(dh.vrSettings.renderScaleFactor) * resolutionScale;
            int eyeFBWidth = roundUsing(eyew * this.renderScale, RoundingMode.CEILING);
            int eyeFBHeight = roundUsing(eyeh * this.renderScale, RoundingMode.CEILING);

            this.framebufferVrRender = new VRTextureTarget("3D Render", eyeFBWidth, eyeFBHeight, true, false, -1, true, true, dh.vrSettings.vrUseStencil);
            WorldRenderPass.stereoXR = new WorldRenderPass((VRTextureTarget) this.framebufferVrRender);
            logger.info(this.framebufferVrRender.toString());
            this.checkGLError("3D framebuffer setup");

            getMirrorTextureSize(eyeFBWidth, eyeFBHeight, resolutionScale);

            List<RenderPass> list = this.getRenderPasses();

            for (RenderPass renderpass : list) {
                logger.info("Passes: {}", renderpass.toString());
            }

            // only do these, if the window is not minimized
            if (mirrorFBWidth > 0 && mirrorFBHeight > 0) {
                if (list.contains(RenderPass.THIRD) || ShadersHelper.isShaderActive()) {
                    this.framebufferMR = new VRTextureTarget("Mixed Reality Render", this.mirrorFBWidth, this.mirrorFBHeight, true, false, -1, true, false, false);
                    WorldRenderPass.mixedReality = new WorldRenderPass((VRTextureTarget) this.framebufferMR);
                    logger.info(this.framebufferMR.toString());
                    this.checkGLError("Mixed reality framebuffer setup");
                }

                if (list.contains(RenderPass.CENTER) || ShadersHelper.isShaderActive()) {
                    this.framebufferUndistorted = new VRTextureTarget("Undistorted View Render", this.mirrorFBWidth, this.mirrorFBHeight, true, false, -1, false, false, false);
                    WorldRenderPass.center = new WorldRenderPass((VRTextureTarget) this.framebufferUndistorted);
                    logger.info(this.framebufferUndistorted.toString());
                    this.checkGLError("Undistorted view framebuffer setup");
                }
            }

            GuiHandler.guiFramebuffer = new VRTextureTarget("GUI", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            logger.info(GuiHandler.guiFramebuffer.toString());
            this.checkGLError("GUI framebuffer setup");
            KeyboardHandler.Framebuffer = new VRTextureTarget("Keyboard", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            logger.info(KeyboardHandler.Framebuffer.toString());
            this.checkGLError("Keyboard framebuffer setup");
            RadialHandler.Framebuffer = new VRTextureTarget("Radial Menu", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            logger.info(RadialHandler.Framebuffer.toString());
            this.checkGLError("Radial framebuffer setup");


            Tuple<Integer, Integer> telescopeSize = getTelescopeTextureSize(eyeFBWidth, eyeFBHeight);
            this.telescopeFramebufferR = new VRTextureTarget("TelescopeR", telescopeSize.getA(), telescopeSize.getB(), true, false, -1, true, false, false);
            WorldRenderPass.rightTelescope = new WorldRenderPass((VRTextureTarget) this.telescopeFramebufferR);
            logger.info(this.telescopeFramebufferR.toString());
            this.telescopeFramebufferR.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            this.telescopeFramebufferR.clear(ON_OSX);
            this.checkGLError("TelescopeR framebuffer setup");

            this.telescopeFramebufferL = new VRTextureTarget("TelescopeL", telescopeSize.getA(), telescopeSize.getB(), true, false, -1, true, false, false);
            WorldRenderPass.leftTelescope = new WorldRenderPass((VRTextureTarget) this.telescopeFramebufferL);
            logger.info(this.telescopeFramebufferL.toString());
            this.telescopeFramebufferL.setClearColor(0.0F, 0.0F, 0.0F, 1.0F);
            this.telescopeFramebufferL.clear(ON_OSX);
            this.checkGLError("TelescopeL framebuffer setup");


            Tuple<Integer, Integer> cameraSize = getCameraTextureSize(eyeFBWidth, eyeFBHeight);
            int cameraRenderFBwidth = cameraSize.getA();
            int cameraRenderFBheight = cameraSize.getB();

            if (ShadersHelper.needsSameSizeBuffers()) {
                cameraRenderFBwidth = eyeFBWidth;
                cameraRenderFBheight = eyeFBHeight;
            }

            this.cameraFramebuffer = new VRTextureTarget("Handheld Camera", cameraSize.getA(), cameraSize.getB(), true, false, -1, true, false, false);
            logger.info(this.cameraFramebuffer.toString());

            this.checkGLError("Camera framebuffer setup");
            this.cameraRenderFramebuffer = new VRTextureTarget("Handheld Camera Render", cameraRenderFBwidth, cameraRenderFBheight, true, false, -1, true, true, false);
            WorldRenderPass.camera = new WorldRenderPass((VRTextureTarget) this.cameraRenderFramebuffer);
            logger.info(this.cameraRenderFramebuffer.toString());

            this.checkGLError("Camera render framebuffer setup");
            GameRendererExtension GRE = (GameRendererExtension) mc.gameRenderer;
            GRE.vivecraft$setupClipPlanes();
            this.getProjectionMatrix(0, GRE.vivecraft$getMinClipDistance(), GRE.vivecraft$getClipDistance(), this.eyeproj[0]);
            this.getProjectionMatrix(1, GRE.vivecraft$getMinClipDistance(), GRE.vivecraft$getClipDistance(), this.eyeproj[1]);

            if (dh.vrSettings.useFsaa) {
                try {
                    this.checkGLError("pre FSAA FBO creation");
                    this.fsaaFirstPassResultFBO = new VRTextureTarget("FSAA Pass1 FBO", eyew, eyeFBHeight, true, false, -1, false, false, false);
                    this.fsaaLastPassResultFBO = new VRTextureTarget("FSAA Pass2 FBO", eyew, eyeh, true, false, -1, false, false, false);
                    logger.info(this.fsaaFirstPassResultFBO.toString());
                    logger.info(this.fsaaLastPassResultFBO.toString());
                    this.checkGLError("FSAA FBO creation");
                    VRShaders.setupFSAA();
                    ShaderHelper.checkGLError("FBO init fsaa shader");
                } catch (Exception exception) {
                    dh.vrSettings.useFsaa = false;
                    dh.vrSettings.saveOptions();
                    logger.error(exception.getMessage());
                    this.reinitFramebuffers = true;
                    return;
                }
            }

            try {
                mc.mainRenderTarget = this.framebufferVrRender;
                VRShaders.setupDepthMask();
                ShaderHelper.checkGLError("init depth shader");
                VRShaders.setupFOVReduction();
                ShaderHelper.checkGLError("init FOV shader");
                VRShaders.setupPortalShaders();
                ShaderHelper.checkGLError("init portal shader");
                mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            } catch (Exception exception1) {
                logger.error(exception1.getMessage());
                System.exit(-1);
            }

            if (mc.screen != null) {
                int l2 = mc.getWindow().getGuiScaledWidth();
                int j3 = mc.getWindow().getGuiScaledHeight();
                mc.screen.init(mc, l2, j3);
            }

            long windowPixels = (long) mc.getWindow().getScreenWidth() * mc.getWindow().getScreenHeight();
            long vrPixels = eyeFBWidth * eyeFBHeight * 2L;

            if (list.contains(RenderPass.CENTER)) {
                vrPixels += windowPixels;
            }

            if (list.contains(RenderPass.THIRD)) {
                vrPixels += windowPixels;
            }

            logger.info("New render config:" +
                "\nOpenVR target width: " + eyew + ", height: " + eyeh + " [" + String.format("%.1f", (float) (eyew * eyeh) / 1000000.0F) + " MP]" +
                "\nRender target width: " + eyeFBWidth + ", height: " + eyeFBHeight + " [Render scale: " + round(dh.vrSettings.renderScaleFactor * 100.0F) + "%, " + String.format("%.1f", (float) (eyeFBWidth * eyeFBHeight) / 1000000.0F) + " MP]" +
                "\nMain window width: " + mc.getWindow().getScreenWidth() + ", height: " + mc.getWindow().getScreenHeight() + " [" + String.format("%.1f", (float) windowPixels / 1000000.0F) + " MP]" +
                "\nTotal shaded pixels per frame: " + String.format("%.1f", (float) vrPixels / 1000000.0F) + " MP (eye stencil not accounted for)");
            this.lastDisplayFBWidth = eyeFBWidth;
            this.lastDisplayFBHeight = eyeFBHeight;
            this.reinitFramebuffers = false;

            acceptReinits = false;
            ShadersHelper.maybeReloadShaders();
            acceptReinits = true;
        }
    }

    public boolean wasDisplayResized() {
        int i = mc.getWindow().getScreenHeight();
        int j = mc.getWindow().getScreenWidth();
        boolean flag = this.dispLastHeight != i || this.dispLastWidth != j;
        this.dispLastHeight = i;
        this.dispLastWidth = j;
        return flag;
    }

    public void destroy() {
        if (this.framebufferVrRender != null) {
            WorldRenderPass.stereoXR.close();
            WorldRenderPass.stereoXR = null;
            this.framebufferVrRender.destroyBuffers();
            this.framebufferVrRender = null;
        }

        if (this.framebufferMR != null) {
            WorldRenderPass.mixedReality.close();
            WorldRenderPass.mixedReality = null;
            this.framebufferMR.destroyBuffers();
            this.framebufferMR = null;
        }

        if (this.framebufferUndistorted != null) {
            WorldRenderPass.center.close();
            WorldRenderPass.center = null;
            this.framebufferUndistorted.destroyBuffers();
            this.framebufferUndistorted = null;
        }

        if (GuiHandler.guiFramebuffer != null) {
            GuiHandler.guiFramebuffer.destroyBuffers();
            GuiHandler.guiFramebuffer = null;
        }

        if (KeyboardHandler.Framebuffer != null) {
            KeyboardHandler.Framebuffer.destroyBuffers();
            KeyboardHandler.Framebuffer = null;
        }

        if (RadialHandler.Framebuffer != null) {
            RadialHandler.Framebuffer.destroyBuffers();
            RadialHandler.Framebuffer = null;
        }

        if (this.telescopeFramebufferL != null) {
            WorldRenderPass.leftTelescope.close();
            WorldRenderPass.leftTelescope = null;
            this.telescopeFramebufferL.destroyBuffers();
            this.telescopeFramebufferL = null;
        }

        if (this.telescopeFramebufferR != null) {
            WorldRenderPass.rightTelescope.close();
            WorldRenderPass.rightTelescope = null;
            this.telescopeFramebufferR.destroyBuffers();
            this.telescopeFramebufferR = null;
        }

        if (this.cameraFramebuffer != null) {
            this.cameraFramebuffer.destroyBuffers();
            this.cameraFramebuffer = null;
        }

        if (this.cameraRenderFramebuffer != null) {
            WorldRenderPass.camera.close();
            WorldRenderPass.camera = null;
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

        if (this.framebufferEye0 != null) {
            this.framebufferEye0.destroyBuffers();
            this.framebufferEye0 = null;
        }

        if (this.framebufferEye1 != null) {
            this.framebufferEye1.destroyBuffers();
            this.framebufferEye1 = null;
        }
    }
}
