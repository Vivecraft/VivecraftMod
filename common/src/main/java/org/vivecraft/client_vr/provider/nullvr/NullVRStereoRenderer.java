package org.vivecraft.client_vr.provider.nullvr;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

public class NullVRStereoRenderer extends VRRenderer {

    protected int leftEyeTextureId = -1;
    protected int rightEyeTextureId = -1;
    public RenderTarget framebufferEyeLeft;
    public RenderTarget framebufferEyeRight;

    private float lastFov = -1;
    private float lastAngle = -1;

    public NullVRStereoRenderer(MCVR vr) {
        super(vr);
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution == null) {
            this.resolution = new Tuple<>(2048, 2048);
            VRSettings.LOGGER.info("Vivecraft: NullVR Render Res {}x{}", this.resolution.getA(),
                this.resolution.getB());
            this.ss = -1.0F;
            VRSettings.LOGGER.info("Vivecraft: NullVR Supersampling: {}", this.ss);
        }
        return this.resolution;
    }

    @Override
    public Matrix4f getCachedProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (this.lastFov != ClientDataHolderVR.getInstance().vrSettings.nullvrFOV ||
            this.lastAngle != ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle)
        {
            // reset far clip plane to force a projection fetch
            this.lastFarClip = 0F;
            this.lastFov = ClientDataHolderVR.getInstance().vrSettings.nullvrFOV;
            this.lastAngle = ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle;
        }
        return super.getCachedProjectionMatrix(eyeType, nearClip, farClip);
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        return new Matrix4f().setPerspectiveOffCenter(
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrFOV,
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle * (eyeType == 0 ? -1F : 1F), 0F,
            1.0F, nearClip, farClip, RenderSystem.getDevice().isZZeroToOne());
    }

    @Override
    public void createRenderTexture(int width, int height) {
        int boundTextureId = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);

        this.leftEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.leftEyeTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_INT,
            null);

        this.rightEyeTextureId = GlStateManager._genTexture();
        GlStateManager._bindTexture(this.rightEyeTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_INT,
            null);

        this.lastError = RenderHelper.checkGLError("create VR textures");
        this.framebufferEyeLeft = VRTextureTarget.builder("L Eye")
            .withSize(width, height)
            .withTexId(this.leftEyeTextureId)
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEyeLeft);
        String leftError = RenderHelper.checkGLError("Left Eye framebuffer setup");

        this.framebufferEyeRight = VRTextureTarget.builder("R Eye")
            .withSize(width, height)
            .withTexId(this.rightEyeTextureId)
            .build();
        VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEyeRight);
        String rightError = RenderHelper.checkGLError("Right Eye framebuffer setup");

        if (this.lastError.isEmpty()) {
            this.lastError = !leftError.isEmpty() ? leftError : rightError;
        }

        GlStateManager._bindTexture(boundTextureId);
    }

    @Override
    public void endFrame() {}

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public RenderTarget getLeftEyeTarget() {
        return this.framebufferEyeLeft;
    }

    @Override
    public RenderTarget getRightEyeTarget() {
        return this.framebufferEyeRight;
    }


    @Override
    public float[] getStencilMask(RenderPass eye) {
        return null;
    }

    @Override
    public String getName() {
        return "NullVR";
    }

    @Override
    public void destroy() {
        super.destroyBuffers();
        super.destroy();

        if (this.framebufferEyeLeft != null) {
            this.framebufferEyeLeft.destroyBuffers();
            this.framebufferEyeLeft = null;
        }

        if (this.framebufferEyeRight != null) {
            this.framebufferEyeRight.destroyBuffers();
            this.framebufferEyeRight = null;
        }

        if (this.leftEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.leftEyeTextureId);
            this.leftEyeTextureId = -1;
        }

        if (this.rightEyeTextureId > -1) {
            GlStateManager._deleteTexture(this.rightEyeTextureId);
            this.rightEyeTextureId = -1;
        }
    }
}
