package org.vivecraft.provider.nullvr;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.VRRenderer;
import org.vivecraft.render.RenderPass;

public class NullVRStereoRenderer extends VRRenderer
{
    public NullVRStereoRenderer(MCVR vr)
    {
        super(vr);
    }

    @Override
    public Tuple<Integer, Integer> getRenderTextureSizes()
    {
        if (this.resolution != null)
        {
            return this.resolution;
        }
        else
        {
            this.resolution = new Tuple<>(1024, 1024);
            System.out.println("NullVR Render Res " + this.resolution.getA() + " x " + this.resolution.getB());
            this.ss = -1.0F;
            System.out.println("NullVR Supersampling: " + this.ss);

            return this.resolution;
        }
    }

    @Override
    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip)
    {
        return com.mojang.math.Matrix4f.perspective(90.0, 1.0F, nearClip, farClip);
    }

    @Override
    public String getLastError()
    {
        return "";
    }

    @Override
    public void createRenderTexture(int lwidth, int lheight)
    {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);

        RenderSystem.bindTexture(i);
        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);
        RenderSystem.bindTexture(i);
    }

    @Override
    public void endFrame()
    {
    }

    @Override
    public boolean providesStencilMask()
    {
        return false;
    }


    @Override
    public float[] getStencilMask(RenderPass eye)
    {
        return null;
    }

    @Override
    public String getName()
    {
        return "OpenVR";
    }

    @Override
    public boolean isInitialized()
    {
        return this.vr.initSuccess;
    }

    @Override
    public String getinitError()
    {
        return this.vr.initStatus;
    }
}
