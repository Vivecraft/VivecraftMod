package org.vivecraft.client_vr.provider.nullvr;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRTextureTarget;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.helpers.graphics.GraphicsHelper;
import org.vivecraft.client_vr.settings.VRSettings;

public class NullVRStereoRenderer extends VRRenderer {

    private float lastFov = -1;
    private float lastAngle = -1;

    public NullVRStereoRenderer(MCVR vr) {
        super(vr);
    }

    @Override
    public Vector2ic getRenderTextureSizes() {
        if (this.resolution == null) {
            this.resolution = new Vector2i(2048, 2048);
            VRSettings.LOGGER.info("Vivecraft: NullVR Render Res {}x{}", this.resolution.x(), this.resolution.y());
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
            this.lastReverseFarClip = 0F;
            this.lastFov = ClientDataHolderVR.getInstance().vrSettings.nullvrFOV;
            this.lastAngle = ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle;
        }
        return super.getCachedProjectionMatrix(eyeType, nearClip, farClip);
    }

    @Override
    public Matrix4f getCachedReverseProjectionMatrix(int eyeType, float nearClip, float farClip) {
        if (this.lastFov != ClientDataHolderVR.getInstance().vrSettings.nullvrFOV ||
            this.lastAngle != ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle)
        {
            // reset far clip plane to force a projection fetch
            this.lastFarClip = 0F;
            this.lastReverseFarClip = 0F;
            this.lastFov = ClientDataHolderVR.getInstance().vrSettings.nullvrFOV;
            this.lastAngle = ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle;
        }
        return super.getCachedReverseProjectionMatrix(eyeType, nearClip, farClip);
    }

    @Override
    protected Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        return new Matrix4f().setPerspectiveOffCenter(
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrFOV,
            Mth.DEG_TO_RAD * ClientDataHolderVR.getInstance().vrSettings.nullvrEyeAngle * (eyeType == 0 ? -1F : 1F), 0F,
            1.0F, nearClip, farClip, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
    }

    @Override
    public void createRenderTexture(int lwidth, int lheight) {
        // generate eye textures
        for (int i = 0; i < 2; i++) {
            this.framebufferEye[i] = VRTextureTarget.builder((i == 0 ? "L" : "R") + " Eye")
                .withSize(lwidth, lheight)
                .withFormat(GpuFormat.RGBA8_UNORM)
                .build();
            VRSettings.LOGGER.info("Vivecraft: {}", this.framebufferEye[i]);
            GraphicsHelper.INSTANCE.checkError((i == 0 ? "Left" : "Right") + " Eye framebuffer setup");
        }

        this.lastError = GraphicsHelper.INSTANCE.checkError("create VR textures");
    }

    @Override
    public void endFrame() {
        if (!((NullVR) this.vr).polled) {
            VRSettings.LOGGER.warn("Vivecraft: frame ended without polling new data first!");
        }

        ((NullVR) this.vr).polled = false;
    }

    @Override
    public boolean providesStencilMask() {
        return false;
    }

    @Override
    public String getName() {
        return "NullVR";
    }
}
