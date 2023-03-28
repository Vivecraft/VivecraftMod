package org.vivecraft.provider.openvr_jna;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jopenvr.HiddenAreaMesh_t;
import jopenvr.HmdMatrix44_t;
import jopenvr.VRTextureBounds_t;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.vivecraft.provider.VRRenderer;
import org.vivecraft.provider.MCVR;
import org.vivecraft.render.RenderConfigException;
import org.vivecraft.render.RenderPass;
import org.vivecraft.utils.Utils;

public class OpenVRStereoRenderer extends VRRenderer
{
    private HiddenAreaMesh_t[] hiddenMeshes = new HiddenAreaMesh_t[2];
    private MCOpenVR openvr;

    public OpenVRStereoRenderer(MCVR vr)
    {
        super(vr);
        this.openvr = (MCOpenVR)vr;
    }

    public Tuple<Integer, Integer> getRenderTextureSizes()
    {
        if (this.resolution != null)
        {
            return this.resolution;
        }
        else
        {
            IntByReference intbyreference = new IntByReference();
            IntByReference intbyreference1 = new IntByReference();
            this.openvr.vrsystem.GetRecommendedRenderTargetSize.apply(intbyreference, intbyreference1);
            this.resolution = new Tuple<>(intbyreference.getValue(), intbyreference1.getValue());
            System.out.println("OpenVR Render Res " + this.resolution.getA() + " x " + this.resolution.getB());
            this.ss = this.openvr.getSuperSampling();
            System.out.println("OpenVR Supersampling: " + this.ss);

            for (int i = 0; i < 2; ++i)
            {
                this.hiddenMeshes[i] = this.openvr.vrsystem.GetHiddenAreaMesh.apply(i, 0);
                this.hiddenMeshes[i].read();
                int j = this.hiddenMeshes[i].unTriangleCount;

                if (j <= 0)
                {
                    System.out.println("No stencil mesh found for eye " + i);
                }
                else
                {
                    this.hiddenMesheVertecies[i] = new float[this.hiddenMeshes[i].unTriangleCount * 3 * 2];
                    new Memory((long)(this.hiddenMeshes[i].unTriangleCount * 3 * 2));
                    this.hiddenMeshes[i].pVertexData.getPointer().read(0L, this.hiddenMesheVertecies[i], 0, this.hiddenMesheVertecies[i].length);

                    for (int k = 0; k < this.hiddenMesheVertecies[i].length; k += 2)
                    {
                        this.hiddenMesheVertecies[i][k] *= (float)this.resolution.getA().intValue();
                        this.hiddenMesheVertecies[i][k + 1] *= (float)this.resolution.getB().intValue();
                    }

                    System.out.println("Stencil mesh loaded for eye " + i);
                }
            }

            return this.resolution;
        }
    }

    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip)
    {
        if (eyeType == 0)
        {
            HmdMatrix44_t hmdmatrix44_t1 = this.openvr.vrsystem.GetProjectionMatrix.apply(0, nearClip, farClip);
            return Utils.Matrix4fFromOpenVR(hmdmatrix44_t1);
        }
        else
        {
            HmdMatrix44_t hmdmatrix44_t = this.openvr.vrsystem.GetProjectionMatrix.apply(1, nearClip, farClip);
            return Utils.Matrix4fFromOpenVR(hmdmatrix44_t);
        }
    }

    public String getLastError()
    {
        return "";
    }

    public void createRenderTexture(int lwidth, int lheight)
    {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType0.handle = Pointer.createConstant(this.LeftEyeTextureId);
        this.openvr.texType0.eColorSpace = 1;
        this.openvr.texType0.eType = 1;
        this.openvr.texType0.write();
        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 9729);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 9729);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType1.handle = Pointer.createConstant(this.RightEyeTextureId);
        this.openvr.texType1.eColorSpace = 1;
        this.openvr.texType1.eType = 1;
        this.openvr.texType1.write();
    }

    public boolean endFrame(RenderPass eye)
    {
        return true;
    }

    public void endFrame() throws RenderConfigException
    {
        if (this.openvr.vrCompositor.Submit != null)
        {
            int i = this.openvr.vrCompositor.Submit.apply(0, this.openvr.texType0, (VRTextureBounds_t)null, 0);
            int j = this.openvr.vrCompositor.Submit.apply(1, this.openvr.texType1, (VRTextureBounds_t)null, 0);
            this.openvr.vrCompositor.PostPresentHandoff.apply();

            if (i + j > 0)
            {
                throw new RenderConfigException("Compositor Error", "Texture submission error: Left/Right " + getCompostiorError(i) + "/" + getCompostiorError(j));
            }
        }
    }

    public static String getCompostiorError(int code)
    {
        switch (code)
        {
            case 0:
                return "None:";

            case 1:
                return "RequestFailed";

            case 100:
                return "IncompatibleVersion";

            case 101:
                return "DoesNotHaveFocus";

            case 102:
                return "InvalidTexture";

            case 103:
                return "IsNotSceneApplication";

            case 104:
                return "TextureIsOnWrongDevice";

            case 105:
                return "TextureUsesUnsupportedFormat:";

            case 106:
                return "SharedTexturesNotSupported";

            case 107:
                return "IndexOutOfRange";

            case 108:
                return "AlreadySubmitted:";

            default:
                return "Unknown";
        }
    }

    public boolean providesStencilMask()
    {
        return true;
    }

    public float[] getStencilMask(RenderPass eye)
    {
        if (this.hiddenMesheVertecies != null && (eye == RenderPass.LEFT || eye == RenderPass.RIGHT))
        {
            return eye == RenderPass.LEFT ? this.hiddenMesheVertecies[0] : this.hiddenMesheVertecies[1];
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        return "OpenVR";
    }

    public boolean isInitialized()
    {
        return this.vr.initSuccess;
    }

    public String getinitError()
    {
        return this.vr.initStatus;
    }
}
