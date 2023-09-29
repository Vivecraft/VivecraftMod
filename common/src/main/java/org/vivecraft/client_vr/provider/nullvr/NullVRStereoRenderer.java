package org.vivecraft.client_vr.provider.nullvr;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.render.RenderPass;

import static org.vivecraft.common.utils.Utils.logger;

public class NullVRStereoRenderer extends org.vivecraft.client_vr.provider.VRRenderer {
    public NullVRStereoRenderer(MCVR vr) {
        super(vr);
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution == null) {
            this.resolution = new Tuple<>(2048, 2048);
            logger.info("NullVR Render Res {} x {}", this.resolution.getA(), this.resolution.getB());
            this.ss = -1.0F;
            logger.info("NullVR Supersampling: {}", this.ss);
        }
        return this.resolution;
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, double nearClip, double farClip, Matrix4f dest) {
        return dest.setPerspective(90.0F, 1.0F, (float) nearClip, (float) farClip);
    }

    @Override
    public void createRenderTexture(int lwidth, int lheight) {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, lwidth, lheight, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);

        RenderSystem.bindTexture(i);
        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11C.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GlStateManager._texImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, lwidth, lheight, 0, GL11C.GL_RGBA, GL11C.GL_INT, null);
        RenderSystem.bindTexture(i);
    }

    @Override
    public void endFrame() {
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
        super.destroy();
        if (this.LeftEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.LeftEyeTextureId);
            this.LeftEyeTextureId = -1;
        }

        if (this.RightEyeTextureId > -1) {
            TextureUtil.releaseTextureId(this.RightEyeTextureId);
            this.RightEyeTextureId = -1;
        }
    }
}
