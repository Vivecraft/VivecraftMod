package org.vivecraft.client_vr.provider;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.ShaderHelper;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client.Xplat;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class VRRenderer {
    public static final String RENDER_SETUP_FAILURE_MESSAGE = "Failed to initialise stereo rendering plugin: ";
    public RenderTarget cameraFramebuffer;
    public RenderTarget cameraRenderFramebuffer;
    protected int dispLastWidth;
    protected int dispLastHeight;
    public Matrix4f[] eyeproj = new Matrix4f[2];
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
    public int lastGuiScale = 0;
    protected VRSettings.MirrorMode lastMirror;
    public int lastRenderDistanceChunks = -1;
    public long lastWindow = 0L;
    public float lastWorldScale = 0.0F;
    protected int LeftEyeTextureId = -1;
    protected int RightEyeTextureId = -1;
    public int mirrorFBHeight;
    public int mirrorFBWidth;
    protected boolean reinitFramebuffers = true;
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
            System.err.println(message);
        }
    }

    public abstract void createRenderTexture(int var1, int var2);

    public abstract Matrix4f getProjectionMatrix(int var1, float var2, float var3);

    public abstract void endFrame() throws RenderConfigException;

    public abstract boolean providesStencilMask();

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
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        //setup stencil for writing
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilMask(0xFF); // Write to stencil buffer

        if (inverse) {
            //clear whole image for total mask in color, stencil, depth
            RenderSystem.clearStencil(0xFF);
            RenderSystem.clearDepth(0);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF); // Set any stencil to 0
            RenderSystem.colorMask(false, false, false, true);

        } else {
            //clear whole image for total transparency
            RenderSystem.clearStencil(0);
            RenderSystem.clearDepth(1);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF); // Set any stencil to 1
            RenderSystem.colorMask(true, true, true, true);
        }

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT, false);

        RenderSystem.clearStencil(0);
        RenderSystem.clearDepth(1);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();

        RenderSystem.setShaderColor(0F, 0F, 0F, 1.0F);


        RenderTarget fb = minecraft.getMainRenderTarget();
        RenderSystem.viewport(0, 0, fb.viewWidth, fb.viewHeight);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, fb.viewWidth, 0.0F, fb.viewHeight, 0.0F, 20.0F), VertexSorting.ORTHOGRAPHIC_Z);
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        if (inverse) //draw on far clip
            RenderSystem.getModelViewStack().translate(0, 0, -20);
        RenderSystem.applyModelViewMatrix();
        int s = GlStateManager._getInteger(GL43.GL_CURRENT_PROGRAM);

        if (dataholder.currentPass == RenderPass.SCOPEL || dataholder.currentPass == RenderPass.SCOPER) {
            drawCircle(fb.viewWidth, fb.viewHeight);
        } else if (dataholder.currentPass == RenderPass.LEFT || dataholder.currentPass == RenderPass.RIGHT) {
            drawMask();
        }

        RenderSystem.restoreProjectionMatrix();
        RenderSystem.getModelViewStack().popPose();

        RenderSystem.depthMask(true); // Do write to depth buffer
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        GlStateManager._glUseProgram(s);
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 255, 1);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0); // Dont Write to stencil buffer
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
    }

    FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
    FloatBuffer buffer2 = MemoryUtil.memAllocFloat(16);

    public void doFSAA(RenderPass eye, boolean hasShaders) {
        if (this.fsaaFirstPassResultFBO == null) {
            this.reinitFrameBuffers("FSAA Setting Changed");
        } else {
            RenderSystem.disableBlend();
            RenderSystem.backupProjectionMatrix();
            Matrix4f matrix4f = new Matrix4f();
            matrix4f.identity();
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.getModelViewStack().pushPose();
            RenderSystem.getModelViewStack().translate(0, 0, -0.7F);
            RenderSystem.applyModelViewMatrix();
            this.fsaaFirstPassResultFBO.clear(Minecraft.ON_OSX);
            this.fsaaFirstPassResultFBO.bindWrite(false);
            RenderSystem.setShaderTexture(0, framebufferVrRender.getColorTextureId());
            RenderSystem.setShaderTexture(1, framebufferVrRender.getDepthTextureId());

            RenderSystem.activeTexture(33985);
            this.framebufferVrRender.bindRead();
            RenderSystem.activeTexture(33986);
            RenderSystem.bindTexture(((RenderTargetExtension) this.framebufferVrRender).getDepthBufferId());

            RenderSystem.activeTexture(33984);
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.clearDepth(1.0D);
            this.fsaaFirstPassResultFBO.bindRead();
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            RenderSystem.viewport(0, 0, this.fsaaFirstPassResultFBO.viewWidth, this.fsaaFirstPassResultFBO.viewHeight);
            VRShaders._Lanczos_texelWidthOffsetUniform.set(1.0F / (3.0F * (float) this.fsaaFirstPassResultFBO.viewWidth));
            VRShaders._Lanczos_texelHeightOffsetUniform.set(0.0F);
            VRShaders._Lanczos_modelViewUniform.set(RenderSystem.getModelViewMatrix());
            VRShaders._Lanczos_projectionUniform.set(RenderSystem.getProjectionMatrix());
            for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
                int l = RenderSystem.getShaderTexture(k);
                VRShaders.lanczosShader.setSampler("Sampler" + k, l);
            }
            VRShaders.lanczosShader.apply();
            RenderSystem.clear(16384, Minecraft.ON_OSX);
            this.drawQuad();
            this.fsaaLastPassResultFBO.clear(Minecraft.ON_OSX);
            this.fsaaLastPassResultFBO.bindWrite(false);
            RenderSystem.activeTexture(33985);
            this.fsaaFirstPassResultFBO.bindRead();
            RenderSystem.setShaderTexture(0, this.fsaaFirstPassResultFBO.getColorTextureId());
            RenderSystem.activeTexture(33986);
            RenderSystem.setShaderTexture(1, this.fsaaFirstPassResultFBO.getDepthTextureId());
            RenderSystem.bindTexture(((RenderTargetExtension) this.fsaaFirstPassResultFBO).getDepthBufferId());

            RenderSystem.activeTexture(33984);
            this.checkGLError("posttex");
            RenderSystem.viewport(0, 0, this.fsaaLastPassResultFBO.viewWidth, this.fsaaLastPassResultFBO.viewHeight);
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.clearDepth(1.0D);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            this.checkGLError("postclear");
            RenderSystem.activeTexture(33984);
            this.checkGLError("postact");
            for (int k = 0; k < RenderSystemAccessor.getShaderTextures().length; ++k) {
                int l = RenderSystem.getShaderTexture(k);
                VRShaders.lanczosShader.setSampler("Sampler" + k, l);
            }
            VRShaders._Lanczos_texelWidthOffsetUniform.set(0.0F);
            VRShaders._Lanczos_texelHeightOffsetUniform.set(1.0F / (3.0F * (float) this.framebufferEye0.viewHeight));
            VRShaders.lanczosShader.apply();
            this.drawQuad();
            this.checkGLError("postdraw");
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.getModelViewStack().popPose();
            // Clean up time
            VRShaders.lanczosShader.clear();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    private void drawCircle(float width, float height) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        int i = 32;
        float f = (float) (width / 2);
        builder.vertex((float) (width / 2), (float) (width / 2), 0.0F).endVertex();
        for (int j = 0; j < i + 1; ++j) {
            float f1 = (float) j / (float) i * (float) Math.PI * 2.0F;
            float f2 = (float) ((double) (width / 2) + Math.cos((double) f1) * (double) f);
            float f3 = (float) ((double) (width / 2) + Math.sin((double) f1) * (double) f);
            builder.vertex(f2, f3, 0.0F).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
    }

    private void drawMask() {
        Minecraft mc = Minecraft.getInstance();
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        float[] verts = getStencilMask(dh.currentPass);
        if (verts == null) return;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        for (int i = 0; i < verts.length; i += 2) {
            builder.vertex(verts[i] * dh.vrRenderer.renderScale, verts[i + 1] * dh.vrRenderer.renderScale, 0.0F).endVertex();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferUploader.drawWithShader(builder.end());
    }

    private void drawQuad() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(-1.0F, -1.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
        builder.vertex(1.0F, -1.0F, 0.0F).uv(1.0F, 0.0F).endVertex();
        builder.vertex(1.0F, 1.0F, 0.0F).uv(1.0F, 1.0F).endVertex();
        builder.vertex(-1.0F, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
        BufferUploader.draw(builder.end());
    }

    public double getCurrentTimeSecs() {
        return (double) System.nanoTime() / 1.0E9D;
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
        return "OpenVR";
    }

    public List<RenderPass> getRenderPasses() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        List<RenderPass> list = new ArrayList<>();
        list.add(RenderPass.LEFT);
        list.add(RenderPass.RIGHT);

        if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
            list.add(RenderPass.CENTER);
        } else if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
            if (dataholder.vrSettings.mixedRealityUndistorted && dataholder.vrSettings.mixedRealityUnityLike) {
                list.add(RenderPass.CENTER);
            }

            list.add(RenderPass.THIRD);
        } else if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
            list.add(RenderPass.THIRD);
        }

        if (minecraft.player != null) {
            if (TelescopeTracker.isTelescope(minecraft.player.getMainHandItem()) && TelescopeTracker.isViewing(0)) {
                list.add(RenderPass.SCOPER);
            }

            if (TelescopeTracker.isTelescope(minecraft.player.getOffhandItem()) && TelescopeTracker.isViewing(1)) {
                list.add(RenderPass.SCOPEL);
            }

            if (dataholder.cameraTracker.isVisible()) {
                list.add(RenderPass.CAMERA);
            }
        }

        return list;
    }

    public abstract Tuple<Integer, Integer> getRenderTextureSizes();

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
        if (!reinitFramebuffers) {
            // only print the first cause
            System.out.println("Reinit Render: " + cause);
        }
        this.reinitFramebuffers = true;
    }

    public void setupRenderConfiguration() throws Exception {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        if (minecraft.getWindow().getWindow() != this.lastWindow) {
            this.lastWindow = minecraft.getWindow().getWindow();
            this.reinitFrameBuffers("Window Handle Changed");
        }

        if (this.lastEnableVsync != minecraft.options.enableVsync().get()) {
            this.reinitFrameBuffers("VSync Changed");
            this.lastEnableVsync = minecraft.options.enableVsync().get();
        }

        if (this.lastMirror != dataholder.vrSettings.displayMirrorMode) {
            if (!((Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) && IrisHelper.isShaderActive())) {
                // don't reinit with shaders, not needed
                this.reinitFrameBuffers("Mirror Changed");
            }
            this.lastMirror = dataholder.vrSettings.displayMirrorMode;
        }

        if ((framebufferMR == null || framebufferUndistorted == null) && ((Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) && IrisHelper.isShaderActive())) {
            this.reinitFrameBuffers("Shaders on, but some buffers not initialized");
        }

        if (this.reinitFramebuffers) {
            this.reinitShadersFlag = true;
            this.checkGLError("Start Init");

            if (GlUtil.getRenderer().toLowerCase().contains("intel")) //Optifine
            {
                throw new RenderConfigException("Incompatible", LangHelper.get("vivecraft.messages.intelgraphics", GlUtil.getRenderer()));
            }

            if (!this.isInitialized()) {
                throw new RenderConfigException("Failed to initialise stereo rendering plugin: " + this.getName(), LangHelper.get(this.getinitError()));
            }

            Tuple<Integer, Integer> tuple = this.getRenderTextureSizes();
            int eyew = tuple.getA();
            int eyeh = tuple.getB();

            destroy();

            if (this.LeftEyeTextureId == -1) {
                this.createRenderTexture(eyew, eyeh);

                if (this.LeftEyeTextureId == -1) {
                    throw new RenderConfigException("Failed to initialise stereo rendering plugin: " + this.getName(), this.getLastError());
                }

                dataholder.print("Provider supplied render texture IDs: " + this.LeftEyeTextureId + " " + this.RightEyeTextureId);
                dataholder.print("Provider supplied texture resolution: " + eyew + " x " + eyeh);
            }

            this.checkGLError("Render Texture setup");

            if (this.framebufferEye0 == null) {
                this.framebufferEye0 = new VRTextureTarget("L Eye", eyew, eyeh, false, false, this.LeftEyeTextureId, false, true, false);
                dataholder.print(this.framebufferEye0.toString());
                this.checkGLError("Left Eye framebuffer setup");
            }

            if (this.framebufferEye1 == null) {
                this.framebufferEye1 = new VRTextureTarget("R Eye", eyew, eyeh, false, false, this.RightEyeTextureId, false, true, false);
                dataholder.print(this.framebufferEye1.toString());
                this.checkGLError("Right Eye framebuffer setup");
            }

            this.renderScale = (float) Math.sqrt((double) dataholder.vrSettings.renderScaleFactor);
            int i = (int) Math.ceil((double) ((float) eyew * this.renderScale));
            int j = (int) Math.ceil((double) ((float) eyeh * this.renderScale));
            this.framebufferVrRender = new VRTextureTarget("3D Render", i, j, true, false, -1, true, true, dataholder.vrSettings.vrUseStencil);
            WorldRenderPass.stereoXR = new WorldRenderPass((VRTextureTarget) this.framebufferVrRender);
            dataholder.print(this.framebufferVrRender.toString());
            this.checkGLError("3D framebuffer setup");
            this.mirrorFBWidth = minecraft.getWindow().getScreenWidth();
            this.mirrorFBHeight = minecraft.getWindow().getScreenHeight();

            if (dataholder.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                this.mirrorFBWidth = minecraft.getWindow().getScreenWidth() / 2;

                if (dataholder.vrSettings.mixedRealityUnityLike) {
                    this.mirrorFBHeight = minecraft.getWindow().getScreenHeight() / 2;
                }
            }

//            if (Config.isShaders()) //Optifine
//            {
//                this.mirrorFBWidth = i;
//                this.mirrorFBHeight = j;
//            }

            List<RenderPass> list = this.getRenderPasses();

            for (RenderPass renderpass : list) {
                System.out.println("Passes: " + renderpass.toString());
            }

            if (list.contains(RenderPass.THIRD) || ((Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) && IrisHelper.isShaderActive())) {
                this.framebufferMR = new VRTextureTarget("Mixed Reality Render", this.mirrorFBWidth, this.mirrorFBHeight, true, false, -1, true, false, false);
                WorldRenderPass.mixedReality = new WorldRenderPass((VRTextureTarget) this.framebufferMR);
                dataholder.print(this.framebufferMR.toString());
                this.checkGLError("Mixed reality framebuffer setup");
            }

            if (list.contains(RenderPass.CENTER) || ((Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) && IrisHelper.isShaderActive())) {
                this.framebufferUndistorted = new VRTextureTarget("Undistorted View Render", this.mirrorFBWidth, this.mirrorFBHeight, true, false, -1, false, false, false);
                WorldRenderPass.center = new WorldRenderPass((VRTextureTarget) this.framebufferUndistorted);
                dataholder.print(this.framebufferUndistorted.toString());
                this.checkGLError("Undistorted view framebuffer setup");
            }

            GuiHandler.guiFramebuffer = new VRTextureTarget("GUI", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            dataholder.print(GuiHandler.guiFramebuffer.toString());
            this.checkGLError("GUI framebuffer setup");
            KeyboardHandler.Framebuffer = new VRTextureTarget("Keyboard", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            dataholder.print(KeyboardHandler.Framebuffer.toString());
            this.checkGLError("Keyboard framebuffer setup");
            RadialHandler.Framebuffer = new VRTextureTarget("Radial Menu", GuiHandler.guiWidth, GuiHandler.guiHeight, true, false, -1, false, true, false);
            dataholder.print(RadialHandler.Framebuffer.toString());
            this.checkGLError("Radial framebuffer setup");
            int j2 = 720;
            int k2 = 720;

//            if (Config.isShaders()) //Optifine
//            {
//                j2 = i;
//                k2 = j;
//            }

            this.checkGLError("Mirror framebuffer setup");
            this.telescopeFramebufferR = new VRTextureTarget("TelescopeR", j2, k2, true, false, -1, true, false, false);
            WorldRenderPass.rightTelescope = new WorldRenderPass((VRTextureTarget) this.telescopeFramebufferR);
            dataholder.print(this.telescopeFramebufferR.toString());
            this.checkGLError("TelescopeR framebuffer setup");
            this.telescopeFramebufferR.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            this.telescopeFramebufferR.clear(Minecraft.ON_OSX);
            this.telescopeFramebufferL = new VRTextureTarget("TelescopeL", j2, k2, true, false, -1, true, false, false);
            WorldRenderPass.leftTelescope = new WorldRenderPass((VRTextureTarget) this.telescopeFramebufferL);
            dataholder.print(this.telescopeFramebufferL.toString());
            this.telescopeFramebufferL.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            this.telescopeFramebufferL.clear(Minecraft.ON_OSX);
            this.checkGLError("TelescopeL framebuffer setup");
            int j1 = Math.round(1920.0F * dataholder.vrSettings.handCameraResScale);
            int k1 = Math.round(1080.0F * dataholder.vrSettings.handCameraResScale);
            int l1 = j1;
            int i2 = k1;

//            if (Config.isShaders()) //Optifine
//            {
//                float f = (float)j1 / (float)k1;
//
//                if (f > (float)(i / j))
//                {
//                    j1 = i;
//                    k1 = Math.round((float)i / f);
//                }
//                else
//                {
//                    j1 = Math.round((float)j * f);
//                    k1 = j;
//                }
//
//                l1 = i;
//                i2 = j;
//            }

            this.cameraFramebuffer = new VRTextureTarget("Handheld Camera", j1, k1, true, false, -1, true, false, false);
            dataholder.print(this.cameraFramebuffer.toString());
            this.checkGLError("Camera framebuffer setup");
            this.cameraRenderFramebuffer = new VRTextureTarget("Handheld Camera Render", l1, i2, true, false, -1, true, true, false);
            WorldRenderPass.camera = new WorldRenderPass((VRTextureTarget) this.cameraRenderFramebuffer);
            dataholder.print(this.cameraRenderFramebuffer.toString());
            this.checkGLError("Camera render framebuffer setup");
            ((GameRendererExtension) minecraft.gameRenderer).setupClipPlanes();
            this.eyeproj[0] = this.getProjectionMatrix(0, ((GameRendererExtension) minecraft.gameRenderer).getMinClipDistance(), ((GameRendererExtension) minecraft.gameRenderer).getClipDistance());
            this.eyeproj[1] = this.getProjectionMatrix(1, ((GameRendererExtension) minecraft.gameRenderer).getMinClipDistance(), ((GameRendererExtension) minecraft.gameRenderer).getClipDistance());

            if (dataholder.vrSettings.useFsaa) {
                try {
                    this.checkGLError("pre FSAA FBO creation");
                    this.fsaaFirstPassResultFBO = new VRTextureTarget("FSAA Pass1 FBO", eyew, j, true, false, -1, false, false, false);
                    this.fsaaLastPassResultFBO = new VRTextureTarget("FSAA Pass2 FBO", eyew, eyeh, true, false, -1, false, false, false);
                    dataholder.print(this.fsaaFirstPassResultFBO.toString());
                    dataholder.print(this.fsaaLastPassResultFBO.toString());
                    this.checkGLError("FSAA FBO creation");
                    VRShaders.setupFSAA();
                    ShaderHelper.checkGLError("FBO init fsaa shader");
                } catch (Exception exception) {
                    dataholder.vrSettings.useFsaa = false;
                    dataholder.vrSettings.saveOptions();
                    System.out.println(exception.getMessage());
                    this.reinitFramebuffers = true;
                    return;
                }
            }

            try {
                minecraft.mainRenderTarget = this.framebufferVrRender;
                VRShaders.setupDepthMask();
                ShaderHelper.checkGLError("init depth shader");
                VRShaders.setupFOVReduction();
                ShaderHelper.checkGLError("init FOV shader");
                minecraft.gameRenderer.checkEntityPostEffect(minecraft.getCameraEntity());
            } catch (Exception exception1) {
                System.out.println(exception1.getMessage());
                System.exit(-1);
            }

            if (minecraft.screen != null) {
                int l2 = minecraft.getWindow().getGuiScaledWidth();
                int j3 = minecraft.getWindow().getGuiScaledHeight();
                minecraft.screen.init(minecraft, l2, j3);
            }

            long i3 = (long) (minecraft.getWindow().getScreenWidth() * minecraft.getWindow().getScreenHeight());
            long k3 = (long) (i * j * 2);

            if (list.contains(RenderPass.CENTER)) {
                k3 += i3;
            }

            if (list.contains(RenderPass.THIRD)) {
                k3 += i3;
            }

            System.out.println("[Minecrift] New render config:\nOpenVR target width: " + eyew + ", height: " + eyeh + " [" + String.format("%.1f", (float) (eyew * eyeh) / 1000000.0F) + " MP]\nRender target width: " + i + ", height: " + j + " [Render scale: " + Math.round(dataholder.vrSettings.renderScaleFactor * 100.0F) + "%, " + String.format("%.1f", (float) (i * j) / 1000000.0F) + " MP]\nMain window width: " + minecraft.getWindow().getScreenWidth() + ", height: " + minecraft.getWindow().getScreenHeight() + " [" + String.format("%.1f", (float) i3 / 1000000.0F) + " MP]\nTotal shaded pixels per frame: " + String.format("%.1f", (float) k3 / 1000000.0F) + " MP (eye stencil not accounted for)");
            this.lastDisplayFBWidth = i;
            this.lastDisplayFBHeight = j;
            this.reinitFramebuffers = false;

            if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                IrisHelper.reload();
            }

        }
    }

    public boolean wasDisplayResized() {
        Minecraft minecraft = Minecraft.getInstance();
        int i = minecraft.getWindow().getScreenHeight();
        int j = minecraft.getWindow().getScreenWidth();
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
