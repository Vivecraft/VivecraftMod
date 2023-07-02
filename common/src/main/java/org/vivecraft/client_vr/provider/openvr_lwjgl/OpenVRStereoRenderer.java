package org.vivecraft.client_vr.provider.openvr_lwjgl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.openvr.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;

import static org.lwjgl.openvr.VRCompositor.*;
import static org.lwjgl.openvr.VRSystem.*;

public class OpenVRStereoRenderer extends VRRenderer {
    private HiddenAreaMesh[] hiddenMeshes = new HiddenAreaMesh[2];
    private final MCOpenVR openvr;

    public OpenVRStereoRenderer(MCVR vr) {
        super(vr);
        this.openvr = (MCOpenVR) vr;
        hiddenMeshes[0] = HiddenAreaMesh.calloc();
        hiddenMeshes[1] = HiddenAreaMesh.calloc();
    }

    public Tuple<Integer, Integer> getRenderTextureSizes() {
        if (this.resolution != null) {
            return this.resolution;
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                var intbyreference = stack.callocInt(1);
                var intbyreference1 = stack.callocInt(1);
                VRSystem_GetRecommendedRenderTargetSize(intbyreference, intbyreference1);
                this.resolution = new Tuple<>(intbyreference.get(0), intbyreference1.get(0));
                System.out.println("OpenVR Render Res " + this.resolution.getA() + " x " + this.resolution.getB());
                this.ss = this.openvr.getSuperSampling();
                System.out.println("OpenVR Supersampling: " + this.ss);
            }

            for (int i = 0; i < 2; ++i) {
                this.hiddenMeshes[i] = VRSystem_GetHiddenAreaMesh(i, 0, this.hiddenMeshes[i]);
                int j = this.hiddenMeshes[i].unTriangleCount();

                if (j <= 0) {
                    System.out.println("No stencil mesh found for eye " + i);
                } else {
                    this.hiddenMesheVertecies[i] = new float[this.hiddenMeshes[i].unTriangleCount() * 3 * 2];
                    MemoryUtil.memFloatBuffer(MemoryUtil.memAddress(this.hiddenMeshes[i].pVertexData()), this.hiddenMesheVertecies[i].length).get(this.hiddenMesheVertecies[i]);

                    for (int k = 0; k < this.hiddenMesheVertecies[i].length; k += 2) {
                        this.hiddenMesheVertecies[i][k] *= (float) this.resolution.getA();
                        this.hiddenMesheVertecies[i][k + 1] *= (float) this.resolution.getB();
                    }

                    System.out.println("Stencil mesh loaded for eye " + i);
                }
            }

            return this.resolution;
        }
    }

    public Matrix4f getProjectionMatrix(int eyeType, float nearClip, float farClip) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (eyeType == 0) {
                return Utils.Matrix4fFromOpenVR(VRSystem_GetProjectionMatrix(0, nearClip, farClip, HmdMatrix44.calloc(stack)));
            } else {
                return Utils.Matrix4fFromOpenVR(VRSystem_GetProjectionMatrix(1, nearClip, farClip, HmdMatrix44.calloc(stack)));
            }
        }
    }

    public String getLastError() {
        return "";
    }

    public void createRenderTexture(int lwidth, int lheight) {
        this.LeftEyeTextureId = GlStateManager._genTexture();
        int i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.LeftEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType0.handle(this.LeftEyeTextureId);
        this.openvr.texType0.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType0.eType(VR.ETextureType_TextureType_OpenGL);

        this.RightEyeTextureId = GlStateManager._genTexture();
        i = GlStateManager._getInteger(GL11.GL_TEXTURE_BINDING_2D);
        RenderSystem.bindTexture(this.RightEyeTextureId);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, lwidth, lheight, 0, GL11.GL_RGBA, GL11.GL_INT, null);
        RenderSystem.bindTexture(i);
        this.openvr.texType1.handle(this.RightEyeTextureId);
        this.openvr.texType1.eColorSpace(VR.EColorSpace_ColorSpace_Gamma);
        this.openvr.texType1.eType(VR.ETextureType_TextureType_OpenGL);
    }

    public boolean endFrame(RenderPass eye) {
        return true;
    }

    public void endFrame() throws RenderConfigException {
        if (OpenVR.VRCompositor.Submit != 0) {
            int i = VRCompositor_Submit(0, this.openvr.texType0, null, 0);
            int j = VRCompositor_Submit(1, this.openvr.texType1, null, 0);
            VRCompositor_PostPresentHandoff();

            if (i + j > 0) {
                throw new RenderConfigException("Compositor Error", "Texture submission error: Left/Right " + getCompostiorError(i) + "/" + getCompostiorError(j));
            }
        }
    }

    public static String getCompostiorError(int code) {
        switch (code) {
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

    public boolean providesStencilMask() {
        return true;
    }

    public float[] getStencilMask(RenderPass eye) {
        if (this.hiddenMesheVertecies != null && (eye == RenderPass.LEFT || eye == RenderPass.RIGHT)) {
            return eye == RenderPass.LEFT ? this.hiddenMesheVertecies[0] : this.hiddenMesheVertecies[1];
        } else {
            return null;
        }
    }

    public String getName() {
        return "OpenVR";
    }

    public boolean isInitialized() {
        return this.vr.initSuccess;
    }

    public String getinitError() {
        return this.vr.initStatus;
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
