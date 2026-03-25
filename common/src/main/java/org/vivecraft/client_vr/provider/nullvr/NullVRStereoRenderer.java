package org.vivecraft.client_vr.provider.nullvr;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

public class NullVRStereoRenderer extends VRRenderer {

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
    public void createRenderTexture(int lwidth, int lheight) {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(this.LeftEyeTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT,
            null);

        GlStateManager._bindTexture(i);
        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        GlStateManager._bindTexture(this.RightEyeTextureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT,
            null);
        GlStateManager._bindTexture(i);
        this.lastError = RenderHelper.checkGLError("create VR textures");
    }

    @Override
    public void endFrame() {}

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public String getName() {
        return "NullVR";
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
    }
}
